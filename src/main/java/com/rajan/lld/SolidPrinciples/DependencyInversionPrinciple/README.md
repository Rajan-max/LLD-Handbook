# Dependency Inversion Principle (DIP)

## 📖 Definition
**High-level modules should not depend on low-level modules. Both should depend on abstractions.**

Also:
**Abstractions should not depend on details. Details should depend on abstractions.**

In simpler terms:
- Don't depend on concrete classes
- Depend on interfaces or abstract classes
- Use dependency injection
- Invert the direction of dependencies

## 🎯 Core Concept
**"Depend on abstractions, not concretions."**

Traditional dependency flow:
```
High-level module → Low-level module (BAD)
```

Inverted dependency flow:
```
High-level module → Abstraction ← Low-level module (GOOD)
```

Both depend on the abstraction in the middle!

## ❌ Bad Example (Violates DIP)

```java
// Low-level module (concrete implementation)
class EmailSender {
    public void sendEmail(String recipient, String message) {
        System.out.println("Sending email...");
    }
}

// PROBLEM: High-level module depends on low-level module
class NotificationService {
    private EmailSender emailSender;
    
    public NotificationService() {
        this.emailSender = new EmailSender(); // Direct dependency!
    }
    
    public void sendNotification(String recipient, String message) {
        emailSender.sendEmail(recipient, message);
    }
}
```

**Why is this bad?**
- 🔴 **Tight coupling** - NotificationService directly depends on EmailSender
- 🔴 **Not flexible** - Cannot switch to SMS without modifying code
- 🔴 **Hard to test** - Cannot inject mock EmailSender
- 🔴 **Violates DIP** - High-level depends on low-level
- 🔴 **Not extensible** - Adding new notification types requires changes

**Real scenario:**
```java
NotificationService service = new NotificationService();
service.sendNotification("user@example.com", "Hello");
// Hardcoded to email - cannot change to SMS!
```

**What if we want SMS?**
- Need to modify NotificationService class
- Or create a new NotificationServiceSMS class
- Code duplication and maintenance nightmare!

## ✅ Good Example (Follows DIP)

```java
// SOLUTION: Define abstraction
interface NotificationSender {
    void send(String recipient, String message);
}

// Low-level modules depend on abstraction
class EmailSender implements NotificationSender {
    public void send(String recipient, String message) {
        System.out.println("Sending email...");
    }
}

class SMSSender implements NotificationSender {
    public void send(String recipient, String message) {
        System.out.println("Sending SMS...");
    }
}

// High-level module depends on abstraction
class NotificationService {
    private NotificationSender sender;
    
    // Dependency Injection via constructor
    public NotificationService(NotificationSender sender) {
        this.sender = sender;
    }
    
    public void sendNotification(String recipient, String message) {
        sender.send(recipient, message); // Works with any implementation!
    }
}
```

**Why is this better?**
- ✅ **Loose coupling** - Depends on interface, not concrete class
- ✅ **Flexible** - Easily switch between Email, SMS, Push, etc.
- ✅ **Easy to test** - Inject mock implementations
- ✅ **Follows DIP** - Both high and low level depend on abstraction
- ✅ **Extensible** - Add new senders without modifying service

**Real scenario:**
```java
// Use Email
NotificationService emailService = new NotificationService(new EmailSender());
emailService.sendNotification("user@example.com", "Hello");

// Use SMS - same class, different implementation!
NotificationService smsService = new NotificationService(new SMSSender());
smsService.sendNotification("+1234567890", "Hello");

// Add Push notifications - no changes to NotificationService!
NotificationService pushService = new NotificationService(new PushSender());
pushService.sendNotification("user123", "Hello");
```

## 🔍 How to Identify DIP Violations

Ask yourself:

1. **"Does my high-level class create instances of low-level classes?"**
   - If yes → Violates DIP

2. **"Am I using 'new' keyword for dependencies inside classes?"**
   - If yes → Likely violates DIP

3. **"Can I easily swap implementations without modifying code?"**
   - If no → Violates DIP

4. **"Are my classes hard to test because of concrete dependencies?"**
   - If yes → Violates DIP

## 🚨 Common DIP Violations

### 1. Direct Instantiation
```java
class OrderService {
    private MySQLDatabase database;
    
    public OrderService() {
        this.database = new MySQLDatabase(); // Violates DIP!
    }
}
```

### 2. Depending on Concrete Classes
```java
class PaymentProcessor {
    public void process(CreditCard card) { // Concrete class!
        // Cannot use PayPal, Bitcoin, etc.
    }
}
```

### 3. No Abstraction Layer
```java
class ReportGenerator {
    private PDFGenerator pdfGenerator = new PDFGenerator();
    
    public void generate() {
        pdfGenerator.create(); // Hardcoded to PDF!
    }
}
```

