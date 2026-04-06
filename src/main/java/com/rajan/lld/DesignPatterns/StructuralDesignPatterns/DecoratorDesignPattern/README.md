# Decorator Design Pattern

## 📖 Definition

The **Decorator Pattern** attaches additional responsibilities to an object dynamically. Decorators provide a flexible alternative to subclassing for extending functionality.

**In simple terms**: Wrap objects like gifts - each wrapper adds a new feature without changing the original object!

## 🎯 Core Concept

The Decorator pattern:
- Wraps an object to add new behavior
- Maintains the same interface as the wrapped object
- Can stack multiple decorators
- Adds features dynamically at runtime
- Avoids subclass explosion

**Key Components**:
1. **Component Interface**: Defines operations for objects
2. **Concrete Component**: Basic object to be decorated
3. **Base Decorator**: Wraps a component and maintains interface
4. **Concrete Decorators**: Add specific responsibilities

## ❌ Problem: Without Decorator Pattern

### The Subclass Explosion Problem

```java
// Without decorator: Need class for each combination
class PlainCoffee { }
class CoffeeWithMilk { }
class CoffeeWithSugar { }
class CoffeeWithMilkAndSugar { }
class CoffeeWithMilkSugarAndWhip { }
class CoffeeWithMilkSugarWhipAndCaramel { }
// ... and so on

// Problem: 
// 3 add-ons = 8 classes (2^3)
// 5 add-ons = 32 classes (2^5)
// 10 add-ons = 1024 classes (2^10)
```

### Why This Is Bad:

| Problem | Description |
|---------|-------------|
| **Subclass Explosion** | Exponential growth: n features = 2^n classes |
| **Inflexible** | Can't add features at runtime |
| **Code Duplication** | Similar code repeated across classes |
| **Hard to Maintain** | Changes affect many classes |
| **Violates Open/Closed** | Must create new class for each combination |
| **Not Composable** | Can't mix and match features easily |

## ✅ Solution: Decorator Pattern

### Step-by-Step Implementation

**Step 1: Define Component Interface**
```java
interface Coffee {
    String getDescription();
    double cost();
}
```

**Step 2: Create Concrete Component**
```java
class SimpleCoffee implements Coffee {
    public String getDescription() {
        return "Simple Coffee";
    }
    
    public double cost() {
        return 2.0;
    }
}
```

**Step 3: Create Base Decorator**
```java
abstract class CoffeeDecorator implements Coffee {
    protected Coffee decoratedCoffee;
    
    public CoffeeDecorator(Coffee coffee) {
        this.decoratedCoffee = coffee;
    }
    
    public String getDescription() {
        return decoratedCoffee.getDescription();
    }
    
    public double cost() {
        return decoratedCoffee.cost();
    }
}
```

**Step 4: Create Concrete Decorators**
```java
class MilkDecorator extends CoffeeDecorator {
    public MilkDecorator(Coffee coffee) {
        super(coffee);
    }
    
    public String getDescription() {
        return decoratedCoffee.getDescription() + ", Milk";
    }
    
    public double cost() {
        return decoratedCoffee.cost() + 0.5;
    }
}

class SugarDecorator extends CoffeeDecorator {
    public SugarDecorator(Coffee coffee) {
        super(coffee);
    }
    
    public String getDescription() {
        return decoratedCoffee.getDescription() + ", Sugar";
    }
    
    public double cost() {
        return decoratedCoffee.cost() + 0.3;
    }
}
```

**Step 5: Use the Pattern**
```java
// Stack decorators dynamically
Coffee coffee = new SugarDecorator(
    new MilkDecorator(new SimpleCoffee())
);
// Result: "Simple Coffee, Milk, Sugar" - $2.8
```

## 📊 Comparison: Before vs After

| Aspect | Without Decorator | With Decorator |
|--------|-------------------|----------------|
| **Classes needed** | 2^n (exponential) | n + 1 (linear) |
| **Runtime flexibility** | ❌ No | ✅ Yes |
| **Code duplication** | High | Low |
| **Maintainability** | Low | High |
| **Open/Closed Principle** | ❌ Violated | ✅ Followed |
| **Feature combination** | Fixed at compile-time | Dynamic at runtime |
| **Adding new feature** | Create many classes | Create one decorator |

## 🌍 Real-World Use Cases

### 1. Coffee Shop
```java
// Base: SimpleCoffee
// Decorators: Milk, Sugar, Whip, Caramel, Vanilla
Coffee deluxe = new CaramelDecorator(
    new WhipDecorator(
        new MilkDecorator(new SimpleCoffee())
    )
);
```

