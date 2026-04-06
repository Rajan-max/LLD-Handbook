package com.rajan.lld.DesignPatterns.BehavioralDesignPatterns.StrategyDesignPattern;

/**
 * STRATEGY DESIGN PATTERN - Complete Example
 * 
 * Definition: Defines a family of algorithms, encapsulates each one, and makes 
 * them interchangeable. Strategy lets the algorithm vary independently from 
 * clients that use it.
 * 
 * In simple terms:
 * - Different ways to solve the same problem
 * - Pick the best algorithm for the situation
 * - Switch algorithms at runtime
 * 
 * When to use:
 * - Multiple algorithms for the same task
 * - Eliminate complex if-else or switch statements
 * - Need to change behavior at runtime
 * - Want to hide algorithm implementation details
 */

// ============================================================================
// PROBLEM - Without Strategy Pattern
// ============================================================================

/**
 * PROBLEM: Messy if-else chains that violate Open/Closed Principle
 * 
 * Why is this bad?
 * - Hard to maintain: Adding new payment method requires modifying this class
 * - Violates Open/Closed: Not open for extension, requires modification
 * - Hard to test: Must test all branches in one method
 * - No flexibility: Can't change payment method at runtime easily
 * - Code duplication: Similar logic scattered across if-else blocks
 */
class BadPaymentProcessor {
    public void processPayment(String paymentType, double amount) {
        // Ugly if-else chain that grows with each new payment method
        if (paymentType.equals("creditcard")) {
            System.out.println("Processing Credit Card payment...");
            System.out.println("Validating card number...");
            System.out.println("Checking credit limit...");
            System.out.println("Charging $" + amount + " to credit card");
            System.out.println("Transaction fee: $" + (amount * 0.03));
        } else if (paymentType.equals("paypal")) {
            System.out.println("Processing PayPal payment...");
            System.out.println("Redirecting to PayPal...");
            System.out.println("Charging $" + amount + " via PayPal");
            System.out.println("Transaction fee: $" + (amount * 0.025));
        } else if (paymentType.equals("crypto")) {
            System.out.println("Processing Cryptocurrency payment...");
            System.out.println("Generating wallet address...");
            System.out.println("Waiting for blockchain confirmation...");
            System.out.println("Charging $" + amount + " in crypto");
            System.out.println("Transaction fee: $" + (amount * 0.01));
        } else if (paymentType.equals("banktransfer")) {
            System.out.println("Processing Bank Transfer...");
            System.out.println("Validating account details...");
            System.out.println("Initiating transfer of $" + amount);
            System.out.println("Transaction fee: $5.00");
        }
        // Adding new payment methods requires modifying this method!
    }
}


// ============================================================================
// SOLUTION - Strategy Pattern
// ============================================================================

/**
 * Step 1: Strategy Interface
 * Defines the contract that all payment strategies must follow
 */
interface PaymentStrategy {
    void pay(double amount);
    double getTransactionFee(double amount);
}

/**
 * Step 2: Concrete Strategy - Credit Card Payment
 */
class CreditCardPayment implements PaymentStrategy {
    private String cardNumber;
    private String cvv;
    
    public CreditCardPayment(String cardNumber, String cvv) {
        this.cardNumber = cardNumber;
        this.cvv = cvv;
    }
    
    @Override
    public void pay(double amount) {
        System.out.println("Processing Credit Card payment...");
        System.out.println("Card: ****" + cardNumber.substring(cardNumber.length() - 4));
        System.out.println("Amount charged: $" + amount);
        System.out.println("Transaction fee: $" + getTransactionFee(amount));
    }
    
    @Override
    public double getTransactionFee(double amount) {
        return amount * 0.03; // 3% fee
    }
}

/**
 * Step 2: Concrete Strategy - PayPal Payment
 */
class PayPalPayment implements PaymentStrategy {
    private String email;
    
    public PayPalPayment(String email) {
        this.email = email;
    }
    
    @Override
    public void pay(double amount) {
        System.out.println("Processing PayPal payment...");
        System.out.println("PayPal account: " + email);
        System.out.println("Amount charged: $" + amount);
        System.out.println("Transaction fee: $" + getTransactionFee(amount));
    }
    
