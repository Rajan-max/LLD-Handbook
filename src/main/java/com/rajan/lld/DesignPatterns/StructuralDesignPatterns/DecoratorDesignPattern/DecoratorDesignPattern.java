package com.rajan.lld.DesignPatterns.StructuralDesignPatterns.DecoratorDesignPattern;

/**
 * DECORATOR DESIGN PATTERN - Complete Example
 * 
 * Definition: Attaches additional responsibilities to an object dynamically. 
 * Decorators provide a flexible alternative to subclassing for extending functionality.
 * 
 * In simple terms:
 * - Wrap objects to add new behaviors
 * - Add features dynamically at runtime
 * - Avoid explosion of subclasses
 * 
 * When to use:
 * - Need to add responsibilities to objects dynamically
 * - Extension by subclassing is impractical
 * - Want to add features without modifying existing code
 * - Need different combinations of features
 */

// ============================================================================
// PROBLEM - Without Decorator Pattern
// ============================================================================

/**
 * PROBLEM: Subclass explosion for feature combinations
 * 
 * Why is this bad?
 * - Too many subclasses: n features = 2^n combinations
 * - Inflexible: Can't add features at runtime
 * - Code duplication: Similar code in multiple classes
 * - Hard to maintain: Changes affect many classes
 * - Violates Open/Closed: Must modify for new combinations
 */

// Without decorator: Need separate class for each combination
class PlainCoffee {
    public String getDescription() {
        return "Plain Coffee";
    }
    public double cost() {
        return 2.0;
    }
}

class CoffeeWithMilk {
    public String getDescription() {
        return "Coffee with Milk";
    }
    public double cost() {
        return 2.5;
    }
}

class CoffeeWithSugar {
    public String getDescription() {
        return "Coffee with Sugar";
    }
    public double cost() {
        return 2.3;
    }
}

class CoffeeWithMilkAndSugar {
    public String getDescription() {
        return "Coffee with Milk and Sugar";
    }
    public double cost() {
        return 2.8;
    }
}
// Problem: 3 add-ons = 8 classes! 10 add-ons = 1024 classes!


// ============================================================================
// SOLUTION - Decorator Pattern
// ============================================================================

/**
 * Step 1: Component Interface
 */
interface Coffee {
    String getDescription();
    double cost();
}

/**
 * Step 2: Concrete Component
 */
class SimpleCoffee implements Coffee {
    @Override
    public String getDescription() {
        return "Simple Coffee";
    }
    
    @Override
    public double cost() {
        return 2.0;
    }
}

/**
 * Step 3: Base Decorator
 */
abstract class CoffeeDecorator implements Coffee {
    protected Coffee decoratedCoffee;
    
    public CoffeeDecorator(Coffee coffee) {
        this.decoratedCoffee = coffee;
    }
    
    @Override
    public String getDescription() {
        return decoratedCoffee.getDescription();
    }
    
    @Override
    public double cost() {
        return decoratedCoffee.cost();
    }
}

/**
 * Step 4: Concrete Decorators
 */
class MilkDecorator extends CoffeeDecorator {
    public MilkDecorator(Coffee coffee) {
        super(coffee);
    }
    
    @Override
    public String getDescription() {
        return decoratedCoffee.getDescription() + ", Milk";
    }
    
    @Override
    public double cost() {
        return decoratedCoffee.cost() + 0.5;
    }
}

class SugarDecorator extends CoffeeDecorator {
    public SugarDecorator(Coffee coffee) {
        super(coffee);
    }
    
    @Override
    public String getDescription() {
        return decoratedCoffee.getDescription() + ", Sugar";
    }
    
    @Override
    public double cost() {
        return decoratedCoffee.cost() + 0.3;
    }
}

class WhipDecorator extends CoffeeDecorator {
    public WhipDecorator(Coffee coffee) {
        super(coffee);
    }
    
    @Override
    public String getDescription() {
        return decoratedCoffee.getDescription() + ", Whipped Cream";
    }
    
    @Override
    public double cost() {
        return decoratedCoffee.cost() + 0.7;
    }
}

class CaramelDecorator extends CoffeeDecorator {
    public CaramelDecorator(Coffee coffee) {
        super(coffee);
    }
    
    @Override
    public String getDescription() {
        return decoratedCoffee.getDescription() + ", Caramel";
    }
    
    @Override
    public double cost() {
        return decoratedCoffee.cost() + 0.6;
    }
}


// ============================================================================
// REAL-WORLD EXAMPLE 1 - Text Formatting
// ============================================================================

interface Text {
    String getContent();
}

class PlainText implements Text {
    private String content;
    
    public PlainText(String content) {
        this.content = content;
    }
    
    @Override
    public String getContent() {
        return content;
    }
}

abstract class TextDecorator implements Text {
    protected Text decoratedText;
    
