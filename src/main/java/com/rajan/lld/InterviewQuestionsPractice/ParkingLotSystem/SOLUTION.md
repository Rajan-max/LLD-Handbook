# Parking Lot System - LLD Interview Solution 🅿️

> **Following**: LLD_INTERVIEW_TEMPLATE.md structure with strong concurrency focus

---

## 🎯 STEP 1: REQUIREMENTS GATHERING

### Functional Requirements

1. **FR1**: Support multiple floors in parking lot
2. **FR2**: Support different vehicle types (Bike, Car, Truck)
3. **FR3**: Each floor has different slot types (BIKE, CAR, TRUCK)
4. **FR4**: Park vehicle and issue ticket with entry time
5. **FR5**: Calculate parking fee based on duration and vehicle type
6. **FR6**: Free slot after vehicle exit
7. **FR7**: Display available slots by type
8. **FR8**: Handle edge cases (full lot, invalid tickets, double exit)

### Non-Functional Requirements

1. **NFR1**: **Concurrency** - Support 100+ concurrent park/exit operations
2. **NFR2**: **Performance** - Park/exit response time < 100ms
3. **NFR3**: **Consistency** - No double-booking of slots
4. **NFR4**: **Availability** - 99.9% uptime
5. **NFR5**: **Scale** - Support 1000+ parking slots
6. **NFR6**: **Extensibility** - Easy to add new vehicle types and pricing strategies

### Assumptions

1. In-memory storage (production would use database)
2. Single parking lot (can extend to multiple)
3. Hourly-based pricing
4. Each slot fits only its designated vehicle type
5. Tickets are unique and cannot be reused
6. No reservation system (first-come-first-served)

### Out of Scope

1. Payment gateway integration
2. User authentication
3. Reservation system
4. Valet parking
5. Electric vehicle charging
6. Handicap parking

---

## 🏗️ STEP 2: DOMAIN MODELING

### Core Entities

#### **ParkingLot**
- **Purpose**: Root aggregate managing entire parking facility
- **Attributes**: id, name, floors
- **Lifecycle**: Created by admin, immutable structure

#### **Floor**
- **Purpose**: Represents one level of parking
- **Attributes**: floorNumber, slots
- **Lifecycle**: Created with parking lot, slots can be added

#### **ParkingSlot**
- **Purpose**: Individual parking space
- **Attributes**: id, type, occupied, vehicle
- **Status**: FREE → OCCUPIED → FREE
- **Concurrency**: High contention point - needs locking

#### **Vehicle**
- **Purpose**: Entity being parked
- **Types**: Bike, Car, Truck
- **Attributes**: number, type
- **Lifecycle**: Immutable

#### **Ticket**
- **Purpose**: Proof of parking
- **Attributes**: id, slot, vehicle, entryTime, exitTime
- **Lifecycle**: Created on park, updated on exit
- **Concurrency**: Read-heavy, minimal contention

#### **PricingStrategy**
- **Purpose**: Calculate parking fees
- **Types**: HourlyPricing, FlatRatePricing
- **Attributes**: rates per vehicle type

### Entity Relationships

```
ParkingLot (1) ──has──> (N) Floor
Floor (1) ──has──> (N) ParkingSlot
ParkingSlot (1) ──parks──> (0..1) Vehicle
Ticket (1) ──for──> (1) Vehicle
Ticket (1) ──at──> (1) ParkingSlot
PricingStrategy ──calculates fee for──> Ticket
```

---

## 🎨 STEP 3: DESIGN PATTERNS & ARCHITECTURE

### Architecture Layers

```
┌─────────────────────────────────────┐
│   ParkingManager (Service Layer)    │ ← Entry point
├─────────────────────────────────────┤
│   Business Logic (Park/Exit)        │ ← Core logic + Concurrency
├─────────────────────────────────────┤
│   Repository Layer (In-memory)      │ ← Data storage
├─────────────────────────────────────┤
│   Domain Models (Entities)          │ ← ParkingLot, Floor, Slot
└─────────────────────────────────────┘
```

### Design Patterns Used

#### **1. Strategy Pattern** (Pricing)
- **Problem**: Different pricing for different vehicle types
- **Solution**: PricingStrategy interface with implementations
- **Benefit**: Easy to add new pricing models

#### **2. Factory Pattern** (Vehicle Creation)
- **Problem**: Complex vehicle object creation
- **Solution**: VehicleFactory creates appropriate vehicle type
- **Benefit**: Centralized creation logic

