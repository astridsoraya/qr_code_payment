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
import android.util.Base64;
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

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

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

        this.confirmCustomerButton = findViewById(R.id.mScanQRButton);
        this.confirmCustomerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        createQRCode();
    }

    private void createQRCode(){
        ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Creating QR code data...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        String idOrder = extras.getString("idOrder");
        String secretKeyString = extras.getString("secretKey");
        String totalHarga = extras.getString("totalHarga");

        SharedPreferences sharedPreferences = this.getSharedPreferences(getString(R.string.shared_pref_appname), Context.MODE_PRIVATE);
        String alias = (sharedPreferences.getString((getString(R.string.shared_pref_email)), ""));
        String username = (sharedPreferences.getString((getString(R.string.shared_pref_username)), ""));

        File digCertFile = new File(Environment.getExternalStorageDirectory(), "01.crt");
        Certificate digitalCertificate = Cryptography.loadCertificate(digCertFile);

        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();

        Bitmap bitmap = null;
        try {
            // Get private key from String
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            KeyStore.Entry entry = keyStore.getEntry(alias, null);
            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) entry;
            PrivateKey privateKey = privateKeyEntry.getPrivateKey();

            byte[] decodedKey = Base64.decode(secretKeyString.getBytes("UTF-8"), Base64.DEFAULT);
            SecretKey secretKey = new SecretKeySpec(decodedKey, "AES/ECB/PKCS5Padding");

            String plaintext = idOrder+";"+username+";"+totalHarga;

            String qrcode = Cryptography.encrypt(plaintext, secretKey)+";"
                    +Cryptography.wrapKey(secretKey, digitalCertificate.getPublicKey())+";"
                    +Cryptography.getDigitalSignature(plaintext, privateKey);

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
    }
}
