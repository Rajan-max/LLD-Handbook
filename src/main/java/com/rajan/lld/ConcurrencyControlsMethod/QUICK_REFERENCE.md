# Concurrency Quick Reference - Interview Cheat Sheet 📋

---

## 🎯 Pattern Selection (30 seconds)

```
┌─────────────────────────────────────────────────────────────┐
│ WHAT DO YOU NEED?                                           │
├─────────────────────────────────────────────────────────────┤
│ Counter/ID generation          → AtomicInteger/AtomicLong   │
│ Shared map                     → ConcurrentHashMap          │
│ Read-heavy (>80% reads)        → ReadWriteLock              │
│ Independent resources          → Fine-Grained Locking       │
│ Need timeout/tryLock           → ReentrantLock              │
│ Simple mutual exclusion        → synchronized               │
│ Lazy initialization            → Double-Checked Locking     │
└─────────────────────────────────────────────────────────────┘
```

---

## 📝 Code Templates

### Template 1: ReentrantLock (Most Common)

```java
class Service {
    private final ReentrantLock lock = new ReentrantLock(true); // fair=true
    
    public boolean operation() {
        try {
            if (lock.tryLock(5, TimeUnit.SECONDS)) {
                try {
                    // Critical section
                    return doWork();
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
}
```

### Template 2: Fine-Grained Locking (Most Impressive)

```java
class BookingService {
    private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();
    
    public boolean bookMultiple(List<String> ids) {
        // CRITICAL: Sort to prevent deadlock
        List<String> sorted = new ArrayList<>(ids);
        Collections.sort(sorted);
        
        List<ReentrantLock> acquired = new ArrayList<>();
        
        try {
            // Acquire all locks
            for (String id : sorted) {
                ReentrantLock lock = locks.get(id);
                if (!lock.tryLock(5, TimeUnit.SECONDS)) {
                    // Rollback
                    for (ReentrantLock l : acquired) l.unlock();
                    return false;
                }
                acquired.add(lock);
            }
            
            // All locks acquired - do work
            for (String id : sorted) {
                book(id);
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

### Template 3: ReadWriteLock (Read-Heavy)

```java
class Cache {
    private final Map<String, String> data = new HashMap<>();
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();
    private final Lock writeLock = rwLock.writeLock();
    
    public String get(String key) {
        readLock.lock();
        try {
            return data.get(key);
        } finally {
            readLock.unlock();
        }
    }
    
    public void put(String key, String value) {
        writeLock.lock();
        try {
            data.put(key, value);
        } finally {
            writeLock.unlock();
        }
    }
}
```

### Template 4: Atomic Variables (Counters)

```java
class Counter {
    private final AtomicLong counter = new AtomicLong(0);
    
    public long increment() {
        return counter.incrementAndGet();
    }
    
    // Compare-And-Swap pattern
    public boolean reserve() {
        while (true) {
            long current = counter.get();
            if (current <= 0) return false;
            if (counter.compareAndSet(current, current - 1)) {
                return true;
            }
        }
    }
}
```

### Template 5: ConcurrentHashMap (Shared State)

```java
class SessionManager {
    private final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();
    
    // Atomic operations
    public Session getOrCreate(String id) {
        return sessions.computeIfAbsent(id, k -> new Session(k));
    }
    
    public void increment(String id) {
        sessions.compute(id, (k, v) -> v == null ? 1 : v + 1);
    }
}
```

---

## 🚨 Common Mistakes (Avoid These!)

### ❌ Mistake 1: Forgetting to Unlock
```java
// WRONG
lock.lock();
if (condition) return; // Lock never released!
lock.unlock();

// CORRECT
lock.lock();
try {
    if (condition) return;
} finally {
    lock.unlock();
}
```

### ❌ Mistake 2: Deadlock from Lock Ordering
```java
// WRONG - Can deadlock
synchronized(resourceA) {
    synchronized(resourceB) { }
}

// CORRECT - Always same order
Resource first = idA < idB ? resourceA : resourceB;
Resource second = idA < idB ? resourceB : resourceA;
synchronized(first) {
    synchronized(second) { }
}
```

### ❌ Mistake 3: Non-Atomic Compound Operations
```java
// WRONG - Race condition
if (map.containsKey(key)) {
    map.put(key, map.get(key) + 1);
}

// CORRECT - Atomic
map.compute(key, (k, v) -> v == null ? 1 : v + 1);
```

### ❌ Mistake 4: Missing volatile
```java
// WRONG - Changes may not be visible
private boolean stopped = false;

