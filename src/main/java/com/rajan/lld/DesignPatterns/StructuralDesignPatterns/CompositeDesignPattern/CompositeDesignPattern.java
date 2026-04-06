package com.rajan.lld.DesignPatterns.StructuralDesignPatterns.CompositeDesignPattern;

import java.util.ArrayList;
import java.util.List;

/**
 * COMPOSITE DESIGN PATTERN - Complete Example
 * 
 * Definition: Composes objects into tree structures to represent part-whole hierarchies.
 * Lets clients treat individual objects and compositions uniformly.
 * 
 * In simple terms:
 * - Treat single objects and groups of objects the same way
 * - Build tree structures (like folders containing files and other folders)
 * - One interface for both leaf and composite objects
 * - Like a file system: files and folders both have common operations
 * 
 * When to use:
 * - Represent part-whole hierarchies (tree structures)
 * - Want clients to ignore difference between individual and composite objects
 * - Need recursive tree structures
 * - Operations should work on both single and group objects
 */

// ============================================================================
// PROBLEM - Without Composite Pattern
// ============================================================================

/**
 * PROBLEM: Different handling for individual vs group objects
 * 
 * Why is this bad?
 * - Client must distinguish between leaf and composite objects
 * - Different code paths for single vs group operations
 * - Hard to add new types
 * - Code duplication for similar operations
 * - Violates Open/Closed Principle
 */

class SimpleFile {
    private String name;
    public void display() {
        System.out.println("File: " + name);
    }
}

class SimpleFolder {
    private String name;
    private List<SimpleFile> files;
    
    public void display() {
        System.out.println("Folder: " + name);
        for (SimpleFile file : files) {
            file.display(); // Different handling!
        }
    }
}

// Problem: Can't treat files and folders uniformly!
// Must check type and handle differently everywhere


// ============================================================================
// SOLUTION - Composite Pattern
// ============================================================================

/**
 * Step 1: Component Interface (common interface for leaf and composite)
 */
interface FileSystemComponent {
    void display(String indent);
    int getSize();
    String getName();
}

/**
 * Step 2: Leaf (individual object - cannot contain others)
 */
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

/**
 * Step 3: Composite (can contain other components)
 */
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
            child.display(indent + "  ");
        }
    }
    
    @Override
    public int getSize() {
        int totalSize = 0;
        for (FileSystemComponent child : children) {
            totalSize += child.getSize();
        }
        return totalSize;
    }
    
    @Override
    public String getName() {
        return name;
    }
}


// ============================================================================
// REAL-WORLD EXAMPLE 1 - Organization Hierarchy
// ============================================================================

/**
 * Company organization with employees and departments
 */

interface Employee {
    void showDetails(String indent);
    double getSalary();
    String getName();
}

class Developer implements Employee {
    private String name;
    private double salary;
    private String position;
    
    public Developer(String name, String position, double salary) {
        this.name = name;
        this.position = position;
        this.salary = salary;
    }
    
    @Override
    public void showDetails(String indent) {
        System.out.println(indent + "👨‍💻 " + name + " - " + position + " ($" + salary + ")");
    }
    
    @Override
    public double getSalary() {
        return salary;
    }
    
    @Override
    public String getName() {
        return name;
    }
}

class Manager implements Employee {
    private String name;
    private double salary;
    private List<Employee> team;
    
    public Manager(String name, double salary) {
        this.name = name;
        this.salary = salary;
        this.team = new ArrayList<>();
    }
    
    public void addTeamMember(Employee employee) {
        team.add(employee);
    }
    
    public void removeTeamMember(Employee employee) {
        team.remove(employee);
    }
    
    @Override
    public void showDetails(String indent) {
        System.out.println(indent + "👔 Manager: " + name + " ($" + salary + ")");
        System.out.println(indent + "   Team:");
        for (Employee member : team) {
            member.showDetails(indent + "   ");
        }
    }
    
    @Override
    public double getSalary() {
        double totalSalary = salary;
        for (Employee member : team) {
            totalSalary += member.getSalary();
        }
        return totalSalary;
    }
    
    @Override
    public String getName() {
        return name;
    }
}


// ============================================================================
// REAL-WORLD EXAMPLE 2 - UI Components (Graphics)
// ============================================================================

/**
 * UI components that can be nested
 */

interface UIComponent {
    void render(String indent);
    int getComponentCount();
}

class Button implements UIComponent {
    private String label;
    
    public Button(String label) {
        this.label = label;
    }
    
    @Override
    public void render(String indent) {
        System.out.println(indent + "🔘 Button: " + label);
    }
    
    @Override
    public int getComponentCount() {
        return 1;
    }
}

class TextBox implements UIComponent {
    private String placeholder;
    
    public TextBox(String placeholder) {
        this.placeholder = placeholder;
    }
    
    @Override
    public void render(String indent) {
        System.out.println(indent + "📝 TextBox: " + placeholder);
    }
    
