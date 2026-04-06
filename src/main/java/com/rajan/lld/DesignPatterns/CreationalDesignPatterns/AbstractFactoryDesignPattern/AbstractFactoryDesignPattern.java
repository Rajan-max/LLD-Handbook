package com.rajan.lld.DesignPatterns.CreationalDesignPatterns.AbstractFactoryDesignPattern;

/**
 * ABSTRACT FACTORY DESIGN PATTERN - Complete Example
 * 
 * Definition: Provides an interface for creating families of related or dependent objects
 * without specifying their concrete classes.
 * 
 * In simple terms:
 * - Factory of factories
 * - Creates families of related objects that work together
 * - Ensures all products from same family are compatible
 * 
 * Key Difference from Factory Pattern:
 * - Factory: Creates ONE type of object
 * - Abstract Factory: Creates FAMILIES of related objects
 */

// ============================================================================
// PROBLEM - Factory Pattern Limitation
// ============================================================================

/**
 * PROBLEM: Factory Pattern creates single objects
 * What if we need FAMILIES of related objects that must work together?
 * 
 * Example: Computer components
 * - High-End: Intel CPU + Nvidia GPU + High-End RAM
 * - Mid-Range: AMD CPU + AMD GPU + Mid-Range RAM
 * 
 * Factory Pattern would require:
 * - Separate CPUFactory, GPUFactory, RAMFactory
 * - Manual coordination to ensure compatibility
 * - Risk of mixing incompatible components
 */


// ============================================================================
// SOLUTION - Abstract Factory Pattern
// ============================================================================

/**
 * Abstract Product A - CPU
 */
interface CPU {
    String getSpecification();
}

/**
 * Concrete Product A1 - Intel CPU (High-End)
 */
class IntelCPU implements CPU {
    @Override
    public String getSpecification() {
        return "Intel Core i9-13900K (High-End)";
    }
}

/**
 * Concrete Product A2 - AMD CPU (Mid-Range)
 */
class AMDCPU implements CPU {
    @Override
    public String getSpecification() {
        return "AMD Ryzen 7 5800X (Mid-Range)";
    }
}

/**
 * Abstract Product B - GPU
 */
interface GPU {
    String getSpecification();
}

/**
 * Concrete Product B1 - Nvidia GPU (High-End)
 */
class NvidiaGPU implements GPU {
    @Override
    public String getSpecification() {
        return "Nvidia GeForce RTX 4090 (High-End)";
    }
}

/**
 * Concrete Product B2 - AMD GPU (Mid-Range)
 */
class AMDGPU implements GPU {
    @Override
    public String getSpecification() {
        return "AMD Radeon RX 7800 XT (Mid-Range)";
    }
}

/**
 * Abstract Product C - RAM
 */
interface RAM {
    String getSpecification();
}

/**
 * Concrete Product C1 - High-End RAM
 */
class HighEndRAM implements RAM {
    @Override
    public String getSpecification() {
        return "64GB DDR5 6000MHz (High-End)";
    }
}

/**
 * Concrete Product C2 - Mid-Range RAM
 */
class MidRangeRAM implements RAM {
    @Override
    public String getSpecification() {
        return "32GB DDR4 3200MHz (Mid-Range)";
    }
}

/**
 * Abstract Factory Interface
 * Declares methods for creating each product in the family
 */
interface ComputerFactory {
    CPU createCPU();
    GPU createGPU();
    RAM createRAM();
}

/**
 * Concrete Factory 1 - High-End Computer Factory
 * Creates high-end family of components
 */
class HighEndComputerFactory implements ComputerFactory {
    @Override
    public CPU createCPU() {
        return new IntelCPU();
    }
    
    @Override
    public GPU createGPU() {
        return new NvidiaGPU();
    }
    
    @Override
    public RAM createRAM() {
        return new HighEndRAM();
    }
}

/**
 * Concrete Factory 2 - Mid-Range Computer Factory
 * Creates mid-range family of components
 */
class MidRangeComputerFactory implements ComputerFactory {
    @Override
    public CPU createCPU() {
        return new AMDCPU();
    }
    
    @Override
    public GPU createGPU() {
        return new AMDGPU();
    }
    
    @Override
    public RAM createRAM() {
        return new MidRangeRAM();
    }
}

/**
 * Client Code - Computer Shop
 * Works with abstract factory and products
 * Doesn't know about concrete classes
 */
class ComputerShop {
    private final ComputerFactory factory;
    
    public ComputerShop(ComputerFactory factory) {
        this.factory = factory;
    }
    
    public void assembleComputer() {
        // Create family of compatible components
        CPU cpu = factory.createCPU();
        GPU gpu = factory.createGPU();
        RAM ram = factory.createRAM();
        
        System.out.println("Assembling computer with:");
        System.out.println("  CPU: " + cpu.getSpecification());
        System.out.println("  GPU: " + gpu.getSpecification());
        System.out.println("  RAM: " + ram.getSpecification());
        System.out.println("All components are compatible!");
    }
}


// ============================================================================
// COMPARISON: Factory vs Abstract Factory
// ============================================================================

/**
 * FACTORY PATTERN - Creates single objects
 */
class CPUFactory {
    public static CPU create(String type) {
        return switch (type) {
            case "INTEL" -> new IntelCPU();
            case "AMD" -> new AMDCPU();
            default -> throw new IllegalArgumentException();
        };
    }
}

