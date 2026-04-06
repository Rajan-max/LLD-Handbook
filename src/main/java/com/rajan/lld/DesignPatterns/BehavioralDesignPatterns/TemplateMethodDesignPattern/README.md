# Template Method Design Pattern

## 📖 Definition

The **Template Method Pattern** defines the skeleton of an algorithm in a method, deferring some steps to subclasses. Template Method lets subclasses redefine certain steps of an algorithm without changing the algorithm's structure.

**In simple terms**: Define the overall algorithm structure in a base class, let subclasses fill in the specific steps - the recipe stays the same, ingredients vary!

## 🎯 Core Concept

The Template Method pattern:
- Defines the algorithm structure in an abstract class
- Uses abstract methods for steps that vary
- Uses concrete methods for common steps
- Provides hook methods for optional customization
- Prevents subclasses from changing the algorithm structure

**Key Components**:
1. **Abstract Class**: Contains template method and defines algorithm structure
2. **Template Method**: Final method that defines the algorithm skeleton
3. **Abstract Methods**: Must be implemented by subclasses (varying steps)
4. **Concrete Methods**: Common implementation shared by all subclasses
5. **Hook Methods**: Optional methods with default implementation

## ❌ Problem: Without Template Method Pattern

### The Code Duplication Problem

```java
class BadCoffee {
    public void prepareRecipe() {
        System.out.println("Boiling water");
        System.out.println("Dripping coffee through filter");
        System.out.println("Pouring into cup");
        System.out.println("Adding sugar and milk");
    }
}

class BadTea {
    public void prepareRecipe() {
        System.out.println("Boiling water");  // Duplicated!
        System.out.println("Steeping the tea");
        System.out.println("Pouring into cup");  // Duplicated!
        System.out.println("Adding lemon");
    }
}

class BadHotChocolate {
    public void prepareRecipe() {
        // Oops! Forgot to boil water - inconsistent!
        System.out.println("Adding chocolate powder");
        System.out.println("Pouring into cup");
        System.out.println("Adding marshmallows");
    }
}
```

### Why This Is Bad:

| Problem | Description |
|---------|-------------|
| **Code Duplication** | Same steps repeated in multiple classes |
| **Inconsistent** | Easy to miss steps or change order |
| **Hard to Maintain** | Changes must be made in multiple places |
| **No Control** | Can't enforce algorithm structure |
| **Violates DRY** | Don't Repeat Yourself principle violated |
| **Error-Prone** | Easy to forget steps (like boiling water) |

## ✅ Solution: Template Method Pattern

### Step-by-Step Implementation

**Step 1: Create Abstract Class with Template Method**
```java
abstract class Beverage {
    // Template Method - final to prevent overriding
    public final void prepareRecipe() {
        boilWater();
        brew();
        pourInCup();
        addCondiments();
    }
    
    // Abstract methods - subclasses must implement
    protected abstract void brew();
    protected abstract void addCondiments();
    
    // Concrete methods - common to all
    private void boilWater() {
        System.out.println("Boiling water");
    }
    
    private void pourInCup() {
        System.out.println("Pouring into cup");
    }
}
```

**Step 2: Create Concrete Subclasses**
```java
class Coffee extends Beverage {
    @Override
    protected void brew() {
        System.out.println("Dripping coffee through filter");
    }
    
    @Override
    protected void addCondiments() {
        System.out.println("Adding sugar and milk");
    }
}

class Tea extends Beverage {
    @Override
    protected void brew() {
        System.out.println("Steeping the tea");
    }
    
    @Override
    protected void addCondiments() {
        System.out.println("Adding lemon");
    }
}
```

**Step 3: Add Hook Methods (Optional)**
```java
abstract class Beverage {
    public final void prepareRecipe() {
        boilWater();
        brew();
        pourInCup();
        addCondiments();
        if (customerWantsCondiments()) {  // Hook method
            addExtraCondiments();
        }
    }
    
    // Hook method - can be overridden
    protected boolean customerWantsCondiments() {
        return true;  // Default behavior
    }
    
    protected void addExtraCondiments() {
        // Default: do nothing
    }
}
```

**Step 4: Use the Pattern**
```java
Beverage coffee = new Coffee();
coffee.prepareRecipe();  // Follows the template

Beverage tea = new Tea();
tea.prepareRecipe();  // Same structure, different steps
```

## 📊 Comparison: Before vs After