## 💡 Real-World Analogy

Think of **electrical outlets**:

**Bad (Violates DIP)**:
- Each appliance has a unique plug shape
- Outlets are designed for specific appliances
- Cannot plug a lamp into a TV outlet

**Good (Follows DIP)**:
- Standard outlet interface (abstraction)
- All appliances use standard plugs (depend on abstraction)
- Any appliance works in any outlet

Similarly, NotificationService should work with any NotificationSender, not just EmailSender!

## 🛠️ How to Apply DIP

### 1. Identify Dependencies
Find where high-level modules depend on low-level modules.

### 2. Create Abstractions
Define interfaces or abstract classes.

### 3. Implement Abstractions
Make low-level modules implement the interfaces.

### 4. Inject Dependencies
Use constructor injection, setter injection, or interface injection.

### Example:
```java
// Step 1: Identify - OrderService depends on MySQLDatabase

// Step 2: Create abstraction
interface Database {
    void save(Order order);
}

// Step 3: Implement
class MySQLDatabase implements Database { ... }
class MongoDatabase implements Database { ... }

// Step 4: Inject
class OrderService {
    private Database database;
    
    public OrderService(Database database) { // Injection!
        this.database = database;
    }
}
```

## 📊 Comparison Table

| Aspect | Bad (Violates DIP) | Good (Follows DIP) |
|--------|-------------------|-------------------|
| Dependency | Concrete classes | Abstractions (interfaces) |
| Coupling | Tight | Loose |
| Flexibility | Low | High |
| Testability | Hard | Easy |
| Extensibility | Difficult | Simple |
| Maintainability | Poor | Excellent |
| Instantiation | Inside class (new) | Injected from outside |

## 🎓 Practice Exercise

Identify the DIP violation:

```java
class UserController {
    private MySQLUserRepository repository;
    
    public UserController() {
        this.repository = new MySQLUserRepository();
    }
    
    public void saveUser(User user) {
        repository.save(user);
    }
}
```

**Answer**: Violates DIP! UserController directly depends on MySQLUserRepository.

**Solution**:
```java
interface UserRepository {
    void save(User user);
}

class MySQLUserRepository implements UserRepository { ... }
class MongoUserRepository implements UserRepository { ... }

class UserController {
    private UserRepository repository;
    
    public UserController(UserRepository repository) { // Inject!
        this.repository = repository;
    }
    
    public void saveUser(User user) {
        repository.save(user);
    }
}
```

## 🔑 Key Takeaways

1. **Depend on abstractions** (interfaces), not concrete classes
2. **Use dependency injection** - inject dependencies from outside
3. **Invert the dependency** - both high and low level depend on abstraction
4. **Avoid 'new' keyword** for dependencies inside classes
5. **Think "plug and play"** - swap implementations easily

## 🚀 Benefits

- ✅ **Loose coupling** - modules are independent
- ✅ **Flexibility** - easily swap implementations
- ✅ **Testability** - inject mocks for testing
- ✅ **Maintainability** - changes isolated to implementations
- ✅ **Extensibility** - add new implementations without changes
- ✅ **Reusability** - abstractions can be reused

## 📝 Summary

**Bad**: High-level → Low-level (concrete) → Tight coupling → Inflexible

**Good**: High-level → Abstraction ← Low-level → Loose coupling → Flexible

---

**Remember**: If you're using 'new' to create dependencies inside your class, you're probably violating DIP. Inject dependencies instead!

## 🔗 Relationship with Other Principles

- **Enables OCP**: Depend on abstractions to extend without modification
- **Works with ISP**: Depend on small, focused interfaces
- **Supports LSP**: Abstractions enable safe substitution
- **Complements SRP**: Each class has one reason to change

## 🎯 Dependency Injection Types

### 1. Constructor Injection (Recommended)
```java
class Service {
    private Repository repo;
    
    public Service(Repository repo) {
        this.repo = repo;
    }
}
```

### 2. Setter Injection
```java
class Service {
    private Repository repo;
    
    public void setRepository(Repository repo) {
        this.repo = repo;
    }
}
```

### 3. Interface Injection
```java
interface RepositoryInjector {
    void injectRepository(Repository repo);
}

class Service implements RepositoryInjector {
    private Repository repo;
    
    public void injectRepository(Repository repo) {
        this.repo = repo;
    }
}
```

## 🏆 DIP Completes SOLID!

Congratulations! You've learned all 5 SOLID principles:
1. ✅ **S**ingle Responsibility Principle
2. ✅ **O**pen/Closed Principle
3. ✅ **L**iskov Substitution Principle
4. ✅ **I**nterface Segregation Principle
5. ✅ **D**ependency Inversion Principle

Together, they create maintainable, flexible, and robust software!
