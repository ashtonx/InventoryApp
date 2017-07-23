package com.example.android.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.inventoryapp.data.InventoryContract.ProductEntry;

class InventoryDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "products.db";
    private static final int DATABASE_VERSION = 1;

    private static final String SQL_CREATE_PRODUCT_ENTRIES =
            "CREATE TABLE " + ProductEntry.TABLE_NAME + " ("
                    + ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + ProductEntry.COL_PRODUCT_NAME + " TEXT NOT NULL,"
                    + ProductEntry.COL_PRODUCT_DESCRIPTION + " TEXT,"
                    + ProductEntry.COL_PRODUCT_IMG_URI + " TEXT,"
                    + ProductEntry.COL_PRODUCT_QUANTITY + " INTEGER NOT NULL DEFAULT 0,"
                    + ProductEntry.COL_PRODUCT_PRICE + " INTEGER NOT NULL DEFAULT 0);";

    private static final String SQL_DELETE_PRODUCT_ENTRIES =
            "DROP TABLE IF EXISTS " + ProductEntry.TABLE_NAME;

    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_PRODUCT_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_PRODUCT_ENTRIES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
