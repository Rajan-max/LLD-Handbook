package com.rajan.lld.ConcurrencyControlsMethod;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;

/**
 * Concurrency Patterns - Runnable Examples
 * 
 * Demonstrates all 7 essential concurrency patterns for LLD interviews
 * Each pattern includes working code and test demonstrating thread-safety
 */
public class ConcurrencyPatterns {

    // ============================================================================
    // PATTERN 1: SYNCHRONIZED METHOD/BLOCK
    // ============================================================================
    
    static class Pattern1_Synchronized {
        
        // UNSAFE Counter (for comparison)
        static class UnsafeCounter {
            private int count = 0;
            
            public void increment() {
                count++; // Race condition!
            }
            
            public int getCount() {
                return count;
            }
        }
        
        // SAFE Counter using synchronized
        static class SafeCounter {
            private int count = 0;
            
            public synchronized void increment() {
                count++;
            }
            
            public synchronized int getCount() {
                return count;
            }
        }
        
        static void demo() throws Exception {
            System.out.println("\n=== PATTERN 1: SYNCHRONIZED ===");
            
            // Test unsafe counter
            UnsafeCounter unsafe = new UnsafeCounter();
            ExecutorService executor = Executors.newFixedThreadPool(10);
            
            for (int i = 0; i < 1000; i++) {
                executor.submit(unsafe::increment);
            }
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
            
            System.out.println("Unsafe counter: " + unsafe.getCount() + " (expected 1000, likely less due to race condition)");
            
            // Test safe counter
            SafeCounter safe = new SafeCounter();
            executor = Executors.newFixedThreadPool(10);
            
            for (int i = 0; i < 1000; i++) {
                executor.submit(safe::increment);
            }
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
            
            System.out.println("Safe counter: " + safe.getCount() + " (expected 1000, always correct)");
        }
    }

    // ============================================================================
    // PATTERN 2: REENTRANT LOCK
    // ============================================================================
    
    static class Pattern2_ReentrantLock {
        
        static class BankAccount {
            private double balance;
            private final ReentrantLock lock = new ReentrantLock(true); // fair=true for FIFO
            
            public BankAccount(double balance) {
                this.balance = balance;
            }
            
            // Blocking with timeout
            public boolean withdraw(double amount) {
                try {
                    if (lock.tryLock(2, TimeUnit.SECONDS)) {
                        try {
                            if (balance >= amount) {
                                Thread.sleep(10); // Simulate processing
                                balance -= amount;
                                return true;
                            }
                            return false;
                        } finally {
                            lock.unlock();
                        }
                    }
                    System.out.println("Timeout acquiring lock");
                    return false;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
            
            public double getBalance() {
                lock.lock();
                try {
                    return balance;
                } finally {
                    lock.unlock();
                }
            }
        }
        
        static void demo() throws Exception {
            System.out.println("\n=== PATTERN 2: REENTRANT LOCK ===");
            
            BankAccount account = new BankAccount(1000);
            ExecutorService executor = Executors.newFixedThreadPool(5);
            List<Future<Boolean>> futures = new ArrayList<>();
            
            // 5 threads trying to withdraw $300 each
            for (int i = 0; i < 5; i++) {
                futures.add(executor.submit(() -> account.withdraw(300)));
            }
            
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
            
            long successCount = futures.stream()
                .map(f -> { try { return f.get(); } catch(Exception e) { return false; } })
                .filter(b -> b)
                .count();
            
            System.out.println("Successful withdrawals: " + successCount + " (expected 3)");
            System.out.println("Final balance: $" + account.getBalance() + " (expected $100)");
        }
    }

    // ============================================================================
    // PATTERN 3: FINE-GRAINED LOCKING
    // ============================================================================
    
    static class Pattern3_FineGrainedLocking {
        
        static class Seat {
            private final String id;
            private volatile boolean booked = false;
            
            public Seat(String id) { this.id = id; }
            public String getId() { return id; }
            public boolean isBooked() { return booked; }
            public void book() { booked = true; }
        }
        
        static class MovieBookingService {
            private final Map<String, Seat> seats = new ConcurrentHashMap<>();
            private final ConcurrentHashMap<String, ReentrantLock> seatLocks = new ConcurrentHashMap<>();
            
            public MovieBookingService(List<String> seatIds) {
                for (String id : seatIds) {
                    seats.put(id, new Seat(id));
                    seatLocks.put(id, new ReentrantLock(true));
                }
            }
            
