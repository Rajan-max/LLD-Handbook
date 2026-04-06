# State Design Pattern

## 📖 Definition

The **State Pattern** allows an object to alter its behavior when its internal state changes. The object will appear to change its class.

**In simple terms**: Same action, different behavior based on current state - the object behaves differently depending on what state it's in!

## 🎯 Core Concept

Instead of using complex if-else or switch statements to handle state-dependent behavior, the State pattern:
- Encapsulates each state as a separate class
- Delegates state-specific behavior to the current state object
- Makes state transitions explicit and manageable

**Key Components**:
1. **State Interface**: Defines operations that all states must implement
2. **Concrete States**: Implement state-specific behavior
3. **Context**: Maintains current state and delegates operations to it
4. **State Transitions**: States can trigger transitions to other states

## ❌ Problem: Without State Pattern

### The Complex If-Else Chain Problem

```java
class BadVendingMachine {
    private static final int NO_COIN = 0;
    private static final int HAS_COIN = 1;
    private static final int SOLD = 2;
    private static final int SOLD_OUT = 3;
    
    private int currentState = NO_COIN;
    
    public void insertCoin() {
        // Complex if-else based on state
        if (currentState == NO_COIN) {
            System.out.println("Coin inserted");
            currentState = HAS_COIN;
        } else if (currentState == HAS_COIN) {
            System.out.println("Coin already inserted!");
        } else if (currentState == SOLD_OUT) {
            System.out.println("Machine is sold out!");
        } else if (currentState == SOLD) {
            System.out.println("Please wait...");
        }
    }
    
    public void pressButton() {
        if (currentState == HAS_COIN) {
            // Logic for HAS_COIN state
        } else if (currentState == NO_COIN) {
            // Logic for NO_COIN state
        } // ... more conditions
    }
    // Every method has similar if-else chains!
}
```

### Why This Is Bad:

| Problem | Description |
|---------|-------------|
| **Violates Open/Closed** | Adding new states requires modifying all methods |
| **Hard to Maintain** | State logic scattered across multiple methods |
| **Error-Prone** | Easy to miss state checks or handle incorrectly |
| **Not Scalable** | More states = exponentially more complex conditions |
| **Poor Readability** | Long if-else chains are hard to understand |
| **Difficult Testing** | Must test all state combinations in each method |

## ✅ Solution: State Pattern

### Step-by-Step Implementation

**Step 1: Define State Interface**
```java
interface VendingMachineState {
    void insertCoin(VendingMachineContext context);
    void ejectCoin(VendingMachineContext context);
    void pressButton(VendingMachineContext context);
    void dispense(VendingMachineContext context);
    String getStateName();
}
```

**Step 2: Create Context Class**
```java
class VendingMachineContext {
    private VendingMachineState currentState;
    private int itemCount;
    
    // State instances
    private final VendingMachineState noCoinState;
    private final VendingMachineState hasCoinState;
    private final VendingMachineState soldState;
    private final VendingMachineState soldOutState;
    
    public VendingMachineContext(int itemCount) {
        this.itemCount = itemCount;
        
        // Create state instances
        noCoinState = new NoCoinState();
        hasCoinState = new HasCoinState();
        soldState = new SoldState();
        soldOutState = new SoldOutState();
        
        // Set initial state
        currentState = itemCount > 0 ? noCoinState : soldOutState;
    }
    
    public void setState(VendingMachineState state) {
        this.currentState = state;
    }
    
    // Delegate to current state
    public void insertCoin() {
        currentState.insertCoin(this);
    }
    
    public void pressButton() {
        currentState.pressButton(this);
    }
    
    // Getters for states
    public VendingMachineState getNoCoinState() { return noCoinState; }
    public VendingMachineState getHasCoinState() { return hasCoinState; }
    // ... other getters
}
```

**Step 3: Create Concrete States**
```java
class NoCoinState implements VendingMachineState {
    @Override
    public void insertCoin(VendingMachineContext context) {
        System.out.println("Coin inserted");
        context.setState(context.getHasCoinState());
    }
    
    @Override
    public void pressButton(VendingMachineContext context) {
        System.out.println("Insert coin first!");
    }
    
    @Override
    public String getStateName() {
        return "No Coin";
    }
}

class HasCoinState implements VendingMachineState {
    @Override
    public void insertCoin(VendingMachineContext context) {
        System.out.println("Coin already inserted!");
    }
    
    @Override
    public void pressButton(VendingMachineContext context) {
        System.out.println("Dispensing...");
        context.setState(context.getSoldState());
    }
    
    @Override
    public String getStateName() {
        return "Has Coin";
    }
}
```

