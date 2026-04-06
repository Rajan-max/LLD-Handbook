# Chain of Responsibility Design Pattern

## 📖 Definition

The **Chain of Responsibility Pattern** passes a request along a chain of handlers. Each handler decides either to process the request or to pass it to the next handler in the chain.

**In simple terms**: Pass the request through a chain - each handler can handle it or pass it along, like passing a hot potato until someone catches it!

## 🎯 Core Concept

The Chain of Responsibility pattern:
- Creates a chain of handler objects
- Request passes through the chain
- Each handler can process or forward the request
- Decouples sender from receiver
- Handlers don't know about each other

**Key Components**:
1. **Handler Interface**: Defines method to handle request and set next handler
2. **Concrete Handlers**: Implement handling logic
3. **Client**: Initiates request to first handler in chain

## ❌ Problem: Without Chain of Responsibility Pattern

### The Tight Coupling Problem

```java
class BadSupportSystem {
    public void handleRequest(String issue, String priority) {
        // Complex if-else chain - tightly coupled
        if (priority.equals("LOW")) {
            System.out.println("Level 1 Support handling: " + issue);
        } else if (priority.equals("MEDIUM")) {
            System.out.println("Level 2 Support handling: " + issue);
        } else if (priority.equals("HIGH")) {
            System.out.println("Level 3 Support handling: " + issue);
        } else if (priority.equals("CRITICAL")) {
            System.out.println("Manager handling: " + issue);
        }
        // Adding new priority requires modifying this method!
    }
}
```

### Why This Is Bad:

| Problem | Description |
|---------|-------------|
| **Tight Coupling** | Sender knows all possible handlers |
| **Complex Logic** | Nested if-else for each handler |
| **Hard to Extend** | Adding handler requires code modification |
| **No Flexibility** | Can't change handler order dynamically |
| **Violates Open/Closed** | Must modify existing code for new handlers |
| **Single Point of Failure** | All logic in one place |

## ✅ Solution: Chain of Responsibility Pattern

### Step-by-Step Implementation

**Step 1: Create Handler Abstract Class**
```java
abstract class SupportHandler {
    protected SupportHandler nextHandler;
    
    public void setNext(SupportHandler handler) {
        this.nextHandler = handler;
    }
    
    public abstract void handleRequest(SupportTicket ticket);
}
```

**Step 2: Create Concrete Handlers**
```java
class Level1Support extends SupportHandler {
    @Override
    public void handleRequest(SupportTicket ticket) {
        if (ticket.getPriority().equals("LOW")) {
            System.out.println("Level 1: Handling " + ticket.getIssue());
        } else if (nextHandler != null) {
            nextHandler.handleRequest(ticket);
        }
    }
}

class Level2Support extends SupportHandler {
    @Override
    public void handleRequest(SupportTicket ticket) {
        if (ticket.getPriority().equals("MEDIUM")) {
            System.out.println("Level 2: Handling " + ticket.getIssue());
        } else if (nextHandler != null) {
            nextHandler.handleRequest(ticket);
        }
    }
}
```

**Step 3: Build the Chain**
```java
SupportHandler level1 = new Level1Support();
SupportHandler level2 = new Level2Support();
SupportHandler level3 = new Level3Support();

level1.setNext(level2);
level2.setNext(level3);
```

**Step 4: Use the Chain**
```java
// Request enters at first handler
level1.handleRequest(new SupportTicket("Issue", "MEDIUM"));
// Automatically passes through chain until handled
```

## 📊 Comparison: Before vs After

| Aspect | Without Chain | With Chain |
|--------|---------------|------------|
| **Coupling** | Tight (knows all handlers) | Loose (knows only interface) |
| **Adding handler** | Modify existing code | Add new handler class |
| **Handler order** | Fixed in code | Dynamic configuration |
| **Flexibility** | Low | High |
| **Open/Closed Principle** | ❌ Violated | ✅ Followed |
| **Maintainability** | Low | High |
| **Testability** | Hard (test all together) | Easy (test independently) |

