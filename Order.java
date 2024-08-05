/**
 * Represents an order in the Gator Delivery system.
 */
public class Order {
    int orderId;
    int currentSystemTime;
    int orderValue;
   
    int deliveryTime;

    /**
     * Constructor for a new Order object with the specified attributes.
     */
    public Order(int orderId, int currentSystemTime, int orderValue, int deliveryTime) {
        this.orderId = orderId;
        this.currentSystemTime = currentSystemTime;
        this.orderValue = orderValue;
        this.deliveryTime = deliveryTime;
    }

    /**
     * Calculates the priority of the order based on its value and current system time.
     * The priority is calculated using a weighted formula.
     * @return The calculated priority of the order.
     */
    double calculatePriority() {
        // Weight assigned to the order value and delivery time
        double valueWeight = 0.3;
        double timeWeight = 0.7;
        
        // Normalize the order value to a range between 0 and 1
        double normalizedOrderValue = orderValue / 50.0;
        
        // Calculate and return the priority
        return valueWeight * normalizedOrderValue - timeWeight * currentSystemTime;
    }
}
