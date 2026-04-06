package com.rajan.lld.DesignPatterns.StructuralDesignPatterns.AdapterDesignPattern;

/**
 * ADAPTER DESIGN PATTERN - Complete Example
 * 
 * Definition: Converts the interface of a class into another interface clients expect. 
 * Adapter lets classes work together that couldn't otherwise because of incompatible interfaces.
 * 
 * In simple terms:
 * - Make incompatible interfaces work together
 * - Acts as a bridge between two incompatible interfaces
 * - Like a power adapter for different plug types
 * 
 * When to use:
 * - Want to use existing class with incompatible interface
 * - Need to integrate third-party libraries
 * - Want to create reusable class with unrelated classes
 * - Legacy code integration
 */

// ============================================================================
// PROBLEM - Without Adapter Pattern
// ============================================================================

/**
 * PROBLEM: Incompatible interfaces prevent integration
 * 
 * Why is this bad?
 * - Can't use existing code with different interface
 * - Must modify existing classes (violates Open/Closed)
 * - Code duplication to handle different interfaces
 * - Hard to integrate third-party libraries
 * - Tight coupling to specific implementations
 */

// Our application expects this interface
interface MediaPlayer {
    void play(String filename);
    void stop();
}

// Third-party library with incompatible interface
class LegacyMP3Player {
    public void playMP3(String file) {
        System.out.println("Playing MP3: " + file);
    }
    public void stopMP3() {
        System.out.println("Stopped MP3");
    }
}

// Without adapter: Can't use LegacyMP3Player directly!
// Must either:
// 1. Modify LegacyMP3Player (not possible if third-party)
// 2. Duplicate code everywhere
// 3. Create tight coupling


// ============================================================================
// SOLUTION - Adapter Pattern
// ============================================================================

/**
 * Step 1: Target Interface (what client expects)
 */
interface AudioPlayer {
    void play(String audioType, String filename);
    void stop();
}

/**
 * Step 2: Adaptee Classes (incompatible interfaces)
 */
class AdvancedMP3Player {
    public void playMP3File(String filename) {
        System.out.println("🎵 Playing MP3: " + filename);
    }
    public void stopMP3File() {
        System.out.println("⏹️  Stopped MP3");
    }
}

class AdvancedWAVPlayer {
    public void playWAVFile(String filename) {
        System.out.println("🎵 Playing WAV: " + filename);
    }
    public void stopWAVFile() {
        System.out.println("⏹️  Stopped WAV");
    }
}

class AdvancedFLACPlayer {
    public void playFLACFile(String filename) {
        System.out.println("🎵 Playing FLAC: " + filename);
    }
    public void stopFLACFile() {
        System.out.println("⏹️  Stopped FLAC");
    }
}

/**
 * Step 3: Adapter (makes adaptees compatible with target interface)
 */
class MediaAdapter implements AudioPlayer {
    private AdvancedMP3Player mp3Player;
    private AdvancedWAVPlayer wavPlayer;
    private AdvancedFLACPlayer flacPlayer;
    
    public MediaAdapter(String audioType) {
        if (audioType.equalsIgnoreCase("mp3")) {
            mp3Player = new AdvancedMP3Player();
        } else if (audioType.equalsIgnoreCase("wav")) {
            wavPlayer = new AdvancedWAVPlayer();
        } else if (audioType.equalsIgnoreCase("flac")) {
            flacPlayer = new AdvancedFLACPlayer();
        }
    }
    
    @Override
    public void play(String audioType, String filename) {
        if (audioType.equalsIgnoreCase("mp3")) {
            mp3Player.playMP3File(filename);
        } else if (audioType.equalsIgnoreCase("wav")) {
            wavPlayer.playWAVFile(filename);
        } else if (audioType.equalsIgnoreCase("flac")) {
            flacPlayer.playFLACFile(filename);
        }
    }
    
    @Override
    public void stop() {
        if (mp3Player != null) mp3Player.stopMP3File();
        if (wavPlayer != null) wavPlayer.stopWAVFile();
        if (flacPlayer != null) flacPlayer.stopFLACFile();
    }
}

/**
 * Step 4: Client using adapter
 */
class UniversalAudioPlayer implements AudioPlayer {
    private MediaAdapter mediaAdapter;
    
    @Override
    public void play(String audioType, String filename) {
        // Built-in support for basic format
        if (audioType.equalsIgnoreCase("mp4")) {
            System.out.println("🎵 Playing MP4: " + filename);
        }
        // Use adapter for advanced formats
        else if (audioType.equalsIgnoreCase("mp3") || 
                 audioType.equalsIgnoreCase("wav") || 
                 audioType.equalsIgnoreCase("flac")) {
            mediaAdapter = new MediaAdapter(audioType);
            mediaAdapter.play(audioType, filename);
        } else {
            System.out.println("❌ Invalid format: " + audioType);
        }
    }
    
