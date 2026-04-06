package com.rajan.lld.DesignPatterns.CreationalDesignPatterns.SingletonDesignPattern;

/**
 * SINGLETON DESIGN PATTERN - Complete Example
 * 
 * Definition: Ensures a class has only ONE instance and provides a global access point to it.
 * 
 * In simple terms:
 * - Only one object of the class exists in the entire application
 * - Provides a way to access that single instance globally
 * - "There can be only one!"
 * 
 * When to use:
 * - Database connections
 * - Logger
 * - Configuration settings
 * - Cache
 * - Thread pools
 */

// ============================================================================
// PROBLEM - Without Singleton
// ============================================================================

/**
 * PROBLEM: Multiple instances waste resources
 * Each instance creates expensive database connection
 */
class DatabaseConnectionBad {
    private String connectionString;
    
    public DatabaseConnectionBad() {
        // PROBLEM: Expensive operation repeated for each instance
        System.out.println("Creating expensive database connection...");
        this.connectionString = "Connected to DB";
    }
    
    public void executeQuery(String sql) {
        System.out.println("Executing: " + sql);
    }
}

/**
 * PROBLEM: Creating multiple instances unnecessarily
 */
class ApplicationBad {
    public void run() {
        // PROBLEM: Each call creates a new expensive connection
        DatabaseConnectionBad conn1 = new DatabaseConnectionBad(); // Expensive!
        conn1.executeQuery("SELECT * FROM users");
        
        DatabaseConnectionBad conn2 = new DatabaseConnectionBad(); // Expensive again!
        conn2.executeQuery("SELECT * FROM orders");
        
        // Two separate connections created - waste of resources!
    }
}


// ============================================================================
// SOLUTION 1 - Basic Singleton (Eager Initialization)
// ============================================================================

/**
 * SOLUTION: Eager Singleton - Instance created at class loading
 * 
 * Pros:
 * - Simple and thread-safe
 * - No synchronization overhead
 * 
 * Cons:
 * - Instance created even if never used
 * - Cannot handle exceptions during creation
 */
class EagerSingleton {
    // Step 1: Create instance at class loading time
    private static final EagerSingleton instance = new EagerSingleton();
    
    // Step 2: Private constructor prevents external instantiation
    private EagerSingleton() {
        System.out.println("Eager Singleton: Instance created at class loading");
    }
    
    // Step 3: Public method to get the instance
    public static EagerSingleton getInstance() {
        return instance;
    }
    
    public void doWork() {
        System.out.println("Eager Singleton working...");
    }
}


// ============================================================================
// SOLUTION 2 - Lazy Singleton (Not Thread-Safe)
// ============================================================================

/**
 * SOLUTION: Lazy Singleton - Instance created when first requested
 * 
 * Pros:
 * - Instance created only when needed
 * - Saves resources if never used
 * 
 * Cons:
 * - NOT thread-safe
 * - Multiple threads can create multiple instances
 */
class LazySingleton {
    // Step 1: Instance initially null
    private static LazySingleton instance;
    
    // Step 2: Private constructor
    private LazySingleton() {
        System.out.println("Lazy Singleton: Instance created on first request");
    }
    
    // Step 3: Create instance only when requested
    public static LazySingleton getInstance() {
        if (instance == null) {
            instance = new LazySingleton();
        }
        return instance;
    }
    
    public void doWork() {
        System.out.println("Lazy Singleton working...");
    }
}


// ============================================================================
// SOLUTION 3 - Thread-Safe Singleton (Double-Checked Locking)
// ============================================================================

/**
 * SOLUTION: Thread-Safe Singleton with Double-Checked Locking
 * 
 * Pros:
 * - Thread-safe
 * - Lazy initialization
 * - Minimal synchronization overhead
 * 
 * This is the RECOMMENDED approach for production!
 */
class ThreadSafeSingleton {
    // volatile ensures visibility across threads
    private static volatile ThreadSafeSingleton instance;
    
    private ThreadSafeSingleton() {
        System.out.println("Thread-Safe Singleton: Instance created safely");
    }
    
    // Double-checked locking pattern
    public static ThreadSafeSingleton getInstance() {
        // First check (no locking)
        if (instance == null) {
            // Synchronize only when instance is null
            synchronized (ThreadSafeSingleton.class) {
                // Second check (with locking)
                if (instance == null) {
                    instance = new ThreadSafeSingleton();
                }
            }
        }
        return instance;
    }
    
    public void doWork() {
        System.out.println("Thread-Safe Singleton working...");
    }
}


