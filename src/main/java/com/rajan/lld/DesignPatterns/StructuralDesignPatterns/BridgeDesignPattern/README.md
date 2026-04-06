# Bridge Design Pattern 🌉

## 📖 Definition

The **Bridge Pattern** is a structural design pattern that decouples an abstraction from its implementation so that the two can vary independently.

**In Simple Terms:**
- Separate "what" (abstraction) from "how" (implementation)
- Two hierarchies that can evolve independently
- Composition over inheritance
- Like a remote control (abstraction) that works with any device (implementation)

---

## 🎯 Core Concept

```
Abstraction ──uses──> Implementation
     │                      │
     │                      │
Refined                 Concrete
Abstraction            Implementation
```

Key participants:
1. **Abstraction** - High-level control (what to do)
2. **Implementation** - Low-level operations (how to do)
3. **Refined Abstraction** - Extended abstraction
4. **Concrete Implementation** - Specific implementation

---

## ❌ Problem: Class Explosion with Multiple Dimensions

### Without Bridge Pattern:

```java
// Need separate class for each combination!
class RedCircle { }
class BlueCircle { }
class GreenCircle { }
class RedSquare { }
class BlueSquare { }
class GreenSquare { }
class RedTriangle { }
class BlueTriangle { }
class GreenTriangle { }

// 3 shapes × 3 colors = 9 classes!
// Adding 1 new color = 3 new classes!
// Adding 1 new shape = 3 new classes!
```

### Issues:
- ❌ Cartesian product of classes (m × n classes)
- ❌ Tight coupling between abstraction and implementation
- ❌ Hard to add new abstractions or implementations
- ❌ Code duplication across similar classes
- ❌ Violates Single Responsibility Principle
- ❌ Inheritance hierarchy becomes unmanageable

---

## ✅ Solution: Bridge Pattern

### Step-by-Step Implementation:

#### Step 1: Implementation Interface
```java
interface Color {
    String fill();
}
```

#### Step 2: Concrete Implementations
```java
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
```

#### Step 3: Abstraction (uses implementation)
```java
abstract class Shape {
    protected Color color; // Bridge to implementation
    
    public Shape(Color color) {
        this.color = color;
    }
    
    public abstract void draw();
}
```

#### Step 4: Refined Abstraction
```java
class Circle extends Shape {
    public Circle(Color color) {
        super(color);
    }
    
    @Override
    public void draw() {
        System.out.println("Drawing Circle in " + color.fill());
    }
}

class Square extends Shape {
    public Square(Color color) {
        super(color);
    }
    
    @Override
    public void draw() {
        System.out.println("Drawing Square in " + color.fill());
    }
}
```

#### Step 5: Client Usage
```java
// Create any combination at runtime!
Shape redCircle = new Circle(new Red());
Shape blueSquare = new Square(new Blue());
Shape greenCircle = new Circle(new Green());

redCircle.draw();   // Drawing Circle in Red
blueSquare.draw();  // Drawing Square in Blue

// 3 shapes + 3 colors = only 6 classes (not 9)!
```

---

## 🔄 Before vs After Comparison

| Aspect | Without Bridge ❌ | With Bridge ✅ |
|--------|------------------|----------------|
| **Class Count** | m × n classes | m + n classes |
| **Coupling** | Tight coupling | Loose coupling |
| **Flexibility** | Hard to extend | Easy to extend |
| **Code Duplication** | High duplication | Minimal duplication |
| **Runtime Binding** | Fixed at compile time | Can change at runtime |
| **Maintainability** | Hard to maintain | Easy to maintain |
| **Example** | 9 classes (3×3) | 6 classes (3+3) |

---

## 🌍 Real-World Use Cases

### 1. Remote Control & Devices 🎮
**Problem:** Different remotes for different devices  
**Solution:** Remote (abstraction) works with any device (implementation)

```java
Device tv = new TV();
RemoteControl remote = new RemoteControl(tv);
remote.togglePower();

// Same remote works with radio
Device radio = new Radio();
remote = new RemoteControl(radio);
remote.togglePower();
```

**Benefit:** Add new remote type or device without affecting the other

---

