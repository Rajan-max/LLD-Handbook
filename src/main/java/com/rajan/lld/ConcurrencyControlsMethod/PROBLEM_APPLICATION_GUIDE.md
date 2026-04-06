# Concurrency Patterns - Problem Application Guide 🎯

> **How to apply the 7 patterns to common LLD interview problems**

---

## 📋 Problem Categories

### Category 1: Booking Systems
- Movie Ticket Booking
- Hotel Room Booking
- Flight Seat Booking
- Restaurant Table Booking

### Category 2: Resource Management
- Parking Lot System
- Meeting Room Scheduler
- Inventory Management
- Connection Pool

### Category 3: Financial Systems
- Banking System
- Payment Gateway
- Wallet System
- Trading Platform

### Category 4: Social Systems
- Rate Limiter
- Notification System
- Message Queue
- Cache System

---

## 🎬 Problem 1: Movie Ticket Booking

### Requirements
- Book single seat: Thread-safe
- Book multiple seats: Atomic (all-or-nothing)
- Prevent double-booking
- Handle 1000+ concurrent users

### Concurrency Analysis

**Shared Resources**: Seats (high contention)
**Critical Section**: Check availability + Book
**Race Condition**: Two users booking same seat

### Pattern Selection

| Operation | Pattern | Why |
|-----------|---------|-----|
| Book single seat | Fine-Grained Locking | One lock per seat, max parallelism |
| Book multiple seats | Fine-Grained + Lock Ordering | Prevent deadlock |
| Seat status lookup | ConcurrentHashMap | Fast, thread-safe reads |
| Booking ID generation | AtomicLong | Lock-free counter |

### Implementation

```java
class MovieBookingSystem {
    private final ConcurrentHashMap<String, Seat> seats;
    private final ConcurrentHashMap<String, ReentrantLock> seatLocks;
    private final AtomicLong bookingIdGenerator = new AtomicLong(0);
    
    // Single seat booking
    public boolean bookSeat(String seatId, String userId) {
        ReentrantLock lock = seatLocks.get(seatId);
        try {
            if (lock.tryLock(5, TimeUnit.SECONDS)) {
                try {
                    Seat seat = seats.get(seatId);
                    if (seat.isAvailable()) {
                        seat.book(userId);
                        return true;
                    }
                    return false;
                } finally {
                    lock.unlock();
                }
            }
            return false; // Timeout
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
    
    // Multiple seats booking (atomic)
    public boolean bookSeats(List<String> seatIds, String userId) {
        // CRITICAL: Sort to prevent deadlock
        List<String> sorted = new ArrayList<>(seatIds);
        Collections.sort(sorted);
        
        List<ReentrantLock> acquired = new ArrayList<>();
        
        try {
            // Phase 1: Acquire all locks
            for (String seatId : sorted) {
                ReentrantLock lock = seatLocks.get(seatId);
                if (!lock.tryLock(5, TimeUnit.SECONDS)) {
                    // Rollback
                    for (ReentrantLock l : acquired) l.unlock();
                    return false;
                }
                acquired.add(lock);
            }
            
            // Phase 2: Check all available
            for (String seatId : sorted) {
                if (!seats.get(seatId).isAvailable()) {
                    return false; // Rollback in finally
                }
            }
            
            // Phase 3: Book all
            long bookingId = bookingIdGenerator.incrementAndGet();
            for (String seatId : sorted) {
                seats.get(seatId).book(userId, bookingId);
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
}
```

### Key Insights
- ✅ Fine-grained locking for maximum throughput
- ✅ Lock ordering prevents deadlock
- ✅ Timeout prevents infinite blocking
- ✅ Rollback ensures atomicity

---

## 🅿️ Problem 2: Parking Lot System

### Requirements
- Park vehicle in available slot
- Exit and calculate fee
- Support multiple vehicle types
- Handle 100+ concurrent operations

### Concurrency Analysis

