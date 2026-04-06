# LLD Interview Problem Template 🎯

> **Purpose**: A systematic approach to solving Low-Level Design interview problems with emphasis on concurrency, scalability, and clean architecture.

---

## 📋 TEMPLATE OVERVIEW

This template guides you through solving any LLD problem systematically. Follow these steps in order during interviews.

### ⏱️ Time Allocation Guide

| Interview Length | Step Focus | Time Distribution |
|-----------------|------------|-------------------|
| **45 min** | Steps 1-5 (80%), Step 6 (20%) | Requirements → Design → Core Implementation |
| **60 min** | Steps 1-6 (70%), Step 7-8 (30%) | Add Concurrency + Testing Strategy |
| **90 min** | All Steps | Complete implementation with trade-offs |

---

## 🎯 STEP 1: REQUIREMENTS GATHERING (5-10 min)

> **🧠 MINDSET**: "Understand the problem deeply before writing any code"

### 📝 Questions to Ask

#### **Functional Requirements**
```
✅ What are the core features? (Must-have vs Nice-to-have)
✅ Who are the users/actors in the system?
✅ What are the main workflows/use cases?
✅ What are the business rules and constraints?
✅ What are the edge cases to handle?
```

#### **Non-Functional Requirements**
```
✅ Scale: How many users/requests/entities?
✅ Performance: Response time expectations?
✅ Concurrency: Multiple users accessing simultaneously?
✅ Persistence: In-memory or database?
✅ Availability: Uptime requirements?
```

#### **Clarifications**
```
✅ What can I assume vs what must I implement?
✅ Are there any specific constraints (memory, latency)?
✅ Should I focus on extensibility or simplicity?
```

### 📋 Requirements Documentation Template

```markdown
## [SYSTEM NAME] Requirements

### Functional Requirements
1. **FR1**: [Description]
2. **FR2**: [Description]
...

### Non-Functional Requirements
1. **NFR1**: Concurrency - [X] concurrent users
2. **NFR2**: Performance - [Y]ms response time
3. **NFR3**: Scale - [Z] entities
...

### Assumptions
1. [Assumption 1]
2. [Assumption 2]
...

### Out of Scope
1. [What we won't implement]
2. [Future enhancements]
```

---

## 🏗️ STEP 2: DOMAIN MODELING (5-10 min)

> **🧠 MINDSET**: "Identify the core entities and their relationships"

### 🎯 Entity Identification

**For each entity, document:**

```markdown
### Entity: [Name]
- **Purpose**: What does it represent?
- **Key Attributes**: Essential data fields
- **Responsibilities**: What can it do?
- **Relationships**: How does it relate to other entities?
- **Lifecycle**: Creation, updates, deletion
```

### 🔗 Relationship Types

| Type | Symbol | Example |
|------|--------|---------|
| **One-to-One** | 1:1 | User ↔ Profile |
| **One-to-Many** | 1:N | Order → OrderItems |
| **Many-to-Many** | M:N | Students ↔ Courses |
| **Composition** | ◆ | Car ◆→ Engine (strong ownership) |
| **Aggregation** | ◇ | Department ◇→ Employees (weak ownership) |

### 📊 Domain Model Diagram

```
[Draw or describe entity relationships]

Example:
User (1) ──has──> (N) Bookings
Booking (N) ──for──> (1) Resource
Resource (1) ──belongs to──> (1) Location
```

---

## 🎨 STEP 3: DESIGN PATTERNS & ARCHITECTURE (10-15 min)

> **🧠 MINDSET**: "Choose patterns that solve specific problems, not for the sake of patterns"

### 🏛️ Architecture Layers

```
┌─────────────────────────────────────┐
│     API/Controller Layer            │ ← Entry points
├─────────────────────────────────────┤
│     Service/Business Logic Layer    │ ← Core logic
├─────────────────────────────────────┤
│     Repository/Data Access Layer    │ ← Data operations
├─────────────────────────────────────┤
│     Model/Domain Layer              │ ← Entities
└─────────────────────────────────────┘
```

### 🎯 Pattern Selection Matrix

