package com.rajan.lld.DesignPatterns.BehavioralDesignPatterns.ChainOfResponsibilityDesignPattern;

/**
 * CHAIN OF RESPONSIBILITY DESIGN PATTERN - Complete Example
 * 
 * Definition: Passes a request along a chain of handlers. Each handler decides 
 * either to process the request or to pass it to the next handler in the chain.
 * 
 * In simple terms:
 * - Request passes through a chain of handlers
 * - Each handler can process or pass to next
 * - Decouples sender from receiver
 * 
 * When to use:
 * - Multiple objects can handle a request
 * - Handler isn't known in advance
 * - Want to issue request without specifying receiver
 * - Set of handlers should be specified dynamically
 */

// ============================================================================
// PROBLEM - Without Chain of Responsibility Pattern
// ============================================================================

/**
 * PROBLEM: Tight coupling and complex if-else chains
 * 
 * Why is this bad?
 * - Tight coupling: Sender knows all handlers
 * - Complex logic: Nested if-else for each handler
 * - Hard to extend: Adding new handler requires modifying code
 * - No flexibility: Can't change handler order dynamically
 * - Violates Open/Closed: Must modify existing code
 */
class BadSupportSystem {
    public void handleRequest(String issue, String priority) {
        // Complex if-else chain - tightly coupled
        if (priority.equals("LOW")) {
            System.out.println("Level 1 Support handling: " + issue);
        } else if (priority.equals("MEDIUM")) {
            System.out.println("Level 2 Support handling: " + issue);
        } else if (priority.equals("HIGH")) {
            System.out.println("Level 3 Support handling: " + issue);
        } else if (priority.equals("CRITICAL")) {
            System.out.println("Manager handling: " + issue);
        } else {
            System.out.println("Unknown priority!");
        }
        // Adding new priority level requires modifying this method!
    }
}


// ============================================================================
// SOLUTION - Chain of Responsibility Pattern
// ============================================================================

/**
 * Step 1: Handler Interface/Abstract Class
 */
abstract class SupportHandler {
    protected SupportHandler nextHandler;
    
    public void setNext(SupportHandler handler) {
        this.nextHandler = handler;
    }
    
    public abstract void handleRequest(SupportTicket ticket);
}

class SupportTicket {
    private String issue;
    private String priority;
    
    public SupportTicket(String issue, String priority) {
        this.issue = issue;
        this.priority = priority;
    }
    
    public String getIssue() { return issue; }
    public String getPriority() { return priority; }
}

/**
 * Step 2: Concrete Handlers
 */
class Level1Support extends SupportHandler {
    @Override
    public void handleRequest(SupportTicket ticket) {
        if (ticket.getPriority().equals("LOW")) {
            System.out.println("👨‍💼 Level 1 Support: Handling '" + ticket.getIssue() + "'");
        } else if (nextHandler != null) {
            System.out.println("👨‍💼 Level 1 Support: Escalating to next level");
            nextHandler.handleRequest(ticket);
        }
    }
}

class Level2Support extends SupportHandler {
    @Override
    public void handleRequest(SupportTicket ticket) {
        if (ticket.getPriority().equals("MEDIUM")) {
            System.out.println("👨‍🔧 Level 2 Support: Handling '" + ticket.getIssue() + "'");
        } else if (nextHandler != null) {
            System.out.println("👨‍🔧 Level 2 Support: Escalating to next level");
            nextHandler.handleRequest(ticket);
        }
    }
}

class Level3Support extends SupportHandler {
    @Override
    public void handleRequest(SupportTicket ticket) {
        if (ticket.getPriority().equals("HIGH")) {
            System.out.println("👨‍💻 Level 3 Support: Handling '" + ticket.getIssue() + "'");
        } else if (nextHandler != null) {
            System.out.println("👨‍💻 Level 3 Support: Escalating to manager");
            nextHandler.handleRequest(ticket);
        }
    }
}

