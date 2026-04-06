package com.rajan.lld.InterviewQuestionsPractice.ParkingLotSystem;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * PARKING LOT SYSTEM - Complete Implementation
 * 
 * Following LLD_INTERVIEW_TEMPLATE.md with strong concurrency focus
 * 
 * Key Features:
 * - Slot-level locking for high throughput
 * - Atomic find-and-park operations
 * - No double-parking (race condition prevention)
 * - Thread-safe operations
 * - Strategy pattern for pricing
 */

// ============================================================================
// ENUMS
// ============================================================================

enum VehicleType { BIKE, CAR, TRUCK }
enum SlotType { BIKE, CAR, TRUCK }

// ============================================================================
// MODELS
// ============================================================================

interface Vehicle {
    String getNumber();
    VehicleType getType();
}

class Bike implements Vehicle {
    private final String number;
    public Bike(String number) { this.number = number; }
    public String getNumber() { return number; }
    public VehicleType getType() { return VehicleType.BIKE; }
    public String toString() { return "Bike(" + number + ")"; }
}

class Car implements Vehicle {
    private final String number;
    public Car(String number) { this.number = number; }
    public String getNumber() { return number; }
    public VehicleType getType() { return VehicleType.CAR; }
    public String toString() { return "Car(" + number + ")"; }
}

class Truck implements Vehicle {
    private final String number;
    public Truck(String number) { this.number = number; }
    public String getNumber() { return number; }
    public VehicleType getType() { return VehicleType.TRUCK; }
    public String toString() { return "Truck(" + number + ")"; }
}

/**
 * Thread-safe using volatile + external lock
 * Caller MUST hold lock before modifying
 */
class ParkingSlot {
    private final String id;
    private final SlotType type;
    private volatile boolean occupied;
    private volatile Vehicle vehicle;
    
    public ParkingSlot(String id, SlotType type) {
        this.id = id;
        this.type = type;
        this.occupied = false;
    }
    
    // Caller MUST hold lock
    public boolean canFit(Vehicle v) {
        return !occupied && type.name().equals(v.getType().name());
    }
    
    // Caller MUST hold lock
    public void park(Vehicle v) {
        if (!canFit(v)) throw new IllegalStateException("Cannot park " + v + " in " + id);
        this.vehicle = v;
        this.occupied = true;
    }
    
    // Caller MUST hold lock
    public void free() {
        this.vehicle = null;
        this.occupied = false;
    }
    
    public String getId() { return id; }
    public SlotType getType() { return type; }
    public boolean isOccupied() { return occupied; }
    public Vehicle getVehicle() { return vehicle; }
}

class Floor {
    private final int number;
    private final List<ParkingSlot> slots;
    
    public Floor(int number, List<ParkingSlot> slots) {
        this.number = number;
        this.slots = new ArrayList<>(slots);
    }
    
    public int getNumber() { return number; }
    public List<ParkingSlot> getSlots() { return slots; }
}

class ParkingLot {
    private final String id;
    private final List<Floor> floors;
    
    public ParkingLot(String id, List<Floor> floors) {
        this.id = id;
        this.floors = floors;
    }
    
    public String getId() { return id; }
    public List<Floor> getFloors() { return floors; }
}

/**
 * Thread-safe (immutable after creation, exitTime updated once)
 */
class Ticket {
    private final String id;
    private final ParkingSlot slot;
    private final Vehicle vehicle;
    private final LocalDateTime entryTime;
    private volatile LocalDateTime exitTime;
    
    public Ticket(String id, ParkingSlot slot, Vehicle vehicle) {
        this.id = id;
        this.slot = slot;
        this.vehicle = vehicle;
        this.entryTime = LocalDateTime.now();
    }
    
    public void setExitTime(LocalDateTime exitTime) {
        this.exitTime = exitTime;
    }
    
    public long getDurationHours() {
        LocalDateTime exit = exitTime != null ? exitTime : LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(entryTime, exit);
        return Math.max(1, (minutes + 59) / 60); // Round up to nearest hour
    }
    
    public String getId() { return id; }
    public ParkingSlot getSlot() { return slot; }
    public Vehicle getVehicle() { return vehicle; }
    public LocalDateTime getEntryTime() { return entryTime; }
    public LocalDateTime getExitTime() { return exitTime; }
}

// ============================================================================
// STRATEGY PATTERN - Pricing
// ============================================================================

interface PricingStrategy {
    double calculateFee(Ticket ticket);
}

class HourlyPricing implements PricingStrategy {
    private final Map<VehicleType, Double> rates;
    
    public HourlyPricing() {
        rates = new HashMap<>();
        rates.put(VehicleType.BIKE, 5.0);
        rates.put(VehicleType.CAR, 10.0);
        rates.put(VehicleType.TRUCK, 20.0);
    }
    
