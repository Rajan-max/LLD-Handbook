package com.rajan.lld.DesignPatterns.BehavioralDesignPatterns.TemplateMethodDesignPattern;

/**
 * TEMPLATE METHOD DESIGN PATTERN - Complete Example
 * 
 * Definition: Defines the skeleton of an algorithm in a method, deferring/delegating some
 * steps to subclasses. Template Method lets subclasses redefine certain steps 
 * of an algorithm without changing the algorithm's structure.
 * 
 * In simple terms:
 * - Define the overall algorithm structure in base class
 * - Let subclasses implement specific steps
 * - Algorithm structure remains the same, steps vary
 * 
 * When to use:
 * - Multiple classes share similar algorithm structure
 * - Want to avoid code duplication
 * - Need to control which parts can be customized
 * - Common behavior should be in one place
 */

// ============================================================================
// PROBLEM - Without Template Method Pattern
// ============================================================================

/**
 * PROBLEM: Code duplication and inconsistent algorithm structure
 * 
 * Why is this bad?
 * - Code duplication: Same steps repeated in multiple classes
 * - Inconsistent: Easy to miss steps or change order
 * - Hard to maintain: Changes must be made in multiple places
 * - No control: Can't enforce algorithm structure
 * - Violates DRY: Don't Repeat Yourself principle
 */
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
        System.out.println("Boiling water");
        System.out.println("Steeping the tea");
        System.out.println("Pouring into cup");
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
// Problem: Duplicated code, inconsistent steps, hard to maintain!


// ============================================================================
// SOLUTION - Template Method Pattern
// ============================================================================

/**
 * Step 1: Abstract Class with Template Method
 * Defines the algorithm skeleton
 */
abstract class Beverage {
    // Template Method - final to prevent overriding
    public final void prepareRecipe() {
        boilWater();
        brew();
        pourInCup();
        addCondiments();
        if (customerWantsCondiments()) {
            addExtraCondiments();
        }
    }
    
    // Abstract methods - must be implemented by subclasses
    protected abstract void brew();
    protected abstract void addCondiments();
    
    // Concrete methods - common to all beverages
    private void boilWater() {
        System.out.println("💧 Boiling water");
    }
    
    private void pourInCup() {
        System.out.println("☕ Pouring into cup");
    }
    
    // Hook method - optional override
    protected boolean customerWantsCondiments() {
        return true;
    }
    
    // Hook method with default implementation
    protected void addExtraCondiments() {
        // Default: do nothing
    }
}

/**
 * Step 2: Concrete Classes
 * Implement specific steps
 */
class Coffee extends Beverage {
    @Override
    protected void brew() {
        System.out.println("☕ Dripping coffee through filter");
    }
    
    @Override
    protected void addCondiments() {
        System.out.println("🥛 Adding sugar and milk");
    }
    
    @Override
    protected void addExtraCondiments() {
        System.out.println("🍫 Adding whipped cream");
    }
}

class Tea extends Beverage {
    @Override
    protected void brew() {
        System.out.println("🍵 Steeping the tea");
    }
    
    @Override
    protected void addCondiments() {
        System.out.println("🍋 Adding lemon");
    }
}

class HotChocolate extends Beverage {
    @Override
    protected void brew() {
        System.out.println("🍫 Mixing chocolate powder");
    }
    
    @Override
    protected void addCondiments() {
        System.out.println("🍬 Adding marshmallows");
    }
    
    @Override
    protected boolean customerWantsCondiments() {
        return true; // Always add marshmallows!
    }
}

class BlackCoffee extends Beverage {
    @Override
    protected void brew() {
        System.out.println("☕ Brewing strong black coffee");
    }
    
    @Override
    protected void addCondiments() {
        System.out.println("⚫ No condiments - black coffee");
    }
    
    @Override
    protected boolean customerWantsCondiments() {
        return false; // No extra condiments
    }
}


// ============================================================================
// REAL-WORLD EXAMPLE 1 - Data Processing
// ============================================================================

abstract class DataProcessor {
    // Template Method
    public final void process() {
        readData();
        processData();
        validateData();
        saveData();
        sendNotification();
    }
    
    // Abstract methods
    protected abstract void readData();
    protected abstract void processData();
    protected abstract void saveData();
    
    // Concrete methods
    private void validateData() {
        System.out.println("   ✓ Validating data");
    }
    
    // Hook method
    protected void sendNotification() {
        System.out.println("   ✓ Sending notification");
    }
}

class CSVDataProcessor extends DataProcessor {
    @Override
    protected void readData() {
        System.out.println("   📄 Reading CSV file");
    }
    
