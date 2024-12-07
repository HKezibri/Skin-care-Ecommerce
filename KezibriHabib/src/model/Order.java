package model;

//@Table(name = "e_Orders")
import java.util.ArrayList;
import java.time.LocalDate;
import java.util.List;
import util.OrderStatus;

public class Order {
    private int orderId; // auto-generated
    private int clientId;
    private List<OrderItem> items;
    private LocalDate orderDate;
    private OrderStatus status;
    private double totalAmount;

    // Constructors
    public Order(int clientId, OrderStatus status, List<OrderItem> items) {
        this.clientId = clientId;
        this.status = status != null ? status : OrderStatus.in_progress; // Default status to Pending if null
        this.items = items != null ? items : new ArrayList<>();  // Ensure items list is not null
        this.totalAmount = calculateTotalAmount();
    }
    public Order(int order_id, int clientId, OrderStatus status, List<OrderItem> items) {
    	this.orderId = order_id;
        this.clientId = clientId;
        this.status = status != null ? status : OrderStatus.in_progress; // Default status to Pending if null
        this.items = items != null ? items : new ArrayList<>();  // Ensure items list is not null
        this.totalAmount = calculateTotalAmount();
    }
    public Order(int order_id, int clientId, OrderStatus status) {
    	this.orderId = order_id;
        this.clientId = clientId;
        this.status = status != null ? status : OrderStatus.in_progress; // Default status to Pending if null
        this.totalAmount = calculateTotalAmount();
    }
    

    // Getters and Setters
    public int getOrderId() {
    	return orderId;
    }
    
    public void setOrderId(int orderId) {
    	this.orderId = orderId;
    }

    public int getClientId() { 
    	return clientId;
    }
    
    public void setClientId(int clientId) { 
    	this.clientId = clientId;
    }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) {
        if (items == null) {
            this.items = new ArrayList<>();
        } else {
            this.items = items;
        }

        // Recalculate the total amount after setting the items
        this.totalAmount = calculateTotalAmount();
    }

   

    public LocalDate getOrderDate() {  return orderDate;   }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    
    
    // Getter for totalAmount
    public double getTotalAmount() { return totalAmount; }
    // Setter for totalAmount
    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    // Calculate total amount based on order items
    private double calculateTotalAmount() {
        double total = 0.0;
        for (OrderItem item : items) {
            total += item.TotalPrice();
        }
        return total;
    }
    
    
    @Override
    public String toString() {
        return "Order{" +
               "orderId=" + orderId +
               ", orderDate=" + orderDate +
               ", status=" + status +
               ", totalAmount=" + totalAmount +
               '}';
    }
}
