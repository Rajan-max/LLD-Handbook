# Hotel Booking System - LLD Interview Solution 🏨

> **Following**: LLD_INTERVIEW_TEMPLATE.md structure with strong concurrency focus

---

## 🎯 STEP 1: REQUIREMENTS GATHERING

### Functional Requirements

1. **FR1**: Support different room types (SINGLE, DOUBLE, SUITE) with base pricing
2. **FR2**: Search available rooms by type and date range
3. **FR3**: Reserve room for date range (two-step: lock → confirm)
4. **FR4**: Calculate total amount based on nights and room type
5. **FR5**: Cancel bookings and release dates
6. **FR6**: Prevent double-booking with thread-safe operations

### Non-Functional Requirements

1. **NFR1**: **Concurrency** - Support 500+ concurrent booking requests
2. **NFR2**: **Performance** - Booking response time < 200ms
3. **NFR3**: **Consistency** - No double-booking, atomic operations
4. **NFR4**: **Availability** - 99.9% uptime
5. **NFR5**: **Scale** - Support 10,000+ rooms
6. **NFR6**: **Extensibility** - Easy to add new room types and pricing strategies

### Assumptions

1. In-memory storage (production would use database)
2. Single hotel (can extend to multiple)
3. Date-based bookings (check-in to check-out)
4. One room per booking
5. Payment processing is synchronous
6. No reservation system beyond the two-step lock → confirm flow

### Out of Scope

1. Multi-hotel management
2. Dynamic pricing strategies
3. Complex cancellation policies
4. Payment gateway integration
5. Check-in/check-out workflow
6. Guest loyalty programs

---

## 🏗️ STEP 2: DOMAIN MODELING

### Core Entities

#### **Room**
- **Purpose**: Bookable accommodation unit with date-based availability
- **Attributes**: id, type, bookingSchedule
- **Status**: AVAILABLE → RESERVED → AVAILABLE
- **Concurrency**: High contention - needs room-level locking

#### **Guest**
- **Purpose**: Customer making reservations
- **Attributes**: id, name
- **Lifecycle**: Immutable after creation

#### **Booking**
- **Purpose**: Reservation linking guest, room, and dates
- **Attributes**: id, guest, room, checkIn, checkOut, totalAmount, status
- **Status**: PENDING → CONFIRMED / CANCELLED
- **Lifecycle**: Created → Paid → Confirmed → Cancelled

### Entity Relationships

```
Booking (1) ──for──> (1) Guest
Booking (1) ──reserves──> (1) Room
Room (N) ──grouped by──> (1) RoomType
```

---

## 🎨 STEP 3: DESIGN PATTERNS & ARCHITECTURE

### Architecture Layers

```
┌─────────────────────────────────────┐
│   BookingManager (Service Layer)    │ ← Entry point
├─────────────────────────────────────┤
│   RoomManager (Resource Mgmt)       │ ← Room registry + locks
├─────────────────────────────────────┤
│   Repository Layer (In-memory)      │ ← Data storage
├─────────────────────────────────────┤
│   Domain Models (Entities)          │ ← Room, Guest, Booking
└─────────────────────────────────────┘
```

### Design Patterns Used

#### **1. State Pattern** (Booking Lifecycle)
- **Problem**: Booking transitions through states
- **Solution**: BookingStatus enum (PENDING → CONFIRMED / CANCELLED)
- **Benefit**: Clear state transitions, invalid transitions prevented

#### **2. Manager Pattern** (Resource + Service separation)
- **Problem**: Separate room management from booking business logic
- **Solution**: RoomManager (resource registry + locks) + BookingManager (business logic)
- **Benefit**: Single responsibility, testable components

#### **3. Repository Pattern** (Data Access)
- **Problem**: Clean separation of business logic and data storage
- **Solution**: BookingRepository with ConcurrentHashMap
- **Benefit**: Testable, swappable storage backends

---

## 🔐 STEP 4: CONCURRENCY CONTROL (CRITICAL!)

### Concurrency Analysis

#### **Shared Resources**
1. **Room.bookingSchedule** - Multiple threads booking same room
2. **BookingRepository** - Concurrent booking creation
3. **RoomManager.roomsByType** - Concurrent search operations

#### **Critical Sections**
1. **Check availability + Reserve** - Must be atomic
2. **Confirm booking** - Must verify PENDING status atomically
3. **Cancel + Release dates** - Must be atomic

#### **Race Conditions**
1. **Double-booking**: Two threads book same room for overlapping dates
2. **Lost update**: Concurrent status changes overwrite
3. **Phantom read**: Room appears available but gets booked

### Concurrency Strategy: Room-Level Locking ⭐

