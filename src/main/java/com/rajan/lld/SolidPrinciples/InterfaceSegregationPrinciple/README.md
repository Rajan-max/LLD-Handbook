# Interface Segregation Principle (ISP)

## 📖 Definition
**Clients should not be forced to depend on interfaces they don't use.**

In simpler terms:
- Don't create "fat" interfaces with too many methods
- Split large interfaces into smaller, specific ones
- Classes should only implement methods they actually need
- Many specific interfaces are better than one general interface

## 🎯 Core Concept
**"No client should be forced to implement methods it doesn't use."**

When designing interfaces:
- Keep them small and focused
- Each interface should represent one capability
- Classes implement only the interfaces they need
- Avoid forcing unnecessary dependencies

## ❌ Bad Example (Violates ISP - Fat Interface)

```java
// PROBLEM: Fat interface with too many methods
interface AllInOneDevice {
    void print();
    void scan();
    void fax();
    void copy();
}

// This works fine - has all capabilities
class ModernPrinter implements AllInOneDevice {
    public void print() { ... }
    public void scan() { ... }
    public void fax() { ... }
    public void copy() { ... }
}

// PROBLEM: BasicPrinter only prints, but forced to implement everything!
class BasicPrinter implements AllInOneDevice {
    public void print() { 
        System.out.println("Printing..."); 
    }
    
    // Forced to implement methods it doesn't support!
    public void scan() { 
        throw new UnsupportedOperationException(); 
    }
    
    public void fax() { 
        throw new UnsupportedOperationException(); 
    }
    
    public void copy() { 
        throw new UnsupportedOperationException(); 
    }
}
```

**Why is this bad?**
- 🔴 **Forced implementations** - BasicPrinter must implement scan, fax, copy
- 🔴 **Throws exceptions** - Methods exist but don't work
- 🔴 **Fat interface** - Too many methods in one interface
- 🔴 **Tight coupling** - All clients depend on all methods
- 🔴 **Violates ISP** - Clients forced to depend on unused methods
- 🔴 **Hard to maintain** - Changes affect all implementers

**Real scenario:**
```java
AllInOneDevice device = new BasicPrinter();
device.print(); // ✓ Works
device.scan();  // ✗ Throws UnsupportedOperationException!
// Cannot safely use the interface
```

## ✅ Good Example (Follows ISP - Segregated Interfaces)

```java
// SOLUTION: Split into smaller, focused interfaces
interface Printer {
    void print();
}

interface Scanner {
    void scan();
}

interface FaxMachine {
    void fax();
}

interface Copier {
    void copy();
}

// BasicPrinter only implements what it needs
class BasicPrinter implements Printer {
    public void print() {
        System.out.println("Printing...");
    }
}

// All-in-one implements multiple interfaces
class AllInOneDevice implements Printer, Scanner, FaxMachine, Copier {
    public void print() { ... }
    public void scan() { ... }
    public void fax() { ... }
    public void copy() { ... }
}

// Printer-Scanner implements only two
class PrinterScanner implements Printer, Scanner {
    public void print() { ... }
    public void scan() { ... }
}

// Client code depends only on what it uses
class OfficeWorker {
    public void printDocument(Printer printer) {
        printer.print(); // Works with any Printer
    }
}
```

**Why is this better?**
- ✅ **No forced implementations** - implement only what you need
- ✅ **No exceptions** - all methods work as expected
- ✅ **Small interfaces** - each represents one capability
- ✅ **Loose coupling** - clients depend only on what they use
- ✅ **Follows ISP** - no unused dependencies
- ✅ **Easy to maintain** - changes are isolated

**Real scenario:**
```java
Printer printer = new BasicPrinter();
printer.print(); // ✓ Works - only has print method

AllInOneDevice allInOne = new AllInOneDevice();
allInOne.print(); // ✓ Works
allInOne.scan();  // ✓ Works
// Safe to use - all methods work
```

## 🔍 How to Identify ISP Violations

Ask yourself:

1. **"Does this interface have methods that some implementers don't need?"**
   - If yes → Violates ISP

2. **"Are implementers throwing UnsupportedOperationException?"**
   - If yes → Violates ISP

3. **"Are implementers providing empty/no-op implementations?"**
   - If yes → Violates ISP

4. **"Would splitting this interface make sense?"**
   - If yes → Consider ISP

## 🚨 Common ISP Violations

