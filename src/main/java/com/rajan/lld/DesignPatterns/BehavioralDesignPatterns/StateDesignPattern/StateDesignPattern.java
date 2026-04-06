package com.rajan.lld.DesignPatterns.BehavioralDesignPatterns.StateDesignPattern;

/**
 * STATE DESIGN PATTERN - Complete Example
 * 
 * Definition: Allows an object to alter its behavior when its internal state 
 * changes. The object will appear to change its class.
 * 
 * In simple terms:
 * - Same action, different behavior based on current state
 * - Object changes behavior when state changes
 * - Eliminates complex if-else chains
 * 
 * When to use:
 * - Object behavior depends on its state
 * - Complex conditional statements based on state
 * - State transitions are well-defined
 * - Need to add new states easily
 */

// ============================================================================
// PROBLEM - Without State Pattern
// ============================================================================

/**
 * PROBLEM: Complex if-else chains based on state
 * 
 * Why is this bad?
 * - Violates Open/Closed: Adding new states requires modifying all methods
 * - Hard to maintain: State logic scattered across multiple methods
 * - Error-prone: Easy to miss state checks
 * - Not scalable: More states = more complex conditions
 * - Poor readability: Long if-else chains
 */
class BadVendingMachine {
    private static final int NO_COIN = 0;
    private static final int HAS_COIN = 1;
    private static final int SOLD = 2;
    private static final int SOLD_OUT = 3;
    
    private int currentState = NO_COIN;
    private int itemCount = 5;
    
    public void insertCoin() {
        // Complex if-else based on state
        if (currentState == NO_COIN) {
            System.out.println("Coin inserted");
            currentState = HAS_COIN;
        } else if (currentState == HAS_COIN) {
            System.out.println("Coin already inserted!");
        } else if (currentState == SOLD_OUT) {
            System.out.println("Machine is sold out!");
        } else if (currentState == SOLD) {
            System.out.println("Please wait, dispensing item...");
        }
    }
    
    public void ejectCoin() {
        if (currentState == HAS_COIN) {
            System.out.println("Coin ejected");
            currentState = NO_COIN;
        } else if (currentState == NO_COIN) {
            System.out.println("No coin to eject!");
        } else if (currentState == SOLD) {
            System.out.println("Cannot eject, already dispensing!");
        } else if (currentState == SOLD_OUT) {
            System.out.println("Cannot eject, machine sold out!");
        }
    }
    
    public void pressButton() {
        if (currentState == HAS_COIN) {
            System.out.println("Button pressed, dispensing...");
            currentState = SOLD;
            dispense();
        } else if (currentState == NO_COIN) {
            System.out.println("Insert coin first!");
        } else if (currentState == SOLD) {
            System.out.println("Already dispensing!");
        } else if (currentState == SOLD_OUT) {
            System.out.println("Machine sold out!");
        }
    }
    
    private void dispense() {
        if (currentState == SOLD) {
            System.out.println("Item dispensed!");
            itemCount--;
            if (itemCount > 0) {
                currentState = NO_COIN;
            } else {
                System.out.println("Machine sold out!");
                currentState = SOLD_OUT;
            }
        }
    }
    // Adding new states requires modifying ALL these methods!
}


// ============================================================================
// SOLUTION - State Pattern
// ============================================================================

/**
 * Step 1: State Interface
 * Defines operations that all states must implement
 */
interface VendingMachineState {
    void insertCoin(VendingMachineContext context);
    void ejectCoin(VendingMachineContext context);
    void pressButton(VendingMachineContext context);
    void dispense(VendingMachineContext context);
    String getStateName();
}

/**
 * Step 2: Context Class
 * Maintains current state and delegates operations to it
 */
class VendingMachineContext {
    private VendingMachineState currentState;
    private int itemCount;
    
    // State instances
    private final VendingMachineState noCoinState;
    private final VendingMachineState hasCoinState;
    private final VendingMachineState soldState;
    private final VendingMachineState soldOutState;
    
    public VendingMachineContext(int itemCount) {
        this.itemCount = itemCount;
        
        // Create state instances
        noCoinState = new NoCoinState();
        hasCoinState = new HasCoinState();
        soldState = new SoldState();
        soldOutState = new SoldOutState();
        
        // Set initial state
        currentState = itemCount > 0 ? noCoinState : soldOutState;
    }
    
