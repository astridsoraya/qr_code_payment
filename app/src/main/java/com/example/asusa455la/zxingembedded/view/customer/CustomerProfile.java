package com.example.asusa455la.zxingembedded.view.customer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.asusa455la.zxingembedded.R;

public class CustomerProfile extends AppCompatActivity {
    private EditText cFirstNameEditText;
    private EditText cLastNameEditText;
    private EditText cEmailAddressEditText;
    private EditText cHandphoneNumberEditText;

    private Button cSaveChangeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_pref_appname), Context.MODE_PRIVATE);

        this.cFirstNameEditText = (EditText) findViewById(R.id.cProfileFirstNameEditText);
        this.cFirstNameEditText.setText(sharedPreferences.getString(getString(R.string.shared_pref_first_name), ""));

        this.cLastNameEditText = (EditText) findViewById(R.id.cProfileLastNameEditText);
        this.cLastNameEditText.setText(sharedPreferences.getString(getString(R.string.shared_pref_last_name), ""));

        this.cEmailAddressEditText = (EditText) findViewById(R.id.cProfileEmailAddressEditText);
        this.cEmailAddressEditText.setText(sharedPreferences.getString(getString(R.string.shared_pref_email), ""));

        this.cHandphoneNumberEditText = (EditText) findViewById(R.id.cProfileHandphoneNumberEditText);
        this.cHandphoneNumberEditText.setText(sharedPreferences.getString(getString(R.string.shared_pref_handphone_number), ""));

        this.cSaveChangeButton = (Button) findViewById(R.id.cSaveChangeButton);
    }

}
