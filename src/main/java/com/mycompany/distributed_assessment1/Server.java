package com.mycompany.distributed_assessment1;

import java.io.*;
import java.net.*;
import java.security.*;
import java.security.spec.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;

public class Server {
    private static final int PORT = 8080;
    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    private PrivateKey privateKey;
    private PublicKey publicKey;

    public Server() {
        try {
            generateKeyPair();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                threadPool.submit(() -> {
                    try {
                        handleClient(clientSocket);
                    } catch (Exception ex) {
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            threadPool.shutdown();
        }
    }
    
    
    private void generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        privateKey = keyPair.getPrivate();
        publicKey = keyPair.getPublic();
    }

 
    private String decryptPassword(String encryptedPassword) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedPassword));
        return new String(decryptedBytes);
    }
    
    
    private void sendPublicKey(ObjectOutputStream output) throws IOException {
        output.writeObject(Base64.getEncoder().encodeToString(publicKey.getEncoded()));
        output.flush();
    }

  
    private void handleClient(Socket clientSocket) throws Exception {
        try (
            ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream());
            ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream())
        ) {
            System.out.println("Connected to client: " + clientSocket.getInetAddress());
            sendPublicKey(output);
            
            while (true) {
                try {
                    String requestType = (String) input.readObject();
                    System.out.println("Request received: " + requestType);

                    switch (requestType) {
                        // a) Register Older Couple
                        case "REGISTER_OLDER_COUPLE":
                            registerOlderCouple(input, output);
                            break;

                        // b) Register Young Family
                        case "REGISTER_YOUNG_FAMILY":
                            registerYoungFamily(input, output);
                            break;

                        // c) Login and Validate User
                        case "LOGIN_VIEW_DETAILS":
                            loginAndViewDetails(input, output);
                            break;

                        // d) View All FIDNs
                        case "VIEW_FIDNS":
                            viewFIDNs(output);
                            break;

                        // e) View Specific Member Details
                        case "VIEW_MEMBER_DETAILS":
                            viewMemberDetails(input, output);
                            break;

                        // f) Create Event
                        case "CREATE_EVENT":
                            createEvent(input, output);
                            break;

                        // g) View All Events
                        case "VIEW_EVENTS":
                            viewEvents(output);
                            break;

                        default:
                            output.writeObject("Invalid Request.");
                    }
                } catch (EOFException e) {
                    System.out.println("Client disconnected.");
                    break;
                } catch (ClassNotFoundException | IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private synchronized void registerOlderCouple(ObjectInputStream input, ObjectOutputStream output) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection()) {
            OlderCouple couple = (OlderCouple) input.readObject();
            String encryptedPassword = (String) input.readObject();
            
            System.out.println(encryptedPassword);
            String password = decryptPassword(encryptedPassword);
            System.out.println(password);
            String fidn = generateFIDN(couple.getSpouse1Name(), couple.getSpouse2Name());

            String sql = "INSERT INTO older_couple (fidn, spouse1_name, spouse2_name, phone, email, address, years_married, password) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, fidn);
            pstmt.setString(2, couple.getSpouse1Name());
            pstmt.setString(3, couple.getSpouse2Name());
            pstmt.setString(4, couple.getPhoneNumber());
            pstmt.setString(5, couple.getEmail());
            pstmt.setString(6, couple.getAddress());
            pstmt.setInt(7, couple.getYearsMarried());
            pstmt.setString(8, password);

            pstmt.executeUpdate();
            output.writeObject("Older Couple Registered Successfully. FIDN: " + fidn);
        } catch (SQLException e) {
            e.printStackTrace();
            output.writeObject("Database Error: " + e.getMessage());
        }
    }


    private synchronized void registerYoungFamily(ObjectInputStream input, ObjectOutputStream output) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection()) {
            YoungFamily family = (YoungFamily) input.readObject();
            String encryptedPassword = (String) input.readObject();
            String password = decryptPassword(encryptedPassword);
            String fidn = generateFIDN(family.getSpouse1Name(), family.getSpouse2Name());

            String sql = "INSERT INTO young_family (fidn, spouse1_name, spouse2_name, phone, email, address, password) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, fidn);
            pstmt.setString(2, family.getSpouse1Name());
            pstmt.setString(3, family.getSpouse2Name());
            pstmt.setString(4, family.getPhoneNumber());
            pstmt.setString(5, family.getEmail());
            pstmt.setString(6, family.getAddress());
            pstmt.setString(7, password);

            pstmt.executeUpdate();
            output.writeObject("Young Family Registered Successfully. FIDN: " + fidn);
        } catch (SQLException e) {
            e.printStackTrace();
            output.writeObject("Database Error: " + e.getMessage());
        }
    }

    private void loginAndViewDetails(ObjectInputStream input, ObjectOutputStream output) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String email = (String) input.readObject();
            String encryptedPassword = (String) input.readObject();
            System.out.println(encryptedPassword);
            String password = decryptPassword(encryptedPassword);
            System.out.println(password);

            // Verify in older_couple table
            String sqlOlder = "SELECT * FROM older_couple WHERE email = ? AND password = ?";
            PreparedStatement pstmtOlder = conn.prepareStatement(sqlOlder);
            pstmtOlder.setString(1, email);
            pstmtOlder.setString(2, password);
            ResultSet rsOlder = pstmtOlder.executeQuery();

            if (rsOlder.next()) {
                output.writeObject("Login successful for: " + rsOlder.getString("spouse1_name") + " and " + rsOlder.getString("spouse2_name"));
                return;
            }

            String sqlYoung = "SELECT * FROM young_family WHERE email = ? AND password = ?";
            PreparedStatement pstmtYoung = conn.prepareStatement(sqlYoung);
            pstmtYoung.setString(1, email);
            pstmtYoung.setString(2, password);
            ResultSet rsYoung = pstmtYoung.executeQuery();

            if (rsYoung.next()) {
                output.writeObject("Login successful for: " + rsYoung.getString("spouse1_name") + " and " + rsYoung.getString("spouse2_name"));
            } else {
                output.writeObject("Incorrect credentials. Login failed.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            output.writeObject("Database error: " + e.getMessage());
        }
    }


    private void viewFIDNs(ObjectOutputStream output) throws IOException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT fidn FROM older_couple UNION SELECT fidn FROM young_family";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            List<String> fidns = new ArrayList<>();
            while (rs.next()) {
                fidns.add(rs.getString("fidn"));
            }
            output.writeObject(fidns);
        } catch (SQLException e) {
            e.printStackTrace();
            output.writeObject("Database error: " + e.getMessage());
        }
    }
    
    
    
    private void viewMemberDetails(ObjectInputStream input, ObjectOutputStream output) throws IOException, ClassNotFoundException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String fidn = (String) input.readObject();
            boolean memberFound = false;
            StringBuilder result = new StringBuilder();

            // Query older_couple table
            String sqlOlder = "SELECT spouse1_name, spouse2_name, phone, email, address, years_married FROM older_couple WHERE fidn = ?";
            try (PreparedStatement pstmtOlder = conn.prepareStatement(sqlOlder)) {
                pstmtOlder.setString(1, fidn);
                ResultSet rsOlder = pstmtOlder.executeQuery();
                if (rsOlder.next()) {
                    result.append("Older Couple Details:\n");
                    result.append("Spouse 1: ").append(rsOlder.getString("spouse1_name")).append("\n");
                    result.append("Spouse 2: ").append(rsOlder.getString("spouse2_name")).append("\n");
                    result.append("Phone: ").append(rsOlder.getString("phone")).append("\n");
                    result.append("Email: ").append(rsOlder.getString("email")).append("\n");
                    result.append("Address: ").append(rsOlder.getString("address")).append("\n");
                    result.append("Years Married: ").append(rsOlder.getInt("years_married")).append("\n");
                    memberFound = true;
                }
            }

            // Query young_family table
            String sqlYoung = "SELECT spouse1_name, spouse2_name, phone, email, address FROM young_family WHERE fidn = ?";
            try (PreparedStatement pstmtYoung = conn.prepareStatement(sqlYoung)) {
                pstmtYoung.setString(1, fidn);
                ResultSet rsYoung = pstmtYoung.executeQuery();
                if (rsYoung.next()) {
                    result.append("\nYoung Family Details:\n");
                    result.append("Spouse 1: ").append(rsYoung.getString("spouse1_name")).append("\n");
                    result.append("Spouse 2: ").append(rsYoung.getString("spouse2_name")).append("\n");
                    result.append("Phone: ").append(rsYoung.getString("phone")).append("\n");
                    result.append("Email: ").append(rsYoung.getString("email")).append("\n");
                    result.append("Address: ").append(rsYoung.getString("address")).append("\n");
                    memberFound = true;

                    // Query children for the young family
                    String sqlChildren = "SELECT child_name, age, gender FROM children WHERE fidn = ?";
                    try (PreparedStatement pstmtChildren = conn.prepareStatement(sqlChildren)) {
                        pstmtChildren.setString(1, fidn);
                        ResultSet rsChildren = pstmtChildren.executeQuery();

                        result.append("\nChildren Details:\n");
                        while (rsChildren.next()) {
                            result.append("Name: ").append(rsChildren.getString("child_name")).append(", ");
                            result.append("Age: ").append(rsChildren.getInt("age")).append(", ");
                            result.append("Gender: ").append(rsChildren.getString("gender")).append("\n");
                        }
                    }
                }
            }


            if (memberFound) {
                output.writeObject(result.toString());
            } else {
                output.writeObject("No member found with the given FIDN.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            output.writeObject("Database error: " + e.getMessage());
        }
    }



    private void viewEvents(ObjectOutputStream output) {
        List<String> events = new ArrayList<>();
        String sql = "SELECT event_type, event_date, event_time, event_duration, event_venue, event_cost FROM event";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String event = String.format(
                        "Type: %s, Date: %s, Time: %s, Duration: %s, Venue: %s, Cost: %s",
                        rs.getString("event_type"),
                        rs.getString("event_date"),
                        rs.getString("event_time"),
                        rs.getString("event_duration"),
                        rs.getString("event_venue"),
                        rs.getString("event_cost")
                );
                events.add(event);
            }
            output.writeObject(events);
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }



    private void createEvent(ObjectInputStream input, ObjectOutputStream output) throws IOException, ClassNotFoundException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String eventType = (String) input.readObject();
            String eventDate = (String) input.readObject();
            String eventTime = (String) input.readObject();
            int eventDuration = (int) input.readObject();
            String eventVenue = (String) input.readObject();
            double eventCost = (double) input.readObject();

            String sql = "INSERT INTO event (event_type, event_date, event_time, event_duration, event_venue, event_cost) VALUES (?, ?, ?, ?, ?, ?)";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, eventType);
                pstmt.setString(2, eventDate);
                pstmt.setString(3, eventTime);
                pstmt.setInt(4, eventDuration);
                pstmt.setString(5, eventVenue);
                pstmt.setDouble(6, eventCost);

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    output.writeObject("Event created successfully!");
                } else {
                    output.writeObject("Failed to create the event.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            output.writeObject("Database error: " + e.getMessage());
        }
    }



    private String generateFIDN(String spouse1, String spouse2) {
        return "FIDN_" + (spouse1 + spouse2).hashCode();
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.startServer();
    }
}
