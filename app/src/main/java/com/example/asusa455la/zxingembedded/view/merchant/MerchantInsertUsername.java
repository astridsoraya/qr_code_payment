package com.example.asusa455la.zxingembedded.view.merchant;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
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
import com.example.asusa455la.zxingembedded.utility.AppController;
import com.example.asusa455la.zxingembedded.utility.Cryptography;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.sql.SQLOutput;
import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;

public class MerchantInsertUsername extends AppCompatActivity {
    private static String urlSearch = "https://qrcodepayment.ddns.net/search_username.php";

    private EditText customerUsernameEditText;
    private Button createQRCodeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_insert_username);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        this.customerUsernameEditText = findViewById(R.id.usernameEditText);
        this.createQRCodeButton = findViewById(R.id.mCreateQRCodeButton);
        this.createQRCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = customerUsernameEditText.getText().toString();

                if(!TextUtils.isEmpty(username)){
                    searchUsername(username);
                }
            }
        });
    }

    private void searchUsername(final String username){
        // Tag used to cancel the request
        String tag_string = "string_req";

        // Show a progress spinner, and kick off a background task to
        // perform the user login attempt.
        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Loading...");
        pDialog.show();

        StringRequest strRequest = new StringRequest(Request.Method.POST, urlSearch,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {
                        Log.d(AppController.TAG, response.toString());

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String notificationSuccess = jsonObject.get("success").toString();
                            String messageResponse = jsonObject.get("message").toString();

                            if(notificationSuccess.equals("1")){
                                Toast.makeText(getApplicationContext(), messageResponse, Toast.LENGTH_SHORT).show();

                                createQRCode(jsonObject);

                                pDialog.hide();

                            }
                            else if(notificationSuccess.equals("0")){
                                pDialog.hide();
                                Toast.makeText(getApplicationContext(), messageResponse, Toast.LENGTH_SHORT).show();
                            }
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
                        Toast.makeText(getApplicationContext(), "System failed to search username", Toast.LENGTH_SHORT).show();
                    }
                })
        {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", username);
                return params;
            }
        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strRequest, tag_string);
    }

    private void createQRCode(JSONObject jsonObject){
        try {
            String digitalCertificatePath = jsonObject.get("digital_certificate").toString();

            Toast.makeText(getApplicationContext(), digitalCertificatePath, Toast.LENGTH_LONG).show();
            Intent thisContext = getIntent();
            String qrCodeData = thisContext.getExtras().getString("QRCodeData");

            Intent intent = new Intent(this, MerchantPrintQRCode.class);
            Bundle extras = new Bundle();
            extras.putString("qrCodeData",qrCodeData);
            extras.putString("digitalCertificate", digitalCertificatePath);
            intent.putExtras(extras);
            startActivity(intent);
            this.finish();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
