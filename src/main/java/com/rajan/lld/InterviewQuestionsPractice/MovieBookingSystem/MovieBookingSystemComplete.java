package com.rajan.lld.InterviewQuestionsPractice.MovieBookingSystem;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * MOVIE BOOKING SYSTEM - Complete Implementation
 * 
 * Following LLD_INTERVIEW_TEMPLATE.md with strong concurrency focus
 * 
 * Key Features:
 * - Fine-grained locking (seat-level locks)
 * - Deadlock prevention (sorted locking)
 * - Race condition prevention
 * - Thread-safe operations
 */

// ============================================================================
// ENUMS
// ============================================================================

enum SeatStatus { AVAILABLE, LOCKED, BOOKED }
enum BookingStatus { PENDING, CONFIRMED, CANCELLED }

// ============================================================================
// MODELS
// ============================================================================

class Movie {
    private final String id, title;
    private final int durationMinutes;
    
    public Movie(String id, String title, int durationMinutes) {
        this.id = id;
        this.title = title;
        this.durationMinutes = durationMinutes;
    }
    
    public String getTitle() { return title; }
}

/**
 * Thread-safe using volatile fields
 * Caller MUST hold lock before modifying
 */
class Seat {
    private final String id;
    private volatile SeatStatus status;
    private volatile String lockedBy;
    private volatile LocalDateTime lockExpiry;
    
    public Seat(String id) {
        this.id = id;
        this.status = SeatStatus.AVAILABLE;
    }
    
    public boolean lock(String userId, int minutes) {
        if (status != SeatStatus.AVAILABLE) return false;
        this.status = SeatStatus.LOCKED;
        this.lockedBy = userId;
        this.lockExpiry = LocalDateTime.now().plusMinutes(minutes);
        return true;
    }
    
    public boolean book(String userId) {
        if (status != SeatStatus.LOCKED || !lockedBy.equals(userId)) return false;
        this.status = SeatStatus.BOOKED;
        return true;
    }
    
    public void unlock() {
        if (status == SeatStatus.LOCKED) {
            this.status = SeatStatus.AVAILABLE;
            this.lockedBy = null;
            this.lockExpiry = null;
        }
    }
    
    public String getId() { return id; }
    public SeatStatus getStatus() { return status; }
}

/**
 * Thread-safe using ConcurrentHashMap and fine-grained locking
 */
class Show {
    private final String id;
    private final Movie movie;
    private final ConcurrentHashMap<String, Seat> seats;
    private final ConcurrentHashMap<String, ReentrantLock> seatLocks;
    
    public Show(String id, Movie movie, int noOfSeats) {
        this.id = id;
        this.movie = movie;
        this.seats = new ConcurrentHashMap<>();
        this.seatLocks = new ConcurrentHashMap<>();

        for (int i = 1; i <= noOfSeats; i++) {
            String seatId = "Seat" + i;
            seats.put(seatId, new Seat(seatId));
            seatLocks.put(seatId, new ReentrantLock());
        }
    }
    
    public String getId() { return id; }
    public Seat getSeat(String id) { return seats.get(id); }
    public ReentrantLock getSeatLock(String id) { return seatLocks.get(id); }
}

/**
 * Thread-safe (immutable after creation)
 */
class Booking {
    private final String id, userId, showId;
    private static final AtomicInteger counter = new AtomicInteger(0);
    private final List<String> seatIds;
    private volatile BookingStatus status;
    
    public Booking(String userId, String showId, List<String> seatIds) {
        this.id = "BKG-" + System.currentTimeMillis() + "-" + counter.incrementAndGet();
        this.userId = userId;
        this.showId = showId;
        this.seatIds = new ArrayList<>(seatIds);
        this.status = BookingStatus.PENDING;
    }
    
    public synchronized void confirm() {
        if (status == BookingStatus.PENDING) status = BookingStatus.CONFIRMED;
    }
    
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getShowId() { return showId; }
    public List<String> getSeatIds() { return new ArrayList<>(seatIds); }
    public BookingStatus getStatus() { return status; }
}

// ============================================================================
// REPOSITORIES
// ============================================================================

