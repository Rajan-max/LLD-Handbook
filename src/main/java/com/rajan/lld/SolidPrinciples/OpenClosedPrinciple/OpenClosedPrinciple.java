package com.rajan.lld.SolidPrinciples.OpenClosedPrinciple;

/**
 * OPEN/CLOSED PRINCIPLE (OCP) - Complete Example
 * 
 * Definition: Software entities should be OPEN for extension but CLOSED for modification.
 * 
 * In simple terms:
 * - OPEN for extension: You can add new functionality
 * - CLOSED for modification: Without changing existing code
 */

// ============================================================================
// BAD EXAMPLE - Violates OCP
// ============================================================================

/**
 * PROBLEM: This class violates OCP
 * Every time we add a new payment method, we must MODIFY this class
 * by adding new if-else conditions.
 * 
 * Why is this bad?
 * - Existing code needs to be changed (risk of breaking working code)
 * - Class grows with every new payment type
 * - Violates "closed for modification" principle
 * - Hard to maintain and test
 * - High risk of introducing bugs in existing functionality
 */
class PaymentProcessorBad {
    
    public void processPayment(String paymentType) {
        // PROBLEM: Using if-else to handle different payment types
        // Every new payment method requires modifying this code!
        
        if (paymentType.equals("CREDIT_CARD")) {
            System.out.println("Processing credit card payment...");
            System.out.println("Validating card number");
            System.out.println("Charging credit card");
            
        } else if (paymentType.equals("PAYPAL")) {
            System.out.println("Processing PayPal payment...");
            System.out.println("Redirecting to PayPal");
            System.out.println("Confirming PayPal transaction");
            
        } else if (paymentType.equals("BITCOIN")) {
            // PROBLEM: Had to MODIFY existing code to add Bitcoin!
            System.out.println("Processing Bitcoin payment...");
            System.out.println("Validating wallet address");
            System.out.println("Transferring Bitcoin");
            
        } else if (paymentType.equals("GOOGLE_PAY")) {
            // PROBLEM: Had to MODIFY existing code again to add Google Pay!
            System.out.println("Processing Google Pay payment...");
            System.out.println("Authenticating with Google");
            System.out.println("Completing Google Pay transaction");
            
        } else {
            System.out.println("Unknown payment type");
        }
        
        // What if we need to add Apple Pay, Cash, etc.?
        // We'll keep modifying this class forever!
    }
}


// ============================================================================
// GOOD EXAMPLE - Follows OCP
// ============================================================================

/**
 * SOLUTION: Payment interface (abstraction)
 * This allows us to extend functionality without modifying existing code
 * 
 * Key idea: Define a contract that all payment methods must follow
 */
interface Payment {
    void pay();
}

/**
 * SOLUTION: Each payment method is a separate class
 * We can add new payment methods by creating new classes (EXTENSION)
 * without touching existing code (CLOSED for modification)
 */

class CreditCardPayment implements Payment {
    @Override
    public void pay() {
        System.out.println("Processing credit card payment...");
        System.out.println("Validating card number");
        System.out.println("Charging credit card");
    }
}

class PayPalPayment implements Payment {
    @Override
    public void pay() {
        System.out.println("Processing PayPal payment...");
        System.out.println("Redirecting to PayPal");
        System.out.println("Confirming PayPal transaction");
    }
}

class BitcoinPayment implements Payment {
    @Override
    public void pay() {
        System.out.println("Processing Bitcoin payment...");
        System.out.println("Validating wallet address");
        System.out.println("Transferring Bitcoin");
    }
}

/**
 * NEW PAYMENT METHOD: Google Pay
 * Notice: We just created a NEW class (EXTENSION)
 * We did NOT modify any existing code (CLOSED for modification)
 * This is OCP in action!
 */
class GooglePayPayment implements Payment {
    @Override
    public void pay() {
        System.out.println("Processing Google Pay payment...");
        System.out.println("Authenticating with Google");
        System.out.println("Completing Google Pay transaction");
    }
}

/**
 * SOLUTION: PaymentProcessor that works with any Payment implementation
 * This class is CLOSED for modification - we never need to change it
 * But it's OPEN for extension - it works with any new Payment type
 */
class PaymentProcessor {
    
    /**
     * This method doesn't care about specific payment types
     * It works with the Payment interface (abstraction)
     * 
     * Benefits:
     * - No if-else chains
     * - No modifications needed when adding new payment types
     * - Clean, maintainable code
     */
    public void processPayment(Payment payment) {
        payment.pay();
    }
}


// ============================================================================
// DEMO - Compare Bad vs Good approach
// ============================================================================

public class OpenClosedPrinciple {
    public static void main(String[] args) {
        
        System.out.println("========================================");
        System.out.println("BAD APPROACH - Violates OCP");
        System.out.println("========================================");
        
        PaymentProcessorBad badProcessor = new PaymentProcessorBad();
        
        // Every payment type is hardcoded as strings
        // Adding new types requires modifying PaymentProcessorBad class
        badProcessor.processPayment("CREDIT_CARD");
        System.out.println();
        badProcessor.processPayment("PAYPAL");
        System.out.println();
        badProcessor.processPayment("BITCOIN");
        System.out.println();
        
        System.out.println("\n========================================");
        System.out.println("GOOD APPROACH - Follows OCP");
        System.out.println("========================================");
        
        PaymentProcessor processor = new PaymentProcessor();
        
        // Each payment is an object implementing Payment interface
        // Adding new types only requires creating new classes
        Payment creditCard = new CreditCardPayment();
        Payment paypal = new PayPalPayment();
        Payment bitcoin = new BitcoinPayment();
        Payment googlePay = new GooglePayPayment(); // NEW! No existing code modified
        
        processor.processPayment(creditCard);
        System.out.println();
        processor.processPayment(paypal);
        System.out.println();
        processor.processPayment(bitcoin);
        System.out.println();
        processor.processPayment(googlePay);
        System.out.println();
    }
}
