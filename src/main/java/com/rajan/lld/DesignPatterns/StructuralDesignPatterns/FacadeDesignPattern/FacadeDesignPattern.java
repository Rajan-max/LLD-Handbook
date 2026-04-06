package com.rajan.lld.DesignPatterns.StructuralDesignPatterns.FacadeDesignPattern;

/**
 * FACADE DESIGN PATTERN - Complete Example
 * 
 * Definition: Provides a unified, simplified interface to a complex subsystem.
 * 
 * In simple terms:
 * - Simplifies complex systems with a simple interface
 * - Hides complexity from clients
 * - One simple interface for many complex operations
 * - Like a hotel receptionist who handles all services for you
 * 
 * When to use:
 * - Complex subsystem with many classes
 * - Want to provide simple interface to complex system
 * - Decouple client from subsystem
 * - Layer your application
 * - Reduce dependencies between systems
 */

// ============================================================================
// PROBLEM - Without Facade Pattern
// ============================================================================

/**
 * PROBLEM: Client must interact with many complex subsystem classes
 * 
 * Why is this bad?
 * - Client needs to know all subsystem classes
 * - Complex initialization and coordination
 * - Tight coupling between client and subsystem
 * - Code duplication (same complex steps everywhere)
 * - Hard to maintain and modify
 * - Violates Law of Demeter (don't talk to strangers)
 */

// Complex subsystem classes
class ComplexAmplifier {
    public void on() { System.out.println("Amplifier on"); }
    public void setVolume(int level) { System.out.println("Volume: " + level); }
    public void setSurroundSound() { System.out.println("Surround sound on"); }
}

class ComplexDVDPlayer {
    public void on() { System.out.println("DVD Player on"); }
    public void play(String movie) { System.out.println("Playing: " + movie); }
}

class ComplexProjector {
    public void on() { System.out.println("Projector on"); }
    public void wideScreenMode() { System.out.println("Widescreen mode"); }
}

// Without Facade: Client must know and coordinate all classes!
// amplifier.on();
// amplifier.setVolume(5);
// amplifier.setSurroundSound();
// dvdPlayer.on();
// projector.on();
// projector.wideScreenMode();
// dvdPlayer.play("Inception");
// ... Complex and error-prone!


// ============================================================================
// SOLUTION - Facade Pattern
// ============================================================================

/**
 * EXAMPLE 1: Home Theater System
 * Complex subsystem with many components
 */

// Subsystem classes
class Amplifier {
    public void on() {
        System.out.println("   🔊 Amplifier: Turning on");
    }
    public void off() {
        System.out.println("   🔊 Amplifier: Turning off");
    }
    public void setVolume(int level) {
        System.out.println("   🔊 Amplifier: Setting volume to " + level);
    }
    public void setSurroundSound() {
        System.out.println("   🔊 Amplifier: Setting surround sound mode");
    }
}

class DVDPlayer {
    public void on() {
        System.out.println("   📀 DVD Player: Turning on");
    }
    public void off() {
        System.out.println("   📀 DVD Player: Turning off");
    }
    public void play(String movie) {
        System.out.println("   📀 DVD Player: Playing '" + movie + "'");
    }
    public void stop() {
        System.out.println("   📀 DVD Player: Stopping");
    }
    public void eject() {
        System.out.println("   📀 DVD Player: Ejecting disc");
    }
}

class Projector {
    public void on() {
        System.out.println("   📽️  Projector: Turning on");
    }
    public void off() {
        System.out.println("   📽️  Projector: Turning off");
    }
    public void wideScreenMode() {
        System.out.println("   📽️  Projector: Setting widescreen mode");
    }
}

class Lights {
    public void dim(int level) {
        System.out.println("   💡 Lights: Dimming to " + level + "%");
    }
    public void on() {
        System.out.println("   💡 Lights: Turning on");
    }
}

class Screen {
    public void down() {
        System.out.println("   🎬 Screen: Lowering screen");
    }
    public void up() {
        System.out.println("   🎬 Screen: Raising screen");
    }
}

/**
 * Facade: Simplifies the complex subsystem
 */
