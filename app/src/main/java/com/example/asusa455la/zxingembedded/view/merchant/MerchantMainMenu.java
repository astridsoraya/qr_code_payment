package com.example.asusa455la.zxingembedded.view.merchant;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.example.asusa455la.zxingembedded.R;
import com.example.asusa455la.zxingembedded.model.Item;
import com.example.asusa455la.zxingembedded.utility.AppController;
import com.example.asusa455la.zxingembedded.utility.AppDatabase;
import com.example.asusa455la.zxingembedded.utility.Cryptography;
import com.example.asusa455la.zxingembedded.view.intro.Login;

import org.bouncycastle.operator.OperatorCreationException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.TrustManagerFactory;

public class MerchantMainMenu extends AppCompatActivity {
    private static String urlListItem = "https://qrcodepayment.crabdance.com/list_item.php";

    private SharedPreferences sharedPreferences;
    private AppDatabase appDatabase;

    private Button mProfileButton;
    private Button mCreateOrderButton;
    private Button mItemsButton;
    private Button mPaymentHistoryButton;
    private Button mLogoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sharedPreferences = getSharedPreferences(getString(R.string.shared_pref_appname), Context.MODE_PRIVATE);
        appDatabase  = AppDatabase.getDatabaseInstance(this);
        String folderCSR = Environment.getExternalStorageDirectory() + File.separator  + "CSR Folder";

        // Create the file.
        String commonName = sharedPreferences.getString((getString(R.string.shared_pref_email)), "");

        File csrFile = new File(folderCSR, commonName + ".csr");

        if(!csrFile.exists()){
            Cryptography.createCertificateRequestFile(this, commonName);
        }

        this.mProfileButton = (Button) findViewById(R.id.mProfileButton);
        this.mProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAnotherActivity(MerchantProfile.class);
            }
        });

        this.mItemsButton = (Button) findViewById(R.id.mItemButton);
        this.mItemsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAnotherActivity(ItemList.class);
            }
        });

        this.mCreateOrderButton = (Button) findViewById(R.id.mCreateOrderButton);
        this.mCreateOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAnotherActivity(MerchantOrder.class);
            }
        });

        this.mPaymentHistoryButton = (Button) findViewById(R.id.mPaymentHistoryButton);
        this.mPaymentHistoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAnotherActivity(MerchantOrderHistory.class);
            }
        });

        this.mLogoutButton = (Button) findViewById(R.id.mLogoutButton);
        this.mLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                appDatabase.itemDao().deleteAllItems();
                sharedPreferences.edit().clear().commit();
                startAnotherActivity(Login.class);
                finishActivity();

            }
        });

        new AsyncTask<Void, Void, Integer>() {

            @Override
            protected Integer doInBackground(Void... voids) {
                return appDatabase.itemDao().getTotalItems();
            }

            @Override
            protected void onPostExecute(Integer itemCounts) {
                if(itemCounts == 0){
                    retrieveItemsJSON();
                }
            }
        }.execute();
    }

    private void startAnotherActivity(Class otherActivity){
        Intent intent = new Intent(this, otherActivity);
        startActivity(intent);
    }


    private void retrieveItemsJSON(){
        // Tag used to cancel the request
        String tag_string = "string_req";

        // Show a progress spinner, and kick off a background task to
        // perform the user login attempt.

        StringRequest strRequest = new StringRequest(Request.Method.POST, urlListItem,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {
                        Log.d(AppController.TAG, response.toString());

                        if(!response.toString().isEmpty()){
                            try {
                                JSONArray itemResponse = new JSONArray(response);

                                if(itemResponse != null){
                                    addRetrievedItems(itemResponse);
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        VolleyLog.d(AppController.TAG, "Error: " + error.getMessage());
                        Toast.makeText(getApplicationContext(), "System failed to login", Toast.LENGTH_SHORT).show();
                    }
                })
        {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String> params = new HashMap<String, String>();
                params.put("id_merchant", getIDMerchant());
                return params;
            }
        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strRequest, tag_string);
    }

    private void addRetrievedItems(JSONArray items){
        try {
            for(int i = 0; i < items.length(); i++) {
                JSONObject itemJson = items.getJSONObject(i);

                final String idBarang = itemJson.getString("id_barang");
                final String namaBarang = itemJson.getString("nama_barang");
                final int harga = Integer.parseInt(itemJson.getString("harga"));
                final int stok = Integer.parseInt(itemJson.getString("stok"));

                new AsyncTask<Void, Void, Integer>() {

                    @Override
                    protected Integer doInBackground(Void... voids) {
                        Item item = new Item(idBarang, namaBarang, harga, stok);
                        appDatabase.itemDao().insertItem(item);

                        return null;
                    }
                }.execute();

                String display = String.format("Id Barang: %s%nNama Barang: %s%nHarga: %d%nStok: %d", idBarang,
                        namaBarang, harga, stok);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private String getIDMerchant(){
        SharedPreferences sharedPreferences = this.getSharedPreferences(getString(R.string.shared_pref_appname), Context.MODE_PRIVATE);
        String idMerchant = sharedPreferences.getString(getString(R.string.shared_pref_id_user), "");
        return idMerchant;
    }

    private void finishActivity(){
        this.finish();
    }
    // Rencana yang ada di tampilan Merchant
    // - Profil merchant
    // - Konfigurasi profil merchant
    // - Pembuatan order barang
    // - Merchant juga memiliki database barang-barang
    // - Pembuatan QR code yang akan dibaca oleh customer

}
