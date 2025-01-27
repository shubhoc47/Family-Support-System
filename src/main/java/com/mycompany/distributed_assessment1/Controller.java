package com.mycompany.distributed_assessment1;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.*;

public class Controller {

    @FXML
    private TextField spouse1Field;

    @FXML
    private TextField spouse2Field;

    @FXML
    private TextField phoneField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField addressField;

    @FXML
    private TextField yearsMarriedField;
    
    @FXML
    private TextField searchFidnField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextArea childrenField;

    @FXML
    private TextField eventTypeField;

    @FXML
    private TextField eventDateField;

    @FXML
    private TextField eventTimeField;

    @FXML
    private TextField eventDurationField;

    @FXML
    private TextField eventVenueField;

    @FXML
    private TextField eventCostField;

    

    @FXML
    private Button registerOlderButton;

    @FXML
    private Button registerYoungButton;

    @FXML
    private Button viewFidnsButton;

    @FXML
    private Button viewDetailsButton;
    
    @FXML
    private Button createEventButton;

    @FXML
    private TextField fidnField;

    @FXML
    private PasswordField loginPasswordField;

    @FXML
    private TextArea detailsArea;
    
    private Client client;
    private boolean isLoggedIn = false;  // **New flag for login check**

    @FXML
    public void initialize() {
        client = new Client();

        registerOlderButton.setOnAction(event -> registerOlderCouple());
        registerYoungButton.setOnAction(event -> registerYoungFamily());
        viewFidnsButton.setOnAction(event -> viewFidns());
        viewDetailsButton.setOnAction(event -> loginAndViewDetails());
        createEventButton.setOnAction(event -> createEvent());
    }

   
    
    private boolean isValidPassword(String password) {
        return password != null && password.length() >= 10 && password.matches(".*[a-zA-Z].*") && password.matches(".*[0-9].*");
    }


  
    private void registerOlderCouple() {
        try {
            String spouse1 = spouse1Field.getText();
            String spouse2 = spouse2Field.getText();
            String phone = phoneField.getText();
            String email = emailField.getText();
            String address = addressField.getText();
            int yearsMarried = Integer.parseInt(yearsMarriedField.getText());
            String password = passwordField.getText();
            
            System.out.println("shubho");

            if (!isValidPassword(password)) {
                detailsArea.setText("Password must be exactly 10 characters long and contain both letters and numbers.");
                return;
            }

            client.registerOlderCouple(spouse1, spouse2, phone, email, address, yearsMarried, password);
            detailsArea.setText("Older Couple Registered Successfully!");
        } catch (Exception e) {
            detailsArea.setText("Error: " + e.getMessage());
        }
    }

    // Register Young Family with Password
    private void registerYoungFamily() {
        try {
            String spouse1 = spouse1Field.getText();
            String spouse2 = spouse2Field.getText();
            String phone = phoneField.getText();
            String email = emailField.getText();
            String address = addressField.getText();
            String password = passwordField.getText();

            if (!isValidPassword(password)) {
                detailsArea.setText("Password must be at least 10 characters long and contain both letters and numbers.");
                return;
            }

            List<Child> children = new ArrayList<>();
            String[] childrenInfo = childrenField.getText().split(";\s*");
            for (String childData : childrenInfo) {
                String[] parts = childData.split(",\s*");
                String name = parts[0];
                int age = Integer.parseInt(parts[1]);
                String gender = parts[2];
                children.add(new Child(name, age, gender));
            }

            client.registerYoungFamily(spouse1, spouse2, phone, email, address, children, password);
            detailsArea.setText("Young Family Registered Successfully!");
        } catch (Exception e) {
            detailsArea.setText("Error: " + e.getMessage());
        }
    }

 
    
    private void viewFidns() {
        if (!isLoggedIn) {
            detailsArea.setText("You must be logged in to view FIDNs.");
            return;
        }

        try {
            List<String> fidns = client.viewFIDNs();
            if (fidns.isEmpty()) {
                detailsArea.setText("No FIDNs found.");
            } else {
                StringBuilder fidnDisplay = new StringBuilder("Available FIDNs:\n");
                for (String fidn : fidns) {
                    fidnDisplay.append(fidn).append("\n");
                }
                detailsArea.setText(fidnDisplay.toString());
            }
        } catch (Exception e) {
            detailsArea.setText("Error retrieving FIDNs: " + e.getMessage());
        }
    }

    
    @FXML
    private void loginAndViewDetails() {
        try {
            String email = fidnField.getText().trim();
            String password = loginPasswordField.getText().trim();

            if (email.isEmpty() || password.isEmpty()) {
                detailsArea.setText("Please enter both Email and Password.");
                return;
            }

            String response = client.loginAndViewDetails(email, password);
            if (!response.contains("Invalid") && !response.equals("FIDN not found.")) {
                detailsArea.setText("Login successful!\n" + response.replace(", ", "\n"));
                isLoggedIn = true;  // **User is now logged in**
            } else {
                detailsArea.setText(response);
                isLoggedIn = false;  // **Login failed, restrict actions**
            }
        } catch (Exception e) {
            detailsArea.setText("Error during login and viewing details: " + e.getMessage());
        }
    }
    
    @FXML
     private void searchSpecificFidn() {
        if (!isLoggedIn) {
            detailsArea.setText("You must be logged in to search for specific FIDNs.");
            return;
        }

        try {
            String fidn = searchFidnField.getText().trim();
            if (fidn.isEmpty()) {
                detailsArea.setText("Please enter a valid FIDN to search.");
                return;
            }

            String result = client.viewSpecificFIDN(fidn);
            detailsArea.setText(result);
        } catch (Exception e) {
            detailsArea.setText("Error searching for FIDN: " + e.getMessage());
        }
    }

    @FXML
    private void createEvent() {
        try {
            String eventType = eventTypeField.getText().trim();
            String eventDate = eventDateField.getText().trim();
            String eventTime = eventTimeField.getText().trim();
            int eventDuration = Integer.parseInt(eventDurationField.getText().trim());
            String eventVenue = eventVenueField.getText().trim();
            double eventCost = Double.parseDouble(eventCostField.getText().trim());

            if (eventType.isEmpty() || eventDate.isEmpty() || eventTime.isEmpty() || eventVenue.isEmpty()) {
                detailsArea.setText("Please fill in all the event details.");
                return;
            }

            // Send event details to the server via client
            client.createEvent(eventType, eventDate, eventTime, eventDuration, eventVenue, eventCost);
            detailsArea.setText("Event Created Successfully!");

        } catch (Exception e) {
            detailsArea.setText("Error while creating event: " + e.getMessage());
        }
    }

    
    @FXML
    private void viewEvents() {
            try {
                List<String> events = client.viewEvents();
                if (events.isEmpty()) {
                    detailsArea.setText("No events found.");
                } else {
                    StringBuilder eventDisplay = new StringBuilder("Available Events:\n");
                    for (String event : events) {
                        eventDisplay.append(event).append("\n");
                    }
                    detailsArea.setText(eventDisplay.toString());
                }
            } catch (Exception e) {
                detailsArea.setText("Error retrieving events: " + e.getMessage());
            }
    }

    @FXML
    public void shutdown() {
        client.closeConnection();
    }
}
