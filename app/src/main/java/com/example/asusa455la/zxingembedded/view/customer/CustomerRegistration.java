package com.example.asusa455la.zxingembedded.view.customer;

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

public class CustomerRegistration extends AppCompatActivity {
    private static String urlRegister = "https://qrcodepayment.000webhostapp.com/register.php";

    private EditText firstNameTextBox;
    private EditText lastNameTextBox;
    private EditText emailAddressTextBox;
    private EditText passwordTextBox;
    private EditText handphoneNumberTextBox;

    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_customer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        this.firstNameTextBox = (EditText) findViewById(R.id.cFirstNameTextBox);
        this.lastNameTextBox = (EditText) findViewById(R.id.cLastNameTextBox);
        this.emailAddressTextBox = (EditText) findViewById(R.id.cEmailAddressTextBox);
        this.passwordTextBox = (EditText) findViewById(R.id.cPasswordTextBox);
        this.handphoneNumberTextBox = (EditText) findViewById(R.id.cHandphoneNumberTextBox);

        this.registerButton = (Button) findViewById(R.id.cRegisterButton);
        this.registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyIdentity();
            }
        });

    }

    private void startCreateWalletActivity(){
        Intent intent = new Intent(this, CreateWallet.class);
        Bundle extras = new Bundle();
        extras.putString("emailAddress", this.emailAddressTextBox.getText().toString());
        extras.putString("userType", "customer");
        intent.putExtras(extras);
        startActivity(intent);
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

                params.put("first_name", firstNameTextBox.getText().toString());
                params.put("last_name", lastNameTextBox.getText().toString());
                params.put("email_address", emailAddressTextBox.getText().toString());
                params.put("password", passwordTextBox.getText().toString());;
                params.put("handphone_number", handphoneNumberTextBox.getText().toString());
                return params;
            }
        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strRequest, tag_string);
    }

    private void verifyIdentity(){
        boolean success = true;
        View focusView = null;

        //Verify if first name is not empty
        String firstName = firstNameTextBox.getText().toString();
        if(TextUtils.isEmpty(firstName)){
            firstNameTextBox.setError("First name cannot be empty");
            focusView = firstNameTextBox;
            success = false;
        }

        //Verify if last name is not empty
        String lastName = lastNameTextBox.getText().toString();
        if(TextUtils.isEmpty(firstName)){
            lastNameTextBox.setError("Last name cannot be empty");
            focusView = lastNameTextBox;
            success = false;
        }

        //verify email address if it's empty or not a valid email address
        String emailAddress = emailAddressTextBox.getText().toString();
        if(TextUtils.isEmpty(emailAddress)){
            emailAddressTextBox.setError("E-mail address is required");
            focusView = emailAddressTextBox;
            success = false;
        }
        else if(!isEmailValid(emailAddress)){
            emailAddressTextBox.setError("E-mail address is not valid");
            focusView = emailAddressTextBox;
            success = false;
        }

        //verify password if it's not empty and more than 8 characters
        String password = passwordTextBox.getText().toString();
        if(TextUtils.isEmpty(password)){
            passwordTextBox.setError("Password is required");
            focusView = passwordTextBox;
            success = false;
        }
        else if(!isPasswordValid(password)){
            passwordTextBox.setError("Password should be more than 8 characters");
            focusView = passwordTextBox;
            success = false;
        }

        String handphoneNumber = handphoneNumberTextBox.getText().toString();
        if(TextUtils.isEmpty(handphoneNumber)){
            handphoneNumberTextBox.setError("Handphone number is required");
            focusView = handphoneNumberTextBox;
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