    @Override
    protected void processData() {
        System.out.println("   ⚙️  Processing CSV data");
    }
    
    @Override
    protected void saveData() {
        System.out.println("   💾 Saving to database");
    }
}

class JSONDataProcessor extends DataProcessor {
    @Override
    protected void readData() {
        System.out.println("   📄 Reading JSON file");
    }
    
    @Override
    protected void processData() {
        System.out.println("   ⚙️  Parsing JSON data");
    }
    
    @Override
    protected void saveData() {
        System.out.println("   💾 Saving to NoSQL database");
    }
    
    @Override
    protected void sendNotification() {
        System.out.println("   📧 Sending email notification");
    }
}

class XMLDataProcessor extends DataProcessor {
    @Override
    protected void readData() {
        System.out.println("   📄 Reading XML file");
    }
    
    @Override
    protected void processData() {
        System.out.println("   ⚙️  Parsing XML data");
    }
    
    @Override
    protected void saveData() {
        System.out.println("   💾 Saving to file system");
    }
    
    @Override
    protected void sendNotification() {
        // Override to do nothing
        System.out.println("   🔕 No notification needed");
    }
}


// ============================================================================
// REAL-WORLD EXAMPLE 2 - Game Character
// ============================================================================

abstract class GameCharacter {
    // Template Method
    public final void performAction() {
        prepareAction();
        executeAction();
        finishAction();
        if (hasSpecialAbility()) {
            useSpecialAbility();
        }
    }
    
    // Abstract methods
    protected abstract void executeAction();
    
    // Concrete methods
    private void prepareAction() {
        System.out.println("   🎮 Preparing action...");
    }
    
    private void finishAction() {
        System.out.println("   ✅ Action completed!");
    }
    
    // Hook methods
    protected boolean hasSpecialAbility() {
        return false;
    }
    
    protected void useSpecialAbility() {
        // Default: do nothing
    }
}

class Warrior extends GameCharacter {
    @Override
    protected void executeAction() {
        System.out.println("   ⚔️  Warrior attacks with sword!");
    }
    
    @Override
    protected boolean hasSpecialAbility() {
        return true;
    }
    
    @Override
    protected void useSpecialAbility() {
        System.out.println("   💥 Special: Berserker Rage!");
    }
}

class Mage extends GameCharacter {
    @Override
    protected void executeAction() {
        System.out.println("   🔮 Mage casts fireball!");
    }
    
    @Override
    protected boolean hasSpecialAbility() {
        return true;
    }
    
    @Override
    protected void useSpecialAbility() {
        System.out.println("   ✨ Special: Meteor Storm!");
    }
}

class Archer extends GameCharacter {
    @Override
    protected void executeAction() {
        System.out.println("   🏹 Archer shoots arrow!");
    }
}


// ============================================================================
// REAL-WORLD EXAMPLE 3 - House Building
// ============================================================================

abstract class HouseBuilder {
    // Template Method
    public final void buildHouse() {
        layFoundation();
        buildWalls();
        buildRoof();
        if (needsGarage()) {
            buildGarage();
        }
        if (needsSwimmingPool()) {
            buildSwimmingPool();
        }
        furnish();
    }
    
    // Abstract methods
    protected abstract void buildWalls();
    protected abstract void buildRoof();
    protected abstract void furnish();
    
    // Concrete methods
    private void layFoundation() {
        System.out.println("   🏗️  Laying foundation");
    }
    
    // Hook methods
    protected boolean needsGarage() {
        return false;
    }
    
    protected boolean needsSwimmingPool() {
        return false;
    }
    
    protected void buildGarage() {
        System.out.println("   🚗 Building garage");
    }
    
    protected void buildSwimmingPool() {
        System.out.println("   🏊 Building swimming pool");
    }
}

class WoodenHouse extends HouseBuilder {
    @Override
    protected void buildWalls() {
        System.out.println("   🪵 Building wooden walls");
    }
    
    @Override
    protected void buildRoof() {
        System.out.println("   🏠 Building wooden roof");
    }
    
    @Override
    protected void furnish() {
        System.out.println("   🪑 Adding rustic furniture");
    }
}

class ConcreteHouse extends HouseBuilder {
    @Override
    protected void buildWalls() {
        System.out.println("   🧱 Building concrete walls");
    }
    
    @Override
    protected void buildRoof() {
        System.out.println("   🏢 Building concrete roof");
    }
    
    @Override
    protected void furnish() {
        System.out.println("   🛋️  Adding modern furniture");
    }
    