### 2. Notification System 📧
**Problem:** Different notification types via different channels  
**Solution:** Notification (abstraction) uses any sender (implementation)

```java
MessageSender email = new EmailSender();
Notification alert = new AlertNotification(email);
alert.notify("Server down!");

// Same alert via SMS
MessageSender sms = new SMSSender();
alert = new AlertNotification(sms);
alert.notify("Server down!");
```

**Benefit:** Add new notification type or sender independently

---

### 3. Database & Platform 🗄️
**Problem:** Different database types on different platforms  
**Solution:** Database (abstraction) uses any driver (implementation)

```java
DatabaseDriver mysql = new MySQLDriver();
Database prodDB = new ProductionDatabase(mysql);
prodDB.query("SELECT * FROM users");

// Same production DB with PostgreSQL
DatabaseDriver postgres = new PostgreSQLDriver();
prodDB = new ProductionDatabase(postgres);
prodDB.query("SELECT * FROM users");
```

**Benefit:** Switch database platform without changing database logic

---

### 4. Shape & Color 🎨
**Problem:** Every shape-color combination needs a class  
**Solution:** Shape (abstraction) uses color (implementation)

```java
Color red = new Red();
Shape circle = new Circle(red);
circle.draw();

// Easy to create any combination
Shape blueSquare = new Square(new Blue());
blueSquare.draw();
```

**Benefit:** 3 shapes + 3 colors = 6 classes (not 9)

---

## 🏭 Industry Examples

### Java Standard Library:
1. **JDBC** - DriverManager (abstraction) + Database drivers (implementation)
2. **Collections** - AbstractList (abstraction) + ArrayList/LinkedList (implementation)
3. **AWT/Swing** - Component hierarchy

### Frameworks:
1. **Logging Frameworks** - SLF4J (abstraction) + Logback/Log4j (implementation)
2. **Persistence** - JPA (abstraction) + Hibernate/EclipseLink (implementation)
3. **Messaging** - JMS (abstraction) + ActiveMQ/RabbitMQ (implementation)

### Real Applications:
1. **Graphics Systems** - Shape + Rendering engine
2. **UI Frameworks** - Widget + Platform (Windows/Mac/Linux)
3. **Payment Systems** - Payment method + Gateway
4. **Cloud Services** - Service + Provider (AWS/Azure/GCP)

---

## ✅ Advantages

| Advantage | Description |
|-----------|-------------|
| **Decoupling** | Abstraction and implementation vary independently |
| **Avoid Class Explosion** | m + n classes instead of m × n |
| **Flexibility** | Easy to add new abstractions or implementations |
| **Runtime Binding** | Can switch implementation at runtime |
| **Single Responsibility** | Separate concerns clearly |
| **Open/Closed** | Extend without modifying existing code |
| **Platform Independence** | Abstract platform-specific details |

---

## ⚠️ Disadvantages

| Disadvantage | Description |
|--------------|-------------|
| **Complexity** | More classes and interfaces |
| **Indirection** | Extra layer of abstraction |
| **Design Effort** | Requires careful planning |
| **Overkill** | May be too complex for simple cases |

---

## 🤔 When to Use

✅ **Use Bridge Pattern when:**
- Want to avoid permanent binding between abstraction and implementation
- Both abstraction and implementation should be extensible
- Changes in implementation shouldn't affect clients
- Want to share implementation among multiple objects
- Avoid class explosion from multiple dimensions
- Need platform independence

❌ **Don't use when:**
- Only one implementation exists
- Abstraction and implementation won't vary
- Adding unnecessary complexity
- Simple inheritance is sufficient

---

## 🆚 Bridge vs Similar Patterns

### Bridge vs Adapter

| Aspect | Bridge | Adapter |
|--------|--------|---------|
| **Purpose** | Decouple abstraction from implementation | Convert interface |
| **Intent** | Design upfront | Fix existing code |
| **Structure** | Two hierarchies | Wrap existing class |
| **Focus** | Flexibility | Compatibility |
| **Example** | Remote + Device | MP3 to MediaPlayer |

### Bridge vs Strategy

