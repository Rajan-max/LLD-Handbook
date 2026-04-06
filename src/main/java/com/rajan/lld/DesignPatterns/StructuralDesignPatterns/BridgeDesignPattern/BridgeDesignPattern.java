package com.rajan.lld.DesignPatterns.StructuralDesignPatterns.BridgeDesignPattern;

/**
 * BRIDGE DESIGN PATTERN - Complete Example
 * 
 * Definition: Decouples an abstraction from its implementation so that the two can vary independently.
 * 
 * In simple terms:
 * - Separate "what" (abstraction) from "how" (implementation)
 * - Two hierarchies that can evolve independently
 * - Composition over inheritance
 * - Like a remote control (abstraction) that works with any device (implementation)
 * 
 * When to use:
 * - Want to avoid permanent binding between abstraction and implementation
 * - Both abstraction and implementation should be extensible
 * - Changes in implementation shouldn't affect clients
 * - Want to share implementation among multiple objects
 * - Avoid class explosion from multiple dimensions
 */

// ============================================================================
// PROBLEM - Without Bridge Pattern
// ============================================================================

/**
 * PROBLEM: Class explosion with multiple dimensions of variation
 * 
 * Why is this bad?
 * - Cartesian product of classes (m abstractions × n implementations = m×n classes)
 * - Tight coupling between abstraction and implementation
 * - Hard to add new abstractions or implementations
 * - Code duplication across similar classes
 * - Violates Single Responsibility Principle
 */

// Without Bridge: Need separate class for each combination!
class RedCircle { }
class BlueCircle { }
class RedSquare { }
class BlueSquare { }
// Adding new color or shape = many new classes!
// 3 shapes × 4 colors = 12 classes!


// ============================================================================
// SOLUTION - Bridge Pattern
// ============================================================================

/**
 * EXAMPLE 1: Remote Control and Devices
 * Abstraction: Remote Control
 * Implementation: Device (TV, Radio)
 */

// Step 1: Implementation Interface
interface Device {
    void turnOn();
    void turnOff();
    void setVolume(int volume);
    int getVolume();
}

// Step 2: Concrete Implementations
class TV implements Device {
    private boolean on = false;
    private int volume = 30;
    
    @Override
    public void turnOn() {
        on = true;
        System.out.println("   📺 TV: Turned ON");
    }
    
    @Override
    public void turnOff() {
        on = false;
        System.out.println("   📺 TV: Turned OFF");
    }
    
    @Override
    public void setVolume(int volume) {
        this.volume = volume;
        System.out.println("   📺 TV: Volume set to " + volume);
    }
    
    @Override
    public int getVolume() {
        return volume;
    }
}

class Radio implements Device {
    private boolean on = false;
    private int volume = 20;
    
    @Override
    public void turnOn() {
        on = true;
        System.out.println("   📻 Radio: Turned ON");
    }
    
    @Override
    public void turnOff() {
        on = false;
        System.out.println("   📻 Radio: Turned OFF");
    }
    
    @Override
    public void setVolume(int volume) {
        this.volume = volume;
        System.out.println("   📻 Radio: Volume set to " + volume);
    }
    
    @Override
    public int getVolume() {
        return volume;
    }
}

// Step 3: Abstraction (uses implementation via composition)
class RemoteControl {
    protected Device device;
    
    public RemoteControl(Device device) {
        this.device = device;
    }
    
    public void togglePower() {
        System.out.println("🎮 Remote: Toggle power");
        device.turnOn();
    }
    
    public void volumeUp() {
        System.out.println("🎮 Remote: Volume up");
        device.setVolume(device.getVolume() + 10);
    }
    
    public void volumeDown() {
        System.out.println("🎮 Remote: Volume down");
        device.setVolume(device.getVolume() - 10);
    }
}

// Step 4: Refined Abstraction (extends abstraction)
class AdvancedRemoteControl extends RemoteControl {
    public AdvancedRemoteControl(Device device) {
        super(device);
    }
    
    public void mute() {
        System.out.println("🎮 Advanced Remote: Mute");
        device.setVolume(0);
    }
}


// ============================================================================
// REAL-WORLD EXAMPLE 1 - Notification System
// ============================================================================

/**
 * Abstraction: Notification
 * Implementation: Sender (Email, SMS, Push)
 */

interface MessageSender {
    void send(String message);
}

class EmailSender implements MessageSender {
    @Override
    public void send(String message) {
        System.out.println("   📧 Email: " + message);
    }
}

class SMSSender implements MessageSender {
    @Override
    public void send(String message) {
        System.out.println("   📱 SMS: " + message);
    }
}

