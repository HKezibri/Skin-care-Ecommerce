
package controller;

import java.time.format.DateTimeFormatter;
import java.time.LocalDate;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javafx.application.Platform;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import javafx.scene.text.Text;
import javafx.beans.property.ReadOnlyObjectWrapper;

import javafx.collections.FXCollections;
import javafx.stage.FileChooser;
import javafx.collections.ObservableList;

import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.control.*;
import model.Client;
import model.Order;
import model.Product;
import model.Admin;
import model.User;
import util.OrderStatus;
import util.UserInputValidator;
import service.OrderService;
import service.UserService;
import service.ProductService;
import service.SearchService;
//import service.AuthentificationService;


import java.sql.SQLException;
import java.util.List;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AdminPageController {

    // UI Components
    @FXML private Button adminLogout;
    @FXML private Button adminAccount;


    // Orders Tab
    @FXML private Tab OrdersTab;
    @FXML private TableView<Order> OrdersTableView;
    @FXML private TableColumn<Order, Integer> OrderIDCol;
    @FXML private TableColumn<Order, Integer> orderClientIDCol;
    @FXML private TableColumn<Order, String> orderNameCol;
    @FXML private TableColumn<Order, String> orderAddressCol;
    @FXML private TableColumn<Order, Double> orderPriceCol;
    @FXML private TableColumn<Order, String> orderStatusCol;
    @FXML private TableColumn<Order, String> orderDateCol;
    @FXML private Button OrderUpdateButton;
    @FXML private Button orderDelButton;

    // Clients Tab
    @FXML private Tab customerTab;
    @FXML private TableView<Client> customerTableView;
    @FXML private TableColumn<Client, Integer> customerIdCol;
    @FXML private TableColumn<Client, String> customerNameCol;
    @FXML private TableColumn<Client, String> customerEmailCol;
    @FXML private TableColumn<Client, String> customerPhoneCol;
    @FXML private TableColumn<Client, String> customerAddressCol;
    @FXML private TableColumn<Client, String> customerPassCol;
    @FXML private Button customerAddButton;
    @FXML private Button customerDelButton;
    @FXML private Button customerUpdateButton, ClientFormClearButton;
    @FXML private TextField addClientNameText;
    @FXML private TextField addClientAddressText;
    @FXML private TextField addClientEmailText;
    @FXML private TextField addClientPhoneText;
    @FXML private TextField addClientPassText;
    @FXML private TextField addClientConfirmText;
    
    // Products Tab
    @FXML private Tab productsTab;
    @FXML private TableView<Product> productTableView;
    @FXML private TableColumn<Product, Integer> productIdCol;
    @FXML private TableColumn<Product, String> productNameCol;
    @FXML private TableColumn<Product, String> productDesripCol;
    @FXML private TableColumn<Product, Double> productPriceCol;
    @FXML private TableColumn<Product, Integer> productStockCol;
    @FXML private TableColumn<Product, String> productImageCol;
    @FXML private Button productAddButton;
    @FXML private Button productDellButton;
    @FXML private Button productUpdateButoon;
    @FXML private Button productImageButton, ProductFormClearButton;
    @FXML private TextField productNameText;
    @FXML private TextField productDescripText;
    @FXML private TextField productStockText;
    @FXML private TextField productPriceText;
    @FXML private TextField imgPath;
    @FXML private ImageView productImageView;
    
    // Staff Tab
    @FXML private Tab staffTab;
    @FXML private TableView<Admin> StaffTableView;
    @FXML private TableColumn<Admin, Integer> staffIDCol;
    @FXML private TableColumn<Admin, String> staffUsernameCol;
    @FXML private TableColumn<Admin, String> staffPasswordCol;
    @FXML private Button StaffAddButton;
    @FXML private Button staffDelButton;
    @FXML private Button staffUpdateButton;
    @FXML private TextField addStaffUsernameText;
    @FXML private TextField addStaffPassText;
    @FXML private TextField addStaffConfirmText;
    
    // Search Tab
    @FXML private Text searchResult;

    // Services
    private final OrderService orderService = new OrderService();
    private final UserService userService = new UserService();
    private final ProductService productService = new ProductService();
    private final SearchService searchService = new SearchService();
    //private final AuthentificationService  authService = new AuthentificationService(); 
    private final ObservableList<Product> products = FXCollections.observableArrayList();

    
    

    @FXML
    public void initialize() {
        initializeOrderTable();
        initializeClientTable();
        initializeProductTable();
        initializeStaffTable();
        loadOrderData();
        loadClientData();
        loadProductData();
        loadStaffData();
       
    }
//ORDERS
    private void initializeOrderTable() {
        OrderIDCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getOrderId()));
        orderClientIDCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getClientId()));

        // Dynamically fetch client name
        orderNameCol.setCellValueFactory(data -> {
            int clientId = data.getValue().getClientId();
            Client client = fetchClientById(clientId);
            return new ReadOnlyObjectWrapper<>(client != null ? client.getUsername() : "Unknown");
        });

        // Dynamically fetch client address
        orderAddressCol.setCellValueFactory(data -> {
            int clientId = data.getValue().getClientId();
            Client client = fetchClientById(clientId);
            return new ReadOnlyObjectWrapper<>(client != null ? client.getAddress() : "Unknown");
        });

        orderPriceCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getTotalAmount()));
        orderStatusCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getStatus().name()));
     // Format and display the date
        orderDateCol.setCellValueFactory(data -> {
            LocalDate orderDate = data.getValue().getOrderDate();
            if (orderDate != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                return new ReadOnlyObjectWrapper<>(orderDate.format(formatter));
            }
            return new ReadOnlyObjectWrapper<>(""); // Return an empty string if the date is null
        }); 
    }

    private void initializeClientTable() {
    	customerTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                populateCustomerForm(newValue);
            }
        });
             
        customerIdCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getClientId()));
    	customerNameCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getUsername()));
    	customerAddressCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getAddress()));
    	customerEmailCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getEmail()));
    	customerPassCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getPassword()));
        customerPhoneCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getPhone()));
       
    }
    
    private void initializeStaffTable() {
        staffIDCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getUserId()));
        staffUsernameCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getUsername()));
        staffPasswordCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getPassword()));
    }
     
    private void initializeProductTable() {
    	// Add a listener to the TableView selection model
        productTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                populateProductForm(newValue);
            }
        });
        productTableView.setItems(products); // Link ObservableList to TableView
        loadProductData();
        // Set cell value factories for each column in the product table
        productIdCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getIdProduct()));
        productNameCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getProductName()));
        productDesripCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getDescription()));
        productPriceCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getPrice()));
        productStockCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getStockQuantity()));

        // For displaying images
        productImageCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getPimage()));

        productImageCol.setCellFactory(col -> new TableCell<>() {
            private final ImageView imageView = new ImageView();

            @Override
            protected void updateItem(String imagePath, boolean empty) {
                super.updateItem(imagePath, empty);
                if (empty || imagePath == null || imagePath.isBlank()) {
                    setGraphic(null); // Clear cell
                } else {
                    Platform.runLater(() -> {
                        try {
                            Image image = imagePath.startsWith("http")
                                ? new Image(imagePath, 50, 50, true, true)
                                : new Image(new File(imagePath).toURI().toString(), 50, 50, true, true);
                            imageView.setImage(image);
                        } catch (Exception e) {
                            System.err.println("Error loading image: " + e.getMessage());
                            imageView.setImage(null); // Clear image on error
                        }
                        setGraphic(imageView);
                    });
                }
            }
        });


    }


    @FXML
    public void handleTabSelection(Event event) {
        Tab selectedTab = (Tab) event.getSource();
        System.out.println("Tab selected: " + selectedTab.getText());

        // Perform different actions based on the selected tab
        if (selectedTab == OrdersTab) {
            System.out.println("Orders tab selected. Load orders data.");
            loadOrderData();
        } else if (selectedTab == customerTab) {
            System.out.println("Customer tab selected. Load customers data.");
            loadClientData();
        } else if (selectedTab == productsTab) {
            System.out.println("Products tab selected. Load products data.");
            loadProductData();
        } else if (selectedTab == staffTab) {
            System.out.println("Staff tab selected. Load staff data.");
            loadStaffData();
        }
    }


    private void loadOrderData() {
        try {
            List<Order> orders = orderService.getAllOrders();
            ObservableList<Order> orderList = FXCollections.observableArrayList(orders);
            OrdersTableView.setItems(orderList);
        } catch (SQLException e) {
            showError("Failed to load orders: " + e.getMessage());
        }
    }

    private void loadClientData() {
        try {
            List<Client> clients = userService.getAllClients();
            ObservableList<Client> clientList = FXCollections.observableArrayList(clients);
            customerTableView.setItems(clientList);
        } catch (SQLException e) {
            showError("Failed to load clients: " + e.getMessage());
        }
    }
   
    private void loadStaffData() {
        try {
            // Retrieve all admins from the database
            List<Admin> admins = userService.getAllAdmins();
            ObservableList<Admin> staffList = FXCollections.observableArrayList(admins);

            // Populate the TableView with the retrieved staff data
            StaffTableView.setItems(staffList);
        } catch (SQLException e) {
            showError("Failed to load staff data: " + e.getMessage());
        }
    }
    
    private void loadProductData() {
        try {
            // Fetch all products using the ProductService
            List<Product> fetchedProducts = productService.getAllProducts();

            // Clear and add the new products to the existing ObservableList
            products.clear();
            products.addAll(fetchedProducts);

            // No need to set the items again as the list is already bound
        } catch (SQLException e) {
            showError("Failed to load products: " + e.getMessage());
        }
    }


