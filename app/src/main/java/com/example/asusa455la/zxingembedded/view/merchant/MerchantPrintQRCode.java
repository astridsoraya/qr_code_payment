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
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

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

import java.io.File;
import java.io.FileNotFoundException;
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

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MerchantPrintQRCode extends AppCompatActivity {
    private static ImageView qrCodeImageView;
    private Button confirmCustomerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_print_qrcode);

        this.qrCodeImageView = findViewById(R.id.mQRCodeView);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        String qrCodeData = extras.getString("qrCodeData");
        String digitalCertificatePath = extras.getString("digitalCertificate");

        this.confirmCustomerButton = findViewById(R.id.mScanQRButton);
        this.confirmCustomerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmCustomer();
            }
        });

        SharedPreferences sharedPreferences = this.getSharedPreferences(getString(R.string.shared_pref_appname), Context.MODE_PRIVATE);
        String alias = (sharedPreferences.getString((getString(R.string.shared_pref_email)), ""));

        new QRCodeCreator(this, alias, qrCodeData).execute("https://qrcodepayment.ddns.net/upload/certs/"+digitalCertificatePath);

    }

    private void confirmCustomer(){
        Intent intent = new Intent(this, CaptureQRCode.class);
        intent.putExtra("userType", "merchant");
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

            Bitmap bitmap = null;
            try {
                // Get private key from String
                KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
                keyStore.load(null);

                KeyStore.Entry entry = keyStore.getEntry(alias, null);
                KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) entry;
                PrivateKey privateKey = privateKeyEntry.getPrivateKey();

                String qrcode = Cryptography.encrypt(qrCodeData, digitalCertificate.getPublicKey())+";"
                        +Cryptography.getDigitalSignature(qrCodeData, privateKey);

                BitMatrix bitMatrix = multiFormatWriter.encode(qrcode, BarcodeFormat.QR_CODE,
                        qrCodeImageView.getWidth(), qrCodeImageView.getHeight());
                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                bitmap = barcodeEncoder.createBitmap(bitMatrix);
                qrCodeImageView.setImageBitmap(bitmap);

                System.out.println("Dean Merchant: " + qrcode);
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

            String[] splitQRCodeData = qrCodeData.split(";");
            String filename = splitQRCodeData[0] + "_secret";
            postImage(bitmap, filename);
        }

        private void postImage(Bitmap bitmap, String filename) {
            File qrCodeFile = new File(Environment.getExternalStorageDirectory() + File.separator, filename + ".jpg");
            try {
                qrCodeFile.createNewFile();
                OutputStream os = new FileOutputStream(qrCodeFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);

                os.flush();
                os.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String url = "https://qrcodepayment.ddns.net/post_image.php";

            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("fileToUpload", qrCodeFile.getName(), RequestBody.create(MediaType.parse("image/jpeg"), qrCodeFile))
                    .build();

            final Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();

            final OkHttpClient client = new OkHttpClient();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String jsonData = response.body().string();
                    try {
                        System.out.println(jsonData);
                        JSONObject jsonObject = new JSONObject(jsonData);
                        final String success = jsonObject.getString("success");
                        final String message = jsonObject.getString("message");

                        new Handler(Looper.getMainLooper()).post(new Runnable() {

                            @Override
                            public void run() {
                                if(success.equals("1")){
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                                    pDialog.dismiss();
                                }
                                else{
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                                    pDialog.dismiss();
                                }
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
