package com.example.asusa455la.zxingembedded.view.merchant;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.asusa455la.zxingembedded.R;
import com.example.asusa455la.zxingembedded.utility.Cryptography;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MerchantPrintQRCode extends AppCompatActivity {
    private ImageView qrCodeImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_print_qrcode);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        this.qrCodeImageView = (ImageView) findViewById(R.id.mQRCodeView);

        Intent intent = getIntent();
        String qrCodeData = intent.getExtras().getString("QRCodeData");

        SharedPreferences sharedPreferences = this.getSharedPreferences(getString(R.string.shared_pref_appname), Context.MODE_PRIVATE);
        String alias = (sharedPreferences.getString((getString(R.string.shared_pref_email)), ""));

        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(qrCodeData+Cryptography.getDigitalSignature(qrCodeData, alias), BarcodeFormat.QR_CODE,qrCodeImageView.getWidth(),
                    qrCodeImageView.getHeight());
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            this.qrCodeImageView.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }


}
