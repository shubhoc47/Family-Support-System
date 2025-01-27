/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.distributed_assessment1;
import java.io.Serializable;
/**
 *
 * @author shubh
 */
public class Person implements Serializable {
    private String spouse1Name;
    private String spouse2Name;
    private String phoneNumber;
    private String email;
    private String address;

    public Person(String spouse1Name, String spouse2Name, String phoneNumber, String email, String address) {
        this.spouse1Name = spouse1Name;
        this.spouse2Name = spouse2Name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.address = address;
    }

    public String getSpouse1Name() {
        return spouse1Name;
    }

    public String getSpouse2Name() {
        return spouse2Name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }

    @Override
    public String toString() {
        return "Spouse1: " + spouse1Name + ", Spouse2: " + spouse2Name + ", Phone: " + phoneNumber + ", Email: " + email + ", Address: " + address;
    }
}