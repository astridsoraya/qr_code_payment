package com.example.asusa455la.zxingembedded.view.customer;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.example.asusa455la.zxingembedded.R;
import com.example.asusa455la.zxingembedded.utility.AppController;
import com.example.asusa455la.zxingembedded.utility.Cryptography;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class CustomerPayment extends AppCompatActivity {
    private static String urlCheckPayment = "http://localhost:5000/check_payment.php";

    private Certificate digitalCertificate;

    private Button cPayButton;
    private TextView cPaymentDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_payment);

        this.cPayButton = (Button) findViewById(R.id.cPayButton);
        this.cPayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAnotherActivity(ConfirmPayment.class);
                onPause();
            }
        });

        this.cPaymentDetail = (TextView) findViewById(R.id.cPaymentDetailTextView);
        checkOrderExist();
    }

    private void checkOrderExist(){
        String tag_string = "string_req";

        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Loading...");
        pDialog.show();

        Intent intent = this.getIntent();
        String qrCodeData = intent.getExtras().getString("QRCodeData");
        String[] splitQRCodeData = qrCodeData.split(";");
        final String idOrder = splitQRCodeData[0];

        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_pref_appname), Context.MODE_PRIVATE);
        final String idCustomer = sharedPreferences.getString(getString(R.string.shared_pref_id_user), "");

        StringRequest strRequest = new StringRequest(com.android.volley.Request.Method.POST, urlCheckPayment,
                new com.android.volley.Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {
                        Log.d(AppController.TAG, response.toString());

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
                Map<String, String> params = new HashMap<String, String>();

                params.put("id_order", idOrder);
                params.put("id_customer", idCustomer);
                return params;
            }
        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strRequest, tag_string);
    }

    private void displayOrder(JSONArray jsonOrder){
        String display = "";
        try {
            JSONObject tempJSONObject = jsonOrder.getJSONObject(0);

            String digitalCertPath = tempJSONObject.getString("digital_certificate");
            Intent intent = this.getIntent();
            String qrCodeData = intent.getExtras().getString("QRCodeData");
            downloadCertificate(digitalCertPath);

            if(!verifyQRCodeData(qrCodeData, digitalCertificate)){
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
                String idOrder= tempJSONObject.getString("id_order");
                display += "Nomor Order: " + idOrder + "\n";

                String namaMerchant = tempJSONObject.getString("nama_merchant");
                display += "Nama Merchant: " + namaMerchant + "\n\n";

                int totalHarga = 0;

                for(int i = 0; i < jsonOrder.length(); i++) {
                    JSONObject itemJson = jsonOrder.getJSONObject(i);

                    String namaBarang = itemJson.getString("nama_barang");
                    int harga = Integer.parseInt(itemJson.getString("harga"));
                    int kuantitas = Integer.parseInt(itemJson.getString("kuantitas"));
                    totalHarga += harga * kuantitas;


                    display += String.format("%s: %d x%d%n", namaBarang, harga, kuantitas);

                }

                display += "\nTotal Harga: " + totalHarga;
                this.cPaymentDetail.setText(display);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void startAnotherActivity(Class anotherClass){
        Intent customerPaymentIntent = this.getIntent();
        String qrCodeData = customerPaymentIntent.getExtras().getString("QRCodeData");

        Intent intent = new Intent(this, anotherClass);
        intent.putExtra("QRCodeData", qrCodeData);
        startActivity(intent);
        finish();
    }

    private void downloadCertificate(String digitalCertPath){
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("http://localhost:5000/upload/certs/"+digitalCertPath)
                .build();

        client.newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(final Call call, IOException e) {
                        // Error

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                System.out.println("JBJ");
                                // For the example, you can show an error dialog or a toast
                                // on the main UI thread
                                Toast.makeText(getApplicationContext(), "File not found", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, final Response response) throws IOException {
                        InputStream is = response.body().byteStream();

                        digitalCertificate = Cryptography.loadCertificateFromRaw(is);

                        response.body().byteStream().close();
                    }
                });
    }

    private boolean verifyQRCodeData(String qrCodeData, Certificate certificate){
        String[] splitQRCodeData = qrCodeData.split(";");
        final String idOrder = splitQRCodeData[0];
        final String namaMerchant = splitQRCodeData[1];

        return Cryptography.verifyDigitalSignature(idOrder+";"
                    +namaMerchant+";",splitQRCodeData[2], certificate.getPublicKey());
    }

    /*private void pay(){
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
    }*/

}
