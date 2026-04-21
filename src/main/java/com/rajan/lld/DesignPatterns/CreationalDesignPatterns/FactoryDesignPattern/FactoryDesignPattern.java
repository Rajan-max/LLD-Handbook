package com.rajan.lld.DesignPatterns.CreationalDesignPatterns.FactoryDesignPattern;

/**
 * FACTORY DESIGN PATTERN - Complete Example
 * 
 * Definition: Creates objects without specifying their exact class.
 * 
 * In simple terms:
 * - Client asks for an object without knowing which class to instantiate
 * - Factory decides which class to create based on input
 * - "I need a vehicle, but I don't care if it's Car or Bike - just give me one!"
 * 
 * When to use:
 * - Object creation logic is complex
 * - Need to decouple object creation from usage
 * - Want to add new types without changing existing code
 * - Multiple related classes with common interface
 */

// ============================================================================
// PROBLEM - Without Factory
// ============================================================================

/**
 * PROBLEM: Client code is tightly coupled to concrete classes
 * Adding new notification types requires modifying client code
 */
class NotificationServiceBad {
    
    public void sendNotification(String type, String message) {
        // PROBLEM: Client knows about all concrete classes
        // PROBLEM: if-else chain grows with each new type
        
        if (type.equals("EMAIL")) {
            System.out.println("Creating Email Notification (complex setup)");
            System.out.println("Setting SMTP server...");
            System.out.println("Authenticating...");
            System.out.println("Sending Email: " + message);
            
        } else if (type.equals("SMS")) {
            System.out.println("Creating SMS Notification (complex setup)");
            System.out.println("Setting provider...");
            System.out.println("Configuring...");
            System.out.println("Sending SMS: " + message);
            
        } else if (type.equals("PUSH")) {
            System.out.println("Creating Push Notification (complex setup)");
            System.out.println("Setting API key...");
            System.out.println("Initializing...");
            System.out.println("Sending Push: " + message);
        }
        
        // PROBLEM: Adding Slack notification requires modifying this code!
        // Violates Open/Closed Principle
    }
}


// ============================================================================
// SOLUTION - Factory Pattern
// ============================================================================

/**
 * Step 1: Common interface for all products
 * All notification types implement this interface
 */
interface Notification {
    void send(String message);
}

/**
 * Step 2: Concrete implementations (Products)
 */
class EmailNotification implements Notification {
    
    public EmailNotification() {
        System.out.println("Creating Email Notification");
    }
    
    @Override
    public void send(String message) {
        System.out.println("Sending Email: " + message);
    }
}

class SMSNotification implements Notification {
    
    public SMSNotification() {
        System.out.println("Creating SMS Notification");
    }
    
    @Override
    public void send(String message) {
        System.out.println("Sending SMS: " + message);
    }
}

class PushNotification implements Notification {
    
    public PushNotification() {
        System.out.println("Creating Push Notification");
    }
    
    @Override
    public void send(String message) {
        System.out.println("Sending Push Notification: " + message);
    }
}

/**
 * NEW: Adding Slack notification - no changes to existing code!
 */
class SlackNotification implements Notification {
    
    public SlackNotification() {
        System.out.println("Creating Slack Notification");
    }
    
    @Override
    public void send(String message) {
        System.out.println("Sending Slack Message: " + message);
    }
}


// ============================================================================
// SOLUTION 1 - Simple Factory
// ============================================================================

/**
 * Step 3: Simple Factory
 * Centralizes object creation logic
 * 
 * Pros:
 * - Simple and easy to understand
 * - Centralizes creation logic
 * 
 * Cons:
 * - Violates Open/Closed (need to modify factory for new types)
 */
class NotificationFactory {
    
    public static Notification createNotification(String type) {
        return switch (type.toUpperCase()) {
            case "EMAIL" -> new EmailNotification();
            case "SMS" -> new SMSNotification();
            case "PUSH" -> new PushNotification();
            case "SLACK" -> new SlackNotification();
            default -> throw new IllegalArgumentException("Unknown notification type: " + type);
        };
    }
}

