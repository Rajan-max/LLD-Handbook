# Behavioral Design Patterns

## 📖 What Are Behavioral Design Patterns?

Behavioral design patterns are concerned with **algorithms and the assignment of responsibilities between objects**. They describe not just patterns of objects or classes but also the patterns of communication between them.

**In simple terms**: These patterns focus on how objects interact and communicate with each other, defining clear responsibilities and flexible communication mechanisms.

## 🎯 Why Are They Important?

1. **Clear Communication**: Define how objects interact and communicate
2. **Flexible Responsibilities**: Assign responsibilities in a flexible way
3. **Loose Coupling**: Reduce dependencies between objects
4. **Reusable Interactions**: Create reusable communication patterns
5. **Maintainable Code**: Make complex interactions easier to understand and modify

## 🔑 Core Concepts

### 1. **Object Interaction**
Focus on how objects communicate and collaborate to accomplish tasks.

### 2. **Responsibility Assignment**
Define clear responsibilities for each object in the interaction.

### 3. **Algorithm Encapsulation**
Encapsulate algorithms and make them interchangeable.

### 4. **Communication Patterns**
Establish patterns for how objects send and receive messages.

## 📚 Patterns Covered

| Pattern | Purpose | Key Benefit | When to Use |
|---------|---------|-------------|-------------|
| **Strategy** | Define family of algorithms | Runtime algorithm selection | Multiple ways to do same task |
| **Observer** | One-to-many dependency | Automatic notifications | State changes affect multiple objects |
| **State** | Alter behavior based on state | Clean state management | Behavior depends on state |
| **Template Method** | Define algorithm skeleton | Code reuse with variation | Common structure, varying steps |
| **Chain of Responsibility** | Pass request through chain | Decouple sender/receiver | Multiple handlers for request |

## 🎓 Pattern Deep Dive

### 1️⃣ Strategy Pattern
**Problem**: Multiple algorithms for same task, complex if-else chains  
**Solution**: Encapsulate each algorithm in separate class  
**Example**: Payment methods (CreditCard, PayPal, Crypto)

```java
// Choose algorithm at runtime
PaymentStrategy strategy = new CreditCardPayment();
processor.setStrategy(strategy);
processor.processPayment(100);
```

**Key Characteristics**:
- Uses composition over inheritance
- Algorithms are interchangeable
- Client chooses strategy
- Strategies are independent

### 2️⃣ Observer Pattern
**Problem**: One object change needs to notify many objects  
**Solution**: Subscribe/notify mechanism  
**Example**: Weather station notifying multiple displays

```java
// Subscribe to notifications
station.registerObserver(mobileDisplay);
station.setMeasurements(25.5f, 65.0f); // All observers notified
```

**Key Characteristics**:
- One-to-many relationship
- Loose coupling
- Dynamic subscription
- Broadcast communication

### 3️⃣ State Pattern
**Problem**: Complex if-else based on state  
**Solution**: Encapsulate state-specific behavior in classes  
**Example**: Vending machine states (NoCoin, HasCoin, Sold)

```java
// Behavior changes with state
machine.insertCoin(); // State: NoCoin → HasCoin
machine.pressButton(); // State: HasCoin → Sold
```

**Key Characteristics**:
- State-specific behavior
- Explicit state transitions
- Eliminates conditionals
- States know about each other

### 4️⃣ Template Method Pattern
**Problem**: Code duplication in similar algorithms  
**Solution**: Define algorithm skeleton, let subclasses fill steps  
**Example**: Beverage preparation (Coffee, Tea, HotChocolate)

```java
// Same structure, different steps
abstract class Beverage {
    final void prepareRecipe() {
        boilWater();    // Common
        brew();         // Varies
        pourInCup();    // Common
        addCondiments(); // Varies
    }
}
```

**Key Characteristics**:
- Uses inheritance
- Fixed algorithm structure
- Varying steps
- Hook methods for customization

