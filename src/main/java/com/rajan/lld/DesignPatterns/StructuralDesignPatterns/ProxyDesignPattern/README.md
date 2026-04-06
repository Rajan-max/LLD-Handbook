# Proxy Design Pattern 🛡️

## 📖 Definition

The **Proxy Pattern** is a structural design pattern that provides a surrogate or placeholder for another object to control access to it.

**In Simple Terms:**
- Acts as a substitute/representative for another object
- Controls access to the real object
- Can add extra functionality without modifying the original object
- Like a security guard controlling access to a building

---

## 🎯 Core Concept

```
Client → Proxy → RealSubject
```

The proxy:
1. Has the same interface as the real object
2. Controls access to the real object
3. Can add functionality (lazy loading, caching, logging, access control)
4. Delegates actual work to the real object

---

## ❌ Problem: Direct Access Issues

### Without Proxy Pattern:

```java
// Expensive object created immediately
class ExpensiveImage {
    public ExpensiveImage(String filename) {
        loadFromDisk(filename); // Takes 5 seconds!
    }
}

// Problem: All images loaded even if not used
ExpensiveImage img1 = new ExpensiveImage("photo1.jpg"); // Loads now
ExpensiveImage img2 = new ExpensiveImage("photo2.jpg"); // Loads now
ExpensiveImage img3 = new ExpensiveImage("photo3.jpg"); // Loads now
// Only img1 is displayed, but all 3 are loaded!
```

### Issues:
- ❌ Expensive objects created even when not needed
- ❌ No access control (anyone can access anything)
- ❌ Repeated expensive operations (no caching)
- ❌ No logging/monitoring
- ❌ Can't add functionality without modifying original class

---

## ✅ Solution: Proxy Pattern

### Step-by-Step Implementation:

#### Step 1: Subject Interface
```java
interface Image {
    void display();
}
```

#### Step 2: RealSubject (Expensive Object)
```java
class RealImage implements Image {
    private String filename;
    
    public RealImage(String filename) {
        this.filename = filename;
        loadFromDisk(); // Expensive operation
    }
    
    private void loadFromDisk() {
        System.out.println("Loading: " + filename);
    }
    
    @Override
    public void display() {
        System.out.println("Displaying: " + filename);
    }
}
```

#### Step 3: Proxy (Controls Access)
```java
class ImageProxy implements Image {
    private String filename;
    private RealImage realImage; // Lazy initialization
    
    public ImageProxy(String filename) {
        this.filename = filename;
        // No loading yet!
    }
    
    @Override
    public void display() {
        if (realImage == null) {
            realImage = new RealImage(filename); // Load only when needed
        }
        realImage.display();
    }
}
```

#### Step 4: Client Usage
```java
// Proxies created instantly (no loading)
Image img1 = new ImageProxy("photo1.jpg");
Image img2 = new ImageProxy("photo2.jpg");
Image img3 = new ImageProxy("photo3.jpg");

img1.display(); // Loads and displays photo1
img1.display(); // Just displays (already loaded)
// photo2 and photo3 never loaded!
```

---

## 🔄 Before vs After Comparison

| Aspect | Without Proxy ❌ | With Proxy ✅ |
|--------|-----------------|---------------|
| **Object Creation** | All objects created immediately | Created only when needed |
| **Memory Usage** | High (all objects in memory) | Low (only used objects) |
| **Performance** | Slow startup | Fast startup |
| **Access Control** | None | Can restrict access |
| **Caching** | No caching | Can cache results |
| **Logging** | Must modify original | Add via proxy |
| **Flexibility** | Low | High |

---

## 🌍 Real-World Use Cases

### 1. Virtual Proxy (Lazy Loading) 🔄
**Problem:** Loading large images/videos upfront is expensive  
**Solution:** Load only when displayed

```java
Image thumbnail = new ImageProxy("4K-video.mp4");
// Video not loaded yet
thumbnail.display(); // Now it loads
```

**Real Examples:**
- Image galleries (load images on scroll)
- Video streaming (load on play)
- Large document viewers
- Game asset loading

---

### 2. Protection Proxy (Access Control) 🔒
**Problem:** Need to restrict access based on permissions  
**Solution:** Proxy checks permissions before allowing access

```java
Document doc = new ProtectedDocumentProxy(realDoc, Role.VIEWER);
doc.view();   // ✅ Allowed
doc.edit();   // ❌ Access denied
doc.delete(); // ❌ Access denied
```

**Real Examples:**
- File system permissions
- Database access control
- API rate limiting
- Admin panels

---

### 3. Cache Proxy (Performance) ⚡
**Problem:** Repeated expensive operations slow down system  
**Solution:** Cache results to avoid redundant work

```java
DatabaseQuery db = new CachingDatabaseProxy();
db.query("SELECT * FROM users"); // Executes query
db.query("SELECT * FROM users"); // Returns cached result
```

**Real Examples:**
- Database query caching
- API response caching
- Computation results
- Web page caching

---

### 4. Remote Proxy (Network Calls) 🌐
**Problem:** Remote objects are in different address space  
**Solution:** Proxy represents remote object locally

```java
VideoService service = new VideoServiceProxy("https://api.com");
service.streamVideo("video123"); // Handles network call
```

**Real Examples:**
- RMI (Remote Method Invocation)
- Web services
- Microservices communication
- Cloud storage access

---

### 5. Logging Proxy (Monitoring) 📝
**Problem:** Need to monitor object usage without modifying it  
**Solution:** Proxy logs all operations

