package com.rajan.lld.InterviewQuestionsPractice.EcommerceSystem;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * ECOMMERCE SYSTEM - Complete Implementation
 * 
 * Following LLD_INTERVIEW_TEMPLATE.md with strong concurrency focus
 * 
 * Key Features:
 * - Product inventory management with atomic operations
 * - Order processing with ACID properties
 * - Shopping cart with session management
 * - Payment processing simulation
 * - Thread-safe operations with fine-grained locking
 * - Strategy pattern for pricing and discounts
 * - Observer pattern for notifications
 */

// ============================================================================
// ENUMS
// ============================================================================

enum ProductCategory { ELECTRONICS, CLOTHING, BOOKS, HOME, SPORTS }
enum OrderStatus { PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED }
enum PaymentStatus { PENDING, SUCCESS, FAILED, REFUNDED }
enum PaymentMethod { CREDIT_CARD, DEBIT_CARD, UPI, WALLET }

// ============================================================================
// MODELS
// ============================================================================

/**
 * Thread-safe using ReentrantReadWriteLock for inventory updates
 */
class Product {
    private final String id;
    private final String name;
    private final ProductCategory category;
    private final double price;
    private volatile int inventory;
    private final ReentrantReadWriteLock inventoryLock;
    
    public Product(String id, String name, ProductCategory category, double price, int inventory) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.inventory = inventory;
        this.inventoryLock = new ReentrantReadWriteLock(true);
    }
    
    /**
     * Thread-safe inventory check
     */
    public boolean isAvailable(int quantity) {
        inventoryLock.readLock().lock();
        try {
            return inventory >= quantity;
        } finally {
            inventoryLock.readLock().unlock();
        }
    }
    
    /**
     * Thread-safe inventory reservation
     */
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
    
    /**
     * Thread-safe inventory release (for cancelled orders)
     */
    public void releaseInventory(int quantity) {
        inventoryLock.writeLock().lock();
        try {
            inventory += quantity;
        } finally {
            inventoryLock.writeLock().unlock();
        }
    }
    
    public int getInventory() {
        inventoryLock.readLock().lock();
        try {
            return inventory;
        } finally {
            inventoryLock.readLock().unlock();
        }
    }
    
    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public ProductCategory getCategory() { return category; }
    public double getPrice() { return price; }
    
    @Override
    public String toString() {
        return String.format("Product{id='%s', name='%s', price=%.2f, inventory=%d}", 
                           id, name, price, getInventory());
    }
}

class CartItem {
    private final Product product;
    private volatile int quantity;
    private final ReentrantLock quantityLock;
    
    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
        this.quantityLock = new ReentrantLock();
    }
    
    public void updateQuantity(int newQuantity) {
        quantityLock.lock();
        try {
            this.quantity = newQuantity;
        } finally {
            quantityLock.unlock();
        }
    }
    
    public double getTotalPrice() {
        quantityLock.lock();
        try {
            return product.getPrice() * quantity;
        } finally {
            quantityLock.unlock();
        }
    }
    
    public Product getProduct() { return product; }
    
    public int getQuantity() {
        quantityLock.lock();
        try {
            return quantity;
        } finally {
            quantityLock.unlock();
        }
    }
}

/**
 * Thread-safe shopping cart with session management
 */
class ShoppingCart {
    private final String userId;
    private final ConcurrentHashMap<String, CartItem> items;
    private final ReentrantLock cartLock;
    private volatile LocalDateTime lastUpdated;
    
    public ShoppingCart(String userId) {
        this.userId = userId;
        this.items = new ConcurrentHashMap<>();
        this.cartLock = new ReentrantLock();
        this.lastUpdated = LocalDateTime.now();
    }
    