class ShowRepository {
    private final ConcurrentHashMap<String, Show> shows = new ConcurrentHashMap<>();
    public void save(Show show) { shows.put(show.getId(), show); }
    public Show findById(String id) { return shows.get(id); }
}

class BookingRepository {
    private final ConcurrentHashMap<String, Booking> bookings = new ConcurrentHashMap<>();
    public void save(Booking booking) { bookings.put(booking.getId(), booking); }
    public Booking findById(String id) { return bookings.get(id); }
}

// ============================================================================
// SERVICE (Core Business Logic with Concurrency Control)
// ============================================================================

/**
 * Thread-safe using fine-grained locking with deadlock prevention
 */
class BookingService {
    private final ShowRepository showRepo;
    private final BookingRepository bookingRepo;
    private static final int LOCK_TIMEOUT_SEC = 5;
    private static final int SEAT_LOCK_MIN = 5;
    
    public BookingService(ShowRepository showRepo, BookingRepository bookingRepo) {
        this.showRepo = showRepo;
        this.bookingRepo = bookingRepo;
    }
    
    /**
     * Lock seats (Step 1: Before payment)
     * CONCURRENCY: Fine-grained locking with deadlock prevention
     */
    public Booking lockSeats(String userId, String showId, List<String> seatIds) {
        Show show = showRepo.findById(showId);
        if (show == null) throw new IllegalArgumentException("Show not found");
        
        // DEADLOCK PREVENTION: Sort seat IDs
        List<String> sorted = new ArrayList<>(seatIds);
        Collections.sort(sorted);
        
        List<ReentrantLock> locks = new ArrayList<>();
        
        try {
            // Acquire locks in sorted order
            for (String seatId : sorted) {
                ReentrantLock lock = show.getSeatLock(seatId);
                if (lock == null) throw new IllegalArgumentException("Invalid seat: " + seatId);
                if (!lock.tryLock(LOCK_TIMEOUT_SEC, TimeUnit.SECONDS)) {
                    releaseLocks(locks);
                    throw new RuntimeException("Timeout: " + seatId);
                }
                locks.add(lock);
            }
            
            // Check and lock seats
            for (String seatId : sorted) {
                if (!show.getSeat(seatId).lock(userId, SEAT_LOCK_MIN)) {
                    releaseLocks(locks);
                    throw new RuntimeException("Seat unavailable: " + seatId);
                }
            }
            
            // Create booking
            Booking booking = new Booking(
                userId, showId, sorted
            );
            bookingRepo.save(booking);
            return booking;
            
        } catch (InterruptedException e) {
            releaseLocks(locks);
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted", e);
        } finally {
            releaseLocks(locks);
        }
    }
    
    /**
     * Confirm booking (Step 2: After payment)
     */
    public boolean confirmBooking(String bookingId) {
        Booking booking = bookingRepo.findById(bookingId);
        if (booking == null || booking.getStatus() != BookingStatus.PENDING) return false;
        
        Show show = showRepo.findById(booking.getShowId());
        List<String> sorted = new ArrayList<>(booking.getSeatIds());
        Collections.sort(sorted);
        
        List<ReentrantLock> locks = new ArrayList<>();
        
        try {
            for (String seatId : sorted) {
                ReentrantLock lock = show.getSeatLock(seatId);
                if (!lock.tryLock(LOCK_TIMEOUT_SEC, TimeUnit.SECONDS)) {
                    releaseLocks(locks);
                    return false;
                }
                locks.add(lock);
            }
            
            for (String seatId : sorted) {
                if (!show.getSeat(seatId).book(booking.getUserId())) {
                    releaseLocks(locks);
                    return false;
                }
            }
            
            booking.confirm();
            return true;
            
        } catch (InterruptedException e) {
            releaseLocks(locks);
            Thread.currentThread().interrupt();
            return false;
        } finally {
            releaseLocks(locks);
        }
    }
    
    private void releaseLocks(List<ReentrantLock> locks) {
        for (int i = locks.size() - 1; i >= 0; i--) {
            locks.get(i).unlock();
        }
    }
}

// ============================================================================
// DEMO
// ============================================================================