    @Override
    public double getTransactionFee(double amount) {
        return amount * 0.025; // 2.5% fee
    }
}

/**
 * Step 2: Concrete Strategy - Cryptocurrency Payment
 */
class CryptoPayment implements PaymentStrategy {
    private String walletAddress;
    
    public CryptoPayment(String walletAddress) {
        this.walletAddress = walletAddress;
    }
    
    @Override
    public void pay(double amount) {
        System.out.println("Processing Cryptocurrency payment...");
        System.out.println("Wallet: " + walletAddress.substring(0, 10) + "...");
        System.out.println("Amount charged: $" + amount);
        System.out.println("Transaction fee: $" + getTransactionFee(amount));
    }
    
    @Override
    public double getTransactionFee(double amount) {
        return amount * 0.01; // 1% fee
    }
}

/**
 * Step 2: Concrete Strategy - Bank Transfer Payment
 */
class BankTransferPayment implements PaymentStrategy {
    private String accountNumber;
    
    public BankTransferPayment(String accountNumber) {
        this.accountNumber = accountNumber;
    }
    
    @Override
    public void pay(double amount) {
        System.out.println("Processing Bank Transfer...");
        System.out.println("Account: ****" + accountNumber.substring(accountNumber.length() - 4));
        System.out.println("Amount transferred: $" + amount);
        System.out.println("Transaction fee: $" + getTransactionFee(amount));
    }
    
    @Override
    public double getTransactionFee(double amount) {
        return 5.0; // Flat $5 fee
    }
}

/**
 * Step 3: Context Class
 * Uses a strategy to perform payment
 */
class PaymentProcessor {
    private PaymentStrategy paymentStrategy;
    
    // Set strategy at runtime
    public void setPaymentStrategy(PaymentStrategy strategy) {
        this.paymentStrategy = strategy;
    }
    
    public void processPayment(double amount) {
        if (paymentStrategy == null) {
            throw new IllegalStateException("Payment strategy not set");
        }
        
        System.out.println("\n--- Payment Processing ---");
        paymentStrategy.pay(amount);
        double totalAmount = amount + paymentStrategy.getTransactionFee(amount);
        System.out.println("Total amount: $" + String.format("%.2f", totalAmount));
        System.out.println("Payment successful!");
    }
}


// ============================================================================
// REAL-WORLD EXAMPLE 1 - Shipping Strategies
// ============================================================================

interface ShippingStrategy {
    double calculateCost(double weight, double distance);
    String getDeliveryTime();
}

class StandardShipping implements ShippingStrategy {
    @Override
    public double calculateCost(double weight, double distance) {
        return weight * 0.5 + distance * 0.1;
    }
    
    @Override
    public String getDeliveryTime() {
        return "5-7 business days";
    }
}

class ExpressShipping implements ShippingStrategy {
    @Override
    public double calculateCost(double weight, double distance) {
        return weight * 1.0 + distance * 0.2 + 10;
    }
    
    @Override
    public String getDeliveryTime() {
        return "2-3 business days";
    }
}

class OvernightShipping implements ShippingStrategy {
    @Override
    public double calculateCost(double weight, double distance) {
        return weight * 2.0 + distance * 0.5 + 25;
    }
    
    @Override
    public String getDeliveryTime() {
        return "Next business day";
    }
}

class ShippingCalculator {
    private ShippingStrategy strategy;
    
    public void setStrategy(ShippingStrategy strategy) {
        this.strategy = strategy;
    }
    
    public void calculateShipping(double weight, double distance) {
        double cost = strategy.calculateCost(weight, distance);
        System.out.println("Delivery time: " + strategy.getDeliveryTime());
        System.out.println("Shipping cost: $" + String.format("%.2f", cost));
    }
}


// ============================================================================
// REAL-WORLD EXAMPLE 2 - Discount Strategies
// ============================================================================

interface DiscountStrategy {
    double applyDiscount(double amount);
    String getDescription();
}

class NoDiscount implements DiscountStrategy {
    @Override
    public double applyDiscount(double amount) {
        return amount;
    }
    
    @Override
    public String getDescription() {
        return "No discount";
    }
}

class PercentageDiscount implements DiscountStrategy {
    private double percentage;
    
