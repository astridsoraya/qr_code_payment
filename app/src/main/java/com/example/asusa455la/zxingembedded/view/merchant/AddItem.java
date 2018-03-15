package com.example.asusa455la.zxingembedded.view.merchant;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AddItem extends AppCompatActivity {
    private static String urlListItem = "http://localhost:5000/add_item.php";

    private AppDatabase appDatabase;

    private EditText idBarangEditText;
    private EditText namaBarangEditText;
    private EditText hargaBarangEditText;
    private EditText stokBarangEditText;

    private Button minStokBarangButton;
    private Button addStokBarangButton;
    private Button addBarangButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        this.appDatabase = AppDatabase.getDatabaseInstance(this);

        this.idBarangEditText = (EditText) findViewById(R.id.idBarangEditText);
        this.namaBarangEditText = (EditText) findViewById(R.id.namaBarangEditText);
        this.hargaBarangEditText = (EditText) findViewById(R.id.hargaBarangEditText);

        this.stokBarangEditText = (EditText) findViewById(R.id.jumlahStokEditText);

        this.minStokBarangButton = (Button) findViewById(R.id.minStokBarangButton);
        this.minStokBarangButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                minStokBarang();
            }
        });

        this.addStokBarangButton = (Button) findViewById(R.id.addStokBarangButton);
        this.addStokBarangButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addStokBarang();
            }
        });

        this.addBarangButton = (Button) findViewById(R.id.tambahBarangButton);
        this.addBarangButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createBarang();
            }
        });
    }

    private void minStokBarang(){
        int stokBarang = Integer.parseInt(this.stokBarangEditText.getText().toString());
        if(stokBarang != 0){
            stokBarang--;
        }
        stokBarangEditText.setText(stokBarang + "");
    }

    private void addStokBarang(){
        int stokBarang = Integer.parseInt(this.stokBarangEditText.getText().toString());
        stokBarang++;
        stokBarangEditText.setText(stokBarang + "");
    }

    private void createBarang(){
        // Tag used to cancel the request
        String tag_string = "string_req";

        // Show a progress spinner, and kick off a background task to
        // perform the user login attempt.
        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Loading...");
        pDialog.show();

        StringRequest strRequest = new StringRequest(Request.Method.POST, urlListItem,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {
                        Log.d(AppController.TAG, response.toString());

                        try {
                            JSONObject jsonObject= new JSONObject(response);
                            String notificationSuccess = jsonObject.get("success").toString();
                            String messageResponse = jsonObject.get("message").toString();

                            if(notificationSuccess.equals("1")){
                                Item item = new Item(idBarangEditText.getText().toString(),
                                        namaBarangEditText.getText().toString(),
                                        Integer.parseInt(hargaBarangEditText.getText().toString()),
                                        Integer.parseInt(stokBarangEditText.getText().toString()));
                                appDatabase.itemDao().insertItem(item);

                                pDialog.hide();
                                Toast.makeText(getApplicationContext(), messageResponse, Toast.LENGTH_SHORT).show();
                                finish();

                            }
                            else if(notificationSuccess.equals("0") || notificationSuccess.equals("2")){
                                pDialog.hide();
                                Toast.makeText(getApplicationContext(), messageResponse, Toast.LENGTH_SHORT).show();
                            }

                            pDialog.hide();


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        VolleyLog.d(AppController.TAG, "Error: " + error.getMessage());
                        pDialog.hide();
                        Toast.makeText(getApplicationContext(), "System failed to add item", Toast.LENGTH_SHORT).show();
                    }
                })
        {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String> params = new HashMap<String, String>();
                params.put("id_barang", idBarangEditText.getText().toString());
                params.put("nama_barang", namaBarangEditText.getText().toString());
                params.put("harga", hargaBarangEditText.getText().toString());
                params.put("stok", stokBarangEditText.getText().toString());
                params.put("id_merchant", getIDMerchant());
                return params;
            }
        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strRequest, tag_string);
    }

    private String getIDMerchant(){
        SharedPreferences sharedPreferences = this.getSharedPreferences(getString(R.string.shared_pref_appname), Context.MODE_PRIVATE);
        String idMerchant = sharedPreferences.getString(getString(R.string.shared_pref_id_user), "");
        return idMerchant;
    }
}