    @Override
    public int getComponentCount() {
        return 1;
    }
}

class Panel implements UIComponent {
    private String name;
    private List<UIComponent> components;
    
    public Panel(String name) {
        this.name = name;
        this.components = new ArrayList<>();
    }
    
    public void add(UIComponent component) {
        components.add(component);
    }
    
    public void remove(UIComponent component) {
        components.remove(component);
    }
    
    @Override
    public void render(String indent) {
        System.out.println(indent + "📦 Panel: " + name);
        for (UIComponent component : components) {
            component.render(indent + "  ");
        }
    }
    
    @Override
    public int getComponentCount() {
        int count = 1; // Count self
        for (UIComponent component : components) {
            count += component.getComponentCount();
        }
        return count;
    }
}


// ============================================================================
// REAL-WORLD EXAMPLE 3 - Menu System
// ============================================================================

/**
 * Restaurant menu with items and submenus
 */

interface MenuComponent {
    void print(String indent);
    double getPrice();
    boolean isVegetarian();
}

class MenuItem implements MenuComponent {
    private String name;
    private String description;
    private double price;
    private boolean vegetarian;
    
    public MenuItem(String name, String description, double price, boolean vegetarian) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.vegetarian = vegetarian;
    }
    
    @Override
    public void print(String indent) {
        String vegIcon = vegetarian ? "🌱" : "🍖";
        System.out.println(indent + vegIcon + " " + name + " - $" + price);
        System.out.println(indent + "   " + description);
    }
    
    @Override
    public double getPrice() {
        return price;
    }
    
    @Override
    public boolean isVegetarian() {
        return vegetarian;
    }
}

class Menu implements MenuComponent {
    private String name;
    private List<MenuComponent> items;
    
    public Menu(String name) {
        this.name = name;
        this.items = new ArrayList<>();
    }
    
    public void add(MenuComponent component) {
        items.add(component);
    }
    
    public void remove(MenuComponent component) {
        items.remove(component);
    }
    
    @Override
    public void print(String indent) {
        System.out.println(indent + "📋 " + name);
        System.out.println(indent + "─".repeat(30));
        for (MenuComponent item : items) {
            item.print(indent + "  ");
        }
    }
    
    @Override
    public double getPrice() {
        double total = 0;
        for (MenuComponent item : items) {
            total += item.getPrice();
        }
        return total;
    }
    
    @Override
    public boolean isVegetarian() {
        for (MenuComponent item : items) {
            if (!item.isVegetarian()) {
                return false;
            }
        }
        return true;
    }
}


// ============================================================================
// DEMO - All Composite Examples
// ============================================================================

