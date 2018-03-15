package com.example.asusa455la.zxingembedded.utility;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;

import com.example.asusa455la.zxingembedded.model.Item;

/**
 * Created by ASUS A455LA on 11/01/2018.
 */

@Database(entities = {Item.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract AppDao itemDao();

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE `Item` (`idBarang` VARCHAR(20), "
                    + "`namaBarang` VARCHAR(70), `harga` INT, `stok` INT, "
                    + "PRIMARY KEY(`idBarang`))");

            database.execSQL("CREATE TABLE `Order` (`idOrder` INT, `waktuOrder` TIMESTAMP, " +
                    "`idMerchant` CHAR(10), PRIMARY KEY (`idOrder`))");

            database.execSQL("CREATE TABLE `OrderItems` (`idOrder` INT, `idBarang` VARCHAR(20)," +
                    "`kuantitas` INT)");
        }
    };

    private static AppDatabase INSTANCE;

    public static AppDatabase getDatabaseInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context,
                    AppDatabase.class, "qrpayment-database").allowMainThreadQueries().build();

            Room.databaseBuilder(context, AppDatabase.class, "qrpayment-database")
                    .addMigrations(MIGRATION_1_2).build();
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }
}