    @Override
    public void stop() {
        if (mediaAdapter != null) {
            mediaAdapter.stop();
        }
    }
}


// ============================================================================
// REAL-WORLD EXAMPLE 1 - Payment Gateway Integration
// ============================================================================

// Target interface our application uses
interface PaymentProcessor {
    void processPayment(double amount);
    boolean refund(String transactionId, double amount);
}

// Third-party PayPal API (incompatible)
class PayPalAPI {
    public void sendPayment(double amount) {
        System.out.println("   💳 PayPal: Processing $" + amount);
    }
    public boolean makeRefund(String txId, double amount) {
        System.out.println("   💰 PayPal: Refunding $" + amount);
        return true;
    }
}

// Third-party Stripe API (incompatible)
class StripeAPI {
    public void charge(double amount) {
        System.out.println("   💳 Stripe: Charging $" + amount);
    }
    public boolean refundCharge(String chargeId, double amount) {
        System.out.println("   💰 Stripe: Refunding $" + amount);
        return true;
    }
}

// Adapters
class PayPalAdapter implements PaymentProcessor {
    private PayPalAPI paypal;
    
    public PayPalAdapter(PayPalAPI paypal) {
        this.paypal = paypal;
    }
    
    @Override
    public void processPayment(double amount) {
        paypal.sendPayment(amount);
    }
    
    @Override
    public boolean refund(String transactionId, double amount) {
        return paypal.makeRefund(transactionId, amount);
    }
}

class StripeAdapter implements PaymentProcessor {
    private StripeAPI stripe;
    
    public StripeAdapter(StripeAPI stripe) {
        this.stripe = stripe;
    }
    
    @Override
    public void processPayment(double amount) {
        stripe.charge(amount);
    }
    
    @Override
    public boolean refund(String transactionId, double amount) {
        return stripe.refundCharge(transactionId, amount);
    }
}


// ============================================================================
// REAL-WORLD EXAMPLE 2 - Database Drivers
// ============================================================================

// Target interface
interface Database {
    void connect(String url);
    void executeQuery(String query);
    void disconnect();
}

// Legacy MySQL driver (incompatible)
class LegacyMySQLDriver {
    public void openConnection(String connectionString) {
        System.out.println("   🔌 MySQL: Connected to " + connectionString);
    }
    public void runQuery(String sql) {
        System.out.println("   📊 MySQL: Executing " + sql);
    }
    public void closeConnection() {
        System.out.println("   🔌 MySQL: Disconnected");
    }
}

// Modern PostgreSQL driver (incompatible)
class ModernPostgreSQLDriver {
    public void establishConnection(String url) {
        System.out.println("   🔌 PostgreSQL: Connected to " + url);
    }
    public void execute(String query) {
        System.out.println("   📊 PostgreSQL: Executing " + query);
    }
    public void terminate() {
        System.out.println("   🔌 PostgreSQL: Disconnected");
    }
}

// Adapters
class MySQLAdapter implements Database {
    private LegacyMySQLDriver driver;
    
    public MySQLAdapter(LegacyMySQLDriver driver) {
        this.driver = driver;
    }
    
    @Override
    public void connect(String url) {
        driver.openConnection(url);
    }
    
    @Override
    public void executeQuery(String query) {
        driver.runQuery(query);
    }
    
    @Override
    public void disconnect() {
        driver.closeConnection();
    }
}

class PostgreSQLAdapter implements Database {
    private ModernPostgreSQLDriver driver;
    
    public PostgreSQLAdapter(ModernPostgreSQLDriver driver) {
        this.driver = driver;
    }
    
    @Override
    public void connect(String url) {
        driver.establishConnection(url);
    }
    
    @Override
    public void executeQuery(String query) {
        driver.execute(query);
    }
    
    @Override
    public void disconnect() {
        driver.terminate();
    }
}


// ============================================================================
// REAL-WORLD EXAMPLE 3 - Temperature Sensor
// ============================================================================

// Target interface (Celsius)
interface TemperatureSensor {
    double getTemperature(); // Returns Celsius
}

// Adaptee (Fahrenheit sensor)
class FahrenheitSensor {
    public double getTemperatureInFahrenheit() {
        return 98.6; // Body temperature in Fahrenheit
    }
}