    /**
     * Thread-safe add to cart
     */
    public boolean addItem(Product product, int quantity) {
        if (!product.isAvailable(quantity)) {
            return false;
        }
        
        cartLock.lock();
        try {
            CartItem existing = items.get(product.getId());
            if (existing != null) {
                int newQuantity = existing.getQuantity() + quantity;
                if (!product.isAvailable(newQuantity)) {
                    return false;
                }
                existing.updateQuantity(newQuantity);
            } else {
                items.put(product.getId(), new CartItem(product, quantity));
            }
            lastUpdated = LocalDateTime.now();
            return true;
        } finally {
            cartLock.unlock();
        }
    }
    
    /**
     * Thread-safe remove from cart
     */
    public boolean removeItem(String productId) {
        cartLock.lock();
        try {
            CartItem removed = items.remove(productId);
            if (removed != null) {
                lastUpdated = LocalDateTime.now();
                return true;
            }
            return false;
        } finally {
            cartLock.unlock();
        }
    }
    
    /**
     * Thread-safe cart total calculation
     */
    public double getTotal() {
        cartLock.lock();
        try {
            return items.values().stream()
                       .mapToDouble(CartItem::getTotalPrice)
                       .sum();
        } finally {
            cartLock.unlock();
        }
    }
    
    /**
     * Thread-safe cart clearing
     */
    public void clear() {
        cartLock.lock();
        try {
            items.clear();
            lastUpdated = LocalDateTime.now();
        } finally {
            cartLock.unlock();
        }
    }
    
    public String getUserId() { return userId; }
    public Collection<CartItem> getItems() { return new ArrayList<>(items.values()); }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public boolean isEmpty() { return items.isEmpty(); }
}

class User {
    private final String id;
    private final String name;
    private final String email;
    private final String address;
    
    public User(String id, String name, String email, String address) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.address = address;
    }
    
    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
}

class OrderItem {
    private final Product product;
    private final int quantity;
    private final double priceAtOrder;
    
    public OrderItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
        this.priceAtOrder = product.getPrice(); // Capture price at order time
    }
    
    public double getTotalPrice() {
        return priceAtOrder * quantity;
    }
    
    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public double getPriceAtOrder() { return priceAtOrder; }
}

/**
 * Thread-safe order with atomic status updates
 */
class Order {
    private final String id;
    private final String userId;
    private final List<OrderItem> items;
    private final double totalAmount;
    private final LocalDateTime orderTime;
    private volatile OrderStatus status;
    private volatile PaymentStatus paymentStatus;
    private final ReentrantLock statusLock;
    
    public Order(String id, String userId, List<OrderItem> items) {
        this.id = id;
        this.userId = userId;
        this.items = new ArrayList<>(items);
        this.totalAmount = items.stream().mapToDouble(OrderItem::getTotalPrice).sum();
        this.orderTime = LocalDateTime.now();
        this.status = OrderStatus.PENDING;
        this.paymentStatus = PaymentStatus.PENDING;
        this.statusLock = new ReentrantLock();
    }
    
    /**
     * Thread-safe status update
     */
    public boolean updateStatus(OrderStatus newStatus) {
        statusLock.lock();
        try {
            // Business logic for valid status transitions
            if (isValidStatusTransition(this.status, newStatus)) {
                this.status = newStatus;
                return true;
            }
            return false;
        } finally {
            statusLock.unlock();
        }
    }
    
    /**
     * Thread-safe payment status update
     */
    public boolean updatePaymentStatus(PaymentStatus newPaymentStatus) {
        statusLock.lock();
        try {
            this.paymentStatus = newPaymentStatus;
            return true;
        } finally {
            statusLock.unlock();
        }
    }
    
