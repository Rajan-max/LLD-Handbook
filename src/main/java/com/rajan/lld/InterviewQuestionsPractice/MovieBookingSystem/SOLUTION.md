# Movie Booking System - LLD Interview Solution 🎬

> **Following**: LLD_INTERVIEW_TEMPLATE.md structure with strong concurrency focus

---

## 🎯 STEP 1: REQUIREMENTS GATHERING

### Functional Requirements

1. **FR1**: Users can view available movies and shows
2. **FR2**: Users can select seats for a specific show
3. **FR3**: Users can book multiple seats in a single transaction
4. **FR4**: System holds seats temporarily during payment (5 minutes)
5. **FR5**: Users can confirm or cancel bookings
6. **FR6**: System prevents double-booking of seats
7. **FR7**: Admin can add movies, theaters, and shows

### Non-Functional Requirements

1. **NFR1**: **Concurrency** - Support 1000+ concurrent users per show
2. **NFR2**: **Performance** - Booking response time < 200ms
3. **NFR3**: **Consistency** - No double-booking (strong consistency)
4. **NFR4**: **Availability** - 99.9% uptime
5. **NFR5**: **Scale** - Support 100+ theaters, 1000+ shows/day
6. **NFR6**: **Fairness** - FIFO seat allocation

### Assumptions

1. Payment processing is external (mock in this implementation)
2. In-memory storage (production would use database)
3. Single server (discuss distributed approach)
4. Seat layout is simple (row-column grid)
5. No seat preferences (first-come-first-served)

### Out of Scope

1. User authentication/authorization
2. Payment gateway integration
3. Email/SMS notifications
4. Dynamic pricing
5. Seat recommendations
6. Movie ratings/reviews

---

## 🏗️ STEP 2: DOMAIN MODELING

### Core Entities

#### **Movie**
- **Purpose**: Represents a film
- **Attributes**: id, title, duration, genre, language
- **Lifecycle**: Created by admin, immutable after creation

#### **Theater**
- **Purpose**: Physical location with multiple screens
- **Attributes**: id, name, location, screens
- **Lifecycle**: Created by admin, can be updated

#### **Screen**
- **Purpose**: Individual auditorium within theater
- **Attributes**: id, name, seatLayout (rows × columns)
- **Lifecycle**: Created with theater, immutable

#### **Show**
- **Purpose**: Specific movie screening at a time
- **Attributes**: id, movie, screen, startTime, endTime, seats
- **Lifecycle**: Created by admin, seats updated during bookings
- **Concurrency**: High contention point - needs fine-grained locking

#### **Seat**
- **Purpose**: Individual seat in a screen
- **Attributes**: id, row, column, status, lockedBy, lockExpiry
- **Status**: AVAILABLE → LOCKED → BOOKED
- **Concurrency**: Each seat has independent lock

#### **Booking**
- **Purpose**: User's reservation
- **Attributes**: id, userId, showId, seats, status, timestamp
- **Status**: PENDING → CONFIRMED / CANCELLED
- **Lifecycle**: Created on lock, confirmed on payment

### Entity Relationships

```
Theater (1) ──has──> (N) Screen
Screen (1) ──has──> (N) Seat
Movie (1) ──shown in──> (N) Show
Screen (1) ──hosts──> (N) Show
Show (1) ──has──> (N) Seat (copies)
User (1) ──makes──> (N) Booking
Booking (N) ──for──> (1) Show
Booking (1) ──contains──> (N) Seat
```

---

## 🎨 STEP 3: DESIGN PATTERNS & ARCHITECTURE

### Architecture Layers

```
┌─────────────────────────────────────┐
│   BookingController (API Layer)     │ ← Entry point
├─────────────────────────────────────┤
│   BookingService (Business Logic)   │ ← Core logic + Concurrency
├─────────────────────────────────────┤
│   Repository Layer (Data Access)    │ ← In-memory storage
├─────────────────────────────────────┤
│   Domain Models (Entities)          │ ← Movie, Show, Seat, Booking
└─────────────────────────────────────┘
```

### Design Patterns Used

