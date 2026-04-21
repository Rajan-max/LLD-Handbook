# Singleton Design Pattern

## 📖 Definition
**Ensures a class has only ONE instance and provides a global access point to it.**

In simpler terms:
- Only one object of the class exists in the entire application
- Everyone uses the same instance
- "There can be only one!"

## 🎯 Core Concept

**Problem**: Creating multiple instances of expensive resources (database connections, loggers, etc.) wastes memory and resources.

**Solution**: Restrict class instantiation to a single object and provide global access to it.

## ❌ Problem - Without Singleton

```java
class DatabaseConnection {
    public DatabaseConnection() {
        // Expensive operation!
        System.out.println("Creating database connection...");
    }
}

// PROBLEM: Multiple expensive instances created
DatabaseConnection conn1 = new DatabaseConnection(); // Expensive!
DatabaseConnection conn2 = new DatabaseConnection(); // Expensive again!
DatabaseConnection conn3 = new DatabaseConnection(); // Waste of resources!
```

**Why is this bad?**
- 🔴 **Resource waste** - Multiple expensive objects created
- 🔴 **Memory overhead** - Each instance consumes memory
- 🔴 **Inconsistent state** - Different instances may have different states
- 🔴 **No control** - Cannot limit instance creation

## ✅ Solution - Singleton Pattern

### Key Components:

1. **Private constructor** - Prevents external instantiation
2. **Private static instance** - Holds the single instance
3. **Public static method** - Provides global access

## 🔧 Implementation Approaches

### 1. Eager Initialization (Simple)

```java
class EagerSingleton {
    // Instance created at class loading
    private static final EagerSingleton instance = new EagerSingleton();
    
    private EagerSingleton() { }
    
    public static EagerSingleton getInstance() {
        return instance;
    }
}
```

**Pros:**
- ✅ Simple and thread-safe
- ✅ No synchronization overhead

**Cons:**
- ❌ Instance created even if never used
- ❌ Cannot handle exceptions

**When to use:** When instance is always needed and creation is cheap

---

### 2. Lazy Initialization (Basic)

```java
class LazySingleton {
    private static LazySingleton instance;
    
    private LazySingleton() { }
    
    public static LazySingleton getInstance() {
        if (instance == null) {
            instance = new LazySingleton();
        }
        return instance;
    }
}
```

**Pros:**
- ✅ Instance created only when needed
- ✅ Saves resources if never used

**Cons:**
- ❌ NOT thread-safe
- ❌ Multiple threads can create multiple instances

**When to use:** Single-threaded applications only

---

### 3. Thread-Safe (Double-Checked Locking) ⭐

```java
class ThreadSafeSingleton {
    private static volatile ThreadSafeSingleton instance;
    
    private ThreadSafeSingleton() { }
    
    public static ThreadSafeSingleton getInstance() {
        if (instance == null) {                    // First check
            synchronized (ThreadSafeSingleton.class) {
                if (instance == null) {            // Second check
                    instance = new ThreadSafeSingleton();
                }
            }
        }
        return instance;
    }
}
```

**Pros:**
- ✅ Thread-safe
- ✅ Lazy initialization
- ✅ Minimal synchronization overhead

**Cons:**
- ⚠️ Slightly complex

**When to use:** Multi-threaded applications (RECOMMENDED)

---

### 4. Enum Singleton (Best Practice) 🏆

```java
enum EnumSingleton {
    INSTANCE;
    
    public void doWork() {
        System.out.println("Working...");
    }
}

// Usage
EnumSingleton.INSTANCE.doWork();
```

**Pros:**
- ✅ Thread-safe by default
- ✅ Prevents reflection attacks
- ✅ Prevents serialization issues
- ✅ Simple and concise

**Cons:**
- ⚠️ Cannot extend other classes

**When to use:** ALWAYS (unless you need inheritance)

**This is Joshua Bloch's recommended approach!**

---

## 📊 Comparison Table