**Shared Resources**: Parking slots (medium contention)
**Critical Section**: Find slot + Park
**Race Condition**: Two vehicles parking in same slot

### Pattern Selection

| Operation | Pattern | Why |
|-----------|---------|-----|
| Park vehicle | Fine-Grained Locking | One lock per slot |
| Exit vehicle | Fine-Grained Locking | Lock same slot |
| Ticket storage | ConcurrentHashMap | Thread-safe map |
| Slot status | volatile + lock | Visibility + atomicity |

### Implementation

```java
class ParkingLotSystem {
    private final ConcurrentHashMap<String, ReentrantLock> slotLocks;
    private final ConcurrentHashMap<String, Ticket> tickets;
    
    public Ticket parkVehicle(Vehicle vehicle) {
        for (Floor floor : floors) {
            for (ParkingSlot slot : floor.getSlots()) {
                ReentrantLock lock = slotLocks.get(slot.getId());
                
                // Non-blocking tryLock for better throughput
                if (lock.tryLock()) {
                    try {
                        if (slot.canFit(vehicle)) {
                            slot.park(vehicle);
                            Ticket ticket = createTicket(slot, vehicle);
                            tickets.put(ticket.getId(), ticket);
                            return ticket;
                        }
                    } finally {
                        lock.unlock();
                    }
                }
            }
        }
        throw new RuntimeException("No available slot");
    }
    
    public double exitVehicle(String ticketId) {
        Ticket ticket = tickets.get(ticketId);
        if (ticket == null) throw new IllegalArgumentException("Invalid ticket");
        
        ParkingSlot slot = ticket.getSlot();
        ReentrantLock lock = slotLocks.get(slot.getId());
        
        lock.lock(); // Blocking lock (must succeed)
        try {
            slot.free();
            ticket.setExitTime(LocalDateTime.now());
            return calculateFee(ticket);
        } finally {
            lock.unlock();
        }
    }
}

class ParkingSlot {
    private final String id;
    private volatile boolean occupied; // Visibility
    private volatile Vehicle vehicle;
    
    // Caller MUST hold lock
    public boolean canFit(Vehicle v) {
        return !occupied && type.matches(v.getType());
    }
    
    // Caller MUST hold lock
    public void park(Vehicle v) {
        this.vehicle = v;
        this.occupied = true;
    }
    
    // Caller MUST hold lock
    public void free() {
        this.vehicle = null;
        this.occupied = false;
    }
}
```

### Key Insights
- ✅ tryLock() for non-blocking park (better UX)
- ✅ Blocking lock for exit (must succeed)
- ✅ No deadlock risk (single lock per operation)
- ✅ volatile for visibility across threads

---

## 💰 Problem 3: Banking System

### Requirements
- Deposit/withdraw from account
- Transfer between accounts
- Prevent overdraft
- Handle concurrent transactions

### Concurrency Analysis

**Shared Resources**: Account balances (high contention)
**Critical Section**: Check balance + Update
**Race Condition**: Concurrent withdrawals exceeding balance

### Pattern Selection

| Operation | Pattern | Why |
|-----------|---------|-----|
| Single account ops | ReentrantLock per account | Fine-grained locking |
| Transfer (2 accounts) | Lock Ordering | Prevent deadlock |
| Transaction ID | AtomicLong | Lock-free counter |
| Transaction log | ConcurrentHashMap | Thread-safe storage |

### Implementation