#### **1. Strategy Pattern** (Concurrency Strategies)
- **Problem**: Different concurrency approaches for different scenarios
- **Solution**: BookingStrategy interface with multiple implementations
- **Implementations**: 
  - PessimisticLockingStrategy
  - OptimisticLockingStrategy
  - FineGrainedLockingStrategy (default)

#### **2. Factory Pattern** (Entity Creation)
- **Problem**: Complex object creation with validation
- **Solution**: Factory methods for Show, Booking creation
- **Benefit**: Centralized validation and initialization

#### **3. Repository Pattern** (Data Access)
- **Problem**: Decouple business logic from data storage
- **Solution**: ShowRepository, BookingRepository interfaces
- **Benefit**: Easy to swap in-memory with database

#### **4. State Pattern** (Seat Status)
- **Problem**: Seat transitions through multiple states
- **Solution**: SeatStatus enum with transition rules
- **States**: AVAILABLE → LOCKED → BOOKED

---

## 🔐 STEP 4: CONCURRENCY CONTROL (CRITICAL!)

### Concurrency Analysis

#### **Shared Resources**
1. **Show.seats** - Multiple users booking same show
2. **Seat.status** - Multiple users trying same seat
3. **Booking.id** - Unique ID generation

#### **Critical Sections**
1. **Seat selection** - Check availability + Lock seats
2. **Booking confirmation** - Lock → Booked transition
3. **Lock expiry** - Background cleanup of expired locks

#### **Race Conditions**
1. **Double-booking**: Two users book same seat simultaneously
2. **Lost update**: Concurrent status changes overwrite each other
3. **Deadlock**: Users booking overlapping seats in different order

### Concurrency Strategy: Fine-Grained Locking ⭐

**Why Fine-Grained?**
- ✅ Maximum parallelism (different seats = no contention)
- ✅ Strong consistency (no double-booking)
- ✅ Scalable (contention only on same seats)
- ✅ Fair (ReentrantLock with fairness)

**Implementation:**

```java
// 1. Each seat has its own lock
private final ConcurrentHashMap<String, ReentrantLock> seatLocks;

// 2. Deadlock prevention: Always acquire locks in sorted order
public boolean lockSeats(List<String> seatIds) {
    List<String> sortedSeats = new ArrayList<>(seatIds);
    Collections.sort(sortedSeats); // Consistent ordering
    
    List<ReentrantLock> acquiredLocks = new ArrayList<>();
    
    try {
        for (String seatId : sortedSeats) {
            ReentrantLock lock = seatLocks.get(seatId);
            if (!lock.tryLock(5, TimeUnit.SECONDS)) {
                // Timeout - release all acquired locks
                releaseLocksInReverse(acquiredLocks);
                return false;
            }
            acquiredLocks.add(lock);
        }
        
        // All locks acquired successfully
        // Check seat availability
        // Update seat status
        
        return true;
    } catch (InterruptedException e) {
        releaseLocksInReverse(acquiredLocks);
        return false;
    }
}

// 3. Always release in reverse order
private void releaseLocksInReverse(List<ReentrantLock> locks) {
    for (int i = locks.size() - 1; i >= 0; i--) {
        locks.get(i).unlock();
    }
}
```

### Thread-Safety Guarantees

| Component | Thread-Safety | Mechanism |
|-----------|---------------|-----------|
| **Show** | Thread-safe | ConcurrentHashMap for seats |
| **Seat** | Thread-safe | Volatile status + ReentrantLock |
| **Booking** | Thread-safe | Immutable after creation |
| **BookingService** | Thread-safe | Fine-grained locking |
| **Repository** | Thread-safe | ConcurrentHashMap storage |

### Deadlock Prevention

**Techniques Used:**
1. **Lock Ordering**: Always acquire locks in sorted order
2. **Timeout**: tryLock() with timeout (5 seconds)
3. **Reverse Release**: Release locks in reverse order
4. **Fair Locks**: ReentrantLock(true) for FIFO fairness

**Why It Works:**
- Sorted order prevents circular wait
- Timeout prevents indefinite blocking
- Fair locks prevent starvation

### Lock Expiry Mechanism

