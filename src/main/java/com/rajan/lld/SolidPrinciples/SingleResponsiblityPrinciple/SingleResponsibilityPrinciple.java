package com.rajan.lld.SolidPrinciples.SingleResponsiblityPrinciple;

/**
 * SINGLE RESPONSIBILITY PRINCIPLE (SRP) - Complete Example
 * 
 * Definition: A class should have only ONE reason to change.
 * In other words, a class should have only ONE responsibility or job.
 */

// ============================================================================
// BAD EXAMPLE - Violates SRP
// ============================================================================

/**
 * PROBLEM: This class has MULTIPLE responsibilities
 * 1. Managing user data (name, email)
 * 2. Sending emails
 * 3. Saving to database
 * 4. Validating email
 * 
 * Why is this bad?
 * - If email sending logic changes → we modify this class
 * - If database logic changes → we modify this class
 * - If validation rules change → we modify this class
 * - Hard to test individual functionalities
 * - Cannot reuse email/database logic elsewhere
 * - Class becomes bloated and difficult to maintain
 */
class UserBad {
    private String name;
    private String email;

    public UserBad(String name, String email) {
        this.name = name;
        this.email = email;
    }

    // Responsibility 1: User data management
    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    // PROBLEM: Responsibility 2 - Email sending (NOT user's job!)
    public void sendEmail(String message) {
        System.out.println("Connecting to email server...");
        System.out.println("Sending email to: " + email);
        System.out.println("Message: " + message);
    }

    // PROBLEM: Responsibility 3 - Database operations (NOT user's job!)
    public void saveToDatabase() {
        System.out.println("Connecting to database...");
        System.out.println("Saving user: " + name + " to database");
    }

    // PROBLEM: Responsibility 4 - Validation logic (NOT user's job!)
    public boolean validateEmail() {
        return email != null && email.contains("@");
    }
}


// ============================================================================
// GOOD EXAMPLE - Follows SRP
// ============================================================================

/**
 * SOLUTION: User class with SINGLE responsibility
 * Only manages user data - nothing else!
 * 
 * This class has only ONE reason to change:
 * - If user data structure changes (e.g., add phone number)
 */
class User {
    private String name;
    private String email;

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    // Only user data related methods
    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}

/**
 * SOLUTION: EmailService with SINGLE responsibility
 * Only handles email operations
 * 
 * This class has only ONE reason to change:
 * - If email sending mechanism changes (e.g., switch email provider)
 */
class EmailService {
    public void sendEmail(String email, String message) {
        // Email sending logic isolated here
        System.out.println("Connecting to email server...");
        System.out.println("Sending email to: " + email);
        System.out.println("Message: " + message);
    }
}

/**
 * SOLUTION: UserRepository with SINGLE responsibility
 * Only handles database operations for users
 * 
 * This class has only ONE reason to change:
 * - If database technology changes (e.g., MySQL to MongoDB)
 */
class UserRepository {
    public void save(User user) {
        // Database logic isolated here
        System.out.println("Connecting to database...");
        System.out.println("Saving user: " + user.getName() + " to database");
    }
}

/**
 * SOLUTION: EmailValidator with SINGLE responsibility
 * Only handles email validation logic
 * 
 * This class has only ONE reason to change:
 * - If validation rules change (e.g., add domain checking)
 */
class EmailValidator {
    public boolean isValid(String email) {
        // Validation logic isolated here
        return email != null && email.contains("@");
    }
}


// ============================================================================
// DEMO - Compare Bad vs Good approach
// ============================================================================

public class SingleResponsibilityPrinciple {
    public static void main(String[] args) {
        
        System.out.println("========================================");
        System.out.println("BAD APPROACH - Violates SRP");
        System.out.println("========================================");
        
        // One class doing everything - tightly coupled
        UserBad badUser = new UserBad("John Doe", "john@example.com");
        badUser.validateEmail();      // User shouldn't validate emails
        badUser.saveToDatabase();     // User shouldn't save to database
        badUser.sendEmail("Welcome!"); // User shouldn't send emails
        
        System.out.println("\n========================================");
        System.out.println("GOOD APPROACH - Follows SRP");
        System.out.println("========================================");
        
        // Each class has one responsibility - loosely coupled
        User user = new User("Jane Doe", "jane@example.com");
        EmailValidator validator = new EmailValidator();
        UserRepository repository = new UserRepository();
        EmailService emailService = new EmailService();
        
        // Each service does its own job
        if (validator.isValid(user.getEmail())) {
            repository.save(user);
            emailService.sendEmail(user.getEmail(), "Welcome!");
        }
    }
}