```java
class BankingSystem {
    private final ConcurrentHashMap<String, Account> accounts;
    private final ConcurrentHashMap<String, ReentrantLock> accountLocks;
    private final AtomicLong txnIdGenerator = new AtomicLong(0);
    
    public boolean withdraw(String accountId, double amount) {
        ReentrantLock lock = accountLocks.get(accountId);
        lock.lock();
        try {
            Account account = accounts.get(accountId);
            if (account.getBalance() >= amount) {
                account.debit(amount);
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }
    
    public boolean transfer(String fromId, String toId, double amount) {
        // CRITICAL: Lock ordering to prevent deadlock
        String firstId = fromId.compareTo(toId) < 0 ? fromId : toId;
        String secondId = fromId.compareTo(toId) < 0 ? toId : fromId;
        
        ReentrantLock firstLock = accountLocks.get(firstId);
        ReentrantLock secondLock = accountLocks.get(secondId);
        
        firstLock.lock();
        try {
            secondLock.lock();
            try {
                Account from = accounts.get(fromId);
                Account to = accounts.get(toId);
                
                if (from.getBalance() >= amount) {
                    from.debit(amount);
                    to.credit(amount);
                    
                    long txnId = txnIdGenerator.incrementAndGet();
                    logTransaction(txnId, fromId, toId, amount);
                    return true;
                }
                return false;
            } finally {
                secondLock.unlock();
            }
        } finally {
            firstLock.unlock();
        }
    }
}
```

### Key Insights
- ✅ Lock ordering by account ID (lexicographic)
- ✅ Nested locking for transfer atomicity
- ✅ Balance check inside critical section
- ✅ Transaction ID generation is lock-free

---

## 🚦 Problem 4: Rate Limiter

### Requirements
- Limit requests per user per time window
- Support 10,000+ concurrent users
- Sliding window algorithm
- High performance (< 1ms overhead)

### Concurrency Analysis

**Shared Resources**: Request counters per user
**Critical Section**: Increment counter + Check limit
**Race Condition**: Counter increment not atomic

### Pattern Selection

| Operation | Pattern | Why |
|-----------|---------|-----|
| Request counting | AtomicInteger | Lock-free, high performance |
| User data storage | ConcurrentHashMap | Thread-safe map |
| Window cleanup | Scheduled task | Background cleanup |

### Implementation

```java
class RateLimiter {
    private final ConcurrentHashMap<String, UserWindow> userWindows;
    private final int maxRequests;
    private final long windowMs;
    
    static class UserWindow {
        private final AtomicInteger count = new AtomicInteger(0);
        private volatile long windowStart;
        
        public UserWindow() {
            this.windowStart = System.currentTimeMillis();
        }
        
        public boolean allowRequest(int maxRequests, long windowMs) {
            long now = System.currentTimeMillis();
            
            // Check if window expired
            if (now - windowStart >= windowMs) {
                // Reset window atomically
                synchronized(this) {
                    if (now - windowStart >= windowMs) {
                        count.set(0);
                        windowStart = now;
                    }
                }
            }
            
            // Increment and check
            int current = count.incrementAndGet();
            return current <= maxRequests;
        }
    }
    
    public boolean allowRequest(String userId) {
        UserWindow window = userWindows.computeIfAbsent(
            userId, 
            k -> new UserWindow()
        );
        return window.allowRequest(maxRequests, windowMs);
    }
}
```

### Key Insights
- ✅ AtomicInteger for lock-free counting
- ✅ synchronized only for window reset (rare)
- ✅ computeIfAbsent for atomic get-or-create
- ✅ volatile for window start visibility

---

## 📦 Problem 5: Inventory Management

### Requirements
- Check stock availability (frequent)
- Update stock (infrequent)
- Reserve items atomically
- Support 1000+ products

### Concurrency Analysis

**Shared Resources**: Product inventory
**Critical Section**: Check stock + Reserve
**Workload**: 90% reads, 10% writes

### Pattern Selection

| Operation | Pattern | Why |
|-----------|---------|-----|
| Check stock | ReadWriteLock (read) | Multiple readers |
| Update stock | ReadWriteLock (write) | Exclusive writer |
| Reserve items | Write lock | Atomic check-and-update |
| Product data | ConcurrentHashMap | Thread-safe storage |

### Implementation

