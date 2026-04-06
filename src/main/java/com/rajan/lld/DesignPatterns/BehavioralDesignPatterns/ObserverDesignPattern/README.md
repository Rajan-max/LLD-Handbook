# Observer Design Pattern

## 📖 Definition

The **Observer Pattern** defines a one-to-many dependency between objects so that when one object changes state, all its dependents are notified and updated automatically.

**In simple terms**: Subscribe to notifications - when something happens, everyone who subscribed gets notified automatically!

**Also known as**: Publish-Subscribe (Pub-Sub) Pattern, Event-Listener Pattern

## 🎯 Core Concept

The Observer pattern establishes a relationship where:
- **Subject (Publisher)**: The object being watched, maintains a list of observers
- **Observer (Subscriber)**: Objects that want to be notified of changes
- When the subject's state changes, it automatically notifies all observers

**Key Components**:
1. **Subject Interface**: Methods to attach, detach, and notify observers
2. **Concrete Subject**: Implements subject interface, stores state
3. **Observer Interface**: Update method that subjects call
4. **Concrete Observers**: Implement observer interface, react to updates

## ❌ Problem: Without Observer Pattern

### The Tight Coupling Problem

```java
class BadWeatherStation {
    private float temperature;
    
    // Hard-coded dependencies - tightly coupled!
    private PhoneDisplay phoneDisplay;
    private TVDisplay tvDisplay;
    private WebDisplay webDisplay;
    
    public BadWeatherStation() {
        // Must create all displays upfront
        this.phoneDisplay = new PhoneDisplay();
        this.tvDisplay = new TVDisplay();
        this.webDisplay = new WebDisplay();
    }
    
    public void setMeasurements(float temperature) {
        this.temperature = temperature;
        
        // Hard-coded notifications
        phoneDisplay.update(temperature);
        tvDisplay.update(temperature);
        webDisplay.update(temperature);
        // Want to add Desktop display? Must modify this class!
    }
}
```

### Why This Is Bad:

| Problem | Description |
|---------|-------------|
| **Tight Coupling** | Subject knows about all concrete observer classes |
| **Violates Open/Closed** | Can't add new observers without modifying subject |
| **No Dynamic Subscription** | Can't add/remove observers at runtime |
| **Hard to Maintain** | All notification logic in one place |
| **Not Scalable** | Adding observers requires code changes |
| **Testing Difficulty** | Can't test subject without all observers |

## ✅ Solution: Observer Pattern

### Step-by-Step Implementation

**Step 1: Define Observer Interface**
```java
interface Observer {
    void update(float temperature, float humidity, float pressure);
    String getObserverName();
}
```

**Step 2: Define Subject Interface**
```java
interface Subject {
    void registerObserver(Observer observer);
    void removeObserver(Observer observer);
    void notifyObservers();
}
```

**Step 3: Create Concrete Subject**
```java
class WeatherStation implements Subject {
    private List<Observer> observers;
    private float temperature;
    
    public WeatherStation() {
        this.observers = new ArrayList<>();
    }
    
    @Override
    public void registerObserver(Observer observer) {
        observers.add(observer);
    }
    
    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }
    
    @Override
    public void notifyObservers() {
        for (Observer observer : observers) {
            observer.update(temperature, humidity, pressure);
        }
    }
    
    public void setMeasurements(float temp, float humidity, float pressure) {
        this.temperature = temp;
        // ... set other values
        notifyObservers(); // Notify all observers
    }
}
```

**Step 4: Create Concrete Observers**
```java
class MobileDisplay implements Observer {
    @Override
    public void update(float temp, float humidity, float pressure) {
        System.out.println("Mobile: " + temp + "°C");
    }
    
    @Override
    public String getObserverName() {
        return "Mobile Display";
    }
}

class DesktopDisplay implements Observer {
    @Override
    public void update(float temp, float humidity, float pressure) {
        System.out.println("Desktop: " + temp + "°C");
    }
    
    @Override
    public String getObserverName() {
        return "Desktop Display";
    }
}
```

**Step 5: Use the Pattern**
```java
WeatherStation station = new WeatherStation();

Observer mobile = new MobileDisplay();
Observer desktop = new DesktopDisplay();

// Subscribe
station.registerObserver(mobile);
station.registerObserver(desktop);

// Update - both observers notified automatically
station.setMeasurements(25.5f, 65.0f, 1013.1f);

// Unsubscribe
station.removeObserver(mobile);

// Update - only desktop notified
station.setMeasurements(27.2f, 70.0f, 1012.5f);
```