// ============================================================================
// SOLUTION 4 - Enum Singleton (Best Practice)
// ============================================================================

/**
 * SOLUTION: Enum Singleton - The BEST way to implement Singleton
 * 
 * Pros:
 * - Thread-safe by default
 * - Prevents reflection attacks
 * - Prevents serialization issues
 * - Simple and concise
 * 
 * This is Joshua Bloch's recommended approach!
 */
enum EnumSingleton {
    INSTANCE;
    
    public void doWork() {
        System.out.println("Enum Singleton working...");
    }
    
    public void executeQuery(String sql) {
        System.out.println("Executing: " + sql);
    }
}


// ============================================================================
// REAL-WORLD EXAMPLE - Database Connection Pool
// ============================================================================

/**
 * Real-world example: Database Connection Pool as Singleton
 */
class DatabaseConnectionPool {
    private static volatile DatabaseConnectionPool instance;
    private String connectionString;
    private int maxConnections = 10;
    
    private DatabaseConnectionPool() {
        System.out.println("Initializing Database Connection Pool...");
        this.connectionString = "jdbc:mysql://localhost:3306/mydb";
    }
    
    public static DatabaseConnectionPool getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnectionPool.class) {
                if (instance == null) {
                    instance = new DatabaseConnectionPool();
                }
            }
        }
        return instance;
    }
    
    public void executeQuery(String sql) {
        System.out.println("Executing query: " + sql);
    }
    
    public String getConnectionString() {
        return connectionString;
    }
}


// ============================================================================
// DEMO - Compare all approaches
// ============================================================================

public class SingletonDesignPattern {
    public static void main(String[] args) {
        
        System.out.println("========================================");
        System.out.println("PROBLEM - Without Singleton");
        System.out.println("========================================\n");
        
        ApplicationBad badApp = new ApplicationBad();
        badApp.run(); // Creates 2 expensive connections!
        
        System.out.println("\n========================================");
        System.out.println("SOLUTION 1 - Eager Singleton");
        System.out.println("========================================\n");
        
        EagerSingleton eager1 = EagerSingleton.getInstance();
        EagerSingleton eager2 = EagerSingleton.getInstance();
        System.out.println("Same instance? " + (eager1 == eager2)); // true
        eager1.doWork();
        
        System.out.println("\n========================================");
        System.out.println("SOLUTION 2 - Lazy Singleton");
        System.out.println("========================================\n");
        
        LazySingleton lazy1 = LazySingleton.getInstance();
        LazySingleton lazy2 = LazySingleton.getInstance();
        System.out.println("Same instance? " + (lazy1 == lazy2)); // true
        lazy1.doWork();
        
        System.out.println("\n========================================");
        System.out.println("SOLUTION 3 - Thread-Safe Singleton");
        System.out.println("========================================\n");
        
        ThreadSafeSingleton safe1 = ThreadSafeSingleton.getInstance();
        ThreadSafeSingleton safe2 = ThreadSafeSingleton.getInstance();
        System.out.println("Same instance? " + (safe1 == safe2)); // true
        safe1.doWork();
        
        System.out.println("\n========================================");
        System.out.println("SOLUTION 4 - Enum Singleton (BEST)");
        System.out.println("========================================\n");
        
        EnumSingleton enum1 = EnumSingleton.INSTANCE;
        EnumSingleton enum2 = EnumSingleton.INSTANCE;
        System.out.println("Same instance? " + (enum1 == enum2)); // true
        enum1.doWork();
        enum1.executeQuery("SELECT * FROM users");
        
        System.out.println("\n========================================");
        System.out.println("REAL-WORLD - Database Connection Pool");
        System.out.println("========================================\n");
        
        DatabaseConnectionPool pool1 = DatabaseConnectionPool.getInstance();
        DatabaseConnectionPool pool2 = DatabaseConnectionPool.getInstance();
        System.out.println("Same pool? " + (pool1 == pool2)); // true
        pool1.executeQuery("SELECT * FROM users");
        pool2.executeQuery("SELECT * FROM orders");
        
        System.out.println("\n========================================");
        System.out.println("KEY TAKEAWAYS");
        System.out.println("========================================");
        System.out.println("✓ Only ONE instance exists");
        System.out.println("✓ Global access point provided");
        System.out.println("✓ Saves resources (no duplicate instances)");
        System.out.println("✓ Use Enum for best practice");
        System.out.println("✓ Use Thread-Safe for complex scenarios");
    }
}
