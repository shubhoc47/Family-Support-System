/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.distributed_assessment1;

import java.util.List;

/**
 *
 * @author shubh
 */

public class YoungFamily extends Person {
    private List<Child> children;

    public YoungFamily(String spouse1Name, String spouse2Name, String phoneNumber, String email, String address, List<Child> children) {
        super(spouse1Name, spouse2Name, phoneNumber, email, address);
        this.children = children;
    }

    public List<Child> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        return super.toString() + ", Children: " + children;
    }
}
