package com.rajan.lld.SolidPrinciples.DependencyInversionPrinciple;

/**
 * DEPENDENCY INVERSION PRINCIPLE (DIP) - Complete Example
 * 
 * Definition: High-level modules should not depend on low-level modules.
 * Both should depend on abstractions (interfaces).
 * 
 * In simple terms:
 * - Don't depend on concrete classes
 * - Depend on interfaces or abstract classes
 * - Abstractions should not depend on details
 * - Details should depend on abstractions
 */

// ============================================================================
// BAD EXAMPLE - Violates DIP
// ============================================================================

/**
 * PROBLEM: Low-level module (concrete implementation)
 * These are fine on their own, but the problem is how they're used
 */
class EmailSenderBad {
    public void sendEmail(String recipient, String message) {
        System.out.println("Sending Email to: " + recipient);
        System.out.println("Message: " + message);
    }
}

class SMSSenderBad {
    public void sendSMS(String recipient, String message) {
        System.out.println("Sending SMS to: " + recipient);
        System.out.println("Message: " + message);
    }
}

/**
 * PROBLEM: High-level module directly depends on low-level module
 * NotificationServiceBad depends on concrete EmailSenderBad class
 * 
 * Why is this bad?
 * - Tight coupling to EmailSenderBad
 * - Cannot switch to SMS without modifying this class
 * - Hard to test (cannot mock EmailSenderBad easily)
 * - Violates DIP - high-level depends on low-level
 * - Not flexible or extensible
 */
class NotificationServiceBad {
    private EmailSenderBad emailSender;
    
    // PROBLEM: Creating concrete instance directly
    public NotificationServiceBad() {
        this.emailSender = new EmailSenderBad(); // Tight coupling!
    }
    
    public void sendNotification(String recipient, String message) {
        // PROBLEM: Hardcoded to use email only
        emailSender.sendEmail(recipient, message);
    }
    
    // If we want to add SMS, we need to modify this class!
    // This violates Open/Closed Principle too!
}

/**
 * PROBLEM: To support SMS, we'd need to modify NotificationServiceBad
 * or create a new class - not flexible!
 */
class NotificationServiceWithSMSBad {
    private SMSSenderBad smsSender;
    
    public NotificationServiceWithSMSBad() {
        this.smsSender = new SMSSenderBad();
    }
    
    public void sendNotification(String recipient, String message) {
        smsSender.sendSMS(recipient, message);
    }
}


// ============================================================================
// GOOD EXAMPLE - Follows DIP
// ============================================================================

/**
 * SOLUTION: Define abstraction (interface)
 * Both high-level and low-level modules depend on this abstraction
 * 
 * Key idea: Create a contract that all notification senders follow
 */
interface NotificationSender {
    void send(String recipient, String message);
}

/**
 * SOLUTION: Low-level modules implement the abstraction
 * These are details that depend on abstraction (DIP satisfied)
 */
class EmailSender implements NotificationSender {
    @Override
    public void send(String recipient, String message) {
        System.out.println("Sending Email to: " + recipient);
        System.out.println("Message: " + message);
    }
}

class SMSSender implements NotificationSender {
    @Override
    public void send(String recipient, String message) {
        System.out.println("Sending SMS to: " + recipient);
        System.out.println("Message: " + message);
    }
}

class PushNotificationSender implements NotificationSender {
    @Override
    public void send(String recipient, String message) {
        System.out.println("Sending Push Notification to: " + recipient);
        System.out.println("Message: " + message);
    }
}

class SlackSender implements NotificationSender {
    @Override
    public void send(String recipient, String message) {
        System.out.println("Sending Slack message to: " + recipient);
        System.out.println("Message: " + message);
    }
}

/**
 * SOLUTION: High-level module depends on abstraction
 * NotificationService depends on NotificationSender interface, not concrete classes
 * 
 * Benefits:
 * - Loose coupling - depends on interface, not implementation
 * - Flexible - can use any NotificationSender implementation
 * - Easy to test - can inject mock implementations
 * - Follows DIP - both high and low level depend on abstraction
 * - Extensible - add new senders without modifying this class
 */
class NotificationService {
    private NotificationSender sender;
    
    // SOLUTION: Dependency Injection via constructor
    // We inject the abstraction, not the concrete class
    public NotificationService(NotificationSender sender) {
        this.sender = sender;
    }
    
    public void sendNotification(String recipient, String message) {
        // Works with any NotificationSender implementation
        sender.send(recipient, message);
    }
    
    // SOLUTION: Can change sender at runtime
    public void setSender(NotificationSender sender) {
        this.sender = sender;
    }
}


// ============================================================================
// DEMO - Compare Bad vs Good approach
// ============================================================================

public class DependencyInversionPrinciple {
    public static void main(String[] args) {
        
        System.out.println("========================================");
        System.out.println("BAD APPROACH - Violates DIP");
        System.out.println("========================================");
        
        // PROBLEM: Hardcoded to use email
        NotificationServiceBad badService = new NotificationServiceBad();
        badService.sendNotification("john@example.com", "Hello John!");
        
        System.out.println("\nPROBLEM: Want to use SMS? Need different class!");
        NotificationServiceWithSMSBad smsService = new NotificationServiceWithSMSBad();
        smsService.sendNotification("+1234567890", "Hello via SMS!");
        
        System.out.println("\n========================================");
        System.out.println("GOOD APPROACH - Follows DIP");
        System.out.println("========================================");
        
        // SOLUTION: Inject dependency - use Email
        System.out.println("Using Email Sender:");
        NotificationService emailService = new NotificationService(new EmailSender());
        emailService.sendNotification("john@example.com", "Hello John!");
        
        // SOLUTION: Same class, different implementation - use SMS
        System.out.println("\nUsing SMS Sender:");
        NotificationService smsServiceGood = new NotificationService(new SMSSender());
        smsServiceGood.sendNotification("+1234567890", "Hello via SMS!");
        
        // SOLUTION: Same class, different implementation - use Push
        System.out.println("\nUsing Push Notification:");
        NotificationService pushService = new NotificationService(new PushNotificationSender());
        pushService.sendNotification("user123", "You have a new message!");
        
        // SOLUTION: Same class, different implementation - use Slack
        System.out.println("\nUsing Slack:");
        NotificationService slackService = new NotificationService(new SlackSender());
        slackService.sendNotification("#general", "Deployment successful!");
        
        // SOLUTION: Can even change sender at runtime
        System.out.println("\nChanging sender at runtime:");
        NotificationService flexibleService = new NotificationService(new EmailSender());
        flexibleService.sendNotification("user@example.com", "First message via email");
        
        flexibleService.setSender(new SMSSender());
        flexibleService.sendNotification("+1234567890", "Second message via SMS");
        
        System.out.println("\n========================================");
        System.out.println("BENEFITS OF DIP:");
        System.out.println("========================================");
        System.out.println("✓ Loose coupling - depend on abstractions");
        System.out.println("✓ Flexible - easily switch implementations");
        System.out.println("✓ Testable - inject mock implementations");
        System.out.println("✓ Extensible - add new senders without modifying service");
        System.out.println("✓ Maintainable - changes isolated to implementations");
        System.out.println("✓ Follows SOLID principles completely");
    }
}