## 📊 Comparison: Before vs After

| Aspect | Without Observer | With Observer |
|--------|------------------|---------------|
| **Coupling** | Tight (subject knows all observers) | Loose (subject knows only interface) |
| **Adding observers** | Modify subject class | Just register new observer |
| **Dynamic subscription** | ❌ Not possible | ✅ Subscribe/unsubscribe anytime |
| **Scalability** | Poor | Excellent |
| **Open/Closed Principle** | ❌ Violated | ✅ Followed |
| **Testing** | Hard (need all observers) | Easy (test independently) |
| **Maintainability** | Low | High |

## 🌍 Real-World Use Cases

### 1. Weather Monitoring System
```java
// Weather station notifies multiple displays
- MobileDisplay
- DesktopDisplay
- StatisticsDisplay
- ForecastDisplay
```

### 2. Social Media Notifications
```java
// User posts update, followers get notified
- EmailNotification
- PushNotification
- SMSNotification
- InAppNotification
```

### 3. Stock Market Monitoring
```java
// Stock price changes, apps get updated
- TradingApp
- InvestorPortfolio
- PriceAlertSystem
- NewsAggregator
```

### 4. YouTube Channel Subscriptions
```java
// Channel uploads video, subscribers notified
- EmailSubscriber
- AppSubscriber
- BrowserSubscriber
- SlackSubscriber
```

### 5. Event Management System
```java
// Event occurs, registered listeners notified
- LoggingListener
- EmailListener
- AnalyticsListener
- AuditListener
```

### 6. Newsletter Subscription
```java
// Newsletter published, subscribers receive it
- EmailSubscriber
- RSSFeedSubscriber
- MobileAppSubscriber
```

## 💼 Industry Examples

| Application | Observer Use |
|-------------|--------------|
| **Social Media** | Post updates, likes, comments, follows |
| **E-commerce** | Price drops, stock availability, order status |
| **News Apps** | Breaking news, category updates |
| **Trading Platforms** | Stock prices, market alerts, portfolio changes |
| **IoT Systems** | Sensor data, device status, alerts |
| **GUI Frameworks** | Button clicks, form submissions, events |

## ✅ Advantages

1. **Loose Coupling**: Subject and observers are loosely coupled
2. **Dynamic Relationships**: Add/remove observers at runtime
3. **Broadcast Communication**: One-to-many notification
4. **Open/Closed Principle**: Add observers without modifying subject
5. **Reusability**: Observers can be reused with different subjects
6. **Flexibility**: Different observers can react differently
7. **Separation of Concerns**: Subject focuses on state, observers on reactions

## ❌ Disadvantages

1. **Memory Leaks**: Forgetting to unsubscribe can cause memory leaks
2. **Unexpected Updates**: Observers may be notified in unexpected order
3. **Performance**: Many observers can slow down notifications
4. **Complexity**: Can be overkill for simple scenarios
5. **Debugging Difficulty**: Hard to track notification flow
6. **Update Overhead**: All observers notified even if not interested

## 🎓 When to Use

### ✅ Use Observer Pattern When:
- One object change should trigger updates in multiple objects
- You need loose coupling between objects
- Dynamic subscription/unsubscription is needed
- Broadcast communication is required
- You want to follow Open/Closed Principle
- Objects need to be notified without knowing who they are

### ❌ Avoid Observer Pattern When:
- Simple one-to-one relationships are sufficient
- Observers need to be notified in specific order
- Performance is critical and many observers exist
- The overhead of maintaining observer list isn't justified
- Direct method calls are simpler and sufficient

## 🔄 Observer vs Other Patterns

### Observer vs Mediator
| Aspect | Observer | Mediator |
|--------|----------|----------|
| **Communication** | One-to-many (broadcast) | Many-to-many (centralized) |
| **Coupling** | Subject-Observer | All through mediator |
| **Purpose** | Notify dependents | Coordinate interactions |
| **Example** | Newsletter subscription | Chat room |

### Observer vs Pub-Sub
| Aspect | Observer | Pub-Sub |
|--------|----------|---------|
| **Coupling** | Subject knows observers | Publisher doesn't know subscribers |
| **Mediator** | Direct connection | Message broker in between |
| **Synchronous** | Usually yes | Can be async |
| **Example** | GUI events | Message queues (Kafka, RabbitMQ) |

### Observer vs Event Listener
| Aspect | Observer | Event Listener |
|--------|----------|----------------|
| **Concept** | Same pattern | Same pattern |
| **Terminology** | Subject/Observer | Event Source/Listener |
| **Usage** | General OOP | GUI frameworks |
| **Example** | Weather station | Button click handler |

