# ECOMMERCE SYSTEM - Low Level Design Solution

> **Following LLD_INTERVIEW_TEMPLATE.md with emphasis on concurrency, scalability, and clean architecture**

---

## 📋 STEP 1: REQUIREMENTS GATHERING

### Functional Requirements
1. **FR1**: Users can browse products by category
2. **FR2**: Users can add/remove items to/from shopping cart
3. **FR3**: Users can place orders from their cart
4. **FR4**: System processes payments for orders
5. **FR5**: Users can cancel orders (before shipping)
6. **FR6**: System manages product inventory
7. **FR7**: System sends notifications on order status changes
8. **FR8**: System applies discounts based on business rules

### Non-Functional Requirements
1. **NFR1**: Concurrency - 1000+ concurrent users
2. **NFR2**: Performance - <200ms response time for cart operations
3. **NFR3**: Scale - 100K+ products, 1M+ orders
4. **NFR4**: Consistency - No overselling of products
5. **NFR5**: Availability - 99.9% uptime

### Assumptions
1. In-memory storage (no database persistence)
2. Simplified payment processing (simulation)
3. Basic user authentication (user ID based)
4. Single currency support

### Out of Scope
1. User registration/authentication
2. Product reviews and ratings
3. Shipping and logistics
4. Tax calculations
5. Multi-currency support

---

## 🏗️ STEP 2: DOMAIN MODELING

### Core Entities

#### Product
- **Purpose**: Represents items available for purchase
- **Key Attributes**: id, name, category, price, inventory
- **Responsibilities**: Manage inventory, provide product details
- **Relationships**: 1:N with OrderItem, 1:N with CartItem
- **Lifecycle**: Created by admin, inventory updated on orders

#### User
- **Purpose**: Represents customers using the system
- **Key Attributes**: id, name, email, address
- **Responsibilities**: Identity management
- **Relationships**: 1:1 with ShoppingCart, 1:N with Order
- **Lifecycle**: Created on registration, persists throughout

#### ShoppingCart
- **Purpose**: Temporary storage for items before purchase
- **Key Attributes**: userId, items, lastUpdated
- **Responsibilities**: Manage cart items, calculate totals
- **Relationships**: 1:1 with User, 1:N with CartItem
- **Lifecycle**: Created on first use, cleaned up after 24h

#### Order
- **Purpose**: Represents a purchase transaction
- **Key Attributes**: id, userId, items, totalAmount, status
- **Responsibilities**: Track order lifecycle, manage status
- **Relationships**: N:1 with User, 1:N with OrderItem, 1:1 with Payment
- **Lifecycle**: Created → Confirmed → Shipped → Delivered

#### Payment
- **Purpose**: Represents payment transaction
- **Key Attributes**: id, orderId, amount, method, status
- **Responsibilities**: Process payment, track status
- **Relationships**: 1:1 with Order
- **Lifecycle**: Created → Processed → Success/Failed

### Domain Model Diagram
```
User (1) ──has──> (1) ShoppingCart
ShoppingCart (1) ──contains──> (N) CartItem
CartItem (N) ──references──> (1) Product

User (1) ──places──> (N) Order
Order (1) ──contains──> (N) OrderItem
OrderItem (N) ──references──> (1) Product
Order (1) ──has──> (1) Payment
```

---

## 🎨 STEP 3: DESIGN PATTERNS & ARCHITECTURE

### Architecture Layers
```
┌─────────────────────────────────────┐
│     Demo/Main Layer                 │ ← Entry points & testing
├─────────────────────────────────────┤
│     Service Layer                   │ ← Business logic (CartService, OrderService)
├─────────────────────────────────────┤
│     Repository Layer                │ ← Data access (ProductRepo, OrderRepo)
├─────────────────────────────────────┤
│     Domain/Model Layer              │ ← Entities (Product, Order, User)
└─────────────────────────────────────┘
```

### Design Patterns Used

#### Strategy Pattern - Pricing & Discounts
- **Problem**: Different discount calculation algorithms
- **Solution**: Pluggable pricing strategies
- **Implementation**: `PricingStrategy` interface with `BulkDiscountStrategy`, `CategoryDiscountStrategy`
- **Trade-offs**: Flexibility vs complexity

#### Observer Pattern - Notifications
- **Problem**: Notify multiple services on order status changes
- **Solution**: Observer pattern for loose coupling
- **Implementation**: `OrderObserver` interface with `EmailNotificationService`, `SMSNotificationService`
- **Trade-offs**: Decoupling vs potential notification failures

#### Repository Pattern - Data Access
- **Problem**: Separate business logic from data access
- **Solution**: Repository abstraction layer
- **Implementation**: `ProductRepository`, `OrderRepository`, etc.
- **Trade-offs**: Clean separation vs additional abstraction

---

## 🔐 STEP 4: CONCURRENCY CONTROL