            // Book multiple seats atomically
            public boolean bookSeats(List<String> seatIds) {
                // CRITICAL: Sort to prevent deadlock
                List<String> sorted = new ArrayList<>(seatIds);
                Collections.sort(sorted);
                
                List<ReentrantLock> acquired = new ArrayList<>();
                
                try {
                    // Acquire all locks
                    for (String seatId : sorted) {
                        ReentrantLock lock = seatLocks.get(seatId);
                        if (!lock.tryLock(5, TimeUnit.SECONDS)) {
                            // Rollback
                            for (ReentrantLock l : acquired) {
                                l.unlock();
                            }
                            return false;
                        }
                        acquired.add(lock);
                    }
                    
                    // Check all available
                    for (String seatId : sorted) {
                        if (seats.get(seatId).isBooked()) {
                            return false;
                        }
                    }
                    
                    // Book all
                    for (String seatId : sorted) {
                        seats.get(seatId).book();
                    }
                    return true;
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                } finally {
                    // Release in reverse order
                    for (int i = acquired.size() - 1; i >= 0; i--) {
                        acquired.get(i).unlock();
                    }
                }
            }
            
            public long getBookedCount() {
                return seats.values().stream().filter(Seat::isBooked).count();
            }
        }
        
        static void demo() throws Exception {
            System.out.println("\n=== PATTERN 3: FINE-GRAINED LOCKING ===");
            
            List<String> seatIds = Arrays.asList("A1", "A2", "A3", "B1", "B2");
            MovieBookingService service = new MovieBookingService(seatIds);
            
            ExecutorService executor = Executors.newFixedThreadPool(10);
            List<Future<Boolean>> futures = new ArrayList<>();
            
            // 10 threads trying to book A1-A2
            for (int i = 0; i < 10; i++) {
                futures.add(executor.submit(() -> 
                    service.bookSeats(Arrays.asList("A1", "A2"))
                ));
            }
            
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
            
            long successCount = futures.stream()
                .map(f -> { try { return f.get(); } catch(Exception e) { return false; } })
                .filter(b -> b)
                .count();
            
            System.out.println("Successful bookings: " + successCount + " (expected 1)");
            System.out.println("Total booked seats: " + service.getBookedCount() + " (expected 2)");
        }
    }

    // ============================================================================
    // PATTERN 4: READ-WRITE LOCK
    // ============================================================================
    
    static class Pattern4_ReadWriteLock {
        
        static class ConfigManager {
            private final Map<String, String> config = new HashMap<>();
            private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
            private final Lock readLock = rwLock.readLock();
            private final Lock writeLock = rwLock.writeLock();
            
            public void set(String key, String value) {
                writeLock.lock();
                try {
                    Thread.sleep(50); // Simulate slow write
                    config.put(key, value);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    writeLock.unlock();
                }
            }
            
            public String get(String key) {
                readLock.lock();
                try {
                    return config.get(key);
                } finally {
                    readLock.unlock();
                }
            }
        }
        
        static void demo() throws Exception {
            System.out.println("\n=== PATTERN 4: READ-WRITE LOCK ===");
            
            ConfigManager config = new ConfigManager();
            config.set("db.host", "localhost");
            
            ExecutorService executor = Executors.newFixedThreadPool(20);
            long startTime = System.currentTimeMillis();
            
            // 100 reads (can happen concurrently)
            for (int i = 0; i < 100; i++) {
                executor.submit(() -> config.get("db.host"));
            }
            
            // 5 writes (must be exclusive)
            for (int i = 0; i < 5; i++) {
                final int idx = i;
                executor.submit(() -> config.set("key" + idx, "value" + idx));
            }
            
            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.SECONDS);
            
            long duration = System.currentTimeMillis() - startTime;
            System.out.println("Completed 100 reads + 5 writes in " + duration + "ms");
            System.out.println("(Multiple reads happened concurrently, writes were exclusive)");
        }
    }

    // ============================================================================
    // PATTERN 5: ATOMIC VARIABLES
    // ============================================================================
    
    static class Pattern5_AtomicVariables {
        
        static class TicketGenerator {
            private final AtomicLong counter = new AtomicLong(0);
            
            public long generateId() {
                return counter.incrementAndGet();
            }
        }
        
        static class SlotManager {
            private final AtomicInteger availableSlots;
            
            public SlotManager(int total) {
                this.availableSlots = new AtomicInteger(total);
            }
            
            // Compare-And-Swap pattern
            public boolean reserveSlot() {
                while (true) {
                    int current = availableSlots.get();
                    if (current <= 0) return false;
                    
                    if (availableSlots.compareAndSet(current, current - 1)) {
                        return true;
                    }
                    // CAS failed, retry
                }
            }
            
            public int getAvailable() {
                return availableSlots.get();
            }
        }
        
