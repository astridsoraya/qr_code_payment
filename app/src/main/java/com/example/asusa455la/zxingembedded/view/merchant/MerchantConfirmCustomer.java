package com.example.asusa455la.zxingembedded.view.merchant;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.example.asusa455la.zxingembedded.R;
import com.example.asusa455la.zxingembedded.utility.AppController;
import com.example.asusa455la.zxingembedded.utility.Cryptography;
import com.example.asusa455la.zxingembedded.view.customer.CustomerPayment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;

public class MerchantConfirmCustomer extends AppCompatActivity {
    private static String urlCheckPayment = "https://qrcodepayment.ddns.net/check_payment.php";

    private static TextView mIdentitasCustomerTextView;
    private Button mFinishSession;

    private String decryptedData;
    private String digitalSignature;
    private String digitalCertPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_confirm_customer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        this.mIdentitasCustomerTextView = findViewById(R.id.mIdentitasTextView);
        this.mFinishSession = findViewById(R.id.mFinishSession);
        this.mFinishSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        decryptQRData();
    }

    private void decryptQRData(){
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        String qrCodeData = bundle.getString("qrCodeData");
        String[] splitQRCodeData = qrCodeData.split(";");
        String encryptedData = splitQRCodeData[0];
        digitalSignature = splitQRCodeData[1];

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

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (UnrecoverableEntryException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }

        decryptedData = Cryptography.decrypt(encryptedData, privateKey);
        String[] splitDecryptedData = decryptedData.split(";");
        final String idOrder = splitDecryptedData[0];
        String usernameCustomer = splitDecryptedData[1];

        System.out.println("Monsta decrypted data dari merchant: " + decryptedData);

        checkOrderExist(idOrder, usernameCustomer);
    }



    private void checkOrderExist(final String idOrder, final String usernameCustomer){
        String tag_string = "string_req";

        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Verifying order...");
        pDialog.show();

        StringRequest strRequest = new StringRequest(com.android.volley.Request.Method.POST, urlCheckPayment,
                new com.android.volley.Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {
                        Log.d(AppController.TAG, response);

                        try {
                            JSONArray jsonArray= new JSONArray(response);

                            if(jsonArray.length() > 0){
                                pDialog.hide();
                                displayOrder(jsonArray);
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

                params.put("id_order", idOrder);
                params.put("username_customer", usernameCustomer);
                return params;
            }
        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strRequest, tag_string);
    }


    private void displayOrder(JSONArray jsonOrder){
        try {
            JSONObject tempJSONObject = jsonOrder.getJSONObject(0);
            digitalCertPath = tempJSONObject.getString("digital_certificate_customer");
            new QRCodeVerifier(this, decryptedData, digitalSignature, jsonOrder).execute("https://qrcodepayment.ddns.net/upload/certs/"+digitalCertPath);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private static class QRCodeVerifier extends AsyncTask<String, Integer, String> {
        private Context context;
        private ProgressDialog pDialog;
        private String decryptedData;
        private String digitalSignature;
        private Certificate digitalCertificate;
        private JSONArray jsonArray;

        QRCodeVerifier(Context context, String decryptedData, String digitalSignature, JSONArray jsonArray) {
            this.context = context;
            this.decryptedData = decryptedData;
            this.digitalSignature = digitalSignature;
            this.jsonArray = jsonArray;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            System.out.println("Starting download");

            pDialog = new ProgressDialog(context);
            pDialog.setMessage("Verifying QR code data...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... sUrl) {
            OkHttpClient client = new OkHttpClient();

            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(sUrl[0])
                    .build();

            try (okhttp3.Response response = client.newCall(request).execute()) {
                InputStream is = response.body().byteStream();
                digitalCertificate = Cryptography.loadCertificate(is);

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        /*        *
                 * After completing background task
                 **/
        @Override
        protected void onPostExecute(String file_url) {
            System.out.println("Downloaded");
            String display = "";

            try{
                JSONObject tempJSONObject = jsonArray.getJSONObject(0);

                if(!verifyQRCodeData(decryptedData, digitalSignature, digitalCertificate)){
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage("QR Code data is not authentic! Returning to main menu.")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    ((Activity) context).finish();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
                else{
                    String idOrder= tempJSONObject.getString("id_order");
                    display += "Nomor Order: " + idOrder + "\n";

                    String usernameCustomer = tempJSONObject.getString("customer_username");
                    display += "Username Customer: " + usernameCustomer + "\n\n";

                    String namaMerchant = tempJSONObject.getString("customer_name");
                    display += "Nama Customer: " + namaMerchant + "\n\n";

                    int totalHarga = 0;

                    for(int i = 0; i < jsonArray.length(); i++) {
                        JSONObject itemJson = jsonArray.getJSONObject(i);

                        String namaBarang = itemJson.getString("nama_barang");
                        int harga = Integer.parseInt(itemJson.getString("harga"));
                        int kuantitas = Integer.parseInt(itemJson.getString("kuantitas"));
                        totalHarga += harga * kuantitas;


                        display += String.format("%s: %d x%d%n", namaBarang, harga, kuantitas);

                    }
                    display += "\nTotal Harga: " + totalHarga;
                    mIdentitasCustomerTextView.setText(display);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            pDialog.dismiss();
        }



        private boolean verifyQRCodeData(String decryptedData, String digitalSignature, Certificate certificate){
            String[] splitQRCodeData = decryptedData.split(";");
            final String idOrder = splitQRCodeData[0];
            final String namaCustomer = splitQRCodeData[1];
            final String totalHarga = splitQRCodeData[2];

            return Cryptography.verifyDigitalSignature(idOrder+";"
                    +namaCustomer+ ";" + totalHarga, digitalSignature, certificate.getPublicKey());
        }
    }


}