### Shared Resources Analysis
✅ **Product inventory** - Modified by multiple order creations
✅ **Shopping carts** - Modified by concurrent add/remove operations
✅ **Order status** - Updated by payment processing and cancellations
✅ **Payment processing** - Concurrent payment attempts

### Concurrency Strategies

#### 1. Product Inventory Management
- **Level**: Read-Write Lock (`ReentrantReadWriteLock`)
- **Approach**: Many reads (availability check), few writes (reservation)
- **Critical Section**: `reserveInventory()`, `releaseInventory()`
- **Race Condition Prevented**: Overselling products

```java
public boolean reserveInventory(int quantity) {
    inventoryLock.writeLock().lock();
    try {
        if (inventory >= quantity) {
            inventory -= quantity;
            return true;
        }
        return false;
    } finally {
        inventoryLock.writeLock().unlock();
    }
}
```

#### 2. Shopping Cart Operations
- **Level**: Method-level synchronization with `ReentrantLock`
- **Approach**: Fine-grained locking per cart operation
- **Critical Section**: `addItem()`, `removeItem()`, `clear()`
- **Race Condition Prevented**: Inconsistent cart state

#### 3. Order Creation (ACID Properties)
- **Atomicity**: All inventory reservations succeed or all fail
- **Consistency**: Inventory always accurate
- **Isolation**: Concurrent orders don't interfere
- **Durability**: Order state persisted immediately

#### 4. Thread-Safe Data Structures
```java
ConcurrentHashMap<K,V>      // For repositories and user carts
AtomicInteger              // For counters (order ID, payment ID)
volatile                   // For status fields with visibility guarantees
```

### Deadlock Prevention
- **Lock Ordering**: Not applicable (single locks per operation)
- **Timeout**: Not used (operations are fast)
- **Resource Ordering**: Inventory locks acquired in product ID order

---

## 💻 STEP 5: CLASS DESIGN & IMPLEMENTATION

### Key Classes with Thread-Safety

#### Product Class
```java
/**
 * Thread-safe using ReentrantReadWriteLock for inventory updates
 */
class Product {
    private volatile int inventory;
    private final ReentrantReadWriteLock inventoryLock;
    
    public boolean reserveInventory(int quantity) { /* Write lock */ }
    public boolean isAvailable(int quantity) { /* Read lock */ }
    public void releaseInventory(int quantity) { /* Write lock */ }
}
```

#### ShoppingCart Class
```java
/**
 * Thread-safe shopping cart with session management
 */
class ShoppingCart {
    private final ConcurrentHashMap<String, CartItem> items;
    private final ReentrantLock cartLock;
    
    public boolean addItem(Product product, int quantity) { /* Synchronized */ }
    public boolean removeItem(String productId) { /* Synchronized */ }
}
```

#### OrderService Class
```java
/**
 * Thread-safe order management with ACID properties
 */
class OrderService {
    public Order createOrder(String userId) {
        // Phase 1: Reserve all inventory atomically
        // Phase 2: Create order or rollback on failure
    }
}
```

### SOLID Principles Applied
✅ **Single Responsibility**: Each class has one clear purpose
✅ **Open/Closed**: Strategy pattern allows extension without modification
✅ **Liskov Substitution**: All strategy implementations are substitutable
✅ **Interface Segregation**: Small, focused interfaces (PricingStrategy, OrderObserver)
✅ **Dependency Inversion**: Services depend on repository abstractions

---

## 🧪 STEP 6: TESTING STRATEGY

### Test Categories

#### Unit Tests (70%)
- Product inventory operations
- Cart add/remove operations
- Order status transitions
- Payment processing simulation
- Discount calculations

#### Concurrency Tests (20%)
```java
@Test
public void testConcurrentInventoryReservation() {
    // 10 threads trying to reserve same product
    // Verify no overselling occurs
}

@Test
public void testConcurrentCartOperations() {
    // Multiple threads adding/removing from same cart
    // Verify cart consistency
}
```

#### Integration Tests (10%)
- Complete user workflow (cart → order → payment)
- Order cancellation and inventory release
- Notification system integration

### Edge Cases Tested
- Empty cart order creation
- Insufficient inventory scenarios
- Payment failure handling
- Invalid order status transitions
- Concurrent order creation with limited inventory

---

## 📊 STEP 7: SCALABILITY & TRADE-OFFS

### Design Trade-offs

#### Decision: In-Memory Storage
**Pros:**
- Fast access times
- Simple implementation
- No database complexity

**Cons:**
- Data loss on restart
- Memory limitations
- No persistence

**Alternatives Considered:**
- Database storage: Better persistence but slower
- Hybrid approach: Cache + database

#### Decision: Fine-Grained Locking
**Pros:**
- High concurrency
- Minimal lock contention
- Better performance