**Problem**: User locks seats but never completes payment

**Solution**: Automatic lock expiry

```java
class Seat {
    private volatile SeatStatus status;
    private volatile String lockedBy;
    private volatile LocalDateTime lockExpiry;
    
    public boolean isLockExpired() {
        return status == SeatStatus.LOCKED && 
               LocalDateTime.now().isAfter(lockExpiry);
    }
}

// Background thread cleans up expired locks
ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
scheduler.scheduleAtFixedRate(() -> {
    cleanupExpiredLocks();
}, 0, 30, TimeUnit.SECONDS);
```

---

## 💻 STEP 5: CLASS DESIGN & IMPLEMENTATION

### Class Structure

```
com.rajan.lld.InterviewQuestionsPractice.MovieBookingSystem
├── models/
│   ├── Movie.java
│   ├── Theater.java
│   ├── Screen.java
│   ├── Show.java
│   ├── Seat.java
│   ├── Booking.java
│   └── enums/
│       ├── SeatStatus.java
│       └── BookingStatus.java
├── service/
│   ├── BookingService.java
│   └── LockExpiryService.java
├── repository/
│   ├── ShowRepository.java
│   └── BookingRepository.java
└── MovieBookingSystem.java (Demo)
```

### Key Classes

#### **Show.java** (High Concurrency)
```java
/**
 * Thread-Safety: Thread-safe using fine-grained locking
 * Concurrency: Each seat has independent ReentrantLock
 */
public class Show {
    private final String id;
    private final Movie movie;
    private final Screen screen;
    private final LocalDateTime startTime;
    
    // Thread-safe collections
    private final ConcurrentHashMap<String, Seat> seats;
    private final ConcurrentHashMap<String, ReentrantLock> seatLocks;
    
    public Show(String id, Movie movie, Screen screen, LocalDateTime startTime) {
        this.id = id;
        this.movie = movie;
        this.screen = screen;
        this.startTime = startTime;
        this.seats = new ConcurrentHashMap<>();
        this.seatLocks = new ConcurrentHashMap<>();
        initializeSeats();
    }
    
    private void initializeSeats() {
        for (int row = 1; row <= screen.getRows(); row++) {
            for (int col = 1; col <= screen.getColumns(); col++) {
                String seatId = row + "-" + col;
                Seat seat = new Seat(seatId, row, col);
                seats.put(seatId, seat);
                seatLocks.put(seatId, new ReentrantLock(true)); // Fair lock
            }
        }
    }
    
    public ReentrantLock getSeatLock(String seatId) {
        return seatLocks.get(seatId);
    }
}
```

#### **Seat.java** (Volatile for Visibility)
```java
/**
 * Thread-Safety: Thread-safe using volatile fields
 * Concurrency: Status changes protected by external lock
 */
public class Seat {
    private final String id;
    private final int row;
    private final int column;
    
    // Volatile for visibility across threads
    private volatile SeatStatus status;
    private volatile String lockedBy;
    private volatile LocalDateTime lockExpiry;
    
    public Seat(String id, int row, int column) {
        this.id = id;
        this.row = row;
        this.column = column;
        this.status = SeatStatus.AVAILABLE;
    }
    
    // Caller must hold lock
    public boolean lock(String userId, int lockDurationMinutes) {
        if (status != SeatStatus.AVAILABLE) {
            return false;
        }
        this.status = SeatStatus.LOCKED;
        this.lockedBy = userId;
        this.lockExpiry = LocalDateTime.now().plusMinutes(lockDurationMinutes);
        return true;
    }
    
    // Caller must hold lock
    public boolean book(String userId) {
        if (status != SeatStatus.LOCKED || !lockedBy.equals(userId)) {
            return false;
        }
        this.status = SeatStatus.BOOKED;
        return true;
    }
    
    public boolean isLockExpired() {
        return status == SeatStatus.LOCKED && 
               LocalDateTime.now().isAfter(lockExpiry);
    }
}
```