| Aspect | Without Template Method | With Template Method |
|--------|------------------------|---------------------|
| **Code duplication** | High (repeated steps) | Low (common steps in one place) |
| **Consistency** | Hard to maintain | Enforced by template |
| **Adding new type** | Copy-paste and modify | Extend and implement |
| **Algorithm control** | No control | Controlled by base class |
| **Maintainability** | Low | High |
| **DRY Principle** | ❌ Violated | ✅ Followed |
| **Flexibility** | High (too much) | Controlled (just right) |

## 🌍 Real-World Use Cases

### 1. Beverage Preparation
```java
// Template: boilWater → brew → pourInCup → addCondiments
- Coffee: drip coffee, add milk
- Tea: steep tea, add lemon
- HotChocolate: mix powder, add marshmallows
```

### 2. Data Processing Pipeline
```java
// Template: readData → processData → validateData → saveData
- CSVProcessor: read CSV, process rows, save to DB
- JSONProcessor: read JSON, parse objects, save to NoSQL
- XMLProcessor: read XML, parse nodes, save to file
```

### 3. Game Character Actions
```java
// Template: prepare → execute → finish → specialAbility
- Warrior: sword attack + berserker rage
- Mage: cast spell + meteor storm
- Archer: shoot arrow (no special ability)
```

### 4. House Construction
```java
// Template: foundation → walls → roof → garage → pool → furnish
- WoodenHouse: wooden walls, no extras
- ConcreteHouse: concrete walls, with garage
- LuxuryHouse: marble walls, garage + pool
```

### 5. Document Generation
```java
// Template: header → body → footer → watermark
- PDFDocument: PDF-specific formatting
- WordDocument: Word-specific formatting
- HTMLDocument: HTML-specific formatting
```

### 6. Test Framework
```java
// Template: setUp → runTest → tearDown
- UnitTest: specific test logic
- IntegrationTest: specific integration logic
- E2ETest: specific end-to-end logic
```

## 💼 Industry Examples

| Application | Template Method Use |
|-------------|-------------------|
| **Testing Frameworks** | JUnit's setUp/test/tearDown pattern |
| **Web Frameworks** | Request handling pipeline (parse → validate → process → respond) |
| **Data Processing** | ETL pipelines (extract → transform → load) |
| **Game Development** | Game loop (input → update → render) |
| **Build Tools** | Build process (clean → compile → test → package) |
| **Workflow Systems** | Process workflows with fixed steps |

## ✅ Advantages

1. **Eliminates Duplication**: Common code in one place
2. **Enforces Structure**: Algorithm structure is controlled
3. **Easy to Extend**: Add new variations by extending
4. **Follows DRY**: Don't Repeat Yourself principle
5. **Consistent Behavior**: All subclasses follow same structure
6. **Flexible**: Hook methods allow optional customization
7. **Maintainable**: Changes to common steps in one place

## ❌ Disadvantages

1. **Inheritance Required**: Must use inheritance (not composition)
2. **Limited Flexibility**: Can't change algorithm structure
3. **Tight Coupling**: Subclasses coupled to base class
4. **Liskov Substitution**: Must be careful not to violate LSP
5. **Debugging Difficulty**: Flow jumps between base and subclass
6. **Template Bloat**: Too many steps can make template complex

## 🎓 When to Use

### ✅ Use Template Method Pattern When:
- Multiple classes share similar algorithm structure
- Want to avoid code duplication
- Need to control which parts can be customized
- Common behavior should be in one place
- Algorithm has well-defined steps
- Want to enforce consistent process across implementations

### ❌ Avoid Template Method Pattern When:
- Algorithm structure varies significantly
- Need composition over inheritance
- Steps are completely independent
- Only one or two implementations exist
- Flexibility is more important than consistency
- Algorithm is too simple to warrant abstraction

## 🔄 Template Method vs Other Patterns

### Template Method vs Strategy
| Aspect | Template Method | Strategy |
|--------|----------------|----------|
| **Mechanism** | Inheritance | Composition |
| **Flexibility** | Fixed structure, varying steps | Entire algorithm varies |
| **Granularity** | Parts of algorithm | Whole algorithm |
| **Coupling** | Tight (inheritance) | Loose (composition) |
| **Example** | Beverage preparation | Payment methods |

### Template Method vs Factory Method
| Aspect | Template Method | Factory Method |
|--------|----------------|----------------|
| **Purpose** | Define algorithm structure | Create objects |
| **Focus** | Behavior | Object creation |
| **Steps** | Multiple steps | Single creation step |
| **Example** | Data processing | Product creation |

## 💡 Implementation Tips

1. **Make Template Method Final**: Prevent subclasses from changing structure
2. **Use Protected Methods**: Allow subclass access but hide from clients
3. **Minimize Abstract Methods**: Only make varying steps abstract
4. **Provide Hook Methods**: Allow optional customization
5. **Document Template**: Clearly document the algorithm flow
6. **Keep Steps Cohesive**: Each step should have single responsibility
7. **Consider Naming**: Use descriptive names for template and steps

