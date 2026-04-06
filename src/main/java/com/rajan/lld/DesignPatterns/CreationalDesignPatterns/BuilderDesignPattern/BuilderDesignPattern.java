package com.rajan.lld.DesignPatterns.CreationalDesignPatterns.BuilderDesignPattern;

/**
 * BUILDER DESIGN PATTERN - Complete Example
 * 
 * Definition: Constructs complex objects step by step, especially when objects
 * have many optional parameters.
 * 
 * In simple terms:
 * - Build complex objects piece by piece
 * - Separate construction from representation
 * - Handle many optional parameters elegantly
 * 
 * When to use:
 * - Objects with many parameters (especially optional ones)
 * - Avoid "telescoping constructor" problem
 * - Need immutable objects with validation
 * - Step-by-step object construction
 */

// ============================================================================
// PROBLEM - Telescoping Constructor Anti-Pattern
// ============================================================================

/**
 * PROBLEM: Too many constructors for different parameter combinations
 * This is called "Telescoping Constructor" antipattern
 * 
 * Why is this bad?
 * - Hard to read: new User("John", "john@email.com", 25, null, null, true, "IT")
 * - Error-prone: Easy to mix up parameter order
 * - Not flexible: Need constructor for every combination
 * - Maintenance nightmare: Adding parameter affects all constructors
 */
class UserBad {
    private String name;
    private String email;
    private int age;
    private String phone;
    private String address;
    private boolean isActive;
    private String department;
    
    // Constructor 1: Only name
    public UserBad(String name) {
        this(name, null, 0, null, null, true, null);
    }
    
    // Constructor 2: Name and email
    public UserBad(String name, String email) {
        this(name, email, 0, null, null, true, null);
    }
    
    // Constructor 3: Name, email, and age
    public UserBad(String name, String email, int age) {
        this(name, email, age, null, null, true, null);
    }
    
    // PROBLEM: The "telescoping constructor" - confusing and hard to maintain
    public UserBad(String name, String email, int age, String phone, 
                   String address, boolean isActive, String department) {
        this.name = name;
        this.email = email;
        this.age = age;
        this.phone = phone;
        this.address = address;
        this.isActive = isActive;
        this.department = department;
    }
    
    @Override
    public String toString() {
        return "User{name='" + name + "', email='" + email + "', age=" + age + 
               ", phone='" + phone + "', address='" + address + "', active=" + isActive + 
               ", dept='" + department + "'}";
    }
}


// ============================================================================
// SOLUTION - Builder Pattern
// ============================================================================

/**
 * SOLUTION: User class with Builder pattern
 * Clean, readable, and flexible object construction
 */
class User {
    // Final fields for immutability
    private final String name;
    private final String email;
    private final int age;
    private final String phone;
    private final String address;
    private final boolean isActive;
    private final String department;
    
    // Private constructor - only Builder can create User
    private User(UserBuilder builder) {
        this.name = builder.name;
        this.email = builder.email;
        this.age = builder.age;
        this.phone = builder.phone;
        this.address = builder.address;
        this.isActive = builder.isActive;
        this.department = builder.department;
    }
    
    @Override
    public String toString() {
        return "User{name='" + name + "', email='" + email + "', age=" + age + 
               ", phone='" + phone + "', address='" + address + "', active=" + isActive + 
               ", dept='" + department + "'}";
    }
    
    /**
     * Static nested Builder class
     * Provides fluent interface for object construction
     */
    public static class UserBuilder {
        // Required parameters
        private final String name;
        
        // Optional parameters with default values
        private String email = "";
        private int age = 0;
        private String phone = "";
        private String address = "";
        private boolean isActive = true;
        private String department = "";
        
        // Constructor with required parameters only
        public UserBuilder(String name) {
            this.name = name;
        }
        
        // Fluent interface - each method returns builder for chaining
        public UserBuilder email(String email) {
            this.email = email;
            return this;
        }
        
        public UserBuilder age(int age) {
            this.age = age;
            return this;
        }
        
        public UserBuilder phone(String phone) {
            this.phone = phone;
            return this;
        }
        