class HomeTheaterFacade {
    private Amplifier amp;
    private DVDPlayer dvd;
    private Projector projector;
    private Lights lights;
    private Screen screen;
    
    public HomeTheaterFacade(Amplifier amp, DVDPlayer dvd, Projector projector, 
                             Lights lights, Screen screen) {
        this.amp = amp;
        this.dvd = dvd;
        this.projector = projector;
        this.lights = lights;
        this.screen = screen;
    }
    
    // Simple interface for complex operation
    public void watchMovie(String movie) {
        System.out.println("\n🎬 Get ready to watch a movie...\n");
        lights.dim(10);
        screen.down();
        projector.on();
        projector.wideScreenMode();
        amp.on();
        amp.setVolume(5);
        amp.setSurroundSound();
        dvd.on();
        dvd.play(movie);
        System.out.println("\n✅ Movie started! Enjoy! 🍿\n");
    }
    
    public void endMovie() {
        System.out.println("\n🛑 Shutting down movie theater...\n");
        dvd.stop();
        dvd.eject();
        dvd.off();
        amp.off();
        projector.off();
        screen.up();
        lights.on();
        System.out.println("\n✅ Theater shut down!\n");
    }
}


// ============================================================================
// REAL-WORLD EXAMPLE 1 - Computer Startup System
// ============================================================================

/**
 * Complex computer boot process simplified
 */

class CPU {
    public void freeze() {
        System.out.println("   🖥️  CPU: Freezing");
    }
    public void jump(long position) {
        System.out.println("   🖥️  CPU: Jumping to position " + position);
    }
    public void execute() {
        System.out.println("   🖥️  CPU: Executing instructions");
    }
}

class Memory {
    public void load(long position, byte[] data) {
        System.out.println("   💾 Memory: Loading data at position " + position);
    }
}

class HardDrive {
    public byte[] read(long lba, int size) {
        System.out.println("   💿 HardDrive: Reading " + size + " bytes from sector " + lba);
        return new byte[size];
    }
}

class BIOS {
    public void initialize() {
        System.out.println("   ⚙️  BIOS: Initializing hardware");
    }
    public void runPowerOnSelfTest() {
        System.out.println("   ⚙️  BIOS: Running POST (Power-On Self-Test)");
    }
}

/**
 * Facade: Simple computer startup
 */
class ComputerFacade {
    private CPU cpu;
    private Memory memory;
    private HardDrive hardDrive;
    private BIOS bios;
    
    public ComputerFacade() {
        this.cpu = new CPU();
        this.memory = new Memory();
        this.hardDrive = new HardDrive();
        this.bios = new BIOS();
    }
    
    public void start() {
        System.out.println("\n💻 Starting computer...\n");
        bios.initialize();
        bios.runPowerOnSelfTest();
        cpu.freeze();
        memory.load(0, hardDrive.read(100, 1024));
        cpu.jump(0);
        cpu.execute();
        System.out.println("\n✅ Computer started successfully!\n");
    }
}


// ============================================================================
// REAL-WORLD EXAMPLE 2 - Order Processing System
// ============================================================================

/**
 * E-commerce order processing with multiple subsystems
 */

class InventoryService {
    public boolean checkStock(String productId, int quantity) {
        System.out.println("   📦 Inventory: Checking stock for " + productId);
        return true; // Simplified
    }
    public void reserveStock(String productId, int quantity) {
        System.out.println("   📦 Inventory: Reserving " + quantity + " units of " + productId);
    }
}

class PaymentService {
    public boolean processPayment(String cardNumber, double amount) {
        System.out.println("   💳 Payment: Processing $" + amount + " on card ending " + 
                         cardNumber.substring(cardNumber.length() - 4));
        return true; // Simplified
    }
}

class ShippingService {
    public String scheduleDelivery(String address) {
        System.out.println("   🚚 Shipping: Scheduling delivery to " + address);
        return "TRACK123456";
    }
}

