package com.example.asusa455la.zxingembedded.view.intro;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CreateWallet extends AppCompatActivity {
    private static String urlCreateWallet = "http://localhost:5000/create_wallet.php";

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            if(charSequence.toString().length() == 4){
                createWalletButton.setBackgroundColor(getResources().getColor(R.color.lightGreen));
                createWalletButton.setEnabled(true);
            }
            else{
                createWalletButton.setBackgroundColor(getResources().getColor(R.color.lightGray));
                createWalletButton.setEnabled(false);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    private EditText pinEditText;
    private Button createWalletButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_wallet);

        this.createWalletButton = (Button) findViewById(R.id.createWalletButton);
        this.createWalletButton.setEnabled(false);
        this.createWalletButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createWallet();
            }
        });

        this.pinEditText = (EditText) findViewById(R.id.enterPINEditText);
        this.pinEditText.addTextChangedListener(this.textWatcher);
    }

    private void createWallet(){
        Intent intent = this.getIntent();
        Bundle extras = intent.getExtras();
        final String emailAddress = extras.getString("emailAddress");
        final String userType = extras.getString("userType");

        // Tag used to cancel the request
        String tag_string = "string_req";

        // Show a progress spinner, and kick off a background task to
        // perform the user login attempt.
        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Loading...");
        pDialog.show();

        StringRequest strRequest = new StringRequest(Request.Method.POST, urlCreateWallet,
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
                                pDialog.hide();
                                finish();

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

                params.put("email_address", emailAddress);
                params.put("pin", pinEditText.getText().toString());
                params.put("user_type", userType);

                return params;
            }
        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strRequest, tag_string);
    }
}