        public UserBuilder address(String address) {
            this.address = address;
            return this;
        }
        
        public UserBuilder isActive(boolean isActive) {
            this.isActive = isActive;
            return this;
        }
        
        public UserBuilder department(String department) {
            this.department = department;
            return this;
        }
        
        // Build method with validation
        public User build() {
            // Validation logic before creating object
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Name is required");
            }
            if (age < 0) {
                throw new IllegalArgumentException("Age cannot be negative");
            }
            
            return new User(this);
        }
    }
}


// ============================================================================
// REAL-WORLD EXAMPLE 1 - Computer Builder
// ============================================================================

/**
 * Real-world example: Building a custom computer
 */
class Computer {
    private final String cpu;
    private final String gpu;
    private final int ram;
    private final int storage;
    private final String motherboard;
    private final String powerSupply;
    private final String coolingSystem;
    
    private Computer(ComputerBuilder builder) {
        this.cpu = builder.cpu;
        this.gpu = builder.gpu;
        this.ram = builder.ram;
        this.storage = builder.storage;
        this.motherboard = builder.motherboard;
        this.powerSupply = builder.powerSupply;
        this.coolingSystem = builder.coolingSystem;
    }
    
    @Override
    public String toString() {
        return "Computer{" +
               "CPU='" + cpu + "'" +
               ", GPU='" + gpu + "'" +
               ", RAM=" + ram + "GB" +
               ", Storage=" + storage + "GB" +
               ", Motherboard='" + motherboard + "'" +
               ", PSU='" + powerSupply + "'" +
               ", Cooling='" + coolingSystem + "'" +
               "}";
    }
    
    public static class ComputerBuilder {
        // Required
        private final String cpu;
        
        // Optional with defaults
        private String gpu = "Integrated Graphics";
        private int ram = 8;
        private int storage = 256;
        private String motherboard = "Standard ATX";
        private String powerSupply = "500W";
        private String coolingSystem = "Stock Cooler";
        
        public ComputerBuilder(String cpu) {
            this.cpu = cpu;
        }
        
        public ComputerBuilder gpu(String gpu) {
            this.gpu = gpu;
            return this;
        }
        
        public ComputerBuilder ram(int ram) {
            this.ram = ram;
            return this;
        }
        
        public ComputerBuilder storage(int storage) {
            this.storage = storage;
            return this;
        }
        
        public ComputerBuilder motherboard(String motherboard) {
            this.motherboard = motherboard;
            return this;
        }
        
        public ComputerBuilder powerSupply(String powerSupply) {
            this.powerSupply = powerSupply;
            return this;
        }
        
        public ComputerBuilder coolingSystem(String coolingSystem) {
            this.coolingSystem = coolingSystem;
            return this;
        }
        
        public Computer build() {
            // Validation
            if (ram < 4) {
                throw new IllegalArgumentException("RAM must be at least 4GB");
            }
            if (storage < 128) {
                throw new IllegalArgumentException("Storage must be at least 128GB");
            }
            return new Computer(this);
        }
    }
}


// ============================================================================
// REAL-WORLD EXAMPLE 2 - HTTP Request Builder
// ============================================================================

/**
 * Real-world example: Building HTTP requests
 */
class HttpRequest {
    private final String url;
    private final String method;
    private final String body;
    private final String contentType;
    private final int timeout;
    private final boolean followRedirects;
    
    private HttpRequest(HttpRequestBuilder builder) {
        this.url = builder.url;
        this.method = builder.method;
        this.body = builder.body;
        this.contentType = builder.contentType;
        this.timeout = builder.timeout;
        this.followRedirects = builder.followRedirects;
    }
    
    @Override
    public String toString() {
        return "HttpRequest{" +
               method + " " + url +
               ", body='" + body + "'" +
               ", contentType='" + contentType + "'" +
               ", timeout=" + timeout + "ms" +
               ", followRedirects=" + followRedirects +
               "}";
    }
    
    public static class HttpRequestBuilder {
        // Required
        private final String url;
        
