package com.example.asusa455la.zxingembedded.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.support.annotation.NonNull;

/**
 * Created by ASUS A455LA on 07/12/2017.
 */

@Entity (primaryKeys = {"idUser"}, indices = {@Index(value = {"emailAddress"},
        unique = true)})
public abstract class User {
    @NonNull
    protected String idUser;

    @NonNull
    protected String emailAddress;
    protected String handphoneNumber;

    public User(String idUser, String emailAddress, String handphoneNumber){
        this.idUser = idUser;
        this.emailAddress = emailAddress;
        this.handphoneNumber = handphoneNumber;
    }

    public String getIdUser(){
        return idUser;
    }

    public String getEmailAddress(){
        return emailAddress;
    }

    public String getHandphoneNumber(){
        return handphoneNumber;
    }

}
