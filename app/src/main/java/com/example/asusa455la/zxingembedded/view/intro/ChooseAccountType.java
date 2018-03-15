package com.example.asusa455la.zxingembedded.view.intro;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.example.asusa455la.zxingembedded.R;
import com.example.asusa455la.zxingembedded.view.customer.CustomerRegistration;
import com.example.asusa455la.zxingembedded.view.merchant.MerchantRegistration;

public class ChooseAccountType extends AppCompatActivity {
    private Button customerSignUpButton;
    private Button merchantSignUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_account_type);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        this.customerSignUpButton = (Button) findViewById(R.id.chooseCustomerSignupButton);
        this.merchantSignUpButton = (Button) findViewById(R.id.chooseMerchantSignUpButton);

        this.customerSignUpButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startRegistrationCustomerActivity();
            }
        });

        this.merchantSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRegistrationMerchantActivity();
            }
        });
    }

    private void startRegistrationCustomerActivity(){
        Intent intent = new Intent(this, CustomerRegistration.class);
        startActivity(intent);
        this.onPause();
    }

    private void startRegistrationMerchantActivity(){
        Intent intent = new Intent(this, MerchantRegistration.class);
        startActivity(intent);
        this.onPause();
    }

}