**Step 4: Use the Pattern**
```java
VendingMachineContext machine = new VendingMachineContext(5);

machine.insertCoin();  // State: No Coin → Has Coin
machine.pressButton(); // State: Has Coin → Sold → No Coin
```

## 📊 Comparison: Before vs After

| Aspect | Without State | With State |
|--------|---------------|------------|
| **Adding new state** | Modify all methods | Create new state class |
| **State logic** | Scattered in if-else | Encapsulated in state classes |
| **Code organization** | All in one class | Separated by state |
| **Maintainability** | Low | High |
| **Open/Closed Principle** | ❌ Violated | ✅ Followed |
| **Testing** | Test all branches together | Test each state independently |
| **Readability** | Poor (complex conditions) | Excellent (clear separation) |

## 🌍 Real-World Use Cases

### 1. Vending Machine
```java
// States: NoCoin, HasCoin, Sold, SoldOut
- Insert coin → transition to HasCoin
- Press button → transition to Sold
- Dispense → transition to NoCoin or SoldOut
```

### 2. Order Processing
```java
// States: Pending, Confirmed, Shipped, Delivered, Cancelled
- Confirm → Pending to Confirmed
- Ship → Confirmed to Shipped
- Deliver → Shipped to Delivered
- Cancel → Any to Cancelled (with restrictions)
```

### 3. Media Player
```java
// States: Playing, Paused, Stopped
- Play → Stopped to Playing
- Pause → Playing to Paused
- Stop → Any to Stopped
```

### 4. Traffic Light
```java
// States: Red, Yellow, Green
- Red → Green
- Green → Yellow
- Yellow → Red
```

### 5. Document Workflow
```java
// States: Draft, Review, Approved, Published, Archived
- Submit → Draft to Review
- Approve → Review to Approved
- Publish → Approved to Published
```

### 6. ATM Machine
```java
// States: CardEjected, CardInserted, PINEntered, Transaction
- Insert card → CardEjected to CardInserted
- Enter PIN → CardInserted to PINEntered
- Select transaction → PINEntered to Transaction
```

## 💼 Industry Examples

| Application | State Use |
|-------------|-----------|
| **E-commerce** | Order states (pending, processing, shipped, delivered) |
| **Gaming** | Character states (idle, running, jumping, attacking) |
| **Workflow Systems** | Document approval workflows |
| **Network Protocols** | TCP connection states (closed, listen, established) |
| **UI Components** | Button states (enabled, disabled, pressed, hover) |
| **Booking Systems** | Reservation states (available, reserved, confirmed, cancelled) |

## ✅ Advantages

1. **Eliminates Conditionals**: No more complex if-else or switch statements
2. **Single Responsibility**: Each state class has one responsibility
3. **Open/Closed Principle**: Add new states without modifying existing code
4. **Explicit State Transitions**: State changes are clear and traceable
5. **Easy Testing**: Test each state independently
6. **Better Organization**: State-specific code is encapsulated
7. **Maintainability**: Easy to understand and modify state behavior

## ❌ Disadvantages

1. **More Classes**: Each state requires a separate class
2. **Complexity for Simple Cases**: Overkill if you have only 2-3 simple states
3. **State Explosion**: Many states can lead to many classes
4. **Context Dependency**: States need reference to context
5. **Memory Overhead**: Multiple state objects in memory

## 🎓 When to Use

### ✅ Use State Pattern When:
- Object behavior depends on its state
- You have complex conditional statements based on state
- State transitions are well-defined and frequent
- Same operation behaves differently in different states
- You want to add new states easily
- State-specific behavior is complex enough to warrant separate classes

### ❌ Avoid State Pattern When:
- You have only 2-3 simple states
- State transitions are rare or simple
- State-specific behavior is trivial
- The overhead of multiple classes isn't justified
- Simple if-else statements are clearer

## 🔄 State vs Other Patterns

### State vs Strategy
| Aspect | State | Strategy |
|--------|-------|----------|
| **Purpose** | Change behavior based on state | Choose algorithm |
| **Who decides** | Object changes state internally | Client sets strategy |
| **Awareness** | States may know about each other | Strategies are independent |
| **Transitions** | States trigger transitions | No transitions |
| **Example** | Order status changes | Payment methods |

