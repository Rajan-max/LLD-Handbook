# Concurrency Controls for LLD Interviews 🔐

> **Master concurrency patterns to ace 20% of your LLD interview evaluation**

---

## 📚 Table of Contents

1. [Why Concurrency Matters](#why-concurrency-matters)
2. [Core Concepts](#core-concepts)
3. [7 Essential Patterns](#7-essential-patterns)
4. [Pattern Selection Guide](#pattern-selection-guide)
5. [Common Pitfalls](#common-pitfalls)
6. [Interview Tips](#interview-tips)

---

## 🎯 Why Concurrency Matters

### In LLD Interviews
- **20% of evaluation** focuses on concurrency control
- Differentiates senior from junior candidates
- Tests understanding of race conditions, deadlocks, and thread-safety

### Real-World Impact
- **Movie Booking**: Prevent double-booking of seats
- **Parking Lot**: Prevent double-parking in same slot
- **E-commerce**: Prevent overselling inventory
- **Banking**: Prevent concurrent withdrawals exceeding balance

---

## 🧠 Core Concepts

### 1. Race Condition
**Problem**: Multiple threads access shared data, final result depends on timing

```java
// UNSAFE - Race condition
class Counter {
    private int count = 0;
    
    public void increment() {
        count++;  // Read-Modify-Write (3 operations, not atomic!)
    }
}

// Thread 1: Read count=0, increment to 1, write 1
// Thread 2: Read count=0, increment to 1, write 1
// Expected: 2, Actual: 1 (Lost update!)
```

### 2. Critical Section
**Definition**: Code segment accessing shared resources that must not be executed by multiple threads simultaneously

```java
// Critical section
lock.lock();
try {
    // Only one thread can execute this at a time
    if (slot.isAvailable()) {
        slot.book();
    }
} finally {
    lock.unlock();
}
```

### 3. Atomicity
**Definition**: Operation appears to happen instantaneously, no intermediate state visible

```java
// Non-atomic (3 operations)
count = count + 1;

// Atomic (1 operation)
atomicCount.incrementAndGet();
```

### 4. Visibility
**Definition**: Changes made by one thread are visible to other threads

```java
// Without volatile - changes may not be visible
private boolean flag = false;

// With volatile - changes immediately visible
private volatile boolean flag = false;
```

### 5. Deadlock
**Definition**: Two or more threads waiting for each other, none can proceed

```java
// Thread 1: Lock A → Lock B
// Thread 2: Lock B → Lock A
// Result: Deadlock! Both waiting forever
```

**Prevention**: Always acquire locks in same order

---

## 🔧 7 Essential Patterns

### Pattern 1: Synchronized Method/Block ⭐

**When to Use**: Simple mutual exclusion, low contention

**Pros**: Simple, built-in, no external dependencies
**Cons**: Coarse-grained, blocks all threads, no timeout

```java
class BookingService {
    private final Map<String, Seat> seats = new HashMap<>();
    
    // Method-level synchronization
    public synchronized boolean bookSeat(String seatId) {
        Seat seat = seats.get(seatId);
        if (seat.isAvailable()) {
            seat.book();
            return true;
        }
        return false;
    }
    
    // Block-level synchronization (better)
    public boolean bookSeatOptimized(String seatId) {
        synchronized(this) {
            Seat seat = seats.get(seatId);
            if (seat.isAvailable()) {
                seat.book();
                return true;
            }
        }
        return false;
    }
}
```

**Use Cases**: Simple counters, small critical sections, low-traffic operations

---

### Pattern 2: ReentrantLock (Fair/Unfair) ⭐⭐⭐

**When to Use**: Need timeout, tryLock, or fair ordering

**Pros**: Timeout support, tryLock (non-blocking), fair/unfair modes, interruptible
**Cons**: Must manually unlock (use try-finally), more complex

```java
class ParkingManager {
    private final ReentrantLock lock = new ReentrantLock(true); // fair=true for FIFO
    
    // Pattern 1: Blocking with timeout
    public boolean parkVehicle(Vehicle v) {
        try {
            if (lock.tryLock(5, TimeUnit.SECONDS)) {
                try {
                    // Critical section
                    return findAndPark(v);
                } finally {
                    lock.unlock(); // MUST unlock
                }
            }
            return false; // Timeout
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
    
    // Pattern 2: Non-blocking
    public boolean tryParkVehicle(Vehicle v) {
        if (lock.tryLock()) {
            try {
                return findAndPark(v);
            } finally {
                lock.unlock();
            }
        }
        return false; // Couldn't acquire lock
    }
}
```

**Use Cases**: Movie booking (timeout), parking lot (tryLock), any operation needing fairness

---

### Pattern 3: Fine-Grained Locking ⭐⭐⭐⭐⭐

**When to Use**: High contention, need maximum parallelism

**Pros**: Maximum throughput, independent resources don't block each other
**Cons**: More memory (lock per resource), deadlock risk if multiple locks

```java
class MovieBookingService {
    private final ConcurrentHashMap<String, ReentrantLock> seatLocks = new ConcurrentHashMap<>();
    
    // Each seat has its own lock
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
            
            // All locks acquired - perform booking
            for (String seatId : sorted) {
                seats.get(seatId).book();
            }
            return true;
            
        } finally {
            // Release in reverse order
            for (int i = acquired.size() - 1; i >= 0; i--) {
                acquired.get(i).unlock();
            }
        }
    }
}
```

**Deadlock Prevention**:
1. **Lock Ordering**: Always acquire locks in sorted order
2. **Timeout**: Use tryLock with timeout
3. **Rollback**: Release all locks if any acquisition fails

**Use Cases**: Movie booking (multiple seats), distributed systems, high-throughput services

---

### Pattern 4: ReadWriteLock ⭐⭐⭐

**When to Use**: Read-heavy workloads (90% reads, 10% writes)

**Pros**: Multiple readers simultaneously, better throughput for reads
**Cons**: Write starvation possible, more complex

```java
class InventoryService {
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();
    private final Lock writeLock = rwLock.writeLock();
    private final Map<String, Integer> inventory = new HashMap<>();
    
    // Multiple threads can read simultaneously
    public int getStock(String productId) {
        readLock.lock();
        try {
            return inventory.getOrDefault(productId, 0);
        } finally {
            readLock.unlock();
        }
    }
    
    // Only one thread can write
    public void updateStock(String productId, int quantity) {
        writeLock.lock();
        try {
            inventory.put(productId, quantity);
        } finally {
            writeLock.unlock();
        }
    }
}
```

**Use Cases**: Configuration management, caching, read-heavy services

---

### Pattern 5: Atomic Variables ⭐⭐⭐⭐

**When to Use**: Simple counters, flags, single-variable updates

**Pros**: Lock-free, high performance, no deadlock
**Cons**: Only for single variables, complex operations need CAS loop

```java
class TicketGenerator {
    private final AtomicLong ticketCounter = new AtomicLong(0);
    
    // Thread-safe, lock-free
    public long generateTicketId() {
        return ticketCounter.incrementAndGet();
    }
}

class SlotManager {
    private final AtomicInteger availableSlots = new AtomicInteger(100);
    
    // Compare-And-Swap (CAS) pattern
    public boolean reserveSlot() {
        while (true) {
            int current = availableSlots.get();
            if (current <= 0) return false;
            
            // Atomic: if current value matches, update to new value
            if (availableSlots.compareAndSet(current, current - 1)) {
                return true;
            }
            // CAS failed, retry
        }
    }
}
```

**Available Types**: AtomicInteger, AtomicLong, AtomicBoolean, AtomicReference

**Use Cases**: Counters, ID generation, simple flags, metrics

---

### Pattern 6: ConcurrentHashMap ⭐⭐⭐⭐

**When to Use**: Shared map with concurrent reads/writes

**Pros**: Thread-safe, high performance, no external locking needed
**Cons**: Weak consistency (size() approximate), no compound operations

```java
class SessionManager {
    private final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();
    
    // Thread-safe operations
    public void createSession(String id, Session session) {
        sessions.put(id, session);
    }
    
    public Session getSession(String id) {
        return sessions.get(id);
    }
    
    // Atomic compound operation
    public Session getOrCreateSession(String id) {
        return sessions.computeIfAbsent(id, k -> new Session(k));
    }
    
    // WRONG: Not atomic!
    public void incrementCounter(String key) {
        Integer count = sessions.get(key);
        sessions.put(key, count + 1); // Race condition!
    }
    
    // CORRECT: Atomic
    public void incrementCounterSafe(String key) {
        sessions.compute(key, (k, v) -> v == null ? 1 : v + 1);
    }
}
```

**Key Methods**:
- `putIfAbsent()` - Atomic put if key doesn't exist
- `computeIfAbsent()` - Atomic get-or-create
- `compute()` - Atomic update
- `merge()` - Atomic merge values

**Use Cases**: Caching, session management, any shared map

---

### Pattern 7: Double-Checked Locking ⭐⭐

**When to Use**: Lazy initialization with minimal locking overhead

**Pros**: Minimal locking after initialization, thread-safe
**Cons**: Requires volatile, tricky to implement correctly

```java
class ConfigManager {
    private volatile Config config; // MUST be volatile
    private final Object lock = new Object();
    
    public Config getConfig() {
        // First check (no locking)
        if (config == null) {
            synchronized(lock) {
                // Second check (with locking)
                if (config == null) {
                    config = loadConfig(); // Expensive operation
                }
            }
        }
        return config;
    }
}

// Modern alternative: Use static initialization
class ConfigManagerModern {
    private static class Holder {
        static final Config INSTANCE = loadConfig();
    }
    
    public static Config getConfig() {
        return Holder.INSTANCE; // Thread-safe, lazy, no locking
    }
}
```

**Use Cases**: Singleton initialization, expensive one-time setup

---

## 🎯 Pattern Selection Guide

### Decision Tree

```
Need concurrency control?
│
├─ Single variable update?
│  └─ Use AtomicInteger/AtomicLong (Pattern 5)
│
├─ Shared map?
│  └─ Use ConcurrentHashMap (Pattern 6)
│
├─ Read-heavy (>80% reads)?
│  └─ Use ReadWriteLock (Pattern 4)
│
├─ High contention on independent resources?
│  └─ Use Fine-Grained Locking (Pattern 3)
│
├─ Need timeout or tryLock?
│  └─ Use ReentrantLock (Pattern 2)
│
├─ Simple mutual exclusion?
│  └─ Use synchronized (Pattern 1)
│
└─ Lazy initialization?
   └─ Use Double-Checked Locking (Pattern 7)
```

### By Use Case

| Use Case | Pattern | Why |
|----------|---------|-----|
| **Movie Booking (multiple seats)** | Fine-Grained Locking | Prevent deadlock, high throughput |
| **Parking Lot (single slot)** | Fine-Grained Locking | Maximum parallelism |
| **Inventory Management** | ReadWriteLock | Read-heavy workload |
| **ID Generation** | AtomicLong | Lock-free, high performance |
| **Session Storage** | ConcurrentHashMap | Thread-safe map |
| **Rate Limiter** | AtomicInteger + synchronized | Counter + window management |
| **Cache** | ConcurrentHashMap + ReadWriteLock | Fast reads, safe writes |
| **Configuration** | Double-Checked Locking | Lazy init, minimal overhead |

---

## ⚠️ Common Pitfalls

### 1. Forgetting to Unlock
```java
// WRONG
lock.lock();
if (condition) {
    return; // Lock never released!
}
lock.unlock();

// CORRECT
lock.lock();
try {
    if (condition) return;
} finally {
    lock.unlock(); // Always executes
}
```

### 2. Deadlock from Lock Ordering
```java
// WRONG - Deadlock possible
void transfer(Account from, Account to, int amount) {
    synchronized(from) {
        synchronized(to) {
            from.debit(amount);
            to.credit(amount);
        }
    }
}

// CORRECT - Always lock in same order
void transfer(Account from, Account to, int amount) {
    Account first = from.getId() < to.getId() ? from : to;
    Account second = from.getId() < to.getId() ? to : from;
    
    synchronized(first) {
        synchronized(second) {
            from.debit(amount);
            to.credit(amount);
        }
    }
}
```

### 3. Non-Atomic Compound Operations
```java
// WRONG - Race condition
if (map.containsKey(key)) {
    map.put(key, map.get(key) + 1);
}

// CORRECT - Atomic
map.compute(key, (k, v) -> v == null ? 1 : v + 1);
```

### 4. Missing volatile for Flags
```java
// WRONG - Changes may not be visible
private boolean stopped = false;

void stop() { stopped = true; }
void run() { while (!stopped) { work(); } }

// CORRECT
private volatile boolean stopped = false;
```

### 5. Synchronized on Wrong Object
```java
// WRONG - Each instance has different lock
public void method() {
    synchronized(new Object()) { // New object every time!
        // Critical section
    }
}

// CORRECT
private final Object lock = new Object();
public void method() {
    synchronized(lock) {
        // Critical section
    }
}
```

---

## 💡 Interview Tips

### 1. Always Discuss Concurrency
Even if not asked, mention: "For production, we'd need concurrency control here..."

### 2. Start Simple, Then Optimize
1. First: Identify shared resources
2. Then: Choose simplest pattern (synchronized)
3. Finally: Optimize if needed (fine-grained locking)

### 3. Document Thread-Safety
```java
/**
 * Thread-Safety: Thread-safe using ReentrantLock
 * Concurrency: Caller MUST hold lock before calling park()
 */
class ParkingSlot {
    // ...
}
```

### 4. Test Concurrency
```java
void testConcurrentBooking() throws Exception {
    ExecutorService executor = Executors.newFixedThreadPool(10);
    List<Future<Boolean>> futures = new ArrayList<>();
    
    // 10 threads trying to book same seat
    for (int i = 0; i < 10; i++) {
        futures.add(executor.submit(() -> bookSeat("A1")));
    }
    
    // Only 1 should succeed
    long successCount = futures.stream()
        .map(f -> { try { return f.get(); } catch(Exception e) { return false; } })
        .filter(b -> b)
        .count();
    
    assertEquals(1, successCount);
}
```

### 5. Explain Trade-offs
"I'm using fine-grained locking for maximum throughput, but it uses more memory and has deadlock risk. For lower traffic, synchronized would be simpler."

---

## 📖 Learning Path

### Week 1: Basics
- [ ] Understand race conditions, atomicity, visibility
- [ ] Practice synchronized and ReentrantLock
- [ ] Implement simple counter with both approaches

### Week 2: Advanced
- [ ] Master fine-grained locking with deadlock prevention
- [ ] Learn ReadWriteLock and atomic variables
- [ ] Implement parking lot with slot-level locking

### Week 3: Real-World
- [ ] Implement movie booking with multiple seat booking
- [ ] Add timeout handling and rollback
- [ ] Write comprehensive concurrency tests

### Week 4: Interview Prep
- [ ] Review all 7 patterns
- [ ] Practice pattern selection for different scenarios
- [ ] Mock interview: Explain concurrency strategy in 5 minutes

---

## 🎓 Summary

### Must-Know for Interviews
1. **Fine-Grained Locking** - Most impressive, shows senior-level thinking
2. **ReentrantLock** - Versatile, handles most scenarios
3. **ConcurrentHashMap** - Essential for shared state
4. **Atomic Variables** - Simple, performant counters

### Key Principles
- **Minimize Critical Sections** - Lock only what's necessary
- **Lock Ordering** - Prevent deadlocks
- **Fail Fast** - Use timeouts, don't block forever
- **Document Thread-Safety** - Make guarantees explicit
- **Test Concurrency** - Race conditions are hard to debug

### Interview Template
1. Identify shared resources
2. Identify critical sections
3. Choose pattern (start simple)
4. Implement with proper locking
5. Document thread-safety
6. Write concurrency tests
7. Discuss trade-offs

---

**Next Steps**: 
- Review [ConcurrencyPatterns.java](./ConcurrencyPatterns.java) for runnable examples
- Check [QUICK_REFERENCE.md](./QUICK_REFERENCE.md) for interview cheat sheet
