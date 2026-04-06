package com.rajan.lld.SolidPrinciples.LiskovSubstitutionPrinciple;

/**
 * LISKOV SUBSTITUTION PRINCIPLE (LSP) - Complete Example
 * 
 * Definition: Objects of a superclass should be replaceable with objects of its subclasses
 * without breaking the application.
 * 
 * In simple terms:
 * - If class B is a subclass of class A, we should be able to replace A with B
 *   without disrupting the behavior of the program
 * - Subclasses should extend, not replace, the behavior of the parent class
 */

// ============================================================================
// BAD EXAMPLE - Violates LSP
// ============================================================================

/**
 * PROBLEM: This hierarchy violates LSP
 * Not all birds can fly, but we're forcing all birds to have a fly() method
 * 
 * Why is this bad?
 * - Penguin and Ostrich cannot fly, but they inherit fly() method
 * - We're forced to throw exceptions or do nothing in fly()
 * - Breaks the substitutability principle
 * - Client code cannot safely use Bird reference for all birds
 */

class BirdBad {
    public void eat() {
        System.out.println("Bird is eating");
    }
    
    // PROBLEM: Assumes all birds can fly
    public void fly() {
        System.out.println("Bird is flying");
    }
}

class SparrowBad extends BirdBad {
    @Override
    public void fly() {
        System.out.println("Sparrow is flying");
    }
}

class EagleBad extends BirdBad {
    @Override
    public void fly() {
        System.out.println("Eagle is soaring high");
    }
}

/**
 * PROBLEM: Penguin cannot fly!
 * But it inherits fly() method from BirdBad
 * This violates LSP - we cannot substitute BirdBad with PenguinBad safely
 */
class PenguinBad extends BirdBad {
    @Override
    public void fly() {
        // PROBLEM: What do we do here?
        // Option 1: Throw exception (breaks LSP - unexpected behavior)
        throw new UnsupportedOperationException("Penguins cannot fly!");
        
        // Option 2: Do nothing (misleading - method exists but does nothing)
        // System.out.println("Penguins cannot fly");
    }
}

class OstrichBad extends BirdBad {
    @Override
    public void fly() {
        // PROBLEM: Same issue - Ostrich cannot fly
        throw new UnsupportedOperationException("Ostriches cannot fly!");
    }
}

/**
 * PROBLEM: This method expects all birds to fly
 * But it breaks when we pass Penguin or Ostrich
 */
class BirdWatcherBad {
    public void makeBirdFly(BirdBad bird) {
        bird.fly(); // This will throw exception for Penguin/Ostrich!
    }
}


// ============================================================================
// GOOD EXAMPLE - Follows LSP
// ============================================================================

/**
 * SOLUTION: Separate flying behavior from Bird
 * Not all birds fly, so flying should not be in the base Bird class
 * 
 * Key idea: Design hierarchy based on actual capabilities
 */

class Bird {
    public void eat() {
        System.out.println("Bird is eating");
    }
    
    // Common behavior for all birds - no fly() here!
}

/**
 * SOLUTION: Create a separate interface for flying birds
 * Only birds that can fly will implement this interface
 */
interface Flyable {
    void fly();
}

/**
 * Flying birds implement both Bird behavior and Flyable interface
 */
class Sparrow extends Bird implements Flyable {
    @Override
    public void fly() {
        System.out.println("Sparrow is flying");
    }
}

class Eagle extends Bird implements Flyable {
    @Override
    public void fly() {
        System.out.println("Eagle is soaring high");
    }
}

/**
 * SOLUTION: Non-flying birds just extend Bird
 * They don't implement Flyable - no forced fly() method!
 * This follows LSP - Penguin can safely substitute Bird
 */
class Penguin extends Bird {
    public void swim() {
        System.out.println("Penguin is swimming");
    }
}

class Ostrich extends Bird {
    public void run() {
        System.out.println("Ostrich is running fast");
    }
}

/**
 * SOLUTION: Methods work with appropriate abstractions
 * This method only accepts birds that can fly
 */
class BirdWatcher {
    // Works with any flying bird - type-safe!
    public void makeBirdFly(Flyable flyingBird) {
        flyingBird.fly(); // Safe - all Flyable birds can fly
    }
    
    // Works with any bird - type-safe!
    public void feedBird(Bird bird) {
        bird.eat(); // Safe - all birds can eat
    }
}


// ============================================================================
// DEMO - Compare Bad vs Good approach
// ============================================================================

public class LiskovSubstitutionPrinciple {
    public static void main(String[] args) {
        
        System.out.println("========================================");
        System.out.println("BAD APPROACH - Violates LSP");
        System.out.println("========================================");
        
        BirdWatcherBad badWatcher = new BirdWatcherBad();
        
        BirdBad sparrowBad = new SparrowBad();
        BirdBad eagleBad = new EagleBad();
        BirdBad penguinBad = new PenguinBad();
        
        // These work fine
        badWatcher.makeBirdFly(sparrowBad);
        badWatcher.makeBirdFly(eagleBad);
        
        // PROBLEM: This breaks! Cannot substitute BirdBad with PenguinBad
        try {
            badWatcher.makeBirdFly(penguinBad); // Throws exception!
        } catch (UnsupportedOperationException e) {
            System.out.println("ERROR: " + e.getMessage());
            System.out.println("LSP violated - cannot substitute parent with child!");
        }
        
        System.out.println("\n========================================");
        System.out.println("GOOD APPROACH - Follows LSP");
        System.out.println("========================================");
        
        BirdWatcher watcher = new BirdWatcher();
        
        // Create different birds
        Sparrow sparrow = new Sparrow();
        Eagle eagle = new Eagle();
        Penguin penguin = new Penguin();
        Ostrich ostrich = new Ostrich();
        
        // All birds can eat - LSP satisfied
        System.out.println("Feeding birds:");
        watcher.feedBird(sparrow);
        watcher.feedBird(eagle);
        watcher.feedBird(penguin);
        watcher.feedBird(ostrich);
        
        System.out.println("\nMaking flying birds fly:");
        // Only flying birds can fly - type-safe!
        watcher.makeBirdFly(sparrow);
        watcher.makeBirdFly(eagle);
        // watcher.makeBirdFly(penguin); // Compile error - penguin is not Flyable!
        
        System.out.println("\nNon-flying birds have their own abilities:");
        penguin.swim();
        ostrich.run();
    }
}