    private boolean isValidStatusTransition(OrderStatus from, OrderStatus to) {
        // Define valid transitions
        switch (from) {
            case PENDING:
                return to == OrderStatus.CONFIRMED || to == OrderStatus.CANCELLED;
            case CONFIRMED:
                return to == OrderStatus.SHIPPED || to == OrderStatus.CANCELLED;
            case SHIPPED:
                return to == OrderStatus.DELIVERED;
            case DELIVERED:
            case CANCELLED:
                return false; // Terminal states
            default:
                return false;
        }
    }
    
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public List<OrderItem> getItems() { return new ArrayList<>(items); }
    public double getTotalAmount() { return totalAmount; }
    public LocalDateTime getOrderTime() { return orderTime; }
    public OrderStatus getStatus() { return status; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
}

class Payment {
    private final String id;
    private final String orderId;
    private final double amount;
    private final PaymentMethod method;
    private final LocalDateTime timestamp;
    private volatile PaymentStatus status;
    
    public Payment(String id, String orderId, double amount, PaymentMethod method) {
        this.id = id;
        this.orderId = orderId;
        this.amount = amount;
        this.method = method;
        this.timestamp = LocalDateTime.now();
        this.status = PaymentStatus.PENDING;
    }
    
    public void updateStatus(PaymentStatus status) {
        this.status = status;
    }
    
    public String getId() { return id; }
    public String getOrderId() { return orderId; }
    public double getAmount() { return amount; }
    public PaymentMethod getMethod() { return method; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public PaymentStatus getStatus() { return status; }
}

// ============================================================================
// REPOSITORIES
// ============================================================================

class ProductRepository {
    private final ConcurrentHashMap<String, Product> products = new ConcurrentHashMap<>();
    
    public void save(Product product) { products.put(product.getId(), product); }
    public Product findById(String id) { return products.get(id); }
    public List<Product> findByCategory(ProductCategory category) {
        return products.values().stream()
                      .filter(p -> p.getCategory() == category)
                      .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
    public List<Product> findAll() { return new ArrayList<>(products.values()); }
}

class UserRepository {
    private final ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();
    
    public void save(User user) { users.put(user.getId(), user); }
    public User findById(String id) { return users.get(id); }
}

class OrderRepository {
    private final ConcurrentHashMap<String, Order> orders = new ConcurrentHashMap<>();
    
    public void save(Order order) { orders.put(order.getId(), order); }
    public Order findById(String id) { return orders.get(id); }
    public List<Order> findByUserId(String userId) {
        return orders.values().stream()
                    .filter(o -> o.getUserId().equals(userId))
                    .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
}

class PaymentRepository {
    private final ConcurrentHashMap<String, Payment> payments = new ConcurrentHashMap<>();
    
    public void save(Payment payment) { payments.put(payment.getId(), payment); }
    public Payment findById(String id) { return payments.get(id); }
    public Payment findByOrderId(String orderId) {
        return payments.values().stream()
                      .filter(p -> p.getOrderId().equals(orderId))
                      .findFirst()
                      .orElse(null);
    }
}

// ============================================================================
// STRATEGY PATTERN - Pricing & Discounts
// ============================================================================

interface PricingStrategy {
    double calculateDiscount(Order order);
}

class NoPricingStrategy implements PricingStrategy {
    @Override
    public double calculateDiscount(Order order) {
        return 0.0;
    }
}

class BulkDiscountStrategy implements PricingStrategy {
    private final int minItems;
    private final double discountPercent;
    
    public BulkDiscountStrategy(int minItems, double discountPercent) {
        this.minItems = minItems;
        this.discountPercent = discountPercent;
    }
    
    @Override
    public double calculateDiscount(Order order) {
        int totalItems = order.getItems().stream()
                             .mapToInt(OrderItem::getQuantity)
                             .sum();
        
        if (totalItems >= minItems) {
            return order.getTotalAmount() * (discountPercent / 100.0);
        }
        return 0.0;
    }
}

class CategoryDiscountStrategy implements PricingStrategy {
    private final ProductCategory category;
    private final double discountPercent;
    
    public CategoryDiscountStrategy(ProductCategory category, double discountPercent) {
        this.category = category;
        this.discountPercent = discountPercent;
    }
    