### 1. Fat Interface with Unused Methods
```java
interface Worker {
    void work();
    void eat();
    void sleep();
    void getPaid();
}

class Robot implements Worker {
    void work() { ... }
    void eat() { } // Robots don't eat!
    void sleep() { } // Robots don't sleep!
    void getPaid() { } // Robots don't get paid!
}
```

### 2. Throwing Exceptions
```java
interface Vehicle {
    void drive();
    void fly();
}

class Car implements Vehicle {
    void drive() { ... }
    void fly() { 
        throw new UnsupportedOperationException(); // Bad!
    }
}
```

### 3. Empty Implementations
```java
interface Animal {
    void walk();
    void swim();
    void fly();
}

class Dog implements Animal {
    void walk() { ... }
    void swim() { ... }
    void fly() { } // Dogs don't fly - empty method!
}
```

## 💡 Real-World Analogy

Think of a **Swiss Army knife vs specialized tools**:

**Bad (Fat Interface)**: 
- One tool that claims to do everything
- But not everyone needs all features
- Bulky and complex

**Good (Segregated Interfaces)**:
- Separate knife, screwdriver, scissors
- Use only what you need
- Simple and focused

Similarly, don't force a basic printer to "have" scanning and faxing capabilities it doesn't use!

## 🛠️ How to Apply ISP

1. **Identify fat interfaces** - interfaces with many methods
2. **Group related methods** - find cohesive sets of methods
3. **Split into smaller interfaces** - one interface per capability
4. **Use multiple inheritance** - classes implement multiple interfaces
5. **Keep interfaces focused** - each interface has one purpose

## 📊 Comparison Table

| Aspect | Bad (Violates ISP) | Good (Follows ISP) |
|--------|-------------------|-------------------|
| Interface size | Large, many methods | Small, focused |
| Forced methods | Yes | No |
| Exceptions | Throws UnsupportedOperationException | All methods work |
| Coupling | Tight | Loose |
| Flexibility | Low | High |
| Maintainability | Hard | Easy |
| Implementation | Forced to implement all | Implement only what's needed |

## 🎓 Practice Exercise

Identify the ISP violation:

```java
interface Employee {
    void work();
    void attendMeeting();
    void submitTimesheet();
    void manageTeam();
    void approveExpenses();
}

class Developer implements Employee {
    void work() { ... }
    void attendMeeting() { ... }
    void submitTimesheet() { ... }
    void manageTeam() { } // Not all developers manage teams!
    void approveExpenses() { } // Not all developers approve expenses!
}
```

**Answer**: Violates ISP! Not all employees manage teams or approve expenses.

**Solution**: 
```java
interface Worker {
    void work();
    void attendMeeting();
    void submitTimesheet();
}

interface Manager {
    void manageTeam();
    void approveExpenses();
}

class Developer implements Worker { ... }
class TeamLead implements Worker, Manager { ... }
```

## 🔑 Key Takeaways

1. **Keep interfaces small and focused**
2. **Split fat interfaces** into smaller ones
3. **No forced implementations** - implement only what you need
4. **Many specific interfaces > one general interface**
5. **Clients depend only on what they use**

## 🚀 Benefits

- ✅ **Flexibility** - classes implement only what they need
- ✅ **No exceptions** - all methods work as expected
- ✅ **Loose coupling** - reduced dependencies
- ✅ **Easy testing** - smaller interfaces are easier to mock
- ✅ **Better design** - clear separation of concerns
- ✅ **Maintainable** - changes don't affect unrelated code

## 📝 Summary

**Bad**: One fat interface → Forced implementations → Exceptions → Tight coupling

**Good**: Multiple small interfaces → Implement only what's needed → Clean code → Loose coupling

---

**Remember**: If you're throwing UnsupportedOperationException or writing empty methods, your interface is probably too fat. Split it!

## 🔗 Relationship with Other Principles

- **Works with SRP**: Small interfaces have single responsibility
- **Enables LSP**: Proper interfaces enable safe substitution
- **Supports OCP**: Easy to extend with new interfaces
- **Foundation for DIP**: Depend on small, focused abstractions

## ⚖️ ISP vs LSP

**LSP**: Child classes should work where parent works (inheritance)
**ISP**: Classes shouldn't implement methods they don't use (interfaces)

Both prevent forcing unwanted behavior, but at different levels!
