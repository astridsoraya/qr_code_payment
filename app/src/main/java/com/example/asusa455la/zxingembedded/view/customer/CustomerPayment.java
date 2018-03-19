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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.Map;


public class CustomerPayment extends AppCompatActivity {
    private static String urlCheckPayment = "https://qrcodepayment.crabdance.com/check_payment.php";

    private Button cPayButton;
    private static TextView cPaymentDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_payment);

        this.cPayButton = findViewById(R.id.cPayButton);
        this.cPayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startConfirmPayment(ConfirmPayment.class);
                onPause();
            }
        });

        this.cPaymentDetail = findViewById(R.id.cPaymentDetailTextView);
        checkOrderExist();
    }

    private void checkOrderExist(){
        String tag_string = "string_req";

        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Verifying order...");
        pDialog.show();

        Intent intent = this.getIntent();
        String qrCodeData = intent.getExtras().getString("QRCodeData");
        String[] splitQRCodeData = qrCodeData.split(";");
        final String idOrder = splitQRCodeData[0];

        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_pref_appname), Context.MODE_PRIVATE);
        final String idCustomer = sharedPreferences.getString(getString(R.string.shared_pref_id_user), "");

        StringRequest strRequest = new StringRequest(Request.Method.POST, urlCheckPayment,
                new Response.Listener<String>()
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
                new Response.ErrorListener()
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
                params.put("id_customer", idCustomer);
                return params;
            }
        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strRequest, tag_string);
    }


    private void displayOrder(JSONArray jsonOrder){
        try {
            JSONObject tempJSONObject = jsonOrder.getJSONObject(0);
            String digitalCertPath = tempJSONObject.getString("digital_certificate");
            Intent intent = this.getIntent();
            String qrCodeData = intent.getExtras().getString("QRCodeData");
            new QRCodeVerifier(this, qrCodeData, jsonOrder).execute("https://qrcodepayment.crabdance.com/upload/certs/"+digitalCertPath);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void startConfirmPayment(Class anotherClass){
        Intent customerPaymentIntent = this.getIntent();
        String qrCodeData = customerPaymentIntent.getExtras().getString("QRCodeData");
        String[] splitQRCodeData = qrCodeData.split(";");
        final String orderData = splitQRCodeData[0];

        Intent intent = new Intent(this, anotherClass);
        intent.putExtra("order_data", orderData);
        startActivity(intent);
        finish();
    }



    private static class QRCodeVerifier extends AsyncTask<String, Integer, String> {

        private Context context;
        private ProgressDialog pDialog;
        private String qrCodeData;
        private JSONArray jsonArray;

        QRCodeVerifier(Context context, String qrCodeData, JSONArray jsonArray) {
            this.context = context;
            this.qrCodeData = qrCodeData;
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
            String digitalCertPath = "";
            try {
                JSONObject tempJSONObject = jsonArray.getJSONObject(0);
                digitalCertPath = tempJSONObject.getString("digital_certificate");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(sUrl[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();
                output = new FileOutputStream(Environment.getExternalStorageDirectory() + File.separator + "CERT Folder/" + digitalCertPath);

                byte data[] = new byte[2048];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                System.out.println("Error downloading file: " + e.toString());
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException e) {
                    System.out.println("Error saving file: " + e.toString());
                }

                if (connection != null)
                    connection.disconnect();
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
                String digitalCertPath = tempJSONObject.getString("digital_certificate");

                File certFile = new File(Environment.getExternalStorageDirectory() + File.separator + "CERT Folder", digitalCertPath);
                Certificate digitalCertificate = Cryptography.loadCertificate(certFile);

                if(!verifyQRCodeData(qrCodeData, digitalCertificate)){
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

                    String namaMerchant = tempJSONObject.getString("nama_merchant");
                    display += "Nama Merchant: " + namaMerchant + "\n\n";

                    int totalHarga = 0;

                    for(int i = 0; i < jsonArray.length(); i++) {
                        JSONObject itemJson = jsonArray.getJSONObject(i);

                        String namaBarang = itemJson.getString("nama_barang");
                        int harga = Integer.parseInt(itemJson.getString("harga"));
                        int kuantitas = Integer.parseInt(itemJson.getString("kuantitas"));
                        totalHarga += harga * kuantitas;


                        display += String.format("%s: %d x%d%n", namaBarang, harga, kuantitas);

                    }
                    boolean deleted = certFile.delete();
                    display += "\nTotal Harga: " + totalHarga;
                    cPaymentDetail.setText(display);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            pDialog.dismiss();
        }

        private boolean verifyQRCodeData(String qrCodeData, Certificate certificate){
            String[] splitQRCodeData = qrCodeData.split(";");
            final String idOrder = splitQRCodeData[0];
            final String namaMerchant = splitQRCodeData[1];
            final String totalHarga = splitQRCodeData[2];

            return Cryptography.verifyDigitalSignature(idOrder+";"
                    +namaMerchant+";" + totalHarga,splitQRCodeData[3], certificate.getPublicKey());
        }
    }
}