| Problem | Pattern | Why Use It |
|---------|---------|------------|
| **Object creation complexity** | Factory/Builder | Encapsulate creation logic |
| **Single instance needed** | Singleton | Shared resource (use carefully!) |
| **Algorithm variations** | Strategy | Swap algorithms at runtime |
| **State-dependent behavior** | State | Clean state transitions |
| **Notify multiple objects** | Observer | Event-driven updates |
| **Add features dynamically** | Decorator | Flexible enhancement |
| **Simplify complex subsystem** | Facade | Hide complexity |
| **Control access** | Proxy | Lazy loading, caching, security |

### 📝 Pattern Documentation Template

```markdown
### Pattern: [Name]
- **Problem**: What problem does it solve?
- **Solution**: How does it solve it?
- **Implementation**: Where/how used in this system?
- **Trade-offs**: What are the costs?
```

---

## 🔐 STEP 4: CONCURRENCY CONTROL (CRITICAL!) ⚠️

> **🧠 MINDSET**: "Identify shared resources and protect them from race conditions"

### 🎯 Concurrency Analysis Checklist

```
✅ What data is shared across threads?
✅ What operations modify shared state?
✅ What are the critical sections?
✅ What are potential race conditions?
✅ What is the expected concurrency level?
```

### 🔒 Concurrency Strategies

#### **1. Synchronization Levels**

| Level | Approach | Use When | Example |
|-------|----------|----------|---------|
| **Method-level** | `synchronized method` | Simple, coarse-grained | `synchronized void book()` |
| **Block-level** | `synchronized(lock)` | Fine-grained control | Lock specific section |
| **Read-Write** | `ReentrantReadWriteLock` | Many reads, few writes | Cache access |
| **Lock-free** | `AtomicInteger`, `ConcurrentHashMap` | High contention | Counters, maps |

#### **2. Thread-Safe Data Structures**

```java
// Choose based on use case:
ConcurrentHashMap<K,V>      // Thread-safe map
CopyOnWriteArrayList<E>     // Read-heavy lists
BlockingQueue<E>            // Producer-consumer
AtomicInteger/AtomicLong    // Counters
```

#### **3. Concurrency Patterns**

**Pattern 1: Double-Checked Locking (Singleton)**
```java
private static volatile Instance instance;

public static Instance getInstance() {
    if (instance == null) {
        synchronized (Instance.class) {
            if (instance == null) {
                instance = new Instance();
            }
        }
    }
    return instance;
}
```

**Pattern 2: Read-Write Lock**
```java
private final ReadWriteLock lock = new ReentrantReadWriteLock();

public Data read() {
    lock.readLock().lock();
    try {
        return data;
    } finally {
        lock.readLock().unlock();
    }
}

public void write(Data newData) {
    lock.writeLock().lock();
    try {
        data = newData;
    } finally {
        lock.writeLock().unlock();
    }
}
```

**Pattern 3: Atomic Operations**
```java
private AtomicInteger counter = new AtomicInteger(0);

public int incrementAndGet() {
    return counter.incrementAndGet(); // Thread-safe
}
```

### 🚨 Common Concurrency Issues

| Issue | Description | Solution |
|-------|-------------|----------|
| **Race Condition** | Multiple threads modify shared data | Synchronization |
| **Deadlock** | Threads wait for each other | Lock ordering, timeouts |
| **Starvation** | Thread never gets resources | Fair locks |
| **Livelock** | Threads keep changing state | Randomized backoff |

### 📝 Concurrency Documentation Template

```markdown
## Concurrency Strategy

### Shared Resources
1. **[Resource Name]**
   - **Access Pattern**: Read-heavy / Write-heavy / Mixed
   - **Protection**: [Synchronization mechanism]
   - **Justification**: [Why this approach]

### Critical Sections
1. **[Operation Name]**
   - **What**: What is being protected
   - **Why**: What race condition it prevents
   - **How**: Synchronization mechanism used

### Thread-Safety Guarantees
- [List what operations are thread-safe]
- [List what operations require external synchronization]

### Performance Considerations
- **Lock Contention**: [How minimized]
- **Scalability**: [How system scales with threads]
```

---

## 💻 STEP 5: CLASS DESIGN & IMPLEMENTATION (20-30 min)

> **🧠 MINDSET**: "Write clean, minimal code that solves the problem"

### 🎯 Class Structure Template

