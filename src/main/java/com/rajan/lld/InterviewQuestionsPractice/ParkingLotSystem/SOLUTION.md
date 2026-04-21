# Parking Lot System - LLD Interview Solution 🅿️
---

## 1) Requirements (~5 min)

**Prompt**: "Design a parking lot system where vehicles are assigned to spots as they pull in."

### Clarifying Questions

| Theme | Question | Answer |
|---|---|---|
| **Primary capabilities** | What operations? | Park vehicle, exit vehicle, calculate fee |
| **Primary capabilities** | Multiple floors? | Yes, each floor has slots of different types |
| **Rules** | How are slots assigned? | First available matching slot (first-come-first-served) |
| **Rules** | How is fee calculated? | Hourly pricing, different rates per vehicle type |
| **Error handling** | What if lot is full? | Reject with error |
| **Error handling** | Invalid ticket on exit? | Reject with error |
| **Scope** | Concurrent access? | Yes, multiple vehicles parking/exiting simultaneously |

### Requirements

```
1. Multiple floors, each with slots of different types (BIKE, CAR, TRUCK)
2. Park vehicle in first available matching slot, issue ticket with entry time
3. Calculate fee based on duration and vehicle type (hourly pricing)
4. Free slot on exit
5. Display available slots by type
6. No double-parking under concurrent access

Out of Scope:
- Reservation system
- Valet parking / EV charging
- Payment gateway integration
- User authentication
- Handicap parking
```

---

## 2) Entities & Relationships (~3 min)

```
Entities:
- ParkingSlot    (individual space — owns occupied/vehicle state)
- Floor          (contains slots + per-slot locks)
- ParkingLot     (contains floors)
- Vehicle        (Bike, Car, Truck — immutable, what's being parked)
- Ticket         (links floor + slot + vehicle + timestamps)
- ParkingManager (orchestrator — park, exit, fee calculation)

Relationships:
- ParkingLot → Floor (1:N)
- Floor → ParkingSlot (1:N) + per-slot ReentrantLocks
- Ticket → ParkingSlot, Vehicle, floorNumber
- ParkingManager → ParkingLot, TicketRepository, PricingStrategy
```

**Key decisions:**
- Locks live inside Floor (not ParkingManager) — Floor owns its slots, so it owns their locks
- Vehicle is an interface with Bike/Car/Truck implementations — type matching via enum
- PricingStrategy is injected into ParkingManager — swappable at construction time

---

## 3) Class Design (~10 min)

### Deriving State from Requirements

| Requirement | What must be tracked | Where |
|---|---|---|
| "Slots of different types" | id, type, occupied, vehicle | ParkingSlot |
| "Multiple floors" | floorNumber, slots, per-slot locks | Floor |
| "Issue ticket with entry time" | id, floorNumber, slot, vehicle, entryTime, exitTime | Ticket |
| "Calculate fee by duration and type" | rates per vehicle type | PricingStrategy |
| "No double-parking" | per-slot ReentrantLock | Floor |

### Deriving Behavior from Requirements

| Need | Method | On |
|---|---|---|
| Park vehicle in first available slot | parkVehicle(vehicle) → Ticket | ParkingManager |
| Exit and calculate fee | exitVehicle(ticketId) → double | ParkingManager |
| Check available slots | getAvailableSlots(type) → long | ParkingManager |
| Check if slot fits vehicle | canFit(vehicle) → bool | ParkingSlot |
| Occupy / free a slot | park(vehicle), free() | ParkingSlot |

### Class Outlines