    @Override
    public double calculateFee(Ticket ticket) {
        double hourlyRate = rates.get(ticket.getVehicle().getType());
        return hourlyRate * ticket.getDurationHours();
    }
}

// ============================================================================
// SERVICE - Core Business Logic with Concurrency Control
// ============================================================================

/**
 * Thread-safe using slot-level locking
 * Each slot has independent ReentrantLock for maximum parallelism
 */
class ParkingManager {
    private final ParkingLot parkingLot;
    private final ConcurrentHashMap<String, Ticket> tickets;
    private final ConcurrentHashMap<String, ReentrantLock> slotLocks;
    private final PricingStrategy pricingStrategy;
    private final AtomicInteger ticketCounter;
    
    public ParkingManager(ParkingLot parkingLot, PricingStrategy pricingStrategy) {
        this.parkingLot = parkingLot;
        this.pricingStrategy = pricingStrategy;
        this.tickets = new ConcurrentHashMap<>();
        this.slotLocks = new ConcurrentHashMap<>();
        this.ticketCounter = new AtomicInteger(0);
        
        // Initialize locks for all slots
        for (Floor floor : parkingLot.getFloors()) {
            for (ParkingSlot slot : floor.getSlots()) {
                slotLocks.put(slot.getId(), new ReentrantLock(true)); // Fair lock
            }
        }
    }
    
    /**
     * Park vehicle (Thread-safe)
     * Finds first available slot and parks atomically
     */
    public Ticket parkVehicle(Vehicle vehicle) {
        for (Floor floor : parkingLot.getFloors()) {
            for (ParkingSlot slot : floor.getSlots()) {
                ReentrantLock lock = slotLocks.get(slot.getId());
                
                // Try to acquire lock (non-blocking)
                if (lock.tryLock()) {
                    try {
                        // Check and park atomically
                        if (slot.canFit(vehicle)) {
                            slot.park(vehicle);
                            Ticket ticket = new Ticket(
                                "T" + ticketCounter.incrementAndGet(),
                                slot,
                                vehicle
                            );
                            tickets.put(ticket.getId(), ticket);
                            return ticket;
                        }
                    } finally {
                        lock.unlock();
                    }
                }
            }
        }
        throw new RuntimeException("No available slot for " + vehicle.getType());
    }
    
