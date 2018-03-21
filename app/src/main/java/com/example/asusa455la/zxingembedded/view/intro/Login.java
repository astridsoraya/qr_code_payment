package com.example.asusa455la.zxingembedded.view.intro;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.example.asusa455la.zxingembedded.R;
import com.example.asusa455la.zxingembedded.utility.AppController;
import com.example.asusa455la.zxingembedded.view.customer.CustomerMainMenu;
import com.example.asusa455la.zxingembedded.view.merchant.MerchantMainMenu;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * A login screen that offers login via email/password.
 */
public class Login extends AppCompatActivity{
    private static String urlLogin = "https://qrcodepayment.ddns.net/login.php";

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mLoginFormView;
    private TextView mRegisterTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_pref_appname), Context.MODE_PRIVATE);

        if(sharedPreferences.getBoolean(getString(R.string.shared_pref_has_login), false)){
            startMainActivity(sharedPreferences.getString(getString(R.string.shared_pref_user_type), ""));
            this.finish();
        }

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    validateFields();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                validateFields();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mRegisterTextView = (TextView) findViewById(R.id.link_register);
        mRegisterTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startChooseRegistrationActivity();
            }
        });
    }

    private void startChooseRegistrationActivity(){
        Intent chooseRegistrationActivity = new Intent(this, ChooseAccountType.class);
        startActivity(chooseRegistrationActivity);
        this.onPause();
    }

    private void startMainActivity(String userType){

        Intent intent = null;

        if(userType.equalsIgnoreCase("customer")){
            intent = new Intent(this, CustomerMainMenu.class);
        }
        else if(userType.equalsIgnoreCase("merchant")){
            intent = new Intent(this, MerchantMainMenu.class);
        }

        this.finish();
        startActivity(intent);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void validateFields() {

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            //mAuthTask = new UserLoginTask(email, password);
            //mAuthTask.execute((Void) null);
            login();
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() >= 8;
    }

    private void login(){
        // Tag used to cancel the request
        String tag_string = "string_req";

        // Show a progress spinner, and kick off a background task to
        // perform the user login attempt.
        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Loading...");
        pDialog.show();

        StringRequest strRequest = new StringRequest(Request.Method.POST, urlLogin,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {
                        Log.d(AppController.TAG, response.toString());

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String notificationSuccess = jsonObject.get("success").toString();
                            String messageResponse = jsonObject.get("message").toString();

                            if(notificationSuccess.equals("1")){
                                Toast.makeText(getApplicationContext(), messageResponse, Toast.LENGTH_SHORT).show();

                                saveUserSession(jsonObject);

                                pDialog.hide();

                            }
                            else if(notificationSuccess.equals("0")){
                                pDialog.hide();
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
                        Toast.makeText(getApplicationContext(), "System failed to login", Toast.LENGTH_SHORT).show();
                    }
                })
        {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String> params = new HashMap<String, String>();
                params.put("email_address", mEmailView.getText().toString());
                params.put("password", mPasswordView.getText().toString());
                return params;
            }
        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strRequest, tag_string);
    }

    private void saveUserSession(JSONObject jsonObject){
        SharedPreferences sharedPreferences = this.getSharedPreferences(getString(R.string.shared_pref_appname), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.clear();

        editor.putBoolean(getString(R.string.shared_pref_has_login), true);
        try {
            String userType = jsonObject.get("user_type").toString();

            editor.putString(getString(R.string.shared_pref_user_type), userType);
            editor.putString(getString(R.string.shared_pref_email), jsonObject.get("email_address").toString());
            editor.putString(getString(R.string.shared_pref_handphone_number), jsonObject.get("handphone_number").toString());
            editor.putString(getString(R.string.shared_pref_username), jsonObject.get("username").toString());

            if(userType.equalsIgnoreCase("merchant")){
                editor.putString(getString(R.string.shared_pref_id_user), jsonObject.get("id_merchant").toString());
                editor.putString(getString(R.string.shared_pref_merchant_name), jsonObject.get("merchant_name").toString());
                editor.putString(getString(R.string.shared_pref_merchant_address), jsonObject.get("address").toString());
            }
            else if(userType.equalsIgnoreCase("customer")){
                editor.putString(getString(R.string.shared_pref_id_user), jsonObject.get("id_customer").toString());
                editor.putString(getString(R.string.shared_pref_first_name), jsonObject.get("first_name").toString());
                editor.putString(getString(R.string.shared_pref_last_name), jsonObject.get("last_name").toString());
            }

            editor.commit();

            startMainActivity(userType);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

    }

}

