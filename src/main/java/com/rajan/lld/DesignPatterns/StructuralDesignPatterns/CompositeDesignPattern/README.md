# Composite Design Pattern 🌳

## 📖 Definition

The **Composite Pattern** is a structural design pattern that composes objects into tree structures to represent part-whole hierarchies. It lets clients treat individual objects and compositions of objects uniformly.

**In Simple Terms:**
- Treat single objects and groups of objects the same way
- Build tree structures (like folders containing files and other folders)
- One interface for both leaf (individual) and composite (group) objects
- Like a file system where files and folders share common operations

---

## 🎯 Core Concept

```
        Component (Interface)
             ↑
      ┌──────┴──────┐
      │             │
    Leaf        Composite
  (Individual)   (Group)
                   │
              Contains Components
```

Key participants:
1. **Component** - Common interface for leaf and composite
2. **Leaf** - Individual object (cannot contain others)
3. **Composite** - Can contain other components (leaf or composite)

---

## ❌ Problem: Different Handling for Individual vs Group

### Without Composite Pattern:

```java
class File {
    void display() { }
}

class Folder {
    List<File> files;
    void display() {
        for (File file : files) {
            file.display(); // Different handling!
        }
    }
}

// Problem: Can't treat files and folders uniformly
if (object instanceof File) {
    ((File) object).display();
} else if (object instanceof Folder) {
    ((Folder) object).display();
    // Must handle differently!
}
```

### Issues:
- ❌ Client must distinguish between leaf and composite
- ❌ Different code paths for single vs group operations
- ❌ Hard to add new types
- ❌ Code duplication for similar operations
- ❌ Violates Open/Closed Principle
- ❌ Can't build flexible tree structures

---

## ✅ Solution: Composite Pattern

### Step-by-Step Implementation:

#### Step 1: Component Interface
```java
interface FileSystemComponent {
    void display(String indent);
    int getSize();
    String getName();
}
```

#### Step 2: Leaf (Individual Object)
```java
class File implements FileSystemComponent {
    private String name;
    private int size;
    
    public File(String name, int size) {
        this.name = name;
        this.size = size;
    }
    
    @Override
    public void display(String indent) {
        System.out.println(indent + "📄 " + name + " (" + size + " KB)");
    }
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public String getName() {
        return name;
    }
}
```

#### Step 3: Composite (Group Object)
```java
class Folder implements FileSystemComponent {
    private String name;
    private List<FileSystemComponent> children;
    
    public Folder(String name) {
        this.name = name;
        this.children = new ArrayList<>();
    }
    
    public void add(FileSystemComponent component) {
        children.add(component);
    }
    
    public void remove(FileSystemComponent component) {
        children.remove(component);
    }
    
    @Override
    public void display(String indent) {
        System.out.println(indent + "📁 " + name + "/");
        for (FileSystemComponent child : children) {
            child.display(indent + "  "); // Recursive!
        }
    }
    
    @Override
    public int getSize() {
        int totalSize = 0;
        for (FileSystemComponent child : children) {
            totalSize += child.getSize(); // Recursive!
        }
        return totalSize;
    }
    
    @Override
    public String getName() {
        return name;
    }
}
```

#### Step 4: Client Usage (Uniform Treatment!)
```java
// Create files
File file1 = new File("resume.pdf", 150);
File file2 = new File("photo.jpg", 2048);

// Create folders
Folder documents = new Folder("Documents");
documents.add(file1);

Folder root = new Folder("Root");
root.add(documents);
root.add(file2);

// Uniform treatment - same method for both!
root.display("");        // Works on entire tree
file1.display("");       // Works on single file
documents.display("");   // Works on folder

// All implement same interface!
```

---

## 🔄 Before vs After Comparison

| Aspect | Without Composite ❌ | With Composite ✅ |
|--------|---------------------|-------------------|
| **Treatment** | Different for leaf and composite | Uniform for all |
| **Type Checking** | Must check instanceof | No type checking needed |
| **Code Complexity** | Complex branching logic | Simple recursive calls |
| **Flexibility** | Hard to add new types | Easy to extend |
| **Tree Building** | Manual and error-prone | Natural and intuitive |
| **Operations** | Duplicate code | Single implementation |
| **Maintainability** | Hard to maintain | Easy to maintain |

---

## 🌍 Real-World Use Cases

### 1. File System 📁
**Problem:** Files and folders need common operations  
**Solution:** Both implement FileSystemComponent

```java
Folder root = new Folder("Root");
root.add(new File("doc.pdf", 100));
root.add(new Folder("Pictures"));

root.display("");  // Works on entire tree!
int size = root.getSize();  // Calculates recursively
```

