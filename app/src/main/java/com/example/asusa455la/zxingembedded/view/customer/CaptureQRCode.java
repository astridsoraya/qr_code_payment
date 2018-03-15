package com.example.asusa455la.zxingembedded.view.customer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.asusa455la.zxingembedded.R;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class CaptureQRCode extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_qrcode);

        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        integrator.setPrompt("Scan a QR code to pay");
        integrator.setOrientationLocked(false);
        integrator.setCameraId(0);
        integrator.setBeepEnabled(false);
        integrator.initiateScan();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (resultCode == RESULT_OK) {
            String capturedData = scanResult.getContents();
            Intent intentPayment = new Intent(this, CustomerPayment.class);
            intentPayment.putExtra("QRCodeData", capturedData);
            startActivity(intentPayment);
            this.finish();
        }
        else{
            this.finish();
        }
    }

}
