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

public class Child implements Serializable {
    private String name;
    private int age;
    private String gender;

    public Child(String name, int age, String gender) {
        this.name = name;
        this.age = age;
        this.gender = gender;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public String getGender() {
        return gender;
    }

    @Override
    public String toString() {
        return "Child(Name: " + name + ", Age: " + age + ", Gender: " + gender + ")";
    }
}