# Strategy Design Pattern

## 📖 Definition

The **Strategy Pattern** defines a family of algorithms, encapsulates each one, and makes them interchangeable. Strategy lets the algorithm vary independently from clients that use it.

**In simple terms**: Different ways to solve the same problem - pick the best one for the situation and switch at runtime!

## 🎯 Core Concept

Instead of implementing a single algorithm directly, the code receives runtime instructions on which algorithm to use from a family of algorithms.

**Key Components**:
1. **Strategy Interface**: Defines the contract for all algorithms
2. **Concrete Strategies**: Different implementations of the algorithm
3. **Context**: Uses a strategy to execute the algorithm

## ❌ Problem: Without Strategy Pattern

### The Messy If-Else Chain Problem

```java
class BadPaymentProcessor {
    public void processPayment(String paymentType, double amount) {
        if (paymentType.equals("creditcard")) {
            // Credit card logic
            System.out.println("Processing credit card...");
            // 10 lines of code
        } else if (paymentType.equals("paypal")) {
            // PayPal logic
            System.out.println("Processing PayPal...");
            // 10 lines of code
        } else if (paymentType.equals("crypto")) {
            // Crypto logic
            System.out.println("Processing crypto...");
            // 10 lines of code
        }
        // Adding new payment method requires modifying this method!
    }
}
```

### Why This Is Bad:

| Problem | Description |
|---------|-------------|
| **Violates Open/Closed** | Must modify existing code to add new payment methods |
| **Hard to Maintain** | All logic in one place, becomes huge and complex |
| **Not Testable** | Must test all branches together |
| **No Runtime Flexibility** | Can't easily switch payment methods |
| **Code Duplication** | Similar patterns repeated in if-else blocks |
| **Tight Coupling** | Payment logic tightly coupled with processor |

## ✅ Solution: Strategy Pattern

### Step-by-Step Implementation

**Step 1: Define Strategy Interface**
```java
interface PaymentStrategy {
    void pay(double amount);
    double getTransactionFee(double amount);
}
```

**Step 2: Create Concrete Strategies**
```java
class CreditCardPayment implements PaymentStrategy {
    private String cardNumber;
    
    public CreditCardPayment(String cardNumber) {
        this.cardNumber = cardNumber;
    }
    
    @Override
    public void pay(double amount) {
        System.out.println("Charging $" + amount + " to credit card");
    }
    
    @Override
    public double getTransactionFee(double amount) {
        return amount * 0.03; // 3% fee
    }
}

class PayPalPayment implements PaymentStrategy {
    private String email;
    
    public PayPalPayment(String email) {
        this.email = email;
    }
    
    @Override
    public void pay(double amount) {
        System.out.println("Charging $" + amount + " via PayPal");
    }
    
    @Override
    public double getTransactionFee(double amount) {
        return amount * 0.025; // 2.5% fee
    }
}
```

**Step 3: Create Context Class**
```java
class PaymentProcessor {
    private PaymentStrategy strategy;
    
    public void setPaymentStrategy(PaymentStrategy strategy) {
        this.strategy = strategy;
    }
    
    public void processPayment(double amount) {
        strategy.pay(amount);
        double fee = strategy.getTransactionFee(amount);
        System.out.println("Transaction fee: $" + fee);
    }
}
```

**Step 4: Use the Pattern**
```java
PaymentProcessor processor = new PaymentProcessor();

// Customer chooses credit card
processor.setPaymentStrategy(new CreditCardPayment("1234-5678"));
processor.processPayment(100.0);

// Customer switches to PayPal
processor.setPaymentStrategy(new PayPalPayment("user@email.com"));
processor.processPayment(100.0);
```

## 📊 Comparison: Before vs After

| Aspect | Without Strategy | With Strategy |
|--------|------------------|---------------|
| **Adding new algorithm** | Modify existing class | Create new strategy class |
| **Testing** | Test entire method | Test each strategy independently |
| **Code organization** | All in one place | Separated by strategy |
| **Runtime flexibility** | Limited | Easy to switch |
| **Open/Closed Principle** | ❌ Violated | ✅ Followed |
| **Maintainability** | Low | High |
| **Code readability** | Poor (long if-else) | Excellent (clear separation) |

## 🌍 Real-World Use Cases

### 1. Payment Processing
```java
// Different payment methods
- CreditCardPayment
- PayPalPayment
- CryptoPayment
- BankTransferPayment
```

### 2. Shipping Calculation
```java
// Different shipping options
- StandardShipping (5-7 days)
- ExpressShipping (2-3 days)
- OvernightShipping (next day)
- DroneDelivery (same day)
```

### 3. Discount Strategies
```java
// Different discount types
- PercentageDiscount (20% off)
- FixedAmountDiscount ($10 off)
- SeasonalDiscount (holiday sale)
- LoyaltyDiscount (member rewards)
```

### 4. Sorting Algorithms
```java
// Different sorting strategies
- BubbleSort (small datasets)
- QuickSort (large datasets)
- MergeSort (stable sorting)
- HeapSort (memory constrained)
```

### 5. Compression Algorithms
```java
// Different compression strategies
- ZipCompression
- RarCompression
- GzipCompression
- NoCompression
```

### 6. Navigation Routes
```java
// Different route strategies
- FastestRoute
- ShortestRoute
- ScenicRoute
- EcoFriendlyRoute
```

## 💼 Industry Examples

