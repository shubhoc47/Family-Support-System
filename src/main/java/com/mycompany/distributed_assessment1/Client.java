package com.mycompany.distributed_assessment1;

import java.io.*;
import java.net.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;
import java.util.Base64;
import java.util.*;

public class Client {
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 8080;
    private Socket socket;
    public ObjectOutputStream output;
    public ObjectInputStream input;
    private PublicKey serverPublicKey;
    

    public Client() {
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
            System.out.println("Connected to the server.");
            
            String publicKeyString = (String) input.readObject();
            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyString);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
            serverPublicKey = keyFactory.generatePublic(keySpec);
            System.out.println("Received server's public key.");
        } catch (IOException | ClassNotFoundException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }
    
    
    private String encryptPassword(String password) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, serverPublicKey);
            byte[] encryptedBytes = cipher.doFinal(password.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Register an Older Couple with Password Validation.
     */
    public void registerOlderCouple(String spouse1, String spouse2, String phone, String email, String address, int yearsMarried, String password) {
        if (!isValidPassword(password)) {
            System.out.println("Invalid password. Must be 10 characters long with letters and numbers.");
            return;
        }

        try {
            output.writeObject("REGISTER_OLDER_COUPLE");
            OlderCouple couple = new OlderCouple(spouse1, spouse2, phone, email, address, yearsMarried);
            output.writeObject(couple);

            // Encrypt password before sending to server
            String encryptedPassword = encryptPassword(password);
            output.writeObject(encryptedPassword);

            String response = (String) input.readObject();
            System.out.println("Server Response: " + response);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Register a Young Family with encrypted password and child details
     */
    public void registerYoungFamily(String spouse1, String spouse2, String phone, String email, String address, List<Child> children, String password) {
        if (!isValidPassword(password)) {
            System.out.println("Invalid password. Must be 10 characters long with letters and numbers.");
            return;
        }

        try {
            output.writeObject("REGISTER_YOUNG_FAMILY");
            YoungFamily family = new YoungFamily(spouse1, spouse2, phone, email, address, children);
            output.writeObject(family);

            // Encrypt password before sending to server
            String encryptedPassword = encryptPassword(password);
            output.writeObject(encryptedPassword);

            String response = (String) input.readObject();
            System.out.println("Server Response: " + response);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    
    public String loginAndViewDetails(String fidn, String password) {
        try {
            output.writeObject("LOGIN_VIEW_DETAILS");
            output.writeObject(fidn);

            // Encrypt password before sending to the server
            String encryptedPassword = encryptPassword(password);
            output.writeObject(encryptedPassword);

            String response = (String) input.readObject();
            return response;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return "Error during login.";
        }
    }


    
    public List<String> viewFIDNs() {
        try {
            output.writeObject("VIEW_FIDNS");
            Object response = input.readObject();

            if (response instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> fidns = (List<String>) response;
                return fidns;
            } else {
                System.err.println("Invalid server response for FIDNs.");
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

 
    
    public String viewMemberDetails(String fidn) {
        try {
            output.writeObject("VIEW_MEMBER_DETAILS");
            output.writeObject(fidn);

            String response = (String) input.readObject();
            return response;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return "Error fetching member details.";
        }
    }

    public void createEvent(String eventType, String eventDate, String eventTime, int eventDuration, String eventVenue, double eventCost) {
        try {
            output.writeObject("CREATE_EVENT");
            output.writeObject(eventType);
            output.writeObject(eventDate);
            output.writeObject(eventTime);
            output.writeObject(eventDuration);
            output.writeObject(eventVenue);
            output.writeObject(eventCost);

            String response = (String) input.readObject();
            System.out.println("Server Response: " + response);
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error sending event details to the server: " + e.getMessage());
        }
    }

    
    
    public String viewSpecificFIDN(String fidn) {
        try {
            output.writeObject("VIEW_MEMBER_DETAILS");  // Send the request type to the server
            output.writeObject(fidn);  // Send the FIDN to the server for lookup

            // Wait for the server response
            String response = (String) input.readObject();
            return response;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return "Error retrieving specific FIDN details: " + e.getMessage();
        }
    }
    
    
    public List<String> viewEvents() {
           try {
               output.writeObject("VIEW_EVENTS");
               Object response = input.readObject();
               if (response instanceof List) {
                   @SuppressWarnings("unchecked")
                   List<String> events = (List<String>) response;
                   return events;
               } else {
                   System.err.println("Invalid server response for events.");
               }
           } catch (IOException | ClassNotFoundException e) {
               e.printStackTrace();
           }
           return new ArrayList<>();
       }



    /**
     * Close the client-server connection
     */
    public void closeConnection() {
        try {
            output.close();
            input.close();
            socket.close();
            System.out.println("Connection Closed.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    
    private boolean isValidPassword(String password) {
        return password.length() >= 10 && password.matches("^(?=.*[a-zA-Z])(?=.*\\d).+$");
    }

    public static void main(String[] args) {
        Client client = new Client();
        javafx.application.Application.launch(App.class);
        System.out.println("Client Running...");
    }
}
