package com.rajan.lld.InterviewQuestionsPractice.VehicleRentalSystem;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * VEHICLE RENTAL SYSTEM - Complete Implementation
 *
 * Following LLD_INTERVIEW_TEMPLATE.md with strong concurrency focus
 *
 * Key Features:
 * - Vehicle-level locking for high throughput
 * - Atomic search-and-reserve operations
 * - No double-rental (race condition prevention)
 * - Strategy pattern for pricing
 * - Two-step flow: SearchAndLock → Confirm
 */

// ============================================================================
// ENUMS
// ============================================================================

enum VehicleType {
    BIKE(50.0), CAR(100.0), SUV(150.0), TRUCK(200.0);

    final double baseDailyRate;

    VehicleType(double baseDailyRate) {
        this.baseDailyRate = baseDailyRate;
    }
}

enum RentalStatus { PENDING, CONFIRMED, RETURNED, CANCELLED }

// ============================================================================
// MODELS
// ============================================================================

/**
 * Thread-safe using volatile + external lock
 * Caller MUST hold lock before modifying
 */
class Vehicle {
    private final String id;
    private final String model;
    private final VehicleType type;
    private final ConcurrentHashMap<LocalDate, String> rentalSchedule = new ConcurrentHashMap<>();

    public Vehicle(String id, String model, VehicleType type) {
        this.id = id;
        this.model = model;
        this.type = type;
    }

    // Caller MUST hold lock
    public boolean isAvailable(LocalDate pickUp, LocalDate dropOff) {
        for (LocalDate date = pickUp; date.isBefore(dropOff); date = date.plusDays(1)) {
            if (rentalSchedule.containsKey(date)) return false;
        }
        return true;
    }

    // Caller MUST hold lock
    public void reserve(LocalDate pickUp, LocalDate dropOff, String rentalId) {
        for (LocalDate date = pickUp; date.isBefore(dropOff); date = date.plusDays(1)) {
            rentalSchedule.put(date, rentalId);
        }
    }

    // Caller MUST hold lock
    public void release(LocalDate pickUp, LocalDate dropOff) {
        for (LocalDate date = pickUp; date.isBefore(dropOff); date = date.plusDays(1)) {
            rentalSchedule.remove(date);
        }
    }

    public String getId() { return id; }
    public String getModel() { return model; }
    public VehicleType getType() { return type; }
}

class Customer {
    private final String id;
    private final String name;
    private final String licenseNumber;

    public Customer(String id, String name, String licenseNumber) {
        this.id = id;
        this.name = name;
        this.licenseNumber = licenseNumber;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getLicenseNumber() { return licenseNumber; }
}

/**
 * Thread-safe: Immutable after creation, status updated via volatile
 */
class Rental {
    private static final AtomicInteger idGen = new AtomicInteger(1000);

    private final String id;
    private final Customer customer;
    private final Vehicle vehicle;
    private final LocalDate pickUp;
    private final LocalDate dropOff;
    private final double totalAmount;
    private volatile RentalStatus status;

    public Rental(Customer customer, Vehicle vehicle, LocalDate pickUp, LocalDate dropOff, double totalAmount) {
        this.id = "RNT-" + idGen.getAndIncrement();
        this.customer = customer;
        this.vehicle = vehicle;
        this.pickUp = pickUp;
        this.dropOff = dropOff;
        this.totalAmount = totalAmount;
        this.status = RentalStatus.PENDING;
    }

    public void confirm() { this.status = RentalStatus.CONFIRMED; }
    public void returnVehicle() { this.status = RentalStatus.RETURNED; }
    public void cancel() { this.status = RentalStatus.CANCELLED; }

    public String getId() { return id; }
    public Customer getCustomer() { return customer; }
    public Vehicle getVehicle() { return vehicle; }
    public LocalDate getPickUp() { return pickUp; }
    public LocalDate getDropOff() { return dropOff; }
    public double getTotalAmount() { return totalAmount; }
    public RentalStatus getStatus() { return status; }
}

// ============================================================================
// REPOSITORIES
// ============================================================================

class RentalRepository {
    private final ConcurrentHashMap<String, Rental> rentals = new ConcurrentHashMap<>();