    public void setState(VendingMachineState state) {
        this.currentState = state;
        System.out.println("🔄 State: " + state.getStateName());
    }
    
    // Delegate operations to current state
    public void insertCoin() {
        currentState.insertCoin(this);
    }
    
    public void ejectCoin() {
        currentState.ejectCoin(this);
    }
    
    public void pressButton() {
        currentState.pressButton(this);
        currentState.dispense(this);
    }
    
    // Getters
    public int getItemCount() {
        return itemCount;
    }
    
    public void releaseItem() {
        if (itemCount > 0) {
            itemCount--;
        }
    }
    
    public VendingMachineState getNoCoinState() {
        return noCoinState;
    }
    
    public VendingMachineState getHasCoinState() {
        return hasCoinState;
    }
    
    public VendingMachineState getSoldState() {
        return soldState;
    }
    
    public VendingMachineState getSoldOutState() {
        return soldOutState;
    }
}

/**
 * Step 3: Concrete States
 */
class NoCoinState implements VendingMachineState {
    @Override
    public void insertCoin(VendingMachineContext context) {
        System.out.println("💰 Coin inserted");
        context.setState(context.getHasCoinState());
    }
    
    @Override
    public void ejectCoin(VendingMachineContext context) {
        System.out.println("⚠️  No coin to eject!");
    }
    
    @Override
    public void pressButton(VendingMachineContext context) {
        System.out.println("⚠️  Insert coin first!");
    }
    
    @Override
    public void dispense(VendingMachineContext context) {
        System.out.println("⚠️  Pay first!");
    }
    
    @Override
    public String getStateName() {
        return "No Coin - Waiting for coin";
    }
}

class HasCoinState implements VendingMachineState {
    @Override
    public void insertCoin(VendingMachineContext context) {
        System.out.println("⚠️  Coin already inserted!");
    }
    
    @Override
    public void ejectCoin(VendingMachineContext context) {
        System.out.println("💰 Coin ejected");
        context.setState(context.getNoCoinState());
    }
    
    @Override
    public void pressButton(VendingMachineContext context) {
        System.out.println("🔘 Button pressed, dispensing...");
        context.setState(context.getSoldState());
    }
    
    @Override
    public void dispense(VendingMachineContext context) {
        System.out.println("⚠️  Press button first!");
    }
    
    @Override
    public String getStateName() {
        return "Has Coin - Ready to dispense";
    }
}

class SoldState implements VendingMachineState {
    @Override
    public void insertCoin(VendingMachineContext context) {
        System.out.println("⚠️  Please wait, dispensing item...");
    }
    
    @Override
    public void ejectCoin(VendingMachineContext context) {
        System.out.println("⚠️  Cannot eject, already dispensing!");
    }
    
    @Override
    public void pressButton(VendingMachineContext context) {
        System.out.println("⚠️  Already dispensing!");
    }
    
    @Override
    public void dispense(VendingMachineContext context) {
        context.releaseItem();
        System.out.println("🎁 Item dispensed! Enjoy!");
        
        if (context.getItemCount() > 0) {
            context.setState(context.getNoCoinState());
        } else {
            System.out.println("🚫 Machine sold out!");
            context.setState(context.getSoldOutState());
        }
    }
    
    @Override
    public String getStateName() {
        return "Sold - Dispensing item";
    }
}

class SoldOutState implements VendingMachineState {
    @Override
    public void insertCoin(VendingMachineContext context) {
        System.out.println("⚠️  Machine is sold out!");
    }
    
    @Override
    public void ejectCoin(VendingMachineContext context) {
        System.out.println("⚠️  No coin to eject!");
    }
    
    @Override
    public void pressButton(VendingMachineContext context) {
        System.out.println("⚠️  Machine is sold out!");
    }
    
    @Override
    public void dispense(VendingMachineContext context) {
        System.out.println("⚠️  No items to dispense!");
    }
    
    @Override
    public String getStateName() {
        return "Sold Out - No items available";
    }
}


// ============================================================================
// REAL-WORLD EXAMPLE 1 - Order Processing
// ============================================================================

interface OrderState {
    void confirmOrder(Order order);
    void shipOrder(Order order);
    void deliverOrder(Order order);
    void cancelOrder(Order order);
    String getStateName();
}

class Order {
    private OrderState currentState;
    private String orderId;
    
    private final OrderState pendingState;
    private final OrderState confirmedState;
    private final OrderState shippedState;
    private final OrderState deliveredState;
    private final OrderState cancelledState;
    
