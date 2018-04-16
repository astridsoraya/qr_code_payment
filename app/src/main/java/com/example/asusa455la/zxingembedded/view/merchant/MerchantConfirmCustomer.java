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
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
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
import com.example.asusa455la.zxingembedded.model.Merchant;
import com.example.asusa455la.zxingembedded.utility.AppController;
import com.example.asusa455la.zxingembedded.utility.Cryptography;
import com.example.asusa455la.zxingembedded.view.customer.CustomerPayment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import okhttp3.OkHttpClient;
import okio.BufferedSink;
import okio.Okio;

public class MerchantConfirmCustomer extends AppCompatActivity {
    private static String urlAuthentication = "https://qrcodepayment.000webhostapp.com/confirm_auth.php";

    private static TextView mIdentitasCustomerTextView;
    private Button mAddOrderItems;

    private String idOrderAttribute;
    private String secretKeyString;

    private String decryptedData;
    private String digitalSignature;
    private String digitalCertPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_confirm_customer);

        this.mIdentitasCustomerTextView = findViewById(R.id.mIdentitasTextView);
        this.mAddOrderItems = findViewById(R.id.mAddOrderItems);
        this.mAddOrderItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle extras = new Bundle();
                extras.putString("idOrder", idOrderAttribute);
                extras.putString("secretKey", secretKeyString);

                startActivityWithData(MerchantOrder.class, extras);
            }
        });

        decryptQRData();
    }

    private void startActivityWithData(Class anotherActivity, Bundle extras){
        Intent intent = new Intent(this, anotherActivity);
        intent.putExtras(extras);
        startActivity(intent);
        this.finish();
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
        SecretKey unwrappedKey = Cryptography.unwrapKey(wrappedKey, privateKey);
        decryptedData = Cryptography.decrypt(encryptedData, unwrappedKey);
        secretKeyString = new String(Base64.encode(unwrappedKey.getEncoded(), Base64.DEFAULT));

        System.out.println("locoloco " + secretKeyString);
        System.out.println("Monsta " + decryptedData);
        String[] splits = decryptedData.split(";");
        String idOrder = splits[0];
        String usernameCustomer = splits[1];

        authenticateCustomer(usernameCustomer, idOrder);
    }

    private void authenticateCustomer(final String usernameCustomer, final String idOrder){
        String tag_string = "string_req";

        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Verifying customer...");
        pDialog.show();

        StringRequest strRequest = new StringRequest(com.android.volley.Request.Method.POST, urlAuthentication,
                new com.android.volley.Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {
                        Log.d(AppController.TAG, response);

                        try {
                            JSONObject jsonObject= new JSONObject(response);
                            String code = jsonObject.get("success").toString();
                            String message = jsonObject.get("message").toString();

                            if(code.equals("-1")){
                                pDialog.hide();
                                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                                finish();
                            }
                            else if(code.equals("1")){
                                pDialog.hide();
                                idOrderAttribute = idOrder;
                                displayCustomer(jsonObject);
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

                params.put("username", usernameCustomer);
                params.put("id_order", idOrder);
                params.put("user_type", "customer");
                return params;
            }
        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strRequest, tag_string);
    }


    private void displayCustomer(JSONObject jsonObject){
        try {
            digitalCertPath = jsonObject.getString("digital_certificate_customer");
            new QRCodeVerifier(this, decryptedData, digitalSignature, jsonObject).execute("https://qrcodepayment.000webhostapp.com/upload/certs/"+digitalCertPath);
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
        private JSONObject jsonObject;

        QRCodeVerifier(Context context, String decryptedData, String digitalSignature, JSONObject jsonObject) {
            this.context = context;
            this.decryptedData = decryptedData;
            this.digitalSignature = digitalSignature;
            this.jsonObject = jsonObject;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            System.out.println("Starting download");

            pDialog = new ProgressDialog(context);
            pDialog.setMessage("Verifying customer's QR code data...");
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

                File file = new File(Environment.getExternalStorageDirectory(), "01.crt");
                BufferedInputStream input = new BufferedInputStream(is);
                OutputStream output = new FileOutputStream(file);

                byte[] data = new byte[1024];

                int count = 0;
                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();
                digitalCertificate = Cryptography.loadCertificate(file);

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
                    String idOrder= jsonObject.getString("id_order");
                    display += "Nomor Order: " + idOrder + "\n";

                    String usernameCustomer = jsonObject.getString("customer_username");
                    display += "Username Customer: " + usernameCustomer + "\n\n";

                    String namaMerchant = jsonObject.getString("customer_name");
                    display += "Nama Customer: " + namaMerchant + "\n\n";

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
            final String usernameCustomer = splitQRCodeData[1];

            return Cryptography.verifyDigitalSignature(idOrder+";"
                    +usernameCustomer, digitalSignature, certificate.getPublicKey());
        }
    }


}