// Adapter
class TemperatureAdapter implements TemperatureSensor {
    private FahrenheitSensor fahrenheitSensor;
    
    public TemperatureAdapter(FahrenheitSensor sensor) {
        this.fahrenheitSensor = sensor;
    }
    
    @Override
    public double getTemperature() {
        double fahrenheit = fahrenheitSensor.getTemperatureInFahrenheit();
        // Convert Fahrenheit to Celsius
        return (fahrenheit - 32) * 5 / 9;
    }
}


// ============================================================================
// DEMO
// ============================================================================

public class AdapterDesignPattern {
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║         ADAPTER DESIGN PATTERN - DEMONSTRATION            ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        
        // PROBLEM: Without Adapter
        System.out.println("\n❌ PROBLEM: Without Adapter Pattern");
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("Third-party libraries have incompatible interfaces:");
        System.out.println("- LegacyMP3Player.playMP3() vs AudioPlayer.play()");
        System.out.println("- PayPalAPI.sendPayment() vs PaymentProcessor.processPayment()");
        System.out.println("- Can't use them directly!");
        System.out.println("\n⚠️  Issues: Incompatible interfaces, can't integrate, tight coupling");
        
        // SOLUTION: With Adapter
        System.out.println("\n\n✅ SOLUTION: With Adapter Pattern");
        System.out.println("═══════════════════════════════════════════════════════════");
        
        AudioPlayer player = new UniversalAudioPlayer();
        
        System.out.println("\n1️⃣  Playing MP4 (built-in):");
        player.play("mp4", "song.mp4");
        
        System.out.println("\n2️⃣  Playing MP3 (via adapter):");
        player.play("mp3", "song.mp3");
        
        System.out.println("\n3️⃣  Playing WAV (via adapter):");
        player.play("wav", "music.wav");
        
        System.out.println("\n4️⃣  Playing FLAC (via adapter):");
        player.play("flac", "audio.flac");
        
        System.out.println("\n5️⃣  Invalid format:");
        player.play("avi", "video.avi");
        
        // EXAMPLE 1: Payment Gateway
        System.out.println("\n\n💳 EXAMPLE 1: Payment Gateway Integration");
        System.out.println("═══════════════════════════════════════════════════════════");
        
        System.out.println("\nPayPal Payment:");
        PaymentProcessor paypal = new PayPalAdapter(new PayPalAPI());
        paypal.processPayment(100.0);
        paypal.refund("TXN123", 50.0);
        
        System.out.println("\nStripe Payment:");
        PaymentProcessor stripe = new StripeAdapter(new StripeAPI());
        stripe.processPayment(200.0);
        stripe.refund("CHG456", 75.0);
        
        // EXAMPLE 2: Database Drivers
        System.out.println("\n\n🗄️  EXAMPLE 2: Database Drivers");
        System.out.println("═══════════════════════════════════════════════════════════");
        
        System.out.println("\nMySQL Database:");
        Database mysql = new MySQLAdapter(new LegacyMySQLDriver());
        mysql.connect("localhost:3306/mydb");
        mysql.executeQuery("SELECT * FROM users");
        mysql.disconnect();
        
        System.out.println("\nPostgreSQL Database:");
        Database postgres = new PostgreSQLAdapter(new ModernPostgreSQLDriver());
        postgres.connect("localhost:5432/mydb");
        postgres.executeQuery("SELECT * FROM products");
        postgres.disconnect();
        
        // EXAMPLE 3: Temperature Sensor
        System.out.println("\n\n🌡️  EXAMPLE 3: Temperature Sensor");
        System.out.println("═══════════════════════════════════════════════════════════");
        
        FahrenheitSensor fahrenheitSensor = new FahrenheitSensor();
        System.out.println("Fahrenheit sensor reading: " + 
                         fahrenheitSensor.getTemperatureInFahrenheit() + "°F");
        
        TemperatureSensor celsiusSensor = new TemperatureAdapter(fahrenheitSensor);
        System.out.println("Celsius sensor reading: " + 
                         String.format("%.1f", celsiusSensor.getTemperature()) + "°C");
        
        // KEY BENEFITS
        System.out.println("\n\n🎯 KEY BENEFITS OF ADAPTER PATTERN");
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("✓ Makes incompatible interfaces work together");
        System.out.println("✓ Integrates third-party libraries easily");
        System.out.println("✓ Follows Open/Closed Principle");
        System.out.println("✓ Single Responsibility Principle");
        System.out.println("✓ Reuses existing code without modification");
        System.out.println("✓ Decouples client from implementation");
        System.out.println("✓ Easy to add new adapters");
    }
}