class NotificationService {
    public void sendOrderConfirmation(String email, String orderId) {
        System.out.println("   📧 Notification: Sending confirmation to " + email);
    }
    public void sendShippingNotification(String email, String trackingId) {
        System.out.println("   📧 Notification: Sending tracking info " + trackingId);
    }
}

class InvoiceService {
    public void generateInvoice(String orderId, double amount) {
        System.out.println("   📄 Invoice: Generating invoice for order " + orderId);
    }
}

/**
 * Facade: Simple order placement
 */
class OrderFacade {
    private InventoryService inventory;
    private PaymentService payment;
    private ShippingService shipping;
    private NotificationService notification;
    private InvoiceService invoice;
    
    public OrderFacade() {
        this.inventory = new InventoryService();
        this.payment = new PaymentService();
        this.shipping = new ShippingService();
        this.notification = new NotificationService();
        this.invoice = new InvoiceService();
    }
    
    public boolean placeOrder(String productId, int quantity, String cardNumber, 
                             double amount, String address, String email) {
        System.out.println("\n🛒 Processing order...\n");
        
        // Check inventory
        if (!inventory.checkStock(productId, quantity)) {
            System.out.println("❌ Out of stock!");
            return false;
        }
        
        // Process payment
        if (!payment.processPayment(cardNumber, amount)) {
            System.out.println("❌ Payment failed!");
            return false;
        }
        
        // Reserve stock
        inventory.reserveStock(productId, quantity);
        
        // Schedule shipping
        String trackingId = shipping.scheduleDelivery(address);
        
        // Generate invoice
        String orderId = "ORD" + System.currentTimeMillis();
        invoice.generateInvoice(orderId, amount);
        
        // Send notifications
        notification.sendOrderConfirmation(email, orderId);
        notification.sendShippingNotification(email, trackingId);
        
        System.out.println("\n✅ Order placed successfully! Order ID: " + orderId + "\n");
        return true;
    }
}


// ============================================================================
// REAL-WORLD EXAMPLE 3 - Hotel Booking System
// ============================================================================

/**
 * Hotel booking with multiple services
 */

class RoomBookingService {
    public boolean checkAvailability(String roomType, String date) {
        System.out.println("   🏨 Room Service: Checking " + roomType + " for " + date);
        return true;
    }
    public String bookRoom(String roomType, String date) {
        System.out.println("   🏨 Room Service: Booking " + roomType);
        return "ROOM-" + System.currentTimeMillis();
    }
}

class RestaurantService {
    public void reserveTable(int guests, String time) {
        System.out.println("   🍽️  Restaurant: Reserving table for " + guests + " at " + time);
    }
}

class SpaService {
    public void bookAppointment(String treatment, String time) {
        System.out.println("   💆 Spa: Booking " + treatment + " at " + time);
    }
}

class TransportService {
    public void arrangePickup(String location, String time) {
        System.out.println("   🚗 Transport: Arranging pickup from " + location + " at " + time);
    }
}

/**
 * Facade: Simple hotel package booking
 */
class HotelBookingFacade {
    private RoomBookingService roomService;
    private RestaurantService restaurant;
    private SpaService spa;
    private TransportService transport;
    
    public HotelBookingFacade() {
        this.roomService = new RoomBookingService();
        this.restaurant = new RestaurantService();
        this.spa = new SpaService();
        this.transport = new TransportService();
    }
    
    public String bookLuxuryPackage(String date, String pickupLocation) {
        System.out.println("\n🏨 Booking Luxury Package...\n");
        
        // Book deluxe room
        String bookingId = roomService.bookRoom("Deluxe Suite", date);
        
        // Reserve dinner table
        restaurant.reserveTable(2, "7:00 PM");
        
        // Book spa treatment
        spa.bookAppointment("Full Body Massage", "3:00 PM");
        
        // Arrange airport pickup
        transport.arrangePickup(pickupLocation, "12:00 PM");
        
        System.out.println("\n✅ Luxury package booked! Booking ID: " + bookingId + "\n");
        return bookingId;
    }
    
