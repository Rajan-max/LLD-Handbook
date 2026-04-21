# Factory Design Pattern

## 📖 Definition
**Creates objects without specifying their exact class.**

In simpler terms:
- Client asks for an object without knowing which specific class to create
- Factory decides which class to instantiate based on input
- "I need a notification, but I don't care if it's Email or SMS - just give me one!"

## 🎯 Core Concept

**Problem**: Client code is tightly coupled to concrete classes and knows too much about object creation.

**Solution**: Delegate object creation to a factory that returns objects through a common interface.

## ❌ Problem - Without Factory

```java
class NotificationService {
    public void send(String type, String message) {
        // PROBLEM: Client knows about all concrete classes
        if (type.equals("EMAIL")) {
            EmailNotification email = new EmailNotification();
            email.setSmtpServer("smtp.gmail.com");
            email.authenticate();
            email.send(message);
        } else if (type.equals("SMS")) {
            SMSNotification sms = new SMSNotification();
            sms.setProvider("Twilio");
            sms.configure();
            sms.send(message);
        }
        // Adding new type requires modifying this code!
    }
}
```

**Why is this bad?**
- 🔴 **Tight coupling** - Client knows all concrete classes
- 🔴 **if-else chains** - Grows with each new type
- 🔴 **Violates OCP** - Must modify code to add new types
- 🔴 **Complex creation** - Client handles initialization logic
- 🔴 **Hard to maintain** - Changes ripple through codebase

## ✅ Solution - Factory Pattern

### Components:

1. **Product Interface** - Common interface for all products
2. **Concrete Products** - Specific implementations
3. **Factory** - Creates and returns products

## 🔧 Implementation Approaches

### 1. Simple Factory (Static Factory)

```java
// Step 1: Product interface
interface Notification {
    void send(String message);
}

// Step 2: Concrete products
class EmailNotification implements Notification {
    public void send(String message) {
        System.out.println("Email: " + message);
    }
}

class SMSNotification implements Notification {
    public void send(String message) {
        System.out.println("SMS: " + message);
    }
}

// Step 3: Simple Factory
class NotificationFactory {
    public static Notification create(String type) {
        return switch (type) {
            case "EMAIL" -> new EmailNotification();
            case "SMS" -> new SMSNotification();
            default -> throw new IllegalArgumentException();
        };
    }
}

// Step 4: Clean client code
Notification notification = NotificationFactory.create("EMAIL");
notification.send("Hello!");
```

**Pros:**
- ✅ Simple and easy to understand
- ✅ Centralizes creation logic
- ✅ Client doesn't know concrete classes

**Cons:**
- ❌ Violates Open/Closed (must modify factory for new types)
- ❌ Single factory can become large

**When to use:** Simple scenarios with few product types

---

### 2. Factory Method Pattern

```java
// Abstract Creator
abstract class NotificationCreator {
    // Template method
    public final Notification getNotification() {
        Notification notification = createNotification();
        configureNotification(notification);
        return notification;
    }
    
    // Factory method - subclasses implement
    protected abstract Notification createNotification();
    
    // Hook method
    protected void configureNotification(Notification n) { }
}

// Concrete Creators
class EmailCreator extends NotificationCreator {
    protected Notification createNotification() {
        return new EmailNotification();
    }
    
    protected void configureNotification(Notification n) {
        System.out.println("Configuring Email with SMTP");
    }
}

class SMSCreator extends NotificationCreator {
    protected Notification createNotification() {
        return new SMSNotification();
    }
}

// Usage
NotificationCreator creator = new EmailCreator();
Notification notification = creator.getNotification();
notification.send("Hello!");
```

**Pros:**
- ✅ Follows Open/Closed Principle
- ✅ More flexible and extensible
- ✅ Each factory can have custom logic
- ✅ Subclasses control object creation

**Cons:**
- ⚠️ More complex than Simple Factory
- ⚠️ Requires more classes

**When to use:** Complex scenarios requiring customization

---

## 📊 Comparison Table

| Aspect | Simple Factory | Factory Method |
|--------|---------------|----------------|
| Complexity | Low | Medium |
| Flexibility | Limited | High |
| Open/Closed | ❌ Violates | ✅ Follows |
| Number of classes | Few | Many |
| Customization | Limited | Extensive |
| Best for | Simple cases | Complex scenarios |

## 🌍 Real-World Use Cases

### 1. Database Connections
```java
Connection conn = DatabaseFactory.createConnection("MYSQL");
// Returns MySQLConnection, PostgreSQLConnection, etc.
```

### 2. Payment Processors
```java
PaymentProcessor processor = PaymentFactory.create("PAYPAL");
processor.processPayment(100.0);
```

### 3. Document Generators
```java
Document doc = DocumentFactory.create("PDF");
doc.generate();
```

### 4. Notification Services
```java
Notification notification = NotificationFactory.create("EMAIL");
notification.send("Hello!");
```

### 5. Vehicle Manufacturing
```java
Vehicle vehicle = VehicleFactory.create("CAR");
vehicle.drive();
```

## 🔍 How to Identify When to Use

Ask yourself:

1. **"Do I have multiple related classes with common interface?"**
   - If yes → Consider Factory

2. **"Is object creation complex or varies based on conditions?"**
   - If yes → Use Factory

3. **"Do I want to decouple creation from usage?"**
   - If yes → Factory Pattern

4. **"Will I add new types frequently?"**
   - If yes → Factory Method Pattern

## 💡 Best Practices

1. **Use Simple Factory** for straightforward scenarios
2. **Use Factory Method** when you need flexibility
3. **Return interface types**, not concrete classes
4. **Keep factories focused** - one responsibility
5. **Consider enum** for type parameters
6. **Handle invalid types** gracefully

## ⚖️ Pros and Cons

### Pros
- ✅ **Loose coupling** - Client doesn't know concrete classes
- ✅ **Single Responsibility** - Creation logic in one place
- ✅ **Open/Closed** - Easy to add new types (Factory Method)
- ✅ **Flexibility** - Easy to change implementations
- ✅ **Testability** - Easy to mock factories

### Cons
- ❌ **More classes** - Can increase code complexity
- ❌ **Indirection** - Extra layer between client and objects
- ❌ **Overkill** - May be unnecessary for simple cases

## 🎓 Practice Exercise

Create a factory for different types of loggers:

**Requirements:**
- FileLogger - logs to file
- ConsoleLogger - logs to console
- DatabaseLogger - logs to database
- Factory to create appropriate logger

**Solution:**
```java
interface Logger {
    void log(String message);
}

class FileLogger implements Logger {
    public void log(String message) {
        System.out.println("File: " + message);
    }
}

class ConsoleLogger implements Logger {
    public void log(String message) {
        System.out.println("Console: " + message);
    }
}

class LoggerFactory {
    public static Logger create(String type) {
        return switch (type) {
            case "FILE" -> new FileLogger();
            case "CONSOLE" -> new ConsoleLogger();
            case "DATABASE" -> new DatabaseLogger();
            default -> throw new IllegalArgumentException();
        };
    }
}

// Usage
Logger logger = LoggerFactory.create("FILE");
logger.log("Application started");
```


## 🚀 When NOT to Use Factory
- When you have only one or two product types
- When object creation is simple (just `new`)
- When you don't expect to add new types
- When the added complexity isn't justified

## 📝 Summary
**Problem**: Client tightly coupled to concrete classes → Hard to extend
**Solution**: Factory creates objects → Client uses interface → Easy to extend
**Simple Factory**: Good for simple cases, violates OCP
**Factory Method**: More flexible, follows OCP, requires more classes

---
