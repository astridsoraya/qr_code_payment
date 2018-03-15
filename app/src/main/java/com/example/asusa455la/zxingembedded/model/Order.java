package com.example.asusa455la.zxingembedded.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * Created by ASUS A455LA on 16/01/2018.
 */

@Entity (primaryKeys = {"idOrder"},
        foreignKeys = {
        @ForeignKey(
                entity = Merchant.class,
                parentColumns = "idUser",
                childColumns = "idMerchant"
        )},
        indices = @Index("idMerchant"))
public class Order {
    private int idOrder;
    private String waktuOrder;


    private String idMerchant;


    public Order(int idOrder, String waktuOrder, String idMerchant){
        this.idOrder = idOrder;
        this.waktuOrder = waktuOrder;
        this.idMerchant = idMerchant;
    }

    public int getIdOrder(){
        return idOrder;
    }

    public String getWaktuOrder(){
        return waktuOrder;
    }

    public String getIdMerchant(){
        return idMerchant;
    }
}
