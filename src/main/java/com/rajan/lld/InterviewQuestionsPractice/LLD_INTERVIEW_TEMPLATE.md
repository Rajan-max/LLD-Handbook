# LLD Interview Delivery Framework 🎯

> A practical, time-boxed framework for delivering low-level design interviews in ~35-45 minutes of active design time within a 60-minute interview.

---

## ⏱️ Time Budget

| Phase | Time | What You're Doing |
|-------|------|-------------------|
| **1. Requirements** | ~5 min | Clarify the prompt, write a spec |
| **2. Entities & Relationships** | ~3 min | Identify core objects and ownership |
| **3. Class Design** | ~10 min | State + behavior for each class |
| **4. Concurrency** | ~5 min | Only if the problem demands it |
| **5. Implementation** | ~10 min | Core methods + walkthrough |
| **6. Testing** | ~3 min | Key scenarios to verify |
| **7. Extensibility** | ~5 min | Interviewer-led follow-ups |

If the interviewer pulls you in a different direction, follow their lead. But gently guide back to ensure you cover the important parts.

---

## 1) Requirements (~5 min)

The prompt is intentionally vague. Your job is to turn it into a spec you can design around.

### Clarifying Question Themes

Work through these four themes to quickly uncover the full expected behavior:

- **Primary capabilities** — What operations must this system support?
- **Rules and completion** — What conditions define success, failure, or state transitions?
- **Error handling** — How should the system respond to invalid inputs or actions?
- **Scope boundaries** — What's in scope (core logic, business rules) and what's explicitly out (UI, storage, networking)?

### Output Format

Write this on the whiteboard. Confirm with your interviewer before moving on.

**Example — Hotel Booking System:**

```
Requirements:
1. Support room types (SINGLE, DOUBLE, SUITE) with different pricing
2. Search available rooms by type and date range
3. Reserve a room for a date range (two-step: lock → confirm)
4. Calculate total based on nights × room rate
5. Cancel bookings and release dates
6. Prevent double-booking under concurrent access

Out of Scope:
- Payment gateway integration
- Check-in/check-out workflow
- Multi-hotel management
- Dynamic pricing / cancellation policies
- Guest loyalty programs
```

**Example — Parking Lot System:**

```
Requirements:
1. Multiple floors, each with slots of different types (BIKE, CAR, TRUCK)
2. Park vehicle in first available matching slot, issue ticket
3. Calculate fee based on duration and vehicle type (hourly pricing)
4. Free slot on exit
5. No double-parking under concurrent access

Out of Scope:
- Reservation system
- Valet parking / EV charging
- Payment gateway
- User authentication
```

### Why This Matters

Without clear requirements, you'll either build the wrong thing or waste time on features the interviewer doesn't care about. A 2-minute clarification saves 10 minutes of rework.

---

## 2) Entities & Relationships (~3 min)

Scan your requirements and pull out the meaningful nouns. These are the "things" that need to exist in your system.

### Simple Filter

- If it **maintains changing state** or **enforces rules** → it's an entity (its own class)
- If it's just **information attached to something else** → it's a field on another class

### What to Capture

For each entity, think about:
- Which entity is the **orchestrator** — the one driving the main workflow?
- Which entities **own durable state**?
- How do they **depend on each other**? (has-a, uses, contains)

### Output Format

Don't overthink notation. Simple boxes, arrows, and labels on the whiteboard.

**Example — Hotel Booking System:**

```
Entities:
- Room          (bookable resource, owns date-based schedule)
- Guest         (person making the booking)
- Booking       (links guest + room + dates, tracks status)
- RoomManager   (resource registry + locks)
- BookingManager (orchestrator — search, lock, confirm, cancel)

Relationships:
- BookingManager → RoomManager (uses for room lookup + locks)
- BookingManager → BookingRepository (persists bookings)
- Booking → Guest, Room (references)
- Room grouped by RoomType
```

**Example — Parking Lot System:**

```
Entities:
- ParkingSlot   (individual space, owns occupied state)
- Floor         (contains slots + per-slot locks)
- ParkingLot    (contains floors)
- Vehicle       (Bike, Car, Truck — what's being parked)
- Ticket        (links slot + vehicle + entry time)
- ParkingManager (orchestrator — park, exit, fee calculation)

Relationships:
- ParkingLot → Floor (1:N)
- Floor → ParkingSlot (1:N) + per-slot locks
- Ticket → ParkingSlot, Vehicle
- ParkingManager → ParkingLot, TicketRepository, PricingStrategy
```

This is all your interviewer needs to follow your thinking. From here, you turn these into well-defined classes.

---

## 3) Class Design (~10 min)

Go entity by entity, top-down. Start with the orchestrator, then supporting entities.

For each class, answer two questions:
- **State** — What does this class need to remember?
- **Behavior** — What does this class need to do?

### Deriving State from Requirements

Go back to your requirements and ask: *Which requirements does this entity own? What must it track?*

**Example — Hotel Booking System:**

