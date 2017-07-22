package com.example.android.inventoryapp;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.inventoryapp.data.InventoryContract.ProductEntry;
import java.io.IOException;

import static android.util.Log.wtf;

public class InventoryCursorAdapter extends CursorAdapter {
    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item,parent,false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView nameTextView = (TextView) view.findViewById(R.id.list_item_name);
        TextView descriptionTextView = (TextView) view.findViewById(R.id.list_item_description);
        ImageView imageView = (ImageView) view.findViewById(R.id.list_item_image_view);
        TextView quantityTextView = (TextView) view.findViewById(R.id.list_item_quantity);
        TextView priceTextView = (TextView) view.findViewById(R.id.list_item_price);

        int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COL_PRODUCT_NAME);
        int descColumnIndex = cursor.getColumnIndex(ProductEntry.COL_PRODUCT_DESCRIPTION);
        int imageUriColumnIndex = cursor.getColumnIndex(ProductEntry.COL_PRODUCT_IMG_URI);
        int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COL_PRODUCT_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COL_PRODUCT_PRICE);

        nameTextView.setText(cursor.getString(nameColumnIndex));
        descriptionTextView.setText(cursor.getString(descColumnIndex));
        String imageUri = cursor.getString(imageUriColumnIndex);
        if (!TextUtils.isEmpty(imageUri)){
            Bitmap bitmap = getBitmap(context, Uri.parse(imageUri), 512);
            imageView.setImageBitmap(bitmap);
        }
        quantityTextView.setText(Integer.toString(cursor.getInt(quantityColumnIndex)));
        priceTextView.setText(Integer.toString(cursor.getInt(priceColumnIndex)));
    }
//todo clean, add viewholder, add values resource
    private Bitmap getBitmap(Context context, Uri imageUri, int MaxWidth){
    Bitmap bitmap = null;
    try {
        Bitmap temp = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
        int newHeight = (int) (temp.getHeight() * ((float)MaxWidth / temp.getWidth()));
        if (temp != null) bitmap = Bitmap.createScaledBitmap(temp,
                MaxWidth,
                newHeight,
                true );
    } catch (IOException e){
        wtf("getBitmap", e);
    }
    return bitmap;
}


}