class ManagerSupport extends SupportHandler {
    @Override
    public void handleRequest(SupportTicket ticket) {
        System.out.println("👔 Manager: Handling critical issue '" + ticket.getIssue() + "'");
    }
}


// ============================================================================
// REAL-WORLD EXAMPLE 1 - Authentication Chain
// ============================================================================

abstract class AuthenticationHandler {
    protected AuthenticationHandler next;
    
    public void setNext(AuthenticationHandler handler) {
        this.next = handler;
    }
    
    public abstract boolean handle(String username, String password);
}

class UserExistsHandler extends AuthenticationHandler {
    @Override
    public boolean handle(String username, String password) {
        if (username == null || username.isEmpty()) {
            System.out.println("❌ User doesn't exist");
            return false;
        }
        System.out.println("✓ User exists");
        return next != null ? next.handle(username, password) : true;
    }
}

class PasswordValidationHandler extends AuthenticationHandler {
    @Override
    public boolean handle(String username, String password) {
        if (!"password123".equals(password)) {
            System.out.println("❌ Invalid password");
            return false;
        }
        System.out.println("✓ Password valid");
        return next != null ? next.handle(username, password) : true;
    }
}

class RoleCheckHandler extends AuthenticationHandler {
    @Override
    public boolean handle(String username, String password) {
        if (username.startsWith("admin")) {
            System.out.println("✓ Admin role verified");
        } else {
            System.out.println("✓ User role verified");
        }
        return next != null ? next.handle(username, password) : true;
    }
}

class TwoFactorAuthHandler extends AuthenticationHandler {
    @Override
    public boolean handle(String username, String password) {
        System.out.println("✓ 2FA verification passed");
        return next != null ? next.handle(username, password) : true;
    }
}


// ============================================================================
// REAL-WORLD EXAMPLE 2 - Logging Chain
// ============================================================================

abstract class Logger {
    public static final int INFO = 1;
    public static final int DEBUG = 2;
    public static final int ERROR = 3;
    
    protected int level;
    protected Logger nextLogger;
    
    public void setNext(Logger logger) {
        this.nextLogger = logger;
    }
    
    public void logMessage(int level, String message) {
        if (this.level <= level) {
            write(message);
        }
        if (nextLogger != null) {
            nextLogger.logMessage(level, message);
        }
    }
    
    protected abstract void write(String message);
}

class ConsoleLogger extends Logger {
    public ConsoleLogger(int level) {
        this.level = level;
    }
    
    @Override
    protected void write(String message) {
        System.out.println("📺 Console: " + message);
    }
}

class FileLogger extends Logger {
    public FileLogger(int level) {
        this.level = level;
    }
    
    @Override
    protected void write(String message) {
        System.out.println("📄 File: " + message);
    }
}

class ErrorLogger extends Logger {
    public ErrorLogger(int level) {
        this.level = level;
    }
    
    @Override
    protected void write(String message) {
        System.out.println("🚨 Error Log: " + message);
    }
}


// ============================================================================
// REAL-WORLD EXAMPLE 3 - Expense Approval Chain
// ============================================================================

abstract class Approver {
    protected Approver nextApprover;
    protected String name;
    protected double approvalLimit;
    
    public Approver(String name, double approvalLimit) {
        this.name = name;
        this.approvalLimit = approvalLimit;
    }
    
    public void setNext(Approver approver) {
        this.nextApprover = approver;
    }
    
    public void approveExpense(double amount, String description) {
        if (amount <= approvalLimit) {
            System.out.println("✅ " + name + " approved $" + amount + " for: " + description);
        } else if (nextApprover != null) {
            System.out.println("⬆️  " + name + " escalating to next approver");
            nextApprover.approveExpense(amount, description);
        } else {
            System.out.println("❌ Amount too high, no approver available");
        }
    }
}

class TeamLead extends Approver {
    public TeamLead() {
        super("Team Lead", 1000);
    }
}

class Manager extends Approver {
    public Manager() {
        super("Manager", 5000);
    }
}

