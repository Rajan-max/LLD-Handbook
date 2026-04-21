# Builder Design Pattern

## 📖 Definition
**Constructs complex objects step by step, especially when objects have many optional parameters.**

In simpler terms:
- Build complex objects piece by piece
- Separate construction from representation
- Handle many optional parameters elegantly
- "Build a custom burger - choose what you want, skip what you don't!"

## 🎯 Core Concept

**Problem**: Objects with many parameters (especially optional ones)
**Solution**: Builder provides a fluent interface to construct objects step by step

## ❌ Problem - Telescoping Constructor Anti-Pattern

```java
class User {
    // Too many constructors!
    public User(String name) { ... }
    public User(String name, String email) { ... }
    public User(String name, String email, int age) { ... }
    public User(String name, String email, int age, String phone) { ... }
    public User(String name, String email, int age, String phone, 
                String address, boolean isActive, String department) { ... }
}

// Usage - Confusing and error-prone
User user = new User("John", "john@email.com", 25, null, null, true, "IT");
// What do these parameters mean? Easy to mix up order!
```

**Why is this bad?**
- 🔴 **Hard to read** - What does each parameter mean?
- 🔴 **Error-prone** - Easy to mix up parameter order
- 🔴 **Not flexible** - Need constructor for every combination
- 🔴 **Maintenance nightmare** - Adding parameter affects all constructors
- 🔴 **Null parameters** - Must pass null for unused optional parameters

## ✅ Solution - Builder Pattern

```java
class User {
    private final String name;
    private final String email;
    private final int age;
    // ... other fields
    
    // Private constructor
    private User(UserBuilder builder) {
        this.name = builder.name;
        this.email = builder.email;
        this.age = builder.age;
    }
    
    // Static nested Builder class
    public static class UserBuilder {
        // Required
        private final String name;
        
        // Optional with defaults
        private String email = "";
        private int age = 0;
        
        public UserBuilder(String name) {
            this.name = name;
        }
        
        // Fluent interface - returns builder
        public UserBuilder email(String email) {
            this.email = email;
            return this;
        }
        
        public UserBuilder age(int age) {
            this.age = age;
            return this;
        }
        
        // Build with validation
        public User build() {
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException("Name required");
            }
            return new User(this);
        }
    }
}

// Usage - Clear and readable!
User user = new User.UserBuilder("John")
        .email("john@email.com")
        .age(25)
        .build();
```

## 📊 Comparison

| Aspect | Telescoping Constructor | Builder Pattern |
|--------|------------------------|-----------------|
| **Readability** | Poor | Excellent |
| **Flexibility** | Low | High |
| **Optional params** | Difficult | Easy |
| **Validation** | Limited | Comprehensive |
| **Immutability** | Possible | Natural |
| **Maintenance** | Hard | Easy |

## 🌍 Real-World Use Cases

### 1. User/Profile Objects
```java
User user = new User.UserBuilder("John")
        .email("john@email.com")
        .age(25)
        .phone("123-456-7890")
        .department("IT")
        .build();
```

### 2. Computer Configuration
```java
Computer pc = new Computer.ComputerBuilder("Intel i9")
        .gpu("Nvidia RTX 4090")
        .ram(64)
        .storage(2000)
        .coolingSystem("Liquid Cooling")
        .build();
```

### 3. HTTP Requests
```java
HttpRequest request = new HttpRequest.HttpRequestBuilder("https://api.example.com")
        .method("POST")
        .body("{\"name\":\"John\"}")
        .contentType("application/json")
        .timeout(10000)
        .build();
```

### 4. StringBuilder (Java Built-in)
```java
String result = new StringBuilder()
        .append("Hello")
        .append(" ")
        .append("World")
        .toString();
```

## 🔍 When to Use Builder Pattern

Ask yourself:

1. **"Does my object have many parameters (4+)?"**
   - If yes → Consider Builder

2. **"Are most parameters optional?"**
   - If yes → Builder is ideal

3. **"Do I need immutable objects?"**
   - If yes → Builder helps

4. **"Is parameter order confusing?"**
   - If yes → Builder makes it clear

5. **"Do I need validation before object creation?"**
   - If yes → Builder provides this

## 💡 Key Advantages

### 1. Readability
```java
// Bad - What do these mean?
new User("John", "john@email.com", 25, null, null, true, "IT");

// Good - Crystal clear!
new User.UserBuilder("John")
        .email("john@email.com")
        .age(25)
        .department("IT")
        .build();
```

### 2. Flexibility
```java
// Set only what you need
User minimal = new User.UserBuilder("Bob").build();

User detailed = new User.UserBuilder("Alice")
        .email("alice@email.com")
        .age(30)
        .phone("555-1234")
        .address("123 Main St")
        .department("Engineering")
        .build();
```