/**
 * Clean client code using Simple Factory
 */
class NotificationService {
    
    public void sendNotification(String type, String message) {
        // Client doesn't know about concrete classes!
        Notification notification = NotificationFactory.createNotification(type);
        notification.send(message);
        // Clean and simple!
    }
}


// ============================================================================
// SOLUTION 2 - Factory Method Pattern
// ============================================================================

/**
 * Factory Method Pattern - More flexible approach
 * Subclasses decide which class to instantiate
 * 
 * Pros:
 * - Follows Open/Closed Principle
 * - More flexible and extensible
 * - Each factory can have custom logic
 */

/**
 * Abstract Creator - defines factory method
 */
abstract class NotificationCreator {
    
    // Template method
    public final Notification getNotification() {
        Notification notification = createNotification();
        configureNotification(notification);
        return notification;
    }
    
    // Factory method - subclasses implement this
    protected abstract Notification createNotification();
    
    // Hook method - can be overridden
    protected void configureNotification(Notification notification) {
        // Default configuration
    }
}

/**
 * Concrete Creators - implement factory method
 */
class EmailNotificationCreator extends NotificationCreator {
    
    @Override
    protected Notification createNotification() {
        return new EmailNotification();
    }
    
    @Override
    protected void configureNotification(Notification notification) {
        System.out.println("Configuring Email with SMTP settings");
    }
}

class SMSNotificationCreator extends NotificationCreator {
    
    @Override
    protected Notification createNotification() {
        return new SMSNotification();
    }
    
    @Override
    protected void configureNotification(Notification notification) {
        System.out.println("Configuring SMS with Twilio API");
    }
}

class PushNotificationCreator extends NotificationCreator {
    
    @Override
    protected Notification createNotification() {
        return new PushNotification();
    }
    
    @Override
    protected void configureNotification(Notification notification) {
        System.out.println("Configuring Push with Firebase");
    }
}


// ============================================================================
// DEMO - Compare all approaches
// ============================================================================

public class FactoryDesignPattern {
    public static void main(String[] args) {
        
        System.out.println("========================================");
        System.out.println("PROBLEM - Without Factory");
        System.out.println("========================================\n");
        
        NotificationServiceBad badService = new NotificationServiceBad();
        badService.sendNotification("EMAIL", "Hello via Email");
        badService.sendNotification("SMS", "Hello via SMS");
        
        System.out.println("\n========================================");
        System.out.println("SOLUTION 1 - Simple Factory");
        System.out.println("========================================\n");
        
        NotificationService service = new NotificationService();
        service.sendNotification("EMAIL", "Hello via Email");
        service.sendNotification("SMS", "Hello via SMS");
        service.sendNotification("PUSH", "Hello via Push");
        service.sendNotification("SLACK", "Hello via Slack");
        
        System.out.println("\n========================================");
        System.out.println("SOLUTION 2 - Factory Method Pattern");
        System.out.println("========================================\n");
        
        NotificationCreator emailCreator = new EmailNotificationCreator();
        NotificationCreator smsCreator = new SMSNotificationCreator();
        NotificationCreator pushCreator = new PushNotificationCreator();
        
        Notification email = emailCreator.getNotification();
        Notification sms = smsCreator.getNotification();
        Notification push = pushCreator.getNotification();
        
        email.send("Factory Method Email");
        sms.send("Factory Method SMS");
        push.send("Factory Method Push");

        
        System.out.println("\n========================================");
        System.out.println("KEY TAKEAWAYS");
        System.out.println("========================================");
        System.out.println("✓ Decouples object creation from usage");
        System.out.println("✓ Client doesn't know concrete classes");
        System.out.println("✓ Easy to add new types");
        System.out.println("✓ Follows Open/Closed Principle");
        System.out.println("✓ Centralizes creation logic");
    }
}
