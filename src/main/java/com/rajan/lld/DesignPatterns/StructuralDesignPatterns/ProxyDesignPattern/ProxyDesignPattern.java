package com.rajan.lld.DesignPatterns.StructuralDesignPatterns.ProxyDesignPattern;

/**
 * PROXY DESIGN PATTERN - Complete Example
 * 
 * Definition: Provides a surrogate or placeholder for another object to control access to it.
 * 
 * In simple terms:
 * - Acts as a substitute/representative for another object
 * - Controls access to the real object
 * - Can add extra functionality (lazy loading, caching, access control, logging)
 * - Like a security guard controlling building access
 * 
 * Types of Proxies:
 * 1. Virtual Proxy - Lazy initialization (create expensive objects on demand)
 * 2. Protection Proxy - Access control (check permissions)
 * 3. Cache Proxy - Cache results (improve performance)
 * 4. Remote Proxy - Represent remote objects (network calls)
 * 5. Logging Proxy - Add logging (monitor access)
 * 
 * When to use:
 * - Lazy initialization of expensive objects
 * - Access control and security
 * - Caching expensive operations
 * - Remote object representation
 * - Logging and monitoring
 */

// ============================================================================
// PROBLEM - Without Proxy Pattern
// ============================================================================

/**
 * PROBLEM: Direct access to expensive objects causes issues
 * 
 * Why is this bad?
 * - Expensive objects created even when not needed (waste resources)
 * - No access control (anyone can access anything)
 * - Repeated expensive operations (no caching)
 * - No logging/monitoring of object usage
 * - Can't add functionality without modifying original class
 */

class ExpensiveImage {
    private String filename;
    
    public ExpensiveImage(String filename) {
        this.filename = filename;
        // This loads immediately - expensive!
        System.out.println("⚠️  Loading large image from disk: " + filename + " (5 seconds...)");
        try { Thread.sleep(1000); } catch (InterruptedException e) {}
    }
    
    public void display() {
        System.out.println("Displaying: " + filename);
    }
}

// Problem: All images loaded immediately, even if never displayed!
// ExpensiveImage img1 = new ExpensiveImage("photo1.jpg"); // Loads now
// ExpensiveImage img2 = new ExpensiveImage("photo2.jpg"); // Loads now
// ExpensiveImage img3 = new ExpensiveImage("photo3.jpg"); // Loads now
// img1.display(); // Only this one is used!


// ============================================================================
// SOLUTION - Proxy Pattern
// ============================================================================

/**
 * Step 1: Subject Interface (common interface for RealSubject and Proxy)
 */
interface Image {
    void display();
    String getFilename();
}

/**
 * Step 2: RealSubject (the actual expensive object)
 */
class RealImage implements Image {
    private String filename;
    
    public RealImage(String filename) {
        this.filename = filename;
        loadFromDisk();
    }
    
    private void loadFromDisk() {
        System.out.println("   📀 Loading image from disk: " + filename);
        try { Thread.sleep(500); } catch (InterruptedException e) {}
    }
    
    @Override
    public void display() {
        System.out.println("   🖼️  Displaying: " + filename);
    }
    
    @Override
    public String getFilename() {
        return filename;
    }
}

/**
 * Step 3: Virtual Proxy (lazy loading)
 */
class ImageProxy implements Image {
    private String filename;
    private RealImage realImage;
    
    public ImageProxy(String filename) {
        this.filename = filename;
        // No loading yet - just store filename
    }
    
    @Override
    public void display() {
        // Load only when needed (lazy initialization)
        if (realImage == null) {
            System.out.println("   🔄 Proxy: First access, loading real image...");
            realImage = new RealImage(filename);
        } else {
            System.out.println("   ✅ Proxy: Using cached image");
        }
        realImage.display();
    }
    
    @Override
    public String getFilename() {
        return filename;
    }
}


// ============================================================================
// REAL-WORLD EXAMPLE 1 - Protection Proxy (Access Control)
// ============================================================================

/**
 * Protection Proxy: Controls access based on permissions
 */

