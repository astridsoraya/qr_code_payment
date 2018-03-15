package com.example.asusa455la.zxingembedded.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;

/**
 * Created by ASUS A455LA on 07/12/2017.
 */

@Entity
public class Merchant extends User{
    private String namaMerchant;
    private String address;

    public Merchant(String idUser, String merchantName, String emailAddress, String address
                    , String handphoneNumber){
        super(idUser, emailAddress, handphoneNumber);
        this.namaMerchant = merchantName;
        this.address = address;
    }

    public String getNamaMerchant(){
        return namaMerchant;
    }

    public String getAddress(){
        return address;
    }
}