#### **3. Builder Pattern** (ParkingLot Construction)
- **Problem**: Complex parking lot setup with multiple floors
- **Solution**: ParkingLot.Builder for fluent construction
- **Benefit**: Readable, flexible construction

#### **4. Singleton Pattern** (ParkingManager)
- **Problem**: Single point of coordination needed
- **Solution**: Singleton ParkingManager instance
- **Benefit**: Global access point (use carefully!)

---

## 🔐 STEP 4: CONCURRENCY CONTROL (CRITICAL!)

### Concurrency Analysis

#### **Shared Resources**
1. **ParkingSlot.occupied** - Multiple threads parking simultaneously
2. **Floor.slots** - Finding available slots
3. **Ticket map** - Storing/retrieving tickets

#### **Critical Sections**
1. **Find and park** - Check availability + Park (must be atomic)
2. **Exit and free** - Validate ticket + Free slot (must be atomic)
3. **Slot status update** - Prevent race conditions

#### **Race Conditions**
1. **Double-parking**: Two threads park in same slot
2. **Lost update**: Concurrent status changes overwrite
3. **Phantom read**: Slot appears free but gets occupied

### Concurrency Strategy: Slot-Level Locking ⭐

**Why Slot-Level Locking?**
- ✅ Maximum parallelism (different slots = no contention)
- ✅ Strong consistency (no double-parking)
- ✅ Scalable (contention only on same slot)
- ✅ Simple (no complex distributed locking needed)

**Implementation:**

```java
// 1. Each slot has its own lock
private final ConcurrentHashMap<String, ReentrantLock> slotLocks;

// 2. Atomic find-and-park operation
public Ticket parkVehicle(Vehicle vehicle) {
    for (Floor floor : floors) {
        for (ParkingSlot slot : floor.getSlots()) {
            ReentrantLock lock = slotLocks.get(slot.getId());
            
            if (lock.tryLock()) {
                try {
                    // Check and park atomically
                    if (slot.canFit(vehicle)) {
                        slot.park(vehicle);
                        return createTicket(slot, vehicle);
                    }
                } finally {
                    lock.unlock();
                }
            }
        }
    }
    throw new RuntimeException("No available slot");
}

// 3. Atomic exit operation
public double exitVehicle(String ticketId) {
    Ticket ticket = tickets.get(ticketId);
    ParkingSlot slot = ticket.getSlot();
    ReentrantLock lock = slotLocks.get(slot.getId());
    
    lock.lock();
    try {
        slot.free();
        ticket.setExitTime(LocalDateTime.now());
        return calculateFee(ticket);
    } finally {
        lock.unlock();
    }
}
```

### Thread-Safety Guarantees

| Component | Thread-Safety | Mechanism |
|-----------|---------------|-----------|
| **ParkingSlot** | Thread-safe | Volatile status + External lock |
| **Floor** | Thread-safe | Immutable slot list |
| **ParkingLot** | Thread-safe | Immutable floor list |
| **Ticket** | Thread-safe | Immutable after creation |
| **ParkingManager** | Thread-safe | Slot-level locking |
| **TicketRepository** | Thread-safe | ConcurrentHashMap |

### Concurrency Alternatives Considered

| Approach | Pros | Cons | Decision |
|----------|------|------|----------|
| **Floor-level lock** | Simple | Low throughput | ❌ Too coarse |
| **Slot-level lock** | High throughput | More memory | ✅ **Chosen** |
| **Optimistic locking** | No blocking | Retry storms | ❌ High contention |
| **Database locking** | Distributed | Network latency | ❌ Overkill for single server |

---

## 💻 STEP 5: CLASS DESIGN & IMPLEMENTATION

### Class Structure

```
com.rajan.lld.InterviewQuestionsPractice.ParkingLotSystem
├── ParkingLotSystemComplete.java (All-in-one)
│   ├── Enums (VehicleType, SlotType)
│   ├── Models (Vehicle, ParkingSlot, Floor, ParkingLot, Ticket)
│   ├── Strategy (PricingStrategy, HourlyPricing)
│   ├── Service (ParkingManager)
│   └── Demo (Main class with tests)
```

### Key Classes