**Tree Structure:**
```
📁 Root/
  📄 doc.pdf (100 KB)
  📁 Pictures/
    📄 photo1.jpg (2048 KB)
    📄 photo2.jpg (1500 KB)
```

---

### 2. Organization Hierarchy 👔
**Problem:** Employees and managers need common operations  
**Solution:** Both implement Employee interface

```java
Manager cto = new Manager("Grace", 200000);
cto.addTeamMember(new Developer("Alice", "Senior", 120000));
cto.addTeamMember(new Manager("Bob", 150000));

cto.showDetails("");  // Shows entire org tree
double payroll = cto.getSalary();  // Total payroll
```

**Tree Structure:**
```
👔 CTO: Grace ($200,000)
   👨💻 Alice - Senior Developer ($120,000)
   👔 Manager: Bob ($150,000)
      👨💻 Charlie - Junior Developer ($80,000)
```

---

### 3. UI Components 🖼️
**Problem:** Panels can contain buttons, textboxes, and other panels  
**Solution:** All implement UIComponent

```java
Panel mainWindow = new Panel("Main Window");
mainWindow.add(new Button("Login"));
mainWindow.add(new Panel("Sidebar"));

mainWindow.render("");  // Renders entire UI tree
int count = mainWindow.getComponentCount();
```

**Tree Structure:**
```
📦 Main Window
  🔘 Button: Login
  📦 Sidebar
    🔘 Button: Home
    🔘 Button: Profile
```

---

### 4. Menu System 🍽️
**Problem:** Menus can contain items and submenus  
**Solution:** Both implement MenuComponent

```java
Menu mainMenu = new Menu("Restaurant Menu");
mainMenu.add(new MenuItem("Burger", "Beef burger", 12.99, false));
mainMenu.add(new Menu("Desserts"));

mainMenu.print("");  // Prints entire menu tree
double total = mainMenu.getPrice();  // Sum of all items
```

**Tree Structure:**
```
📋 Restaurant Menu
  🍖 Burger - $12.99
  📋 Desserts
    🌱 Cake - $6.99
    🌱 Ice Cream - $4.99
```

---

## 🏭 Industry Examples

### Java Standard Library:
1. **java.awt.Component** - UI components (Container, Button, etc.)
2. **javax.swing.JComponent** - Swing components
3. **org.w3c.dom.Node** - XML DOM tree

### Frameworks:
1. **React/Vue Components** - Nested component trees
2. **Android View Hierarchy** - ViewGroup and View
3. **JavaFX Scene Graph** - Parent and Node

### Real Applications:
1. **File Systems** - Files and directories
2. **Graphics Editors** - Shapes and groups
3. **Organization Charts** - Employees and departments
4. **GUI Frameworks** - Nested UI components

---

## ✅ Advantages

| Advantage | Description |
|-----------|-------------|
| **Uniform Treatment** | Same interface for leaf and composite |
| **Tree Structures** | Easy to build complex hierarchies |
| **Flexibility** | Easy to add new component types |
| **Simplicity** | Client code is simple and uniform |
| **Open/Closed** | Add new components without changing code |
| **Recursive Operations** | Operations work on entire tree naturally |
| **Polymorphism** | Leverage polymorphism effectively |

---

## ⚠️ Disadvantages

| Disadvantage | Description |
|--------------|-------------|
| **Overly General** | Hard to restrict component types |
| **Design Complexity** | May be overkill for simple cases |
| **Type Safety** | Harder to enforce type constraints |
| **Performance** | Recursive calls can be expensive |

---

## 🤔 When to Use

✅ **Use Composite Pattern when:**
- Need to represent part-whole hierarchies
- Want to treat individual and composite objects uniformly
- Building tree structures (files, UI, org charts)
- Operations should work on both single and group objects
- Need recursive tree traversal
- Want to ignore difference between leaf and composite

❌ **Don't use when:**
- Simple flat structure (no hierarchy)
- Need strict type constraints
- Performance is critical (avoid recursion)
- Leaf and composite are fundamentally different

---

## 🆚 Composite vs Similar Patterns

### Composite vs Decorator

| Aspect | Composite | Decorator |
|--------|-----------|-----------|
| **Purpose** | Represent hierarchies | Add responsibilities |
| **Structure** | Tree (one-to-many) | Chain (one-to-one) |
| **Focus** | Part-whole relationships | Enhancement |
| **Children** | Multiple children | Single wrapped object |
| **Example** | Folder with files | Coffee with decorators |

### Composite vs Flyweight