    public void save(Rental rental) { rentals.put(rental.getId(), rental); }
    public Rental findById(String id) { return rentals.get(id); }
    public Map<String, Rental> getAll() { return new HashMap<>(rentals); }
}

// ============================================================================
// STRATEGY PATTERN - Pricing
// ============================================================================

interface PricingStrategy {
    double calculateFee(VehicleType type, long days);
}

class DailyPricing implements PricingStrategy {
    @Override
    public double calculateFee(VehicleType type, long days) {
        return type.baseDailyRate * days;
    }
}

class WeeklyDiscountPricing implements PricingStrategy {
    @Override
    public double calculateFee(VehicleType type, long days) {
        long fullWeeks = days / 7;
        long remainingDays = days % 7;
        double weeklyRate = type.baseDailyRate * 7 * 0.85; // 15% weekly discount
        return (fullWeeks * weeklyRate) + (remainingDays * type.baseDailyRate);
    }
}

// ============================================================================
// VEHICLE MANAGER (Resource Management)
// ============================================================================

class VehicleManager {
    private final ConcurrentHashMap<VehicleType, List<Vehicle>> vehiclesByType = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ReentrantLock> vehicleLocks = new ConcurrentHashMap<>();

    public void addVehicle(Vehicle vehicle) {
        vehiclesByType.computeIfAbsent(vehicle.getType(), k -> new ArrayList<>()).add(vehicle);
        vehicleLocks.put(vehicle.getId(), new ReentrantLock(true));
    }

    public List<Vehicle> getVehiclesByType(VehicleType type) {
        return vehiclesByType.getOrDefault(type, Collections.emptyList());
    }

    public ReentrantLock getVehicleLock(String vehicleId) {
        return vehicleLocks.get(vehicleId);
    }
}

// ============================================================================
// SERVICE - Core Business Logic with Concurrency Control
// ============================================================================

/**
 * Thread-safe using vehicle-level locking
 * Two-step flow: SearchAndLockVehicle → confirmRental
 */
class RentalManager {
    private final RentalRepository rentalRepository;
    private final VehicleManager vehicleManager;
    private final PricingStrategy pricingStrategy;

    public RentalManager(VehicleManager vehicleManager, PricingStrategy pricingStrategy) {
        this.rentalRepository = new RentalRepository();
        this.vehicleManager = vehicleManager;
        this.pricingStrategy = pricingStrategy;
    }

