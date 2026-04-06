# Adapter Design Pattern

## 📖 Definition

The **Adapter Pattern** converts the interface of a class into another interface clients expect. Adapter lets classes work together that couldn't otherwise because of incompatible interfaces.

**In simple terms**: Like a power adapter that lets you plug a US device into a European outlet - it makes incompatible things work together!

**Also known as**: Wrapper Pattern

## 🎯 Core Concept

The Adapter pattern:
- Acts as a bridge between two incompatible interfaces
- Wraps an existing class with a new interface
- Allows classes to work together that couldn't otherwise
- Converts one interface to another
- Enables integration of legacy or third-party code

**Key Components**:
1. **Target Interface**: Interface that client expects
2. **Adaptee**: Existing class with incompatible interface
3. **Adapter**: Converts adaptee's interface to target interface
4. **Client**: Works with target interface

## ❌ Problem: Without Adapter Pattern

### The Incompatible Interface Problem

```java
// Our application expects this interface
interface MediaPlayer {
    void play(String filename);
    void stop();
}

// Third-party library has different interface
class LegacyMP3Player {
    void playMP3(String file) { }
    void stopMP3() { }
}

// Problem: Can't use LegacyMP3Player directly!
// MediaPlayer player = new LegacyMP3Player(); // Won't compile!
```

### Why This Is Bad:

| Problem | Description |
|---------|-------------|
| **Incompatible Interfaces** | Can't use existing code with different interface |
| **Must Modify Code** | Would need to change LegacyMP3Player (not possible if third-party) |
| **Code Duplication** | Must write wrapper code everywhere |
| **Tight Coupling** | Direct dependency on specific implementation |
| **Hard Integration** | Difficult to integrate third-party libraries |
| **Violates Open/Closed** | Must modify existing code |

## ✅ Solution: Adapter Pattern

### Step-by-Step Implementation

**Step 1: Define Target Interface**
```java
interface AudioPlayer {
    void play(String audioType, String filename);
    void stop();
}
```

**Step 2: Existing Adaptee (incompatible)**
```java
class AdvancedMP3Player {
    public void playMP3File(String filename) {
        System.out.println("Playing MP3: " + filename);
    }
    public void stopMP3File() {
        System.out.println("Stopped MP3");
    }
}
```

**Step 3: Create Adapter**
```java
class MediaAdapter implements AudioPlayer {
    private AdvancedMP3Player mp3Player;
    
    public MediaAdapter() {
        mp3Player = new AdvancedMP3Player();
    }
    
    @Override
    public void play(String audioType, String filename) {
        mp3Player.playMP3File(filename);
    }
    
    @Override
    public void stop() {
        mp3Player.stopMP3File();
    }
}
```

**Step 4: Use the Adapter**
```java
AudioPlayer player = new MediaAdapter();
player.play("mp3", "song.mp3"); // Works!
player.stop();
```

## 📊 Comparison: Before vs After

| Aspect | Without Adapter | With Adapter |
|--------|-----------------|--------------|
| **Interface compatibility** | ❌ Incompatible | ✅ Compatible |
| **Code modification** | Must modify existing code | No modification needed |
| **Third-party integration** | Difficult/impossible | Easy |
| **Coupling** | Tight | Loose |
| **Open/Closed Principle** | ❌ Violated | ✅ Followed |
| **Reusability** | Low | High |
| **Flexibility** | Low | High |

## 🌍 Real-World Use Cases

### 1. Media Player
```java
// Target: AudioPlayer
// Adaptees: MP3Player, WAVPlayer, FLACPlayer
// Adapter: MediaAdapter
AudioPlayer player = new MediaAdapter("mp3");
player.play("mp3", "song.mp3");
```

### 2. Payment Gateway Integration
```java
// Target: PaymentProcessor
// Adaptees: PayPalAPI, StripeAPI, SquareAPI
// Adapters: PayPalAdapter, StripeAdapter
PaymentProcessor processor = new PayPalAdapter(new PayPalAPI());
processor.processPayment(100.0);
```

### 3. Database Drivers
```java
// Target: Database
// Adaptees: MySQLDriver, PostgreSQLDriver, MongoDBDriver
// Adapters: MySQLAdapter, PostgreSQLAdapter
Database db = new MySQLAdapter(new LegacyMySQLDriver());
db.connect("localhost:3306");
```

