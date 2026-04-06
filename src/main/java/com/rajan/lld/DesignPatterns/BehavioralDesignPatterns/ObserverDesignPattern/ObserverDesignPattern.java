package com.rajan.lld.DesignPatterns.BehavioralDesignPatterns.ObserverDesignPattern;

import java.util.ArrayList;
import java.util.List;

/**
 * OBSERVER DESIGN PATTERN - Complete Example
 * 
 * Definition: Defines a one-to-many dependency between objects so that when 
 * one object changes state, all its dependents are notified and updated automatically.
 * 
 * In simple terms:
 * - Subscribe to notifications
 * - When something happens, everyone gets notified
 * - Publish-Subscribe mechanism
 * 
 * When to use:
 * - One object change affects multiple objects
 * - Need loose coupling between objects
 * - Dynamic subscription/unsubscription needed
 * - Broadcast communication required
 */

// ============================================================================
// PROBLEM - Without Observer Pattern
// ============================================================================

/**
 * PROBLEM: Tight coupling and hard-coded dependencies
 * 
 * Why is this bad?
 * - Tight coupling: Subject knows about all concrete observers
 * - Violates Open/Closed: Can't add new observers without modifying subject
 * - Hard to maintain: All notification logic in one place
 * - No flexibility: Can't dynamically add/remove observers
 * - Code duplication: Similar notification code repeated
 */
class BadWeatherStation {
    private float temperature;
    private float humidity;
    
    // Hard-coded dependencies - tightly coupled
    private PhoneDisplay phoneDisplay;
    private TVDisplay tvDisplay;
    private WebDisplay webDisplay;
    
    public BadWeatherStation() {
        // Must create all displays - can't add new ones dynamically
        this.phoneDisplay = new PhoneDisplay();
        this.tvDisplay = new TVDisplay();
        this.webDisplay = new WebDisplay();
    }
    
    public void setMeasurements(float temperature, float humidity) {
        this.temperature = temperature;
        this.humidity = humidity;
        
        // Hard-coded notifications - violates Open/Closed Principle
        phoneDisplay.update(temperature, humidity);
        tvDisplay.update(temperature, humidity);
        webDisplay.update(temperature, humidity);
        // What if we want to add a Desktop app? We'd have to modify this class!
    }
}

class PhoneDisplay {
    public void update(float temp, float humidity) {
        System.out.println("📱 Phone: " + temp + "°C, " + humidity + "%");
    }
}

class TVDisplay {
    public void update(float temp, float humidity) {
        System.out.println("📺 TV: " + temp + "°C, " + humidity + "%");
    }
}

class WebDisplay {
    public void update(float temp, float humidity) {
        System.out.println("🌐 Web: " + temp + "°C, " + humidity + "%");
    }
}


// ============================================================================
// SOLUTION - Observer Pattern
// ============================================================================

/**
 * Step 1: Observer Interface
 * All observers must implement this interface
 */
interface Observer {
    void update(float temperature, float humidity, float pressure);
    String getObserverName();
}

/**
 * Step 2: Subject Interface
 * Defines methods for attaching, detaching, and notifying observers
 */
interface Subject {
    void registerObserver(Observer observer);
    void removeObserver(Observer observer);
    void notifyObservers();
}

/**
 * Step 3: Concrete Subject - Weather Station
 */
class WeatherStation implements Subject {
    private List<Observer> observers;
    private float temperature;
    private float humidity;
    private float pressure;
    
    public WeatherStation() {
        this.observers = new ArrayList<>();
    }
    
    @Override
    public void registerObserver(Observer observer) {
        observers.add(observer);
        System.out.println("✅ " + observer.getObserverName() + " registered");
    }
    
    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
        System.out.println("❌ " + observer.getObserverName() + " removed");
    }
    
    @Override
    public void notifyObservers() {
        System.out.println("\n🔔 Notifying " + observers.size() + " observers...");
        for (Observer observer : observers) {
            observer.update(temperature, humidity, pressure);
        }
    }
    
    public void setMeasurements(float temperature, float humidity, float pressure) {
        System.out.println("\n🌡️  Weather Station: New measurements received");
        this.temperature = temperature;
        this.humidity = humidity;
        this.pressure = pressure;
        notifyObservers();
    }
}