class Director extends Approver {
    public Director() {
        super("Director", 20000);
    }
}

class CEO extends Approver {
    public CEO() {
        super("CEO", 100000);
    }
}


// ============================================================================
// REAL-WORLD EXAMPLE 4 - Request Validation Chain
// ============================================================================

abstract class ValidationHandler {
    protected ValidationHandler next;
    
    public void setNext(ValidationHandler handler) {
        this.next = handler;
    }
    
    public boolean validate(Request request) {
        if (!check(request)) {
            return false;
        }
        if (next != null) {
            return next.validate(request);
        }
        return true;
    }
    
    protected abstract boolean check(Request request);
}

class Request {
    private String username;
    private String email;
    private String password;
    
    public Request(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
    
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
}

class UsernameValidator extends ValidationHandler {
    @Override
    protected boolean check(Request request) {
        if (request.getUsername() == null || request.getUsername().length() < 3) {
            System.out.println("❌ Username must be at least 3 characters");
            return false;
        }
        System.out.println("✓ Username valid");
        return true;
    }
}

class EmailValidator extends ValidationHandler {
    @Override
    protected boolean check(Request request) {
        if (!request.getEmail().contains("@")) {
            System.out.println("❌ Invalid email format");
            return false;
        }
        System.out.println("✓ Email valid");
        return true;
    }
}

class PasswordValidator extends ValidationHandler {
    @Override
    protected boolean check(Request request) {
        if (request.getPassword().length() < 8) {
            System.out.println("❌ Password must be at least 8 characters");
            return false;
        }
        System.out.println("✓ Password valid");
        return true;
    }
}


// ============================================================================
// DEMO
// ============================================================================

public class ChainOfResponsibilityDesignPattern {
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║   CHAIN OF RESPONSIBILITY PATTERN - DEMONSTRATION         ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        
        // PROBLEM: Without Chain of Responsibility
        System.out.println("\n❌ PROBLEM: Without Chain of Responsibility Pattern");
        System.out.println("═══════════════════════════════════════════════════════════");
        BadSupportSystem badSystem = new BadSupportSystem();
        badSystem.handleRequest("Password reset", "LOW");
        badSystem.handleRequest("Server down", "CRITICAL");
        System.out.println("\n⚠️  Issues: Tight coupling, complex if-else, hard to extend");
        
        // SOLUTION: With Chain of Responsibility
        System.out.println("\n\n✅ SOLUTION: With Chain of Responsibility Pattern");
        System.out.println("═══════════════════════════════════════════════════════════");
        
        // Build the chain
        SupportHandler level1 = new Level1Support();
        SupportHandler level2 = new Level2Support();
        SupportHandler level3 = new Level3Support();
        SupportHandler manager = new ManagerSupport();
        
        level1.setNext(level2);
        level2.setNext(level3);
        level3.setNext(manager);
        
        System.out.println("\n1️⃣  Low Priority Ticket:");
        level1.handleRequest(new SupportTicket("Password reset", "LOW"));
        
        System.out.println("\n2️⃣  Medium Priority Ticket:");
        level1.handleRequest(new SupportTicket("Software installation", "MEDIUM"));
        
        System.out.println("\n3️⃣  High Priority Ticket:");
        level1.handleRequest(new SupportTicket("Database error", "HIGH"));
        
        System.out.println("\n4️⃣  Critical Priority Ticket:");
        level1.handleRequest(new SupportTicket("Server down", "CRITICAL"));
        
        // EXAMPLE 1: Authentication Chain
        System.out.println("\n\n🔐 EXAMPLE 1: Authentication Chain");
        System.out.println("═══════════════════════════════════════════════════════════");
        
        AuthenticationHandler userCheck = new UserExistsHandler();
        AuthenticationHandler passwordCheck = new PasswordValidationHandler();
        AuthenticationHandler roleCheck = new RoleCheckHandler();
        AuthenticationHandler twoFactorAuth = new TwoFactorAuthHandler();
        