class PushNotificationSender implements MessageSender {
    @Override
    public void send(String message) {
        System.out.println("   🔔 Push: " + message);
    }
}

abstract class Notification {
    protected MessageSender sender;
    
    public Notification(MessageSender sender) {
        this.sender = sender;
    }
    
    public abstract void notify(String message);
}

class AlertNotification extends Notification {
    public AlertNotification(MessageSender sender) {
        super(sender);
    }
    
    @Override
    public void notify(String message) {
        System.out.println("🚨 ALERT:");
        sender.send("⚠️  " + message);
    }
}

class InfoNotification extends Notification {
    public InfoNotification(MessageSender sender) {
        super(sender);
    }
    
    @Override
    public void notify(String message) {
        System.out.println("ℹ️  INFO:");
        sender.send("ℹ️  " + message);
    }
}


// ============================================================================
// REAL-WORLD EXAMPLE 2 - Database and Platform
// ============================================================================

/**
 * Abstraction: Database
 * Implementation: Platform (MySQL, PostgreSQL, MongoDB)
 */

interface DatabaseDriver {
    void connect(String url);
    void executeQuery(String query);
    void disconnect();
}

class MySQLDriver implements DatabaseDriver {
    @Override
    public void connect(String url) {
        System.out.println("   🐬 MySQL: Connected to " + url);
    }
    
    @Override
    public void executeQuery(String query) {
        System.out.println("   🐬 MySQL: Executing " + query);
    }
    
    @Override
    public void disconnect() {
        System.out.println("   🐬 MySQL: Disconnected");
    }
}

class PostgreSQLDriver implements DatabaseDriver {
    @Override
    public void connect(String url) {
        System.out.println("   🐘 PostgreSQL: Connected to " + url);
    }
    
    @Override
    public void executeQuery(String query) {
        System.out.println("   🐘 PostgreSQL: Executing " + query);
    }
    
    @Override
    public void disconnect() {
        System.out.println("   🐘 PostgreSQL: Disconnected");
    }
}

abstract class Database {
    protected DatabaseDriver driver;
    
    public Database(DatabaseDriver driver) {
        this.driver = driver;
    }
    
    public abstract void query(String sql);
}

class ProductionDatabase extends Database {
    public ProductionDatabase(DatabaseDriver driver) {
        super(driver);
    }
    
    @Override
    public void query(String sql) {
        System.out.println("🏭 Production DB:");
        driver.connect("prod-server:5432");
        driver.executeQuery(sql);
        driver.disconnect();
    }
}

class TestDatabase extends Database {
    public TestDatabase(DatabaseDriver driver) {
        super(driver);
    }
    
    @Override
    public void query(String sql) {
        System.out.println("🧪 Test DB:");
        driver.connect("test-server:5432");
        driver.executeQuery(sql);
        driver.disconnect();
    }
}


// ============================================================================
// REAL-WORLD EXAMPLE 3 - Shape and Color
// ============================================================================

/**
 * Abstraction: Shape
 * Implementation: Color
 */

interface Color {
    String fill();
}

class Red implements Color {
    @Override
    public String fill() {
        return "Red";
    }
}

class Blue implements Color {
    @Override
    public String fill() {
        return "Blue";
    }
}

class Green implements Color {
    @Override
    public String fill() {
        return "Green";
    }
}

abstract class Shape {
    protected Color color;
    
    public Shape(Color color) {
        this.color = color;
    }
    
    public abstract void draw();
}

class Circle extends Shape {
    public Circle(Color color) {
        super(color);
    }
    
    @Override
    public void draw() {
        System.out.println("   ⭕ Drawing Circle in " + color.fill() + " color");
    }
}

class Square extends Shape {
    public Square(Color color) {
        super(color);
    }
    
    @Override
    public void draw() {
        System.out.println("   ⬜ Drawing Square in " + color.fill() + " color");
    }
}

class Triangle extends Shape {
    public Triangle(Color color) {
        super(color);
    }
    
    @Override
    public void draw() {
        System.out.println("   🔺 Drawing Triangle in " + color.fill() + " color");
    }
}


// ============================================================================
// DEMO - All Bridge Examples
// ============================================================================

