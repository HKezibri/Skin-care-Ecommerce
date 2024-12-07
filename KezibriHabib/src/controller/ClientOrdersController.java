package controller;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import javafx.scene.layout.AnchorPane;


import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Client;
import model.Order;
import model.OrderItem;
import model.User;
import service.OrderService;
import service.InvoiceService;
import service.UserService;
import util.OrderStatus;
import util.SessionContext;




public class ClientOrdersController {
	
	 @FXML private TableView<Order> ordersTableView;
	 @FXML private TableColumn<Order, Integer> orderNumber; 
	 @FXML private TableColumn<Order, String> orderUsername;
	 @FXML private TableColumn<Order, String> orderAddress;
	 @FXML private TableColumn<Order, String> orderDate;
	 @FXML private TableColumn<Order, Double> orderTotal;
	 @FXML private TableColumn<Order, String> orderStatus;
	 @FXML private TableColumn<Order, Button> orderInvoice;
	 @FXML private TableColumn<Order, Button> updateStatusButton;  // New column for updating status
	 @FXML private Button clientAccount;
	 @FXML private Button clientLogout;
	 @FXML private Button homePage;
	 @FXML private Button orderDelButton;
	 @FXML private Button yourOrders;
	 @FXML private Button update;

	
	 private final OrderService orderService = new OrderService();
	 private final UserService userService = new UserService();
	 //private final ProductService productService = new ProductService();
	 private int clientId;
	 private User loggedInUser;
	
	 @FXML
	 public void initialize() {
	     initializeOrderTable();
	     
	     // Fetch the logged-in client ID from the session context
	 loggedInUser = SessionContext.getInstance().getLoggedInUser();
	 if (loggedInUser instanceof Client) {
	     clientId = ((Client) loggedInUser).getClientId();
	     clientAccount.setText(formatUsername(loggedInUser.getUsername())); // Set formatted username on the button 
	 }
	
	 if (clientId > 0) {
	     loadClientOrderData();
	 } else {
	     System.out.println("Client ID not found in session context. Please login again.");
	     showError("Unable to find your session. Please try logging in again.");
	     }
	 }
	
	 private void initializeOrderTable() {
		    // Incrementing order number column (1-based index)
		    orderNumber.setCellValueFactory(cellData -> {
		        int index = ordersTableView.getItems().indexOf(cellData.getValue()) + 1;
		        return new ReadOnlyObjectWrapper<>(index);
		    });

		    // Username column with formatted name
		    orderUsername.setCellValueFactory(data -> {
		        Client client = fetchClientById(data.getValue().getClientId());
		        String username = client != null ? formatUsername(client.getUsername()) : "Unknown";
		        return new ReadOnlyObjectWrapper<>(username);
		    });

		    // Address column
		    orderAddress.setCellValueFactory(data -> {
		        Client client = fetchClientById(data.getValue().getClientId());
		        return new ReadOnlyObjectWrapper<>(client != null ? client.getAddress() : "Unknown");
		    });

		    // Order date column
		    orderDate.setCellValueFactory(data -> {
		        LocalDate date = data.getValue().getOrderDate();
		        return date != null
		                ? new ReadOnlyObjectWrapper<>(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
		                : new ReadOnlyObjectWrapper<>("");
		    });

		    // Order total column
		    orderTotal.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getTotalAmount()));