///////////////////////////////////////CRUD//////////////////////////////////////////////
    //********************************************ORDERS*****************************************************

    @FXML
    void handleOrderUpdateButton(ActionEvent event) {
        // Get the selected order from the table
        Order selectedOrder = OrdersTableView.getSelectionModel().getSelectedItem();

        if (selectedOrder == null) {
            showError("No order selected.");
            return;
        }

        // Create a choice dialog for updating the order status
        ChoiceDialog<String> dialog = new ChoiceDialog<>(
            selectedOrder.getStatus().name(), 
            "in_progress", "validated", "delivered"
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
                loadOrderData();
                showInfo("Order status updated successfully.");
            } catch (IllegalArgumentException e) {
                showError("Invalid status selected.");
            } catch (SQLException e) {
                showError("Failed to update order status: " + e.getMessage());
            }
        });
    }
    @FXML
    void handleOrderDeleteButton(ActionEvent event) {
        Order selectedOrder = OrdersTableView.getSelectionModel().getSelectedItem();
        if (selectedOrder == null) {
            showError("No order selected.");
            return;
        }

        try {
            orderService.deleteOrder(selectedOrder.getOrderId());
            loadOrderData();
            showInfo("Order deleted successfully.");
        } catch (SQLException e) {
            showError("Failed to delete order: " + e.getMessage());
        }
    }
   
    //********************************************CLIENT*****************************************************
    @FXML
    private void onClientSelected() {
        Client selectedClient = customerTableView.getSelectionModel().getSelectedItem();
        if (selectedClient != null) {
            addClientNameText.setText(selectedClient.getUsername());
            addClientAddressText.setText(selectedClient.getAddress());
            addClientEmailText.setText(selectedClient.getEmail());
            addClientPhoneText.setText(selectedClient.getPhone());
        }
    }
   
    private void populateCustomerForm(Client client) {
        if (client == null) {
            // Clear the form fields if no client is selected
        	clearClientForm();
        } else {
            try {
                // Populate text fields with the client's details
                addClientNameText.setText(client.getUsername());
                addClientAddressText.setText(client.getAddress());
                addClientEmailText.setText(client.getEmail());
               addClientPhoneText.setText(client.getPhone());

                // Optionally, display the password (if allowed, or leave it blank for security)
               addClientPassText.setText(client.getPassword());
            } catch (Exception e) {
                showError("Error populating client form: " + e.getMessage());
            }
        }
    }
    @FXML
    void handleClientUpdateButton(ActionEvent event) {
        // Get the selected client from the table
        Client selectedClient = customerTableView.getSelectionModel().getSelectedItem();

        if (selectedClient == null) {
            showError("No client selected.");
            return;
        }

        // Validate the updated input fields
        String updatedName = addClientNameText.getText().trim();
        String updatedAddress = addClientAddressText.getText().trim();
        String updatedEmail = addClientEmailText.getText().trim();
        String updatedPhone = addClientPhoneText.getText().trim();

        if (updatedName.isEmpty() || updatedAddress.isEmpty() || updatedEmail.isEmpty() || updatedPhone.isEmpty()) {
            showError("All fields are required.");
            return;
        }

        if (!UserInputValidator.isValidUsername(updatedName)) {
            showError("Invalid name. It must consist of two words separated by a space.");
            return;
        }

        if (!UserInputValidator.isValidEmail(updatedEmail)) {
            showError("Invalid email format.");
            return;
        }

        if (!UserInputValidator.isValidPhoneNumber(updatedPhone)) {
            showError("Invalid phone number format.");
            return;
        }

        try {
            // Update the client object with new details
            selectedClient.setUsername(updatedName);
            selectedClient.setAddress(updatedAddress);
            selectedClient.setEmail(updatedEmail);
            selectedClient.setPhone(updatedPhone);

            // Update the client in the database
            userService.updateClient(selectedClient);

            // Reload the client table to reflect changes
            loadClientData();
            showInfo("Client updated successfully.");
        } catch (SQLException e) {
            showError("Failed to update client: " + e.getMessage());
        }
    }

    @FXML
    void handleClientDeleteButton(ActionEvent event) {
        Client selectedClient = customerTableView.getSelectionModel().getSelectedItem();
        if (selectedClient == null) {
            showError("No client selected.");
            return;
        }

        try {
            userService.deleteUser(selectedClient.getUserId());
            loadClientData();
            showInfo("Client deleted successfully.");
        } catch (SQLException e) {
            showError("Failed to delete client: " + e.getMessage());
        }
    }


    private Client fetchClientById(int clientId) {
        try {
            return userService.getClientById(clientId);
        } catch (SQLException e) {
            System.err.println("Error fetching client: " + e.getMessage());
            return null;
        }
    }
    
    @FXML
    void handleClientAddButton(ActionEvent event) {
        // Retrieve values from the text fields
        String name = addClientNameText.getText().trim();
        String address = addClientAddressText.getText().trim();
        String email = addClientEmailText.getText().trim();
        String phone = addClientPhoneText.getText().trim();
        String password = addClientPassText.getText().trim();
        String confirmPassword = addClientConfirmText.getText().trim();

        try {
            // Validate input
            if (name.isEmpty() || address.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                throw new IllegalArgumentException("All fields are required.");
            }

            if (!UserInputValidator.isValidUsername(name)) {
                throw new IllegalArgumentException("Invalid name. It must consist of two words separated by a space.");
            }

            if (!UserInputValidator.isValidEmail(email)) {
                throw new IllegalArgumentException("Invalid email format.");
            }

            if (!UserInputValidator.isValidPhoneNumber(phone)) {
                throw new IllegalArgumentException("Invalid phone number format.");
            }

            if (!UserInputValidator.isValidPassword(password)) {
                throw new IllegalArgumentException("Password must be at least 8 characters long and contain both letters and numbers.");
            }

            if (!password.equals(confirmPassword)) {
                throw new IllegalArgumentException("Passwords do not match.");
            }

            // Create a new client
            Client newClient = new Client(name, password, email, phone, address);

            // Save the client using the UserService
            userService.createUser(newClient);

            // Reload the client data to reflect the changes
            loadClientData();

            // Clear input fields
            clearClientForm();

            // Show success message
            showInfo("Client added successfully.");

        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (SQLException e) {
            showError("Failed to add client: " + e.getMessage());
        }
    }

    /**
     * Clears the input fields in the client form.
     */
 
    private void clearClientForm() {
        addClientNameText.clear();
        addClientAddressText.clear();
        addClientEmailText.clear();
        addClientPhoneText.clear();
        addClientPassText.clear();
        addClientConfirmText.clear();
    }
    
    @FXML
    void clearClientForm(ActionEvent event) {
    	clearClientForm();
    }
    
    
    //********************************************ADMIN STAFF*************************************************
    @FXML
    void handleStaffAddButton(ActionEvent event) {
        // Retrieve input from the text fields
        String username = addStaffUsernameText.getText().trim();
        String password = addStaffPassText.getText().trim();
        String confirmPassword = addStaffConfirmText.getText().trim();

        // Validate inputs
        try {
            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                throw new IllegalArgumentException("All fields are required.");
            }

            if (!password.equals(confirmPassword)) {
                throw new IllegalArgumentException("Passwords do not match.");
            }

            if (!UserInputValidator.isValidUsername(username)) {
                throw new IllegalArgumentException("Invalid username. It must consist of a first and last name separated by a space.");
            }

            if (!UserInputValidator.isValidPassword(password)) {
                throw new IllegalArgumentException("Invalid password. It must be at least 8 characters long and contain both letters and numbers.");
            }

            // Create a new Admin object
            Admin newAdmin = new Admin(username, password);

            // Add the new admin to the database
            UserService userService = new UserService();
            userService.createUser(newAdmin);

            // Reload staff data to reflect the new addition
            loadStaffData();

            // Clear input fields
            clearStaffForm();

            // Show success message
            showInfo("Staff added successfully.");

        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (SQLException e) {
            showError("Failed to add staff: " + e.getMessage());
        }
    }
    @FXML
    void handleStaffUpdateButton(ActionEvent event) {
        // Get the selected staff from the TableView
        Admin selectedStaff = StaffTableView.getSelectionModel().getSelectedItem();

        if (selectedStaff == null) {
            showError("No staff selected for update.");
            return;
        }

        // Retrieve input from the text fields
        String updatedUsername = addStaffUsernameText.getText().trim();
        String updatedPassword = addStaffPassText.getText().trim();
        String confirmPassword = addStaffConfirmText.getText().trim();

        // Validate inputs
        try {
            if (updatedUsername.isEmpty() || updatedPassword.isEmpty() || confirmPassword.isEmpty()) {
                throw new IllegalArgumentException("All fields are required.");
            }

            if (!updatedPassword.equals(confirmPassword)) {
                throw new IllegalArgumentException("Passwords do not match.");
            }

            if (!UserInputValidator.isValidUsername(updatedUsername)) {
                throw new IllegalArgumentException("Invalid username. It must consist of a first and last name separated by a space.");
            }

            if (!UserInputValidator.isValidPassword(updatedPassword)) {
                throw new IllegalArgumentException("Invalid password. It must be at least 8 characters long and contain both letters and numbers.");
            }

            // Update the staff in the database
            UserService userService = new UserService();
            selectedStaff.setUsername(updatedUsername);
            selectedStaff.setPassword(updatedPassword);

            userService.updateUser(selectedStaff);

            // Reload staff data to reflect the updated details
            loadStaffData();

            // Clear input fields
            clearStaffForm();

            // Show success message
            showInfo("Staff updated successfully.");

        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (SQLException e) {
            showError("Failed to update staff: " + e.getMessage());
        }
    }

    /**
     * Clears the input fields in the staff form.
     */
    private void clearStaffForm() {
        addStaffUsernameText.clear();
        addStaffPassText.clear();
        addStaffConfirmText.clear();
    }
    @FXML
    void handleDeleteButton(ActionEvent event) {
        // Get the selected staff from the TableView
        Admin selectedStaff = StaffTableView.getSelectionModel().getSelectedItem();

        if (selectedStaff == null) {
            showError("No staff selected for deletion.");
            return;
        }

        // Confirm deletion
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Confirm Deletion");
        confirmationAlert.setHeaderText("Are you sure you want to delete the selected staff?");
        confirmationAlert.setContentText("This action cannot be undone.");
        confirmationAlert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        // Handle confirmation response
        ButtonType response = confirmationAlert.showAndWait().orElse(ButtonType.NO);
        if (response == ButtonType.NO) {
            return;
        }

        try {
            // Delete the staff from the database
            UserService userService = new UserService();
            userService.deleteUser(selectedStaff.getUserId());

            // Reload staff data to reflect the deletion
            loadStaffData();

            // Show success message
            showInfo("Staff deleted successfully.");
        } catch (SQLException e) {
            showError("Failed to delete staff: " + e.getMessage());
        }
    }


    //********************************************PRODUCT*****************************************************
    //private Product currentProduct; // Represents the product being edited or added
    @FXML
    void handleProductDelButton(ActionEvent event) {
        // Get the selected product from the TableView
        Product selectedProduct = productTableView.getSelectionModel().getSelectedItem();

        if (selectedProduct == null) {
            showError("No product selected for deletion.");
            return;
        }

        // Confirm deletion with the user
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Confirm Deletion");
        confirmationAlert.setHeaderText("Delete Product");
        confirmationAlert.setContentText("Are you sure you want to delete the selected product?");
        var result = confirmationAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Delete the product from the database
                ProductService productService = new ProductService();
                productService.deleteProduct(selectedProduct.getIdProduct());

                // Reload product data to reflect the deletion
                loadProductData();

                // Show success message
                showInfo("Product deleted successfully.");
                clearProductForm();
            } catch (SQLException e) {
                showError("Failed to delete product: " + e.getMessage());
            }
        }
    }
    @FXML
    void handleProductAddButton(ActionEvent event) {
        String name = productNameText.getText().trim();
        String description = productDescripText.getText().trim();
        String stock = productStockText.getText().trim();
        String price = productPriceText.getText().trim();
        String imagePath = imgPath.getText().trim();

        try {
            if (name.isEmpty() || description.isEmpty() || stock.isEmpty() || price.isEmpty()) {
                throw new IllegalArgumentException("All fields are required.");
            }
            if (!stock.matches("\\d+")) {
                throw new IllegalArgumentException("Stock must be a valid positive integer.");
            }
            if (!price.matches("\\d+(\\.\\d{1,2})?")) {
                throw new IllegalArgumentException("Price must be a valid number with up to two decimal places.");
            }

            Product newProduct = new Product();
            newProduct.setName(name);
            newProduct.setDescription(description);
            newProduct.setStockQuantity(Integer.parseInt(stock));
            newProduct.setPrice(Double.parseDouble(price));
            newProduct.setPimage(imagePath);

            productService.addProduct(newProduct); // Add product to database

           
                products.add(newProduct); // Add to ObservableList
                productTableView.refresh(); // Refresh TableView
                System.out.println("Product added to ObservableList. Total products: " + products.size());
            

            showInfo("Product added successfully.");
            clearProductForm();

        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (SQLException e) {
            showError("Failed to add product: " + e.getMessage());
        }
    }

  
    /**
     * Populates the product form with the details of the selected product.
     */
    private void populateProductForm(Product product) {
        if (product == null) {
            clearProductForm();
            return;
        }

        productNameText.setText(product.getProductName());
        productDescripText.setText(product.getDescription());
        productStockText.setText(String.valueOf(product.getStockQuantity()));
        productPriceText.setText(String.valueOf(product.getPrice()));
        imgPath.setText(product.getPimage());

        // Validate and set the image
        if (product.getPimage() != null && !product.getPimage().isBlank()) {
            try {
                Image productImage;
                if (product.getPimage().startsWith("http")) {
                    // If it's a URL, try to load it
                    productImage = new Image(product.getPimage(), true);
                } else {
                    // If it's a local file, validate its existence
                    File imageFile = new File(product.getPimage());
                    if (imageFile.exists()) {
                        productImage = new Image(imageFile.toURI().toString());
                    } else {
                        throw new IllegalArgumentException("File not found: " + product.getPimage());
                    }
                }
                productImageView.setImage(productImage);
            } catch (Exception e) {
                // Log and use a placeholder image if the provided image is invalid
                System.err.println("Error loading image: " + e.getMessage());
                productImageView.setImage(new Image("default.png")); // Use a default placeholder image
            }
        } else {
            // Clear the ImageView if no image is available
            productImageView.setImage(null);
        }
    }



    
    @FXML
    void handleProductUpdateButton(ActionEvent event) {
        // Retrieve the selected product from the TableView
        Product selectedProduct = productTableView.getSelectionModel().getSelectedItem();

        if (selectedProduct == null) {
            showError("No product selected for update.");
            return;
        }

        // Retrieve updated values from the text fields
        String updatedName = productNameText.getText().trim();
        String updatedDescription = productDescripText.getText().trim();
        String updatedStock = productStockText.getText().trim();
        String updatedPrice = productPriceText.getText().trim();
        String updatedImagePath = imgPath.getText().trim();
        try {
            // Validate input
            if (updatedName.isEmpty() || updatedDescription.isEmpty() || updatedStock.isEmpty() || updatedPrice.isEmpty()) {
                throw new IllegalArgumentException("All fields are required.");
            }

            if (!updatedStock.matches("\\d+")) {
                throw new IllegalArgumentException("Stock must be a valid positive integer.");
            }

            if (!updatedPrice.matches("\\d+(\\.\\d{1,2})?")) {
                throw new IllegalArgumentException("Price must be a valid number with up to two decimal places.");
            }

            // Update product details
            selectedProduct.setName(updatedName);
            selectedProduct.setDescription(updatedDescription);
            selectedProduct.setStockQuantity(Integer.parseInt(updatedStock));
            selectedProduct.setPrice(Double.parseDouble(updatedPrice));
            selectedProduct.setPimage(updatedImagePath);
            // Check if the image is updated
            /*if (productImageView.getImage() != null) {
            	String updatedImagePath = saveImageToDatabase(selectedProduct.getProductName(), productImageView.getImage());
                if (updatedImagePath != null) {
                    selectedProduct.setPimage(updatedImagePath); // Update the image path
                }
            }*/

            // Update the product in the database
            productService.updateProduct(selectedProduct);

            // Reload product data to reflect changes
            loadProductData();

            // Clear the input fields
            clearProductForm();

            // Show success message
            showInfo("Product updated successfully.");

        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (SQLException e) {
            showError("Failed to update product: " + e.getMessage());
        }
    }
    

    /**
     * Handles the image import button.
     */
    @FXML
    public void handleProductImageButton(ActionEvent event) {
        TextInputDialog inputDialog = new TextInputDialog();
        inputDialog.setTitle("Add Product Image");
        inputDialog.setHeaderText("Enter an HTTPS URL or select a local file");
        inputDialog.setContentText("Enter URL (or leave blank to select a file):");

        String imageUrl = inputDialog.showAndWait().orElse("");

        if (imageUrl != null && !imageUrl.isEmpty()) {
            // User provided a URL
            if (imageUrl.startsWith("http") || imageUrl.startsWith("https")) {
                try {
                    Image remoteImage = new Image(imageUrl, true);

                    if (remoteImage.isError()) {
                        showError("Failed to load the image from the provided URL.");
                        return;
                    }

                    // Set the HTTPS URL in the imgPath TextField
                    imgPath.setText(imageUrl);

                    // Display the image in the ImageView
                    productImageView.setImage(remoteImage);

                    showInfo("Image successfully loaded from URL.");
                } catch (Exception e) {
                    showError("Error loading image from URL: " + e.getMessage());
                }
            } else {
                showError("Invalid URL. Please provide a valid HTTPS URL.");
            }
        } else {
            // User wants to select a local file
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));

            File selectedFile = fileChooser.showOpenDialog(imgPath.getScene().getWindow());

            if (selectedFile != null) {
                try {
                    File destinationFolder = new File("images");
                    if (!destinationFolder.exists()) destinationFolder.mkdir();

                    File destinationFile = new File(destinationFolder, selectedFile.getName());
                    Files.copy(selectedFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                    // Set the local file's relative path in the imgPath TextField
                    imgPath.setText("images/" + selectedFile.getName());

                    // Display the image in the ImageView
                    productImageView.setImage(new Image(destinationFile.toURI().toString()));

                    showInfo("Image successfully loaded from file.");
                } catch (IOException e) {
                    showError("Error saving image: " + e.getMessage());
                }
            } else {
                showError("No file selected.");
            }
        }
    }


    /**
     * Clears the input fields in the product form.
     */
    private void clearProductForm() {
    	productNameText.clear();
        productDescripText.clear();
        productStockText.clear();
        productPriceText.clear();
        imgPath.clear();
        productImageView.setImage(null);
    }
    
    @FXML
    void clearProductForm(ActionEvent event) {
    	clearProductForm();
    }
    
    
    
    //**********************************************SEARCH**************************************
    @FXML
    private TextField SearchText; // Input field for search query
   
    /**
     * Handles search functionality across clients, products, or orders based on user input.
     */
    @FXML
    private void onSearch() {
        String searchQuery = SearchText.getText().trim();

        // Validate input
        if (searchQuery.isEmpty()) {
            searchResult.setText("Please enter a search term.");
            return;
        }

        try {
            // Search for clients by username
            List<Client> clients = searchService.searchUserByUsername(searchQuery).stream()
                    .filter(Client.class::isInstance)
                    .map(Client.class::cast)
                    .toList();

            // Search for products by name
            List<Product> products = searchService.searchProductsByName(searchQuery);

            // Build the result text
            StringBuilder resultText = new StringBuilder();

            if (!clients.isEmpty()) {
                resultText.append("Clients Found:\n")
                          .append(clients.stream()
                                         .map(this::formatClientInfo)
                                         .reduce((a, b) -> a + "\n" + b)
                                         .orElse(""))
                          .append("\n\n");
            }

            if (!products.isEmpty()) {
                resultText.append("Products Found:\n")
                          .append(products.stream()
                                         .map(this::formatProductInfo)
                                         .reduce((a, b) -> a + "\n" + b)
                                         .orElse(""))
                          .append("\n\n");
            }

            // Set the result text or show a "No results" message
            if (resultText.length() == 0) {
                searchResult.setText("No results found for: " + searchQuery);
            } else {
                searchResult.setText(resultText.toString().trim());
            }
        } catch (SQLException e) {
            searchResult.setText("Search failed: " + e.getMessage());
        }
    }


   

    //********************************************LOGIN********************************************
    private User loggedInUser; // Store the logged-in user
    
    //@SuppressWarnings("unused")
    //@FXML
    public void setLoggedInUser(User user) {
        //AuthentificationService authService = new AuthentificationService();

       
            // Store the logged-in user
            //this.loggedInUser = user;

            // Personalize the button text with the user's full name and role
            String userRole = loggedInUser instanceof Admin ? "Admin" : "Client";
            adminAccount.setText(user.getUsername());

            // Optionally, perform further actions such as redirecting the user to another page
            System.out.println("Login successful for: " + user.getUsername() + " (" + userRole + ")");
       
    }
    
   
    
    //********************************************LOGOUT********************************************
    @FXML
    public void handleLogout(ActionEvent event) {
        // Show confirmation alert
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout Confirmation");
        alert.setHeaderText("Are you sure you want to log out?");
        alert.setContentText("You will be redirected to the welcome page.");
        
        // Wait for user response
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            System.out.println("Admin logged out.");
            
            // Load the login page
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/WelcomePage.fxml"));
                Parent loginPage = loader.load();

                // Get the current stage and set the login scene
                Stage stage = (Stage) adminLogout.getScene().getWindow();
                Scene scene = new Scene(loginPage);
                stage.setScene(scene);
                stage.setTitle("Welcome Page");
                stage.centerOnScreen();
                stage.setResizable(false);
                stage.show();
            } catch (IOException e) {
                showError("Failed to load the page: " + e.getMessage());
            }
        } else {
            // User cancelled logout
            System.out.println("Logout cancelled.");
        }
    }


    
    
    ////////////////////////////////////////////////////
    
    /**
     * Formats client information for display.
     *
     * @param client The client to format.
     * @return A formatted string with client details.
     */
    private String formatClientInfo(Client client) {
        if (client == null) {
            return "Client information not available.";
        }
        StringBuilder clientInfo = new StringBuilder();
        clientInfo.append("Full Name: ").append(client.getUsername() != null ? client.getUsername() : "N/A")
                  .append(", Email: ").append(client.getEmail() != null && !client.getEmail().isEmpty() ? client.getEmail() : "N/A")
                  .append(", Phone: ").append(client.getPhone() != null && !client.getPhone().isEmpty() ? client.getPhone() : "N/A")
                  .append(", Address: ").append(client.getAddress() != null && !client.getAddress().isEmpty() ? client.getAddress() : "N/A");

        return clientInfo.toString();
    }

    /**
     * Formats product information for display.
     *
     * @param product The product to format.
     * @return A formatted string with product details.
     */
    private String formatProductInfo(Product product) {
        return "ID: " + product.getIdProduct() + ", Name: " + product.getProductName() +
               ", Price: $" + product.getPrice() + ", Stock: " + product.getStockQuantity() +
               ", Description: " + product.getDescription();
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
   

}