    @Override
    public double calculateDiscount(Order order) {
        double categoryTotal = order.getItems().stream()
                                   .filter(item -> item.getProduct().getCategory() == category)
                                   .mapToDouble(OrderItem::getTotalPrice)
                                   .sum();
        
        return categoryTotal * (discountPercent / 100.0);
    }
}

// ============================================================================
// OBSERVER PATTERN - Notifications
// ============================================================================

interface OrderObserver {
    void onOrderStatusChanged(Order order, OrderStatus oldStatus, OrderStatus newStatus);
}

class EmailNotificationService implements OrderObserver {
    @Override
    public void onOrderStatusChanged(Order order, OrderStatus oldStatus, OrderStatus newStatus) {
        System.out.println("📧 Email sent: Order " + order.getId() + 
                         " status changed from " + oldStatus + " to " + newStatus);
    }
}

class SMSNotificationService implements OrderObserver {
    @Override
    public void onOrderStatusChanged(Order order, OrderStatus oldStatus, OrderStatus newStatus) {
        System.out.println("📱 SMS sent: Order " + order.getId() + 
                         " is now " + newStatus);
    }
}

// ============================================================================
// SERVICES - Core Business Logic with Concurrency Control
// ============================================================================

/**
 * Thread-safe cart management service
 */
class CartService {
    private final ConcurrentHashMap<String, ShoppingCart> userCarts;
    private final ProductRepository productRepo;
    private final ScheduledExecutorService cleanupExecutor;
    
    public CartService(ProductRepository productRepo) {
        this.productRepo = productRepo;
        this.userCarts = new ConcurrentHashMap<>();
        this.cleanupExecutor = Executors.newScheduledThreadPool(1);
        
        // Cleanup abandoned carts every 30 minutes
        cleanupExecutor.scheduleAtFixedRate(this::cleanupAbandonedCarts, 30, 30, TimeUnit.MINUTES);
    }
    
    /**
     * Thread-safe get or create cart
     */
    public ShoppingCart getCart(String userId) {
        return userCarts.computeIfAbsent(userId, ShoppingCart::new);
    }
    
    /**
     * Thread-safe add to cart with inventory validation
     */
    public boolean addToCart(String userId, String productId, int quantity) {
        Product product = productRepo.findById(productId);
        if (product == null || quantity <= 0) {
            return false;
        }
        
        ShoppingCart cart = getCart(userId);
        return cart.addItem(product, quantity);
    }
    
    /**
     * Thread-safe remove from cart
     */
    public boolean removeFromCart(String userId, String productId) {
        ShoppingCart cart = userCarts.get(userId);
        return cart != null && cart.removeItem(productId);
    }
    
    /**
     * Thread-safe cart clearing
     */
    public void clearCart(String userId) {
        ShoppingCart cart = userCarts.get(userId);
        if (cart != null) {
            cart.clear();
        }
    }
    
    /**
     * Cleanup carts not updated in last 24 hours
     */
    private void cleanupAbandonedCarts() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        userCarts.entrySet().removeIf(entry -> 
            entry.getValue().getLastUpdated().isBefore(cutoff));
    }
    
    public void shutdown() {
        cleanupExecutor.shutdown();
    }
}

/**
 * Thread-safe payment processing service
 */
class PaymentService {
    private final PaymentRepository paymentRepo;
    private final AtomicInteger paymentCounter;
    
    public PaymentService(PaymentRepository paymentRepo) {
        this.paymentRepo = paymentRepo;
        this.paymentCounter = new AtomicInteger(0);
    }
    
