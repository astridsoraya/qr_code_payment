package com.example.asusa455la.zxingembedded.view.customer;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;
import com.example.asusa455la.zxingembedded.R;
import com.example.asusa455la.zxingembedded.model.Item;
import com.example.asusa455la.zxingembedded.utility.AppController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class CustomerOrderHistory extends ListActivity {
    private static String urlListOrder = "https://qrcodepayment.crabdance.com/list_order.php";

    private ArrayAdapter<String> orderAdapter;
    private ListView orderListView;

    private ArrayList<JSONObject> orderJSONArrayList;
    private ArrayList<String> orderStringArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_order_history);

        this.orderJSONArrayList = new ArrayList<JSONObject>();
        this.orderStringArrayList = new ArrayList<String>();

        retrieveOrdersJSON();

        final Context context = this;
        this.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    JSONObject orderJSON = orderJSONArrayList.get(i);
                    try {
                        String[] splitNamaBarang = orderJSON.getString("nama_barang").split(";");
                        String[] splitHarga = orderJSON.getString("harga").split(";");
                        String[] splitKuantitas = orderJSON.getString("kuantitas").split(";");

                        String res = "";

                        for(int j = 0; j < splitNamaBarang.length; j++){
                            res += splitNamaBarang[j] + ": " + splitHarga[j] + " x" + splitKuantitas[j] + "\n";
                        }

                        AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
                        builder1.setMessage(res);
                        builder1.setCancelable(true);

                        builder1.setPositiveButton(
                                "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                        AlertDialog alert11 = builder1.create();
                        alert11.show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
        });
    }

    private void retrieveOrdersJSON(){
        // Tag used to cancel the request
        String tag_string = "string_req";

        // Show a progress spinner, and kick off a background task to
        // perform the user login attempt.
        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Loading orders...");
        pDialog.show();

        /*HurlStack hurlStack = new HurlStack() {
            @Override
            protected HttpURLConnection createConnection(URL url) throws IOException {
                HttpsURLConnection httpsURLConnection = (HttpsURLConnection) super.createConnection(url);
                try {
                    httpsURLConnection.setSSLSocketFactory(getSSLSocketFactory());
                    httpsURLConnection.setHostnameVerifier(getHostnameVerifier());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return httpsURLConnection;
            }
        };*/

        StringRequest strRequest = new StringRequest(Request.Method.POST, urlListOrder,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {
                        Log.d(AppController.TAG, response.toString());

                        if(!response.toString().isEmpty()){
                            try {
                                JSONArray itemResponse = new JSONArray(response);

                                if(itemResponse != null){
                                    addRetrievedOrders(itemResponse);
                                }
                                pDialog.hide();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        else{
                            pDialog.hide();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        VolleyLog.d(AppController.TAG, "Error: " + error.getMessage());
                        Toast.makeText(getApplicationContext(), "System failed to login", Toast.LENGTH_SHORT).show();
                        pDialog.hide();
                    }
                })
        {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String> params = new HashMap<String, String>();
                params.put("user_type", "customer");
                params.put("id_customer", getIDCustomer());
                return params;
            }
        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strRequest, tag_string);
    }
/*
    // Let's assume your server app is hosting inside a server machine
    // which has a server certificate in which "Issued to" is "localhost",for example.
    // Then, inside verify method you can verify "localhost".
    // If not, you can temporarily return true
    private HostnameVerifier getHostnameVerifier() {
        return new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                //return true; // verify always returns true, which could cause insecure network traffic due to trusting TLS/SSL server certificates for wrong hostnames
                HostnameVerifier hv = HttpsURLConnection.getDefaultHostnameVerifier();
                return hv.verify("localhost", session);
            }
        };
    }*/

   /* private SSLSocketFactory getSSLSocketFactory()
            throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException, KeyManagementException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        InputStream caInput = getResources().openRawResource(R.raw.my_cert); // this cert file stored in \app\src\main\res\raw folder path

        Certificate ca = cf.generateCertificate(caInput);
        caInput.close();

        KeyStore keyStore = KeyStore.getInstance("BKS");
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca", ca);

        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);

        TrustManager[] wrappedTrustManagers = getWrappedTrustManagers(tmf.getTrustManagers());

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, wrappedTrustManagers, null);

        return sslContext.getSocketFactory();
    }*/


    private void addRetrievedOrders(JSONArray orders){
        try {
            for(int i = 0; i < orders.length(); i++) {
                JSONObject ordersJSON = orders.getJSONObject(i);
                this.orderJSONArrayList.add(ordersJSON);

                String namaMerchant = ordersJSON.getString("nama_merchant");
                String harga = ordersJSON.getString("harga");
                String kuantitas = ordersJSON.getString("kuantitas");

                String[] prices = harga.split(";");
                String[] splitKuantitas = kuantitas.split(";");
                int totalHarga = 0;

                for(int j = 0; j < prices.length; j++) {
                    int tempHargaItem = Integer.parseInt(prices[j]) * Integer.parseInt(splitKuantitas[j]);
                    totalHarga += tempHargaItem;
                }

                String waktuBayar = ordersJSON.getString("waktu_bayar");
                String status = "";

                if(waktuBayar.equals(null)){
                    status = "Masih dipesan";
                }
                else{
                    status = "Sudah dibayar";
                }

                String display = String.format("Merchant: %s%nTotal harga: %d%nStatus: %s", namaMerchant,
                        totalHarga, status);
                this.orderStringArrayList.add(display);

            }

            this.orderAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, orderStringArrayList);
            this.setListAdapter(this.orderAdapter);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private String getIDCustomer(){
        SharedPreferences sharedPreferences = this.getSharedPreferences(getString(R.string.shared_pref_appname), Context.MODE_PRIVATE);
        String idCustomer = sharedPreferences.getString(getString(R.string.shared_pref_id_user), "");
        return idCustomer;
    }

}