```java
class InventorySystem {
    private final ConcurrentHashMap<String, ProductInventory> inventory;
    
    static class ProductInventory {
        private int quantity;
        private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
        private final Lock readLock = rwLock.readLock();
        private final Lock writeLock = rwLock.writeLock();
        
        // Read operation (multiple threads)
        public int getQuantity() {
            readLock.lock();
            try {
                return quantity;
            } finally {
                readLock.unlock();
            }
        }
        
        // Write operation (exclusive)
        public void setQuantity(int newQuantity) {
            writeLock.lock();
            try {
                this.quantity = newQuantity;
            } finally {
                writeLock.unlock();
            }
        }
        
        // Atomic reserve
        public boolean reserve(int count) {
            writeLock.lock();
            try {
                if (quantity >= count) {
                    quantity -= count;
                    return true;
                }
                return false;
            } finally {
                writeLock.unlock();
            }
        }
    }
    
    public boolean reserveItems(String productId, int count) {
        ProductInventory inv = inventory.get(productId);
        return inv != null && inv.reserve(count);
    }
}
```

### Key Insights
- ✅ ReadWriteLock for read-heavy workload
- ✅ Multiple readers don't block each other
- ✅ Writers get exclusive access
- ✅ Reserve operation uses write lock

---

## 🎯 Pattern Selection Matrix

| Problem Type | Primary Pattern | Secondary Pattern | ID Generation |
|--------------|----------------|-------------------|---------------|
| **Movie Booking** | Fine-Grained Locking | ConcurrentHashMap | AtomicLong |
| **Parking Lot** | Fine-Grained Locking | ConcurrentHashMap | AtomicLong |
| **Banking** | Fine-Grained Locking | Lock Ordering | AtomicLong |
| **Rate Limiter** | AtomicInteger | ConcurrentHashMap | - |
| **Inventory** | ReadWriteLock | ConcurrentHashMap | - |
| **Cache** | ConcurrentHashMap | ReadWriteLock | - |
| **Session Manager** | ConcurrentHashMap | - | - |

---

## 🧪 Testing Strategy

### Test Template for All Problems

```java
@Test
void testConcurrentOperations() throws Exception {
    // Setup
    Service service = new Service();
    ExecutorService executor = Executors.newFixedThreadPool(20);
    List<Future<Boolean>> futures = new ArrayList<>();
    
    // Execute concurrent operations
    for (int i = 0; i < 100; i++) {
        futures.add(executor.submit(() -> service.operation()));
    }
    
    // Wait for completion
    executor.shutdown();
    executor.awaitTermination(10, TimeUnit.SECONDS);
    
    // Verify results
    long successCount = futures.stream()
        .map(f -> { try { return f.get(); } catch(Exception e) { return false; } })
        .filter(b -> b)
        .count();
    
    // Assert expectations
    assertEquals(expectedCount, successCount);
    assertEquals(expectedState, service.getState());
}
```

---

## 📝 Documentation Template

```java
/**
 * [Class Name] - [Purpose]
 * 
 * Thread-Safety: Thread-safe using [pattern name]
 * Concurrency Strategy:
 *   - [Resource 1]: [Pattern] - [Reason]
 *   - [Resource 2]: [Pattern] - [Reason]
 * 
 * Deadlock Prevention: [Explain if applicable]
 * Performance: [Expected throughput/latency]
 * 
 * @author [Your Name]
 */
class MyClass {
    // Implementation
}
```

---

## 🚀 Interview Checklist

Before coding:
- [ ] Identify all shared resources
- [ ] Identify all critical sections
- [ ] Choose appropriate pattern(s)
- [ ] Explain deadlock prevention (if multiple locks)

During coding:
- [ ] Use try-finally for lock release
- [ ] Sort IDs before acquiring multiple locks
- [ ] Use timeout for user-facing operations
- [ ] Document thread-safety guarantees

After coding:
- [ ] Explain testing strategy
- [ ] Discuss trade-offs (memory, complexity, performance)
- [ ] Mention monitoring (lock contention, timeouts)
- [ ] Suggest optimizations for scale

---

**Master these 5 problems and you can handle any LLD concurrency question!** 🎯