    @Override
    protected boolean needsGarage() {
        return true;
    }
}

class LuxuryHouse extends HouseBuilder {
    @Override
    protected void buildWalls() {
        System.out.println("   💎 Building marble walls");
    }
    
    @Override
    protected void buildRoof() {
        System.out.println("   👑 Building premium roof");
    }
    
    @Override
    protected void furnish() {
        System.out.println("   🏆 Adding luxury furniture");
    }
    
    @Override
    protected boolean needsGarage() {
        return true;
    }
    
    @Override
    protected boolean needsSwimmingPool() {
        return true;
    }
}


// ============================================================================
// DEMO
// ============================================================================

public class TemplateMethodDesignPattern {
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║      TEMPLATE METHOD DESIGN PATTERN - DEMONSTRATION       ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        
        // PROBLEM: Without Template Method
        System.out.println("\n❌ PROBLEM: Without Template Method Pattern");
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("Making Coffee:");
        BadCoffee badCoffee = new BadCoffee();
        badCoffee.prepareRecipe();
        
        System.out.println("\nMaking Hot Chocolate:");
        BadHotChocolate badChocolate = new BadHotChocolate();
        badChocolate.prepareRecipe();
        System.out.println("\n⚠️  Issues: Code duplication, inconsistent steps, forgot to boil water!");
        
        // SOLUTION: With Template Method
        System.out.println("\n\n✅ SOLUTION: With Template Method Pattern");
        System.out.println("═══════════════════════════════════════════════════════════");
        
        System.out.println("\n1️⃣  Making Coffee:");
        Beverage coffee = new Coffee();
        coffee.prepareRecipe();
        
        System.out.println("\n2️⃣  Making Tea:");
        Beverage tea = new Tea();
        tea.prepareRecipe();
        
        System.out.println("\n3️⃣  Making Hot Chocolate:");
        Beverage hotChocolate = new HotChocolate();
        hotChocolate.prepareRecipe();
        
        System.out.println("\n4️⃣  Making Black Coffee:");
        Beverage blackCoffee = new BlackCoffee();
        blackCoffee.prepareRecipe();
        
        // EXAMPLE 1: Data Processing
        System.out.println("\n\n📊 EXAMPLE 1: Data Processing Pipeline");
        System.out.println("═══════════════════════════════════════════════════════════");
        
        System.out.println("\nProcessing CSV:");
        DataProcessor csvProcessor = new CSVDataProcessor();
        csvProcessor.process();
        
        System.out.println("\nProcessing JSON:");
        DataProcessor jsonProcessor = new JSONDataProcessor();
        jsonProcessor.process();
        
        System.out.println("\nProcessing XML:");
        DataProcessor xmlProcessor = new XMLDataProcessor();
        xmlProcessor.process();
        
        // EXAMPLE 2: Game Characters
        System.out.println("\n\n🎮 EXAMPLE 2: Game Character Actions");
        System.out.println("═══════════════════════════════════════════════════════════");
        
        System.out.println("\nWarrior Action:");
        GameCharacter warrior = new Warrior();
        warrior.performAction();
        
        System.out.println("\nMage Action:");
        GameCharacter mage = new Mage();
        mage.performAction();
        
        System.out.println("\nArcher Action:");
        GameCharacter archer = new Archer();
        archer.performAction();
        
        // EXAMPLE 3: House Building
        System.out.println("\n\n🏠 EXAMPLE 3: House Construction");
        System.out.println("═══════════════════════════════════════════════════════════");
        
        System.out.println("\nBuilding Wooden House:");
        HouseBuilder woodenHouse = new WoodenHouse();
        woodenHouse.buildHouse();
        
        System.out.println("\nBuilding Concrete House:");
        HouseBuilder concreteHouse = new ConcreteHouse();
        concreteHouse.buildHouse();
        
        System.out.println("\nBuilding Luxury House:");
        HouseBuilder luxuryHouse = new LuxuryHouse();
        luxuryHouse.buildHouse();
        
        // KEY BENEFITS
        System.out.println("\n\n🎯 KEY BENEFITS OF TEMPLATE METHOD PATTERN");
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("✓ Eliminates code duplication");
        System.out.println("✓ Enforces consistent algorithm structure");
        System.out.println("✓ Easy to add new variations");
        System.out.println("✓ Common behavior in one place");
        System.out.println("✓ Follows Don't Repeat Yourself (DRY) principle");
        System.out.println("✓ Subclasses control specific steps only");
        System.out.println("✓ Hook methods provide optional customization");
    }
}