```java
/**
 * [Class Purpose]
 * 
 * Thread-Safety: [Thread-safe / Not thread-safe / Conditionally thread-safe]
 * Concurrency: [Synchronization strategy if applicable]
 */
public class ClassName {
    // 1. CONSTANTS
    private static final int MAX_CAPACITY = 100;
    
    // 2. INSTANCE VARIABLES (with thread-safety annotations)
    private final Object lock = new Object(); // Explicit lock object
    private volatile int counter; // Volatile for visibility
    private final ConcurrentHashMap<K, V> map; // Thread-safe collection
    
    // 3. CONSTRUCTOR
    public ClassName() {
        // Initialize thread-safe structures
        this.map = new ConcurrentHashMap<>();
    }
    
    // 4. PUBLIC METHODS (document thread-safety)
    /**
     * Thread-safe method using synchronization
     */
    public synchronized void synchronizedMethod() {
        // Implementation
    }
    
    /**
     * Thread-safe method using explicit lock
     */
    public void explicitLockMethod() {
        synchronized(lock) {
            // Critical section
        }
    }
    
    /**
     * Thread-safe using atomic operations
     */
    public void atomicMethod() {
        // Use atomic operations
    }
    
    // 5. PRIVATE HELPER METHODS
    private void helperMethod() {
        // Assume caller holds lock if needed
    }
}
```

### 🎯 SOLID Principles Checklist

```
✅ Single Responsibility: Each class has one reason to change
✅ Open/Closed: Open for extension, closed for modification
✅ Liskov Substitution: Subtypes are substitutable
✅ Interface Segregation: Small, focused interfaces
✅ Dependency Inversion: Depend on abstractions
```

### 🎯 Code Quality Checklist

```
✅ Meaningful names (no abbreviations)
✅ Small methods (< 20 lines)
✅ No magic numbers (use constants)
✅ Proper error handling
✅ Thread-safety documented
✅ Null checks where needed
✅ Input validation
```

---

## 🧪 STEP 6: TESTING STRATEGY (5-10 min)

> **🧠 MINDSET**: "Think about testing while designing, not after"

### 🎯 Test Categories

#### **1. Unit Tests (70%)**
```java
@Test
public void testHappyPath() {
    // Given: Setup
    // When: Execute
    // Then: Verify
}

@Test
public void testEdgeCase() {
    // Test boundaries
}

@Test
public void testErrorHandling() {
    // Test invalid inputs
}
```

#### **2. Concurrency Tests (20%)**
```java
@Test
public void testConcurrentAccess() throws InterruptedException {
    ExecutorService executor = Executors.newFixedThreadPool(10);
    CountDownLatch latch = new CountDownLatch(100);
    
    for (int i = 0; i < 100; i++) {
        executor.submit(() -> {
            try {
                // Concurrent operation
            } finally {
                latch.countDown();
            }
        });
    }
    
    latch.await();
    // Verify consistency
}

@Test
public void testNoRaceCondition() {
    // Verify atomic operations
}
```

#### **3. Integration Tests (10%)**
```java
@Test
public void testCompleteWorkflow() {
    // Test end-to-end scenario
}
```

### 📝 Test Documentation Template

```markdown
## Test Strategy

### Unit Tests
- [List key scenarios to test]

### Concurrency Tests
- **Race Condition Tests**: [What to verify]
- **Deadlock Tests**: [How to detect]
- **Performance Tests**: [Load testing approach]

### Edge Cases
- [List boundary conditions]
```

---

## 📊 STEP 7: SCALABILITY & TRADE-OFFS (5-10 min)

> **🧠 MINDSET**: "Every design decision has trade-offs"

### ⚖️ Trade-offs Analysis

```markdown
## Design Trade-offs

### Decision: [What you chose]
**Pros:**
- [Benefit 1]
- [Benefit 2]

**Cons:**
- [Cost 1]
- [Cost 2]

**Alternatives Considered:**
- [Alternative 1]: Why not chosen
- [Alternative 2]: Why not chosen

**When to Reconsider:**
- [Condition that would make you change]
```

### 📈 Scalability Analysis

```markdown
## Scalability

### Current Limitations
- **Bottleneck**: [What limits scale]
- **Breaking Point**: [When system fails]

### Scaling Strategies
1. **Vertical**: [Bigger machine approach]
2. **Horizontal**: [More machines approach]
3. **Caching**: [What to cache]
4. **Partitioning**: [How to shard]
5. **Async Processing**: [What to decouple]

### Performance Characteristics
- **Time Complexity**: O(?) for main operations
- **Space Complexity**: O(?) for data storage
- **Concurrency**: [How it scales with threads]
```