/**
 * Step 4: Concrete Observers
 */
class MobileDisplay implements Observer {
    private String deviceName;
    
    public MobileDisplay(String deviceName) {
        this.deviceName = deviceName;
    }
    
    @Override
    public void update(float temperature, float humidity, float pressure) {
        System.out.println("📱 " + deviceName + " Display: " + 
                         temperature + "°C, " + humidity + "% humidity, " + 
                         pressure + " hPa");
    }
    
    @Override
    public String getObserverName() {
        return "Mobile(" + deviceName + ")";
    }
}

class DesktopDisplay implements Observer {
    private String location;
    
    public DesktopDisplay(String location) {
        this.location = location;
    }
    
    @Override
    public void update(float temperature, float humidity, float pressure) {
        System.out.println("🖥️  Desktop at " + location + ": " + 
                         temperature + "°C, " + humidity + "% humidity, " + 
                         pressure + " hPa");
    }
    
    @Override
    public String getObserverName() {
        return "Desktop(" + location + ")";
    }
}

class StatisticsDisplay implements Observer {
    private float maxTemp = Float.MIN_VALUE;
    private float minTemp = Float.MAX_VALUE;
    private float tempSum = 0.0f;
    private int numReadings = 0;
    
    @Override
    public void update(float temperature, float humidity, float pressure) {
        tempSum += temperature;
        numReadings++;
        
        if (temperature > maxTemp) maxTemp = temperature;
        if (temperature < minTemp) minTemp = temperature;
        
        System.out.println("📊 Statistics: Avg=" + (tempSum / numReadings) + 
                         "°C, Max=" + maxTemp + "°C, Min=" + minTemp + "°C");
    }
    
    @Override
    public String getObserverName() {
        return "Statistics Display";
    }
}


// ============================================================================
// REAL-WORLD EXAMPLE 1 - YouTube Channel Subscription
// ============================================================================

interface Subscriber {
    void notifyVideo(String channelName, String videoTitle);
    String getSubscriberInfo();
}

interface Channel {
    void subscribe(Subscriber subscriber);
    void unsubscribe(Subscriber subscriber);
    void notifySubscribers(String videoTitle);
}

class YouTubeChannel implements Channel {
    private String channelName;
    private List<Subscriber> subscribers;
    
    public YouTubeChannel(String channelName) {
        this.channelName = channelName;
        this.subscribers = new ArrayList<>();
    }
    
    @Override
    public void subscribe(Subscriber subscriber) {
        subscribers.add(subscriber);
        System.out.println("✅ " + subscriber.getSubscriberInfo() + 
                         " subscribed to " + channelName);
    }
    
    @Override
    public void unsubscribe(Subscriber subscriber) {
        subscribers.remove(subscriber);
        System.out.println("❌ " + subscriber.getSubscriberInfo() + 
                         " unsubscribed from " + channelName);
    }
    
    @Override
    public void notifySubscribers(String videoTitle) {
        System.out.println("\n🔔 Notifying " + subscribers.size() + " subscribers...");
        for (Subscriber subscriber : subscribers) {
            subscriber.notifyVideo(channelName, videoTitle);
        }
    }
    
    public void uploadVideo(String videoTitle) {
        System.out.println("\n🎥 " + channelName + " uploaded: '" + videoTitle + "'");
        notifySubscribers(videoTitle);
    }
}

class EmailSubscriber implements Subscriber {
    private String email;
    
    public EmailSubscriber(String email) {
        this.email = email;
    }
    
    @Override
    public void notifyVideo(String channelName, String videoTitle) {
        System.out.println("📧 Email to " + email + ": New video '" + 
                         videoTitle + "' from " + channelName);
    }
    
    @Override
    public String getSubscriberInfo() {
        return "Email(" + email + ")";
    }
}

class AppSubscriber implements Subscriber {
    private String username;
    
    public AppSubscriber(String username) {
        this.username = username;
    }
    