### 2. Text Formatting
```java
// Base: PlainText
// Decorators: Bold, Italic, Underline, Color
Text formatted = new UnderlineDecorator(
    new ItalicDecorator(
        new BoldDecorator(new PlainText("Hello"))
    )
);
// Result: <u><i><b>Hello</b></i></u>
```

### 3. Data Source (I/O Streams)
```java
// Base: FileDataSource
// Decorators: Encryption, Compression, Buffering
DataSource secure = new CompressionDecorator(
    new EncryptionDecorator(new FileDataSource("data.txt"))
);
```

### 4. Notification System
```java
// Base: EmailNotifier
// Decorators: SMS, Slack, Facebook, WhatsApp
Notifier multiChannel = new SlackDecorator(
    new SMSDecorator(new EmailNotifier())
);
```

### 5. UI Components
```java
// Base: Window
// Decorators: ScrollBar, Border, Shadow, Tooltip
Window decorated = new ShadowDecorator(
    new BorderDecorator(
        new ScrollBarDecorator(new SimpleWindow())
    )
);
```

### 6. HTTP Request/Response
```java
// Base: HttpRequest
// Decorators: Authentication, Logging, Caching, Retry
HttpRequest request = new RetryDecorator(
    new CachingDecorator(
        new LoggingDecorator(new BasicHttpRequest())
    )
);
```

## 💼 Industry Examples

| Application | Decorator Use |
|-------------|---------------|
| **Java I/O** | BufferedInputStream, GZIPInputStream wrapping FileInputStream |
| **Web Frameworks** | Middleware layers (authentication, logging, compression) |
| **UI Frameworks** | Component decorators (borders, scrollbars, shadows) |
| **Logging** | Log decorators (timestamp, level, formatting) |
| **Security** | Encryption, authentication layers |
| **Caching** | Cache decorators for data sources |

## ✅ Advantages

1. **Flexible Extension**: Add responsibilities without modifying code
2. **Runtime Configuration**: Combine features dynamically
3. **Avoids Subclass Explosion**: Linear growth instead of exponential
4. **Single Responsibility**: Each decorator has one job
5. **Open/Closed Principle**: Open for extension, closed for modification
6. **Composable**: Stack multiple decorators
7. **Transparent**: Decorators maintain same interface

## ❌ Disadvantages

1. **Complexity**: Many small objects can be confusing
2. **Order Matters**: Decorator order affects behavior
3. **Identity Issues**: Decorated object != original object
4. **Debugging Difficulty**: Hard to trace through decorator layers
5. **Instantiation Complexity**: Verbose object creation

## 🎓 When to Use

### ✅ Use Decorator Pattern When:
- Need to add responsibilities to objects dynamically
- Extension by subclassing is impractical
- Want to add features without modifying existing code
- Need different combinations of features
- Features can be added/removed at runtime
- Want to avoid subclass explosion

### ❌ Avoid Decorator Pattern When:
- Only one or two simple extensions needed
- Order of decorators doesn't matter (consider other patterns)
- Need to remove decorators frequently
- Component interface is unstable
- Simple inheritance is sufficient

## 🔄 Decorator vs Other Patterns

### Decorator vs Adapter
| Aspect | Decorator | Adapter |
|--------|-----------|---------|
| **Purpose** | Add responsibilities | Change interface |
| **Interface** | Same as component | Different interface |
| **Wrapping** | Can stack multiple | Usually single |
| **Intent** | Enhance behavior | Make compatible |
| **Example** | Add encryption | Convert API |

### Decorator vs Proxy
| Aspect | Decorator | Proxy |
|--------|-----------|-------|
| **Purpose** | Add functionality | Control access |
| **Composition** | Stack multiple | Usually single |
| **Transparency** | Client aware | Client unaware |
| **Focus** | Enhancement | Access control |
| **Example** | Add logging | Lazy loading |

### Decorator vs Composite
| Aspect | Decorator | Composite |
|--------|-----------|-----------|
| **Purpose** | Add behavior | Tree structure |
| **Wrapping** | Single object | Multiple objects |
| **Responsibility** | Enhancement | Aggregation |
| **Example** | Text formatting | File system |

## 💡 Implementation Tips

1. **Keep Interface Consistent**: Decorators must implement same interface
2. **Base Decorator**: Create abstract base decorator for common code
3. **Order Matters**: Document decorator order dependencies
4. **Avoid Deep Nesting**: Too many layers = complexity
5. **Consider Builder**: Use builder pattern for complex decorator chains
6. **Null Checks**: Handle null wrapped objects
7. **Immutability**: Consider making decorators immutable

