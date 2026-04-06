package com.rajan.lld.SolidPrinciples.InterfaceSegregationPrinciple;

/**
 * INTERFACE SEGREGATION PRINCIPLE (ISP) - Complete Example
 * 
 * Definition: Clients should not be forced to depend on interfaces they don't use.
 * 
 * In simple terms:
 * - Don't create fat interfaces with too many methods
 * - Split large interfaces into smaller, specific ones
 * - Classes should only implement methods they actually need
 */

// ============================================================================
// BAD EXAMPLE - Violates ISP (Fat Interface)
// ============================================================================

/**
 * PROBLEM: This is a "fat interface" that violates ISP
 * It forces all implementing classes to provide all methods,
 * even if they don't support all functionality
 * 
 * Why is this bad?
 * - BasicPrinter doesn't scan, fax, or copy, but must implement these methods
 * - Forces unnecessary method implementations
 * - Leads to empty methods or throwing exceptions
 * - Tight coupling - changes affect all implementers
 * - Violates ISP - clients forced to depend on unused methods
 */
interface AllInOneDeviceBad {
    void print();
    void scan();
    void fax();
    void copy();
}

/**
 * This works fine - has all capabilities
 */
class ModernPrinterBad implements AllInOneDeviceBad {
    @Override
    public void print() {
        System.out.println("Modern Printer: Printing document...");
    }
    
    @Override
    public void scan() {
        System.out.println("Modern Printer: Scanning document...");
    }
    
    @Override
    public void fax() {
        System.out.println("Modern Printer: Faxing document...");
    }
    
    @Override
    public void copy() {
        System.out.println("Modern Printer: Copying document...");
    }
}

/**
 * PROBLEM: BasicPrinter only prints, but forced to implement all methods!
 * This violates ISP - forced to depend on methods it doesn't use
 */
class BasicPrinterBad implements AllInOneDeviceBad {
    @Override
    public void print() {
        System.out.println("Basic Printer: Printing document...");
    }
    
    // PROBLEM: Forced to implement methods it doesn't support!
    @Override
    public void scan() {
        throw new UnsupportedOperationException("Basic printer cannot scan!");
    }
    
    @Override
    public void fax() {
        throw new UnsupportedOperationException("Basic printer cannot fax!");
    }
    
    @Override
    public void copy() {
        throw new UnsupportedOperationException("Basic printer cannot copy!");
    }
}

/**
 * PROBLEM: Old fax machine only faxes, but forced to implement all methods!
 */
class OldFaxMachineBad implements AllInOneDeviceBad {
    @Override
    public void print() {
        throw new UnsupportedOperationException("Fax machine cannot print!");
    }
    
    @Override
    public void scan() {
        throw new UnsupportedOperationException("Fax machine cannot scan!");
    }
    
    @Override
    public void fax() {
        System.out.println("Fax Machine: Sending fax...");
    }
    
    @Override
    public void copy() {
        throw new UnsupportedOperationException("Fax machine cannot copy!");
    }
}


// ============================================================================
// GOOD EXAMPLE - Follows ISP (Segregated Interfaces)
// ============================================================================

/**
 * SOLUTION: Split the fat interface into smaller, focused interfaces
 * Each interface represents a single capability
 * 
 * Key idea: Many specific interfaces are better than one general interface
 */

interface Printer {
    void print();
}

interface Scanner {
    void scan();
}

interface FaxMachine {
    void fax();
}

interface Copier {
    void copy();
}

/**
 * SOLUTION: BasicPrinter only implements what it needs
 * No forced methods, no exceptions!
 */
class BasicPrinter implements Printer {
    @Override
    public void print() {
        System.out.println("Basic Printer: Printing document...");
    }
}

/**
 * SOLUTION: Scanner device only implements scanning
 */
class SimpleScanner implements Scanner {
    @Override
    public void scan() {
        System.out.println("Scanner: Scanning document...");
    }
}

/**
 * SOLUTION: Fax machine only implements faxing
 */
class SimpleFaxMachine implements FaxMachine {
    @Override
    public void fax() {
        System.out.println("Fax Machine: Sending fax...");
    }
}

