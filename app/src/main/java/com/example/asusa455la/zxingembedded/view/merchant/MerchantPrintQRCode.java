package com.example.asusa455la.zxingembedded.view.merchant;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.asusa455la.zxingembedded.R;
import com.example.asusa455la.zxingembedded.utility.Cryptography;
import com.example.asusa455la.zxingembedded.view.intro.CaptureQRCode;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

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

import okhttp3.OkHttpClient;

public class MerchantPrintQRCode extends AppCompatActivity {
    private static ImageView qrCodeImageView;
    private Button confirmCustomerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_print_qrcode);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        this.qrCodeImageView = findViewById(R.id.mQRCodeView);
        this.confirmCustomerButton = findViewById(R.id.mScanQRButton);
        this.confirmCustomerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmCustomer();
            }
        });

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        String qrCodeData = extras.getString("qrCodeData");
        String digitalCertificatePath = extras.getString("digitalCertificate");

        SharedPreferences sharedPreferences = this.getSharedPreferences(getString(R.string.shared_pref_appname), Context.MODE_PRIVATE);
        String alias = (sharedPreferences.getString((getString(R.string.shared_pref_email)), ""));

        new QRCodeCreator(this, alias, qrCodeData).execute("https://qrcodepayment.ddns.net/upload/certs/"+digitalCertificatePath);

    }

    private void confirmCustomer(){
        Intent intent = new Intent(this, CaptureQRCode.class);
        startActivity(intent);
        this.finish();
    }

    private static class QRCodeCreator extends AsyncTask<String, Integer, String> {
        private Context context;
        private ProgressDialog pDialog;
        private String qrCodeData;
        private String alias;
        private Certificate digitalCertificate;

        QRCodeCreator(Context context, String alias, String qrCodeData) {
            this.context = context;
            this.qrCodeData = qrCodeData;
            this.alias = alias;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            System.out.println("Starting download");

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

            try {
                // Get private key from String
                KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
                keyStore.load(null);

                KeyStore.Entry entry = keyStore.getEntry(alias, null);
                KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) entry;
                PrivateKey privateKey = privateKeyEntry.getPrivateKey();

                BitMatrix bitMatrix = multiFormatWriter.encode(Cryptography.encrypt(qrCodeData, digitalCertificate.getPublicKey())+";"
                                +Cryptography.getDigitalSignature(qrCodeData, privateKey), BarcodeFormat.QR_CODE,
                        qrCodeImageView.getWidth(), qrCodeImageView.getHeight());
                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
                qrCodeImageView.setImageBitmap(bitmap);
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

            pDialog.dismiss();
        }
    }
}
