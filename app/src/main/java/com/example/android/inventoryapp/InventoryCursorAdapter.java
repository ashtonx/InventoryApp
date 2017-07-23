package com.example.android.inventoryapp;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.inventoryapp.data.InventoryContract.ProductEntry;

public class InventoryCursorAdapter extends CursorAdapter {

    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        viewHolder holder = new viewHolder(view);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        viewHolder holder = (viewHolder) view.getTag();

        final int nameColIdx = cursor.getColumnIndex(ProductEntry.COL_PRODUCT_NAME);
        holder.name.setText(cursor.getString(nameColIdx));

        final int descriptionColIdx = cursor.getColumnIndex(
                ProductEntry.COL_PRODUCT_DESCRIPTION);
        holder.description.setText(cursor.getString(descriptionColIdx));

        final int imageUriColIdx = cursor.getColumnIndex(ProductEntry.COL_PRODUCT_IMG_URI);
        String imageUri = cursor.getString(imageUriColIdx);
        if (TextUtils.isEmpty(imageUri)) {
            holder.image.setImageResource(R.drawable.ic_image_black);
            holder.image.invalidate();
        } else {
            Bitmap bitmap = Utils.getBitmap(context, Uri.parse(imageUri),
                    Utils.DEFAULT_BITMAP_SCALE);
            holder.image.setImageBitmap(bitmap);
        }

        final int quantityColIdx = cursor.getColumnIndex(ProductEntry.COL_PRODUCT_QUANTITY);
        final int quantity = cursor.getInt(quantityColIdx);
        final String quantityDisplay = context.getString(R.string.list_item_quantity)
                + " " + String.valueOf(quantity);
        holder.quantity.setText(quantityDisplay);

        final int priceColIdx = cursor.getColumnIndex(ProductEntry.COL_PRODUCT_PRICE);
        float price = (float) cursor.getInt(priceColIdx);
        if (price > 0) price = (price / 100);
        final String priceDisplay = context.getString(R.string.list_item_price)
                + " " + String.valueOf(price);
        holder.price.setText(priceDisplay);

        final int productIdColIdx = cursor.getColumnIndex(ProductEntry._ID);
        final long productId = cursor.getLong(productIdColIdx);
        holder.sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.sellItem(context, productId, quantity);
            }
        });
    }

    private static class viewHolder {
        TextView name;
        TextView description;
        ImageView image;
        TextView quantity;
        TextView price;
        Button sellButton;

        viewHolder(View view) {
            name = (TextView) view.findViewById(R.id.list_item_name);
            description = (TextView) view.findViewById(R.id.list_item_description);
            image = (ImageView) view.findViewById(R.id.list_item_image_view);
            quantity = (TextView) view.findViewById(R.id.list_item_quantity);
            price = (TextView) view.findViewById(R.id.list_item_price);
            sellButton = (Button) view.findViewById(R.id.list_item_button_sell);
        }
    }
}