/**
 * PROBLEM with Factory Pattern:
 * - Need separate factories for CPU, GPU, RAM
 * - No guarantee components are compatible
 * - Client must coordinate multiple factories
 */
class ComputerShopBad {
    public void assembleComputer(String cpuType, String gpuType, String ramType) {
        // PROBLEM: Multiple factory calls, no compatibility guarantee
        CPU cpu = CPUFactory.create(cpuType);
        // Would need: GPU gpu = GPUFactory.create(gpuType);
        // Would need: RAM ram = RAMFactory.create(ramType);
        
        // RISK: Could mix Intel CPU with AMD GPU - incompatible!
        System.out.println("CPU: " + cpu.getSpecification());
    }
}


// ============================================================================
// REAL-WORLD EXAMPLE - UI Components for Different Platforms
// ============================================================================

/**
 * Products for UI family
 */
interface Button {
    void render();
}

interface Checkbox {
    void render();
}

interface TextField {
    void render();
}

/**
 * Windows UI Family
 */
class WindowsButton implements Button {
    public void render() {
        System.out.println("Rendering Windows style button");
    }
}

class WindowsCheckbox implements Checkbox {
    public void render() {
        System.out.println("Rendering Windows style checkbox");
    }
}

class WindowsTextField implements TextField {
    public void render() {
        System.out.println("Rendering Windows style text field");
    }
}

/**
 * Mac UI Family
 */
class MacButton implements Button {
    public void render() {
        System.out.println("Rendering Mac style button");
    }
}

class MacCheckbox implements Checkbox {
    public void render() {
        System.out.println("Rendering Mac style checkbox");
    }
}

class MacTextField implements TextField {
    public void render() {
        System.out.println("Rendering Mac style text field");
    }
}

/**
 * Abstract Factory for UI
 */
interface UIFactory {
    Button createButton();
    Checkbox createCheckbox();
    TextField createTextField();
}

class WindowsUIFactory implements UIFactory {
    public Button createButton() { return new WindowsButton(); }
    public Checkbox createCheckbox() { return new WindowsCheckbox(); }
    public TextField createTextField() { return new WindowsTextField(); }
}

class MacUIFactory implements UIFactory {
    public Button createButton() { return new MacButton(); }
    public Checkbox createCheckbox() { return new MacCheckbox(); }
    public TextField createTextField() { return new MacTextField(); }
}

/**
 * Client using UI family
 */
class Application {
    private Button button;
    private Checkbox checkbox;
    private TextField textField;
    
    public Application(UIFactory factory) {
        this.button = factory.createButton();
        this.checkbox = factory.createCheckbox();
        this.textField = factory.createTextField();
    }
    
    public void render() {
        System.out.println("Rendering Application UI:");
        button.render();
        checkbox.render();
        textField.render();
    }
}


// ============================================================================
// DEMO - Compare all approaches
// ============================================================================

public class AbstractFactoryDesignPattern {
    public static void main(String[] args) {
        
        System.out.println("========================================");
        System.out.println("PROBLEM - Factory Pattern Limitation");
        System.out.println("========================================\n");
        
        ComputerShopBad badShop = new ComputerShopBad();
        System.out.println("Risk: Can mix incompatible components!");
        badShop.assembleComputer("INTEL", "AMD", "MID_RANGE");
        System.out.println("Problem: No guarantee of compatibility\n");
        
        System.out.println("========================================");
        System.out.println("SOLUTION - Abstract Factory Pattern");
        System.out.println("========================================\n");
        
        // High-End Computer - All components guaranteed compatible
        System.out.println("Building High-End Gaming PC:");
        ComputerFactory highEndFactory = new HighEndComputerFactory();
        ComputerShop highEndShop = new ComputerShop(highEndFactory);
        highEndShop.assembleComputer();
        
        System.out.println("\nBuilding Mid-Range Office PC:");
        ComputerFactory midRangeFactory = new MidRangeComputerFactory();
        ComputerShop midRangeShop = new ComputerShop(midRangeFactory);
        midRangeShop.assembleComputer();
        
        System.out.println("\n========================================");
        System.out.println("REAL-WORLD - UI Components");
        System.out.println("========================================\n");
        
        System.out.println("Windows Application:");
        UIFactory windowsFactory = new WindowsUIFactory();
        Application windowsApp = new Application(windowsFactory);
        windowsApp.render();
        
        System.out.println("\nMac Application:");
        UIFactory macFactory = new MacUIFactory();
        Application macApp = new Application(macFactory);
        macApp.render();
        
        System.out.println("\n========================================");
        System.out.println("KEY ADVANTAGES");
        System.out.println("========================================");
        System.out.println("✓ Creates families of related objects");
        System.out.println("✓ Guarantees product compatibility");
        System.out.println("✓ Easy to switch entire family");
        System.out.println("✓ Isolates concrete classes from client");
        System.out.println("✓ Promotes consistency among products");
        
        System.out.println("\n========================================");
        System.out.println("FACTORY vs ABSTRACT FACTORY");
        System.out.println("========================================");
        System.out.println("Factory Pattern:");
        System.out.println("  - Creates single objects");
        System.out.println("  - No family guarantee");
        System.out.println("  - Example: CPUFactory.create(\"INTEL\")");
        System.out.println("\nAbstract Factory Pattern:");
        System.out.println("  - Creates families of objects");
        System.out.println("  - Guarantees compatibility");
        System.out.println("  - Example: HighEndFactory creates CPU+GPU+RAM");
    }
}
