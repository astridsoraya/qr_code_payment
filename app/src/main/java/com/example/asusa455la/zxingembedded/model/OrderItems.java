package com.example.asusa455la.zxingembedded.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * Created by ASUS A455LA on 16/01/2018.
 */

@Entity (foreignKeys = {
        @ForeignKey(
                entity = Order.class,
                parentColumns = "idOrder",
                childColumns = "idOrder"
        ),
        @ForeignKey(
                entity = Item.class,
                parentColumns = "idBarang",
                childColumns = "idBarang"
        )
    }, indices = {@Index("idOrder"), @Index("idBarang")})
public class OrderItems {
    private int idOrder;
    private String idBarang;
    private int kuantitas;

    public OrderItems(int idOrder, String idBarang, int kuantitas){
        this.idOrder = idOrder;
        this.idBarang = idBarang;
        this.kuantitas = kuantitas;
    }

    public int getIdOrder(){
        return idOrder;
    }

    public String getIdBarang(){
        return idBarang;
    }

    public int getKuantitas(){
        return kuantitas;
    }
}