### 3. Immutability
```java
// All fields final - object is immutable after creation
class User {
    private final String name;
    private final String email;
    // No setters - immutable!
}
```

### 4. Validation
```java
public User build() {
    if (name == null || name.isEmpty()) {
        throw new IllegalArgumentException("Name required");
    }
    if (age < 0) {
        throw new IllegalArgumentException("Age cannot be negative");
    }
    return new User(this);
}
```

## ⚖️ Pros and Cons

### Pros
- ✅ **Readable code** - Clear what each parameter is
- ✅ **Flexible** - Set only needed parameters
- ✅ **Immutable objects** - Thread-safe
- ✅ **Validation** - Check before creating object
- ✅ **No telescoping constructors** - Clean code
- ✅ **Fluent interface** - Method chaining

### Cons
- ❌ **More code** - Need builder class
- ❌ **Slight overhead** - Extra object creation
- ❌ **Overkill** - For simple objects with few parameters

## 🎓 Practice Exercise

Create a Builder for a Pizza ordering system:

**Requirements:**
- Required: Size
- Optional: Cheese, Pepperoni, Mushrooms, Olives, Sauce type
- Validate: Size must be Small, Medium, or Large

**Solution:**
```java
class Pizza {
    private final String size;
    private final boolean cheese;
    private final boolean pepperoni;
    private final boolean mushrooms;
    private final boolean olives;
    private final String sauce;
    
    private Pizza(PizzaBuilder builder) {
        this.size = builder.size;
        this.cheese = builder.cheese;
        this.pepperoni = builder.pepperoni;
        this.mushrooms = builder.mushrooms;
        this.olives = builder.olives;
        this.sauce = builder.sauce;
    }
    
    public static class PizzaBuilder {
        private final String size;
        private boolean cheese = true;
        private boolean pepperoni = false;
        private boolean mushrooms = false;
        private boolean olives = false;
        private String sauce = "Tomato";
        
        public PizzaBuilder(String size) {
            this.size = size;
        }
        
        public PizzaBuilder cheese(boolean cheese) {
            this.cheese = cheese;
            return this;
        }
        
        public PizzaBuilder pepperoni(boolean pepperoni) {
            this.pepperoni = pepperoni;
            return this;
        }
        
        public PizzaBuilder mushrooms(boolean mushrooms) {
            this.mushrooms = mushrooms;
            return this;
        }
        
        public PizzaBuilder olives(boolean olives) {
            this.olives = olives;
            return this;
        }
        
        public PizzaBuilder sauce(String sauce) {
            this.sauce = sauce;
            return this;
        }
        
        public Pizza build() {
            if (!size.equals("Small") && !size.equals("Medium") && !size.equals("Large")) {
                throw new IllegalArgumentException("Invalid size");
            }
            return new Pizza(this);
        }
    }
}

// Usage
Pizza pizza = new Pizza.PizzaBuilder("Large")
        .pepperoni(true)
        .mushrooms(true)
        .sauce("BBQ")
        .build();
```

## 🔑 Key Takeaways

1. **Use for complex objects** with many parameters
2. **Fluent interface** makes code readable
3. **Immutability** comes naturally
4. **Validation** before object creation
5. **Flexible** - set only what you need

## 🚀 When NOT to Use

- Objects with few parameters (2-3)
- Simple objects without optional parameters
- When mutability is required
- Performance-critical code (slight overhead)

## 📝 Summary

**Problem**: Telescoping constructors → Hard to read → Error-prone

**Solution**: Builder pattern → Fluent interface → Clear and flexible

**Key Benefit**: Readable, flexible object construction with validation!

---

**Remember**: Builder is like ordering a custom burger - you choose exactly what you want, and skip what you don't need!

## 🆚 Builder vs Factory

**Factory Pattern:**
- Creates objects in one step
- Simple object creation
- Example: `UserFactory.create("John")`

**Builder Pattern:**
- Constructs objects step by step
- Complex objects with many parameters
- Example: `new UserBuilder("John").email(...).age(...).build()`

## 💻 Implementation Tips

1. **Make fields final** for immutability
2. **Private constructor** - only builder creates objects
3. **Return builder** from each method for chaining
4. **Validate in build()** method
5. **Required parameters** in builder constructor
6. **Optional parameters** with default values
7. **Static nested class** for builder

## 🎯 Quick Decision Guide

**Use Builder when:**
- 4+ parameters
- Many optional parameters
- Need immutability
- Want validation
- Readability is important

**Use Constructor when:**
- 1-3 parameters
- All parameters required
- Simple object
- Performance critical
