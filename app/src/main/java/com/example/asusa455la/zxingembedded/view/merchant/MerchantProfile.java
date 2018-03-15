package com.example.asusa455la.zxingembedded.view.merchant;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.asusa455la.zxingembedded.R;

public class MerchantProfile extends AppCompatActivity {
    private EditText mNameEditText;
    private EditText mEmailAddressEditText;
    private EditText mHandphoneNumberEditText;

    private Button mSaveChangeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SharedPreferences sharedPreferences = this.getSharedPreferences(getString(R.string.shared_pref_appname), Context.MODE_PRIVATE);

        this.mNameEditText = (EditText) findViewById(R.id.mProfileNameTextBox);
        this.mNameEditText.setText(sharedPreferences.getString((getString(R.string.shared_pref_merchant_name)), ""));

        this.mEmailAddressEditText = (EditText) findViewById(R.id.mProfileEmailTextBox);
        this.mEmailAddressEditText.setText(sharedPreferences.getString((getString(R.string.shared_pref_email)), ""));

        this.mHandphoneNumberEditText = (EditText) findViewById(R.id.mProfileHandphoneNumberTextBox);
        this.mHandphoneNumberEditText.setText(sharedPreferences.getString((getString(R.string.shared_pref_handphone_number)), ""));

        this.mSaveChangeButton = (Button) findViewById(R.id.mSaveChangeButton);
        this.mSaveChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });
    }
}
