# Vehicle Rental System - LLD Interview Solution 🚗

> **Following**: LLD_INTERVIEW_TEMPLATE.md structure with strong concurrency focus

---

## 🎯 STEP 1: REQUIREMENTS GATHERING

### Functional Requirements

1. **FR1**: Support multiple vehicle types (BIKE, CAR, SUV, TRUCK)
2. **FR2**: Search available vehicles by type and date range
3. **FR3**: Reserve vehicle for date range (two-step: lock → confirm)
4. **FR4**: Calculate rental fee based on duration and vehicle type
5. **FR5**: Return vehicle and release dates
6. **FR6**: Cancel rental and release dates
7. **FR7**: Prevent double-rental with thread-safe operations
8. **FR8**: Support different pricing strategies

### Non-Functional Requirements

1. **NFR1**: **Concurrency** - Support 500+ concurrent rental requests
2. **NFR2**: **Performance** - Rental response time < 200ms
3. **NFR3**: **Consistency** - No double-rental, atomic operations
4. **NFR4**: **Availability** - 99.9% uptime
5. **NFR5**: **Scale** - Support 10,000+ vehicles
6. **NFR6**: **Extensibility** - Easy to add new vehicle types and pricing strategies

### Assumptions

1. In-memory storage (production would use database)
2. Single rental location (can extend to multiple)
3. Date-based rentals (pick-up to drop-off)
4. One vehicle per rental
5. Payment processing is synchronous
6. No reservation system beyond the two-step lock → confirm flow

### Out of Scope

1. Multi-location management and transfers
2. Insurance and damage tracking
3. GPS tracking and fleet management
4. Customer loyalty programs
5. Late return penalties
6. Vehicle maintenance scheduling

---

## 🏗️ STEP 2: DOMAIN MODELING

### Core Entities

#### **Vehicle**
- **Purpose**: Rentable asset with date-based availability
- **Attributes**: id, model, type, rentalSchedule
- **Status**: AVAILABLE → RESERVED → AVAILABLE
- **Concurrency**: High contention - needs vehicle-level locking

#### **Customer**
- **Purpose**: Person renting a vehicle
- **Attributes**: id, name, licenseNumber
- **Lifecycle**: Immutable after creation

#### **Rental**
- **Purpose**: Reservation linking customer, vehicle, and dates
- **Attributes**: id, customer, vehicle, pickUp, dropOff, totalAmount, status
- **Status**: PENDING → CONFIRMED → RETURNED / CANCELLED
- **Lifecycle**: Created → Paid → Confirmed → Returned/Cancelled

### Entity Relationships

```
Rental (1) ──for──> (1) Customer
Rental (1) ──reserves──> (1) Vehicle
Vehicle (N) ──grouped by──> (1) VehicleType
Rental (1) ──priced by──> (1) PricingStrategy
```

---

## 🎨 STEP 3: DESIGN PATTERNS & ARCHITECTURE

### Architecture Layers

```
┌─────────────────────────────────────┐
│   RentalManager (Service Layer)     │ ← Entry point
├─────────────────────────────────────┤
│   VehicleManager (Resource Mgmt)    │ ← Vehicle registry + locks
├─────────────────────────────────────┤
│   PricingStrategy (Strategy)        │ ← Daily, Weekly discount
├─────────────────────────────────────┤
│   Repository Layer (In-memory)      │ ← Data storage
├─────────────────────────────────────┤
│   Domain Models (Entities)          │ ← Vehicle, Customer, Rental
└─────────────────────────────────────┘
```

### Design Patterns Used

#### **1. Strategy Pattern** (Pricing)
- **Problem**: Different pricing for different rental durations
- **Solution**: PricingStrategy interface with DailyPricing, WeeklyDiscountPricing
- **Benefit**: Easy to add seasonal, loyalty, or surge pricing

#### **2. State Pattern** (Rental Lifecycle)
- **Problem**: Rental transitions through states
- **Solution**: RentalStatus enum (PENDING → CONFIRMED → RETURNED / CANCELLED)
- **Benefit**: Clear state transitions, invalid transitions prevented

#### **3. Manager Pattern** (Resource + Service separation)
- **Problem**: Separate vehicle management from rental business logic
- **Solution**: VehicleManager (resource registry + locks) + RentalManager (business logic)
- **Benefit**: Single responsibility, testable components

---

## 🔐 STEP 4: CONCURRENCY CONTROL (CRITICAL!)

### Concurrency Analysis

#### **Shared Resources**
1. **Vehicle.rentalSchedule** - Multiple threads renting same vehicle
2. **RentalRepository** - Concurrent rental creation
3. **VehicleManager.vehiclesByType** - Concurrent search operations

#### **Critical Sections**
1. **Check availability + Reserve** - Must be atomic
2. **Confirm rental** - Must verify PENDING status atomically
3. **Return/Cancel + Release dates** - Must be atomic