| Aspect | Bridge | Strategy |
|--------|--------|----------|
| **Purpose** | Separate abstraction from implementation | Encapsulate algorithms |
| **Structure** | Two hierarchies | Single hierarchy |
| **Focus** | Structural | Behavioral |
| **Relationship** | Has-a (composition) | Uses-a (delegation) |
| **Example** | Shape + Color | Payment + Method |

### Bridge vs Abstract Factory

| Aspect | Bridge | Abstract Factory |
|--------|--------|------------------|
| **Purpose** | Decouple hierarchies | Create families of objects |
| **Focus** | Structure | Creation |
| **Relationship** | Composition | Factory methods |
| **Flexibility** | Runtime switching | Family consistency |
| **Example** | Remote + Device | UI + Theme |

---

## 💡 Implementation Tips

### 1. Identify Two Dimensions
```java
// Dimension 1: Abstraction (What)
// - BasicRemote, AdvancedRemote

// Dimension 2: Implementation (How)
// - TV, Radio, SmartTV

// Bridge connects them via composition
```

### 2. Use Composition, Not Inheritance
```java
// Bad: Inheritance
class TVRemote extends TV { }

// Good: Composition (Bridge)
class Remote {
    private Device device; // Bridge!
}
```

### 3. Keep Implementation Interface Simple
```java
// Good: Simple, focused interface
interface Device {
    void turnOn();
    void turnOff();
    void setVolume(int volume);
}

// Bad: Too many methods
interface Device {
    void turnOn();
    void turnOff();
    void setVolume(int volume);
    void setChannel(int channel);
    void setBrightness(int level);
    // ... 20 more methods
}
```

### 4. Consider Factory for Creation
```java
class RemoteFactory {
    public static Remote createRemote(String type, Device device) {
        if (type.equals("basic")) {
            return new BasicRemote(device);
        } else {
            return new AdvancedRemote(device);
        }
    }
}
```

---

## 🏋️ Practice Exercise

**Challenge:** Create a Drawing Application

**Requirements:**
1. **Shapes**: Circle, Rectangle, Triangle
2. **Renderers**: VectorRenderer, RasterRenderer
3. Operations: draw(), resize()
4. Each shape can use any renderer
5. Add new shape or renderer easily

**Example:**
```java
Renderer vector = new VectorRenderer();
Shape circle = new Circle(vector, 5);
circle.draw();

Renderer raster = new RasterRenderer();
Shape rect = new Rectangle(raster, 10, 20);
rect.draw();
```

**Bonus:**
- Add 3D renderer
- Add polygon shape
- Support multiple colors
- Add animation

---

## 🎯 Key Takeaways

1. **Bridge separates abstraction from implementation**
2. **Two hierarchies** that vary independently
3. **Composition over inheritance**
4. **Avoid class explosion** (m + n instead of m × n)
5. **Runtime flexibility** - switch implementation dynamically
6. **Common in drivers** (JDBC, logging, persistence)
7. **Use when** you have two dimensions of variation

---

## 📊 Bridge Pattern Structure

```
┌──────────────┐
│ Abstraction  │───────uses────────┐
└──────┬───────┘                   │
       │                           │
       │ extends                   │
       │                           ▼
┌──────▼───────┐          ┌────────────────┐
│  Refined     │          │ Implementation │
│ Abstraction  │          │   (Interface)  │
└──────────────┘          └────────┬───────┘
                                   │
                          ┌────────┴────────┐
                          │                 │
                    ┌─────▼──────┐   ┌─────▼──────┐
                    │  Concrete  │   │  Concrete  │
                    │    Impl A  │   │    Impl B  │
                    └────────────┘   └────────────┘
```

---

## 📚 Related Patterns

- **Adapter** - Convert interfaces (fix existing code)
- **Strategy** - Encapsulate algorithms (behavioral)
- **Abstract Factory** - Create families of objects
- **State** - Change behavior based on state

---

## 🔗 Next Steps

1. ✅ Identify two dimensions of variation
2. ✅ Separate abstraction from implementation
3. ✅ Use composition instead of inheritance
4. ✅ Calculate class savings (m + n vs m × n)
5. ✅ Practice with JDBC, logging frameworks
6. ✅ Combine with Factory for object creation

---

**Remember:** Bridge is for **two hierarchies**, Adapter is for **interface conversion**! 🌉
