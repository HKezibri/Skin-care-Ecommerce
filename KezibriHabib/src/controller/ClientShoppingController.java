package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Pos; // For alignment settings in layouts like HBox and VBox
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.*;
import service.*;
import util.OrderStatus;
import javafx.scene.Node;
import util.SessionContext;


import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public class ClientShoppingController {

    @FXML private ImageView Img;
    @FXML private Button addCartButton, removeBtn, checkoutBtn, clientLogout, clientAccount, SearchButton, yourOrders, homePage;
    @FXML private TableColumn<OrderItem, String> itemCol;
    @FXML private TableColumn<OrderItem, Integer> qtyCol;
    @FXML private TableColumn<OrderItem, Double> costCol, amountCol;
    @FXML private TableView<OrderItem> cartTable;
    @FXML private ChoiceBox<Product> productNameChoice;
    @FXML private Label productPrice, totalLabel;
    @FXML private Spinner<Integer> productQty;
    @FXML private TextField SearchText;
    @FXML private FlowPane flowPane;
    

    private final ObservableList<OrderItem> orderItems = FXCollections.observableArrayList();
    private final ProductService productService = new ProductService();
    private final OrderService orderService = new OrderService();
    //private final UserService userservice = new UserService();
    private int clientId; // Holds the retrieved client_id
    //private User loggedInUser;
 
    
   
    @FXML
    public void initialize() {
        // Fetch logged-in user from the session context
        User loggedInUser = SessionContext.getInstance().getLoggedInUser();

        if (loggedInUser != null) {
            clientAccount.setText(formatUsername(loggedInUser.getUsername())); // Set formatted username on the button 

            if (loggedInUser instanceof Client) {
                // Store client ID in the session context for future use
                this.clientId = ((Client) loggedInUser).getClientId(); // Assign to controller's clientId field
                SessionContext.getInstance().setClientId(this.clientId);
                System.out.println("Client ID for logged-in user: " + this.clientId);
            }
        } else {
            showError("Failed to retrieve client information. Please contact support.");
        }
        setupProductChoiceBox();
        setupTableView();
        setupSpinner();
        loadProductChoices();
        loadProductCards();
    }


    private void setupProductChoiceBox() {
        productNameChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                displayProductImage(newVal);
            }
        });
    }

    private void setupTableView() {
        itemCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(getProductNameById(data.getValue().getProductId())));
        qtyCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getQuantity()));
        costCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getItemPrice()));
        amountCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().TotalPrice()));
        cartTable.setItems(orderItems);
        formatPriceColumns();
    }

    private void setupSpinner() {
        // Listen for changes in the selected product
        productNameChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldProduct, selectedProduct) -> {
            if (selectedProduct != null) {
                int maxStock = selectedProduct.getStockQuantity(); // Fetch stock for selected product
                if (maxStock > 0) {
                    productQty.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, maxStock, 1));
                    productQty.setDisable(false); // Enable the spinner
                } else {
                	showInfo("This product is currently out of stock. Please check back later");
                    productQty.setDisable(true); // Disable the spinner if no stock is available
                }
            } else {
                productQty.setValueFactory(null); // Reset spinner if no product is selected
            }
        });
    }

    
    private void loadProductChoices() {
        try {
            List<Product> products = productService.getAllProducts();
            productNameChoice.setItems(FXCollections.observableArrayList(products));
        } catch (SQLException e) {
            showError("Failed to load products: " + e.getMessage());
        }
    }

    private void loadProductCards() {
        try {
            List<Product> products = productService.getAllProducts();
            flowPane.getChildren().clear();
            for (Product product : products) {
                VBox productCard = createProductCard(product);
                flowPane.getChildren().add(productCard);
            }
        } catch (SQLException e) {
            showError("Failed to load product cards: " + e.getMessage());
        }
    }
   

    @FXML
    private void handleSearch(ActionEvent event) {
        String searchTerm = SearchText.getText().trim(); // Get text from the search field

        if (searchTerm.isEmpty()) {
            loadProductCards(); // Reload all products if the search term is empty
            return;
        }

        try {
            // Filter products based on the search term (case-insensitive search)
            List<Product> filteredProducts = productService.getAllProducts().stream()
                .filter(product -> product.getProductName().toLowerCase().contains(searchTerm.toLowerCase()) ||
                                   product.getDescription().toLowerCase().contains(searchTerm.toLowerCase()))
                .toList();

            flowPane.getChildren().clear(); // Clear the existing product cards

            if (filteredProducts.isEmpty()) {
                // Show a "no results" message if no products match
                Label noResultsLabel = new Label("No products match your search.");
                noResultsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: red;");
                flowPane.getChildren().add(noResultsLabel);
            } else {
                // Display filtered products
                for (Product product : filteredProducts) {
                    VBox productCard = createProductCard(product);
                    flowPane.getChildren().add(productCard);
                }
            }
        } catch (SQLException e) {
            showError("Failed to search products: " + e.getMessage());
        }
    }



    private VBox createProductCard(Product product) {
        // Create the product image
        ImageView productImage = new ImageView();
        String imagePath = product.getPimage();
        try {
            if (imagePath != null) {
                productImage.setImage(new Image(imagePath.startsWith("http") ? imagePath : "file:" + imagePath, true));
            } else {
                productImage.setImage(new Image("/images/default.png"));
            }
        } catch (Exception e) {
            productImage.setImage(new Image("/images/default.png"));
        }
        productImage.setFitWidth(180); // Adjusted for proper centering
        productImage.setFitHeight(140);
        productImage.setPreserveRatio(true);

        // Create the product name
        Label nameLabel = new Label(product.getProductName());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        // Create the product description
        Label descriptionLabel = new Label(product.getDescription());
        descriptionLabel.setWrapText(true); // Enable text wrapping
        descriptionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;");

        // Combine name and description into a VBox
        VBox nameAndDescriptionBox = new VBox(5, nameLabel, descriptionLabel); // Add spacing
        nameAndDescriptionBox.setAlignment(Pos.TOP_LEFT);
        nameAndDescriptionBox.setPrefWidth(200); // Consistent width
        nameAndDescriptionBox.setMaxWidth(200);

        // Create the product price label
        Label priceLabel = new Label(String.format("$%.2f", product.getPrice()));
        priceLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: normal;");

        // Create the "Stock" label
        Label stockLabel = new Label("Only a Few Left!");
        stockLabel.setStyle("-fx-background-color: #FFC107; -fx-text-fill: white; -fx-padding: 5px 10px; -fx-border-radius: 5px; -fx-background-radius: 5px;");

        // Create the "Out of Stock" label
        Label outOfStockLabel = new Label("Out of Stock!");
        outOfStockLabel.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-padding: 5px 10px; -fx-border-radius: 5px; -fx-background-radius: 5px;");

        // Update visibility based on stock quantity
        int stockQuantity = product.getStockQuantity();
        stockLabel.setVisible(stockQuantity > 0 && stockQuantity <= 10); // Show when stock is 1-10
        outOfStockLabel.setVisible(stockQuantity == 0);                 // Show when stock is 0

        // Combine price, "Stock," and "Out of Stock" labels into an HBox
        HBox priceAndStockBox = new HBox(10, priceLabel);
        if (stockLabel.isVisible()) {
            priceAndStockBox.getChildren().add(stockLabel);
        }
        if (outOfStockLabel.isVisible()) {
            priceAndStockBox.getChildren().add(outOfStockLabel);
        }

        priceAndStockBox.setAlignment(Pos.CENTER_LEFT);


        // Create the VBox container for the product card
        VBox productCard = new VBox(15, nameAndDescriptionBox, productImage, priceAndStockBox);
        productCard.setAlignment(Pos.TOP_CENTER); // Align everything at the top
        productCard.setPrefWidth(200);
        productCard.setPrefHeight(300);
        productCard.setSpacing(10);
        productCard.setStyle("-fx-border-color: #cccccc; "
                           + "-fx-border-radius: 10px; "
                           + "-fx-background-color: #ffffff; "
                           + "-fx-background-radius: 10px; "
                           + "-fx-padding: 15px;");

        // Add click event to the card
        productCard.setOnMouseClicked(event -> {
            productNameChoice.getSelectionModel().select(product);
            displayProductImage(product);
            productQty.getValueFactory().setValue(1);
        });

        return productCard;
    }




    private String getProductNameById(int productId) {
        try {
            Product product = productService.getProductById(productId);
            return product != null ? product.getProductName() : "Unknown Product";
        } catch (SQLException e) {
            return "Unknown Product";
        }
    }

    private void displayProductImage(Product product) {
        try {
            String imagePath = product.getPimage();
            Img.setImage(new Image(imagePath.startsWith("http") ? imagePath : "file:" + imagePath, true));
        } catch (Exception e) {
            Img.setImage(new Image("/images/default.png"));
        }
    }

    @FXML
    private void handleAddToCart(ActionEvent event) {
        Product selectedProduct = productNameChoice.getSelectionModel().getSelectedItem();
        Integer quantity = productQty.getValue(); // Use Integer to handle null values safely

        // Validate product selection and quantity
        if (selectedProduct == null) {
            showError("Please select a product to add to the cart.");
            return;
        }

        if (quantity == null || quantity <= 0) {
            // Show alert if the quantity is not set or invalid
            showError("Please select a valid quantity to add to the cart.");
            return;
        }

        if (selectedProduct.getStockQuantity() < quantity) {
            showError("Insufficient stock available.");
            return;
        }

        // Check if the product already exists in the cart and update its quantity
        OrderItem existingItem = orderItems.stream()
                .filter(item -> item.getProductId() == selectedProduct.getIdProduct())
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
        } else {
            orderItems.add(new OrderItem(0, selectedProduct.getIdProduct(), quantity, selectedProduct.getPrice()));
        }

        cartTable.refresh(); // Ensure table updates
        try {
            productService.updateProductStock(selectedProduct.getIdProduct(), -quantity);

            // Fetch or create the in-progress order for this client
            List<Order> existingOrders = orderService.getOrdersByClientId(this.clientId);
            Order inProgressOrder = existingOrders.stream()
                    .filter(order -> order.getStatus() == OrderStatus.in_progress)
                    .findFirst()
                    .orElse(null);

            if (inProgressOrder == null) {
                // Create a new in-progress order if none exists
                inProgressOrder = new Order(this.clientId, OrderStatus.in_progress, new ArrayList<>(orderItems));
                inProgressOrder.setTotalAmount(calculateTotalCost());
                orderService.createOrder(inProgressOrder); // Save the new order to the database
            } else {
                // Update the total price of the existing order
                double updatedTotalPrice = calculateTotalCost();
                orderService.updateOrderTotalPrice(inProgressOrder.getOrderId(), updatedTotalPrice);

                // Update items in the database if necessary
                /*for (OrderItem item : orderItems) {
                    // Update or add items in the database
                    // Logic for saving/updating items should be handled in service
                }*/
            }

            updateTotalCost();
            showInfo("Item added to cart successfully!");
        } catch (SQLException e) {
            showError("Failed to update stock or create/update order: " + e.getMessage());
        }
    }


    private double calculateTotalCost() {
        return orderItems.stream().mapToDouble(OrderItem::TotalPrice).sum();
    }

    private void updateTotalCost() {
        double total = calculateTotalCost();
        totalLabel.setText(String.format("Total: $%.2f", total));
    }

    @FXML
    private void handleRemoveButton(ActionEvent event) {
        OrderItem selectedItem = cartTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showError("No item selected.");
            return;
        }

        orderItems.remove(selectedItem);
        cartTable.refresh();
        try {
            productService.updateProductStock(selectedItem.getProductId(), selectedItem.getQuantity());

            // Fetch the in-progress order
            List<Order> existingOrders = orderService.getOrdersByClientId(this.clientId);
            Order inProgressOrder = existingOrders.stream()
                    .filter(order -> order.getStatus() == OrderStatus.in_progress)
                    .findFirst()
                    .orElse(null);

            if (inProgressOrder != null) {
                // Update total price in the database
                double updatedTotalPrice = calculateTotalCost();
                orderService.updateOrderTotalPrice(inProgressOrder.getOrderId(), updatedTotalPrice);

            }

            updateTotalCost();
            showInfo("Item removed from cart successfully!"); // Informational alert added here
        } catch (SQLException e) {
            showError("Failed to update stock or order: " + e.getMessage());
        }
    }


    @FXML
    private void handleCheckout(ActionEvent event) {
        if (orderItems.isEmpty()) {
            showError("Cart is empty.");
            return;
        }

        try {
            double totalCost = calculateTotalCost();

            // Fetch the in-progress order for this client
            List<Order> existingOrders = orderService.getOrdersByClientId(clientId);
            Order inProgressOrder = existingOrders.stream()
                    .filter(order -> order.getStatus() == OrderStatus.in_progress)
                    .findFirst()
                    .orElse(null);

            if (inProgressOrder != null) {
                // Finalize the order by updating its status to validated
                orderService.updateOrderStatus(inProgressOrder.getOrderId(), OrderStatus.validated);

                // Update total price one last time
                orderService.updateOrderTotalPrice(inProgressOrder.getOrderId(), totalCost);

                // Save all order items to the database
                for (OrderItem item : orderItems) {
                    item.setOrderId(inProgressOrder.getOrderId()); // Link each item to the finalized order
                    orderService.saveOrderItem(item); // Save or update the item in the database
                }
            } else {
                showError("No in-progress order found to finalize.");
                return;
            }

            // Clear the cart and reset UI
            orderItems.clear();
            cartTable.refresh();
            updateTotalCost();
            loadProductCards();
            showInfo("Checkout completed successfully!");
        } catch (SQLException e) {
            showError("Checkout failed: " + e.getMessage());
        }
    }



    @FXML
    private void GoToOrders(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ClientOrdersPage.fxml"));
            Parent ordersPage = loader.load();

            // Get the current stage and set the new scene
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(ordersPage));
            stage.setTitle("Your Orders");
            stage.centerOnScreen();
        } catch (IOException e) {
            showError("Unable to load the Orders Page: " + e.getMessage());
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


    
    @FXML
    private void GoToHomePage(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ClientShoppingPage.fxml"));
            Parent homePage = loader.load();

            // Get the current stage and set the new scene
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(homePage));
            stage.setTitle("Shopping Page");
            stage.centerOnScreen();
        } catch (IOException e) {
            showError("Unable to load the Shopping Page: " + e.getMessage());
        }
    }

    


    @FXML
    private void handleLogout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout Confirmation");
        alert.setHeaderText("Are you sure you want to log out?");
        alert.setContentText("You will be redirected to the Welcome Page.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/WelcomePage.fxml"));
                Parent welcomePage = loader.load();

                // Get the current stage and set the new scene
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(welcomePage));
                stage.setTitle("Welcome Page");
                stage.centerOnScreen();
            } catch (IOException e) {
                showError("Failed to load the Welcome Page: " + e.getMessage());
            }
        }
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

    private void formatPriceColumns() {
        costCol.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("$%.2f", item));
            }
        });

        amountCol.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("$%.2f", item));
            }
        });
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.showAndWait();
    }
}

    