// CORRECT
private volatile boolean stopped = false;
```

---

## 💬 Interview Script (2 minutes)

### Step 1: Identify Shared Resources (20 sec)
"The shared resources here are [seats/slots/inventory]. Multiple threads will access these concurrently."

### Step 2: Identify Critical Sections (20 sec)
"The critical section is the check-and-book operation. We need atomicity to prevent double-booking."

### Step 3: Choose Pattern (30 sec)
"I'll use fine-grained locking with one lock per [seat/slot]. This gives maximum parallelism since different resources don't block each other."

### Step 4: Explain Deadlock Prevention (30 sec)
"For multiple resource booking, I'll sort the IDs before acquiring locks. This ensures consistent lock ordering and prevents circular wait."

### Step 5: Mention Testing (20 sec)
"I'd write concurrency tests with 10+ threads trying to book the same resource, verifying only one succeeds."

---

## 📊 Pattern Comparison

| Pattern | Complexity | Performance | Use When |
|---------|-----------|-------------|----------|
| **synchronized** | ⭐ | ⭐⭐ | Simple, low contention |
| **ReentrantLock** | ⭐⭐ | ⭐⭐⭐ | Need timeout/tryLock |
| **Fine-Grained** | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | High contention, independent resources |
| **ReadWriteLock** | ⭐⭐⭐ | ⭐⭐⭐⭐ | Read-heavy (>80%) |
| **Atomic** | ⭐ | ⭐⭐⭐⭐⭐ | Single variable |
| **ConcurrentHashMap** | ⭐⭐ | ⭐⭐⭐⭐ | Shared map |
| **Double-Checked** | ⭐⭐⭐ | ⭐⭐⭐⭐ | Lazy init |

---

## 🎯 By Problem Type

### Movie Booking System
```java
// Multiple seats → Fine-Grained Locking
ConcurrentHashMap<String, ReentrantLock> seatLocks;
// Sort seat IDs before locking
// Timeout: 5 seconds
// Rollback on failure
```

### Parking Lot System
```java
// Single slot → Fine-Grained Locking
ConcurrentHashMap<String, ReentrantLock> slotLocks;
// tryLock() for non-blocking
// No deadlock risk (single lock per operation)
```

### Inventory Management
```java
// Read-heavy → ReadWriteLock
ReadWriteLock rwLock = new ReentrantReadWriteLock();
// Multiple readers, exclusive writer
```

### Rate Limiter
```java
// Counter → AtomicInteger
AtomicInteger requestCount = new AtomicInteger(0);
// Lock-free, high performance
```

### Cache
```java
// Shared map → ConcurrentHashMap
ConcurrentHashMap<String, Value> cache;
// computeIfAbsent for get-or-create
```

---

## 🧪 Testing Template

```java
@Test
void testConcurrentBooking() throws Exception {
    ExecutorService executor = Executors.newFixedThreadPool(10);
    List<Future<Boolean>> futures = new ArrayList<>();
    
    // 10 threads trying to book same resource
    for (int i = 0; i < 10; i++) {
        futures.add(executor.submit(() -> service.book("resource-1")));
    }
    
    executor.shutdown();
    executor.awaitTermination(5, TimeUnit.SECONDS);
    
    // Only 1 should succeed
    long successCount = futures.stream()
        .map(f -> { try { return f.get(); } catch(Exception e) { return false; } })
        .filter(b -> b)
        .count();
    
    assertEquals(1, successCount);
}
```

---

## 📝 Documentation Template

```java
/**
 * Thread-Safety: Thread-safe using [pattern name]
 * Concurrency: [Explain locking strategy]
 * Deadlock Prevention: [Explain if applicable]
 */
class MyClass {
    // Implementation
}
```

---

## ⏱️ Time Allocation (45-min interview)

- **5 min**: Requirements + identify shared resources
- **5 min**: Choose concurrency pattern + explain
- **25 min**: Implementation
- **5 min**: Testing strategy
- **5 min**: Trade-offs discussion

---

## 🎓 Key Principles (Memorize!)

1. **Minimize Critical Sections** - Lock only what's necessary
2. **Lock Ordering** - Always acquire in sorted order
3. **Fail Fast** - Use timeouts, don't block forever
4. **Document Thread-Safety** - Make guarantees explicit
5. **Test Concurrency** - Race conditions are hard to debug

---

## 🔥 Pro Tips

1. **Always mention concurrency** even if not asked
2. **Start simple** (synchronized) then optimize
3. **Explain trade-offs** (memory vs throughput)
4. **Show deadlock awareness** (lock ordering)
5. **Mention testing** (10 threads, verify only 1 succeeds)

---

## 📚 Quick Lookup

### When interviewer says...

| Phrase | Pattern to Use |
|--------|---------------|
| "Multiple users booking seats" | Fine-Grained Locking |
| "High read traffic" | ReadWriteLock |
| "Generate unique IDs" | AtomicLong |
| "Prevent double-booking" | ReentrantLock + tryLock |
| "Session management" | ConcurrentHashMap |
| "Initialize once" | Double-Checked Locking |

---

## 🚀 Interview Confidence Boosters

### Opening Statement
"Before we start coding, I want to identify the concurrency requirements. The shared resources are [X], and we need to prevent [race condition]. I'll use [pattern] because [reason]."

### Closing Statement
"For production, I'd add comprehensive concurrency tests with 100+ threads to verify no race conditions or deadlocks. I'd also add metrics to monitor lock contention and timeout rates."

---

**Print this page and keep it handy during interviews!** 🎯