interface Document {
    void view();
    void edit(String content);
    void delete();
}

class RealDocument implements Document {
    private String name;
    private String content;
    
    public RealDocument(String name, String content) {
        this.name = name;
        this.content = content;
    }
    
    @Override
    public void view() {
        System.out.println("   📄 Viewing document: " + name);
        System.out.println("   Content: " + content);
    }
    
    @Override
    public void edit(String newContent) {
        this.content = newContent;
        System.out.println("   ✏️  Document edited: " + name);
    }
    
    @Override
    public void delete() {
        System.out.println("   🗑️  Document deleted: " + name);
    }
}

enum Role { VIEWER, EDITOR, ADMIN }

class ProtectedDocumentProxy implements Document {
    private RealDocument document;
    private Role userRole;
    
    public ProtectedDocumentProxy(RealDocument document, Role userRole) {
        this.document = document;
        this.userRole = userRole;
    }
    
    @Override
    public void view() {
        // Everyone can view
        System.out.println("   🔓 Access granted: Viewing");
        document.view();
    }
    
    @Override
    public void edit(String content) {
        if (userRole == Role.EDITOR || userRole == Role.ADMIN) {
            System.out.println("   🔓 Access granted: Editing");
            document.edit(content);
        } else {
            System.out.println("   🔒 Access denied: Need EDITOR or ADMIN role");
        }
    }
    
    @Override
    public void delete() {
        if (userRole == Role.ADMIN) {
            System.out.println("   🔓 Access granted: Deleting");
            document.delete();
        } else {
            System.out.println("   🔒 Access denied: Need ADMIN role");
        }
    }
}


// ============================================================================
// REAL-WORLD EXAMPLE 2 - Cache Proxy (Performance Optimization)
// ============================================================================

/**
 * Cache Proxy: Caches expensive operations
 */

interface DatabaseQuery {
    String executeQuery(String query);
}

class RealDatabase implements DatabaseQuery {
    @Override
    public String executeQuery(String query) {
        System.out.println("   🗄️  Executing expensive database query: " + query);
        try { Thread.sleep(500); } catch (InterruptedException e) {}
        return "Result for: " + query;
    }
}

class CachingDatabaseProxy implements DatabaseQuery {
    private RealDatabase database;
    private java.util.Map<String, String> cache;
    
    public CachingDatabaseProxy() {
        this.database = new RealDatabase();
        this.cache = new java.util.HashMap<>();
    }
    
    @Override
    public String executeQuery(String query) {
        if (cache.containsKey(query)) {
            System.out.println("   ⚡ Cache hit! Returning cached result");
            return cache.get(query);
        }
        
        System.out.println("   ❌ Cache miss! Querying database...");
        String result = database.executeQuery(query);
        cache.put(query, result);
        return result;
    }
}


// ============================================================================
// REAL-WORLD EXAMPLE 3 - Remote Proxy (Network Calls)
// ============================================================================

/**
 * Remote Proxy: Represents object in different address space
 */

interface VideoService {
    void streamVideo(String videoId);
    String getVideoInfo(String videoId);
}

class RemoteVideoService implements VideoService {
    private String serverUrl;
    
    public RemoteVideoService(String serverUrl) {
        this.serverUrl = serverUrl;
    }
    
    @Override
    public void streamVideo(String videoId) {
        System.out.println("   🌐 Connecting to " + serverUrl);
        System.out.println("   📡 Streaming video: " + videoId);
        try { Thread.sleep(300); } catch (InterruptedException e) {}
    }
    
    @Override
    public String getVideoInfo(String videoId) {
        System.out.println("   🌐 Fetching info from " + serverUrl);
        try { Thread.sleep(200); } catch (InterruptedException e) {}
        return "Video Info: " + videoId;
    }
}

class VideoServiceProxy implements VideoService {
    private RemoteVideoService remoteService;
    private String serverUrl;
    private java.util.Map<String, String> infoCache;
    
    public VideoServiceProxy(String serverUrl) {
        this.serverUrl = serverUrl;
        this.infoCache = new java.util.HashMap<>();
    }
    