        userCheck.setNext(passwordCheck);
        passwordCheck.setNext(roleCheck);
        roleCheck.setNext(twoFactorAuth);
        
        System.out.println("\nAuthentication attempt 1 (Success):");
        boolean result1 = userCheck.handle("admin", "password123");
        System.out.println("Result: " + (result1 ? "✅ Authenticated" : "❌ Failed"));
        
        System.out.println("\nAuthentication attempt 2 (Failed):");
        boolean result2 = userCheck.handle("john", "wrong");
        System.out.println("Result: " + (result2 ? "✅ Authenticated" : "❌ Failed"));
        
        // EXAMPLE 2: Logging Chain
        System.out.println("\n\n📝 EXAMPLE 2: Logging Chain");
        System.out.println("═══════════════════════════════════════════════════════════");
        
        Logger consoleLogger = new ConsoleLogger(Logger.INFO);
        Logger fileLogger = new FileLogger(Logger.DEBUG);
        Logger errorLogger = new ErrorLogger(Logger.ERROR);
        
        consoleLogger.setNext(fileLogger);
        fileLogger.setNext(errorLogger);
        
        System.out.println("\nINFO level message:");
        consoleLogger.logMessage(Logger.INFO, "Application started");
        
        System.out.println("\nDEBUG level message:");
        consoleLogger.logMessage(Logger.DEBUG, "Processing request");
        
        System.out.println("\nERROR level message:");
        consoleLogger.logMessage(Logger.ERROR, "Database connection failed");
        
        // EXAMPLE 3: Expense Approval Chain
        System.out.println("\n\n💰 EXAMPLE 3: Expense Approval Chain");
        System.out.println("═══════════════════════════════════════════════════════════");
        
        Approver teamLead = new TeamLead();
        Approver manager2 = new Manager();
        Approver director = new Director();
        Approver ceo = new CEO();
        
        teamLead.setNext(manager2);
        manager2.setNext(director);
        director.setNext(ceo);
        
        System.out.println("\nExpense 1: $500");
        teamLead.approveExpense(500, "Office supplies");
        
        System.out.println("\nExpense 2: $3000");
        teamLead.approveExpense(3000, "New laptops");
        
        System.out.println("\nExpense 3: $15000");
        teamLead.approveExpense(15000, "Team training");
        
        System.out.println("\nExpense 4: $50000");
        teamLead.approveExpense(50000, "Office renovation");
        
        // EXAMPLE 4: Request Validation Chain
        System.out.println("\n\n✅ EXAMPLE 4: Request Validation Chain");
        System.out.println("═══════════════════════════════════════════════════════════");
        
        ValidationHandler usernameValidator = new UsernameValidator();
        ValidationHandler emailValidator = new EmailValidator();
        ValidationHandler passwordValidator = new PasswordValidator();
        
        usernameValidator.setNext(emailValidator);
        emailValidator.setNext(passwordValidator);
        
        System.out.println("\nValidation 1 (Valid request):");
        Request request1 = new Request("john_doe", "john@example.com", "password123");
        boolean valid1 = usernameValidator.validate(request1);
        System.out.println("Result: " + (valid1 ? "✅ Valid" : "❌ Invalid"));
        
        System.out.println("\nValidation 2 (Invalid email):");
        Request request2 = new Request("jane", "invalid-email", "pass123456");
        boolean valid2 = usernameValidator.validate(request2);
        System.out.println("Result: " + (valid2 ? "✅ Valid" : "❌ Invalid"));
        
        // KEY BENEFITS
        System.out.println("\n\n🎯 KEY BENEFITS OF CHAIN OF RESPONSIBILITY PATTERN");
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("✓ Decouples sender from receiver");
        System.out.println("✓ Flexible chain configuration");
        System.out.println("✓ Easy to add new handlers");
        System.out.println("✓ Single Responsibility Principle");
        System.out.println("✓ Open/Closed Principle");
        System.out.println("✓ Dynamic handler ordering");
        System.out.println("✓ Request can be handled by multiple handlers");
    }
}