**Cons:**
- More complex code
- Potential for deadlocks (mitigated)

### Scalability Analysis

#### Current Limitations
- **Bottleneck**: Memory usage for large product catalogs
- **Breaking Point**: ~1M products in memory

#### Scaling Strategies
1. **Horizontal**: Shard products by category
2. **Caching**: Redis for frequently accessed products
3. **Database**: Move to persistent storage
4. **Async Processing**: Decouple notifications
5. **Load Balancing**: Multiple service instances

#### Performance Characteristics
- **Time Complexity**: O(1) for most operations
- **Space Complexity**: O(P + U + O) where P=products, U=users, O=orders
- **Concurrency**: Scales linearly with threads for different products

---

## 🚀 STEP 8: EXTENSIBILITY & FUTURE ENHANCEMENTS

### Extension Points

#### Adding New Discount Types
1. Implement `PricingStrategy` interface
2. Register in service configuration
3. No changes to existing code

#### Adding New Notification Channels
1. Implement `OrderObserver` interface
2. Register with `OrderService`
3. Automatic integration

#### Adding New Payment Methods
1. Extend `PaymentMethod` enum
2. Update `PaymentService` logic
3. Backward compatible

### Future Roadmap

#### Phase 1: Immediate (< 1 month)
- Database persistence layer
- User authentication system
- Basic admin panel

#### Phase 2: Short-term (1-3 months)
- Product search and filtering
- Order history and tracking
- Email/SMS integration

#### Phase 3: Long-term (3-6 months)
- Recommendation engine
- Multi-tenant support
- Analytics and reporting

---

## 🎯 CONCURRENCY HIGHLIGHTS

### Thread-Safety Guarantees
- **Product inventory**: Always consistent, no overselling
- **Shopping carts**: Atomic operations, no lost updates
- **Order creation**: ACID properties maintained
- **Payment processing**: Idempotent operations

### Performance Optimizations
- Read-write locks for inventory (many reads, few writes)
- ConcurrentHashMap for repositories (lock-free reads)
- Atomic counters for ID generation
- Minimal lock scope to reduce contention

### Race Condition Prevention
- Inventory reservation before order creation
- Atomic cart operations with proper locking
- Status transition validation with business rules
- Observer notification error isolation

---

## 📝 HOW TO RUN

```bash
# Compilation
cd /Users/rajan/Desktop/personal-projects/LLD-Handbook
javac -d target/classes src/main/java/com/rajan/lld/InterviewQuestionsPractice/EcommerceSystem/*.java

# Execution
java -cp target/classes com.rajan.lld.InterviewQuestionsPractice.EcommerceSystem.EcommerceSystemComplete
```

### Expected Output
```
======================================================================
ECOMMERCE SYSTEM - CONCURRENCY DEMO
======================================================================

✅ Setup: 5 products, 3 users

TEST 1: Single User Complete Workflow
----------------------------------------------------------------------
✅ Cart total: $1389.95
✅ Order created: ORD-1
📧 Email sent: Order ORD-1 status changed from PENDING to CONFIRMED
📱 SMS sent: Order ORD-1 is now CONFIRMED
✅ Order confirmed: true

TEST 2: Concurrent Cart Operations
----------------------------------------------------------------------
✅ Concurrent operations completed. Cart items: 1

TEST 3: Concurrent Order Creation (Inventory Contention)
----------------------------------------------------------------------
✅ Successful orders: 3/3 (Limited by inventory)

TEST 4: Order Cancellation and Inventory Release
----------------------------------------------------------------------
✅ Order created: ORD-5
📧 Email sent: Order ORD-5 status changed from PENDING to CANCELLED
📱 SMS sent: Order ORD-5 is now CANCELLED
✅ Order cancelled: true
✅ Inventory released back to pool

======================================================================
ALL TESTS PASSED! ✅
======================================================================
```

---

## 🎓 KEY DESIGN DECISIONS

### 1. Inventory Management Strategy
- **Choice**: Pessimistic locking with immediate reservation
- **Rationale**: Prevents overselling in high-concurrency scenarios
- **Alternative**: Optimistic locking with retry logic

### 2. Cart Session Management
- **Choice**: In-memory with scheduled cleanup
- **Rationale**: Fast access, automatic cleanup of abandoned carts
- **Alternative**: Database storage with TTL

### 3. Order Processing Flow
- **Choice**: Two-phase commit (reserve → confirm)
- **Rationale**: ACID properties with rollback capability
- **Alternative**: Event-driven saga pattern

### 4. Notification System
- **Choice**: Synchronous observer pattern
- **Rationale**: Immediate notifications, simple implementation
- **Alternative**: Asynchronous message queue

This ecommerce system demonstrates enterprise-level design patterns with strong concurrency control, following the same architectural principles as your ParkingLot and MovieBooking systems while addressing the unique challenges of inventory management and order processing.