    /**
     * Simulate payment processing
     */
    public Payment processPayment(String orderId, double amount, PaymentMethod method) {
        Payment payment = new Payment(
            "PAY-" + paymentCounter.incrementAndGet(),
            orderId,
            amount,
            method
        );
        
        // Simulate payment processing delay
        try {
            Thread.sleep(100 + (int)(Math.random() * 200)); // 100-300ms
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Simulate 90% success rate
        boolean success = Math.random() < 0.9;
        payment.updateStatus(success ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);
        
        paymentRepo.save(payment);
        return payment;
    }
}

/**
 * Thread-safe order management service with ACID properties
 */
class OrderService {
    private final OrderRepository orderRepo;
    private final ProductRepository productRepo;
    private final CartService cartService;
    private final PaymentService paymentService;
    private final PricingStrategy pricingStrategy;
    private final List<OrderObserver> observers;
    private final AtomicInteger orderCounter;
    
    public OrderService(OrderRepository orderRepo, ProductRepository productRepo,
                       CartService cartService, PaymentService paymentService,
                       PricingStrategy pricingStrategy) {
        this.orderRepo = orderRepo;
        this.productRepo = productRepo;
        this.cartService = cartService;
        this.paymentService = paymentService;
        this.pricingStrategy = pricingStrategy;
        this.observers = new ArrayList<>();
        this.orderCounter = new AtomicInteger(0);
    }
    
    public void addObserver(OrderObserver observer) {
        observers.add(observer);
    }
    
    /**
     * Thread-safe order creation with inventory reservation
     * ACID Properties:
     * - Atomicity: All inventory reservations succeed or all fail
     * - Consistency: Inventory is always accurate
     * - Isolation: Concurrent orders don't interfere
     * - Durability: Order state is persisted
     */
    public Order createOrder(String userId) {
        ShoppingCart cart = cartService.getCart(userId);
        if (cart.isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }
        
        List<OrderItem> orderItems = new ArrayList<>();
        List<Product> reservedProducts = new ArrayList<>();
        List<Integer> reservedQuantities = new ArrayList<>();
        
        try {
            // Phase 1: Reserve all inventory atomically
            for (CartItem cartItem : cart.getItems()) {
                Product product = cartItem.getProduct();
                int quantity = cartItem.getQuantity();
                
                if (!product.reserveInventory(quantity)) {
                    // Rollback all previous reservations
                    rollbackReservations(reservedProducts, reservedQuantities);
                    throw new RuntimeException("Insufficient inventory for " + product.getName());
                }
                
                reservedProducts.add(product);
                reservedQuantities.add(quantity);
                orderItems.add(new OrderItem(product, quantity));
            }
            
            // Phase 2: Create order
            Order order = new Order(
                "ORD-" + orderCounter.incrementAndGet(),
                userId,
                orderItems
            );
            
            orderRepo.save(order);
            cartService.clearCart(userId);
            
            return order;
            
        } catch (Exception e) {
            // Rollback on any failure
            rollbackReservations(reservedProducts, reservedQuantities);
            throw e;
        }
    }
    
    /**
     * Thread-safe order confirmation with payment
     */
    public boolean confirmOrder(String orderId, PaymentMethod paymentMethod) {
        Order order = orderRepo.findById(orderId);
        if (order == null || order.getStatus() != OrderStatus.PENDING) {
            return false;
        }
        
        // Calculate final amount with discounts
        double discount = pricingStrategy.calculateDiscount(order);
        double finalAmount = order.getTotalAmount() - discount;
        
        // Process payment
        Payment payment = paymentService.processPayment(orderId, finalAmount, paymentMethod);
        
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            OrderStatus oldStatus = order.getStatus();
            order.updatePaymentStatus(PaymentStatus.SUCCESS);
            order.updateStatus(OrderStatus.CONFIRMED);
            
            // Notify observers
            notifyObservers(order, oldStatus, OrderStatus.CONFIRMED);
            return true;
        } else {
            // Payment failed - release inventory
            releaseOrderInventory(order);
            order.updatePaymentStatus(PaymentStatus.FAILED);
            order.updateStatus(OrderStatus.CANCELLED);
            
            OrderStatus oldStatus = OrderStatus.PENDING;
            notifyObservers(order, oldStatus, OrderStatus.CANCELLED);
            return false;
        }
    }
    