    public TextDecorator(Text text) {
        this.decoratedText = text;
    }
    
    @Override
    public String getContent() {
        return decoratedText.getContent();
    }
}

class BoldDecorator extends TextDecorator {
    public BoldDecorator(Text text) {
        super(text);
    }
    
    @Override
    public String getContent() {
        return "<b>" + decoratedText.getContent() + "</b>";
    }
}

class ItalicDecorator extends TextDecorator {
    public ItalicDecorator(Text text) {
        super(text);
    }
    
    @Override
    public String getContent() {
        return "<i>" + decoratedText.getContent() + "</i>";
    }
}

class UnderlineDecorator extends TextDecorator {
    public UnderlineDecorator(Text text) {
        super(text);
    }
    
    @Override
    public String getContent() {
        return "<u>" + decoratedText.getContent() + "</u>";
    }
}


// ============================================================================
// REAL-WORLD EXAMPLE 2 - Data Source (Encryption & Compression)
// ============================================================================

interface DataSource {
    void writeData(String data);
    String readData();
}

class FileDataSource implements DataSource {
    private String filename;
    private String data = "";
    
    public FileDataSource(String filename) {
        this.filename = filename;
    }
    
    @Override
    public void writeData(String data) {
        this.data = data;
        System.out.println("   📄 Writing to file: " + data);
    }
    
    @Override
    public String readData() {
        System.out.println("   📄 Reading from file");
        return data;
    }
}

abstract class DataSourceDecorator implements DataSource {
    protected DataSource wrappee;
    
    public DataSourceDecorator(DataSource source) {
        this.wrappee = source;
    }
    
    @Override
    public void writeData(String data) {
        wrappee.writeData(data);
    }
    
    @Override
    public String readData() {
        return wrappee.readData();
    }
}

class EncryptionDecorator extends DataSourceDecorator {
    public EncryptionDecorator(DataSource source) {
        super(source);
    }
    
    @Override
    public void writeData(String data) {
        System.out.println("   🔒 Encrypting data");
        super.writeData(encrypt(data));
    }
    
    @Override
    public String readData() {
        String data = super.readData();
        System.out.println("   🔓 Decrypting data");
        return decrypt(data);
    }
    
    private String encrypt(String data) {
        return "encrypted(" + data + ")";
    }
    
    private String decrypt(String data) {
        return data.replace("encrypted(", "").replace(")", "");
    }
}

class CompressionDecorator extends DataSourceDecorator {
    public CompressionDecorator(DataSource source) {
        super(source);
    }
    
    @Override
    public void writeData(String data) {
        System.out.println("   📦 Compressing data");
        super.writeData(compress(data));
    }
    
    @Override
    public String readData() {
        String data = super.readData();
        System.out.println("   📂 Decompressing data");
        return decompress(data);
    }
    
    private String compress(String data) {
        return "compressed(" + data + ")";
    }
    
    private String decompress(String data) {
        return data.replace("compressed(", "").replace(")", "");
    }
}


// ============================================================================
// REAL-WORLD EXAMPLE 3 - Notification System
// ============================================================================

interface Notifier {
    void send(String message);
}

class EmailNotifier implements Notifier {
    @Override
    public void send(String message) {
        System.out.println("   📧 Sending email: " + message);
    }
}

abstract class NotifierDecorator implements Notifier {
    protected Notifier wrappee;
    
    public NotifierDecorator(Notifier notifier) {
        this.wrappee = notifier;
    }
    
    @Override
    public void send(String message) {
        wrappee.send(message);
    }
}

class SMSDecorator extends NotifierDecorator {
    public SMSDecorator(Notifier notifier) {
        super(notifier);
    }
    
    @Override
    public void send(String message) {
        super.send(message);
        System.out.println("   📱 Sending SMS: " + message);
    }
}

class SlackDecorator extends NotifierDecorator {
    public SlackDecorator(Notifier notifier) {
        super(notifier);
    }
    
    @Override
    public void send(String message) {
        super.send(message);
        System.out.println("   💬 Sending Slack message: " + message);
    }
}

class FacebookDecorator extends NotifierDecorator {
    public FacebookDecorator(Notifier notifier) {
        super(notifier);
    }
    
    @Override
    public void send(String message) {
        super.send(message);
        System.out.println("   📘 Posting to Facebook: " + message);
    }
}


// ============================================================================
// DEMO
// ============================================================================

public class DecoratorDesignPattern {
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║        DECORATOR DESIGN PATTERN - DEMONSTRATION           ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        
        // PROBLEM: Without Decorator
        System.out.println("\n❌ PROBLEM: Without Decorator Pattern");
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("Need separate classes for each combination:");
        System.out.println("- PlainCoffee");
        System.out.println("- CoffeeWithMilk");
        System.out.println("- CoffeeWithSugar");
        System.out.println("- CoffeeWithMilkAndSugar");
        System.out.println("- CoffeeWithMilkSugarAndWhip");
        System.out.println("... (exponential growth!)");
        System.out.println("\n⚠️  Issues: Subclass explosion, inflexible, code duplication");
        