### 4. Temperature Conversion
```java
// Target: TemperatureSensor (Celsius)
// Adaptee: FahrenheitSensor
// Adapter: TemperatureAdapter
TemperatureSensor sensor = new TemperatureAdapter(new FahrenheitSensor());
double celsius = sensor.getTemperature(); // Converts F to C
```

### 5. Legacy System Integration
```java
// Target: ModernAPI
// Adaptee: LegacySystem
// Adapter: LegacySystemAdapter
ModernAPI api = new LegacySystemAdapter(new LegacySystem());
api.newMethod(); // Calls legacy methods internally
```

### 6. XML to JSON Converter
```java
// Target: JSONParser
// Adaptee: XMLParser
// Adapter: XMLToJSONAdapter
JSONParser parser = new XMLToJSONAdapter(new XMLParser());
JSONObject json = parser.parse(data);
```

## 💼 Industry Examples

| Application | Adapter Use |
|-------------|-------------|
| **JDBC** | Database driver adapters for different databases |
| **Java I/O** | InputStreamReader adapts InputStream to Reader |
| **Collections** | Arrays.asList() adapts array to List |
| **Logging** | SLF4J adapters for different logging frameworks |
| **Web Services** | SOAP to REST adapters |
| **Mobile Apps** | Platform-specific API adapters |

## ✅ Advantages

1. **Interface Compatibility**: Makes incompatible interfaces work together
2. **Open/Closed Principle**: Add adapters without modifying existing code
3. **Single Responsibility**: Adapter handles interface conversion
4. **Reusability**: Reuse existing classes with different interfaces
5. **Flexibility**: Easy to integrate third-party libraries
6. **Decoupling**: Client doesn't depend on concrete adaptee
7. **Legacy Integration**: Integrate legacy code without modification

## ❌ Disadvantages

1. **Complexity**: Adds extra layer of abstraction
2. **Performance**: Slight overhead from adapter layer
3. **Many Adapters**: May need many adapters for many adaptees
4. **Maintenance**: Must maintain adapter code
5. **Over-Engineering**: Overkill for simple conversions

## 🎓 When to Use

### ✅ Use Adapter Pattern When:
- Want to use existing class with incompatible interface
- Need to integrate third-party libraries
- Want to create reusable class that cooperates with unrelated classes
- Need to use several existing subclasses with missing functionality
- Legacy code integration required
- Interface conversion is needed

### ❌ Avoid Adapter Pattern When:
- Interfaces are already compatible
- Simple conversion can be done inline
- You control both interfaces (just make them compatible)
- Performance overhead is critical
- Adding complexity isn't justified

## 🔄 Adapter vs Other Patterns

### Adapter vs Decorator
| Aspect | Adapter | Decorator |
|--------|---------|-----------|
| **Purpose** | Change interface | Add functionality |
| **Interface** | Different interface | Same interface |
| **Intent** | Make compatible | Enhance behavior |
| **Wrapping** | Single adaptee | Can stack multiple |
| **Example** | Plug adapter | Gift wrapping |

### Adapter vs Facade
| Aspect | Adapter | Facade |
|--------|---------|--------|
| **Purpose** | Interface conversion | Simplify interface |
| **Complexity** | One-to-one | One-to-many |
| **Interface** | Adapts existing | Creates new simplified |
| **Focus** | Compatibility | Simplification |
| **Example** | Power adapter | TV remote (many buttons → few) |

### Adapter vs Proxy
| Aspect | Adapter | Proxy |
|--------|---------|-------|
| **Purpose** | Interface conversion | Control access |
| **Interface** | Different | Same |
| **Functionality** | Converts calls | Adds control logic |
| **Example** | Plug adapter | Security guard |

## 💡 Implementation Tips

1. **Identify Interfaces**: Clearly identify target and adaptee interfaces
2. **Composition Over Inheritance**: Prefer object adapter over class adapter
3. **Single Responsibility**: Adapter should only convert interface
4. **Null Checks**: Handle null adaptees gracefully
5. **Documentation**: Document what's being adapted and why
6. **Consider Two-Way**: Sometimes need bidirectional adaptation
7. **Factory Pattern**: Use factory to create appropriate adapters

