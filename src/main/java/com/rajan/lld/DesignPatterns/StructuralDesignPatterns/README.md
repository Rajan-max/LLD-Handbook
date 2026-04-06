# Structural Design Patterns 🏗️

A comprehensive guide to all major Structural Design Patterns with detailed examples, comparisons, and real-world use cases.

---

## 📚 Table of Contents

1. [Overview](#-overview)
2. [Pattern Summary](#-pattern-summary)
3. [Detailed Patterns](#-detailed-patterns)
4. [Pattern Comparison](#-pattern-comparison)
5. [When to Use Which Pattern](#-when-to-use-which-pattern)
6. [Real-World Applications](#-real-world-applications)
7. [Learning Path](#-learning-path)
8. [Practice Exercises](#-practice-exercises)

---

## 🎯 Overview

**Structural Design Patterns** deal with object composition and relationships between entities. They help ensure that if one part of a system changes, the entire system doesn't need to change.

### What are Structural Patterns?

Structural patterns explain how to assemble objects and classes into larger structures while keeping these structures flexible and efficient.

### Why Use Structural Patterns?

- ✅ **Flexibility** - Easy to modify and extend systems
- ✅ **Reusability** - Promote code reuse
- ✅ **Maintainability** - Easier to maintain and understand
- ✅ **Decoupling** - Reduce dependencies between components
- ✅ **Simplification** - Simplify complex relationships

---

## 📋 Pattern Summary

| Pattern | Purpose | Key Benefit | Use Case |
|---------|---------|-------------|----------|
| **Adapter** 🔌 | Convert interface | Compatibility | Integrate incompatible interfaces |
| **Decorator** 🎨 | Add responsibilities | Enhancement | Add features dynamically |
| **Proxy** 🛡️ | Control access | Access control | Lazy loading, caching, security |
| **Facade** 🎭 | Simplify interface | Simplification | Hide complex subsystems |
| **Composite** 🌳 | Tree structures | Uniform treatment | Files/folders, UI components |
| **Bridge** 🌉 | Decouple abstraction | Avoid explosion | Separate what from how |

---

## 📖 Detailed Patterns

### 1. Adapter Pattern 🔌

**Definition:** Converts the interface of a class into another interface clients expect.

**Problem:** Incompatible interfaces prevent integration

**Solution:** Adapter bridges the gap between incompatible interfaces

**Example:**
```java
// Target interface
interface MediaPlayer {
    void play(String filename);
}

// Adaptee (incompatible)
class MP3Player {
    void playMP3(String file) { }
}

// Adapter
class MediaAdapter implements MediaPlayer {
    private MP3Player mp3Player;
    
    public void play(String filename) {
        mp3Player.playMP3(filename);
    }
}
```

**Real-World Examples:**
- Payment gateway integration (PayPal, Stripe)
- Database drivers (MySQL, PostgreSQL)
- Legacy system integration
- Third-party library integration

**Key Takeaway:** Adapter makes incompatible interfaces work together

---

### 2. Decorator Pattern 🎨

**Definition:** Attaches additional responsibilities to an object dynamically.

**Problem:** Need to add features without modifying existing code

**Solution:** Wrap objects with decorator objects that add new behavior

**Example:**
```java
interface Coffee {
    double cost();
    String description();
}

class SimpleCoffee implements Coffee {
    public double cost() { return 5.0; }
    public String description() { return "Simple Coffee"; }
}

class MilkDecorator implements Coffee {
    private Coffee coffee;
    
    public MilkDecorator(Coffee coffee) {
        this.coffee = coffee;
    }
    
    public double cost() {
        return coffee.cost() + 1.5;
    }
    
    public String description() {
        return coffee.description() + ", Milk";
    }
}
```

**Real-World Examples:**
- Coffee shop (add milk, sugar, whip)
- Text formatting (bold, italic, underline)
- Data streams (encryption, compression)
- UI components (borders, scrollbars)

**Key Takeaway:** Decorator adds responsibilities dynamically without inheritance

---

### 3. Proxy Pattern 🛡️

**Definition:** Provides a surrogate or placeholder for another object to control access.

**Problem:** Need to control access to expensive or sensitive objects

**Solution:** Proxy controls access and adds functionality (lazy loading, caching, security)

**Example:**
```java
interface Image {
    void display();
}

class RealImage implements Image {
    private String filename;
    
    public RealImage(String filename) {
        loadFromDisk(filename); // Expensive!
    }
    
    public void display() {
        System.out.println("Displaying: " + filename);
    }
}

class ImageProxy implements Image {
    private String filename;
    private RealImage realImage;
    
    public ImageProxy(String filename) {
        this.filename = filename;
    }
    
    public void display() {
        if (realImage == null) {
            realImage = new RealImage(filename); // Lazy load
        }
        realImage.display();
    }
}
```

**Types of Proxies:**
- **Virtual Proxy** - Lazy loading
- **Protection Proxy** - Access control
- **Cache Proxy** - Performance optimization
- **Remote Proxy** - Network calls
- **Logging Proxy** - Monitoring

**Real-World Examples:**
- Image lazy loading
- Database connection pooling
- Security access control
- Remote method invocation (RMI)

**Key Takeaway:** Proxy controls access to objects

---

### 4. Facade Pattern 🎭

**Definition:** Provides a unified, simplified interface to a complex subsystem.

**Problem:** Complex subsystems with many classes are hard to use

**Solution:** Facade provides simple interface to complex subsystem

**Example:**
```java
// Complex subsystem
class Amplifier { void on() { } }
class DVDPlayer { void play(String movie) { } }
class Projector { void on() { } }
class Lights { void dim(int level) { } }

// Facade
class HomeTheaterFacade {
    private Amplifier amp;
    private DVDPlayer dvd;
    private Projector projector;
    private Lights lights;
    
    public void watchMovie(String movie) {
        lights.dim(10);
        projector.on();
        amp.on();
        dvd.play(movie);
    }
}
```

**Real-World Examples:**
- Home theater system
- Computer startup
- Order processing
- Hotel booking system

**Key Takeaway:** Facade simplifies complex subsystems

---

### 5. Composite Pattern 🌳

**Definition:** Composes objects into tree structures to represent part-whole hierarchies.

**Problem:** Need to treat individual and composite objects uniformly

**Solution:** Common interface for leaf and composite objects

**Example:**
```java
interface FileSystemComponent {
    void display();
    int getSize();
}

class File implements FileSystemComponent {
    private String name;
    private int size;
    
    public void display() {
        System.out.println("File: " + name);
    }
    
    public int getSize() {
        return size;
    }
}

class Folder implements FileSystemComponent {
    private String name;
    private List<FileSystemComponent> children;
    
    public void display() {
        System.out.println("Folder: " + name);
        for (FileSystemComponent child : children) {
            child.display(); // Recursive!
        }
    }
    
    public int getSize() {
        int total = 0;
        for (FileSystemComponent child : children) {
            total += child.getSize(); // Recursive!
        }
        return total;
    }
}
```

**Real-World Examples:**
- File systems (files and folders)
- Organization hierarchy (employees and managers)
- UI components (panels and widgets)
- Menu systems (items and submenus)

**Key Takeaway:** Composite treats individual and groups uniformly

---

### 6. Bridge Pattern 🌉

**Definition:** Decouples an abstraction from its implementation so both can vary independently.

**Problem:** Class explosion with multiple dimensions of variation

**Solution:** Separate abstraction from implementation using composition

**Example:**
```java
// Implementation
interface Color {
    String fill();
}

class Red implements Color {
    public String fill() { return "Red"; }
}

// Abstraction
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
    
    public void draw() {
        System.out.println("Circle in " + color.fill());
    }
}

// Usage: 3 shapes + 3 colors = 6 classes (not 9!)
Shape redCircle = new Circle(new Red());
```

**Real-World Examples:**
- Remote control and devices
- Notification system (type + channel)
- Database and platform (DB + driver)
- Shape and color

**Key Takeaway:** Bridge avoids class explosion (m + n instead of m × n)

---

## 🔄 Pattern Comparison

### Adapter vs Decorator vs Proxy

| Aspect | Adapter | Decorator | Proxy |
|--------|---------|-----------|-------|
| **Purpose** | Convert interface | Add responsibilities | Control access |
| **Interface** | Different interface | Same interface | Same interface |
| **Focus** | Compatibility | Enhancement | Access control |
| **Example** | MP3 to MediaPlayer | Coffee + Milk | Image lazy loading |

### Facade vs Proxy

| Aspect | Facade | Proxy |
|--------|--------|-------|
| **Purpose** | Simplify subsystem | Control access |
| **Complexity** | Many classes | Single class |
| **Interface** | New simplified | Same as original |
| **Example** | Home theater | Image proxy |

### Composite vs Decorator

| Aspect | Composite | Decorator |
|--------|-----------|-----------|
| **Purpose** | Tree structures | Add responsibilities |
| **Structure** | Tree (one-to-many) | Chain (one-to-one) |
| **Focus** | Part-whole hierarchy | Enhancement |
| **Example** | File system | Coffee decorators |

### Bridge vs Adapter

| Aspect | Bridge | Adapter |
|--------|--------|---------|
| **Purpose** | Decouple hierarchies | Convert interface |
| **Intent** | Design upfront | Fix existing code |
| **Structure** | Two hierarchies | Wrap existing |
| **Example** | Shape + Color | Legacy integration |

---

## 🤔 When to Use Which Pattern?

### Use Adapter when:
- ✅ Need to integrate incompatible interfaces
- ✅ Working with third-party libraries
- ✅ Legacy system integration
- ✅ Want to reuse existing classes

### Use Decorator when:
- ✅ Need to add responsibilities dynamically
- ✅ Want to avoid subclass explosion
- ✅ Need flexible feature combinations
- ✅ Want to follow Open/Closed Principle

### Use Proxy when:
- ✅ Need lazy initialization
- ✅ Want to control access (security)
- ✅ Need caching for performance
- ✅ Working with remote objects
- ✅ Want to add logging/monitoring

### Use Facade when:
- ✅ Complex subsystem with many classes
- ✅ Want to provide simple interface
- ✅ Need to decouple client from subsystem
- ✅ Want to layer your application

### Use Composite when:
- ✅ Need to represent part-whole hierarchies
- ✅ Want to treat individual and groups uniformly
- ✅ Building tree structures
- ✅ Need recursive operations

### Use Bridge when:
- ✅ Want to avoid class explosion
- ✅ Both abstraction and implementation should vary
- ✅ Need runtime binding
- ✅ Have two dimensions of variation

---

## 🏭 Real-World Applications

### Java Standard Library

| Pattern | Java Examples |
|---------|---------------|
| **Adapter** | `InputStreamReader`, `Arrays.asList()` |
| **Decorator** | `BufferedReader`, `FilterInputStream` |
| **Proxy** | `java.lang.reflect.Proxy`, RMI |
| **Facade** | `javax.faces.context.FacesContext` |
| **Composite** | `java.awt.Component`, `javax.swing.JComponent` |
| **Bridge** | JDBC (`DriverManager` + drivers) |

### Frameworks

| Pattern | Framework Examples |
|---------|-------------------|
| **Adapter** | Spring adapters, Hibernate adapters |
| **Decorator** | Spring AOP, Servlet filters |
| **Proxy** | Hibernate lazy loading, Spring proxies |
| **Facade** | Spring `ApplicationContext`, SLF4J |
| **Composite** | React/Vue components, Android Views |
| **Bridge** | JPA (abstraction) + Hibernate (implementation) |

---

## 🎓 Learning Path

### Beginner Level
1. **Start with Adapter** - Easiest to understand
2. **Then Facade** - Simple concept, practical use
3. **Then Proxy** - Builds on simple concepts

### Intermediate Level
4. **Learn Decorator** - More complex, powerful pattern
5. **Learn Composite** - Tree structures, recursion

### Advanced Level
6. **Master Bridge** - Most complex, requires careful design

### Practice Order
```
Adapter → Facade → Proxy → Decorator → Composite → Bridge
  ↓         ↓        ↓         ↓          ↓          ↓
Easy    Simple   Medium   Complex   Advanced   Expert
```

---

## 🏋️ Practice Exercises

### Exercise 1: Adapter Pattern
**Challenge:** Create a universal payment system that works with PayPal, Stripe, and Square APIs.

**Requirements:**
- Common `PaymentProcessor` interface
- Adapters for each payment gateway
- Support payment, refund, and status check

---

### Exercise 2: Decorator Pattern
**Challenge:** Create a notification system with multiple decorators.

**Requirements:**
- Base notification (email)
- Decorators: SMS, Push, Slack, Facebook
- Stack multiple decorators
- Calculate total cost

---

### Exercise 3: Proxy Pattern
**Challenge:** Create a document management system with different proxy types.

**Requirements:**
- Virtual proxy for lazy loading
- Protection proxy for access control
- Cache proxy for frequently accessed documents
- Logging proxy for audit trail

---

### Exercise 4: Facade Pattern
**Challenge:** Create a smart home system facade.

**Requirements:**
- Control lights, thermostat, security, entertainment
- Modes: Morning, Away, Night, Party
- Each mode configures multiple devices
- Simple interface for complex operations

---

### Exercise 5: Composite Pattern
**Challenge:** Create a graphics drawing system.

**Requirements:**
- Basic shapes: Circle, Rectangle, Line
- Group shapes together
- Operations: draw(), move(), resize()
- Groups can contain shapes and other groups

---

### Exercise 6: Bridge Pattern
**Challenge:** Create a messaging system with bridge pattern.

**Requirements:**
- Message types: Text, Image, Video
- Platforms: WhatsApp, Telegram, Signal
- Each message type works with any platform
- Avoid class explosion

---

## 📊 Pattern Selection Guide

```
Need to integrate incompatible interfaces?
    → Use ADAPTER

Need to add features dynamically?
    → Use DECORATOR

Need to control access to objects?
    → Use PROXY

Need to simplify complex subsystem?
    → Use FACADE

Need to build tree structures?
    → Use COMPOSITE

Need to avoid class explosion?
    → Use BRIDGE
```

---

## 🎯 Key Takeaways

### Remember the Mnemonics:

- **Adapter** = **C**onvert (Compatibility)
- **Decorator** = **E**nhance (Enhancement)
- **Proxy** = **C**ontrol (Control access)
- **Facade** = **S**implify (Simplification)
- **Composite** = **T**ree (Tree structures)
- **Bridge** = **D**ecouple (Decouple hierarchies)

### Quick Reference:

| Need | Pattern |
|------|---------|
| Make incompatible work together | Adapter |
| Add features without modifying | Decorator |
| Lazy load or cache | Proxy |
| Hide complexity | Facade |
| Treat individual and groups same | Composite |
| Separate what from how | Bridge |

---

## 🔗 Pattern Relationships

```
Adapter ←→ Bridge (Both use composition, different intent)
Decorator ←→ Composite (Both use recursion, different structure)
Proxy ←→ Decorator (Same interface, different purpose)
Facade ←→ Adapter (Both simplify, different scope)
```

---

## ✅ Checklist for Mastery

- [ ] Understand all 6 structural patterns
- [ ] Know when to use each pattern
- [ ] Can identify patterns in existing code
- [ ] Can implement each pattern from scratch
- [ ] Understand pattern trade-offs
- [ ] Can combine patterns effectively
- [ ] Recognize patterns in frameworks
- [ ] Can explain patterns to others

---