        // SOLUTION: With Decorator
        System.out.println("\n\n✅ SOLUTION: With Decorator Pattern");
        System.out.println("═══════════════════════════════════════════════════════════");
        
        System.out.println("\n1️⃣  Simple Coffee:");
        Coffee coffee1 = new SimpleCoffee();
        System.out.println(coffee1.getDescription() + " - $" + coffee1.cost());
        
        System.out.println("\n2️⃣  Coffee with Milk:");
        Coffee coffee2 = new MilkDecorator(new SimpleCoffee());
        System.out.println(coffee2.getDescription() + " - $" + coffee2.cost());
        
        System.out.println("\n3️⃣  Coffee with Milk and Sugar:");
        Coffee coffee3 = new SugarDecorator(new MilkDecorator(new SimpleCoffee()));
        System.out.println(coffee3.getDescription() + " - $" + coffee3.cost());
        
        System.out.println("\n4️⃣  Deluxe Coffee (Milk, Sugar, Whip, Caramel):");
        Coffee coffee4 = new CaramelDecorator(
            new WhipDecorator(
                new SugarDecorator(
                    new MilkDecorator(new SimpleCoffee())
                )
            )
        );
        System.out.println(coffee4.getDescription() + " - $" + coffee4.cost());
        
        // EXAMPLE 1: Text Formatting
        System.out.println("\n\n📝 EXAMPLE 1: Text Formatting");
        System.out.println("═══════════════════════════════════════════════════════════");
        
        Text text = new PlainText("Hello World");
        System.out.println("\nPlain: " + text.getContent());
        
        Text boldText = new BoldDecorator(new PlainText("Hello World"));
        System.out.println("Bold: " + boldText.getContent());
        
        Text italicText = new ItalicDecorator(new PlainText("Hello World"));
        System.out.println("Italic: " + italicText.getContent());
        
        Text formattedText = new UnderlineDecorator(
            new ItalicDecorator(
                new BoldDecorator(new PlainText("Hello World"))
            )
        );
        System.out.println("Bold + Italic + Underline: " + formattedText.getContent());
        
        // EXAMPLE 2: Data Source
        System.out.println("\n\n💾 EXAMPLE 2: Data Source (Encryption & Compression)");
        System.out.println("═══════════════════════════════════════════════════════════");
        
        String data = "Sensitive Data";
        
        System.out.println("\nPlain file:");
        DataSource plainFile = new FileDataSource("data.txt");
        plainFile.writeData(data);
        System.out.println("Read: " + plainFile.readData());
        
        System.out.println("\nEncrypted file:");
        DataSource encryptedFile = new EncryptionDecorator(new FileDataSource("secure.txt"));
        encryptedFile.writeData(data);
        System.out.println("Read: " + encryptedFile.readData());
        
        System.out.println("\nCompressed + Encrypted file:");
        DataSource secureFile = new CompressionDecorator(
            new EncryptionDecorator(new FileDataSource("secure-compressed.txt"))
        );
        secureFile.writeData(data);
        System.out.println("Read: " + secureFile.readData());
        
        // EXAMPLE 3: Notification System
        System.out.println("\n\n🔔 EXAMPLE 3: Notification System");
        System.out.println("═══════════════════════════════════════════════════════════");
        
        String notification = "Server is down!";
        
        System.out.println("\nEmail only:");
        Notifier emailOnly = new EmailNotifier();
        emailOnly.send(notification);
        
        System.out.println("\nEmail + SMS:");
        Notifier emailAndSMS = new SMSDecorator(new EmailNotifier());
        emailAndSMS.send(notification);
        
        System.out.println("\nEmail + SMS + Slack:");
        Notifier multiChannel = new SlackDecorator(
            new SMSDecorator(new EmailNotifier())
        );
        multiChannel.send(notification);
        
        System.out.println("\nAll channels (Email + SMS + Slack + Facebook):");
        Notifier allChannels = new FacebookDecorator(
            new SlackDecorator(
                new SMSDecorator(new EmailNotifier())
            )
        );
        allChannels.send(notification);
        
        // KEY BENEFITS
        System.out.println("\n\n🎯 KEY BENEFITS OF DECORATOR PATTERN");
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("✓ Add responsibilities dynamically at runtime");
        System.out.println("✓ Avoid subclass explosion");
        System.out.println("✓ Flexible alternative to subclassing");
        System.out.println("✓ Follows Open/Closed Principle");
        System.out.println("✓ Single Responsibility Principle");
        System.out.println("✓ Combine decorators for different features");
        System.out.println("✓ Easy to add new decorators");
    }
}
