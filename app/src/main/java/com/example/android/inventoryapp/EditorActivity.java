package com.example.android.inventoryapp;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract.ProductEntry;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int EXISTING_INVENTORY_LOADER_ID = 1;
    private static final int SELECT_FILE = 1;

    private EditText mNameEditText;
    private EditText mDescriptionEditText;
    private EditText mQuantityEditText;
    private EditText mPriceEditText;

    private Uri mCurrItemUri;
    private Uri mImageUri;

    private boolean mProductHasChanged = false;
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mProductHasChanged = true;
            return false;
        }
    };

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        mCurrItemUri = intent.getData();

        if (mCurrItemUri == null) {
            setTitle("Add a product");
            invalidateOptionsMenu();
        } else setTitle("Edit product");

        //todo change later
        mNameEditText = (EditText) findViewById(R.id.edit_product_name);
        mNameEditText.setOnTouchListener(mTouchListener);
        mDescriptionEditText = (EditText) findViewById(R.id.edit_product_description);
        mDescriptionEditText.setOnTouchListener(mTouchListener);
        Button mImageUriButton = (Button) findViewById(R.id.edit_button_select_image);
        mImageUriButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
            }
        });
        mQuantityEditText = (EditText) findViewById(R.id.edit_product_quantity);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mPriceEditText = (EditText) findViewById(R.id.edit_product_price);
        mPriceEditText.setOnTouchListener(mTouchListener);

        if (mCurrItemUri != null) {
            getLoaderManager().initLoader(EXISTING_INVENTORY_LOADER_ID, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                ProductEntry.COL_PRODUCT_NAME,
                ProductEntry.COL_PRODUCT_DESCRIPTION,
                ProductEntry.COL_PRODUCT_IMG_URI,
                ProductEntry.COL_PRODUCT_QUANTITY,
                ProductEntry.COL_PRODUCT_PRICE
        };
        return new CursorLoader(this, mCurrItemUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {
            int nameColumnIndex = data.getColumnIndex(ProductEntry.COL_PRODUCT_NAME);
            int descriptionColumnIndex = data.getColumnIndex(ProductEntry.COL_PRODUCT_DESCRIPTION);
            int imageUriColumnIndex = data.getColumnIndex(ProductEntry.COL_PRODUCT_IMG_URI);
            int quantityColumnIndex = data.getColumnIndex(ProductEntry.COL_PRODUCT_QUANTITY);
            int priceColumnIndex = data.getColumnIndex(ProductEntry.COL_PRODUCT_PRICE);

            mNameEditText.setText(data.getString(nameColumnIndex));
            mDescriptionEditText.setText(data.getString(descriptionColumnIndex));
            mImageUri = Uri.parse(data.getString(imageUriColumnIndex));
            mQuantityEditText.setText(Integer.toString(data.getInt(quantityColumnIndex)));
            mPriceEditText.setText(Integer.toString(data.getInt(priceColumnIndex)));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
//        mNameEditText.setText("");
//        mDescriptionEditText.setText("");
//        mQuantityEditText.setText("0");
//        mPriceEditText.setText("0");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mCurrItemUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveProduct();
                finish();
                return true;
            case R.id.action_delete:
                deleteProduct();
                return true;
            case android.R.id.home:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

//    @Override
//    public void onBackPressed() {
//
//        super.onBackPressed();
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_FILE && resultCode == Activity.RESULT_OK) {
            if (data != null)
                mImageUri = data.getData();
        }
    }


    //helpers
    private void saveProduct() {
        String nameString = mNameEditText.getText().toString().trim();
        String descriptionString = mDescriptionEditText.getText().toString().trim();
        String imageUriString = mImageUri.toString();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();

        ContentValues values = new ContentValues();
        values.put(ProductEntry.COL_PRODUCT_NAME, nameString);
        values.put(ProductEntry.COL_PRODUCT_DESCRIPTION, descriptionString);
        values.put(ProductEntry.COL_PRODUCT_IMG_URI, imageUriString);
        int quantity = 0;
        if (!TextUtils.isEmpty(quantityString)) quantity = Integer.parseInt(quantityString);
        values.put(ProductEntry.COL_PRODUCT_QUANTITY, quantity);
        int price = 0;
        if (!TextUtils.isEmpty(priceString)) price = Integer.parseInt(priceString);
        values.put(ProductEntry.COL_PRODUCT_PRICE, price);

        if (mCurrItemUri == null) {
            Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
            if (newUri == null) {
                Toast.makeText(this, "Error saving product", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Product saved with row id: " + newUri.toString(),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(mCurrItemUri, values, null, null);
            if (rowsAffected == 0) {
                Toast.makeText(this, "Error saving product", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Product updated", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void deleteProduct() {
        if (mCurrItemUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrItemUri, null, null);

            if (rowsDeleted == 0) {
                Toast.makeText(this, "Error deleting product", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Product deleted", Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

}