| Requirement | What BookingManager must track |
|---|---|
| "Search rooms by type and dates" | RoomManager (room registry) |
| "Reserve room (lock → confirm)" | BookingRepository (persists bookings) |
| "Prevent double-booking" | Room-level locks via RoomManager |

| Requirement | What Room must track |
|---|---|
| "Date-based availability" | bookingSchedule: Map<LocalDate, String> |
| "Room type and pricing" | type: RoomType |

### Deriving Behavior from Requirements

Ask: *What operations does the outside world need from this class?*

**Example — Hotel Booking System:**

| Need from requirements | Method |
|---|---|
| Search and reserve a room | SearchAndLockRoom(guest, type, checkIn, checkOut) → Booking |
| Confirm after payment | confirmBooking(bookingId) → Booking |
| Cancel and release dates | cancelBooking(bookingId) |

### Output Format

Write the class outlines on the whiteboard. Don't worry about syntax — communicate structure.

```
class Room:
  - id: String
  - type: RoomType
  - bookingSchedule: Map<LocalDate, String>

  + isAvailable(checkIn, checkOut) → bool       // Caller MUST hold lock
  + reserve(checkIn, checkOut, bookingId)        // Caller MUST hold lock
  + release(checkIn, checkOut)                   // Caller MUST hold lock

class RoomManager:
  - roomsByType: Map<RoomType, List<Room>>
  - roomLocks: Map<String, ReentrantLock>

  + addRoom(room)
  + getRoomByType(type) → List<Room>
  + getRoomLock(roomId) → ReentrantLock

class BookingManager:
  - roomManager: RoomManager
  - bookingRepository: BookingRepository

  + SearchAndLockRoom(guest, type, checkIn, checkOut) → Booking
  + confirmBooking(bookingId) → Booking
  + cancelBooking(bookingId)

class Booking:
  - id, guest, room, checkIn, checkOut, totalAmount
  - status: PENDING → CONFIRMED / CANCELLED

class Guest:
  - id, name (immutable)
```

### Key Principle

Keep rules with the entity that owns the relevant state:
- **Workflow rules** (can this operation run now?) → orchestrator (BookingManager)
- **Data rules** (is this date available?) → entity that owns the data (Room)

---

## 4) Concurrency Control (~5 min)

**Only if the problem involves concurrent access.** Skip entirely for single-threaded problems like Tic Tac Toe.

### Three Questions to Answer

1. **What is shared?** — Which data can multiple threads touch simultaneously?
2. **What can go wrong?** — What race conditions exist?
3. **What's the locking strategy?** — At what granularity do you lock?

### Locking Granularity Decision

| Granularity | Throughput | Complexity | When to Use |
|---|---|---|---|
| **System-level** | Very low | Simple | Never in interviews (too coarse) |
| **Resource-level** | High | Moderate | Most problems (room, vehicle, slot) |
| **Sub-resource** | Very high | Complex | Only if needed (seat-level in movie booking) |

Almost always, **resource-level locking** is the right answer. Each resource (room, vehicle, parking slot) gets its own ReentrantLock.

### Output Format

State this clearly to the interviewer:

```
Concurrency Strategy: Room-level locking

Shared resources:
- Room.bookingSchedule — multiple threads booking same room

Race condition prevented:
- Double-booking: Two threads book same room for overlapping dates
  → Prevented by acquiring room lock before check + reserve (atomic)

Locking approach:
- Each room has its own ReentrantLock (fair lock)
- tryLock with 5s timeout to prevent indefinite blocking
- Lock ordering not needed (single lock per operation, no deadlock risk)

Thread-safety:
- Room: volatile + external lock (caller MUST hold lock)
- Booking: volatile status + immutable fields
- Repositories: ConcurrentHashMap
```

### Multi-Resource Locking (Deadlock Prevention)

If a single operation needs to lock multiple resources (e.g., multiple seats in movie booking):

```
DEADLOCK PREVENTION: Always acquire locks in sorted order

// Sort seat IDs before locking
List<String> sorted = new ArrayList<>(seatIds);
Collections.sort(sorted);

// Acquire locks in sorted order
for (String seatId : sorted) {
    lock = show.getSeatLock(seatId);
    lock.tryLock(5, TimeUnit.SECONDS);
}
```

---

## 5) Implementation (~10 min)

Implement the core methods that show how your classes cooperate, how state transitions occur, and how edge cases are handled. Ask your interviewer which methods they want to see.

### Focus On

- **Happy path first** — the normal flow when everything goes right
- **Edge cases second** — invalid inputs, illegal state transitions, timeouts
- **The most interesting method** — usually the one with concurrency or complex logic

### Example — SearchAndLockRoom (Hotel Booking)