### Object Adapter vs Class Adapter

**Object Adapter** (Composition - Preferred):
```java
class Adapter implements Target {
    private Adaptee adaptee;
    
    public Adapter(Adaptee adaptee) {
        this.adaptee = adaptee;
    }
    
    public void request() {
        adaptee.specificRequest();
    }
}
```

**Class Adapter** (Inheritance - Less flexible):
```java
class Adapter extends Adaptee implements Target {
    public void request() {
        specificRequest();
    }
}
```

## 🧪 Practice Exercise

### Challenge: Social Media Integration

Create adapters to integrate different social media APIs.

**Requirements**:
- Target interface: `SocialMediaPoster` with `post(String message)` method
- Adaptees: `TwitterAPI`, `FacebookAPI`, `InstagramAPI` (each with different methods)
- Create adapters for each platform
- Client should work with `SocialMediaPoster` interface only

**Hints**:
1. Define `SocialMediaPoster` interface
2. Create mock API classes with different method names
3. Create adapter for each API
4. Test posting to all platforms through common interface

<details>
<summary>💡 Solution Outline</summary>

```java
interface SocialMediaPoster {
    void post(String message);
}

class TwitterAPI {
    public void tweet(String text) {
        System.out.println("Twitter: " + text);
    }
}

class FacebookAPI {
    public void publishPost(String content) {
        System.out.println("Facebook: " + content);
    }
}

class TwitterAdapter implements SocialMediaPoster {
    private TwitterAPI twitter;
    
    public TwitterAdapter(TwitterAPI twitter) {
        this.twitter = twitter;
    }
    
    public void post(String message) {
        twitter.tweet(message);
    }
}

class FacebookAdapter implements SocialMediaPoster {
    private FacebookAPI facebook;
    
    public FacebookAdapter(FacebookAPI facebook) {
        this.facebook = facebook;
    }
    
    public void post(String message) {
        facebook.publishPost(message);
    }
}

// Usage
SocialMediaPoster twitter = new TwitterAdapter(new TwitterAPI());
SocialMediaPoster facebook = new FacebookAdapter(new FacebookAPI());

twitter.post("Hello World!");
facebook.post("Hello World!");
```

</details>

## 🎯 Key Takeaways

1. **Bridge Incompatible Interfaces**: Makes different interfaces work together
2. **Wrapper Pattern**: Wraps adaptee to match target interface
3. **Open/Closed**: Add adapters without modifying existing code
4. **Third-Party Integration**: Essential for integrating external libraries
5. **Composition Preferred**: Use object adapter over class adapter
6. **Single Purpose**: Adapter only converts interface, nothing more

## 📚 Related Patterns

- **Decorator**: Similar structure but adds functionality (Adapter changes interface)
- **Facade**: Simplifies interface (Adapter makes compatible)
- **Proxy**: Same interface (Adapter changes interface)
- **Bridge**: Separates abstraction from implementation
- **Strategy**: Swaps algorithms (Adapter converts interface)

## 🔗 Java Standard Library Examples

- `java.io.InputStreamReader` - Adapts InputStream to Reader
- `java.io.OutputStreamWriter` - Adapts OutputStream to Writer
- `java.util.Arrays.asList()` - Adapts array to List
- `java.util.Collections.list()` - Adapts Enumeration to List
- `javax.xml.bind.annotation.adapters.XmlAdapter` - XML type adaptation

## 🏗️ Adapter Structure

```
Adapter Pattern Structure:

Client → Target Interface
           ↑
           |
        Adapter ----wraps---→ Adaptee
           |                    |
    implements Target      has different
    interface              interface

Example:
Client → AudioPlayer
           ↑
           |
      MediaAdapter ----→ AdvancedMP3Player
      (implements         (playMP3File,
       AudioPlayer)        stopMP3File)
```

---

## 🚀 Running the Demo

```bash
# Compile
mvn clean compile

# Run
mvn exec:java -Dexec.mainClass="com.rajan.lld.DesignPatterns.StructuralDesignPatterns.AdapterDesignPattern.AdapterDesignPattern"
```

---

**Remember**: Use Adapter Pattern when you need to make incompatible interfaces work together. It's essential for integrating third-party libraries and legacy code without modification!