```java
BankAccount account = new LoggingBankAccountProxy(realAccount);
account.deposit(100); // Logs: "Depositing $100"
account.withdraw(50); // Logs: "Withdrawing $50"
```

**Real Examples:**
- Audit trails
- Performance monitoring
- Debug logging
- Usage analytics

---

## 🏭 Industry Examples

### Java Standard Library:
1. **java.lang.reflect.Proxy** - Dynamic proxies
2. **java.rmi.*** - Remote Method Invocation
3. **javax.persistence.*** - JPA lazy loading
4. **Spring AOP** - Aspect-Oriented Programming

### Frameworks:
1. **Hibernate** - Lazy loading of entities
2. **Spring** - Transaction management, security
3. **Mockito** - Mock objects for testing
4. **CDN Services** - Caching proxies

---

## ✅ Advantages

| Advantage | Description |
|-----------|-------------|
| **Lazy Initialization** | Create expensive objects only when needed |
| **Access Control** | Protect sensitive operations |
| **Performance** | Cache results, reduce redundant work |
| **Separation of Concerns** | Add functionality without modifying original |
| **Open/Closed Principle** | Extend behavior without changing code |
| **Single Responsibility** | Proxy handles cross-cutting concerns |

---

## ⚠️ Disadvantages

| Disadvantage | Description |
|--------------|-------------|
| **Complexity** | Additional layer of indirection |
| **Response Time** | May introduce slight delay |
| **Code Overhead** | More classes to maintain |
| **Memory** | Proxy objects consume memory |

---

## 🤔 When to Use

✅ **Use Proxy Pattern when:**
- Need lazy initialization of expensive objects
- Want to control access to objects
- Need to cache expensive operations
- Working with remote objects
- Want to add logging/monitoring
- Need to protect sensitive operations

❌ **Don't use when:**
- Object creation is cheap
- No need for access control
- Adding unnecessary complexity
- Direct access is sufficient

---

## 🆚 Proxy vs Similar Patterns

### Proxy vs Decorator

| Aspect | Proxy | Decorator |
|--------|-------|-----------|
| **Purpose** | Control access | Add responsibilities |
| **Creation** | Controls object creation | Wraps existing object |
| **Focus** | Access control, lazy loading | Enhance functionality |
| **Example** | Lazy image loading | Add borders to image |

### Proxy vs Adapter

| Aspect | Proxy | Adapter |
|--------|-------|---------|
| **Purpose** | Control access | Convert interface |
| **Interface** | Same as real object | Different interface |
| **Focus** | Access control | Interface compatibility |
| **Example** | Image proxy | MP3 to MediaPlayer adapter |

### Proxy vs Facade

| Aspect | Proxy | Facade |
|--------|-------|--------|
| **Purpose** | Control single object | Simplify subsystem |
| **Interface** | Same as real object | New simplified interface |
| **Complexity** | One-to-one | One-to-many |
| **Example** | Database proxy | Home theater facade |

---

## 🎓 Types of Proxies Summary

| Type | Purpose | Example |
|------|---------|---------|
| **Virtual Proxy** | Lazy loading | Image loading on demand |
| **Protection Proxy** | Access control | Document permissions |
| **Cache Proxy** | Performance | Database query caching |
| **Remote Proxy** | Network calls | RMI, web services |
| **Logging Proxy** | Monitoring | Audit trails |
| **Smart Reference** | Extra actions | Reference counting |

---

## 💡 Implementation Tips

### 1. Object Adapter vs Class Adapter
```java
// Object Adapter (Composition - Preferred)
class Proxy implements Subject {
    private RealSubject subject;
}

// Class Adapter (Inheritance - Less flexible)
class Proxy extends RealSubject {
}
```

### 2. Thread Safety
```java
class ThreadSafeProxy implements Subject {
    private volatile RealSubject subject;
    
    public void operation() {
        if (subject == null) {
            synchronized(this) {
                if (subject == null) {
                    subject = new RealSubject();
                }
            }
        }
        subject.operation();
    }
}
```

### 3. Dynamic Proxies (Java)
```java
Subject proxy = (Subject) Proxy.newProxyInstance(
    Subject.class.getClassLoader(),
    new Class[] { Subject.class },
    new InvocationHandler() {
        public Object invoke(Object proxy, Method method, Object[] args) {
            // Add behavior here
            return method.invoke(realSubject, args);
        }
    }
);
```

---

## 🏋️ Practice Exercise

**Challenge:** Create a Smart Reference Proxy for resource management

**Requirements:**
1. Track number of references to an object
2. Automatically clean up when no references exist
3. Log all access attempts
4. Implement lazy loading

**Bonus:**
- Add thread safety
- Implement weak references
- Add memory usage tracking

---

## 🎯 Key Takeaways

1. **Proxy controls access** to another object
2. **Same interface** as the real object
3. **Five main types**: Virtual, Protection, Cache, Remote, Logging
4. **Lazy loading** is the most common use case
5. **Adds functionality** without modifying original
6. **Open/Closed Principle** - extend without changing
7. **Use when** you need control over object access

---

## 📚 Related Patterns

- **Decorator** - Add responsibilities dynamically
- **Adapter** - Convert interfaces
- **Facade** - Simplify complex subsystems
- **Flyweight** - Share objects to save memory

---

## 🔗 Next Steps

1. ✅ Understand the five proxy types
2. ✅ Implement virtual proxy for lazy loading
3. ✅ Add protection proxy for access control
4. ✅ Use cache proxy for performance
5. ✅ Explore Java's dynamic proxies
6. ✅ Practice with real-world scenarios

---

**Remember:** Proxy is about **controlling access**, Decorator is about **adding functionality**! 🎯