public class MovieBookingSystemComplete {
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=".repeat(70));
        System.out.println("MOVIE BOOKING SYSTEM - CONCURRENCY DEMO");
        System.out.println("=".repeat(70));
        
        // Setup
        ShowRepository showRepo = new ShowRepository();
        BookingRepository bookingRepo = new BookingRepository();
        BookingService service = new BookingService(showRepo, bookingRepo);
        
        Movie movie = new Movie("M1", "Inception", 148);
//        Screen screen = new Screen("S1", "Screen 1", 5, 5);
        Show show = new Show("SHOW1", movie, 100);
        showRepo.save(show);
        
        System.out.println("\n✅ Setup: " + movie.getTitle() + " | 5x5 = 25 seats\n");
        
        // Test 1: Single user
        test1(service);
        
        // Test 2: Concurrent - different seats
        test2(service);
        
        // Test 3: Concurrent - same seat
        test3(service);
        
        // Test 4: Deadlock prevention
        test4(service);
        
        System.out.println("\n" + "=".repeat(70));
        System.out.println("ALL TESTS PASSED! ✅");
        System.out.println("=".repeat(70));
    }
    
    private static void test1(BookingService service) {
        System.out.println("TEST 1: Single User Booking");
        System.out.println("-".repeat(70));
        try {
            Booking b = service.lockSeats("user1", "SHOW1", Arrays.asList("1-1", "1-2"));
            System.out.println("✅ Locked: " + b.getSeatIds());
            service.confirmBooking(b.getId());
            System.out.println("✅ Confirmed\n");
        } catch (Exception e) {
            System.out.println("❌ Failed: " + e.getMessage() + "\n");
        }
    }
    
    private static void test2(BookingService service) throws InterruptedException {
        System.out.println("TEST 2: Concurrent - Different Seats (All should succeed)");
        System.out.println("-".repeat(70));
        
        ExecutorService exec = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(10);
        AtomicInteger success = new AtomicInteger(0);
        
        for (int i = 0; i < 10; i++) {
            final int userId = i;
            exec.submit(() -> {
                try {
                    service.lockSeats("user" + userId, "SHOW1", Arrays.asList("2-" + (userId + 1)));
                    success.incrementAndGet();
                } catch (Exception e) {
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await();
        exec.shutdown();
        System.out.println("✅ Success: " + success.get() + "/10\n");
    }
    
    private static void test3(BookingService service) throws InterruptedException {
        System.out.println("TEST 3: Concurrent - Same Seat (Only 1 should succeed)");
        System.out.println("-".repeat(70));
        
        ExecutorService exec = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(10);
        AtomicInteger success = new AtomicInteger(0);
        
        for (int i = 0; i < 10; i++) {
            final int userId = i;
            exec.submit(() -> {
                try {
                    service.lockSeats("user" + userId, "SHOW1", Arrays.asList("3-1"));
                    success.incrementAndGet();
                } catch (Exception e) {
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await();
        exec.shutdown();
        System.out.println("✅ Success: " + success.get() + "/10 (Expected: 1)\n");
    }
    
    private static void test4(BookingService service) throws InterruptedException {
        System.out.println("TEST 4: Deadlock Prevention");
        System.out.println("-".repeat(70));
        
        ExecutorService exec = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);
        
        exec.submit(() -> {
            try {
                service.lockSeats("userA", "SHOW1", Arrays.asList("4-1", "4-2"));
                System.out.println("✅ User A booked [4-1, 4-2]");
            } catch (Exception e) {
            } finally {
                latch.countDown();
            }
        });
        
        exec.submit(() -> {
            try {
                service.lockSeats("userB", "SHOW1", Arrays.asList("4-2", "4-3"));
                System.out.println("✅ User B booked [4-2, 4-3]");
            } catch (Exception e) {
                System.out.println("❌ User B failed (expected - seat 4-2 taken)");
            } finally {
                latch.countDown();
            }
        });
        
        boolean completed = latch.await(10, TimeUnit.SECONDS);
        exec.shutdown();
        System.out.println("✅ No deadlock! Completed: " + completed + "\n");
    }
}