## 🌍 Real-World Use Cases

### 1. Support Ticket System
```java
// Chain: Level1 → Level2 → Level3 → Manager
- LOW: Level 1 handles
- MEDIUM: Level 2 handles
- HIGH: Level 3 handles
- CRITICAL: Manager handles
```

### 2. Authentication Pipeline
```java
// Chain: UserExists → PasswordValid → RoleCheck → 2FA
- Each step validates one aspect
- Fails fast if any check fails
- All checks must pass for success
```

### 3. Logging System
```java
// Chain: Console → File → Error
- INFO: Console logs
- DEBUG: Console + File log
- ERROR: Console + File + Error log
```

### 4. Expense Approval
```java
// Chain: TeamLead → Manager → Director → CEO
- $0-1K: Team Lead approves
- $1K-5K: Manager approves
- $5K-20K: Director approves
- $20K+: CEO approves
```

### 5. Request Validation
```java
// Chain: Username → Email → Password → Terms
- Each validator checks one field
- Stops at first validation failure
- All must pass for valid request
```

### 6. Middleware Pipeline
```java
// Chain: Auth → Logging → RateLimit → CORS → Handler
- Each middleware processes request
- Can short-circuit or continue
- Common in web frameworks
```

## 💼 Industry Examples

| Application | Chain of Responsibility Use |
|-------------|---------------------------|
| **Web Frameworks** | Middleware pipeline (Express.js, Spring) |
| **Logging** | Log level filtering (Log4j, SLF4J) |
| **Event Handling** | Event bubbling in DOM |
| **Security** | Authentication/authorization filters |
| **Validation** | Multi-step validation pipelines |
| **Exception Handling** | Try-catch blocks in call stack |

## ✅ Advantages

1. **Decoupling**: Sender doesn't know which handler will process
2. **Flexibility**: Add/remove handlers dynamically
3. **Single Responsibility**: Each handler has one job
4. **Open/Closed**: Add handlers without modifying existing code
5. **Dynamic Configuration**: Change chain at runtime
6. **Multiple Handlers**: Request can be processed by multiple handlers
7. **Fail-Safe**: Can handle unprocessed requests gracefully

## ❌ Disadvantages

1. **No Guarantee**: Request might not be handled
2. **Debugging Difficulty**: Hard to trace request flow
3. **Performance**: Request passes through multiple handlers
4. **Chain Configuration**: Must ensure chain is properly set up
5. **Complexity**: Can be overkill for simple scenarios

## 🎓 When to Use

### ✅ Use Chain of Responsibility When:
- Multiple objects can handle a request
- Handler isn't known in advance
- Want to issue request without specifying receiver
- Set of handlers should be specified dynamically
- Request should be handled by multiple handlers
- Want to decouple sender from receiver

### ❌ Avoid Chain of Responsibility When:
- Only one handler exists
- Handler is always known in advance
- Performance is critical (avoid chain overhead)
- Request must be handled (no guarantee in chain)
- Simple if-else is clearer

## 🔄 Chain of Responsibility vs Other Patterns

### Chain of Responsibility vs Command
| Aspect | Chain of Responsibility | Command |
|--------|------------------------|---------|
| **Purpose** | Pass request through chain | Encapsulate request as object |
| **Handlers** | Multiple handlers | Single receiver |
| **Processing** | One or more handle | One executes |
| **Example** | Support escalation | Undo/redo operations |

### Chain of Responsibility vs Decorator
| Aspect | Chain of Responsibility | Decorator |
|--------|------------------------|-----------|
| **Purpose** | Handle or pass request | Add behavior |
| **Processing** | One handles | All add behavior |
| **Order** | Matters (first match) | Matters (layered) |
| **Example** | Logging levels | Stream wrappers |

## 💡 Implementation Tips