    @Override
    public void streamVideo(String videoId) {
        // Lazy initialization
        if (remoteService == null) {
            System.out.println("   🔄 Initializing connection to remote server...");
            remoteService = new RemoteVideoService(serverUrl);
        }
        remoteService.streamVideo(videoId);
    }
    
    @Override
    public String getVideoInfo(String videoId) {
        // Cache video info
        if (infoCache.containsKey(videoId)) {
            System.out.println("   ⚡ Returning cached video info");
            return infoCache.get(videoId);
        }
        
        if (remoteService == null) {
            remoteService = new RemoteVideoService(serverUrl);
        }
        
        String info = remoteService.getVideoInfo(videoId);
        infoCache.put(videoId, info);
        return info;
    }
}


// ============================================================================
// REAL-WORLD EXAMPLE 4 - Logging Proxy (Monitoring)
// ============================================================================

/**
 * Logging Proxy: Adds logging without modifying original class
 */

interface BankAccount {
    void deposit(double amount);
    void withdraw(double amount);
    double getBalance();
}

class RealBankAccount implements BankAccount {
    private double balance;
    
    public RealBankAccount(double initialBalance) {
        this.balance = initialBalance;
    }
    
    @Override
    public void deposit(double amount) {
        balance += amount;
    }
    
    @Override
    public void withdraw(double amount) {
        if (balance >= amount) {
            balance -= amount;
        }
    }
    
    @Override
    public double getBalance() {
        return balance;
    }
}

class LoggingBankAccountProxy implements BankAccount {
    private RealBankAccount account;
    private String accountHolder;
    
    public LoggingBankAccountProxy(RealBankAccount account, String accountHolder) {
        this.account = account;
        this.accountHolder = accountHolder;
    }
    
    @Override
    public void deposit(double amount) {
        System.out.println("   📝 LOG: " + accountHolder + " depositing $" + amount);
        account.deposit(amount);
        System.out.println("   📝 LOG: New balance: $" + account.getBalance());
    }
    
    @Override
    public void withdraw(double amount) {
        System.out.println("   📝 LOG: " + accountHolder + " withdrawing $" + amount);
        double beforeBalance = account.getBalance();
        account.withdraw(amount);
        double afterBalance = account.getBalance();
        
        if (beforeBalance == afterBalance) {
            System.out.println("   ⚠️  LOG: Withdrawal failed - insufficient funds");
        } else {
            System.out.println("   📝 LOG: New balance: $" + afterBalance);
        }
    }
    
    @Override
    public double getBalance() {
        System.out.println("   📝 LOG: " + accountHolder + " checking balance");
        return account.getBalance();
    }
}


// ============================================================================
// DEMO - All Proxy Types
// ============================================================================