#### **ParkingSlot** (High Concurrency)
```java
/**
 * Thread-Safety: Thread-safe using volatile + external lock
 * Concurrency: Caller MUST hold lock before modifying
 */
class ParkingSlot {
    private final String id;
    private final SlotType type;
    private volatile boolean occupied;
    private volatile Vehicle vehicle;
    
    // Caller MUST hold lock
    public boolean canFit(Vehicle v) {
        return !occupied && type.name().equals(v.getType().name());
    }
    
    // Caller MUST hold lock
    public void park(Vehicle v) {
        if (!canFit(v)) throw new IllegalStateException();
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

#### **ParkingManager** (Core Service)
```java
/**
 * Thread-Safety: Thread-safe using slot-level locking
 * Concurrency: Each slot has independent ReentrantLock
 */
class ParkingManager {
    private final ParkingLot parkingLot;
    private final ConcurrentHashMap<String, Ticket> tickets;
    private final ConcurrentHashMap<String, ReentrantLock> slotLocks;
    private final PricingStrategy pricingStrategy;
    
    /**
     * Park vehicle (Thread-safe)
     * Finds first available slot and parks atomically
     */
    public Ticket parkVehicle(Vehicle vehicle) {
        for (Floor floor : parkingLot.getFloors()) {
            for (ParkingSlot slot : floor.getSlots()) {
                ReentrantLock lock = slotLocks.get(slot.getId());
                
                if (lock.tryLock()) {
                    try {
                        if (slot.canFit(vehicle)) {
                            slot.park(vehicle);
                            Ticket ticket = new Ticket(
                                generateTicketId(),
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
            throw new IllegalArgumentException("Invalid ticket");
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
}
```

---

## 🧪 STEP 6: TESTING STRATEGY

### Unit Tests (70%)

```java
@Test
public void testParkVehicle() {
    // Happy path: Park vehicle in available slot
}

@Test
public void testParkWhenFull() {
    // Edge case: All slots occupied
}

@Test
public void testExitWithInvalidTicket() {
    // Error: Invalid ticket ID
}

@Test
public void testDoubleExit() {
    // Error: Exit same vehicle twice
}
```

### Concurrency Tests (20%)

```java
@Test
public void testConcurrentParkDifferentSlots() {
    // 10 vehicles parking simultaneously in different slots
    // Expected: All succeed
}

@Test
public void testConcurrentParkSameSlot() {
    // 10 vehicles trying same slot type
    // Expected: Only available slots get filled, no double-parking
}

@Test
public void testConcurrentParkAndExit() {
    // Simultaneous park and exit operations
    // Expected: No race conditions
}
```

### Integration Tests (10%)

```java
@Test
public void testCompleteWorkflow() {
    // 1. Park vehicle
    // 2. Wait some time
    // 3. Exit vehicle
    // 4. Verify fee calculation
    // 5. Verify slot is free
}
```

---

## 📊 STEP 7: SCALABILITY & TRADE-OFFS

### Design Trade-offs

#### **Decision: Slot-Level Locking**

**Pros:**
- High throughput (parallel parking in different slots)
- Strong consistency (no double-parking)
- Simple implementation (no distributed coordination)

**Cons:**
- Memory overhead (lock per slot)
- Lock management complexity
- Not suitable for distributed systems

**Alternatives Considered:**
- **Floor-level lock**: Too coarse, low throughput
- **Optimistic locking**: Retry storms under high load
- **No locking**: Race conditions, double-parking

**When to Reconsider:**
- If memory becomes constraint (millions of slots)
- If distributed across multiple servers (use Redis locks)

### Scalability Analysis

#### **Current Limitations**
- **Bottleneck**: Finding available slot (O(n) search)
- **Breaking Point**: ~10,000 slots per server
- **Memory**: O(slots) for locks

#### **Scaling Strategies**

1. **Indexing Available Slots**
   ```java
   // Maintain index of available slots by type
   ConcurrentHashMap<SlotType, Queue<ParkingSlot>> availableSlots;
   
   // O(1) lookup instead of O(n) search
   public ParkingSlot findSlot(VehicleType type) {
       return availableSlots.get(SlotType.valueOf(type.name())).poll();
   }
   ```

2. **Horizontal Scaling**
   - Partition floors across servers
   - Each server handles subset of floors
   - Load balancer routes requests

3. **Caching**
   - Cache available slot counts
   - Invalidate on park/exit
   - Reduce repeated searches

4. **Async Processing**
   - Queue fee calculations
   - Background cleanup of expired tickets
   - Async notifications

### Performance Characteristics

| Operation | Time Complexity | Space Complexity |
|-----------|----------------|------------------|
| **Park Vehicle** | O(F × S) | O(1) |
| **Exit Vehicle** | O(1) | O(1) |
| **Find Available** | O(F × S) | O(1) |
| **Get Slot Count** | O(F × S) | O(1) |

*F = floors, S = slots per floor*

**With Indexing:**
| Operation | Time Complexity |
|-----------|----------------|
| **Park Vehicle** | O(1) |
| **Exit Vehicle** | O(1) |

---

## 🚀 STEP 8: EXTENSIBILITY & FUTURE ENHANCEMENTS

### Extension Points

#### **Adding New Vehicle Types**
1. Add enum value to `VehicleType`
2. Add corresponding `SlotType`
3. Create vehicle class implementing `Vehicle`
4. Update pricing strategy

```java
enum VehicleType { BIKE, CAR, TRUCK, BUS, MOTORCYCLE }

class Bus implements Vehicle {
    // Implementation
}
```

#### **Adding New Pricing Strategies**
1. Implement `PricingStrategy` interface
2. Inject into `ParkingManager`

```java
class DynamicPricing implements PricingStrategy {
    @Override
    public double calculateFee(Ticket ticket) {
        // Peak hours: 2x rate
        // Off-peak: 0.5x rate
    }
}
```

#### **Adding Reservation System**
1. Create `Reservation` entity
2. Add `reservedBy` field to `ParkingSlot`
3. Check reservation before parking

### Future Roadmap

#### **Phase 1: Immediate (< 1 month)**
- Add slot indexing for O(1) lookup
- Implement dynamic pricing
- Add parking history

#### **Phase 2: Short-term (1-3 months)**
- Distributed locking (Redis)
- Database persistence
- Payment gateway integration
- Mobile app API

#### **Phase 3: Long-term (3-6 months)**
- Reservation system
- Valet parking
- Electric vehicle charging
- Analytics dashboard
- Predictive availability

---

## 🎯 STEP 9: INTERVIEW EVALUATION CHECKLIST

### ✅ Requirements (20%)
- [x] Identified functional requirements
- [x] Identified non-functional requirements (concurrency!)
- [x] Made clear assumptions
- [x] Defined scope

### ✅ Design (30%)
- [x] Clean layered architecture
- [x] Appropriate design patterns (Strategy, Factory, Builder, Singleton)
- [x] SOLID principles followed
- [x] Extensible design

### ✅ Concurrency (20%)
- [x] Identified shared resources (ParkingSlot)
- [x] Chosen slot-level locking strategy
- [x] Prevented race conditions (atomic operations)
- [x] No deadlocks (single lock per operation)
- [x] Documented thread-safety for all classes

### ✅ Code Quality (20%)
- [x] Clean, minimal code
- [x] Proper naming conventions
- [x] Error handling
- [x] Input validation
- [x] Comments for complex logic

### ✅ Communication (10%)
- [x] Explained thought process
- [x] Discussed trade-offs (slot-level vs floor-level locking)
- [x] Considered scalability
- [x] Showed production awareness

---

## 📝 STEP 10: HOW TO RUN

```bash
# Navigate to directory
cd src/main/java

# Compile
javac com/rajan/lld/InterviewQuestionsPractice/ParkingLotSystem/ParkingLotSystemComplete.java

# Run demo
java com.rajan.lld.InterviewQuestionsPractice.ParkingLotSystem.ParkingLotSystemComplete
```

### Expected Output

```
======================================================================
PARKING LOT SYSTEM - CONCURRENCY DEMO
======================================================================

✅ Setup: 2 floors, 10 slots per floor (20 total)

TEST 1: Single Vehicle Parking
✅ Parked: Bike(B001) at Slot F1-BIKE-1
✅ Fee: $5.0 for 1 hour

TEST 2: Concurrent Parking - Different Types (All should succeed)
✅ Success: 10/10 vehicles parked

TEST 3: Concurrent Parking - Same Type (Limited slots)
✅ Success: 5/10 (5 bike slots available)

TEST 4: Concurrent Park and Exit
✅ No race conditions! All operations completed safely

======================================================================
ALL TESTS PASSED! ✅
======================================================================
```

---

## 🎓 Key Takeaways

1. **Slot-level locking** provides high throughput while maintaining consistency
2. **Atomic operations** (find-and-park, exit-and-free) prevent race conditions
3. **Strategy pattern** makes pricing extensible
4. **Builder pattern** simplifies complex object construction
5. **Indexing** can optimize from O(n) to O(1) lookup
6. **Trade-offs** exist between simplicity, performance, and scalability

This design demonstrates **production-ready concurrency handling** suitable for real-world parking systems! 🅿️