### Hook Methods Best Practices

**Hook Method Types**:
```java
// Boolean hook - control flow
protected boolean needsValidation() {
    return true;
}

// Empty hook - optional behavior
protected void logOperation() {
    // Default: do nothing
}

// Default implementation hook
protected String getDefaultValue() {
    return "default";
}
```

## 🧪 Practice Exercise

### Challenge: Report Generation System

Create a report generation system that produces different types of reports.

**Requirements**:
- Template: header → fetchData → formatData → generateChart → footer
- Report types: SalesReport, InventoryReport, FinancialReport
- All reports have header and footer
- Each report fetches and formats data differently
- Only SalesReport and FinancialReport need charts
- Use hook method for optional chart generation

**Hints**:
1. Create abstract `Report` class with `generateReport()` template method
2. Define abstract methods: `fetchData()`, `formatData()`
3. Implement concrete methods: `printHeader()`, `printFooter()`
4. Add hook method: `needsChart()` returning false by default
5. Add hook method: `generateChart()` with empty default implementation
6. Create concrete classes: `SalesReport`, `InventoryReport`, `FinancialReport`

<details>
<summary>💡 Solution Outline</summary>

```java
abstract class Report {
    // Template Method
    public final void generateReport() {
        printHeader();
        fetchData();
        formatData();
        if (needsChart()) {
            generateChart();
        }
        printFooter();
    }
    
    // Abstract methods
    protected abstract void fetchData();
    protected abstract void formatData();
    
    // Concrete methods
    private void printHeader() {
        System.out.println("=== Report Header ===");
    }
    
    private void printFooter() {
        System.out.println("=== Report Footer ===");
    }
    
    // Hook methods
    protected boolean needsChart() {
        return false;
    }
    
    protected void generateChart() {
        // Default: do nothing
    }
}

class SalesReport extends Report {
    protected void fetchData() {
        System.out.println("Fetching sales data...");
    }
    
    protected void formatData() {
        System.out.println("Formatting sales data...");
    }
    
    protected boolean needsChart() {
        return true;
    }
    
    protected void generateChart() {
        System.out.println("Generating sales chart...");
    }
}
```

</details>

## 🎯 Key Takeaways

1. **Algorithm Skeleton**: Define structure in base class
2. **Varying Steps**: Subclasses implement specific steps
3. **Code Reuse**: Common steps in one place
4. **Controlled Flexibility**: Structure fixed, steps vary
5. **Hook Methods**: Optional customization points
6. **Final Template**: Prevent structure changes

## 📚 Related Patterns

- **Strategy**: Alternative using composition instead of inheritance
- **Factory Method**: Often used within template methods
- **Builder**: Similar step-by-step approach but for object construction
- **Command**: Can encapsulate steps as commands
- **State**: Can be used to vary algorithm based on state

## 🔗 Java Standard Library Examples

- `java.io.InputStream` - read() template with abstract methods
- `java.io.OutputStream` - write() template with abstract methods
- `java.util.AbstractList` - get() and size() abstract methods
- `java.util.AbstractSet` - iterator() and size() abstract methods
- `javax.servlet.http.HttpServlet` - doGet(), doPost() template methods
- JUnit's `@Before`, `@Test`, `@After` annotations

## 🏗️ Template Method Structure

```
Algorithm Flow:

┌─────────────────────────────────┐
│   Template Method (final)       │
│   ┌─────────────────────────┐   │
│   │ 1. Common Step          │   │ ← Concrete method
│   └─────────────────────────┘   │
│   ┌─────────────────────────┐   │
│   │ 2. Varying Step         │   │ ← Abstract method
│   └─────────────────────────┘   │
│   ┌─────────────────────────┐   │
│   │ 3. Common Step          │   │ ← Concrete method
│   └─────────────────────────┘   │
│   ┌─────────────────────────┐   │
│   │ 4. Optional Step (hook) │   │ ← Hook method
│   └─────────────────────────┘   │
└─────────────────────────────────┘
```

---

## 🚀 Running the Demo

```bash
# Compile
mvn clean compile

# Run
mvn exec:java -Dexec.mainClass="com.rajan.lld.DesignPatterns.BehavioralDesignPatterns.TemplateMethodDesignPattern.TemplateMethodDesignPattern"
```

---

**Remember**: Use Template Method Pattern when you have multiple classes that follow the same algorithm structure but differ in specific steps. It's perfect for eliminating code duplication while maintaining consistency!
