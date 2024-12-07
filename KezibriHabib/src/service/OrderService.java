package service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.Order;
import model.OrderItem;
import mySQLdb.DBConnect;
import util.OrderStatus;
import java.time.LocalDate;


public class OrderService {

	public void createOrder(Order order) throws SQLException {
	    // Validate order and items
	    if (order.getItems() == null || order.getItems().isEmpty()) {
	        throw new IllegalArgumentException("Order must have at least one item.");
	    }
	    if (order.getClientId() <= 0) {
	        throw new IllegalArgumentException("Invalid client ID: " + order.getClientId());
	    }
	    for (OrderItem item : order.getItems()) {
	        if (item.getProductId() <= 0 || item.getQuantity() <= 0 || item.getItemPrice() <= 0) {
	            throw new IllegalArgumentException("Invalid order item: " + item);
	        }
	    }

	    String insertOrderQuery = "INSERT INTO e_Orders (client_id, totalPrice, status) VALUES (?, ?, ?)";
	    String insertOrderItemQuery = "INSERT INTO e_OrderItem (order_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";

	    try (Connection conn = DBConnect.getConnection()) {
	        conn.setAutoCommit(false); // Start transaction
	        conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE); // Optional

	        try (PreparedStatement orderStmt = conn.prepareStatement(insertOrderQuery, Statement.RETURN_GENERATED_KEYS)) {
	            // Insert order details
	            orderStmt.setInt(1, order.getClientId());
	            orderStmt.setDouble(2, order.getTotalAmount());
	            orderStmt.setString(3, order.getStatus().name());
	            int rowsAffected = orderStmt.executeUpdate();

	            if (rowsAffected == 0) {
	                throw new SQLException("Failed to create order, no rows affected.");
	            }

	            // Retrieve the generated order ID
	            try (ResultSet generatedKeys = orderStmt.getGeneratedKeys()) {
	                if (generatedKeys.next()) {
	                    int orderId = generatedKeys.getInt(1);
	                    order.setOrderId(orderId);

	                    // Insert order items
	                    try (PreparedStatement itemStmt = conn.prepareStatement(insertOrderItemQuery)) {
	                        for (OrderItem item : order.getItems()) {
	                            itemStmt.setInt(1, orderId);
	                            itemStmt.setInt(2, item.getProductId());
	                            itemStmt.setInt(3, item.getQuantity());
	                            itemStmt.setDouble(4, item.getItemPrice());
	                            itemStmt.addBatch();
	                        }
	                        itemStmt.executeBatch(); // Execute all item insertions
	                    }
	                } else {
	                    throw new SQLException("Failed to retrieve generated order ID.");
	                }
	            }

	            conn.commit(); // Commit the transaction
	        } catch (SQLException e) {
	            conn.rollback(); // Rollback on error
	            e.printStackTrace();
	            throw new SQLException("Error creating order and order items: " + e.getMessage(), e);
	        }
	    }
	}



    /*private void addOrderItems(List<OrderItem> items, int orderId, Connection conn) throws SQLException {
        String query = "INSERT INTO e_OrderItem (order_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            for (OrderItem item : items) {
                stmt.setInt(1, orderId);
                stmt.setInt(2, item.getProductId());
                stmt.setInt(3, item.getQuantity());
                stmt.setDouble(4, item.getItemPrice());
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error adding order items: " + e.getMessage(), e);
        }
    }*/

    public List<Order> getOrdersByClientId(int clientId) throws SQLException {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT * FROM e_Orders WHERE client_id = ?";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, clientId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
            	Timestamp timestamp = rs.getTimestamp("orderDate");
            	LocalDate localDate = timestamp.toLocalDateTime().toLocalDate(); // Convert to LocalDate
            	    
            	// Set the value in your model
                Order order = new Order(
                        rs.getInt("order_id"),
                        rs.getInt("client_id"),
                        OrderStatus.valueOf(rs.getString("status")),
                        new ArrayList<>()  // Empty list for order items
                );
                order.setOrderDate(localDate);
                order.setTotalAmount(rs.getDouble("totalPrice")); // Correct column name
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error fetching orders: " + e.getMessage(), e);
        }
        return orders;
    }

    public List<Order> getAllOrders() throws SQLException {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT order_id, client_id, totalPrice, status, orderDate FROM e_Orders";

        try (Connection conn = DBConnect.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Order order = new Order(
                    rs.getInt("order_id"),
                    rs.getInt("client_id"),
                    OrderStatus.valueOf(rs.getString("status")),
                    null // Order items can be fetched separately if needed
                );
                order.setTotalAmount(rs.getDouble("totalPrice"));
                order.setOrderDate(rs.getDate("orderDate").toLocalDate()); // Assuming a LocalDate field in Order
                orders.add(order);
            }
        } catch (SQLException e) {
            throw new SQLException("Error fetching orders: " + e.getMessage(), e);
        }
        return orders;
    }


    public void updateOrderStatus(int orderId, OrderStatus status) throws SQLException {
        String query = "UPDATE e_Orders SET status = ? WHERE order_id = ?";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, status.name());
            stmt.setInt(2, orderId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Failed to update order status, no rows affected.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error updating order status: " + e.getMessage(), e);
        }
    }
    public void updateOrderTotalPrice(int orderId, double totalPrice) throws SQLException {
        String query = "UPDATE e_Orders SET totalPrice = ? WHERE order_id = ?";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setDouble(1, totalPrice);
            stmt.setInt(2, orderId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Failed to update total price, no rows affected.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error updating total price: " + e.getMessage(), e);
        }
    }
    public List<OrderItem> getOrderItemsByOrderId(int orderId) throws SQLException {
        List<OrderItem> orderItems = new ArrayList<>();
        String query = """
            SELECT oi.orderItem_id, oi.product_id, p.name, oi.quantity, oi.price 
            FROM e_OrderItem oi
            JOIN e_Product p ON oi.product_id = p.product_id
            WHERE oi.order_id = ?
        """;

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem(
                        rs.getInt("orderItem_id"),
                        rs.getInt("product_id"),
                        rs.getInt("quantity"),
                        rs.getDouble("price")
                    );
                    item.setProductName(rs.getString("name"));
                    orderItems.add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error fetching order items: " + e.getMessage(), e);
        }
        return orderItems;
    }
    
    public void saveOrderItem(OrderItem item) throws SQLException {
        String query = """
            INSERT INTO e_OrderItem (order_id, product_id, quantity, price)
            VALUES (?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
            quantity = VALUES(quantity), price = VALUES(price)
        """;

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, item.getOrderId());
            stmt.setInt(2, item.getProductId());
            stmt.setInt(3, item.getQuantity());
            stmt.setDouble(4, item.getItemPrice());
            stmt.executeUpdate();
        }
    }



    public void deleteOrder(int orderId) throws SQLException {
        String query = "DELETE FROM e_Orders WHERE order_id = ?";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, orderId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Failed to delete order, no rows affected.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error deleting order: " + e.getMessage(), e);
        }
    }
}