```
interface Vehicle:
  + getNumber() → String
  + getType() → VehicleType

class ParkingSlot:                          // Caller MUST hold lock
  - id: String
  - type: SlotType
  - occupied: volatile boolean
  - vehicle: volatile Vehicle

  + canFit(vehicle) → bool
  + park(vehicle)
  + free()

class Floor:
  - number: int
  - slots: List<ParkingSlot>
  - slotLocks: Map<String, ReentrantLock>   // Fair locks, one per slot

  + getSlots() → List<ParkingSlot>
  + getLockForSlot(slotId) → ReentrantLock

class ParkingLot:
  - id: String
  - floors: List<Floor>

  + getFloors() → List<Floor>
  + getFloor(floorNumber) → Optional<Floor>

class Ticket:
  - id: String (auto-generated)
  - floorNumber: int
  - slot: ParkingSlot
  - vehicle: Vehicle
  - entryTime: LocalDateTime
  - exitTime: volatile LocalDateTime

  + setExitTime(time)
  + getDurationHours() → long              // Rounds up to nearest hour

class ParkingManager:                       // Orchestrator
  - parkingLot: ParkingLot
  - ticketRepository: TicketRepository
  - pricingStrategy: PricingStrategy

  + parkVehicle(vehicle) → Ticket
  + exitVehicle(ticketId) → double
  + getAvailableSlots(type) → long

interface PricingStrategy:
  + calculateFee(ticket) → double
```

---

## 4) Concurrency Control (~5 min)

### Three Questions

**What is shared?**
- ParkingSlot.occupied / ParkingSlot.vehicle — multiple threads parking simultaneously

**What can go wrong?**
- Double-parking: Two threads park in the same slot
- Lost update: Concurrent status changes overwrite each other

**What's the locking strategy?**
- Slot-level locking. Each slot has its own ReentrantLock inside Floor.

### Why Slot-Level Locking?

| Approach | Throughput | Decision |
|---|---|---|
| **Lot-level lock** | Very low (serializes everything) | ❌ Too coarse |
| **Floor-level lock** | Low (serializes per floor) | ❌ Still too coarse |
| **Slot-level lock** | High (parallel across all slots) | ✅ Chosen |

### Concurrency Strategy

```
Shared resource:
- ParkingSlot.occupied — multiple threads trying to park in same slot

Race condition prevented:
- Double-parking: tryLock + canFit + park is atomic under lock

Locking approach:
- Each slot has its own ReentrantLock(true) — fair lock, inside Floor
- parkVehicle uses tryLock() (non-blocking) — skip to next slot if locked
- exitVehicle uses lock() (blocking) — must free the specific slot

Thread-safety:
- ParkingSlot: volatile fields + external lock (caller MUST hold lock)
- Floor: immutable slot list, ConcurrentHashMap for locks
- ParkingLot: immutable floor list
- Ticket: immutable after creation, exitTime updated once via volatile
- TicketRepository: ConcurrentHashMap
```

**Why tryLock() for parking (non-blocking)?**
A vehicle doesn't care *which* slot it gets — if one is locked, skip to the next. No need to wait.

**Why lock() for exit (blocking)?**
A vehicle *must* free its specific slot. We have to wait for the lock.

---

## 5) Implementation (~10 min)

### Core Method: parkVehicle

```java
public Ticket parkVehicle(Vehicle vehicle) {
    for (Floor floor : parkingLot.getFloors()) {
        for (ParkingSlot slot : floor.getSlots()) {
            if (slot.getType() != SlotType.valueOf(vehicle.getType().name()))
                continue; // Skip incompatible slot types

            ReentrantLock lock = floor.getLockForSlot(slot.getId());

            if (lock.tryLock()) {                    // Non-blocking
                try {
                    if (slot.canFit(vehicle)) {      // Atomic check + park
                        slot.park(vehicle);
                        Ticket ticket = new Ticket(floor.getNumber(), slot, vehicle);
                        ticketRepository.save(ticket);
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
```

**What this demonstrates:**
- Skips incompatible slot types early (no wasted lock attempts)
- tryLock() — non-blocking, moves to next slot if this one is contended
- Atomic canFit + park under lock — no gap for another thread to sneak in
- Lock released in finally — even if park() throws

### Core Method: exitVehicle

