package service;

import java.sql.*;
import java.util.Date;
import model.Invoice;
import mySQLdb.DBConnect;

public class InvoiceService {

    public void CreateInvoice(int orderId) throws SQLException {
        String query = """
            SELECT o.order_id, o.orderDate, oi.product_id, oi.quantity, p.price 
            FROM e_Orders o
            JOIN e_OrderItem oi ON o.order_id = oi.order_id
            JOIN e_Product p ON oi.product_id = p.product_id
            WHERE o.order_id = ?
        """;

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();

            // Check if the order exists and fetch order details
            double totalPrice = 0.0;
            Date orderDate = null;

            while (rs.next()) {
                if (orderDate == null) {
                    orderDate = rs.getDate("orderDate");
                }
                int quantity = rs.getInt("quantity");
                double productPrice = rs.getDouble("price");
                totalPrice += quantity * productPrice; // Accumulate total price
            }

            if (orderDate == null) {
                throw new SQLException("No valid order found with ID: " + orderId);
            }

            // Create and save the invoice
            Invoice invoice = new Invoice(orderId, totalPrice, new java.util.Date());
            saveInvoice(invoice, conn);

            System.out.println("Invoice generated and saved successfully for Order ID: " + orderId);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error generating invoice: " + e.getMessage(), e);
        }
    }

    private void saveInvoice(Invoice invoice, Connection conn) throws SQLException {
        String query = "INSERT INTO e_Invoice (totalPrice, order_id, invoiceDate) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setDouble(1, invoice.getTotalprice());
            stmt.setInt(2, invoice.getOrderId());
            stmt.setDate(3, new java.sql.Date(invoice.getInvoiceDate().getTime()));

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Failed to generate invoice, no rows affected.");
            }

            // Retrieve and set the generated ID for the invoice
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    invoice.setIdInvoice(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error saving invoice: " + e.getMessage(), e);
        }
    }

    public void updateInvoice(int orderId, double newTotalPrice) throws SQLException {
        String query = "UPDATE e_Invoice SET totalPrice = ? WHERE order_id = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setDouble(1, newTotalPrice);
            stmt.setInt(2, orderId);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("Failed to update invoice for order ID " + orderId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error updating invoice: " + e.getMessage(), e);
        }
    }

    public void deleteInvoice(int idInvoice) throws SQLException {
        String query = "DELETE FROM e_Invoice WHERE invoice_id = ?";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idInvoice);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("No invoice found with ID: " + idInvoice + ". Deletion failed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error deleting invoice: " + e.getMessage(), e);
        }
    }
}
