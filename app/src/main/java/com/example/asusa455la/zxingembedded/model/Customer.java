package com.example.asusa455la.zxingembedded.model;

import android.arch.persistence.room.Entity;

/**
 * Created by ASUS A455LA on 07/12/2017.
 */

@Entity
public class Customer extends User{
    private String firstName;
    private String lastName;

    public Customer(String idUser, String firstName, String lastName, String emailAddress, String handphoneNumber){
        super(idUser, emailAddress, handphoneNumber);
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getFirstName(){
        return firstName;
    }

    public String getLastName(){
        return lastName;
    }
}
