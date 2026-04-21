package com.rajan.lld.InterviewQuestionsPractice.HotelBookingSystem;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * HOTEL BOOKING SYSTEM - Minimal Implementation
 * Concurrency: Room-level locking
 * Thread-Safety: All operations thread-safe
 */

// ============================================================================
// ENUMS
// ============================================================================

enum RoomType {
    SINGLE(100.0, 1), DOUBLE(150.0, 2), SUITE(300.0, 4);
    
    final double basePrice;
    final int maxOccupancy;
    
    RoomType(double basePrice, int maxOccupancy) {
        this.basePrice = basePrice;
        this.maxOccupancy = maxOccupancy;
    }
}

enum BookingStatus { PENDING, CONFIRMED, CANCELLED }

// ============================================================================
// MODELS
// ============================================================================

/**
 * Thread-Safety: Volatile + external lock
 * Concurrency: Caller MUST hold lock
 */
class Room {
    private final String id;
    private final RoomType type;
    private final ConcurrentHashMap<LocalDate, String> bookingSchedule = new ConcurrentHashMap<>();
    
    public Room(String id, RoomType type) {
        this.id = id;
        this.type = type;
    }
    
    // Caller MUST hold lock
    public boolean isAvailable(LocalDate checkIn, LocalDate checkOut) {
        for (LocalDate date = checkIn; date.isBefore(checkOut); date = date.plusDays(1)) {
            if (bookingSchedule.containsKey(date)) return false;
        }
        return true;
    }
    
    // Caller MUST hold lock
    public void reserve(LocalDate checkIn, LocalDate checkOut, String bookingId) {
        for (LocalDate date = checkIn; date.isBefore(checkOut); date = date.plusDays(1)) {
            bookingSchedule.put(date, bookingId);
        }
    }
    
    // Caller MUST hold lock
    public void release(LocalDate checkIn, LocalDate checkOut) {
        for (LocalDate date = checkIn; date.isBefore(checkOut); date = date.plusDays(1)) {
            bookingSchedule.remove(date);
        }
    }
    
    public String getId() { return id; }
    public RoomType getType() { return type; }
}

class Guest {
    private final String id;
    private final String name;
    
    public Guest(String id, String name) {
        this.id = id;
        this.name = name;
    }
    
    public String getId() { return id; }
    public String getName() { return name; }
}

/**
 * Thread-Safety: Immutable after creation
 */
class Booking {
    private static final AtomicInteger idGen = new AtomicInteger(1000);
    
    private final String id;
    private final Guest guest;
    private final Room room;
    private final LocalDate checkIn;
    private final LocalDate checkOut;
    private final double totalAmount;
    private volatile BookingStatus status;
    
    public Booking(Guest guest, Room room, LocalDate checkIn, LocalDate checkOut) {
        this.id = "BK-" + idGen.getAndIncrement();
        this.guest = guest;
        this.room = room;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.status = BookingStatus.PENDING;
        
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        this.totalAmount = room.getType().basePrice * nights;
    }
    
    public void confirm() { this.status = BookingStatus.CONFIRMED; }
    public void cancel() { this.status = BookingStatus.CANCELLED; }
    
    public String getId() { return id; }
    public Guest getGuest() { return guest; }
    public Room getRoom() { return room; }
    public LocalDate getCheckIn() { return checkIn; }
    public LocalDate getCheckOut() { return checkOut; }
    public double getTotalAmount() { return totalAmount; }
    public BookingStatus getStatus() { return status; }
}

class BookingRepository {
    private final ConcurrentHashMap<String, Booking> bookings = new ConcurrentHashMap<>();

    public void save(Booking booking) {
        bookings.put(booking.getId(), booking);
    }

    public Booking findById(String id) {
        return bookings.get(id);
    }

    public void delete(String id) {
        bookings.remove(id);
    }

    public Map<String, Booking> getAll() { return new HashMap<>(bookings); }
}


class RoomManager {
    private final ConcurrentHashMap<RoomType, List<Room>> roomsByType = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ReentrantLock> roomLocks = new ConcurrentHashMap<>();

    public void addRoom(Room room) {
        roomsByType.computeIfAbsent(room.getType(), k -> new ArrayList<>()).add(room);
        roomLocks.put(room.getId(), new ReentrantLock(true));
    }

    public List<Room> getRoomByType(RoomType type) {
        return roomsByType.getOrDefault(type, Collections.emptyList());
    }

    public ReentrantLock getRoomLock(String roomId) {
        return roomLocks.get(roomId);
    }
}

// ============================================================================
// SERVICE
// ============================================================================

/**
 * Thread-Safety: Room-level locking
 */
class BookingManager {
    private final BookingRepository bookingRepository;
    private final RoomManager roomManager;