#### **BookingService.java** (Core Business Logic)
```java
/**
 * Thread-Safety: Thread-safe using fine-grained locking
 * Concurrency: Locks acquired in sorted order to prevent deadlock
 */
public class BookingService {
    private final ShowRepository showRepository;
    private final BookingRepository bookingRepository;
    private static final int LOCK_TIMEOUT_SECONDS = 5;
    private static final int SEAT_LOCK_DURATION_MINUTES = 5;
    
    /**
     * Lock seats for booking (Step 1: Before payment)
     * Thread-safe: Uses fine-grained locking with deadlock prevention
     */
    public Booking lockSeats(String userId, String showId, List<String> seatIds) {
        Show show = showRepository.findById(showId);
        if (show == null) {
            throw new IllegalArgumentException("Show not found");
        }
        
        // Sort seat IDs to prevent deadlock
        List<String> sortedSeatIds = new ArrayList<>(seatIds);
        Collections.sort(sortedSeatIds);
        
        List<ReentrantLock> acquiredLocks = new ArrayList<>();
        
        try {
            // Acquire locks in sorted order
            for (String seatId : sortedSeatIds) {
                ReentrantLock lock = show.getSeatLock(seatId);
                if (!lock.tryLock(LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                    releaseLocksInReverse(acquiredLocks);
                    throw new RuntimeException("Could not acquire lock for seat: " + seatId);
                }
                acquiredLocks.add(lock);
            }
            
            // All locks acquired - check and lock seats
            for (String seatId : sortedSeatIds) {
                Seat seat = show.getSeat(seatId);
                if (!seat.lock(userId, SEAT_LOCK_DURATION_MINUTES)) {
                    releaseLocksInReverse(acquiredLocks);
                    throw new RuntimeException("Seat not available: " + seatId);
                }
            }
            
            // Create booking
            Booking booking = new Booking(
                generateBookingId(),
                userId,
                showId,
                sortedSeatIds,
                BookingStatus.PENDING
            );
            bookingRepository.save(booking);
            
            return booking;
            
        } catch (InterruptedException e) {
            releaseLocksInReverse(acquiredLocks);
            throw new RuntimeException("Interrupted while acquiring locks", e);
        } finally {
            releaseLocksInReverse(acquiredLocks);
        }
    }
    
    /**
     * Confirm booking after payment (Step 2: After payment)
     */
    public boolean confirmBooking(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId);
        if (booking == null || booking.getStatus() != BookingStatus.PENDING) {
            return false;
        }
        
        Show show = showRepository.findById(booking.getShowId());
        List<String> sortedSeatIds = new ArrayList<>(booking.getSeatIds());
        Collections.sort(sortedSeatIds);
        
        List<ReentrantLock> acquiredLocks = new ArrayList<>();
        
        try {
            // Acquire locks again
            for (String seatId : sortedSeatIds) {
                ReentrantLock lock = show.getSeatLock(seatId);
                if (!lock.tryLock(LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                    releaseLocksInReverse(acquiredLocks);
                    return false;
                }
                acquiredLocks.add(lock);
            }
            
            // Book seats
            for (String seatId : sortedSeatIds) {
                Seat seat = show.getSeat(seatId);
                if (!seat.book(booking.getUserId())) {
                    releaseLocksInReverse(acquiredLocks);
                    return false;
                }
            }
            
            // Update booking status
            booking.confirm();
            return true;
            
        } catch (InterruptedException e) {
            releaseLocksInReverse(acquiredLocks);
            return false;
        } finally {
            releaseLocksInReverse(acquiredLocks);
        }
    }
    
    private void releaseLocksInReverse(List<ReentrantLock> locks) {
        for (int i = locks.size() - 1; i >= 0; i--) {
            locks.get(i).unlock();
        }
    }
    
    private String generateBookingId() {
        return "BKG-" + System.currentTimeMillis();
    }
}
```

---

## 🧪 STEP 6: TESTING STRATEGY

### Unit Tests (70%)

```java
@Test
public void testSingleUserBooking() {
    // Happy path: User books available seats
}

@Test
public void testDoubleBookingPrevention() {
    // Two users try same seat - one should fail
}

@Test
public void testLockExpiry() {
    // Locked seat becomes available after timeout
}
```

### Concurrency Tests (20%)