    public Order(String orderId) {
        this.orderId = orderId;
        
        pendingState = new PendingState();
        confirmedState = new ConfirmedState();
        shippedState = new ShippedState();
        deliveredState = new DeliveredState();
        cancelledState = new CancelledState();
        
        currentState = pendingState;
        System.out.println("📦 Order " + orderId + " created");
    }
    
    public void setState(OrderState state) {
        this.currentState = state;
        System.out.println("   State: " + state.getStateName());
    }
    
    public void confirm() {
        currentState.confirmOrder(this);
    }
    
    public void ship() {
        currentState.shipOrder(this);
    }
    
    public void deliver() {
        currentState.deliverOrder(this);
    }
    
    public void cancel() {
        currentState.cancelOrder(this);
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public OrderState getPendingState() { return pendingState; }
    public OrderState getConfirmedState() { return confirmedState; }
    public OrderState getShippedState() { return shippedState; }
    public OrderState getDeliveredState() { return deliveredState; }
    public OrderState getCancelledState() { return cancelledState; }
}

class PendingState implements OrderState {
    @Override
    public void confirmOrder(Order order) {
        System.out.println("✅ Order confirmed");
        order.setState(order.getConfirmedState());
    }
    
    @Override
    public void shipOrder(Order order) {
        System.out.println("⚠️  Confirm order first!");
    }
    
    @Override
    public void deliverOrder(Order order) {
        System.out.println("⚠️  Order not shipped yet!");
    }
    
    @Override
    public void cancelOrder(Order order) {
        System.out.println("❌ Order cancelled");
        order.setState(order.getCancelledState());
    }
    
    @Override
    public String getStateName() {
        return "Pending";
    }
}

class ConfirmedState implements OrderState {
    @Override
    public void confirmOrder(Order order) {
        System.out.println("⚠️  Already confirmed!");
    }
    
    @Override
    public void shipOrder(Order order) {
        System.out.println("🚚 Order shipped");
        order.setState(order.getShippedState());
    }
    
    @Override
    public void deliverOrder(Order order) {
        System.out.println("⚠️  Ship order first!");
    }
    
    @Override
    public void cancelOrder(Order order) {
        System.out.println("❌ Order cancelled");
        order.setState(order.getCancelledState());
    }
    
    @Override
    public String getStateName() {
        return "Confirmed";
    }
}

class ShippedState implements OrderState {
    @Override
    public void confirmOrder(Order order) {
        System.out.println("⚠️  Already confirmed and shipped!");
    }
    
    @Override
    public void shipOrder(Order order) {
        System.out.println("⚠️  Already shipped!");
    }
    
    @Override
    public void deliverOrder(Order order) {
        System.out.println("📬 Order delivered");
        order.setState(order.getDeliveredState());
    }
    
    @Override
    public void cancelOrder(Order order) {
        System.out.println("⚠️  Cannot cancel, already shipped!");
    }
    
    @Override
    public String getStateName() {
        return "Shipped";
    }
}

class DeliveredState implements OrderState {
    @Override
    public void confirmOrder(Order order) {
        System.out.println("⚠️  Order already delivered!");
    }
    
    @Override
    public void shipOrder(Order order) {
        System.out.println("⚠️  Order already delivered!");
    }
    
    @Override
    public void deliverOrder(Order order) {
        System.out.println("⚠️  Already delivered!");
    }
    
    @Override
    public void cancelOrder(Order order) {
        System.out.println("⚠️  Cannot cancel delivered order!");
    }
    
    @Override
    public String getStateName() {
        return "Delivered";
    }
}

class CancelledState implements OrderState {
    @Override
    public void confirmOrder(Order order) {
        System.out.println("⚠️  Order is cancelled!");
    }
    
    @Override
    public void shipOrder(Order order) {
        System.out.println("⚠️  Order is cancelled!");
    }
    
    @Override
    public void deliverOrder(Order order) {
        System.out.println("⚠️  Order is cancelled!");
    }
    
    @Override
    public void cancelOrder(Order order) {
        System.out.println("⚠️  Already cancelled!");
    }
    
    @Override
    public String getStateName() {
        return "Cancelled";
    }
}


// ============================================================================
// REAL-WORLD EXAMPLE 2 - Traffic Light
// ============================================================================

interface TrafficLightState {
    void change(TrafficLight light);
    String getColor();
}

class TrafficLight {
    private TrafficLightState currentState;
    