/**
 * SOLUTION: All-in-one device implements multiple interfaces
 * Only implements what it actually supports
 */
class AllInOneDevice implements Printer, Scanner, FaxMachine, Copier {
    @Override
    public void print() {
        System.out.println("All-in-One: Printing document...");
    }
    
    @Override
    public void scan() {
        System.out.println("All-in-One: Scanning document...");
    }
    
    @Override
    public void fax() {
        System.out.println("All-in-One: Faxing document...");
    }
    
    @Override
    public void copy() {
        System.out.println("All-in-One: Copying document...");
    }
}

/**
 * SOLUTION: Print-and-scan device implements only what it needs
 */
class PrinterScanner implements Printer, Scanner {
    @Override
    public void print() {
        System.out.println("Printer-Scanner: Printing document...");
    }
    
    @Override
    public void scan() {
        System.out.println("Printer-Scanner: Scanning document...");
    }
}

/**
 * SOLUTION: Client code works with specific interfaces
 * Only depends on what it actually uses
 */
class OfficeWorker {
    // Only needs printing capability
    public void printDocument(Printer printer) {
        printer.print();
    }
    
    // Only needs scanning capability
    public void scanDocument(Scanner scanner) {
        scanner.scan();
    }
    
    // Only needs faxing capability
    public void sendFax(FaxMachine faxMachine) {
        faxMachine.fax();
    }
}


// ============================================================================
// DEMO - Compare Bad vs Good approach
// ============================================================================

public class InterfaceSegregationPrinciple {
    public static void main(String[] args) {
        
        System.out.println("========================================");
        System.out.println("BAD APPROACH - Violates ISP");
        System.out.println("========================================");
        
        AllInOneDeviceBad modernBad = new ModernPrinterBad();
        AllInOneDeviceBad basicBad = new BasicPrinterBad();
        
        // Modern printer works fine
        System.out.println("Modern Printer:");
        modernBad.print();
        modernBad.scan();
        
        // PROBLEM: Basic printer forced to implement all methods
        System.out.println("\nBasic Printer:");
        basicBad.print(); // Works
        
        try {
            basicBad.scan(); // Throws exception!
        } catch (UnsupportedOperationException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
        
        try {
            basicBad.fax(); // Throws exception!
        } catch (UnsupportedOperationException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
        
        System.out.println("\n========================================");
        System.out.println("GOOD APPROACH - Follows ISP");
        System.out.println("========================================");
        
        // Each device implements only what it needs
        BasicPrinter basicPrinter = new BasicPrinter();
        SimpleScanner scanner = new SimpleScanner();
        SimpleFaxMachine faxMachine = new SimpleFaxMachine();
        AllInOneDevice allInOne = new AllInOneDevice();
        PrinterScanner printerScanner = new PrinterScanner();
        
        OfficeWorker worker = new OfficeWorker();
        
        System.out.println("Using Basic Printer:");
        worker.printDocument(basicPrinter); // Works - only implements print
        
        System.out.println("\nUsing Scanner:");
        worker.scanDocument(scanner); // Works - only implements scan
        
        System.out.println("\nUsing Fax Machine:");
        worker.sendFax(faxMachine); // Works - only implements fax
        
        System.out.println("\nUsing All-in-One Device:");
        worker.printDocument(allInOne);
        worker.scanDocument(allInOne);
        worker.sendFax(allInOne);
        
        System.out.println("\nUsing Printer-Scanner:");
        worker.printDocument(printerScanner);
        worker.scanDocument(printerScanner);
        // worker.sendFax(printerScanner); // Compile error - doesn't implement FaxMachine!
        
        System.out.println("\n========================================");
        System.out.println("BENEFITS OF ISP:");
        System.out.println("========================================");
        System.out.println("✓ Classes implement only what they need");
        System.out.println("✓ No forced method implementations");
        System.out.println("✓ No UnsupportedOperationException");
        System.out.println("✓ Smaller, focused interfaces");
        System.out.println("✓ Better flexibility and maintainability");
        System.out.println("✓ Loose coupling - clients depend only on what they use");
    }
}