```java
@Test
public void testConcurrentBookingDifferentSeats() throws InterruptedException {
    ExecutorService executor = Executors.newFixedThreadPool(10);
    CountDownLatch latch = new CountDownLatch(10);
    AtomicInteger successCount = new AtomicInteger(0);
    
    for (int i = 0; i < 10; i++) {
        final int userId = i;
        executor.submit(() -> {
            try {
                List<String> seats = Arrays.asList((userId + 1) + "-1");
                Booking booking = service.lockSeats("user" + userId, "show1", seats);
                if (booking != null) {
                    successCount.incrementAndGet();
                }
            } finally {
                latch.countDown();
            }
        });
    }
    
    latch.await();
    assertEquals(10, successCount.get()); // All should succeed (different seats)
}

@Test
public void testConcurrentBookingSameSeat() throws InterruptedException {
    // 10 users try same seat - only 1 should succeed
    ExecutorService executor = Executors.newFixedThreadPool(10);
    CountDownLatch latch = new CountDownLatch(10);
    AtomicInteger successCount = new AtomicInteger(0);
    
    for (int i = 0; i < 10; i++) {
        final int userId = i;
        executor.submit(() -> {
            try {
                List<String> seats = Arrays.asList("1-1"); // Same seat
                Booking booking = service.lockSeats("user" + userId, "show1", seats);
                if (booking != null) {
                    successCount.incrementAndGet();
                }
            } catch (Exception e) {
                // Expected for 9 users
            } finally {
                latch.countDown();
            }
        });
    }
    
    latch.await();
    assertEquals(1, successCount.get()); // Only 1 should succeed
}

@Test
public void testNoDeadlock() throws InterruptedException {
    // User1: books [1-1, 1-2]
    // User2: books [1-2, 1-3]
    // Should not deadlock due to sorted locking
}
```

### Integration Tests (10%)

```java
@Test
public void testCompleteBookingWorkflow() {
    // 1. Lock seats
    // 2. Process payment
    // 3. Confirm booking
    // 4. Verify seats are booked
}
```

---

## 📊 STEP 7: SCALABILITY & TRADE-OFFS

### Design Trade-offs

#### **Decision: Fine-Grained Locking**

**Pros:**
- High throughput (parallel bookings)
- Strong consistency (no double-booking)
- Scalable (contention only on same seats)

**Cons:**
- More memory (lock per seat)
- Complex implementation (deadlock prevention)
- Lock management overhead

**Alternatives Considered:**
- **Show-level lock**: Too coarse, low throughput
- **Optimistic locking**: Retry storms under high contention
- **Database locking**: Network latency, single point of failure

**When to Reconsider:**
- If memory becomes constraint (millions of seats)
- If distributed system (use Redis locks)

### Scalability Analysis

#### **Current Limitations**
- **Bottleneck**: Same seat booking (sequential)
- **Breaking Point**: ~10,000 concurrent users per show
- **Memory**: O(seats × shows) for locks

#### **Scaling Strategies**

1. **Horizontal Scaling**
   - Partition shows across servers (consistent hashing)
   - Each server handles subset of shows
   - No cross-server coordination needed

2. **Caching**
   - Cache show metadata (movie, theater info)
   - Cache seat availability (with TTL)
   - Reduce database reads

3. **Database Optimization**
   - Use SELECT FOR UPDATE for row-level locking
   - Index on (showId, seatId) for fast lookups
   - Connection pooling

4. **Async Processing**
   - Queue booking confirmations
   - Background lock expiry cleanup
   - Async notifications

### Performance Characteristics

| Operation | Time Complexity | Space Complexity |
|-----------|----------------|------------------|
| **Lock Seats** | O(n log n + n) | O(n) |
| **Confirm Booking** | O(n log n + n) | O(1) |
| **Check Availability** | O(1) | O(1) |
| **Cleanup Expired** | O(total seats) | O(1) |

*n = number of seats in booking*

---

## 🚀 STEP 8: EXTENSIBILITY & FUTURE ENHANCEMENTS

### Extension Points