**Why Room-Level Locking?**
- ✅ Maximum parallelism (different rooms = no contention)
- ✅ Strong consistency (no double-booking)
- ✅ Scalable (contention only on same room)
- ✅ Simple (no complex distributed locking)

**Implementation:**

```java
// 1. Each room has its own fair lock (in RoomManager)
private final ConcurrentHashMap<String, ReentrantLock> roomLocks;

// 2. Atomic search-and-reserve operation
public Booking SearchAndLockRoom(Guest guest, RoomType type,
                                 LocalDate checkIn, LocalDate checkOut) {
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
```

### Thread-Safety Guarantees

| Component | Thread-Safety | Mechanism |
|-----------|---------------|-----------|
| **Room** | Thread-safe | Volatile + External lock |
| **Guest** | Thread-safe | Immutable after creation |
| **Booking** | Thread-safe | Volatile status + immutable fields |
| **RoomManager** | Thread-safe | ConcurrentHashMap + per-room locks |
| **BookingManager** | Thread-safe | Room-level locking |
| **BookingRepository** | Thread-safe | ConcurrentHashMap |

### Concurrency Alternatives Considered

| Approach | Pros | Cons | Decision |
|----------|------|------|----------|
| **Hotel-level lock** | Simple | Very low throughput | ❌ Too coarse |
| **Room-level lock** | High throughput | More memory | ✅ **Chosen** |
| **Optimistic locking** | No blocking | Retry storms | ❌ High contention |
| **Date-level lock** | Fine-grained | Complex deadlock risk | ❌ Over-engineered |

---

## 💻 STEP 5: CLASS DESIGN & IMPLEMENTATION

### Class Structure

```
com.rajan.lld.InterviewQuestionsPractice.HotelBookingSystem
├── HotelBookingSystemComplete.java (All-in-one)
│   ├── Enums (RoomType, BookingStatus)
│   ├── Models (Room, Guest, Booking)
│   ├── Repository (BookingRepository)
│   ├── Manager (RoomManager, BookingManager)
│   └── Demo (Main class with 4 concurrency tests)
```

### Key Classes

#### **Room** (High Concurrency)
```java
/**
 * Thread-Safety: Volatile + external lock
 * Concurrency: Caller MUST hold lock before modifying
 */
class Room {
    private final String id;
    private final RoomType type;
    private final ConcurrentHashMap<LocalDate, String> bookingSchedule;

    // Caller MUST hold lock
    public boolean isAvailable(LocalDate checkIn, LocalDate checkOut) {
        for (LocalDate date = checkIn; date.isBefore(checkOut); date = date.plusDays(1)) {
            if (bookingSchedule.containsKey(date)) return false;
        }
        return true;
    }

    // Caller MUST hold lock
    public void reserve(LocalDate checkIn, LocalDate checkOut, String bookingId) { ... }

    // Caller MUST hold lock
    public void release(LocalDate checkIn, LocalDate checkOut) { ... }
}
```

#### **RoomManager** (Resource Management)
```java
/**
 * Thread-safe: ConcurrentHashMap + per-room fair locks
 * Separates resource registry from business logic
 */
class RoomManager {
    private final ConcurrentHashMap<RoomType, List<Room>> roomsByType;
    private final ConcurrentHashMap<String, ReentrantLock> roomLocks;

    public void addRoom(Room room) { ... }
    public List<Room> getRoomByType(RoomType type) { ... }
    public ReentrantLock getRoomLock(String roomId) { ... }
}
```

#### **BookingManager** (Core Service)
```java
/**
 * Thread-safe using room-level locking
 * Two-step flow: SearchAndLockRoom → confirmBooking
 */
class BookingManager {
    private final BookingRepository bookingRepository;
    private final RoomManager roomManager;

    public BookingManager(RoomManager roomManager) { ... }

    // Step 1: Search + Lock (before payment)
    public Booking SearchAndLockRoom(Guest, RoomType, checkIn, checkOut) { ... }

    // Step 2: Confirm (after payment)
    public Booking confirmBooking(String bookingId) { ... }

    // Cancel booking
    public void cancelBooking(String bookingId) { ... }
}
```

---

## 🧪 STEP 6: TESTING STRATEGY

### Test Distribution
- **70%** Unit tests
- **20%** Concurrency tests
- **10%** Integration tests

### Concurrency Tests

1. **Single Room Concurrent Booking**: 10 threads, same SINGLE room, same dates → Only 1 succeeds
2. **Different Rooms**: 10 threads, 10 different DOUBLE rooms → All 10 succeed
3. **Overlapping Dates**: Thread 1 books Day 50-55, Thread 2 books Day 53-58 → One fails
4. **Cancel and Book**: Cancel existing booking, then another thread books same room → Both succeed

