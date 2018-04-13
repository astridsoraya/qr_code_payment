package com.example.asusa455la.zxingembedded.view.customer;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.asusa455la.zxingembedded.R;
import com.example.asusa455la.zxingembedded.utility.AppDatabase;
import com.example.asusa455la.zxingembedded.utility.Cryptography;
import com.example.asusa455la.zxingembedded.view.intro.CaptureQRCode;
import com.example.asusa455la.zxingembedded.view.intro.Login;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CustomerMainMenu extends AppCompatActivity {
    private static final int PICK_IMAGE = 1;

    private SharedPreferences sharedPreferences;
    private AppDatabase appDatabase;

    private Button cProfileButton;
    private Button cPaymentButton;
    private Button cPaymentHistoryButton;
    private Button cLogoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sharedPreferences = getSharedPreferences(getString(R.string.shared_pref_appname), Context.MODE_PRIVATE);
        appDatabase  = AppDatabase.getDatabaseInstance(this);

        String folderCSR = Environment.getExternalStorageDirectory() + File.separator  + "CSR Folder";

        // Create the file.
        String commonName = sharedPreferences.getString((getString(R.string.shared_pref_email)), "");

        File csrFile = new File(folderCSR, commonName + ".csr");

        if(!csrFile.exists()){
            Cryptography.createCertificateRequestFile(this, commonName);
        }

        this.cProfileButton = (Button) findViewById(R.id.cProfileButton);
        this.cProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOtherActivity(CustomerProfile.class);
            }
        });

        this.cPaymentButton = (Button) findViewById(R.id.cPaymentButton);
        this.cPaymentButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
            Bundle bundle = new Bundle();
            bundle.putString("userType", "customer");
            showOtherActivityWithData(CaptureQRCode.class, bundle);
            }
        });

        this.cPaymentHistoryButton = (Button) findViewById(R.id.cPaymentHistoryButton);
        this.cPaymentHistoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOtherActivity(CustomerOrderHistory.class);
            }
        });

        this.cLogoutButton = (Button) findViewById(R.id.cLogOutButton);
        this.cLogoutButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                appDatabase.itemDao().deleteAllItems();
                sharedPreferences.edit().clear().commit();

                showOtherActivity(Login.class);
                finishActivity();
            }
        });
    }

    private void showOtherActivity(Class otherActivity){
        Intent intent = new Intent(this, otherActivity);
        startActivity(intent);
    }

    private void showOtherActivityWithData(Class otherActivity, Bundle bundle){
        Intent intent = new Intent(this, otherActivity);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void finishActivity(){
        this.finish();
    }

    // Rencana yang ada di tampilan customer
    // - Profil customer
    // - Konfigurasi ubah profil customer
    // - Histori barang yang sudah dibeli oleh customer
    // - QR code scanner untuk membaca QR code yang dibuat oleh merchant
    // - Pemesanan barang
    // - Konfirmasi pembayaran barang
    // - Pembuatan digital certificate untuk customer

}