```java
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

**What this demonstrates:**
- Atomic check-and-reserve (no gap between availability check and reservation)
- tryLock with timeout (non-blocking, prevents indefinite wait)
- Proper lock release in finally block
- Iterates through all rooms of type (first available wins)

### Example — parkVehicle (Parking Lot)

```java
public Ticket parkVehicle(Vehicle vehicle) {
    for (Floor floor : parkingLot.getFloors()) {
        for (ParkingSlot slot : floor.getSlots()) {
            if (slot.getType() != SlotType.valueOf(vehicle.getType().name()))
                continue;

            ReentrantLock lock = floor.getLockForSlot(slot.getId());
            if (lock.tryLock()) {
                try {
                    if (slot.canFit(vehicle)) {
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

### Verification: Walk Through a Scenario

After implementing, trace through a concrete example (1-2 min):

```
Scenario: Two threads try to book the same DOUBLE room for overlapping dates

Thread A: SearchAndLockRoom(Guest A, DOUBLE, Jan 5-10)
  → Acquires lock on Room R2
  → R2.isAvailable(Jan 5-10) = true
  → R2.reserve(Jan 5-10, "BK-1001")
  → Returns Booking BK-1001
  → Releases lock

Thread B: SearchAndLockRoom(Guest B, DOUBLE, Jan 8-12)
  → Acquires lock on Room R2 (after Thread A releases)
  → R2.isAvailable(Jan 8-12) = false (Jan 8,9 already reserved)
  → Moves to next room or throws "No available rooms"

✓ No double-booking. Atomic check + reserve under lock.
```

This catches logical errors before the interviewer finds them.

---

## 6) Testing Strategy (~3 min)

You won't write full tests in the interview. But briefly describe the key scenarios you'd verify.

### What to Cover

**Functional tests** — Does the happy path work?
- Book a room, confirm it, verify status
- Park a vehicle, exit, verify fee calculation

**Concurrency tests** — Does it hold under parallel access?
- **Single resource contention**: 10 threads, 1 room, same dates → only 1 succeeds
- **Parallel different resources**: 10 threads, 10 rooms → all 10 succeed
- **Overlapping conflicts**: 2 threads, overlapping date ranges → 1 fails
- **Release + re-acquire**: Cancel/return, then another thread books → both succeed

**Edge cases** — Does it fail gracefully?
- Invalid dates (checkOut before checkIn)
- Booking not found
- Double cancellation
- All resources full

### Example — How to State This

> "I'd write four concurrency tests: single-room contention where only 1 of 10 threads succeeds, parallel booking of different rooms where all succeed, overlapping date conflict where one thread is correctly rejected, and a cancel-then-rebook flow where both operations succeed. These cover the main race conditions."

---

## 7) Extensibility (~5 min, if time allows)

This is usually interviewer-led. They'll propose a twist to see if your design can evolve cleanly.

### How to Answer

Stay high-level. Point to the parts of your design that make the change clean. Don't rewrite code.

### Common Extensions and How to Handle Them

**"How would you add different pricing strategies?"**
> "I'd extract pricing into a Strategy interface. The manager takes a PricingStrategy in its constructor. To add surge pricing or weekly discounts, I implement a new strategy — no changes to existing code."

```java
interface PricingStrategy {
    double calculateFee(RoomType type, long nights);
}

class SeasonalPricing implements PricingStrategy { ... }
class LoyaltyPricing implements PricingStrategy { ... }
```

**"How would you support multiple hotels/locations?"**
> "Each hotel would have its own RoomManager. A top-level HotelManager routes requests to the right hotel's RoomManager. The BookingManager and Room classes don't change."

**"How would you add a notification system?"**
> "I'd add an Observer/event mechanism. When a booking is confirmed or cancelled, the BookingManager fires an event. Notification channels (email, SMS, push) subscribe to these events. The core booking logic doesn't change."

**"How would you scale this to millions of rooms?"**
> "The current design already supports horizontal scaling — rooms are independent, locks are per-room. For distributed systems, I'd replace in-memory locks with Redis distributed locks and ConcurrentHashMap with a database. The class structure stays the same."

### Key Principle

If your initial design is well-structured (clean separation, single responsibility, resource-level locking), most extensions are additive — you add new classes without modifying existing ones. That's the signal the interviewer is looking for.

---

## Quick Reference Card

### Before You Start Coding
```
□ Requirements written and confirmed with interviewer
□ Core entities identified with ownership boundaries
□ Class outlines with state + behavior
□ Concurrency strategy stated (if applicable)
```

### During Implementation
```
□ Happy path first, edge cases second
□ Lock acquisition with timeout + finally block
□ Atomic check-and-mutate under lock
□ Walk through a concrete scenario to verify
```

### Common Mistakes to Avoid
```
✗ Diving into code before clarifying requirements
✗ Modeling every noun as a separate class
✗ Forcing design patterns where they don't add value
✗ Ignoring concurrency until the end
✗ Spending too long on one section
✗ Fighting the interviewer's direction
```

---

**Remember**: This framework is a guide, not a rigid script. Adapt based on the problem, the interviewer's focus, and the time remaining. The goal is to demonstrate clear thinking, clean structure, and production-level awareness — not to check every box.