### 5️⃣ Chain of Responsibility Pattern
**Problem**: Tight coupling between sender and receiver  
**Solution**: Pass request through chain of handlers  
**Example**: Support ticket escalation (Level1 → Level2 → Level3 → Manager)

```java
// Request passes through chain
level1.setNext(level2);
level2.setNext(level3);
level1.handleRequest(ticket); // Automatically escalates
```

**Key Characteristics**:
- Decouples sender/receiver
- Dynamic chain configuration
- Multiple handlers
- Request can be handled by one or many


## 🌍 Real-World Use Cases by Pattern

### Strategy Pattern
- Payment processing (credit card, PayPal, crypto)
- Shipping calculation (standard, express, overnight)
- Sorting algorithms (bubble, quick, merge)
- Compression (zip, rar, gzip)

### Observer Pattern
- Social media notifications (likes, comments, follows)
- Stock market monitoring (price updates)
- Weather monitoring (multiple displays)
- Event management systems

### State Pattern
- Order processing (pending, confirmed, shipped, delivered)
- Media player (playing, paused, stopped)
- Vending machine (no coin, has coin, sold, sold out)
- ATM machine (card ejected, card inserted, PIN entered)

### Template Method Pattern
- Data processing pipelines (CSV, JSON, XML)
- Game character actions (warrior, mage, archer)
- House construction (wooden, concrete, luxury)
- Test frameworks (setUp, test, tearDown)

### Chain of Responsibility Pattern
- Authentication pipeline (user check, password, role, 2FA)
- Logging systems (console, file, error)
- Expense approval (team lead, manager, director, CEO)
- Request validation (username, email, password)

## 💼 Industry Applications

| Industry | Patterns Used |
|----------|---------------|
| **E-commerce** | Strategy (payment), Observer (inventory), State (orders) |
| **Banking** | Chain (approval), Strategy (interest calculation), State (account) |
| **Gaming** | State (character), Strategy (AI), Observer (events) |
| **Social Media** | Observer (notifications), Chain (content moderation) |
| **Logging** | Chain (log levels), Strategy (log destinations) |
| **Web Frameworks** | Chain (middleware), Template Method (request handling) |

## 🔄 Pattern Relationships

```
Behavioral Patterns Ecosystem:

Strategy ←→ State
  ↓         ↓
Similar structure, different intent
Strategy: Choose algorithm
State: Change behavior with state

Template Method ←→ Strategy
  ↓                  ↓
Different mechanism
Template: Inheritance
Strategy: Composition

Observer ←→ Chain of Responsibility
  ↓              ↓
Different communication
Observer: Broadcast to all
Chain: Pass through handlers

All patterns promote:
- Loose coupling
- Flexibility
- Maintainability
- Open/Closed Principle
```


## 🎯 When to Use Which Pattern?

### Use Strategy When:
- ✅ Multiple algorithms for same task
- ✅ Need runtime algorithm selection
- ✅ Want to eliminate if-else chains
- ❌ Only one or two simple algorithms

### Use Observer When:
- ✅ One change affects multiple objects
- ✅ Need loose coupling
- ✅ Dynamic subscription needed
- ❌ Simple one-to-one relationships

### Use State When:
- ✅ Behavior depends on state
- ✅ Complex state-based conditionals
- ✅ Well-defined state transitions
- ❌ Only 2-3 simple states

### Use Template Method When:
- ✅ Multiple classes share algorithm structure
- ✅ Want to avoid code duplication
- ✅ Need to control customization points
- ❌ Algorithm structure varies significantly

### Use Chain of Responsibility When:
- ✅ Multiple objects can handle request
- ✅ Handler not known in advance
- ✅ Want to decouple sender/receiver
- ❌ Only one handler exists


## 🔗 Java Standard Library Examples

| Pattern | Java Examples |
|---------|---------------|
| **Strategy** | `Comparator`, `LayoutManager` |
| **Observer** | `java.util.Observer`, Event Listeners |
| **State** | `Thread` states, `Iterator` |
| **Template Method** | `InputStream`, `AbstractList` |
| **Chain** | `FilterChain`, Exception handling |