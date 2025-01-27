/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.distributed_assessment1;
import java.io.*;
import java.util.*;
/**
 *
 * @author shubh
 */

public class FileHandler {
    private static final String FILE_NAME = "members.dat";


    static {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    System.out.println("File created: " + file.getAbsolutePath());
                }
            } catch (IOException e) {
                System.err.println("Error creating file: " + e.getMessage());
            }
        } else {
            System.out.println("File exists at: " + file.getAbsolutePath());
        }
    }

 
    public static synchronized void writeToFile(Object data) {
        String dat = data.toString();
//        System.out.println("Printing: ");
//        System.out.println(data);
        try (
            FileWriter writer = new FileWriter(FILE_NAME, true);
            BufferedWriter bufferedWriter = new BufferedWriter(writer)
        ) {
            bufferedWriter.write(dat);
            bufferedWriter.newLine(); // Add a newline after each entry
            System.out.println("Data written to file: " + dat);
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }



    // Helper method to check if the file is empty
    private static boolean isFileEmpty(String fileName) {
        File file = new File(fileName);
        return file.length() == 0;
    }

    // Read all FIDNs from the file
    public static synchronized List<String> readFidns() {
        List<String> fidns = new ArrayList<>();

        System.out.println("Reading FIDNs from file content...");
        try (
            FileReader fr = new FileReader(FILE_NAME);
            BufferedReader br = new BufferedReader(fr)
        ) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println("Read line from file: " + line);

                // Extract spouse1 and spouse2 from the string
                String spouse1 = extractField(line, "Spouse1: ", ",");
                String spouse2 = extractField(line, "Spouse2: ", ",");

                // Generate FIDN and add to the list
                if (spouse1 != null && spouse2 != null) {
                    String fidn = generateFIDN(spouse1, spouse2);
                    fidns.add(fidn);
                } else {
                    System.err.println("Could not extract Spouse1 and Spouse2 from: " + line);
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error reading FIDNs: " + e.getMessage());
        }

        System.out.println("Finished reading FIDNs. Generated FIDNs:");
        for (String fidn : fidns) {
            System.out.println(fidn);
        }

        return fidns;
    }

    private static String extractField(String line, String key, String delimiter) {
        int start = line.indexOf(key);
        if (start == -1) {
            return null; // Key not found
        }
        start += key.length();
        int end = line.indexOf(delimiter, start);
        return end == -1 ? line.substring(start).trim() : line.substring(start, end).trim();
    }


    // Read specific member details based on FIDN
    public static synchronized String readMemberDetails(String fidn) {
        System.out.println("Reading member details for FIDN: " + fidn);
        try (
            FileReader fr = new FileReader(FILE_NAME);
            BufferedReader br = new BufferedReader(fr)
        ) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println("Read line from file: " + line);

                // Extract spouse1 and spouse2 from the string
                String spouse1 = extractField(line, "Spouse1: ", ",");
                String spouse2 = extractField(line, "Spouse2: ", ",");

                // Generate FIDN and compare with the input FIDN
                if (spouse1 != null && spouse2 != null) {
                    String generatedFIDN = generateFIDN(spouse1, spouse2);
                    if (generatedFIDN.equals(fidn)) {
                        return line; // Return the full line as the member details
                    }
                } else {
                    System.err.println("Could not extract Spouse1 and Spouse2 from: " + line);
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error reading member details: " + e.getMessage());
        }

        System.out.println("No member found with FIDN: " + fidn);
        return null; // Return null if no matching FIDN is found
    }


    // Generate FIDN based on spouse names
    private static String generateFIDN(String spouse1, String spouse2) {
        String fidn = "FIDN_" + (spouse1 + spouse2).hashCode();
        System.out.println("Generated FIDN for " + spouse1 + " and " + spouse2 + ": " + fidn);
        return fidn;
    }

    // Custom ObjectOutputStream to prevent header overwrite
    private static class AppendingObjectOutputStream extends ObjectOutputStream {
        public AppendingObjectOutputStream(OutputStream out) throws IOException {
            super(out);
        }

        @Override
        protected void writeStreamHeader() throws IOException {
            reset();
        }
    }
    
    private static boolean validateFileIntegrity() {
        try (FileInputStream fis = new FileInputStream(FILE_NAME);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            while (true) {
                Object data = ois.readObject(); // Try reading the file
            }
        } catch (EOFException e) {
            return true; // End of file is valid
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("File validation failed: " + e.getMessage());
            return false; // File is corrupted or invalid
        }
    }

}
