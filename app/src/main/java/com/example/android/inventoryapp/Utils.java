package com.example.android.inventoryapp;


import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract;

import java.io.IOException;

import static android.util.Log.wtf;

public final class Utils {
        Utils(){}
    public static final int DEFAULT_BITMAP_SCALE = 512;

    public static Bitmap getBitmap(Context context, Uri imageUri, int MaxWidth) {
        Bitmap bitmap = null;
        try {
            Bitmap temp = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
            int newHeight = (int) (temp.getHeight() * ((float) MaxWidth / temp.getWidth()));
            bitmap = Bitmap.createScaledBitmap(temp,
                    MaxWidth,
                    newHeight,
                    true);
        } catch (IOException e) {
            wtf("getBitmap", e);
        }
        return bitmap;
    }

    public static void sellItem(Context context, long productId, int quantity) {
        if (quantity > 0) {
            Uri currProductUri = ContentUris.withAppendedId(InventoryContract.ProductEntry.CONTENT_URI, productId);
            ContentValues values = new ContentValues();
            values.put(InventoryContract.ProductEntry.COL_PRODUCT_QUANTITY, --quantity);
            int rowsAffected = context.getContentResolver().update(
                    currProductUri, values, null, null);
            if (rowsAffected == 0) {
                Toast.makeText(context, context.getString(R.string.sell_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, context.getString(R.string.sell_product_success),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, context.getString(R.string.sell_button_no_stock),
                    Toast.LENGTH_SHORT).show();
        }
    }
}