---

## 📊 STEP 7: COMPLEXITY ANALYSIS

### Time Complexity

| Operation | Complexity | Explanation |
|-----------|------------|-------------|
| **Search & Lock** | O(R × D) | R rooms of type, D days to check |
| **Confirm booking** | O(1) | Status update with lock |
| **Cancel booking** | O(D) | D days to release |

### Space Complexity

| Component | Complexity | Explanation |
|-----------|------------|-------------|
| **Rooms** | O(R) | R rooms in system |
| **Bookings** | O(B) | B bookings |
| **Room locks** | O(R) | One lock per room |
| **Booking schedule** | O(R × D) | R rooms, D days booked |

---

## 🚀 STEP 8: SCALABILITY & EXTENSIBILITY

### Extension Points

#### **1. Pricing Strategies**
```java
interface PricingStrategy {
    double calculateFee(RoomType type, long nights);
}

class SeasonalPricing implements PricingStrategy {
    @Override
    public double calculateFee(RoomType type, long nights) {
        double multiplier = isPeakSeason() ? 1.5 : 1.0;
        return type.basePrice * nights * multiplier;
    }
}
```

#### **2. New Room Types**
```java
enum RoomType {
    SINGLE(100.0, 1), DOUBLE(150.0, 2), SUITE(300.0, 4),
    PENTHOUSE(800.0, 6), FAMILY(200.0, 5);
}
```

#### **3. Multi-Hotel Support**
```java
class Hotel {
    private final String id;
    private final RoomManager roomManager;
    // Each hotel manages its own room fleet
}
```

### Scaling Strategies

1. **Room Indexing**: Maintain available room queues per type for O(1) lookup
2. **Horizontal Scaling**: Partition rooms by hotel/location across servers
3. **Caching**: Cache availability counts, invalidate on reserve/release
4. **Async Processing**: Queue fee calculations and notifications

---

## 🔧 STEP 9: TRADE-OFFS & DESIGN DECISIONS

### Decision 1: Room-Level Locking vs Hotel-Level Locking

**Chosen**: Room-level locking

**Justification**: High throughput — different rooms booked in parallel with zero contention

### Decision 2: Two-Step Flow (Lock → Confirm)

**Chosen**: SearchAndLockRoom → confirmBooking

**Justification**: Mirrors real-world payment flow. Room is reserved while guest pays, preventing race conditions between search and payment.

### Decision 3: Blocking with Timeout

**Chosen**: tryLock with 5 seconds

**Pros**: User gets immediate feedback, prevents infinite waiting

### Decision 4: Separate RoomManager from BookingManager

**Chosen**: RoomManager handles resource registry + locks, BookingManager handles business logic

**Justification**: Single responsibility. RoomManager is reusable across different service layers.

---

## 📝 STEP 10: EVALUATION CHECKLIST

### Functional Completeness (30%)
- [x] Search rooms by type and dates
- [x] Two-step booking flow (lock → confirm)
- [x] Calculate total based on nights and room type
- [x] Cancel bookings and release dates
- [x] Prevent double-booking

### Concurrency Control (20%)
- [x] Room-level locking implemented
- [x] No race conditions
- [x] No deadlocks
- [x] Timeout handling (tryLock 5s)
- [x] Thread-safety documented

### Design Patterns (15%)
- [x] State (Booking lifecycle)
- [x] Manager (Resource + Service separation)
- [x] Repository (Data access)

### Code Quality (20%)
- [x] Clean, minimal code
- [x] Proper naming conventions
- [x] Error handling and validation
- [x] "Caller MUST hold lock" documentation

### Testing (15%)
- [x] Single room contention (1/10 succeeds)
- [x] Parallel different rooms (10/10 succeed)
- [x] Overlapping date conflict (1/2 succeeds)
- [x] Cancel + re-book flow (both succeed)

**Total Score**: 100% ✅

---

## 🎓 Key Takeaways

1. **Room-level locking** provides high throughput while maintaining consistency
2. **Two-step flow** (lock → confirm) mirrors real-world payment workflows
3. **RoomManager / BookingManager separation** follows single responsibility
4. **Date-based scheduling** (ConcurrentHashMap<LocalDate, String>) enables overlap detection
5. **Fair locks** (ReentrantLock(true)) prevent thread starvation
6. **Trade-offs** exist between locking granularity, memory, and throughput

This design demonstrates **production-ready concurrency handling** suitable for real-world hotel booking systems! 🏨

---

**Implementation**: See [HotelBookingSystemComplete.java](./HotelBookingSystemComplete.java)