---

## 🚀 STEP 8: EXTENSIBILITY & FUTURE ENHANCEMENTS

> **🧠 MINDSET**: "Design for change"

### 🔮 Extension Points

```markdown
## How to Extend

### Adding New Entity Types
1. Implement [Interface/Base Class]
2. Register in [Factory/Registry]
3. Update [Configuration]

### Adding New Features
1. Create new [Service/Component]
2. Integrate with [Existing Component]
3. Update [API/Interface]

### Adding External Integrations
1. Create [Adapter/Wrapper]
2. Handle [Failures/Timeouts]
3. Transform [Data Format]
```

### 📋 Future Roadmap

```markdown
## Future Enhancements

### Phase 1: Immediate (< 1 month)
- [Enhancement 1]
- [Enhancement 2]

### Phase 2: Short-term (1-3 months)
- [Feature 1]
- [Feature 2]

### Phase 3: Long-term (3-6 months)
- [Major feature 1]
- [Architectural change]
```

---

## 🎯 STEP 9: INTERVIEW EVALUATION CHECKLIST

> **🧠 MINDSET**: "What the interviewer is looking for"

### ✅ Requirements (20%)
- [ ] Asked clarifying questions
- [ ] Identified functional vs non-functional requirements
- [ ] Made reasonable assumptions
- [ ] Understood problem domain

### ✅ Design (30%)
- [ ] Clear architecture with proper layers
- [ ] Appropriate design patterns
- [ ] SOLID principles followed
- [ ] Extensible design
- [ ] Good abstractions

### ✅ Concurrency (20%)
- [ ] Identified shared resources
- [ ] Proper synchronization strategy
- [ ] Prevented race conditions
- [ ] Considered deadlocks
- [ ] Documented thread-safety

### ✅ Code Quality (20%)
- [ ] Clean, readable code
- [ ] Proper naming conventions
- [ ] Error handling
- [ ] Input validation
- [ ] Minimal and focused

### ✅ Communication (10%)
- [ ] Explained thought process
- [ ] Discussed trade-offs
- [ ] Responded to feedback
- [ ] Managed time well
- [ ] Stayed organized

---

## 📝 STEP 10: HOW TO RUN

```bash
# Compilation
javac -d bin src/**/*.java

# Execution
java -cp bin com.company.SystemName

# Testing
java -cp bin:junit.jar org.junit.runner.JUnitCore TestClass
```

### Expected Output
```
[Show sample input/output]
```

---

## 🎓 INTERVIEW SUCCESS TIPS

### ⏱️ Time Management

| Phase | Time | Focus |
|-------|------|-------|
| **Requirements** | 10% | Clarify and document |
| **Design** | 30% | Architecture and patterns |
| **Implementation** | 40% | Core functionality |
| **Testing/Discussion** | 20% | Edge cases and trade-offs |

### 🎯 Common Mistakes to Avoid

```
❌ Jumping to code without design
❌ Ignoring concurrency requirements
❌ Over-engineering with unnecessary patterns
❌ Not considering scalability
❌ Poor time management
❌ Not testing edge cases
❌ Ignoring error handling
```

### ✅ How to Stand Out

```
✅ Systematic approach (use this template!)
✅ Strong concurrency understanding
✅ Clear communication of trade-offs
✅ Testing mindset throughout
✅ Real-world considerations
✅ Clean, minimal code
```

---

## 🎯 QUICK REFERENCE CARD

### Interview Flow
```
1. Clarify Requirements (5-10 min)
2. Design Architecture (10-15 min)
3. Identify Concurrency Needs (5 min)
4. Implement Core Logic (20-30 min)
5. Discuss Testing & Trade-offs (10 min)
```

### Concurrency Quick Check
```
□ What is shared?
□ What is modified?
□ What synchronization?
□ What are race conditions?
□ How to test?
```

### Code Quality Quick Check
```
□ SOLID principles?
□ Proper naming?
□ Error handling?
□ Thread-safety documented?
□ Edge cases handled?
```

---

**Remember**: This template is a guide, not a rigid checklist. Adapt based on:
- Problem complexity
- Interview length
- Interviewer's focus
- Your strengths

**Focus on**: Clear thinking, clean code, and strong concurrency understanding! 🚀