1. **Define Clear Interface**: Handler interface should be simple
2. **Set Next Handler**: Provide method to link handlers
3. **Handle Unprocessed**: Decide what happens if no handler processes
4. **Consider Order**: Handler order matters
5. **Avoid Cycles**: Ensure chain doesn't loop back
6. **Logging**: Log when request passes through handlers
7. **Default Handler**: Consider adding catch-all handler at end

### Chain Building Patterns

**Pattern 1: Manual Chaining**
```java
handler1.setNext(handler2);
handler2.setNext(handler3);
```

**Pattern 2: Fluent Interface**
```java
handler1.setNext(handler2).setNext(handler3);
```

**Pattern 3: Builder Pattern**
```java
Chain chain = new ChainBuilder()
    .addHandler(handler1)
    .addHandler(handler2)
    .addHandler(handler3)
    .build();
```

## 🧪 Practice Exercise

### Challenge: Email Spam Filter

Create an email spam filter using Chain of Responsibility.

**Requirements**:
- Filters: BlacklistFilter, KeywordFilter, SenderFilter, AttachmentFilter
- Each filter checks one aspect
- Email passes through all filters
- Any filter can mark email as spam
- Track which filter caught the spam

**Hints**:
1. Create `SpamFilter` abstract class with `filter(Email email)` method
2. Each filter checks specific criteria
3. If spam detected, mark and optionally continue or stop
4. Build chain: Blacklist → Keyword → Sender → Attachment
5. Test with various email scenarios

<details>
<summary>💡 Solution Outline</summary>

```java
abstract class SpamFilter {
    protected SpamFilter next;
    
    public void setNext(SpamFilter filter) {
        this.next = filter;
    }
    
    public boolean filter(Email email) {
        if (isSpam(email)) {
            System.out.println(getFilterName() + " caught spam");
            return true;
        }
        return next != null ? next.filter(email) : false;
    }
    
    protected abstract boolean isSpam(Email email);
    protected abstract String getFilterName();
}

class BlacklistFilter extends SpamFilter {
    private Set<String> blacklist = Set.of("spam@bad.com");
    
    protected boolean isSpam(Email email) {
        return blacklist.contains(email.getSender());
    }
    
    protected String getFilterName() {
        return "Blacklist Filter";
    }
}
```

</details>

## 🎯 Key Takeaways

1. **Chain of Handlers**: Request passes through linked handlers
2. **Decoupling**: Sender doesn't know receiver
3. **Dynamic Configuration**: Build chain at runtime
4. **Flexible Processing**: Handler can process or pass
5. **Multiple Handlers**: Request can be handled by multiple handlers
6. **Open/Closed**: Add handlers without modifying existing code

## 📚 Related Patterns

- **Command**: Encapsulates request as object
- **Decorator**: Similar structure but adds behavior
- **Composite**: Can be used to build handler tree
- **Observer**: Multiple objects react to event
- **Strategy**: Choose algorithm, not pass through chain

## 🔗 Java Standard Library Examples

- `javax.servlet.Filter` - Servlet filter chain
- `java.util.logging.Logger` - Logging hierarchy
- Exception handling - Try-catch propagation
- Event handling - Event bubbling in Swing/AWT
- `javax.servlet.FilterChain` - Web filter chain

## 🏗️ Chain Structure

```
Request Flow:

Client → Handler1 → Handler2 → Handler3 → Handler4
           ↓          ↓          ↓          ↓
        Process?   Process?   Process?   Process?
           ↓          ↓          ↓          ↓
        Yes/No     Yes/No     Yes/No     Yes/No

If Yes: Handle and optionally continue
If No: Pass to next handler
```

---

## 🚀 Running the Demo

```bash
# Compile
mvn clean compile

# Run
mvn exec:java -Dexec.mainClass="com.rajan.lld.DesignPatterns.BehavioralDesignPatterns.ChainOfResponsibilityDesignPattern.ChainOfResponsibilityDesignPattern"
```

---

**Remember**: Use Chain of Responsibility when you want to decouple senders from receivers and give multiple objects a chance to handle the request. It's perfect for middleware, validation, and escalation scenarios!