public class CompositeDesignPattern {
    public static void main(String[] args) {
        System.out.println("=".repeat(70));
        System.out.println("COMPOSITE DESIGN PATTERN DEMO");
        System.out.println("=".repeat(70));
        
        // PROBLEM Demo
        System.out.println("\n❌ PROBLEM: Without Composite Pattern");
        System.out.println("-".repeat(70));
        System.out.println("Must handle files and folders differently:");
        System.out.println("  if (object instanceof File) {");
        System.out.println("      file.display();");
        System.out.println("  } else if (object instanceof Folder) {");
        System.out.println("      folder.display();");
        System.out.println("      for (File f : folder.getFiles()) {");
        System.out.println("          f.display(); // Different handling!");
        System.out.println("      }");
        System.out.println("  }");
        System.out.println("\n⚠️  Can't treat individual and composite objects uniformly!");
        
        // SOLUTION Demo
        System.out.println("\n✅ SOLUTION: With Composite Pattern");
        System.out.println("-".repeat(70));
        
        // 1. File System Example
        System.out.println("\n1️⃣  FILE SYSTEM (Files and Folders)");
        System.out.println("-".repeat(70));
        
        // Create files
        File file1 = new File("resume.pdf", 150);
        File file2 = new File("photo.jpg", 2048);
        File file3 = new File("report.docx", 500);
        File file4 = new File("data.xlsx", 300);
        File file5 = new File("presentation.pptx", 1500);
        
        // Create folders
        Folder documents = new Folder("Documents");
        documents.add(file1);
        documents.add(file3);
        
        Folder pictures = new Folder("Pictures");
        pictures.add(file2);
        
        Folder work = new Folder("Work");
        work.add(file4);
        work.add(file5);
        
        // Create root folder
        Folder root = new Folder("Root");
        root.add(documents);
        root.add(pictures);
        root.add(work);
        
        // Display entire tree (uniform treatment!)
        root.display("");
        System.out.println("\n📊 Total size: " + root.getSize() + " KB");
        
        // 2. Organization Hierarchy
        System.out.println("\n\n2️⃣  ORGANIZATION HIERARCHY");
        System.out.println("-".repeat(70));
        
        // Create developers
        Developer dev1 = new Developer("Alice", "Senior Developer", 120000);
        Developer dev2 = new Developer("Bob", "Junior Developer", 80000);
        Developer dev3 = new Developer("Charlie", "Senior Developer", 125000);
        Developer dev4 = new Developer("Diana", "Junior Developer", 75000);
        
        // Create managers
        Manager techLead = new Manager("Eve", 150000);
        techLead.addTeamMember(dev1);
        techLead.addTeamMember(dev2);
        
        Manager productLead = new Manager("Frank", 145000);
        productLead.addTeamMember(dev3);
        productLead.addTeamMember(dev4);
        
        // Create CTO
        Manager cto = new Manager("Grace (CTO)", 200000);
        cto.addTeamMember(techLead);
        cto.addTeamMember(productLead);
        
        // Display organization (uniform treatment!)
        cto.showDetails("");
        System.out.println("\n💰 Total payroll: $" + cto.getSalary());
        
        // 3. UI Components
        System.out.println("\n\n3️⃣  UI COMPONENTS (Nested Panels)");
        System.out.println("-".repeat(70));
        
        // Create simple components
        Button loginBtn = new Button("Login");
        Button cancelBtn = new Button("Cancel");
        TextBox username = new TextBox("Enter username");
        TextBox password = new TextBox("Enter password");
        
        // Create login panel
        Panel loginPanel = new Panel("Login Form");
        loginPanel.add(username);
        loginPanel.add(password);
        loginPanel.add(loginBtn);
        loginPanel.add(cancelBtn);
        
        // Create navigation
        Button homeBtn = new Button("Home");
        Button profileBtn = new Button("Profile");
        Button settingsBtn = new Button("Settings");
        
        Panel navbar = new Panel("Navigation Bar");
        navbar.add(homeBtn);
        navbar.add(profileBtn);
        navbar.add(settingsBtn);
        
        // Create main window
        Panel mainWindow = new Panel("Main Window");
        mainWindow.add(navbar);
        mainWindow.add(loginPanel);
        
        // Render UI (uniform treatment!)
        mainWindow.render("");
        System.out.println("\n🔢 Total components: " + mainWindow.getComponentCount());
        
        // 4. Menu System
        System.out.println("\n\n4️⃣  RESTAURANT MENU (Items and Submenus)");
        System.out.println("-".repeat(70));
        
        // Create menu items
        MenuItem pancakes = new MenuItem("Pancakes", "Fluffy pancakes with syrup", 8.99, true);
        MenuItem eggs = new MenuItem("Scrambled Eggs", "Farm fresh eggs", 6.99, true);
        MenuItem bacon = new MenuItem("Bacon", "Crispy bacon strips", 4.99, false);
        
        MenuItem burger = new MenuItem("Burger", "Beef burger with fries", 12.99, false);
        MenuItem salad = new MenuItem("Caesar Salad", "Fresh romaine lettuce", 9.99, true);
        MenuItem pasta = new MenuItem("Pasta", "Italian pasta with marinara", 11.99, true);
        
        MenuItem cake = new MenuItem("Chocolate Cake", "Rich chocolate cake", 6.99, true);
        MenuItem iceCream = new MenuItem("Ice Cream", "Vanilla ice cream", 4.99, true);
        
        // Create submenus
        Menu breakfast = new Menu("Breakfast Menu");
        breakfast.add(pancakes);
        breakfast.add(eggs);
        breakfast.add(bacon);
        
        Menu lunch = new Menu("Lunch Menu");
        lunch.add(burger);
        lunch.add(salad);
        lunch.add(pasta);
        
        Menu dessert = new Menu("Dessert Menu");
        dessert.add(cake);
        dessert.add(iceCream);
        
        // Create main menu
        Menu mainMenu = new Menu("Restaurant Menu");
        mainMenu.add(breakfast);
        mainMenu.add(lunch);
        mainMenu.add(dessert);
        
        // Print menu (uniform treatment!)
        mainMenu.print("");
        System.out.println("\n💵 Total menu value: $" + String.format("%.2f", mainMenu.getPrice()));
        System.out.println("🌱 All vegetarian? " + (breakfast.isVegetarian() ? "Yes" : "No (contains meat)"));
        
        // Summary
        System.out.println("\n\n" + "=".repeat(70));
        System.out.println("KEY BENEFITS OF COMPOSITE PATTERN");
        System.out.println("=".repeat(70));
        System.out.println("✅ Uniform Treatment: Same interface for leaf and composite");
        System.out.println("✅ Tree Structures: Easy to build hierarchies");
        System.out.println("✅ Flexibility: Easy to add new component types");
        System.out.println("✅ Simplicity: Client code is simple and uniform");
        System.out.println("✅ Open/Closed: Add new components without changing code");
        System.out.println("✅ Recursive Operations: Operations work on entire tree");
        System.out.println("=".repeat(70));
    }
}