		    // Order status column
		    orderStatus.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getStatus().name()));

		    // Purchase column to display item count using OrderService
		    TableColumn<Order, Integer> purchaseColumn = new TableColumn<>("Delivred to");
		    purchaseColumn.setCellValueFactory(data -> {
		        try {
		            return new ReadOnlyObjectWrapper<>(orderService.getOrderItemsByOrderId(data.getValue().getOrderId()).size());
		        } catch (SQLException e) {
		            e.printStackTrace();
		            return new ReadOnlyObjectWrapper<>(0);
		        }
		    });

		    // Add "View Invoice" button dynamically
		    orderInvoice.setCellFactory(column -> new TableCell<>() {
		        private final Button invoiceButton = new Button("View Invoice");

		        @Override
		        protected void updateItem(Button item, boolean empty) {
		            super.updateItem(item, empty);
		            if (empty) {
		                setGraphic(null);
		            } else {
		                Order order = getTableView().getItems().get(getIndex());
		                if (order.getStatus() == OrderStatus.validated || order.getStatus() == OrderStatus.delivered) {
		                    invoiceButton.setOnAction(e -> handleViewInvoice(order));
		                    setGraphic(invoiceButton);
		                } else {
		                    setGraphic(null);  // Hide button if order is not validated
		                }
		            }
		        }
		    });

		    
		}


	
	 private void loadClientOrderData() {
	     // Check if clientId is valid before trying to load orders
	 if (clientId <= 0) {
	     showError("Client ID is invalid. Cannot load orders.");
	     return;
	 }
	
	 try {
	     System.out.println("Fetching orders for client ID: " + clientId);
	     List<Order> orders = orderService.getOrdersByClientId(clientId);
	     ObservableList<Order> orderList = FXCollections.observableArrayList(orders);
	
	     // Set data in the TableView
	     ordersTableView.setItems(orderList);
	     ordersTableView.refresh();  // Ensure table updates properly
	 } catch (SQLException e) {
	     showError("Failed to load orders: " + e.getMessage());
	     }
	 }
	
	 
	 @FXML
	 private void handleUpdateOrderStatus(ActionEvent event) {
	     // Get the selected order from the table
	        Order selectedOrder = ordersTableView.getSelectionModel().getSelectedItem();		
		 if (selectedOrder == null) {
		     showError("No order selected.");
		     return;
		 }
		
		 // Create a choice dialog for updating the order status
		 ChoiceDialog<String> dialog = new ChoiceDialog<>(
		     selectedOrder.getStatus().name(), 
		     "in_progress", "validated"
		 );
		 dialog.setTitle("Update Order Status");
		 dialog.setHeaderText("Change the status of the selected order");
		 dialog.setContentText("Select new status:");
		
		 // Show the dialog and wait for the user's input
		 dialog.showAndWait().ifPresent(newStatus -> {
		     try {
		         // Update the order's status in the database
		         selectedOrder.setStatus(OrderStatus.valueOf(newStatus));
		         orderService.updateOrderStatus(selectedOrder.getOrderId(), OrderStatus.valueOf(newStatus));
		
		         // Reload the table data to reflect the changes
		         loadClientOrderData();
		         showInfo("Order status updated successfully.");
		     } catch (IllegalArgumentException e) {
		         showError("Invalid status selected.");
		     } catch (SQLException e) {
		         showError("Failed to update order status: " + e.getMessage());
		     }
		 }); 
		     
	 }
	
	 private Client fetchClientById(int clientId) {
	     try {
	         // Replace with actual database query logic
	     return userService.getClientById(clientId);
	 } catch (SQLException e) {
	     showError("Failed to fetch client data: " + e.getMessage());
	         return null;
	     }
	 }
	
	 @FXML
	 private void handleOrderDeleteButton(ActionEvent event) {
	     Order selectedOrder = ordersTableView.getSelectionModel().getSelectedItem();
	     if (selectedOrder == null) {
	         showError("No order selected.");
	     return;
	 }
	
	 try {
	     orderService.deleteOrder(selectedOrder.getOrderId());
	     loadClientOrderData(); // Reload data after deletion
	     showInfo("Order deleted successfully.");
	 } catch (SQLException e) {
	     showError("Failed to delete order: " + e.getMessage());
	     }
	 }

    @FXML
    private void handleLogout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout Confirmation");
        alert.setHeaderText("Are you sure you want to log out?");
        alert.setContentText("You will be redirected to the login page.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/WelcomePage.fxml"));
                Parent loginPage = loader.load();

                Stage stage = (Stage) clientLogout.getScene().getWindow();
                stage.setScene(new Scene(loginPage));
                stage.setTitle("Welcome Page");
                stage.centerOnScreen();
                stage.setResizable(false);
                stage.show();
            } catch (IOException e) {
                showError("Failed to load the login page: " + e.getMessage());
            }
        }
    }

    @FXML
    private void GoToHomePage(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ClientShoppingPage.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showError("Unable to load the home page: " + e.getMessage());
        }
    }
    
    @FXML
    private void GoToClientAccount(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ClientAccount.fxml"));
            Parent clientAccountPage = loader.load();

            // Get the current stage and set the new scene
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(clientAccountPage));
            stage.setTitle("Account Settings");
            stage.centerOnScreen();
        } catch (IOException e) {
            showError("Unable to load the Client Account Page: " + e.getMessage());
        }
    }

 
    
   
    private void handleViewInvoice(Order order) {
        System.out.println("Viewing invoice for Order ID: " + order.getOrderId());

        // Generate and save invoice
        try {
            InvoiceService invoiceService = new InvoiceService();
            invoiceService.CreateInvoice(order.getOrderId()); // Save invoice in the database
            System.out.println("Invoice saved successfully for Order ID: " + order.getOrderId());
        } catch (SQLException e) {
            showError("Failed to generate invoice: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // Create a new Stage to show the invoice
        Stage invoiceStage = new Stage();
        invoiceStage.setTitle("Invoice for Order #" + order.getOrderId());

        try {
            // Load the InvoicePage.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/InvoicePage.fxml"));
            AnchorPane invoicePage = loader.load();

            // Dynamically populate data
            populateInvoicePage(invoicePage, order);

            // Set the scene and show the stage
            Scene scene = new Scene(invoicePage);
            invoiceStage.setScene(scene);
            invoiceStage.show();

        } catch (IOException e) {
            System.err.println("Failed to load InvoicePage.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    
    
    private void populateInvoicePage(AnchorPane invoicePage, Order order) {
        // Fetch client details
        Client client = fetchClientById(order.getClientId());
        if (client != null) {
        	Label invoiceNumberLabel = (Label) invoicePage.lookup("#invoiceNumber");
        	if (invoiceNumberLabel != null) {
        	    invoiceNumberLabel.setText(String.valueOf(order.getOrderId())); // Assuming invoice ID is tied to the order ID
        	} else {
        	    System.out.println("Label '#invoiceNumber' not found in the FXML.");
        	}
            Label clientNameLabel = (Label) invoicePage.lookup("#clientName");
            if (clientNameLabel != null) clientNameLabel.setText(formatUsername(client.getUsername()));

            Label clientAddressLabel = (Label) invoicePage.lookup("#clientAddress");
            if (clientAddressLabel != null) clientAddressLabel.setText(client.getAddress());

            Label clientEmailLabel = (Label) invoicePage.lookup("#clientEmail");
            if (clientEmailLabel != null) clientEmailLabel.setText(client.getEmail());

            Label clientPhoneLabel = (Label) invoicePage.lookup("#clientPhone");
            if (clientPhoneLabel != null) clientPhoneLabel.setText(client.getPhone());
        }

        // Populate order date
        Label orderDateLabel = (Label) invoicePage.lookup("#dateOfIssue");
        if (orderDateLabel != null) {
            orderDateLabel.setText(order.getOrderDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }

        // Fetch order items
        List<OrderItem> orderItems;
        try {
            orderItems = orderService.getOrderItemsByOrderId(order.getOrderId());
        } catch (SQLException e) {
            showError("Failed to load order items: " + e.getMessage());
            return;
        }

        // Check TableView
        @SuppressWarnings("unchecked")
        TableView<OrderItem> itemsTable = (TableView<OrderItem>) invoicePage.lookup("#itemsTable");
        if (itemsTable == null) {
            System.out.println("TableView '#itemsTable' not found in the FXML.");
            return;
        }

        // Populate the TableView
        ObservableList<OrderItem> items = FXCollections.observableArrayList(orderItems);
        System.out.println("Number of items: " + items.size()); // Debugging line

        itemsTable.getColumns().clear(); // Clear old columns

        // Add Product Name Column
        TableColumn<OrderItem, String> productNameColumn = new TableColumn<>("Product Name");
        productNameColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getProductName()));
        itemsTable.getColumns().add(productNameColumn);

        // Add Quantity Column
        TableColumn<OrderItem, Integer> quantityColumn = new TableColumn<>("Quantity");
        quantityColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getQuantity()));
        itemsTable.getColumns().add(quantityColumn);

        // Add Unit Cost Column
        TableColumn<OrderItem, Double> priceColumn = new TableColumn<>("Unit Cost");
        priceColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getItemPrice()));
        itemsTable.getColumns().add(priceColumn);

        // Add Amount Column
        TableColumn<OrderItem, Double> amountColumn = new TableColumn<>("Amount");
        amountColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(
            data.getValue().getQuantity() * data.getValue().getItemPrice()
        ));
        amountColumn.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : String.format("%.2f", item));
            }
        });
        itemsTable.getColumns().add(amountColumn);

        // Set items to the TableView
        itemsTable.setItems(items);

        // Populate total amount
        Label invoiceTotalLabel = (Label) invoicePage.lookup("#invoiceTotal");
        if (invoiceTotalLabel != null) {
            invoiceTotalLabel.setText(String.format("$%.2f", order.getTotalAmount()));
        }
    }




    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Utility method to capitalize the first letter of each word in the username.
     * Example: "john doe" -> "John Doe"
     */
    private String formatUsername(String username) {
        if (username == null || username.isEmpty()) {
            return "";
        }

        String[] words = username.split("\\s+"); // Split by whitespace
        StringBuilder formattedUsername = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                formattedUsername.append(Character.toUpperCase(word.charAt(0)))  // Capitalize first letter
                                 .append(word.substring(1).toLowerCase())       // Rest in lowercase
                                 .append(" ");
            }
        }

        return formattedUsername.toString().trim(); // Remove trailing space
    }
}