public class ProxyDesignPattern {
    public static void main(String[] args) {
        System.out.println("=".repeat(70));
        System.out.println("PROXY DESIGN PATTERN DEMO");
        System.out.println("=".repeat(70));
        
        // PROBLEM Demo
        System.out.println("\n❌ PROBLEM: Without Proxy (Direct Access)");
        System.out.println("-".repeat(70));
        System.out.println("Creating 3 expensive images...");
        // Uncomment to see problem:
        // ExpensiveImage img1 = new ExpensiveImage("photo1.jpg");
        // ExpensiveImage img2 = new ExpensiveImage("photo2.jpg");
        // ExpensiveImage img3 = new ExpensiveImage("photo3.jpg");
        System.out.println("⚠️  All 3 images loaded immediately (even if not used)!");
        
        // SOLUTION Demo
        System.out.println("\n✅ SOLUTION: With Proxy Pattern");
        System.out.println("-".repeat(70));
        
        // 1. Virtual Proxy - Lazy Loading
        System.out.println("\n1️⃣  VIRTUAL PROXY (Lazy Loading)");
        System.out.println("-".repeat(70));
        System.out.println("Creating 3 image proxies (instant, no loading)...");
        Image img1 = new ImageProxy("photo1.jpg");
        Image img2 = new ImageProxy("photo2.jpg");
        Image img3 = new ImageProxy("photo3.jpg");
        System.out.println("✅ All proxies created instantly!\n");
        
        System.out.println("Displaying photo1 (first time):");
        img1.display();
        
        System.out.println("\nDisplaying photo1 (second time):");
        img1.display();
        
        System.out.println("\nDisplaying photo2 (first time):");
        img2.display();
        
        System.out.println("\n💡 photo3 never displayed = never loaded!");
        
        // 2. Protection Proxy - Access Control
        System.out.println("\n\n2️⃣  PROTECTION PROXY (Access Control)");
        System.out.println("-".repeat(70));
        
        RealDocument doc = new RealDocument("Confidential.pdf", "Secret data");
        
        System.out.println("👤 User with VIEWER role:");
        Document viewerDoc = new ProtectedDocumentProxy(doc, Role.VIEWER);
        viewerDoc.view();
        viewerDoc.edit("New content");
        viewerDoc.delete();
        
        System.out.println("\n👤 User with EDITOR role:");
        Document editorDoc = new ProtectedDocumentProxy(doc, Role.EDITOR);
        editorDoc.view();
        editorDoc.edit("Updated content");
        editorDoc.delete();
        
        System.out.println("\n👤 User with ADMIN role:");
        Document adminDoc = new ProtectedDocumentProxy(doc, Role.ADMIN);
        adminDoc.view();
        adminDoc.edit("Admin update");
        adminDoc.delete();
        
        // 3. Cache Proxy - Performance
        System.out.println("\n\n3️⃣  CACHE PROXY (Performance Optimization)");
        System.out.println("-".repeat(70));
        
        DatabaseQuery db = new CachingDatabaseProxy();
        
        System.out.println("Query 1 (first time):");
        String result1 = db.executeQuery("SELECT * FROM users");
        System.out.println("   Result: " + result1);
        
        System.out.println("\nQuery 1 (second time - cached):");
        String result2 = db.executeQuery("SELECT * FROM users");
        System.out.println("   Result: " + result2);
        
        System.out.println("\nQuery 2 (different query):");
        String result3 = db.executeQuery("SELECT * FROM orders");
        System.out.println("   Result: " + result3);
        
        // 4. Remote Proxy - Network Calls
        System.out.println("\n\n4️⃣  REMOTE PROXY (Network Calls)");
        System.out.println("-".repeat(70));
        
        VideoService videoService = new VideoServiceProxy("https://video-server.com");
        
        System.out.println("Getting video info (first time):");
        String info1 = videoService.getVideoInfo("video123");
        System.out.println("   " + info1);
        
        System.out.println("\nGetting same video info (cached):");
        String info2 = videoService.getVideoInfo("video123");
        System.out.println("   " + info2);
        
        System.out.println("\nStreaming video:");
        videoService.streamVideo("video123");
        
        // 5. Logging Proxy - Monitoring
        System.out.println("\n\n5️⃣  LOGGING PROXY (Monitoring)");
        System.out.println("-".repeat(70));
        
        RealBankAccount realAccount = new RealBankAccount(1000.0);
        BankAccount account = new LoggingBankAccountProxy(realAccount, "John Doe");
        
        account.deposit(500.0);
        account.withdraw(200.0);
        account.withdraw(2000.0); // Insufficient funds
        double balance = account.getBalance();
        System.out.println("   Current balance: $" + balance);
        
        // Summary
        System.out.println("\n\n" + "=".repeat(70));
        System.out.println("KEY BENEFITS OF PROXY PATTERN");
        System.out.println("=".repeat(70));
        System.out.println("✅ Lazy Loading: Create expensive objects only when needed");
        System.out.println("✅ Access Control: Protect sensitive operations");
        System.out.println("✅ Caching: Improve performance by caching results");
        System.out.println("✅ Remote Access: Simplify network communication");
        System.out.println("✅ Logging: Monitor object usage without modification");
        System.out.println("✅ Open/Closed Principle: Add functionality without changing original");
        System.out.println("=".repeat(70));
    }
}
