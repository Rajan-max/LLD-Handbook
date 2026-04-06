# Liskov Substitution Principle (LSP)

## 📖 Definition
**Objects of a superclass should be replaceable with objects of its subclasses without breaking the application.**

In simpler terms:
- If you have a parent class, you should be able to use any child class in its place
- Child classes should extend, not replace or break, parent class behavior
- Substituting a parent with a child should not cause unexpected behavior

## 🎯 Core Concept
**"If it looks like a duck, quacks like a duck, but needs batteries - you probably have the wrong abstraction."**

When class B extends class A:
- B should be usable wherever A is expected
- B should not throw unexpected exceptions
- B should not break the contract established by A

## ❌ Bad Example (Violates LSP)

```java
class Bird {
    public void fly() {
        System.out.println("Bird is flying");
    }
}

class Sparrow extends Bird {
    public void fly() {
        System.out.println("Sparrow is flying"); // ✓ Works fine
    }
}

class Penguin extends Bird {
    public void fly() {
        // PROBLEM: Penguins cannot fly!
        throw new UnsupportedOperationException("Penguins cannot fly!");
    }
}

// Client code
void makeBirdFly(Bird bird) {
    bird.fly(); // Breaks when bird is a Penguin!
}
```

**Why is this bad?**
- 🔴 **Cannot substitute** Bird with Penguin safely
- 🔴 **Throws unexpected exception** - breaks the contract
- 🔴 **Wrong abstraction** - not all birds fly
- 🔴 **Violates LSP** - child class breaks parent's behavior
- 🔴 **Forces bad design** - either throw exception or do nothing

**Real scenario:**
```java
Bird bird = new Penguin();
bird.fly(); // BOOM! UnsupportedOperationException
// Cannot safely use Penguin where Bird is expected
```

## ✅ Good Example (Follows LSP)

```java
// Base class with common behavior
class Bird {
    public void eat() {
        System.out.println("Bird is eating");
    }
}

// Separate interface for flying capability
interface Flyable {
    void fly();
}

// Flying birds implement both
class Sparrow extends Bird implements Flyable {
    public void fly() {
        System.out.println("Sparrow is flying");
    }
}

// Non-flying birds just extend Bird
class Penguin extends Bird {
    public void swim() {
        System.out.println("Penguin is swimming");
    }
}

// Client code works with appropriate abstractions
void makeBirdFly(Flyable bird) {
    bird.fly(); // Safe - all Flyable birds can fly
}

void feedBird(Bird bird) {
    bird.eat(); // Safe - all birds can eat
}
```

**Why is this better?**
- ✅ **Safe substitution** - Penguin can replace Bird for eating
- ✅ **No exceptions** - each class does what it promises
- ✅ **Correct abstraction** - flying is separate from being a bird
- ✅ **Follows LSP** - child classes extend without breaking
- ✅ **Type-safe** - compiler prevents misuse

**Real scenario:**
```java
Bird bird1 = new Sparrow();
Bird bird2 = new Penguin();
feedBird(bird1); // ✓ Works
feedBird(bird2); // ✓ Works - safe substitution!

Flyable flyingBird = new Sparrow();
makeBirdFly(flyingBird); // ✓ Works
// makeBirdFly(new Penguin()); // Compile error - type-safe!
```

## 🔍 How to Identify LSP Violations

Ask yourself:

1. **"Does the child class throw exceptions the parent doesn't?"**
   - If yes → Violates LSP

2. **"Does the child class do nothing in overridden methods?"**
   - If yes → Likely violates LSP

3. **"Can I use the child class everywhere I use the parent?"**
   - If no → Violates LSP

4. **"Does the child class weaken preconditions or strengthen postconditions?"**
   - If yes → Violates LSP

## 🚨 Common LSP Violations

### 1. Throwing Unexpected Exceptions
```java
class Rectangle {
    void setWidth(int w) { ... }
}

class Square extends Rectangle {
    void setWidth(int w) {
        throw new Exception("Cannot set width independently!");
    }
}
```

### 2. Empty/No-op Methods
```java
class Vehicle {
    void startEngine() { ... }
}

class Bicycle extends Vehicle {
    void startEngine() {
        // Does nothing - bicycles don't have engines!
    }
}
```

### 3. Changing Expected Behavior
```java
class Account {
    void withdraw(int amount) {
        balance -= amount;
    }
}

class FixedDepositAccount extends Account {
    void withdraw(int amount) {
        // PROBLEM: Cannot withdraw from fixed deposit!
        throw new Exception("Withdrawal not allowed");
    }
}
```

## 💡 Real-World Analogy

Think of **electrical outlets**:
- **Standard outlet**: Accepts any standard plug
- **USB outlet**: Accepts any USB device

You wouldn't create a "USB device" that requires a standard plug - that breaks the contract!

Similarly, don't create a "Bird" that cannot do what birds are expected to do.

## 🛠️ How to Apply LSP

1. **Design by contract** - child classes must honor parent's contract
2. **Use composition over inheritance** when behavior differs significantly
3. **Separate interfaces** for different capabilities (like Flyable)
4. **Don't force behavior** - if a child can't do something, parent shouldn't require it
5. **Think "IS-A" carefully** - Penguin IS-A Bird, but not IS-A FlyingBird

## 📊 Comparison Table

| Aspect | Bad (Violates LSP) | Good (Follows LSP) |
|--------|-------------------|-------------------|
| Substitution | Breaks when using child | Works seamlessly |
| Exceptions | Throws unexpected errors | No surprises |
| Behavior | Child breaks parent's contract | Child extends parent |
| Type safety | Runtime errors | Compile-time safety |
| Design | Forced inheritance | Proper abstraction |
| Maintainability | Fragile | Robust |

## 🎓 Practice Exercise

Identify the LSP violation:

```java
class Employee {
    public double calculateBonus() {
        return salary * 0.1;
    }
}

class Intern extends Employee {
    public double calculateBonus() {
        throw new Exception("Interns don't get bonuses!");
    }
}
```

**Answer**: Violates LSP! Intern cannot substitute Employee safely.

**Solution**: 
- Option 1: Don't make Intern extend Employee
- Option 2: Make calculateBonus return 0 for interns (if that's valid business logic)
- Option 3: Create separate hierarchy for bonus-eligible employees

## 🔑 Key Takeaways

1. **Child classes should extend, not break** parent behavior
2. **No unexpected exceptions** in child classes
3. **Honor the contract** established by parent class
4. **Use interfaces** to separate different capabilities
5. **Think carefully about inheritance** - not everything is IS-A relationship

## 🚀 Benefits

- ✅ **Predictable behavior** - no surprises when using subclasses
- ✅ **Type safety** - compiler catches misuse
- ✅ **Robust code** - substitution works correctly
- ✅ **Better design** - proper abstractions
- ✅ **Easier testing** - can test with any subclass
- ✅ **Maintainable** - clear contracts and expectations

## 📝 Summary

**Bad**: Child class breaks parent's behavior → Runtime exceptions → Fragile code

**Good**: Child class extends parent properly → Safe substitution → Robust code

---

**Remember**: If you cannot safely replace a parent with a child, your inheritance hierarchy is wrong. Fix the abstraction, don't force the inheritance!

## 🔗 Relationship with Other Principles

- **Works with OCP**: Proper substitution enables extension
- **Requires SRP**: Classes with single responsibility are easier to substitute
- **Enables polymorphism**: Safe substitution makes polymorphism work correctly