### State vs Command
| Aspect | State | Command |
|--------|-------|---------|
| **Purpose** | Manage state-dependent behavior | Encapsulate requests |
| **Focus** | Object's internal state | Actions/operations |
| **Transitions** | States transition to other states | Commands don't transition |
| **Example** | Media player states | Undo/redo operations |

## 💡 Implementation Tips

1. **Shared State Instances**: Reuse state objects if they're stateless
2. **State Factory**: Use factory to create state instances
3. **State Transitions**: Let states handle their own transitions
4. **Context Reference**: Pass context to state methods
5. **Initial State**: Set appropriate initial state in context constructor
6. **State History**: Consider maintaining state history for undo functionality
7. **Thread Safety**: Consider synchronization for multi-threaded contexts

## 🧪 Practice Exercise

### Challenge: Document Approval System

Create a document approval system with different states and transitions.

**Requirements**:
- States: Draft, UnderReview, Approved, Rejected, Published
- Actions: submit(), approve(), reject(), publish(), revise()
- Rules:
  - Can only submit from Draft
  - Can approve/reject only from UnderReview
  - Can publish only from Approved
  - Can revise from Rejected back to Draft
  - Cannot modify Published documents

**Hints**:
1. Create `DocumentState` interface with all action methods
2. Create `Document` context class
3. Implement concrete states: `DraftState`, `UnderReviewState`, etc.
4. Each state should handle valid transitions and reject invalid ones
5. Test all valid and invalid state transitions

<details>
<summary>💡 Solution Outline</summary>

```java
interface DocumentState {
    void submit(Document doc);
    void approve(Document doc);
    void reject(Document doc);
    void publish(Document doc);
    void revise(Document doc);
    String getStateName();
}

class Document {
    private DocumentState currentState;
    private final DocumentState draftState;
    private final DocumentState underReviewState;
    private final DocumentState approvedState;
    private final DocumentState rejectedState;
    private final DocumentState publishedState;
    
    public Document() {
        // Initialize states
        draftState = new DraftState();
        underReviewState = new UnderReviewState();
        // ... initialize other states
        
        currentState = draftState; // Initial state
    }
    
    public void setState(DocumentState state) {
        this.currentState = state;
    }
    
    public void submit() { currentState.submit(this); }
    public void approve() { currentState.approve(this); }
    // ... other methods
}

class DraftState implements DocumentState {
    public void submit(Document doc) {
        System.out.println("Submitted for review");
        doc.setState(doc.getUnderReviewState());
    }
    
    public void approve(Document doc) {
        System.out.println("Cannot approve draft!");
    }
    // ... implement other methods
}
```

</details>

## 🎯 Key Takeaways

1. **State = Behavior Change**: Object behavior changes with state
2. **Encapsulation**: Each state encapsulates its own behavior
3. **No Conditionals**: Eliminates complex if-else chains
4. **Explicit Transitions**: State changes are clear and traceable
5. **Open/Closed**: Add new states without modifying existing code
6. **Context Delegation**: Context delegates to current state

## 📚 Related Patterns

- **Strategy**: Similar structure, but strategies don't know about each other
- **Command**: Can be used to implement state transitions
- **Flyweight**: Share state objects if they're stateless
- **Singleton**: State instances can be singletons
- **Memento**: Save and restore state history

## 🔗 Java Standard Library Examples

- `javax.faces.lifecycle.LifeCycle` - JSF lifecycle states
- `java.util.Iterator` - hasNext() behavior depends on iteration state
- TCP connection states in networking
- Thread states (`NEW`, `RUNNABLE`, `BLOCKED`, `WAITING`, `TERMINATED`)

## 🏗️ State Machine Diagram

```
Vending Machine State Transitions:

    ┌─────────────┐
    │   No Coin   │◄─────────┐
    └──────┬──────┘           │
           │ insertCoin()     │
           ▼                  │
    ┌─────────────┐           │
    │  Has Coin   │           │
    └──────┬──────┘           │
           │ pressButton()    │
           ▼                  │
    ┌─────────────┐           │
    │    Sold     │───────────┘
    └──────┬──────┘  dispense()
           │
           ▼ (if itemCount == 0)
    ┌─────────────┐
    │  Sold Out   │
    └─────────────┘
```

---

## 🚀 Running the Demo

```bash
# Compile
mvn clean compile

# Run
mvn exec:java -Dexec.mainClass="com.rajan.lld.DesignPatterns.BehavioralDesignPatterns.StateDesignPattern.StateDesignPattern"
```

---

**Remember**: Use State Pattern when an object's behavior depends on its state and you have complex conditional logic. It makes state transitions explicit and behavior changes clear!