```java
public double exitVehicle(String ticketId) {
    Ticket ticket = ticketRepository.findById(ticketId);
    if (ticket == null)
        throw new IllegalArgumentException("Invalid ticket: " + ticketId);
    if (ticket.getExitTime() != null)
        throw new IllegalStateException("Vehicle already exited");

    ParkingSlot slot = ticket.getSlot();
    Floor floor = parkingLot.getFloor(ticket.getFloorNumber())
                            .orElseThrow(() -> new IllegalStateException("Invalid floor"));
    ReentrantLock lock = floor.getLockForSlot(slot.getId());

    lock.lock();                                     // Blocking — must free this specific slot
    try {
        slot.free();
        ticket.setExitTime(LocalDateTime.now());
        return pricingStrategy.calculateFee(ticket);
    } finally {
        lock.unlock();
    }
}
```

**Edge cases handled:**
- Invalid ticket ID → IllegalArgumentException
- Double exit (already exited) → IllegalStateException
- Fee calculation delegated to PricingStrategy (Strategy pattern)

### Verification: Walk Through a Scenario

```
Scenario: Two threads try to park a CAR simultaneously, only 1 CAR slot left

Thread A: parkVehicle(Car("C1"))
  → Iterates to F1-CAR-1
  → tryLock() succeeds
  → canFit(Car) = true
  → slot.park(Car("C1")), issues Ticket TK-1
  → Releases lock

Thread B: parkVehicle(Car("C2"))
  → Iterates to F1-CAR-1
  → tryLock() fails (Thread A holds it) OR succeeds but canFit = false (already occupied)
  → Moves to next slot... no more CAR slots
  → Throws "No available slot for CAR"

✓ No double-parking. Atomic check + park under lock.
```

---

## 6) Testing Strategy (~3 min)

**Functional tests:**
- Park a bike, exit, verify fee = hourlyRate × hours (rounded up)
- Park when lot is full → RuntimeException
- Exit with invalid ticket → IllegalArgumentException
- Double exit → IllegalStateException

**Concurrency tests:**
- **Different types in parallel**: 5 bikes + 3 cars + 2 trucks concurrently → all 10 succeed
- **Same type, limited slots**: 10 bikes, only 5 BIKE slots → exactly 5 succeed, 5 fail
- **Concurrent park and exit**: 3 threads parking + 3 threads exiting simultaneously → no race conditions

**Edge cases:**
- Vehicle type doesn't match any slot type
- All slots of a type are occupied
- Concurrent exit of same ticket (only one should succeed)

---

## 7) Extensibility (~5 min)

**"How would you add a new vehicle type (e.g., BUS)?"**
> "Add BUS to VehicleType and SlotType enums, create a Bus class implementing Vehicle, add BUS slots to floors, and update the pricing rates. No changes to ParkingManager or Floor."

**"How would you add dynamic pricing (peak hours)?"**
> "PricingStrategy is already an interface. I'd implement a new DynamicPricing that checks the hour and applies a multiplier. Inject it into ParkingManager — no changes to existing code."

```java
class DynamicPricing implements PricingStrategy {
    @Override
    public double calculateFee(Ticket ticket) {
        double multiplier = isPeakHour(ticket.getEntryTime()) ? 2.0 : 1.0;
        return baseRate * ticket.getDurationHours() * multiplier;
    }
}
```

**"How would you optimize the O(F×S) slot search?"**
> "Maintain a per-type queue of available slots. parkVehicle polls from the queue (O(1)), exitVehicle adds back to the queue. The slot-level lock still protects the actual park/free operation."

**"How would you add a reservation system?"**
> "Add a reservedBy field to ParkingSlot and a Reservation entity. parkVehicle checks if the slot is reserved for the incoming vehicle before parking. The lock structure doesn't change."

**"How would you scale to millions of slots?"**
> "The design already scales horizontally — slots are independent, locks are per-slot. For distributed systems, replace in-memory locks with Redis distributed locks and TicketRepository with a database. The class structure stays the same."

---

### Complexity

| Operation | Time | Space |
|---|---|---|
| **parkVehicle** | O(F × S) worst case | O(1) |
| **exitVehicle** | O(1) | O(1) |
| **getAvailableSlots** | O(F × S) | O(1) |

*F = floors, S = slots per floor. With slot indexing: parkVehicle becomes O(1).*

---

**Implementation**: See [ParkingLotSystemComplete.java](./ParkingLotSystemComplete.java)