    public String bookBudgetPackage(String date) {
        System.out.println("\n🏨 Booking Budget Package...\n");
        
        String bookingId = roomService.bookRoom("Standard Room", date);
        
        System.out.println("\n✅ Budget package booked! Booking ID: " + bookingId + "\n");
        return bookingId;
    }
}


// ============================================================================
// DEMO - All Facade Examples
// ============================================================================

public class FacadeDesignPattern {
    public static void main(String[] args) {
        System.out.println("=".repeat(70));
        System.out.println("FACADE DESIGN PATTERN DEMO");
        System.out.println("=".repeat(70));
        
        // PROBLEM Demo
        System.out.println("\n❌ PROBLEM: Without Facade (Complex Client Code)");
        System.out.println("-".repeat(70));
        System.out.println("Client must know and coordinate all subsystem classes:");
        System.out.println("  amplifier.on()");
        System.out.println("  amplifier.setVolume(5)");
        System.out.println("  amplifier.setSurroundSound()");
        System.out.println("  dvdPlayer.on()");
        System.out.println("  projector.on()");
        System.out.println("  projector.wideScreenMode()");
        System.out.println("  lights.dim(10)");
        System.out.println("  screen.down()");
        System.out.println("  dvdPlayer.play(\"Inception\")");
        System.out.println("\n⚠️  Too complex! Client needs to know everything!");
        
        // SOLUTION Demo
        System.out.println("\n✅ SOLUTION: With Facade Pattern");
        System.out.println("-".repeat(70));
        
        // 1. Home Theater Facade
        System.out.println("\n1️⃣  HOME THEATER FACADE");
        System.out.println("-".repeat(70));
        
        Amplifier amp = new Amplifier();
        DVDPlayer dvd = new DVDPlayer();
        Projector projector = new Projector();
        Lights lights = new Lights();
        Screen screen = new Screen();
        
        HomeTheaterFacade homeTheater = new HomeTheaterFacade(amp, dvd, projector, lights, screen);
        
        // Simple one-line call!
        homeTheater.watchMovie("Inception");
        
        System.out.println("⏸️  ... watching movie ...");
        
        homeTheater.endMovie();
        
        // 2. Computer Startup Facade
        System.out.println("\n2️⃣  COMPUTER STARTUP FACADE");
        System.out.println("-".repeat(70));
        
        ComputerFacade computer = new ComputerFacade();
        computer.start(); // Simple one-line call!
        
        // 3. Order Processing Facade
        System.out.println("\n3️⃣  ORDER PROCESSING FACADE");
        System.out.println("-".repeat(70));
        
        OrderFacade orderSystem = new OrderFacade();
        orderSystem.placeOrder(
            "LAPTOP-123",           // productId
            1,                      // quantity
            "4532-1234-5678-9012", // cardNumber
            999.99,                 // amount
            "123 Main St, NYC",    // address
            "customer@email.com"   // email
        );
        
        // 4. Hotel Booking Facade
        System.out.println("\n4️⃣  HOTEL BOOKING FACADE");
        System.out.println("-".repeat(70));
        
        HotelBookingFacade hotel = new HotelBookingFacade();
        
        System.out.println("Booking luxury package:");
        hotel.bookLuxuryPackage("2024-12-25", "JFK Airport");
        
        System.out.println("Booking budget package:");
        hotel.bookBudgetPackage("2024-12-26");
        
        // Summary
        System.out.println("\n" + "=".repeat(70));
        System.out.println("KEY BENEFITS OF FACADE PATTERN");
        System.out.println("=".repeat(70));
        System.out.println("✅ Simplicity: One simple interface for complex subsystem");
        System.out.println("✅ Decoupling: Client doesn't depend on subsystem classes");
        System.out.println("✅ Flexibility: Can change subsystem without affecting client");
        System.out.println("✅ Layering: Provides clear separation of concerns");
        System.out.println("✅ Ease of Use: Reduces learning curve for complex systems");
        System.out.println("✅ Maintainability: Changes isolated to facade");
        System.out.println("=".repeat(70));
        
        System.out.println("\n💡 Remember: Facade simplifies, doesn't hide!");
        System.out.println("   Clients can still access subsystem directly if needed.");
    }
}
