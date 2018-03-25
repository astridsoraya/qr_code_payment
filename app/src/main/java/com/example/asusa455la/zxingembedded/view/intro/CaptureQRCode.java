package com.example.asusa455la.zxingembedded.view.intro;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.asusa455la.zxingembedded.R;
import com.example.asusa455la.zxingembedded.view.customer.CustomerPayment;
import com.example.asusa455la.zxingembedded.view.merchant.MerchantConfirmCustomer;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class CaptureQRCode extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
            Intent thisContext = this.getIntent();
            Bundle extras = thisContext.getExtras();
            String userType = extras.getString("userType");

            Intent intentPayment = null;

            if(userType.equalsIgnoreCase("customer")){
                intentPayment = new Intent(this, CustomerPayment.class);
            }
            else if(userType.equalsIgnoreCase("merchant")){
                intentPayment = new Intent(this, MerchantConfirmCustomer.class);
            }

            intentPayment.putExtra("qrCodeData", capturedData);
            System.out.println("JBJ QR Code Data " + userType + " " + capturedData);

            startActivity(intentPayment);
            this.finish();
        }
        else{
            this.finish();
        }
    }

}
