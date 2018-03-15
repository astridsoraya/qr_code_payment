package com.example.asusa455la.zxingembedded.view.merchant;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.ArrayAdapter;

import com.example.asusa455la.zxingembedded.R;
import com.example.asusa455la.zxingembedded.model.Item;
import com.example.asusa455la.zxingembedded.utility.AppDatabase;

import java.util.ArrayList;
import java.util.List;

public class ItemList extends ListActivity {
    private AppDatabase appDatabase;

    private ArrayAdapter<String> itemAdapter;
    private ArrayList<String> itemsArrayList;

    private FloatingActionButton floatingAddItemButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);

        this.appDatabase = AppDatabase.getDatabaseInstance(this);

        this.floatingAddItemButton = (FloatingActionButton) findViewById(R.id.floatingAddItemButton);
        this.floatingAddItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAddItemActivity();
            }
        });

        this.itemsArrayList = new ArrayList<String>();

        getItemsFromLocalDatabase();

    }

    private void openAddItemActivity(){
        Intent addItemIntent = new Intent(this, AddItem.class);
        startActivity(addItemIntent);
        this.onPause();
    }

    private void getItemsFromLocalDatabase(){
        List<Item> items = this.appDatabase.itemDao().getAllItems();

        for (Item item: items) {
            String idBarang = item.getIdBarang();
            String namaBarang = item.getNamaBarang();
            int harga = item.getHarga();
            int stok = item.getStok();

            String display = String.format("Id Barang: %s%nNama Barang: %s%nHarga: %d%nStok: %d", idBarang,
                    namaBarang, harga, stok);
            this.itemsArrayList.add(display);
        }

        this.itemAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, itemsArrayList);
        this.setListAdapter(this.itemAdapter);
    }

    private String getIDMerchant(){
        SharedPreferences sharedPreferences = this.getSharedPreferences(getString(R.string.shared_pref_appname), Context.MODE_PRIVATE);
        String idMerchant = sharedPreferences.getString(getString(R.string.shared_pref_id_user), "");
        return idMerchant;
    }
}