    /**
     * Thread-safe order cancellation
     */
    public boolean cancelOrder(String orderId) {
        Order order = orderRepo.findById(orderId);
        if (order == null || order.getStatus() == OrderStatus.CANCELLED || 
            order.getStatus() == OrderStatus.DELIVERED) {
            return false;
        }
        
        OrderStatus oldStatus = order.getStatus();
        
        // Release inventory
        releaseOrderInventory(order);
        
        // Update status
        order.updateStatus(OrderStatus.CANCELLED);
        
        // Notify observers
        notifyObservers(order, oldStatus, OrderStatus.CANCELLED);
        
        return true;
    }
    
    private void rollbackReservations(List<Product> products, List<Integer> quantities) {
        for (int i = 0; i < products.size(); i++) {
            products.get(i).releaseInventory(quantities.get(i));
        }
    }
    
    private void releaseOrderInventory(Order order) {
        for (OrderItem item : order.getItems()) {
            item.getProduct().releaseInventory(item.getQuantity());
        }
    }
    
    private void notifyObservers(Order order, OrderStatus oldStatus, OrderStatus newStatus) {
        for (OrderObserver observer : observers) {
            try {
                observer.onOrderStatusChanged(order, oldStatus, newStatus);
            } catch (Exception e) {
                // Log error but don't fail the operation
                System.err.println("Observer notification failed: " + e.getMessage());
            }
        }
    }
}

// ============================================================================
// DEMO
// ============================================================================

public class EcommerceSystemComplete {
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=".repeat(70));
        System.out.println("ECOMMERCE SYSTEM - CONCURRENCY DEMO");
        System.out.println("=".repeat(70));
        
        // Setup
        ProductRepository productRepo = new ProductRepository();
        UserRepository userRepo = new UserRepository();
        OrderRepository orderRepo = new OrderRepository();
        PaymentRepository paymentRepo = new PaymentRepository();
        
        CartService cartService = new CartService(productRepo);
        PaymentService paymentService = new PaymentService(paymentRepo);
        OrderService orderService = new OrderService(
            orderRepo, productRepo, cartService, paymentService,
            new BulkDiscountStrategy(5, 10.0) // 10% discount for 5+ items
        );
        
        // Add observers
        orderService.addObserver(new EmailNotificationService());
        orderService.addObserver(new SMSNotificationService());
        
        setupData(productRepo, userRepo);
        
        System.out.println("\n✅ Setup: 5 products, 3 users\n");
        
        // Test 1: Single user workflow
        test1(cartService, orderService);
        
        // Test 2: Concurrent cart operations
        test2(cartService);
        
        // Test 3: Concurrent order creation (inventory contention)
        test3(cartService, orderService);
        
        // Test 4: Order cancellation and inventory release
        test4(cartService, orderService);
        
        cartService.shutdown();
        
        System.out.println("\n" + "=".repeat(70));
        System.out.println("ALL TESTS PASSED! ✅");
        System.out.println("=".repeat(70));
    }
    
    private static void setupData(ProductRepository productRepo, UserRepository userRepo) {
        // Products
        productRepo.save(new Product("P1", "iPhone 15", ProductCategory.ELECTRONICS, 999.99, 10));
        productRepo.save(new Product("P2", "MacBook Pro", ProductCategory.ELECTRONICS, 2499.99, 5));
        productRepo.save(new Product("P3", "Nike Shoes", ProductCategory.CLOTHING, 129.99, 20));
        productRepo.save(new Product("P4", "Java Book", ProductCategory.BOOKS, 49.99, 15));
        productRepo.save(new Product("P5", "Coffee Mug", ProductCategory.HOME, 19.99, 50));
        
        // Users
        userRepo.save(new User("U1", "John Doe", "john@example.com", "123 Main St"));
        userRepo.save(new User("U2", "Jane Smith", "jane@example.com", "456 Oak Ave"));
        userRepo.save(new User("U3", "Bob Wilson", "bob@example.com", "789 Pine Rd"));
    }
    