| Application | Strategy Use |
|-------------|--------------|
| **E-commerce** | Payment methods, shipping options, pricing strategies |
| **Maps/GPS** | Route calculation (fastest, shortest, avoid tolls) |
| **Games** | AI behavior (aggressive, defensive, balanced) |
| **File Systems** | Compression algorithms, encryption methods |
| **Databases** | Query optimization strategies |
| **Social Media** | Content recommendation algorithms |

## ✅ Advantages

1. **Open/Closed Principle**: Open for extension, closed for modification
2. **Runtime Flexibility**: Switch algorithms on the fly
3. **Eliminates Conditionals**: No more messy if-else chains
4. **Easy Testing**: Test each strategy independently
5. **Code Reusability**: Strategies can be reused across different contexts
6. **Single Responsibility**: Each strategy has one job
7. **Maintainability**: Easy to add, modify, or remove strategies

## ❌ Disadvantages

1. **Increased Classes**: More classes to manage (one per strategy)
2. **Client Awareness**: Client must know about different strategies
3. **Communication Overhead**: Context and strategy must communicate
4. **Overkill for Simple Cases**: Too complex if you only have 2-3 simple algorithms

## 🎓 When to Use

### ✅ Use Strategy Pattern When:
- You have multiple algorithms for the same task
- You want to eliminate complex if-else or switch statements
- Algorithms need to be switched at runtime
- You want to hide implementation details of algorithms
- Different variants of an algorithm are needed
- You want to follow Open/Closed Principle

### ❌ Avoid Strategy Pattern When:
- You only have one or two simple algorithms
- Algorithms never change
- The overhead of creating multiple classes isn't justified
- Algorithms are tightly coupled with context data

## 🔄 Strategy vs Other Patterns

### Strategy vs State Pattern
| Aspect | Strategy | State |
|--------|----------|-------|
| **Purpose** | Choose algorithm | Change behavior based on state |
| **Who decides** | Client sets strategy | Object changes state internally |
| **Relationship** | Strategies independent | States know about each other |
| **Example** | Payment methods | Order status (pending → shipped) |

### Strategy vs Template Method
| Aspect | Strategy | Template Method |
|--------|----------|-----------------|
| **Mechanism** | Composition | Inheritance |
| **Flexibility** | Runtime switching | Compile-time |
| **Granularity** | Entire algorithm | Parts of algorithm |
| **Coupling** | Loose | Tight |

## 💡 Implementation Tips

1. **Use Interfaces**: Define clear contracts for strategies
2. **Keep Strategies Stateless**: When possible, make strategies reusable
3. **Consider Factory**: Use Factory pattern to create strategies
4. **Default Strategy**: Provide a sensible default strategy
5. **Validate Strategy**: Check if strategy is set before using
6. **Document Strategies**: Clearly document when to use each strategy

## 🧪 Practice Exercise

### Challenge: Notification System

Create a notification system that can send messages through different channels.

**Requirements**:
- Support Email, SMS, Push Notification, and Slack
- Each channel has different cost and delivery time
- User should be able to switch channels at runtime
- Add a new channel (WhatsApp) without modifying existing code

**Hints**:
1. Create `NotificationStrategy` interface with `send(String message)` method
2. Implement concrete strategies: `EmailNotification`, `SMSNotification`, etc.
3. Create `NotificationService` context class
4. Test switching between different notification channels

<details>
<summary>💡 Solution Outline</summary>

```java
interface NotificationStrategy {
    void send(String message);
    double getCost();
    String getDeliveryTime();
}

class EmailNotification implements NotificationStrategy {
    public void send(String message) {
        System.out.println("Sending email: " + message);
    }
    public double getCost() { return 0.01; }
    public String getDeliveryTime() { return "Instant"; }
}

class SMSNotification implements NotificationStrategy {
    public void send(String message) {
        System.out.println("Sending SMS: " + message);
    }
    public double getCost() { return 0.05; }
    public String getDeliveryTime() { return "Instant"; }
}

class NotificationService {
    private NotificationStrategy strategy;
    
    public void setStrategy(NotificationStrategy strategy) {
        this.strategy = strategy;
    }
    
    public void notify(String message) {
        strategy.send(message);
        System.out.println("Cost: $" + strategy.getCost());
    }
}
```

</details>

## 🎯 Key Takeaways

1. **Strategy = Interchangeable Algorithms**: Different ways to do the same thing
2. **Runtime Flexibility**: Switch algorithms on the fly
3. **Eliminates Conditionals**: Replace if-else chains with polymorphism
4. **Open/Closed**: Add new strategies without modifying existing code
5. **Composition over Inheritance**: Use object composition for flexibility

## 📚 Related Patterns

- **State Pattern**: Similar structure, but strategies don't know about each other
- **Factory Pattern**: Often used together to create strategies
- **Template Method**: Alternative using inheritance instead of composition
- **Command Pattern**: Encapsulates requests, Strategy encapsulates algorithms

## 🔗 Java Standard Library Examples

- `java.util.Comparator` - Different comparison strategies
- `java.util.Collections.sort()` - Accepts different comparators
- `javax.servlet.Filter` - Different filtering strategies
- `java.io.OutputStream` - Different output strategies

---

## 🚀 Running the Demo

```bash
# Compile
mvn clean compile

# Run
mvn exec:java -Dexec.mainClass="com.rajan.lld.DesignPatterns.BehavioralDesignPatterns.StrategyDesignPattern.StrategyDesignPattern"
```

---

**Remember**: Use Strategy Pattern when you need to switch between different algorithms at runtime. It's perfect for eliminating complex conditional logic and making your code more maintainable and extensible!
