package com.example.asusa455la.zxingembedded.view.customer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.asusa455la.zxingembedded.R;
import com.example.asusa455la.zxingembedded.utility.AppController;
import com.example.asusa455la.zxingembedded.utility.Cryptography;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.sql.SQLOutput;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class CustomerPayment extends AppCompatActivity {
    private static String urlAuthMerchant = "https://qrcodepayment.000webhostapp.com/confirm_auth.php";

    private Button cPayButton;
    private TextView cPaymentDetail;
    private ProgressDialog pDialog;

    private String decryptedData;
    private String digitalSignature;
    private String idOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_payment);

        this.decryptedData = "";
        this.digitalSignature = "";
        this.idOrder = "";

        this.cPayButton = findViewById(R.id.cCreateQRCodeButton);
        this.cPayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle extras = new Bundle();
                extras.putString("idOrder", idOrder);
                startAnotherActivityWithData(ConfirmPayment.class, extras);
            }
        });

        this.cPaymentDetail = findViewById(R.id.cPaymentDetailTextView);
        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Verifying order...");
        pDialog.show();

        decryptQRData();
    }

    private void decryptQRData(){
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        String qrCodeData = bundle.getString("qrCodeData");
        String[] splitQRCodeData = qrCodeData.split(";");
        String encryptedData = splitQRCodeData[0];
        String wrappedKey = splitQRCodeData[1];
        digitalSignature = splitQRCodeData[2];

        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_pref_appname), Context.MODE_PRIVATE);
        String email_address = sharedPreferences.getString(getString(R.string.shared_pref_email), "");
        PrivateKey privateKey = null;

        try {
            // Get private key from String
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            KeyStore.Entry entry = keyStore.getEntry(email_address, null);

            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) entry;
            privateKey = privateKeyEntry.getPrivateKey();

        } catch (IOException | KeyStoreException | UnrecoverableEntryException | CertificateException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        SecretKey secretKey = Cryptography.unwrapKey(wrappedKey, privateKey);
        decryptedData = Cryptography.decrypt(encryptedData, secretKey);
        System.out.println("Monsta decrypted data dari merchant: " + decryptedData);

        String[] splitDecryptedData = decryptedData.split(";");
        idOrder = splitDecryptedData[0];
        String usernameMerchant = splitDecryptedData[1];

        checkOrderExist(idOrder, usernameMerchant);
    }


    private void checkOrderExist(final String idOrderTemp, final String usernameMerchant){
        String tag_string = "string_req";

        StringRequest strRequest = new StringRequest(com.android.volley.Request.Method.POST, urlAuthMerchant,
                new com.android.volley.Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {
                        Log.d(AppController.TAG, response);

                        try {
                            JSONArray jsonArray= new JSONArray(response);

                            if(jsonArray.length() > 0){
                                displayOrder(jsonArray);
                                pDialog.hide();
                            }
                            else{
                                pDialog.hide();
                                Toast.makeText(getApplicationContext(), "Order not found", Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new com.android.volley.Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        VolleyLog.d(AppController.TAG, "Error: " + error.getMessage());
                        pDialog.hide();
                        Toast.makeText(getApplicationContext(), "System failed to show order", Toast.LENGTH_SHORT).show();
                    }
                })
        {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String> params = new HashMap<>();
                params.put("id_order", idOrderTemp);
                params.put("username", usernameMerchant);
                params.put("user_type", "merchant");
                return params;
            }
        };



        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strRequest, tag_string);
    }


    private void displayOrder(JSONArray jsonOrders){
            String display = "";
            int totalHarga = 0;

            File digCertFile = new File(Environment.getExternalStorageDirectory(), "01.crt");
            Certificate digitalCertificate = Cryptography.loadCertificate(digCertFile);
            boolean deletedFile = digCertFile.delete();

            try{
                pDialog.hide();
                if(!verifyQRCodeData(decryptedData, digitalSignature, digitalCertificate)){
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("QR Code data is not authentic! Returning to main menu.")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    finish();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
                else{
                    JSONObject tempJSONObject = jsonOrders.getJSONObject(0);

                    String idOrder= tempJSONObject.getString("id_order");
                    display += "Nomor Order: " + idOrder + "\n";

                    String namaMerchant = tempJSONObject.getString("nama_merchant");
                    display += "Nama Merchant: " + namaMerchant + "\n\n";

                    for(int i = 0; i < jsonOrders.length(); i++) {
                        JSONObject itemJson = jsonOrders.getJSONObject(i);

                        String namaBarang = itemJson.getString("nama_barang");
                        int harga = Integer.parseInt(itemJson.getString("harga"));
                        int kuantitas = Integer.parseInt(itemJson.getString("kuantitas"));
                        totalHarga += harga * kuantitas;


                        display += String.format("%s: %d x%d%n", namaBarang, harga, kuantitas);

                    }
                    display += "\nTotal Harga: " + totalHarga;
                    cPaymentDetail.setText(display);
                    pDialog.hide();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

    }

    private boolean verifyQRCodeData(String decryptedData, String digitalSignature, Certificate certificate){
        String[] splitQRCodeData = decryptedData.split(";");
        String tempIdOrder = splitQRCodeData[0];
        String merchantUsername = splitQRCodeData[1];
        String totalHarga = splitQRCodeData[2];

        return Cryptography.verifyDigitalSignature(tempIdOrder+";"
                +merchantUsername+ ";" + totalHarga, digitalSignature, certificate.getPublicKey());
    }


    private void startAnotherActivityWithData(Class anotherActivity, Bundle extras){
        Intent intent = new Intent(this, anotherActivity);
        intent.putExtras(extras);
        startActivity(intent);
        finish();
    }
}