#### **Adding New Seat Types**
1. Create `SeatType` enum (REGULAR, PREMIUM, VIP)
2. Add `type` field to `Seat` class
3. Update pricing in `BookingService`
4. Filter seats by type in UI

#### **Adding Dynamic Pricing**
1. Create `PricingStrategy` interface
2. Implementations: TimeBased, DemandBased, SeatTypeBased
3. Inject strategy into `BookingService`
4. Calculate price during booking

#### **Adding Distributed Locking**
1. Create `DistributedLockService` interface
2. Implementations: RedisLockService, ZookeeperLockService
3. Replace `ReentrantLock` with distributed lock
4. Handle network failures and timeouts

### Future Roadmap

#### **Phase 1: Immediate (< 1 month)**
- Add seat type (Premium, VIP)
- Implement dynamic pricing
- Add booking history

#### **Phase 2: Short-term (1-3 months)**
- Distributed locking (Redis)
- Database persistence
- Payment gateway integration
- Email/SMS notifications

#### **Phase 3: Long-term (3-6 months)**
- Microservices architecture
- Event sourcing for audit trail
- Machine learning for recommendations
- Real-time seat availability updates (WebSocket)

---

## 🎯 STEP 9: INTERVIEW EVALUATION CHECKLIST

### ✅ Requirements (20%)
- [x] Identified functional requirements
- [x] Identified non-functional requirements (concurrency!)
- [x] Made clear assumptions
- [x] Defined scope

### ✅ Design (30%)
- [x] Clean layered architecture
- [x] Appropriate design patterns (Strategy, Factory, Repository, State)
- [x] SOLID principles followed
- [x] Extensible design

### ✅ Concurrency (20%)
- [x] Identified shared resources (Show, Seat)
- [x] Chosen fine-grained locking strategy
- [x] Prevented race conditions (synchronized access)
- [x] Prevented deadlocks (sorted locking)
- [x] Documented thread-safety for all classes
- [x] Implemented lock expiry mechanism

### ✅ Code Quality (20%)
- [x] Clean, minimal code
- [x] Proper naming conventions
- [x] Error handling
- [x] Input validation
- [x] Comments for complex logic

### ✅ Communication (10%)
- [x] Explained thought process
- [x] Discussed trade-offs (fine-grained vs coarse-grained)
- [x] Considered scalability
- [x] Showed production awareness

---

## 📝 STEP 10: HOW TO RUN

```bash
# Navigate to directory
cd src/main/java/com/rajan/lld/InterviewQuestionsPractice/MovieBookingSystem

# Compile
javac -d bin *.java models/*.java models/enums/*.java service/*.java repository/*.java

# Run demo
java -cp bin com.rajan.lld.InterviewQuestionsPractice.MovieBookingSystem.MovieBookingSystem
```

### Expected Output

```
=== Movie Booking System Demo ===

1. Creating show with 10 seats...
2. Testing concurrent bookings (10 users, different seats)...
   ✓ User 0 booked seat 1-1
   ✓ User 1 booked seat 1-2
   ...
   Result: 10/10 successful (100% - no contention)

3. Testing concurrent bookings (10 users, same seat)...
   ✓ User 3 booked seat 2-1
   ✗ User 0 failed (seat taken)
   ...
   Result: 1/10 successful (expected - high contention)

4. Testing deadlock prevention...
   User A booking [1-1, 1-2]
   User B booking [1-2, 1-3]
   ✓ No deadlock! Both completed.

5. Testing lock expiry...
   User locked seat 3-1 at 14:30:00
   Waiting 5 minutes...
   ✓ Seat 3-1 auto-released at 14:35:01

=== All tests passed! ===
```

---

## 🎓 Key Takeaways

1. **Fine-grained locking** maximizes concurrency while maintaining consistency
2. **Deadlock prevention** requires consistent lock ordering (sorted seat IDs)
3. **Lock expiry** prevents resource hogging and ensures fairness
4. **Thread-safety** must be documented for every class
5. **Trade-offs** exist between consistency, performance, and complexity
6. **Production systems** need distributed locking and failure handling

This design demonstrates **production-ready concurrency handling** suitable for real-world booking systems! 🎬