## 💡 Implementation Tips

1. **Prevent Memory Leaks**: Always unsubscribe when done
2. **Thread Safety**: Use synchronized collections for multi-threading
3. **Weak References**: Consider weak references for observers
4. **Update Granularity**: Pass only necessary data in update()
5. **Error Handling**: Handle exceptions in observer updates
6. **Notification Order**: Document if order matters
7. **Push vs Pull**: Decide if observers pull data or receive it

### Push Model vs Pull Model

**Push Model** (Pass data in update):
```java
interface Observer {
    void update(float temp, float humidity); // Data pushed
}
```

**Pull Model** (Observers pull data):
```java
interface Observer {
    void update(Subject subject); // Observer pulls data from subject
}
```

## 🧪 Practice Exercise

### Challenge: News Notification System

Create a news notification system where users can subscribe to different news categories.

**Requirements**:
- News agency publishes articles in categories (Tech, Sports, Politics)
- Users can subscribe to specific categories
- When article is published, only interested users are notified
- Users can subscribe/unsubscribe dynamically
- Support multiple notification channels (Email, SMS, Push)

**Hints**:
1. Create `NewsObserver` interface with `notify(String category, String article)` method
2. Create `NewsAgency` as subject with category-based subscription
3. Implement concrete observers: `EmailObserver`, `SMSObserver`, `PushObserver`
4. Each observer should filter by interested categories
5. Test subscribing, unsubscribing, and category filtering

<details>
<summary>💡 Solution Outline</summary>

```java
interface NewsObserver {
    void notify(String category, String headline);
    boolean isInterestedIn(String category);
}

class NewsAgency {
    private Map<String, List<NewsObserver>> categoryObservers;
    
    public void subscribe(String category, NewsObserver observer) {
        categoryObservers.get(category).add(observer);
    }
    
    public void publishArticle(String category, String headline) {
        for (NewsObserver observer : categoryObservers.get(category)) {
            if (observer.isInterestedIn(category)) {
                observer.notify(category, headline);
            }
        }
    }
}

class EmailObserver implements NewsObserver {
    private String email;
    private Set<String> interestedCategories;
    
    public boolean isInterestedIn(String category) {
        return interestedCategories.contains(category);
    }
    
    public void notify(String category, String headline) {
        System.out.println("Email to " + email + ": " + headline);
    }
}
```

</details>

## 🎯 Key Takeaways

1. **One-to-Many**: One subject notifies many observers
2. **Loose Coupling**: Subject doesn't know concrete observer classes
3. **Dynamic Subscription**: Add/remove observers at runtime
4. **Broadcast**: All observers notified automatically
5. **Open/Closed**: Add observers without modifying subject
6. **Event-Driven**: Perfect for event-driven architectures

## 📚 Related Patterns

- **Mediator**: Centralizes complex communications
- **Singleton**: Subject can be singleton
- **Factory**: Create observers using factory
- **Command**: Encapsulate observer notifications as commands
- **Chain of Responsibility**: Alternative for event handling

## 🔗 Java Standard Library Examples

- `java.util.Observer` and `java.util.Observable` (deprecated in Java 9)
- `java.beans.PropertyChangeListener` - JavaBeans property changes
- `javax.swing.event.*` - Swing GUI event listeners
- `java.awt.event.*` - AWT event listeners
- `javax.servlet.http.HttpSessionListener` - Session lifecycle events
- Event listeners in Android (`OnClickListener`, `OnTouchListener`)

## 🏗️ Modern Alternatives

### Java 9+ Flow API
```java
// Reactive Streams implementation
Flow.Publisher<String> publisher = ...;
Flow.Subscriber<String> subscriber = ...;
publisher.subscribe(subscriber);
```

### RxJava (Reactive Extensions)
```java
Observable<String> observable = Observable.just("Hello", "World");
observable.subscribe(item -> System.out.println(item));
```

### Spring Events
```java
@EventListener
public void handleEvent(CustomEvent event) {
    // Handle event
}
```

---

## 🚀 Running the Demo

```bash
# Compile
mvn clean compile

# Run
mvn exec:java -Dexec.mainClass="com.rajan.lld.DesignPatterns.BehavioralDesignPatterns.ObserverDesignPattern.ObserverDesignPattern"
```

---

**Remember**: Use Observer Pattern when you need to maintain consistency between related objects without making them tightly coupled. It's the foundation of event-driven programming and reactive systems!
