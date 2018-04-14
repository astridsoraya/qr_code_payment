package com.example.asusa455la.zxingembedded.view.customer;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.example.asusa455la.zxingembedded.R;
import com.example.asusa455la.zxingembedded.utility.AppController;
import com.example.asusa455la.zxingembedded.utility.Cryptography;
import com.example.asusa455la.zxingembedded.view.intro.CaptureQRCode;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
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

import javax.crypto.SecretKey;

import okhttp3.OkHttpClient;
import okio.BufferedSink;
import okio.Okio;

public class CustomerPrintQRCode extends AppCompatActivity {
    private static String requestOrderUrl = "https://qrcodepayment.ddns.net/add_order.php";

    private static ImageView qrCodeImageView;
    private Button payOrderButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_print_qrcode);

        this.qrCodeImageView = findViewById(R.id.cQRCodeView);
        this.payOrderButton = findViewById(R.id.cPayOrderButton);

        this.payOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                captureQRCode();
            }
        });

        requestOrder();
    }

    private void requestOrder(){
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        final String digitalCertificatePath = extras.getString("digitalCertificate");

        SharedPreferences sharedPreferences = this.getSharedPreferences(getString(R.string.shared_pref_appname), Context.MODE_PRIVATE);
        final String alias = (sharedPreferences.getString((getString(R.string.shared_pref_email)), ""));
        final String username = (sharedPreferences.getString(getString(R.string.shared_pref_username), ""));
        final Context context = this;

        String tag_string = "string_req";

        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Requesting order...");
        pDialog.show();

        StringRequest strRequest = new StringRequest(Request.Method.POST, requestOrderUrl,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {
                        Log.d(AppController.TAG, response.toString());

                        try {
                            JSONObject itemResponse = new JSONObject(response);
                            String success = itemResponse.getString("success");
                            String message = itemResponse.getString("message");

                            if(success.equals("1")){
                                String idOrder = itemResponse.getString("id_order");
                                new QRCodeCreator(context, alias, idOrder, username).execute("https://qrcodepayment.ddns.net/upload/certs/"+digitalCertificatePath);
                                pDialog.hide();
                            }
                            else{
                                pDialog.hide();
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
                    }
                });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strRequest, tag_string);
    }

    private void captureQRCode(){
        Bundle extras = new Bundle();

        SharedPreferences sharedPreferences = this.getSharedPreferences(getString(R.string.shared_pref_appname), Context.MODE_PRIVATE);
        String userType = (sharedPreferences.getString((getString(R.string.shared_pref_user_type)), ""));
        extras.putString("userType", userType);

        Intent intent = new Intent(this, CaptureQRCode.class);
        intent.putExtras(extras);
        startActivity(intent);
        this.finish();
    }

    private static class QRCodeCreator extends AsyncTask<String, Integer, String> {
        private Context context;
        private ProgressDialog pDialog;

        private String username;
        private String alias;
        private String idOrder;

        private Certificate digitalCertificate;

        QRCodeCreator(Context context, String alias, String idOrder, String username) {
            this.context = context;
            this.username= username;
            this.alias = alias;
            this.idOrder = idOrder;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(context);
            pDialog.setMessage("Creating QR code data...");
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

                BufferedSink sink = Okio.buffer(Okio.sink(new File(Environment.getExternalStorageDirectory(), "01.crt")));
                sink.writeAll(response.body().source());
                sink.close();
                response.body().close();
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
                MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                Bitmap bitmap = null;
                try {
                    // Get private key from String
                    KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
                    keyStore.load(null);

                    KeyStore.Entry entry = keyStore.getEntry(alias, null);
                    KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) entry;
                    PrivateKey privateKey = privateKeyEntry.getPrivateKey();

                    SecretKey secretKey = Cryptography.generateSecretKey();
                    System.out.println("locoloco " + Base64.encodeToString(secretKey.getEncoded(), Base64.DEFAULT));

                    String plaintext = idOrder+";"+username;
                    System.out.println("Hurr durr " + plaintext);
                    String ciphertext = Cryptography.encrypt(plaintext, secretKey);
                    String wrappedKey = Cryptography.wrapKey(secretKey, digitalCertificate.getPublicKey());
                    String digitalSignature = Cryptography.getDigitalSignature(plaintext, privateKey);

                    String qrcode = ciphertext + ";" + wrappedKey + ";" + digitalSignature;

                    BitMatrix bitMatrix = multiFormatWriter.encode(qrcode, BarcodeFormat.QR_CODE,
                            qrCodeImageView.getWidth(), qrCodeImageView.getHeight());
                    BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                    bitmap = barcodeEncoder.createBitmap(bitMatrix);
                    qrCodeImageView.setImageBitmap(bitmap);

                    System.out.println("Dean Customer: " + qrcode);
                } catch (WriterException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (CertificateException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (UnrecoverableEntryException e) {
                    e.printStackTrace();
                } catch (KeyStoreException e) {
                    e.printStackTrace();
                }

            pDialog.hide();
        }
    }
}