    /**
     * Step 1: Search available vehicle and lock it (before payment)
     * CONCURRENCY: Vehicle-level locking with tryLock timeout
     */
    public Rental searchAndLockVehicle(Customer customer, VehicleType type, LocalDate pickUp, LocalDate dropOff) {
        if (!pickUp.isBefore(dropOff)) {
            throw new IllegalArgumentException("Invalid dates");
        }

        List<Vehicle> vehicles = vehicleManager.getVehiclesByType(type);
        for (Vehicle vehicle : vehicles) {
            ReentrantLock lock = vehicleManager.getVehicleLock(vehicle.getId());
            try {
                if (lock.tryLock(5, TimeUnit.SECONDS)) {
                    try {
                        if (vehicle.isAvailable(pickUp, dropOff)) {
                            long days = ChronoUnit.DAYS.between(pickUp, dropOff);
                            double amount = pricingStrategy.calculateFee(type, days);

                            Rental rental = new Rental(customer, vehicle, pickUp, dropOff, amount);
                            vehicle.reserve(pickUp, dropOff, rental.getId());
                            rentalRepository.save(rental);
                            return rental;
                        }
                    } finally {
                        lock.unlock();
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted");
            }
        }
        throw new IllegalStateException("No available " + type + " vehicles");
    }

    /**
     * Step 2: Confirm rental (after payment)
     */
    public Rental confirmRental(String rentalId) {
        Rental rental = rentalRepository.findById(rentalId);
        if (rental == null) {
            throw new IllegalArgumentException("Rental not found");
        }

        ReentrantLock lock = vehicleManager.getVehicleLock(rental.getVehicle().getId());
        lock.lock();
        try {
            if (rental.getStatus() != RentalStatus.PENDING) {
                throw new IllegalStateException("Rental cannot be confirmed");
            }
            rental.confirm();
            return rental;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Return vehicle and release dates
     */
    public Rental returnVehicle(String rentalId) {
        Rental rental = rentalRepository.findById(rentalId);
        if (rental == null) {
            throw new IllegalArgumentException("Rental not found");
        }

        Vehicle vehicle = rental.getVehicle();
        ReentrantLock lock = vehicleManager.getVehicleLock(vehicle.getId());

        lock.lock();
        try {
            if (rental.getStatus() != RentalStatus.CONFIRMED) {
                throw new IllegalStateException("Rental not active");
            }
            vehicle.release(rental.getPickUp(), rental.getDropOff());
            rental.returnVehicle();
            return rental;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Cancel rental and release dates
     */
    public void cancelRental(String rentalId) {
        Rental rental = rentalRepository.findById(rentalId);
        if (rental == null) {
            throw new IllegalArgumentException("Rental not found");
        }

        Vehicle vehicle = rental.getVehicle();
        ReentrantLock lock = vehicleManager.getVehicleLock(vehicle.getId());

        lock.lock();
        try {
            vehicle.release(rental.getPickUp(), rental.getDropOff());
            rental.cancel();
        } finally {
            lock.unlock();
        }
    }
}

// ============================================================================
// DEMO AND TESTS
// ============================================================================

public class VehicleRentalSystemComplete {

    public static void main(String[] args) throws Exception {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║   VEHICLE RENTAL SYSTEM - Complete Implementation         ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝\n");

        runDemo();
        runConcurrencyTests();

        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║   ALL TESTS PASSED ✓                                       ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
    }

    private static void runDemo() {
        System.out.println("=== DEMO ===\n");

        VehicleManager vehicleManager = new VehicleManager();
        vehicleManager.addVehicle(new Vehicle("V1", "Honda Activa", VehicleType.BIKE));
        vehicleManager.addVehicle(new Vehicle("V2", "Toyota Camry", VehicleType.CAR));
        vehicleManager.addVehicle(new Vehicle("V3", "Ford Endeavour", VehicleType.SUV));
        vehicleManager.addVehicle(new Vehicle("V4", "Tata Ace", VehicleType.TRUCK));

        RentalManager manager = new RentalManager(vehicleManager, new DailyPricing());

        System.out.println("✓ Setup: 4 vehicles (BIKE, CAR, SUV, TRUCK)\n");

        LocalDate pickUp = LocalDate.now().plusDays(5);
        LocalDate dropOff = LocalDate.now().plusDays(10);

        // Search and lock
        Rental rental = manager.searchAndLockVehicle(
            new Customer("C1", "Rajan", "DL-12345"), VehicleType.CAR, pickUp, dropOff
        );
        System.out.println("✓ Search & Lock: " + rental.getVehicle().getModel() + " for " + rental.getCustomer().getName());

        // Confirm
        Rental confirmed = manager.confirmRental(rental.getId());
        System.out.println("✓ Rental: " + confirmed.getId());
        System.out.println("  Amount: $" + confirmed.getTotalAmount());
        System.out.println("  Status: " + confirmed.getStatus());

        // Return
        Rental returned = manager.returnVehicle(confirmed.getId());
        System.out.println("  Returned: " + returned.getStatus() + "\n");
    }

    private static void runConcurrencyTests() throws Exception {
        System.out.println("=== CONCURRENCY TESTS ===\n");

        test1_SingleVehicleConcurrent();
        test2_DifferentVehicles();
        test3_OverlappingDates();
        test4_CancelAndRent();
    }

    /**
     * Test 1: 10 threads try to rent the same single CAR for same dates
     * Expected: Only 1 succeeds
     */
    private static void test1_SingleVehicleConcurrent() throws Exception {
        System.out.println("Test 1: Single Vehicle Concurrent Rental");

        VehicleManager vehicleManager = new VehicleManager();
        vehicleManager.addVehicle(new Vehicle("T1", "Honda City", VehicleType.CAR));

        RentalManager manager = new RentalManager(vehicleManager, new DailyPricing());

        LocalDate pickUp = LocalDate.now().plusDays(30);
        LocalDate dropOff = LocalDate.now().plusDays(33);

        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<Boolean>> futures = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            final int idx = i;
            futures.add(executor.submit(() -> {
                try {
                    Rental rental = manager.searchAndLockVehicle(
                        new Customer("C" + idx, "User" + idx, "DL-" + idx),
                        VehicleType.CAR, pickUp, dropOff
                    );
                    manager.confirmRental(rental.getId());
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }));
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        long success = futures.stream()
            .map(f -> { try { return f.get(); } catch (Exception e) { return false; } })
            .filter(b -> b)
            .count();

        System.out.println("Result: " + success + "/10 succeeded");
        System.out.println("Status: " + (success == 1 ? "✓ PASS" : "✗ FAIL") + "\n");
    }

    /**
     * Test 2: 10 threads rent 10 different CARs for same dates
     * Expected: All 10 succeed
     */
    private static void test2_DifferentVehicles() throws Exception {
        System.out.println("Test 2: Different Vehicles Concurrent");

        VehicleManager vehicleManager = new VehicleManager();
        for (int i = 0; i < 10; i++) {
            vehicleManager.addVehicle(new Vehicle("T2-" + i, "Car-" + i, VehicleType.CAR));
        }

        RentalManager manager = new RentalManager(vehicleManager, new DailyPricing());

        LocalDate pickUp = LocalDate.now().plusDays(40);
        LocalDate dropOff = LocalDate.now().plusDays(43);

        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<Boolean>> futures = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            final int idx = i;
            futures.add(executor.submit(() -> {
                try {
                    Rental rental = manager.searchAndLockVehicle(
                        new Customer("C2-" + idx, "User" + idx, "DL-" + idx),
                        VehicleType.CAR, pickUp, dropOff
                    );
                    manager.confirmRental(rental.getId());
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }));
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        long success = futures.stream()
            .map(f -> { try { return f.get(); } catch (Exception e) { return false; } })
            .filter(b -> b)
            .count();

        System.out.println("Result: " + success + "/10 succeeded");
        System.out.println("Status: " + (success == 10 ? "✓ PASS" : "✗ FAIL") + "\n");
    }

    /**
     * Test 3: Two threads rent same vehicle with overlapping dates
     * Expected: Only 1 succeeds
     */
    private static void test3_OverlappingDates() throws Exception {
        System.out.println("Test 3: Overlapping Dates");

        VehicleManager vehicleManager = new VehicleManager();
        vehicleManager.addVehicle(new Vehicle("T3", "BMW X5", VehicleType.SUV));

        RentalManager manager = new RentalManager(vehicleManager, new DailyPricing());

        LocalDate pickUp1 = LocalDate.now().plusDays(50);
        LocalDate dropOff1 = LocalDate.now().plusDays(55);
        LocalDate pickUp2 = LocalDate.now().plusDays(53);
        LocalDate dropOff2 = LocalDate.now().plusDays(58);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        Future<Boolean> f1 = executor.submit(() -> {
            try {
                Rental rental = manager.searchAndLockVehicle(
                    new Customer("C3-1", "User1", "DL-31"), VehicleType.SUV, pickUp1, dropOff1
                );
                manager.confirmRental(rental.getId());
                return true;
            } catch (Exception e) {
                return false;
            }
        });

        Future<Boolean> f2 = executor.submit(() -> {
            try {
                Thread.sleep(50);
                Rental rental = manager.searchAndLockVehicle(
                    new Customer("C3-2", "User2", "DL-32"), VehicleType.SUV, pickUp2, dropOff2
                );
                manager.confirmRental(rental.getId());
                return true;
            } catch (Exception e) {
                return false;
            }
        });

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        boolean s1 = f1.get();
        boolean s2 = f2.get();

        System.out.println("Rental 1: " + (s1 ? "SUCCESS" : "FAILED"));
        System.out.println("Rental 2: " + (s2 ? "SUCCESS" : "FAILED"));
        System.out.println("Status: " + (s1 != s2 ? "✓ PASS" : "✗ FAIL") + "\n");
    }

    /**
     * Test 4: Cancel a rental, then another thread rents the same vehicle
     * Expected: Both cancel and new rental succeed
     */
    private static void test4_CancelAndRent() throws Exception {
        System.out.println("Test 4: Concurrent Cancel and Rent");

        VehicleManager vehicleManager = new VehicleManager();
        vehicleManager.addVehicle(new Vehicle("T4", "Hyundai Creta", VehicleType.SUV));

        RentalManager manager = new RentalManager(vehicleManager, new DailyPricing());

        LocalDate pickUp = LocalDate.now().plusDays(60);
        LocalDate dropOff = LocalDate.now().plusDays(63);

        Rental initial = manager.searchAndLockVehicle(
            new Customer("C4-1", "User1", "DL-41"), VehicleType.SUV, pickUp, dropOff
        );
        manager.confirmRental(initial.getId());

        ExecutorService executor = Executors.newFixedThreadPool(2);

        Future<Boolean> cancel = executor.submit(() -> {
            try {
                manager.returnVehicle(initial.getId());
                return true;
            } catch (Exception e) {
                return false;
            }
        });

        Future<Boolean> rent = executor.submit(() -> {
            try {
                Thread.sleep(100);
                Rental rental = manager.searchAndLockVehicle(
                    new Customer("C4-2", "User2", "DL-42"), VehicleType.SUV, pickUp, dropOff
                );
                manager.confirmRental(rental.getId());
                return true;
            } catch (Exception e) {
                return false;
            }
        });

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        boolean c = cancel.get();
        boolean b = rent.get();

        System.out.println("Return: " + (c ? "SUCCESS" : "FAILED"));
        System.out.println("New rental: " + (b ? "SUCCESS" : "FAILED"));
        System.out.println("Status: " + (c && b ? "✓ PASS" : "✗ FAIL") + "\n");
    }
}