### Decorator Chain Building

**Pattern 1: Direct Nesting**
```java
Coffee coffee = new CaramelDecorator(
    new WhipDecorator(
        new MilkDecorator(new SimpleCoffee())
    )
);
```

**Pattern 2: Step-by-Step**
```java
Coffee coffee = new SimpleCoffee();
coffee = new MilkDecorator(coffee);
coffee = new WhipDecorator(coffee);
coffee = new CaramelDecorator(coffee);
```

**Pattern 3: Builder Pattern**
```java
Coffee coffee = new CoffeeBuilder()
    .base(new SimpleCoffee())
    .addMilk()
    .addWhip()
    .addCaramel()
    .build();
```

## 🧪 Practice Exercise

### Challenge: Pizza Ordering System

Create a pizza ordering system using Decorator pattern.

**Requirements**:
- Base: PlainPizza ($8)
- Toppings: Cheese (+$1), Pepperoni (+$2), Mushrooms (+$1.5), Olives (+$1)
- Each topping is a decorator
- Calculate total price
- Get full description
- Support multiple toppings

**Hints**:
1. Create `Pizza` interface with `getDescription()` and `cost()` methods
2. Create `PlainPizza` as concrete component
3. Create `ToppingDecorator` as base decorator
4. Implement concrete decorators: `CheeseDecorator`, `PepperoniDecorator`, etc.
5. Test various combinations

<details>
<summary>💡 Solution Outline</summary>

```java
interface Pizza {
    String getDescription();
    double cost();
}

class PlainPizza implements Pizza {
    public String getDescription() {
        return "Plain Pizza";
    }
    public double cost() {
        return 8.0;
    }
}

abstract class ToppingDecorator implements Pizza {
    protected Pizza pizza;
    
    public ToppingDecorator(Pizza pizza) {
        this.pizza = pizza;
    }
}

class CheeseDecorator extends ToppingDecorator {
    public CheeseDecorator(Pizza pizza) {
        super(pizza);
    }
    
    public String getDescription() {
        return pizza.getDescription() + ", Cheese";
    }
    
    public double cost() {
        return pizza.cost() + 1.0;
    }
}

// Usage
Pizza myPizza = new PepperoniDecorator(
    new CheeseDecorator(
        new MushroomDecorator(new PlainPizza())
    )
);
```

</details>

## 🎯 Key Takeaways

1. **Wrap, Don't Modify**: Add behavior by wrapping objects
2. **Same Interface**: Decorators maintain component interface
3. **Stack Decorators**: Combine multiple decorators
4. **Runtime Flexibility**: Add/remove features dynamically
5. **Avoid Explosion**: Linear growth instead of exponential
6. **Open/Closed**: Extend without modifying existing code

## 📚 Related Patterns

- **Adapter**: Changes interface (Decorator keeps same interface)
- **Proxy**: Controls access (Decorator adds functionality)
- **Composite**: Tree structure (Decorator wraps single object)
- **Strategy**: Swaps algorithm (Decorator adds behavior)
- **Chain of Responsibility**: Can be implemented with decorators

## 🔗 Java Standard Library Examples

- `java.io.BufferedInputStream` wrapping `FileInputStream`
- `java.io.BufferedReader` wrapping `FileReader`
- `java.util.Collections.synchronizedList()`
- `java.util.Collections.unmodifiableList()`
- `javax.servlet.http.HttpServletRequestWrapper`
- `javax.servlet.http.HttpServletResponseWrapper`

## 🏗️ Decorator Structure

```
Decorator Pattern Structure:

Client → Decorator → Decorator → Component
           ↓           ↓            ↓
        Feature 1   Feature 2   Core Object

Example:
Client → Caramel → Whip → Milk → SimpleCoffee
         +$0.6     +$0.7  +$0.5   $2.0
         
Total: $4.3
Description: "Simple Coffee, Milk, Whip, Caramel"
```

---

## 🚀 Running the Demo

```bash
# Compile
mvn clean compile

# Run
mvn exec:java -Dexec.mainClass="com.rajan.lld.DesignPatterns.StructuralDesignPatterns.DecoratorDesignPattern.DecoratorDesignPattern"
```

---

**Remember**: Use Decorator Pattern when you need to add responsibilities to objects dynamically without creating a subclass for every combination. It's perfect for adding features in a flexible, composable way!
