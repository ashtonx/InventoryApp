package com.example.android.inventoryapp.data;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.inventoryapp.R;
import com.example.android.inventoryapp.data.InventoryContract.ProductEntry;

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
        TextView imageUriTextView = (TextView) view.findViewById(R.id.list_item_image_uri);
        TextView quantityTextView = (TextView) view.findViewById(R.id.list_item_quantity);
        TextView priceTextView = (TextView) view.findViewById(R.id.list_item_price);

        int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COL_PRODUCT_NAME);
        int descColumnIndex = cursor.getColumnIndex(ProductEntry.COL_PRODUCT_DESCRIPTION);
        int imageUriColumnIndex = cursor.getColumnIndex(ProductEntry.COL_PRODUCT_IMG_URI);
        int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COL_PRODUCT_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COL_PRODUCT_PRICE);

        nameTextView.setText(cursor.getString(nameColumnIndex));
        descriptionTextView.setText(cursor.getString(descColumnIndex));
        imageUriTextView.setText(cursor.getString(imageUriColumnIndex));
        quantityTextView.setText(Integer.toString(cursor.getInt(quantityColumnIndex)));
        priceTextView.setText(Integer.toString(cursor.getInt(priceColumnIndex)));
    }
}
