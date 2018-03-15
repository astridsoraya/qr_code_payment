package com.example.asusa455la.zxingembedded.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;

/**
 * Created by ASUS A455LA on 01/02/2018.
 */

@Entity (foreignKeys = {
        @ForeignKey(
                entity = Order.class,
                parentColumns = "idOrder",
                childColumns = "idOrder"
        ),
        @ForeignKey(
                entity = Customer.class,
                parentColumns = "idCustomer",
                childColumns = "idCustomer"
        )
})
public class Payment {
    private int idPayment;
    private String waktuOrder;

    private int idOrder;
    private String idCustomer;

    public Payment(int idOrder, String idCustomer){
        this.idOrder = idOrder;
        this.idCustomer = idCustomer;
    }

    public int getIdPayment(){
        return idPayment;
    }

    public String getWaktuOrder(){
        return waktuOrder;
    }

    public int getIdOrder(){
        return idOrder;
    }

    public String getIdCustomer(){
        return idCustomer;
    }
}