#### **Race Conditions**
1. **Double-rental**: Two threads rent same vehicle for overlapping dates
2. **Lost update**: Concurrent status changes overwrite
3. **Phantom read**: Vehicle appears available but gets rented

### Concurrency Strategy: Vehicle-Level Locking ⭐

**Why Vehicle-Level Locking?**
- ✅ Maximum parallelism (different vehicles = no contention)
- ✅ Strong consistency (no double-rental)
- ✅ Scalable (contention only on same vehicle)
- ✅ Simple (no complex distributed locking)

**Implementation:**

```java
// 1. Each vehicle has its own lock (fair lock)
private final ConcurrentHashMap<String, ReentrantLock> vehicleLocks;

// 2. Atomic search-and-reserve operation
public Rental searchAndLockVehicle(Customer customer, VehicleType type,
                                   LocalDate pickUp, LocalDate dropOff) {
    List<Vehicle> vehicles = vehicleManager.getVehiclesByType(type);
    for (Vehicle vehicle : vehicles) {
        ReentrantLock lock = vehicleManager.getVehicleLock(vehicle.getId());
        try {
            if (lock.tryLock(5, TimeUnit.SECONDS)) {
                try {
                    if (vehicle.isAvailable(pickUp, dropOff)) {
                        // Reserve dates atomically
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
    throw new IllegalStateException("No available vehicles");
}
```

### Thread-Safety Guarantees

| Component | Thread-Safety | Mechanism |
|-----------|---------------|-----------|
| **Vehicle** | Thread-safe | Volatile + External lock |
| **Customer** | Thread-safe | Immutable after creation |
| **Rental** | Thread-safe | Volatile status + immutable fields |
| **VehicleManager** | Thread-safe | ConcurrentHashMap + per-vehicle locks |
| **RentalManager** | Thread-safe | Vehicle-level locking |
| **RentalRepository** | Thread-safe | ConcurrentHashMap |

### Concurrency Alternatives Considered

| Approach | Pros | Cons | Decision |
|----------|------|------|----------|
| **Fleet-level lock** | Simple | Very low throughput | ❌ Too coarse |
| **Vehicle-level lock** | High throughput | More memory | ✅ **Chosen** |
| **Optimistic locking** | No blocking | Retry storms | ❌ High contention |
| **Date-level lock** | Fine-grained | Complex deadlock risk | ❌ Over-engineered |

---

## 💻 STEP 5: CLASS DESIGN & IMPLEMENTATION

### Class Structure

```
com.rajan.lld.InterviewQuestionsPractice.VehicleRentalSystem
├── VehicleRentalSystemComplete.java (All-in-one)
│   ├── Enums (VehicleType, RentalStatus)
│   ├── Models (Vehicle, Customer, Rental)
│   ├── Repository (RentalRepository)
│   ├── Strategy (PricingStrategy, DailyPricing, WeeklyDiscountPricing)
│   ├── Manager (VehicleManager, RentalManager)
│   └── Demo (Main class with 4 concurrency tests)
```

### Key Classes

#### **Vehicle** (High Concurrency)
```java
/**
 * Thread-Safety: Volatile + external lock
 * Concurrency: Caller MUST hold lock before modifying
 */
class Vehicle {
    private final String id;
    private final String model;
    private final VehicleType type;
    private final ConcurrentHashMap<LocalDate, String> rentalSchedule;

    // Caller MUST hold lock
    public boolean isAvailable(LocalDate pickUp, LocalDate dropOff) {
        for (LocalDate date = pickUp; date.isBefore(dropOff); date = date.plusDays(1)) {
            if (rentalSchedule.containsKey(date)) return false;
        }
        return true;
    }

    // Caller MUST hold lock
    public void reserve(LocalDate pickUp, LocalDate dropOff, String rentalId) { ... }

    // Caller MUST hold lock
    public void release(LocalDate pickUp, LocalDate dropOff) { ... }
}
```

#### **RentalManager** (Core Service)
```java
/**
 * Thread-safe using vehicle-level locking
 * Two-step flow: searchAndLockVehicle → confirmRental
 */
class RentalManager {
    private final VehicleManager vehicleManager;
    private final RentalRepository rentalRepository;
    private final PricingStrategy pricingStrategy;

    // Step 1: Search + Lock (before payment)
    public Rental searchAndLockVehicle(Customer, VehicleType, pickUp, dropOff) { ... }

    // Step 2: Confirm (after payment)
    public Rental confirmRental(String rentalId) { ... }

    // Return vehicle
    public Rental returnVehicle(String rentalId) { ... }

    // Cancel rental
    public void cancelRental(String rentalId) { ... }
}
```

---

## 🧪 STEP 6: TESTING STRATEGY

### Test Distribution
- **70%** Unit tests
- **20%** Concurrency tests
- **10%** Integration tests

### Concurrency Tests