    @Override
    public void notifyVideo(String channelName, String videoTitle) {
        System.out.println("📱 App notification to " + username + ": " + 
                         channelName + " posted '" + videoTitle + "'");
    }
    
    @Override
    public String getSubscriberInfo() {
        return "App(" + username + ")";
    }
}


// ============================================================================
// REAL-WORLD EXAMPLE 2 - Stock Market
// ============================================================================

interface StockObserver {
    void priceUpdate(String symbol, double price, double change);
    String getObserverName();
}

class Stock {
    private String symbol;
    private double price;
    private List<StockObserver> observers;
    
    public Stock(String symbol, double initialPrice) {
        this.symbol = symbol;
        this.price = initialPrice;
        this.observers = new ArrayList<>();
    }
    
    public void addObserver(StockObserver observer) {
        observers.add(observer);
        System.out.println("✅ " + observer.getObserverName() + 
                         " watching " + symbol);
    }
    
    public void removeObserver(StockObserver observer) {
        observers.remove(observer);
        System.out.println("❌ " + observer.getObserverName() + 
                         " stopped watching " + symbol);
    }
    
    public void setPrice(double newPrice) {
        double change = newPrice - this.price;
        this.price = newPrice;
        notifyObservers(change);
    }
    
    private void notifyObservers(double change) {
        System.out.println("\n📈 " + symbol + " price changed to $" + price);
        for (StockObserver observer : observers) {
            observer.priceUpdate(symbol, price, change);
        }
    }
}

class TradingApp implements StockObserver {
    private String appName;
    
    public TradingApp(String appName) {
        this.appName = appName;
    }
    
    @Override
    public void priceUpdate(String symbol, double price, double change) {
        String trend = change > 0 ? "📈 UP" : "📉 DOWN";
        System.out.println("💼 " + appName + ": " + symbol + " " + trend + 
                         " $" + String.format("%.2f", Math.abs(change)));
    }
    
    @Override
    public String getObserverName() {
        return appName;
    }
}

class InvestorPortfolio implements StockObserver {
    private String investorName;
    private double totalValue;
    
    public InvestorPortfolio(String investorName) {
        this.investorName = investorName;
        this.totalValue = 0;
    }
    
    @Override
    public void priceUpdate(String symbol, double price, double change) {
        totalValue += change * 100; // Assume 100 shares
        System.out.println("👤 " + investorName + "'s portfolio: " + 
                         (change > 0 ? "Gained" : "Lost") + " $" + 
                         String.format("%.2f", Math.abs(change * 100)));
    }
    
    @Override
    public String getObserverName() {
        return "Portfolio(" + investorName + ")";
    }
}


// ============================================================================
// REAL-WORLD EXAMPLE 3 - News Agency
// ============================================================================

interface NewsSubscriber {
    void receiveNews(String category, String headline);
}

class NewsAgency {
    private List<NewsSubscriber> subscribers;
    
    public NewsAgency() {
        this.subscribers = new ArrayList<>();
    }
    
    public void subscribe(NewsSubscriber subscriber) {
        subscribers.add(subscriber);
    }
    
    public void publishNews(String category, String headline) {
        System.out.println("\n📰 Breaking News [" + category + "]: " + headline);
        for (NewsSubscriber subscriber : subscribers) {
            subscriber.receiveNews(category, headline);
        }
    }
}

class NewsReader implements NewsSubscriber {
    private String name;
    private String interestedCategory;
    
    public NewsReader(String name, String interestedCategory) {
        this.name = name;
        this.interestedCategory = interestedCategory;
    }
    
    @Override
    public void receiveNews(String category, String headline) {
        if (category.equals(interestedCategory)) {
            System.out.println("👤 " + name + " received: " + headline);
        }
    }
}


// ============================================================================
// DEMO
// ============================================================================

public class ObserverDesignPattern {
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║         OBSERVER DESIGN PATTERN - DEMONSTRATION           ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        
        // PROBLEM: Without Observer
        System.out.println("\n❌ PROBLEM: Without Observer Pattern");
        System.out.println("═══════════════════════════════════════════════════════════");
        BadWeatherStation badStation = new BadWeatherStation();
        badStation.setMeasurements(25.5f, 65.0f);
        System.out.println("\n⚠️  Issues: Tight coupling, can't add new displays dynamically");
        