public class BridgeDesignPattern {
    public static void main(String[] args) {
        System.out.println("=".repeat(70));
        System.out.println("BRIDGE DESIGN PATTERN DEMO");
        System.out.println("=".repeat(70));
        
        // PROBLEM Demo
        System.out.println("\n❌ PROBLEM: Without Bridge Pattern (Class Explosion)");
        System.out.println("-".repeat(70));
        System.out.println("Need separate class for each combination:");
        System.out.println("  RedCircle, BlueCircle, GreenCircle");
        System.out.println("  RedSquare, BlueSquare, GreenSquare");
        System.out.println("  RedTriangle, BlueTriangle, GreenTriangle");
        System.out.println("\n⚠️  3 shapes × 3 colors = 9 classes!");
        System.out.println("⚠️  Adding new shape or color = many new classes!");
        
        // SOLUTION Demo
        System.out.println("\n✅ SOLUTION: With Bridge Pattern");
        System.out.println("-".repeat(70));
        
        // 1. Remote Control and Devices
        System.out.println("\n1️⃣  REMOTE CONTROL & DEVICES");
        System.out.println("-".repeat(70));
        
        Device tv = new TV();
        Device radio = new Radio();
        
        System.out.println("Basic Remote with TV:");
        RemoteControl basicRemote = new RemoteControl(tv);
        basicRemote.togglePower();
        basicRemote.volumeUp();
        basicRemote.volumeDown();
        
        System.out.println("\nAdvanced Remote with Radio:");
        AdvancedRemoteControl advancedRemote = new AdvancedRemoteControl(radio);
        advancedRemote.togglePower();
        advancedRemote.volumeUp();
        advancedRemote.mute();
        
        System.out.println("\n💡 Same remote works with different devices!");
        
        // 2. Notification System
        System.out.println("\n\n2️⃣  NOTIFICATION SYSTEM");
        System.out.println("-".repeat(70));
        
        MessageSender emailSender = new EmailSender();
        MessageSender smsSender = new SMSSender();
        MessageSender pushSender = new PushNotificationSender();
        
        System.out.println("Alert via Email:");
        Notification alertEmail = new AlertNotification(emailSender);
        alertEmail.notify("Server is down!");
        
        System.out.println("\nAlert via SMS:");
        Notification alertSMS = new AlertNotification(smsSender);
        alertSMS.notify("Server is down!");
        
        System.out.println("\nInfo via Push:");
        Notification infoPush = new InfoNotification(pushSender);
        infoPush.notify("Update available");
        
        System.out.println("\n💡 Same notification type works with different senders!");
        
        // 3. Database and Platform
        System.out.println("\n\n3️⃣  DATABASE & PLATFORM");
        System.out.println("-".repeat(70));
        
        DatabaseDriver mysqlDriver = new MySQLDriver();
        DatabaseDriver postgresDriver = new PostgreSQLDriver();
        
        System.out.println("Production DB with MySQL:");
        Database prodMySQL = new ProductionDatabase(mysqlDriver);
        prodMySQL.query("SELECT * FROM users");
        
        System.out.println("\nTest DB with PostgreSQL:");
        Database testPostgres = new TestDatabase(postgresDriver);
        testPostgres.query("SELECT * FROM test_users");
        
        System.out.println("\n💡 Same database type works with different drivers!");
        
        // 4. Shape and Color
        System.out.println("\n\n4️⃣  SHAPE & COLOR");
        System.out.println("-".repeat(70));
        
        Color red = new Red();
        Color blue = new Blue();
        Color green = new Green();
        
        System.out.println("Drawing shapes in different colors:");
        Shape redCircle = new Circle(red);
        Shape blueSquare = new Square(blue);
        Shape greenTriangle = new Triangle(green);
        Shape blueCircle = new Circle(blue);
        
        redCircle.draw();
        blueSquare.draw();
        greenTriangle.draw();
        blueCircle.draw();
        
        System.out.println("\n💡 3 shapes + 3 colors = only 6 classes (not 9)!");
        System.out.println("💡 Adding new shape or color = just 1 new class!");
        
        // Summary
        System.out.println("\n\n" + "=".repeat(70));
        System.out.println("KEY BENEFITS OF BRIDGE PATTERN");
        System.out.println("=".repeat(70));
        System.out.println("✅ Decoupling: Abstraction and implementation vary independently");
        System.out.println("✅ Avoid Class Explosion: m + n classes instead of m × n");
        System.out.println("✅ Flexibility: Easy to add new abstractions or implementations");
        System.out.println("✅ Runtime Binding: Can switch implementation at runtime");
        System.out.println("✅ Single Responsibility: Separate concerns clearly");
        System.out.println("✅ Open/Closed: Extend without modifying existing code");
        System.out.println("=".repeat(70));
        
        System.out.println("\n📊 Class Count Comparison:");
        System.out.println("   Without Bridge: 3 shapes × 3 colors = 9 classes");
        System.out.println("   With Bridge: 3 shapes + 3 colors = 6 classes");
        System.out.println("   Savings: 33% fewer classes!");
    }
}
