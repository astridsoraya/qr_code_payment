package com.example.asusa455la.zxingembedded.view.merchant;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.example.asusa455la.zxingembedded.R;
import com.example.asusa455la.zxingembedded.model.Item;
import com.example.asusa455la.zxingembedded.utility.AppController;
import com.example.asusa455la.zxingembedded.utility.AppDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MerchantOrder extends AppCompatActivity {
    private static String urlOrderItems = "https://qrcodepayment.ddns.net/add_order_items.php";

    private AppDatabase appDatabase;

    private ArrayList<Item> itemsList;

    private OrderItemAdapter orderItemAdapter;

    private ListView itemListView;
    private Button orderItemButton;

    private int totalHarga;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_order);

        this.appDatabase = AppDatabase.getDatabaseInstance(this);
        this.itemsList = new ArrayList<Item>();

        this.itemListView = (ListView) findViewById(R.id.mOrderListView);

        this.orderItemButton = (Button) findViewById(R.id.mOrderItemsButton);
        this.orderItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                order();
            }
        });

        showItemsList();
    }


    // order quantity and
    private void order(){
        int[] quantityEachItem = orderItemAdapter.getQuantityArray();
        int totalItem = 0;

        for(int i = 0; i < quantityEachItem.length; i++){
            totalItem+=quantityEachItem[i];
        }

        if(totalItem == 0){
            CharSequence text = "No items to order!";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(this, text, duration);
            toast.show();
        }
        else{
            addItemsOrder();
        }
    }

    private void addItemsOrder(){
        String tag_string = "string_req";
        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Ordering items...");
        pDialog.show();

        StringRequest strRequest = new StringRequest(Request.Method.POST, urlOrderItems,
                    new Response.Listener<String>()
                    {
                        @Override
                        public void onResponse(String response)
                        {
                            Log.d(AppController.TAG, response.toString());

                            try {
                                JSONObject itemResponse = new JSONObject(response);
                                String success = itemResponse.getString("success");
                                String message = itemResponse.getString("message");

                                if(success.equals("1")){
                                    pDialog.hide();
                                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                                    String idOrder = itemResponse.getString("id_order");
                                    showQRCodePayment(idOrder);

                                }
                                else{
                                    pDialog.hide();
                                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(getApplicationContext(), "System failed to add items order", Toast.LENGTH_SHORT).show();
                        }
                    })
            {
                @Override
                protected Map<String, String> getParams()
                {
                    Map<String, String> params = new HashMap<String, String>();
                    JSONObject jsonObject = new JSONObject();

                    int[] quantityEachItem = orderItemAdapter.getQuantityArray();
                    int counter = 0;

                    for(int i = 0; i < quantityEachItem.length; i++){
                        if(quantityEachItem[i] != 0){
                            params.put("id_merchant", getIDMerchant());
                            params.put(String.format("id_barang_%s",counter), itemsList.get(i).getIdBarang());
                            params.put(String.format("kuantitas_%s",counter), quantityEachItem[i]+"");

                            counter++;
                        }
                    }

                    return params;
                }
            };

            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(strRequest, tag_string);

    }

    private void showQRCodePayment(String idOrder){
            /*for(int i = 0; i < items.length(); i++) {*/
                //JSONObject itemJson = items.getJSONObject(0);
/*

                String idBarang = itemJson.getString("id_barang");
                int kuantitas = Integer.parseInt(itemJson.getString("kuantitas"));
                String waktuOrder = itemJson.getString("waktu_order");*/

                //orderListString += String.format("%s", idOrder);

/*                if(i < items.length() - 1){
                    orderListString += "|";
                }*/

            /*}*/

            Intent intent = new Intent(this, MerchantInsertUsername.class);
            intent.putExtra("QRCodeData", idOrder+";"+getNamaMerchant()+";" + totalHarga);
            this.onPause();
            startActivity(intent);

    }

    private String getIDMerchant(){
        SharedPreferences sharedPreferences = this.getSharedPreferences(getString(R.string.shared_pref_appname), Context.MODE_PRIVATE);
        String idMerchant = sharedPreferences.getString(getString(R.string.shared_pref_id_user), "");
        return idMerchant;
    }

    private String getNamaMerchant(){
        SharedPreferences sharedPreferences = this.getSharedPreferences(getString(R.string.shared_pref_appname), Context.MODE_PRIVATE);
        String namaMerchant = sharedPreferences.getString(getString(R.string.shared_pref_merchant_name), "");
        return namaMerchant;
    }

    private void showItemsList(){
        List<Item> items = this.appDatabase.itemDao().getAllItems();

        for (Item item: items) {
            this.itemsList.add(item);
        }

        this.orderItemAdapter = new OrderItemAdapter(this, R.layout.order_item, itemsList);
        itemListView.setAdapter(this.orderItemAdapter);
    }

    private class OrderItemAdapter extends ArrayAdapter<Item> {
        private int[] quantityArray;

        public OrderItemAdapter(Context context, int textViewResourceId, ArrayList<Item> items) {
            super(context, textViewResourceId, items);

            quantityArray = new int[items.size()];
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = convertView;

            if (view == null) {
                convertView = LayoutInflater.from(this.getContext())
                        .inflate(R.layout.order_item, parent, false);
            }

            final Item item = getItem(position);

            if (item != null) {
                TextView namaBarang = (TextView) convertView.findViewById(R.id.mOrderItemName);
                final EditText quantity = (EditText) convertView.findViewById(R.id.mOrderItemQty);
                final TextView harga = (TextView) convertView.findViewById(R.id.mOrderItemPrice);

                Button addQuantity = (Button) convertView.findViewById(R.id.mOrderAddQty);
                Button minQuantity = (Button) convertView.findViewById(R.id.mOrderMinQty);

                namaBarang.setText(item.getNamaBarang());

                final int angkaHarga = item.getHarga();
                harga.setText(angkaHarga+"");
                addQuantity.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int tempQuantity = Integer.parseInt(quantity.getText().toString());
                        tempQuantity++;
                        quantity.setText(tempQuantity+"");

                        int tempHarga = angkaHarga;
                        tempHarga *= Integer.parseInt(quantity.getText().toString());
                        harga.setText(tempHarga+"");

                        quantityArray[position] = tempQuantity;

                        totalHarga+= angkaHarga;
                        System.out.println("Total Harga sementara: " + totalHarga);
                    }
                });
                minQuantity.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int tempQuantity = Integer.parseInt(quantity.getText().toString());
                        if(tempQuantity > 0){
                            tempQuantity--;
                            quantity.setText(tempQuantity+"");

                            totalHarga -= angkaHarga;
                            System.out.println("Total Harga sementara: " + totalHarga);
                        }

                        int tempHarga = angkaHarga;
                        tempHarga *= Integer.parseInt(quantity.getText().toString());
                        harga.setText(tempHarga+"");

                        quantityArray[position] = tempQuantity;
                    }
                });
            }

            return convertView;
        }

        public int[] getQuantityArray(){
            return quantityArray;
        }
    }



}