    public PercentageDiscount(double percentage) {
        this.percentage = percentage;
    }
    
    @Override
    public double applyDiscount(double amount) {
        return amount * (1 - percentage / 100);
    }
    
    @Override
    public String getDescription() {
        return percentage + "% off";
    }
}

class FixedAmountDiscount implements DiscountStrategy {
    private double discountAmount;
    
    public FixedAmountDiscount(double discountAmount) {
        this.discountAmount = discountAmount;
    }
    
    @Override
    public double applyDiscount(double amount) {
        return Math.max(0, amount - discountAmount);
    }
    
    @Override
    public String getDescription() {
        return "$" + discountAmount + " off";
    }
}

class SeasonalDiscount implements DiscountStrategy {
    @Override
    public double applyDiscount(double amount) {
        return amount * 0.7; // 30% off
    }
    
    @Override
    public String getDescription() {
        return "Seasonal Sale - 30% off";
    }
}

class ShoppingCart {
    private DiscountStrategy discountStrategy;
    
    public ShoppingCart() {
        this.discountStrategy = new NoDiscount();
    }
    
    public void setDiscountStrategy(DiscountStrategy strategy) {
        this.discountStrategy = strategy;
    }
    
    public void checkout(double amount) {
        System.out.println("\nOriginal amount: $" + amount);
        System.out.println("Discount applied: " + discountStrategy.getDescription());
        double finalAmount = discountStrategy.applyDiscount(amount);
        System.out.println("Final amount: $" + String.format("%.2f", finalAmount));
        System.out.println("You saved: $" + String.format("%.2f", amount - finalAmount));
    }
}


// ============================================================================
// REAL-WORLD EXAMPLE 3 - Sorting Strategies
// ============================================================================

interface SortStrategy {
    void sort(int[] array);
    String getName();
}

class BubbleSort implements SortStrategy {
    @Override
    public void sort(int[] array) {
        int n = array.length;
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (array[j] > array[j + 1]) {
                    int temp = array[j];
                    array[j] = array[j + 1];
                    array[j + 1] = temp;
                }
            }
        }
    }
    
    @Override
    public String getName() {
        return "Bubble Sort";
    }
}

class QuickSort implements SortStrategy {
    @Override
    public void sort(int[] array) {
        quickSort(array, 0, array.length - 1);
    }
    
    private void quickSort(int[] arr, int low, int high) {
        if (low < high) {
            int pi = partition(arr, low, high);
            quickSort(arr, low, pi - 1);
            quickSort(arr, pi + 1, high);
        }
    }
    
    private int partition(int[] arr, int low, int high) {
        int pivot = arr[high];
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (arr[j] < pivot) {
                i++;
                int temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
            }
        }
        int temp = arr[i + 1];
        arr[i + 1] = arr[high];
        arr[high] = temp;
        return i + 1;
    }
    
    @Override
    public String getName() {
        return "Quick Sort";
    }
}

class Sorter {
    private SortStrategy strategy;
    
    public void setStrategy(SortStrategy strategy) {
        this.strategy = strategy;
    }
    
    public void performSort(int[] array) {
        System.out.println("\nUsing " + strategy.getName());
        long startTime = System.nanoTime();
        strategy.sort(array);
        long endTime = System.nanoTime();
        System.out.println("Time taken: " + (endTime - startTime) / 1000 + " microseconds");
    }
}


// ============================================================================
// DEMO
// ============================================================================

public class StrategyDesignPattern {
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║         STRATEGY DESIGN PATTERN - DEMONSTRATION           ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        
        // PROBLEM: Without Strategy
        System.out.println("\n❌ PROBLEM: Without Strategy Pattern");
        System.out.println("═══════════════════════════════════════════════════════════");
        BadPaymentProcessor badProcessor = new BadPaymentProcessor();
        badProcessor.processPayment("creditcard", 100.0);
        System.out.println("\n⚠️  Issues: Hard to maintain, violates Open/Closed, can't switch at runtime");
        
        // SOLUTION: With Strategy
        System.out.println("\n\n✅ SOLUTION: With Strategy Pattern");
        System.out.println("═══════════════════════════════════════════════════════════");
        
        PaymentProcessor processor = new PaymentProcessor();
        