        // SOLUTION: With Observer
        System.out.println("\n\n✅ SOLUTION: With Observer Pattern");
        System.out.println("═══════════════════════════════════════════════════════════");
        
        WeatherStation station = new WeatherStation();
        
        // Create observers
        Observer mobileDisplay = new MobileDisplay("iPhone 15");
        Observer desktopDisplay = new DesktopDisplay("Office");
        Observer statsDisplay = new StatisticsDisplay();
        
        // Register observers
        station.registerObserver(mobileDisplay);
        station.registerObserver(desktopDisplay);
        station.registerObserver(statsDisplay);
        
        // Update weather - all observers notified
        station.setMeasurements(25.5f, 65.0f, 1013.1f);
        station.setMeasurements(27.2f, 70.0f, 1012.5f);
        
        // Remove one observer
        System.out.println();
        station.removeObserver(mobileDisplay);
        
        // Update again - only remaining observers notified
        station.setMeasurements(23.8f, 60.0f, 1014.0f);
        
        // EXAMPLE 1: YouTube Channel
        System.out.println("\n\n🎥 EXAMPLE 1: YouTube Channel Subscription");
        System.out.println("═══════════════════════════════════════════════════════════");
        
        YouTubeChannel channel = new YouTubeChannel("CodeMaster");
        
        Subscriber emailSub = new EmailSubscriber("john@email.com");
        Subscriber appSub1 = new AppSubscriber("jane_dev");
        Subscriber appSub2 = new AppSubscriber("bob_coder");
        
        channel.subscribe(emailSub);
        channel.subscribe(appSub1);
        channel.subscribe(appSub2);
        
        channel.uploadVideo("Design Patterns Explained");
        
        System.out.println();
        channel.unsubscribe(emailSub);
        
        channel.uploadVideo("Advanced Java Concepts");
        
        // EXAMPLE 2: Stock Market
        System.out.println("\n\n📈 EXAMPLE 2: Stock Market Monitoring");
        System.out.println("═══════════════════════════════════════════════════════════");
        
        Stock appleStock = new Stock("AAPL", 150.00);
        
        StockObserver robinhood = new TradingApp("Robinhood");
        StockObserver etrade = new TradingApp("E*TRADE");
        StockObserver portfolio = new InvestorPortfolio("John Doe");
        
        appleStock.addObserver(robinhood);
        appleStock.addObserver(etrade);
        appleStock.addObserver(portfolio);
        
        appleStock.setPrice(155.50);
        appleStock.setPrice(148.75);
        
        System.out.println();
        appleStock.removeObserver(etrade);
        
        appleStock.setPrice(152.30);
        
        // EXAMPLE 3: News Agency
        System.out.println("\n\n📰 EXAMPLE 3: News Agency");
        System.out.println("═══════════════════════════════════════════════════════════");
        
        NewsAgency agency = new NewsAgency();
        
        NewsSubscriber techReader = new NewsReader("Alice", "Technology");
        NewsSubscriber sportsReader = new NewsReader("Bob", "Sports");
        NewsSubscriber allReader = new NewsReader("Charlie", "Technology");
        
        agency.subscribe(techReader);
        agency.subscribe(sportsReader);
        agency.subscribe(allReader);
        
        agency.publishNews("Technology", "AI reaches new milestone");
        agency.publishNews("Sports", "Team wins championship");
        
        // KEY BENEFITS
        System.out.println("\n\n🎯 KEY BENEFITS OF OBSERVER PATTERN");
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("✓ Loose coupling between subject and observers");
        System.out.println("✓ Dynamic subscription/unsubscription at runtime");
        System.out.println("✓ Broadcast communication to multiple objects");
        System.out.println("✓ Follows Open/Closed Principle");
        System.out.println("✓ Subject doesn't need to know concrete observer classes");
        System.out.println("✓ Easy to add new observers without modifying subject");
    }
}
