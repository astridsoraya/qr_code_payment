package com.example.asusa455la.zxingembedded.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * Created by ASUS A455LA on 07/12/2017.
 */

@Entity (primaryKeys = {"idBarang"})
public class Item {
    @NonNull
    private String idBarang;

    private String namaBarang;
    private int harga;
    private int stok;

    public Item(String idBarang, String namaBarang, int harga, int stok){
        this.idBarang = idBarang;
        this.namaBarang = namaBarang;
        this.harga = harga;
        this.stok = stok;
    }

    public String getIdBarang() { return idBarang; }

    public String getNamaBarang(){
        return namaBarang;
    }

    public int getHarga(){
        return harga;
    }

    public int getStok(){
        return stok;
    }
}