        // Customer chooses Credit Card
        System.out.println("\n1️⃣  Customer selects Credit Card:");
        processor.setPaymentStrategy(new CreditCardPayment("1234567890123456", "123"));
        processor.processPayment(100.0);
        
        // Customer switches to PayPal
        System.out.println("\n2️⃣  Customer switches to PayPal:");
        processor.setPaymentStrategy(new PayPalPayment("user@example.com"));
        processor.processPayment(100.0);
        
        // Customer switches to Crypto
        System.out.println("\n3️⃣  Customer switches to Cryptocurrency:");
        processor.setPaymentStrategy(new CryptoPayment("1A2B3C4D5E6F7G8H9I0J"));
        processor.processPayment(100.0);
        
        // Customer switches to Bank Transfer
        System.out.println("\n4️⃣  Customer switches to Bank Transfer:");
        processor.setPaymentStrategy(new BankTransferPayment("9876543210"));
        processor.processPayment(100.0);
        
        // EXAMPLE 1: Shipping Strategies
        System.out.println("\n\n📦 EXAMPLE 1: Shipping Strategies");
        System.out.println("═══════════════════════════════════════════════════════════");
        
        ShippingCalculator shipping = new ShippingCalculator();
        double weight = 5.0;
        double distance = 100.0;
        
        System.out.println("\nPackage: " + weight + "kg, Distance: " + distance + "km\n");
        
        System.out.println("Standard Shipping:");
        shipping.setStrategy(new StandardShipping());
        shipping.calculateShipping(weight, distance);
        
        System.out.println("\nExpress Shipping:");
        shipping.setStrategy(new ExpressShipping());
        shipping.calculateShipping(weight, distance);
        
        System.out.println("\nOvernight Shipping:");
        shipping.setStrategy(new OvernightShipping());
        shipping.calculateShipping(weight, distance);
        
        // EXAMPLE 2: Discount Strategies
        System.out.println("\n\n💰 EXAMPLE 2: Discount Strategies");
        System.out.println("═══════════════════════════════════════════════════════════");
        
        ShoppingCart cart = new ShoppingCart();
        double cartAmount = 200.0;
        
        System.out.println("\n1. Regular customer (no discount):");
        cart.checkout(cartAmount);
        
        System.out.println("\n2. Loyalty member (20% off):");
        cart.setDiscountStrategy(new PercentageDiscount(20));
        cart.checkout(cartAmount);
        
        System.out.println("\n3. Coupon code ($30 off):");
        cart.setDiscountStrategy(new FixedAmountDiscount(30));
        cart.checkout(cartAmount);
        
        System.out.println("\n4. Seasonal sale (30% off):");
        cart.setDiscountStrategy(new SeasonalDiscount());
        cart.checkout(cartAmount);
        
        // EXAMPLE 3: Sorting Strategies
        System.out.println("\n\n🔢 EXAMPLE 3: Sorting Strategies");
        System.out.println("═══════════════════════════════════════════════════════════");
        
        int[] data1 = {64, 34, 25, 12, 22, 11, 90};
        int[] data2 = {64, 34, 25, 12, 22, 11, 90};
        
        Sorter sorter = new Sorter();
        
        sorter.setStrategy(new BubbleSort());
        sorter.performSort(data1);
        System.out.print("Result: ");
        printArray(data1);
        
        sorter.setStrategy(new QuickSort());
        sorter.performSort(data2);
        System.out.print("Result: ");
        printArray(data2);
        
        // KEY BENEFITS
        System.out.println("\n\n🎯 KEY BENEFITS OF STRATEGY PATTERN");
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("✓ Eliminates if-else/switch statements");
        System.out.println("✓ Easy to add new strategies without modifying existing code");
        System.out.println("✓ Algorithms can be switched at runtime");
        System.out.println("✓ Follows Open/Closed Principle");
        System.out.println("✓ Each strategy is independently testable");
        System.out.println("✓ Promotes code reusability and maintainability");
    }
    
    private static void printArray(int[] arr) {
        System.out.print("[");
        for (int i = 0; i < arr.length; i++) {
            System.out.print(arr[i]);
            if (i < arr.length - 1) System.out.print(", ");
        }
        System.out.println("]");
    }
}
