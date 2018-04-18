package com.example.asusa455la.zxingembedded.view.merchant;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.example.asusa455la.zxingembedded.R;
import com.example.asusa455la.zxingembedded.utility.AppController;
import com.example.asusa455la.zxingembedded.view.intro.CreateWallet;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MerchantRegistration extends AppCompatActivity {
    private static String urlRegister = "https://qrcodepayment.000webhostapp.com/register.php";

    private EditText mMerchantNameEditText;
    private EditText mUsernameEditText;
    private EditText mEmailAddressEditText;
    private EditText mPasswordEditText;
    private EditText mAddressEditText;
    private EditText mHandphoneNumberEditText;

    private Button mRegisterButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_merchant);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mMerchantNameEditText = findViewById(R.id.mProfileNameTextBox);
        mUsernameEditText = findViewById(R.id.mUsernameEditText);
        mEmailAddressEditText = findViewById(R.id.mEmailAddressTextBox);
        mPasswordEditText = findViewById(R.id.mPasswordTextBox);
        mHandphoneNumberEditText = findViewById(R.id.mHandphoneNumberTextBox);
        mAddressEditText = findViewById(R.id.mAddressEditText);

        mRegisterButton =  findViewById(R.id.mRegisterButton);
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyIdentity();
            }
        });

    }

    private void register(){
        // Tag used to cancel the request
        String tag_string = "string_req";

        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Loading...");
        pDialog.show();

        StringRequest strRequest = new StringRequest(Request.Method.POST, urlRegister,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {
                        Log.d(AppController.TAG, response.toString());
                        pDialog.hide();

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String notificationSuccess = jsonObject.get("success").toString();
                            String messageResponse = jsonObject.get("message").toString();

                            if(notificationSuccess.equals("1")){
                                Toast.makeText(getApplicationContext(), messageResponse, Toast.LENGTH_SHORT).show();
                                startCreateWalletActivity();
                                finish();
                            }
                            else if(notificationSuccess.equals("0")){
                                Toast.makeText(getApplicationContext(), messageResponse, Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getApplicationContext(), "System failed to create an account", Toast.LENGTH_SHORT).show();
                    }
                })
        {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String> params = new HashMap<String, String>();

                params.put("merchant_name", mMerchantNameEditText.getText().toString());
                params.put("username", mUsernameEditText.getText().toString());
                params.put("email_address", mEmailAddressEditText.getText().toString());
                params.put("password",mPasswordEditText.getText().toString());
                params.put("address", mAddressEditText.getText().toString());
                params.put("handphone_number", mHandphoneNumberEditText.getText().toString());
                return params;
            }
        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strRequest, tag_string);
    }

    private void startCreateWalletActivity(){
        Intent intent = new Intent(this, CreateWallet.class);
        Bundle extras = new Bundle();
        extras.putString("emailAddress", this.mEmailAddressEditText.getText().toString());
        extras.putString("userType", "merchant");
        intent.putExtras(extras);
        startActivity(intent);
    }

    private void verifyIdentity(){
        boolean success = true;
        View focusView = null;

        //Verify if merchant name is not empty
        String merchantName = mMerchantNameEditText.getText().toString();
        if(TextUtils.isEmpty(merchantName)){
            mMerchantNameEditText.setError("Merchant's name cannot be empty");
            focusView = mMerchantNameEditText;
            success = false;
        }

        //Verify if merchant name is not empty
        String username = mUsernameEditText.getText().toString();
        if(TextUtils.isEmpty(username)){
            mUsernameEditText.setError("Username cannot be empty");
            focusView = mUsernameEditText;
            success = false;
        }

        //verify email address if it's empty or not a valid email address
        String emailAddress = mEmailAddressEditText.getText().toString();
        if(TextUtils.isEmpty(emailAddress)){
            mEmailAddressEditText.setError("E-mail address is required");
            focusView = mEmailAddressEditText;
            success = false;
        }
        else if(!isEmailValid(emailAddress)){
            mEmailAddressEditText.setError("E-mail address is not valid");
            focusView = mEmailAddressEditText;
            success = false;
        }

        //verify password if it's not empty and more than 8 characters
        String password = mPasswordEditText.getText().toString();
        if(TextUtils.isEmpty(password)){
            mPasswordEditText.setError("Password is required");
            focusView = mPasswordEditText;
            success = false;
        }
        else if(!isPasswordValid(password)){
            mPasswordEditText.setError("Password should be more than 8 characters");
            focusView = mPasswordEditText;
            success = false;
        }

        String address = mAddressEditText.getText().toString();
        if(TextUtils.isEmpty(address)){
            mAddressEditText.setError("Address is required");
            focusView = mAddressEditText;
            success = false;
        }

        String handphoneNumber = mHandphoneNumberEditText.getText().toString();
        if(TextUtils.isEmpty(handphoneNumber)){
            mHandphoneNumberEditText.setError("Handphone number is required");
            focusView = mHandphoneNumberEditText;
            success = false;
        }

        if (!success) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            register();
        }

    }

    //verify email address if it's empty or not a valid email address
    private boolean isEmailValid(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    //verify password if it's not empty and more than 8 characters
    private boolean isPasswordValid(String password) {
        return password.length() >= 8;
    }

}
