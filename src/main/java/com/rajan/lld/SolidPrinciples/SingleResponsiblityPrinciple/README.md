# Single Responsibility Principle (SRP)

## 📖 Definition
**A class should have only ONE reason to change.**

In simpler terms: A class should do ONE thing and do it well.

## 🎯 Core Concept
When we say "one reason to change," we mean:
- If email logic changes → only EmailService changes
- If database logic changes → only UserRepository changes
- If user data structure changes → only User class changes

Each class should have a single, well-defined purpose.

## ❌ Bad Example (Violates SRP)

```java
class UserBad {
    // PROBLEM: This class has 4 different responsibilities!
    
    // 1. Managing user data ✓ (This is OK)
    private String name;
    private String email;
    
    // 2. Sending emails ✗ (NOT user's job)
    public void sendEmail(String message) { ... }
    
    // 3. Database operations ✗ (NOT user's job)
    public void saveToDatabase() { ... }
    
    // 4. Validation ✗ (NOT user's job)
    public boolean validateEmail() { ... }
}
```

**Why is this bad?**
- **Multiple reasons to change**: Email provider changes? Database changes? Validation rules change? All require modifying this class
- **Hard to test**: Can't test email logic without creating a User object
- **Not reusable**: Can't use email logic for other entities (Order, Product, etc.)
- **Tight coupling**: Everything is tangled together
- **Difficult maintenance**: Class becomes bloated with unrelated code

## ✅ Good Example (Follows SRP)

```java
// Each class has ONE responsibility

class User {                    // Only manages user data
    private String name;
    private String email;
}

class EmailService {            // Only sends emails
    public void sendEmail(String email, String message) { ... }
}

class UserRepository {          // Only handles database
    public void save(User user) { ... }
}

class EmailValidator {          // Only validates emails
    public boolean isValid(String email) { ... }
}
```

**Why is this better?**
- ✓ **Single reason to change**: Each class changes only when its specific responsibility changes
- ✓ **Easy to test**: Test each component independently
- ✓ **Reusable**: EmailService can be used for Order, Product, etc.
- ✓ **Loose coupling**: Changes in one class don't affect others
- ✓ **Clean code**: Each class is small, focused, and easy to understand

## 🔍 How to Identify SRP Violations

Ask yourself these questions:

1. **"Does this class have more than one reason to change?"**
   - If yes → Split it!

2. **"Can I describe this class's purpose without using 'AND'?"**
   - "User manages data AND sends emails" → Violation!
   - "User manages data" → Good!

3. **"Would changes in different parts of the system require modifying this class?"**
   - If yes → Split it!

## 💡 Real-World Analogy

Think of a restaurant:
- **Chef** → Cooks food (one job)
- **Waiter** → Serves customers (one job)
- **Cashier** → Handles payments (one job)

You wouldn't want the chef to also handle payments and serve tables. Same principle applies to code!

## 🎓 Key Takeaways

1. **One class = One responsibility**
2. **One reason to change**
3. **Easier to maintain, test, and reuse**
4. **Foundation for other SOLID principles**

## 🚀 Practice Exercise

Look at this class and identify SRP violations:

```java
class Invoice {
    public void calculateTotal() { ... }      // Calculation logic
    public void printInvoice() { ... }        // Printing logic
    public void saveToDatabase() { ... }      // Database logic
}
```

**Answer**: This class has 3 responsibilities! Split into:
- `Invoice` (calculation)
- `InvoicePrinter` (printing)
- `InvoiceRepository` (database)

## 📝 Summary

| Aspect | Bad (Violates SRP) | Good (Follows SRP) |
|--------|-------------------|--------------------|
| Responsibilities | Multiple | Single |
| Reasons to change | Many | One |
| Testability | Hard | Easy |
| Reusability | Low | High |
| Maintenance | Difficult | Simple |
| Coupling | Tight | Loose |

---

**Remember**: If a class is doing too much, split it up. Keep it simple, keep it focused!
