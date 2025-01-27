/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.distributed_assessment1;

/**
 *
 * @author shubh
 */
public class OlderCouple extends Person {
    private int yearsMarried;

    public OlderCouple(String spouse1Name, String spouse2Name, String phoneNumber, String email, String address, int yearsMarried) {
        super(spouse1Name, spouse2Name, phoneNumber, email, address);
        this.yearsMarried = yearsMarried;
    }

    public int getYearsMarried() {
        return yearsMarried;
    }

    @Override
    public String toString() {
        return super.toString() + ", Years Married: " + yearsMarried;
    }
}