        static void demo() throws Exception {
            System.out.println("\n=== PATTERN 5: ATOMIC VARIABLES ===");
            
            // Test ticket generator
            TicketGenerator generator = new TicketGenerator();
            ExecutorService executor = Executors.newFixedThreadPool(10);
            Set<Long> ids = ConcurrentHashMap.newKeySet();
            
            for (int i = 0; i < 1000; i++) {
                executor.submit(() -> ids.add(generator.generateId()));
            }
            
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
            
            System.out.println("Generated " + ids.size() + " unique IDs (expected 1000)");
            
            // Test slot manager
            SlotManager manager = new SlotManager(10);
            executor = Executors.newFixedThreadPool(20);
            List<Future<Boolean>> futures = new ArrayList<>();
            
            for (int i = 0; i < 20; i++) {
                futures.add(executor.submit(manager::reserveSlot));
            }
            
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
            
            long successCount = futures.stream()
                .map(f -> { try { return f.get(); } catch(Exception e) { return false; } })
                .filter(b -> b)
                .count();
            
            System.out.println("Successful reservations: " + successCount + " (expected 10)");
            System.out.println("Remaining slots: " + manager.getAvailable() + " (expected 0)");
        }
    }

    // ============================================================================
    // PATTERN 6: CONCURRENT HASH MAP
    // ============================================================================
    
    static class Pattern6_ConcurrentHashMap {
        
        static class SessionManager {
            private final ConcurrentHashMap<String, Integer> sessionCounts = new ConcurrentHashMap<>();
            
            // Atomic increment
            public void recordAccess(String sessionId) {
                sessionCounts.compute(sessionId, (k, v) -> v == null ? 1 : v + 1);
            }
            
            // Atomic get-or-create
            public int getOrCreateSession(String sessionId) {
                return sessionCounts.computeIfAbsent(sessionId, k -> 0);
            }
            
            public int getAccessCount(String sessionId) {
                return sessionCounts.getOrDefault(sessionId, 0);
            }
        }
        
        static void demo() throws Exception {
            System.out.println("\n=== PATTERN 6: CONCURRENT HASH MAP ===");
            
            SessionManager manager = new SessionManager();
            ExecutorService executor = Executors.newFixedThreadPool(10);
            
            // 100 threads incrementing same session
            for (int i = 0; i < 100; i++) {
                executor.submit(() -> manager.recordAccess("session-1"));
            }
            
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
            
            System.out.println("Session access count: " + manager.getAccessCount("session-1") + " (expected 100)");
        }
    }

    // ============================================================================
    // PATTERN 7: DOUBLE-CHECKED LOCKING
    // ============================================================================
    
    static class Pattern7_DoubleCheckedLocking {
        
        static class ExpensiveResource {
            private static volatile ExpensiveResource instance;
            private final long creationTime;
            
            private ExpensiveResource() {
                try {
                    Thread.sleep(100); // Simulate expensive initialization
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                this.creationTime = System.currentTimeMillis();
            }
            
            public static ExpensiveResource getInstance() {
                // First check (no locking)
                if (instance == null) {
                    synchronized(ExpensiveResource.class) {
                        // Second check (with locking)
                        if (instance == null) {
                            instance = new ExpensiveResource();
                        }
                    }
                }
                return instance;
            }
            
            public long getCreationTime() {
                return creationTime;
            }
        }
        
        static void demo() throws Exception {
            System.out.println("\n=== PATTERN 7: DOUBLE-CHECKED LOCKING ===");
            
            ExecutorService executor = Executors.newFixedThreadPool(10);
            Set<Long> creationTimes = ConcurrentHashMap.newKeySet();
            
            // 10 threads trying to get instance
            for (int i = 0; i < 10; i++) {
                executor.submit(() -> {
                    ExpensiveResource resource = ExpensiveResource.getInstance();
                    creationTimes.add(resource.getCreationTime());
                });
            }
            
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
            
            System.out.println("Unique instances created: " + creationTimes.size() + " (expected 1)");
            System.out.println("(Only one thread performed expensive initialization)");
        }
    }

    // ============================================================================
    // MAIN - RUN ALL DEMOS
    // ============================================================================
    
    public static void main(String[] args) throws Exception {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║   CONCURRENCY PATTERNS - RUNNABLE EXAMPLES                 ║");
        System.out.println("║   7 Essential Patterns for LLD Interviews                  ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        
        Pattern1_Synchronized.demo();
        Pattern2_ReentrantLock.demo();
        Pattern3_FineGrainedLocking.demo();
        Pattern4_ReadWriteLock.demo();
        Pattern5_AtomicVariables.demo();
        Pattern6_ConcurrentHashMap.demo();
        Pattern7_DoubleCheckedLocking.demo();
        
        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║   ALL PATTERNS DEMONSTRATED SUCCESSFULLY ✓                 ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
    }
}