        // Optional with defaults
        private String method = "GET";
        private String body = "";
        private String contentType = "application/json";
        private int timeout = 5000;
        private boolean followRedirects = true;
        
        public HttpRequestBuilder(String url) {
            this.url = url;
        }
        
        public HttpRequestBuilder method(String method) {
            this.method = method;
            return this;
        }
        
        public HttpRequestBuilder body(String body) {
            this.body = body;
            return this;
        }
        
        public HttpRequestBuilder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }
        
        public HttpRequestBuilder timeout(int timeout) {
            this.timeout = timeout;
            return this;
        }
        
        public HttpRequestBuilder followRedirects(boolean followRedirects) {
            this.followRedirects = followRedirects;
            return this;
        }
        
        public HttpRequest build() {
            if (url == null || url.trim().isEmpty()) {
                throw new IllegalArgumentException("URL is required");
            }
            return new HttpRequest(this);
        }
    }
}


// ============================================================================
// DEMO - Compare Bad vs Good approach
// ============================================================================

public class BuilderDesignPattern {
    public static void main(String[] args) {
        
        System.out.println("========================================");
        System.out.println("PROBLEM - Telescoping Constructor");
        System.out.println("========================================\n");
        
        // Confusing - what do these parameters mean?
        UserBad badUser1 = new UserBad("John", "john@email.com", 25, null, null, true, "IT");
        System.out.println(badUser1);
        
        // Have to pass null for unused parameters
        UserBad badUser2 = new UserBad("Jane", null, 0, "123-456-7890", null, false, null);
        System.out.println(badUser2);
        System.out.println("Problem: Hard to read, error-prone, inflexible\n");
        
        System.out.println("========================================");
        System.out.println("SOLUTION - Builder Pattern");
        System.out.println("========================================\n");
        
        // Clear, readable, and flexible
        User user1 = new User.UserBuilder("John")
                .email("john@email.com")
                .age(25)
                .department("IT")
                .build();
        System.out.println(user1);
        
        // Only set what you need
        User user2 = new User.UserBuilder("Jane")
                .phone("123-456-7890")
                .isActive(false)
                .build();
        System.out.println(user2);
        
        // Minimal user
        User user3 = new User.UserBuilder("Bob").build();
        System.out.println(user3);
        
        System.out.println("\n========================================");
        System.out.println("REAL-WORLD - Computer Builder");
        System.out.println("========================================\n");
        
        // Basic office computer
        Computer officePC = new Computer.ComputerBuilder("Intel i5")
                .ram(16)
                .storage(512)
                .build();
        System.out.println("Office PC: " + officePC);
        
        // High-end gaming computer
        Computer gamingPC = new Computer.ComputerBuilder("Intel i9-13900K")
                .gpu("Nvidia RTX 4090")
                .ram(64)
                .storage(2000)
                .motherboard("ASUS ROG")
                .powerSupply("1000W")
                .coolingSystem("Liquid Cooling")
                .build();
        System.out.println("\nGaming PC: " + gamingPC);
        
        System.out.println("\n========================================");
        System.out.println("REAL-WORLD - HTTP Request Builder");
        System.out.println("========================================\n");
        
        // Simple GET request
        HttpRequest getRequest = new HttpRequest.HttpRequestBuilder("https://api.example.com/users")
                .build();
        System.out.println(getRequest);
        
        // Complex POST request
        HttpRequest postRequest = new HttpRequest.HttpRequestBuilder("https://api.example.com/users")
                .method("POST")
                .body("{\"name\":\"John\",\"email\":\"john@email.com\"}")
                .contentType("application/json")
                .timeout(10000)
                .followRedirects(false)
                .build();
        System.out.println("\n" + postRequest);
        
        System.out.println("\n========================================");
        System.out.println("KEY ADVANTAGES");
        System.out.println("========================================");
        System.out.println("✓ Readable and clear code");
        System.out.println("✓ Handles optional parameters elegantly");
        System.out.println("✓ Immutable objects");
        System.out.println("✓ Validation before object creation");
        System.out.println("✓ Flexible - set only what you need");
        System.out.println("✓ No telescoping constructors");
    }
}
