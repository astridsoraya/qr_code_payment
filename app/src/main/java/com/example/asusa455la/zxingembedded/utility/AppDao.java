package com.example.asusa455la.zxingembedded.utility;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.example.asusa455la.zxingembedded.model.Item;
import com.example.asusa455la.zxingembedded.model.Order;
import com.example.asusa455la.zxingembedded.model.OrderItems;

import java.util.List;

/**
 * Created by ASUS A455LA on 11/01/2018.
 */

@Dao
public interface AppDao {
    // Items
    @Query("SELECT idBarang, namaBarang, harga, stok FROM Item")
    public List<Item> getAllItems();

    @Query("SELECT COUNT(*) FROM Item")
    public int getTotalItems();

    @Insert
    public void insertItem(Item item);

    @Delete
    public void deleteItem(Item item);

    @Query("DELETE FROM Item")
    public void deleteAllItems();

    @Update
    public void updateItem(Item item);


    /*//History
    @Query("SELECT idOrder, waktuOrder, statusOrder, waktuBayar, idCustomer, kuantitas FROM `Order` INNER JOIN OrderItems ON `Order`.idOrder=OrderItems.idOrder WHERE idMerchant = merchantID")
    public List<Order> getAllOrders(String merchantID);*/


    //Order
/*    @Insert
    public void insertOrder(Order order);*/

/*    @Delete
    public void deleteOrder(Order order);

    @Update
    public void updateOrder(Order order);*/


    //Order Items
/*    @Insert
    public void insertOrderItems(OrderItems orderItems);*/

/*    @Delete
    public void deleteOrderItems(OrderItems orderItems);

    @Update
    public void updateOrderItems(OrderItems orderItems);*/

}