| Approach | Thread-Safe | Lazy Init | Complexity | Recommended |
|----------|-------------|-----------|------------|-------------|
| Eager | ✅ | ❌ | Low | For simple cases |
| Lazy | ❌ | ✅ | Low | Single-threaded only |
| Thread-Safe | ✅ | ✅ | Medium | Multi-threaded apps |
| Enum | ✅ | ❌ | Low | **BEST PRACTICE** |

## 🌍 Real-World Use Cases

### 1. Database Connection Pool
```java
DatabaseConnectionPool pool = DatabaseConnectionPool.getInstance();
pool.executeQuery("SELECT * FROM users");
```

### 2. Logger
```java
Logger logger = Logger.getInstance();
logger.log("Application started");
```

### 3. Configuration Manager
```java
ConfigManager config = ConfigManager.getInstance();
String apiKey = config.get("API_KEY");
```

### 4. Cache
```java
Cache cache = Cache.getInstance();
cache.put("user:123", userData);
```

## ⚠️ Common Pitfalls

### 1. Reflection Attack
```java
// Can break singleton using reflection
Constructor<Singleton> constructor = Singleton.class.getDeclaredConstructor();
constructor.setAccessible(true);
Singleton instance2 = constructor.newInstance(); // Creates second instance!
```

**Solution:** Use Enum Singleton (immune to reflection)

### 2. Serialization Issue
```java
// Deserialization creates new instance
Singleton instance1 = Singleton.getInstance();
// Serialize and deserialize
Singleton instance2 = deserialize(); // Different instance!
```

**Solution:** Implement `readResolve()` method or use Enum

### 3. Cloning
```java
Singleton instance2 = (Singleton) instance1.clone(); // Creates second instance!
```

**Solution:** Override `clone()` and throw exception

## 🔍 How to Identify When to Use

Ask yourself:

1. **"Should only one instance exist?"**
   - If yes → Consider Singleton

2. **"Is this a shared resource?"**
   - Database, Logger, Config → Singleton

3. **"Do I need global access?"**
   - If yes → Singleton

4. **"Is creation expensive?"**
   - If yes → Singleton saves resources

## 💡 Best Practices

1. **Use Enum Singleton** when possible (best practice)
2. **Use Thread-Safe Singleton** for complex scenarios
3. **Make constructor private** always
4. **Use volatile** for thread-safe lazy initialization
5. **Prevent cloning** by overriding clone()
6. **Handle serialization** properly

## ⚖️ Pros and Cons

### Pros
- ✅ **Controlled access** - Single instance guaranteed
- ✅ **Resource efficiency** - No duplicate instances
- ✅ **Global access** - Available throughout application
- ✅ **Lazy initialization** - Create when needed (some approaches)

### Cons
- ❌ **Global state** - Can make testing difficult
- ❌ **Hidden dependencies** - Not clear from class signature
- ❌ **Tight coupling** - Classes depend on singleton directly
- ❌ **Difficult to mock** - Hard to replace in tests

## 🎓 Practice Exercise

Implement a Singleton for a Configuration Manager:

**Requirements:**
- Load configuration from file
- Provide global access
- Thread-safe
- Lazy initialization

**Solution:**
```java
enum ConfigManager {
    INSTANCE;
    
    private Properties config;
    
    ConfigManager() {
        config = new Properties();
        // Load from file
    }
    
    public String get(String key) {
        return config.getProperty(key);
    }
}

// Usage
String apiKey = ConfigManager.INSTANCE.get("API_KEY");
```

## 🔑 Key Takeaways

1. **One instance only** - Singleton ensures single object
2. **Global access** - Available throughout application
3. **Use Enum** - Best practice for most cases
4. **Thread-safety matters** - Use appropriate approach
5. **Consider alternatives** - Dependency injection may be better

## 🚀 When NOT to Use Singleton

- When you need multiple instances
- When testing is important (consider dependency injection)
- When you need different configurations
- When state needs to be isolated

---