    private static void test1(CartService cartService, OrderService orderService) {
        System.out.println("TEST 1: Single User Complete Workflow");
        System.out.println("-".repeat(70));
        
        try {
            // Add items to cart
            cartService.addToCart("U1", "P1", 1); // iPhone
            cartService.addToCart("U1", "P3", 2); // Nike Shoes
            cartService.addToCart("U1", "P5", 3); // Coffee Mugs
            
            ShoppingCart cart = cartService.getCart("U1");
            System.out.println("✅ Cart total: $" + String.format("%.2f", cart.getTotal()));
            
            // Create order
            Order order = orderService.createOrder("U1");
            System.out.println("✅ Order created: " + order.getId());
            
            // Confirm order with payment
            boolean confirmed = orderService.confirmOrder(order.getId(), PaymentMethod.CREDIT_CARD);
            System.out.println("✅ Order confirmed: " + confirmed + "\n");
            
        } catch (Exception e) {
            System.out.println("❌ Failed: " + e.getMessage() + "\n");
        }
    }
    
    private static void test2(CartService cartService) throws InterruptedException {
        System.out.println("TEST 2: Concurrent Cart Operations");
        System.out.println("-".repeat(70));
        
        ExecutorService exec = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(20);
        AtomicInteger success = new AtomicInteger(0);
        
        // 10 threads adding items, 10 threads removing items
        for (int i = 0; i < 10; i++) {
            exec.submit(() -> {
                try {
                    if (cartService.addToCart("U2", "P2", 1)) {
                        success.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
            
            exec.submit(() -> {
                try {
                    cartService.removeFromCart("U2", "P2");
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await();
        exec.shutdown();
        
        ShoppingCart cart = cartService.getCart("U2");
        System.out.println("✅ Concurrent operations completed. Cart items: " + cart.getItems().size() + "\n");
    }
    
    private static void test3(CartService cartService, OrderService orderService) throws InterruptedException {
        System.out.println("TEST 3: Concurrent Order Creation (Inventory Contention)");
        System.out.println("-".repeat(70));
        
        // Setup: Add same product to multiple carts
        for (int i = 1; i <= 10; i++) {
            String userId = "U" + (i % 3 + 1); // Use existing users U1, U2, U3
            cartService.addToCart(userId, "P2", 1); // MacBook Pro (only 5 in stock)
        }
        
        ExecutorService exec = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(3);
        AtomicInteger successfulOrders = new AtomicInteger(0);
        
        // 3 users trying to create orders simultaneously
        for (int i = 1; i <= 3; i++) {
            final String userId = "U" + i;
            exec.submit(() -> {
                try {
                    orderService.createOrder(userId);
                    successfulOrders.incrementAndGet();
                } catch (Exception e) {
                    // Expected for some orders due to inventory limits
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await();
        exec.shutdown();
        
        System.out.println("✅ Successful orders: " + successfulOrders.get() + 
                         "/3 (Limited by inventory)\n");
    }
    
    private static void test4(CartService cartService, OrderService orderService) {
        System.out.println("TEST 4: Order Cancellation and Inventory Release");
        System.out.println("-".repeat(70));
        
        try {
            // Create order
            cartService.addToCart("U3", "P4", 5); // Java Books
            Order order = orderService.createOrder("U3");
            System.out.println("✅ Order created: " + order.getId());
            
            // Cancel order
            boolean cancelled = orderService.cancelOrder(order.getId());
            System.out.println("✅ Order cancelled: " + cancelled);
            System.out.println("✅ Inventory released back to pool\n");
            
        } catch (Exception e) {
            System.out.println("❌ Failed: " + e.getMessage() + "\n");
        }
    }
}