| Aspect | Composite | Flyweight |
|--------|-----------|-----------|
| **Purpose** | Tree structures | Share objects |
| **Memory** | Each object separate | Objects shared |
| **Focus** | Hierarchy | Memory optimization |
| **Relationship** | Parent-child | Shared instances |
| **Example** | File system | Character objects |

### Composite vs Chain of Responsibility

| Aspect | Composite | Chain of Responsibility |
|--------|-----------|------------------------|
| **Purpose** | Tree structures | Pass request along chain |
| **Structure** | Tree | Linear chain |
| **Processing** | All nodes process | One node processes |
| **Focus** | Hierarchy | Request handling |
| **Example** | Menu system | Support ticket routing |

---

## 💡 Implementation Tips

### 1. Component Interface Design
```java
// Good: Common operations for all components
interface Component {
    void operation();
    int getSize();
}

// Consider: Optional methods for composite-specific operations
interface Component {
    void operation();
    
    // Default implementations for leaf nodes
    default void add(Component c) {
        throw new UnsupportedOperationException();
    }
    
    default void remove(Component c) {
        throw new UnsupportedOperationException();
    }
}
```

### 2. Safety vs Transparency Trade-off
```java
// Transparent: add/remove in Component interface
// - Uniform interface
// - Leaf nodes must handle add/remove (throw exception)

// Safe: add/remove only in Composite
// - Type-safe
// - Client must know if object is composite
// - Loses uniformity

// Choose based on your needs!
```

### 3. Parent References
```java
class Component {
    private Component parent;
    
    public void setParent(Component parent) {
        this.parent = parent;
    }
    
    public Component getParent() {
        return parent;
    }
}

// Useful for:
// - Navigation (go up the tree)
// - Deletion (remove from parent)
// - Path calculation
```

### 4. Caching for Performance
```java
class Folder implements FileSystemComponent {
    private List<FileSystemComponent> children;
    private Integer cachedSize = null;
    
    @Override
    public int getSize() {
        if (cachedSize == null) {
            cachedSize = 0;
            for (FileSystemComponent child : children) {
                cachedSize += child.getSize();
            }
        }
        return cachedSize;
    }
    
    public void add(FileSystemComponent component) {
        children.add(component);
        cachedSize = null; // Invalidate cache
    }
}
```

---

## 🏋️ Practice Exercise

**Challenge:** Create a Graphics Drawing System

**Requirements:**
1. Basic shapes: Circle, Rectangle, Line
2. Group shapes together
3. Operations: draw(), move(x, y), resize(scale)
4. Groups can contain shapes and other groups
5. Calculate total area

**Example:**
```java
Shape circle = new Circle(5);
Shape rect = new Rectangle(10, 20);

Group group1 = new Group();
group1.add(circle);
group1.add(rect);

Group mainGroup = new Group();
mainGroup.add(group1);
mainGroup.add(new Line());

mainGroup.draw();  // Draws entire composition
mainGroup.move(10, 20);  // Moves all shapes
```

**Bonus:**
- Add color property
- Implement bounds calculation
- Add rotation
- Support z-order (layering)

---

## 🎯 Key Takeaways

1. **Composite represents tree structures** (part-whole hierarchies)
2. **Uniform treatment** of leaf and composite objects
3. **Same interface** for individual and group objects
4. **Recursive operations** work naturally on trees
5. **Common in UI frameworks** and file systems
6. **Trade-off**: Transparency vs type safety
7. **Use when** you need to treat objects uniformly regardless of composition

---

## 📊 Composite Pattern Structure

```
┌─────────────────┐
│    Component    │ ◄─── Common interface
│   (Interface)   │
└────────┬────────┘
         │
    ┌────┴────┐
    │         │
┌───▼───┐ ┌──▼──────┐
│ Leaf  │ │Composite│
└───────┘ └────┬────┘
               │
          Contains ───┐
               │      │
          ┌────▼──┐   │
          │ Leaf  │   │
          └───────┘   │
          ┌───────┐   │
          │Composite◄─┘
          └───────┘
```

---

## 📚 Related Patterns

- **Decorator** - Add responsibilities (linear chain)
- **Flyweight** - Share objects to save memory
- **Iterator** - Traverse composite structures
- **Visitor** - Operations on composite structures
- **Chain of Responsibility** - Linear chain vs tree

---

## 🔗 Next Steps

1. ✅ Understand leaf vs composite distinction
2. ✅ Practice building tree structures
3. ✅ Implement recursive operations
4. ✅ Consider transparency vs safety trade-off
5. ✅ Use in UI frameworks and file systems
6. ✅ Combine with Iterator for traversal
7. ✅ Combine with Visitor for operations

---

**Remember:** Composite is for **tree structures**, Decorator is for **linear chains**! 🌳