1. **Single Vehicle Concurrent Rental**: 10 threads, same CAR, same dates → Only 1 succeeds
2. **Different Vehicles**: 10 threads, 10 different CARs → All 10 succeed
3. **Overlapping Dates**: Thread 1 rents Day 50-55, Thread 2 rents Day 53-58 → One fails
4. **Return and Rent**: Return vehicle, then another thread rents same vehicle → Both succeed

---

## 📊 STEP 7: COMPLEXITY ANALYSIS

### Time Complexity

| Operation | Complexity | Explanation |
|-----------|------------|-------------|
| **Search & Lock** | O(V × D) | V vehicles of type, D days to check |
| **Confirm rental** | O(1) | Status update with lock |
| **Return vehicle** | O(D) | D days to release |
| **Cancel rental** | O(D) | D days to release |

### Space Complexity

| Component | Complexity | Explanation |
|-----------|------------|-------------|
| **Vehicles** | O(V) | V vehicles in system |
| **Rentals** | O(R) | R rentals |
| **Vehicle locks** | O(V) | One lock per vehicle |
| **Rental schedule** | O(V × D) | V vehicles, D days booked |

---

## 🚀 STEP 8: SCALABILITY & EXTENSIBILITY

### Extension Points

#### **1. New Pricing Strategies**
```java
class SurgePricing implements PricingStrategy {
    @Override
    public double calculateFee(VehicleType type, long days) {
        double multiplier = isHolidaySeason() ? 1.5 : 1.0;
        return type.baseDailyRate * days * multiplier;
    }
}
```

#### **2. New Vehicle Types**
```java
enum VehicleType {
    BIKE(50.0), CAR(100.0), SUV(150.0), TRUCK(200.0),
    LUXURY(500.0), ELECTRIC(120.0);
}
```

#### **3. Multi-Location Support**
```java
class Location {
    private final String id;
    private final VehicleManager vehicleManager;
    // Each location manages its own fleet
}
```

### Scaling Strategies

1. **Vehicle Indexing**: Maintain available vehicle queues per type for O(1) lookup
2. **Horizontal Scaling**: Partition vehicles by location across servers
3. **Caching**: Cache availability counts, invalidate on reserve/release
4. **Async Processing**: Queue fee calculations and notifications

---

## 🔧 STEP 9: TRADE-OFFS & DESIGN DECISIONS

### Decision 1: Vehicle-Level Locking vs Fleet-Level Locking

**Chosen**: Vehicle-level locking

**Justification**: High throughput — different vehicles rented in parallel with zero contention

### Decision 2: Two-Step Flow (Lock → Confirm)

**Chosen**: searchAndLockVehicle → confirmRental

**Justification**: Mirrors real-world payment flow. Vehicle is reserved while customer pays, preventing race conditions between search and payment.

### Decision 3: Blocking with Timeout

**Chosen**: tryLock with 5 seconds

**Pros**: User gets immediate feedback, prevents infinite waiting

### Decision 4: Separate VehicleManager from RentalManager

**Chosen**: VehicleManager handles resource registry + locks, RentalManager handles business logic

**Justification**: Single responsibility. VehicleManager is reusable across different service layers.

---

## 📝 STEP 10: EVALUATION CHECKLIST

### Functional Completeness (30%)
- [x] Search vehicles by type and dates
- [x] Two-step rental flow (lock → confirm)
- [x] Multiple pricing strategies (Daily, Weekly discount)
- [x] Return vehicle and release dates
- [x] Cancel rental and release dates
- [x] Prevent double-rental

### Concurrency Control (20%)
- [x] Vehicle-level locking implemented
- [x] No race conditions
- [x] No deadlocks
- [x] Timeout handling (tryLock 5s)
- [x] Thread-safety documented

### Design Patterns (15%)
- [x] Strategy (Pricing)
- [x] State (Rental lifecycle)
- [x] Manager (Resource + Service separation)
- [x] Repository (Data access)

### Code Quality (20%)
- [x] Clean, minimal code
- [x] Proper naming conventions
- [x] Error handling and validation
- [x] "Caller MUST hold lock" documentation

### Testing (15%)
- [x] Single vehicle contention (1/10 succeeds)
- [x] Parallel different vehicles (10/10 succeed)
- [x] Overlapping date conflict (1/2 succeeds)
- [x] Return + re-rent flow (both succeed)

**Total Score**: 100% ✅

---

## 🎓 Key Takeaways

1. **Vehicle-level locking** provides high throughput while maintaining consistency
2. **Two-step flow** (lock → confirm) mirrors real-world payment workflows
3. **Strategy pattern** makes pricing extensible without modifying core logic
4. **VehicleManager / RentalManager separation** follows single responsibility
5. **Date-based scheduling** (ConcurrentHashMap<LocalDate, String>) enables overlap detection
6. **Trade-offs** exist between locking granularity, memory, and throughput

This design demonstrates **production-ready concurrency handling** suitable for real-world vehicle rental systems! 🚗

---

**Implementation**: See [VehicleRentalSystemComplete.java](./VehicleRentalSystemComplete.java)