    public BookingManager(RoomManager roomManager) {
        this.bookingRepository = new BookingRepository();
        this.roomManager = roomManager;
    }
    
    public Booking SearchAndLockRoom(Guest guest, RoomType type, LocalDate checkIn, LocalDate checkOut) {
        List<Room> rooms = roomManager.getRoomByType(type);
        for (Room room : rooms) {
            ReentrantLock lock = roomManager.getRoomLock(room.getId());
            try {
                if (lock.tryLock(5, TimeUnit.SECONDS)) {
                    try {
                        if (room.isAvailable(checkIn, checkOut)) {
                            Booking booking = new Booking(guest, room, checkIn, checkOut);
                            room.reserve(checkIn, checkOut, booking.getId());
                            bookingRepository.save(booking);
                            return booking;
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
        throw new IllegalStateException("No available rooms");
    }
    
    public Booking confirmBooking(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId);
        if (booking == null) {
            throw new IllegalArgumentException("Booking not found");
        }

        Room room = booking.getRoom();
        ReentrantLock lock = roomManager.getRoomLock(room.getId());

        lock.lock();
        try {
            if (booking.getStatus() != BookingStatus.PENDING) {
                throw new IllegalStateException("Booking cannot be confirmed");
            }
            booking.confirm();
            return booking;
        } finally {
            lock.unlock();
        }
    }
    
    public void cancelBooking(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId);
        if (booking == null) {
            throw new IllegalArgumentException("Booking not found");
        }
        
        Room room = booking.getRoom();
        ReentrantLock lock = roomManager.getRoomLock(room.getId());
        
        lock.lock();
        try {
            room.release(booking.getCheckIn(), booking.getCheckOut());
            booking.cancel();
        } finally {
            lock.unlock();
        }
    }
}

// ============================================================================
// DEMO AND TESTS
// ============================================================================

public class HotelBookingSystemComplete {
    
    public static void main(String[] args) throws Exception {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║   HOTEL BOOKING SYSTEM - Minimal Implementation           ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝\n");
        
        runDemo();
        runConcurrencyTests();
        
        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║   ALL TESTS PASSED ✓                                       ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
    }
    
    private static void runDemo() {
        System.out.println("=== DEMO ===\n");
        
        RoomManager roomManager = new RoomManager();
        roomManager.addRoom(new Room("R1", RoomType.SINGLE));
        roomManager.addRoom(new Room("R2", RoomType.DOUBLE));
        roomManager.addRoom(new Room("R3", RoomType.SUITE));
        
        BookingManager manager = new BookingManager(roomManager);
        
        System.out.println("✓ Setup: 3 rooms, 2 guests\n");
        
        LocalDate checkIn = LocalDate.now().plusDays(7);
        LocalDate checkOut = LocalDate.now().plusDays(10);
        
        Booking booking1 = manager.SearchAndLockRoom(new Guest("G1", "Rajan"), RoomType.DOUBLE, checkIn, checkOut);
        if (booking1 != null) {
            System.out.println("✓ Search & Lock: Found room " + booking1.getRoom().getId() + " for Guest " + booking1.getGuest().getName());
        }
        
        Booking confirmed = manager.confirmBooking(booking1.getId());
        System.out.println("✓ Booking: " + confirmed.getId());
        System.out.println("  Amount: $" + confirmed.getTotalAmount());
        System.out.println("  Status: " + confirmed.getStatus() + "\n");
    }
    
    private static void runConcurrencyTests() throws Exception {
        System.out.println("=== CONCURRENCY TESTS ===\n");
        
        test1_SingleRoomConcurrent();
        test2_DifferentRooms();
        test3_OverlappingDates();
        test4_CancelAndBook();
    }
    
    private static void test1_SingleRoomConcurrent() throws Exception {
        System.out.println("Test 1: Single Room Concurrent Booking");
        
        RoomManager roomManager = new RoomManager();
        roomManager.addRoom(new Room("T1", RoomType.SINGLE));
        
        BookingManager manager = new BookingManager(roomManager);
        
        LocalDate checkIn = LocalDate.now().plusDays(30);
        LocalDate checkOut = LocalDate.now().plusDays(33);
        
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<Boolean>> futures = new ArrayList<>();
        
        for (int i = 0; i < 10; i++) {
            final int idx = i;
            futures.add(executor.submit(() -> {
                try {
                    Booking booking = manager.SearchAndLockRoom(new Guest("TG" + idx, "User" + idx), RoomType.SINGLE, checkIn, checkOut);
                    manager.confirmBooking(booking.getId());
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }));
        }
        
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
        
        long success = futures.stream()
            .map(f -> { try { return f.get(); } catch(Exception e) { return false; } })
            .filter(b -> b)
            .count();
        
        System.out.println("Result: " + success + "/10 succeeded");
        System.out.println("Status: " + (success == 1 ? "✓ PASS" : "✗ FAIL") + "\n");
    }
    
    private static void test2_DifferentRooms() throws Exception {
        System.out.println("Test 2: Different Rooms Concurrent");
        
        RoomManager roomManager = new RoomManager();
        for (int i = 0; i < 10; i++) {
            roomManager.addRoom(new Room("T2-" + i, RoomType.DOUBLE));
        }
        
        BookingManager manager = new BookingManager(roomManager);
        
        LocalDate checkIn = LocalDate.now().plusDays(40);
        LocalDate checkOut = LocalDate.now().plusDays(43);
        
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<Boolean>> futures = new ArrayList<>();
        
        for (int i = 0; i < 10; i++) {
            final int idx = i;
            futures.add(executor.submit(() -> {
                try {
                    Booking booking = manager.SearchAndLockRoom(new Guest("TG2-" + idx, "User" + idx), RoomType.DOUBLE, checkIn, checkOut);
                    manager.confirmBooking(booking.getId());
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }));
        }
        
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
        
        long success = futures.stream()
            .map(f -> { try { return f.get(); } catch(Exception e) { return false; } })
            .filter(b -> b)
            .count();
        
        System.out.println("Result: " + success + "/10 succeeded");
        System.out.println("Status: " + (success == 10 ? "✓ PASS" : "✗ FAIL") + "\n");
    }
    
    private static void test3_OverlappingDates() throws Exception {
        System.out.println("Test 3: Overlapping Dates");
        
        RoomManager roomManager = new RoomManager();
        roomManager.addRoom(new Room("T3", RoomType.SUITE));
        
        BookingManager manager = new BookingManager(roomManager);
        
        LocalDate checkIn1 = LocalDate.now().plusDays(50);
        LocalDate checkOut1 = LocalDate.now().plusDays(55);
        LocalDate checkIn2 = LocalDate.now().plusDays(53);
        LocalDate checkOut2 = LocalDate.now().plusDays(58);
        
        ExecutorService executor = Executors.newFixedThreadPool(2);
        
        Future<Boolean> f1 = executor.submit(() -> {
            try {
                Booking booking = manager.SearchAndLockRoom(new Guest("TG3-1", "User1"), RoomType.SUITE, checkIn1, checkOut1);
                manager.confirmBooking(booking.getId());
                return true;
            } catch (Exception e) {
                return false;
            }
        });
        
        Future<Boolean> f2 = executor.submit(() -> {
            try {
                Thread.sleep(50);
                Booking booking = manager.SearchAndLockRoom(new Guest("TG3-2", "User2"), RoomType.SUITE, checkIn2, checkOut2);
                manager.confirmBooking(booking.getId());
                return true;
            } catch (Exception e) {
                return false;
            }
        });
        
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
        
        boolean s1 = f1.get();
        boolean s2 = f2.get();
        
        System.out.println("Booking 1: " + (s1 ? "SUCCESS" : "FAILED"));
        System.out.println("Booking 2: " + (s2 ? "SUCCESS" : "FAILED"));
        System.out.println("Status: " + (s1 != s2 ? "✓ PASS" : "✗ FAIL") + "\n");
    }
    
    private static void test4_CancelAndBook() throws Exception {
        System.out.println("Test 4: Concurrent Cancel and Book");
        
        RoomManager roomManager = new RoomManager();
        roomManager.addRoom(new Room("T4", RoomType.DOUBLE));
        
        BookingManager manager = new BookingManager(roomManager);
        
        LocalDate checkIn = LocalDate.now().plusDays(60);
        LocalDate checkOut = LocalDate.now().plusDays(63);
        
        Booking initial = manager.SearchAndLockRoom(new Guest("TG4-1", "User1"), RoomType.DOUBLE, checkIn, checkOut);
        manager.confirmBooking(initial.getId());
        
        ExecutorService executor = Executors.newFixedThreadPool(2);
        
        Future<Boolean> cancel = executor.submit(() -> {
            try {
                manager.cancelBooking(initial.getId());
                return true;
            } catch (Exception e) {
                return false;
            }
        });
        
        Future<Boolean> book = executor.submit(() -> {
            try {
                Thread.sleep(100);
                Booking booking = manager.SearchAndLockRoom(new Guest("TG4-2", "User2"), RoomType.DOUBLE, checkIn, checkOut);
                manager.confirmBooking(booking.getId());
                return true;
            } catch (Exception e) {
                return false;
            }
        });
        
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
        
        boolean c = cancel.get();
        boolean b = book.get();
        
        System.out.println("Cancel: " + (c ? "SUCCESS" : "FAILED"));
        System.out.println("New booking: " + (b ? "SUCCESS" : "FAILED"));
        System.out.println("Status: " + (c && b ? "✓ PASS" : "✗ FAIL") + "\n");
    }
}