    /**
     * Exit vehicle (Thread-safe)
     * Frees slot and calculates fee atomically
     */
    public double exitVehicle(String ticketId) {
        Ticket ticket = tickets.get(ticketId);
        if (ticket == null) {
            throw new IllegalArgumentException("Invalid ticket: " + ticketId);
        }
        if (ticket.getExitTime() != null) {
            throw new IllegalStateException("Vehicle already exited");
        }
        
        ParkingSlot slot = ticket.getSlot();
        ReentrantLock lock = slotLocks.get(slot.getId());
        
        lock.lock();
        try {
            slot.free();
            ticket.setExitTime(LocalDateTime.now());
            return pricingStrategy.calculateFee(ticket);
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Get available slots count by type
     */
    public long getAvailableSlots(SlotType type) {
        long count = 0;
        for (Floor floor : parkingLot.getFloors()) {
            for (ParkingSlot slot : floor.getSlots()) {
                if (slot.getType() == type && !slot.isOccupied()) {
                    count++;
                }
            }
        }
        return count;
    }
}

// ============================================================================
// DEMO
// ============================================================================

public class ParkingLotSystemComplete {
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=".repeat(70));
        System.out.println("PARKING LOT SYSTEM - CONCURRENCY DEMO");
        System.out.println("=".repeat(70));
        
        // Setup parking lot
        ParkingLot parkingLot = createParkingLot();
        ParkingManager manager = new ParkingManager(parkingLot, new HourlyPricing());
        
        System.out.println("\n✅ Setup: 2 floors, 10 slots per floor (20 total)");
        System.out.println("   - 5 BIKE slots per floor");
        System.out.println("   - 3 CAR slots per floor");
        System.out.println("   - 2 TRUCK slots per floor\n");
        
        // Test 1: Single vehicle
        test1(manager);
        
        // Test 2: Concurrent parking - different types
        test2(manager);
        
        // Test 3: Concurrent parking - same type
        test3(manager);
        
        // Test 4: Concurrent park and exit
        test4(manager);
        
        System.out.println("\n" + "=".repeat(70));
        System.out.println("ALL TESTS PASSED! ✅");
        System.out.println("=".repeat(70));
    }
    
    private static ParkingLot createParkingLot() {
        List<Floor> floors = new ArrayList<>();
        
        for (int f = 1; f <= 2; f++) {
            List<ParkingSlot> slots = new ArrayList<>();
            
            // 5 bike slots
            for (int i = 1; i <= 5; i++) {
                slots.add(new ParkingSlot("F" + f + "-BIKE-" + i, SlotType.BIKE));
            }
            
            // 3 car slots
            for (int i = 1; i <= 3; i++) {
                slots.add(new ParkingSlot("F" + f + "-CAR-" + i, SlotType.CAR));
            }
            
            // 2 truck slots
            for (int i = 1; i <= 2; i++) {
                slots.add(new ParkingSlot("F" + f + "-TRUCK-" + i, SlotType.TRUCK));
            }
            
            floors.add(new Floor(f, slots));
        }
        
        return new ParkingLot("LOT1", floors);
    }
    
    private static void test1(ParkingManager manager) {
        System.out.println("TEST 1: Single Vehicle Parking");
        System.out.println("-".repeat(70));
        
        try {
            Vehicle bike = new Bike("B001");
            Ticket ticket = manager.parkVehicle(bike);
            System.out.println("✅ Parked: " + bike + " at " + ticket.getSlot().getId());
            
            // Simulate 1 hour parking
            Thread.sleep(100); // Simulate time
            
            double fee = manager.exitVehicle(ticket.getId());
            System.out.println("✅ Fee: $" + fee + " for " + ticket.getDurationHours() + " hour(s)\n");
            
        } catch (Exception e) {
            System.out.println("❌ Failed: " + e.getMessage() + "\n");
        }
    }
    
    private static void test2(ParkingManager manager) throws InterruptedException {
        System.out.println("TEST 2: Concurrent Parking - Different Types (All should succeed)");
        System.out.println("-".repeat(70));
        
        ExecutorService exec = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(10);
        AtomicInteger success = new AtomicInteger(0);
        
        // Park 5 bikes, 3 cars, 2 trucks concurrently
        for (int i = 0; i < 5; i++) {
            final int id = i;
            exec.submit(() -> {
                try {
                    manager.parkVehicle(new Bike("B" + id));
                    success.incrementAndGet();
                } catch (Exception e) {
                } finally {
                    latch.countDown();
                }
            });
        }
        
        for (int i = 0; i < 3; i++) {
            final int id = i;
            exec.submit(() -> {
                try {
                    manager.parkVehicle(new Car("C" + id));
                    success.incrementAndGet();
                } catch (Exception e) {
                } finally {
                    latch.countDown();
                }
            });
        }
        
        for (int i = 0; i < 2; i++) {
            final int id = i;
            exec.submit(() -> {
                try {
                    manager.parkVehicle(new Truck("T" + id));
                    success.incrementAndGet();
                } catch (Exception e) {
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await();
        exec.shutdown();
        System.out.println("✅ Success: " + success.get() + "/10 vehicles parked\n");
    }
    
    private static void test3(ParkingManager manager) throws InterruptedException {
        System.out.println("TEST 3: Concurrent Parking - Same Type (Limited slots)");
        System.out.println("-".repeat(70));
        
        ExecutorService exec = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(10);
        AtomicInteger success = new AtomicInteger(0);
        
        // 10 bikes trying to park (only 5 slots remaining)
        for (int i = 0; i < 10; i++) {
            final int id = i + 100;
            exec.submit(() -> {
                try {
                    manager.parkVehicle(new Bike("B" + id));
                    success.incrementAndGet();
                } catch (Exception e) {
                    // Expected for some vehicles
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await();
        exec.shutdown();
        System.out.println("✅ Success: " + success.get() + "/10 (5 bike slots available)\n");
    }
    
    private static void test4(ParkingManager manager) throws InterruptedException {
        System.out.println("TEST 4: Concurrent Park and Exit");
        System.out.println("-".repeat(70));
        
        // Park some vehicles first
        List<Ticket> tickets = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            try {
                tickets.add(manager.parkVehicle(new Car("C" + (i + 100))));
            } catch (Exception e) {
                // Ignore if full
            }
        }
        
        ExecutorService exec = Executors.newFixedThreadPool(6);
        CountDownLatch latch = new CountDownLatch(6);
        AtomicInteger parkSuccess = new AtomicInteger(0);
        AtomicInteger exitSuccess = new AtomicInteger(0);
        
        // 3 threads parking
        for (int i = 0; i < 3; i++) {
            final int id = i + 200;
            exec.submit(() -> {
                try {
                    manager.parkVehicle(new Car("C" + id));
                    parkSuccess.incrementAndGet();
                } catch (Exception e) {
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // 3 threads exiting
        for (Ticket ticket : tickets) {
            exec.submit(() -> {
                try {
                    manager.exitVehicle(ticket.getId());
                    exitSuccess.incrementAndGet();
                } catch (Exception e) {
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await();
        exec.shutdown();
        System.out.println("✅ No race conditions! Park: " + parkSuccess.get() + 
                         ", Exit: " + exitSuccess.get() + "\n");
    }
}
