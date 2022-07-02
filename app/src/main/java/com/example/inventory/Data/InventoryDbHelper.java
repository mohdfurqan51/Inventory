package com.example.inventory.Data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.inventory.Data.InventoryContract.ProductEntry;

import androidx.annotation.Nullable;

public class InventoryDbHelper extends SQLiteOpenHelper {
    private static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + ProductEntry.TABLE_NAME + " (" +
            ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            ProductEntry.COLUMN_PRODUCT_IMAGE + " BLOB," +
            ProductEntry.COLUMN_PRODUCT_NAME + " TEXT," +
            ProductEntry.COLUMN_PRODUCT_QUANTITY + " INTEGER," +
            ProductEntry.COLUMN_PRODUCT_PRICE + " INTEGER)";


    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + ProductEntry.TABLE_NAME;

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "products.db";

    public InventoryDbHelper(@Nullable Context context) {
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
}