    private final TrafficLightState redState;
    private final TrafficLightState yellowState;
    private final TrafficLightState greenState;
    
    public TrafficLight() {
        redState = new RedLightState();
        yellowState = new YellowLightState();
        greenState = new GreenLightState();
        
        currentState = redState;
    }
    
    public void setState(TrafficLightState state) {
        this.currentState = state;
        System.out.println("🚦 Light: " + state.getColor());
    }
    
    public void change() {
        currentState.change(this);
    }
    
    public TrafficLightState getRedState() { return redState; }
    public TrafficLightState getYellowState() { return yellowState; }
    public TrafficLightState getGreenState() { return greenState; }
}

class RedLightState implements TrafficLightState {
    @Override
    public void change(TrafficLight light) {
        light.setState(light.getGreenState());
    }
    
    @Override
    public String getColor() {
        return "🔴 RED - STOP";
    }
}

class YellowLightState implements TrafficLightState {
    @Override
    public void change(TrafficLight light) {
        light.setState(light.getRedState());
    }
    
    @Override
    public String getColor() {
        return "🟡 YELLOW - SLOW DOWN";
    }
}

class GreenLightState implements TrafficLightState {
    @Override
    public void change(TrafficLight light) {
        light.setState(light.getYellowState());
    }
    
    @Override
    public String getColor() {
        return "🟢 GREEN - GO";
    }
}


// ============================================================================
// DEMO
// ============================================================================

public class StateDesignPattern {
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║          STATE DESIGN PATTERN - DEMONSTRATION             ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        
        // PROBLEM: Without State
        System.out.println("\n❌ PROBLEM: Without State Pattern");
        System.out.println("═══════════════════════════════════════════════════════════");
        BadVendingMachine badMachine = new BadVendingMachine();
        badMachine.insertCoin();
        badMachine.pressButton();
        System.out.println("\n⚠️  Issues: Complex if-else chains, hard to add new states");
        
        // SOLUTION: With State
        System.out.println("\n\n✅ SOLUTION: With State Pattern");
        System.out.println("═══════════════════════════════════════════════════════════");
        
        VendingMachineContext machine = new VendingMachineContext(2);
        
        System.out.println("\n1️⃣  First Purchase:");
        machine.insertCoin();
        machine.pressButton();
        
        System.out.println("\n2️⃣  Second Purchase:");
        machine.insertCoin();
        machine.pressButton();
        
        System.out.println("\n3️⃣  Try after sold out:");
        machine.insertCoin();
        
        System.out.println("\n4️⃣  Test coin ejection:");
        VendingMachineContext machine2 = new VendingMachineContext(3);
        machine2.insertCoin();
        machine2.ejectCoin();
        
        // EXAMPLE 1: Order Processing
        System.out.println("\n\n📦 EXAMPLE 1: Order Processing System");
        System.out.println("═══════════════════════════════════════════════════════════");
        
        Order order1 = new Order("ORD-001");
        System.out.println("\n▶️  Normal flow:");
        order1.confirm();
        order1.ship();
        order1.deliver();
        
        System.out.println("\n▶️  Try invalid operations:");
        order1.cancel(); // Can't cancel delivered order
        
        System.out.println("\n▶️  Cancellation flow:");
        Order order2 = new Order("ORD-002");
        order2.confirm();
        order2.cancel(); // Cancel before shipping
        order2.ship(); // Can't ship cancelled order
        
        // EXAMPLE 2: Traffic Light
        System.out.println("\n\n🚦 EXAMPLE 2: Traffic Light System");
        System.out.println("═══════════════════════════════════════════════════════════");
        
        TrafficLight light = new TrafficLight();
        System.out.println("Initial state:");
        
        for (int i = 0; i < 6; i++) {
            try {
                Thread.sleep(1000);
                light.change();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        // KEY BENEFITS
        System.out.println("\n\n🎯 KEY BENEFITS OF STATE PATTERN");
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("✓ Eliminates complex if-else chains based on state");
        System.out.println("✓ Each state encapsulates its own behavior");
        System.out.println("✓ Easy to add new states without modifying existing code");
        System.out.println("✓ Follows Single Responsibility Principle");
        System.out.println("✓ Follows Open/Closed Principle");
        System.out.println("✓ State transitions are explicit and clear");
        System.out.println("✓ Makes state-specific behavior easy to understand");
    }
}
