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
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
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
    private static final int GET_IMAGE = 0;

    private EditText mNameEditText;
    private EditText mDescriptionEditText;
    private EditText mQuantityEditText;
    private EditText mPriceEditText;

    private Uri mCurrItemUri;
    private Uri mImageUri;

    boolean mProductHasChanged = false;
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
            setTitle(getString(R.string.editor_title_product_add));
            invalidateOptionsMenu();
        } else setTitle(getString(R.string.editor_title_product_edit));

        //todo change later
        mNameEditText = (EditText) findViewById(R.id.edit_product_name);
        mNameEditText.setOnTouchListener(mTouchListener);
        mDescriptionEditText = (EditText) findViewById(R.id.edit_product_description);
        mDescriptionEditText.setOnTouchListener(mTouchListener);
        Button mImageUriButton = (Button) findViewById(R.id.edit_button_select_image);
        mImageUriButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(intent, GET_IMAGE);
            }
        });
        mImageUriButton.setOnTouchListener(mTouchListener);
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
                if (saveProduct()) finish();
                return true;
            case R.id.action_delete:
                deleteProduct();
                return true;
            case android.R.id.home:
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }
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
        if (requestCode == GET_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri selectedImageUri = data.getData();
                mImageUri = Uri.parse(getRealPathFromURI(selectedImageUri));
            }
        }
    }


    //helpers
    private boolean saveProduct() {
        String nameString = mNameEditText.getText().toString().trim();
        String descriptionString = mDescriptionEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();

        if (mCurrItemUri == null && TextUtils.isEmpty(nameString)
                && TextUtils.isEmpty(descriptionString) && mImageUri == null
                && TextUtils.isEmpty(quantityString) && TextUtils.isEmpty(priceString)) {
            return false;
        }

        Integer quantity = null;
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = parseQuantity(quantityString);
            if (quantity == null) return false;
        }
        Integer price = null;
        if (!TextUtils.isEmpty(priceString)) {
            price = parsePrice(priceString);
            if (price == null) return false;
        }

        boolean savedStatus = false;
        if (checkInput(nameString, quantity, price)) {
            ContentValues values = new ContentValues();
            if (!TextUtils.isEmpty(nameString)) {
                values.put(ProductEntry.COL_PRODUCT_NAME, nameString);
            }
            if (!TextUtils.isEmpty(descriptionString)) {
                values.put(ProductEntry.COL_PRODUCT_DESCRIPTION, descriptionString);
            }
            if (mImageUri!=null){
                values.put(ProductEntry.COL_PRODUCT_IMG_URI, mImageUri.toString());
            }
            if (quantity != null) values.put(ProductEntry.COL_PRODUCT_QUANTITY, quantity);
            if (price != null) values.put(ProductEntry.COL_PRODUCT_PRICE, price);

            if (mCurrItemUri == null) {
                Uri tempUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
                savedStatus = checkIfSaved(tempUri);
            } else {
                int rowsAffected = getContentResolver().update(mCurrItemUri, values, null, null);
                savedStatus = checkIfUpdated(rowsAffected);
            }
        }
        return savedStatus;
    }

    private void deleteProduct() {
        if (mCurrItemUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrItemUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_toast_product_delete_error),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_toast_product_delete_success),
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    private String getRealPathFromURI(Uri contentUri) {
        String resource = null;
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, projection, null, null, null);
        if (cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            resource = "file://" + cursor.getString(columnIndex);
        }
        cursor.close();
        return resource;
    }

    private Integer parseQuantity(String quantityString) {
        boolean parsed = false;
        Integer quantity = null;
        try {
            quantity = Integer.parseInt(quantityString);
            parsed = true;
        } catch (NumberFormatException e) {
            return null;
        } finally {
            if (!parsed) {
                Toast.makeText(this, getString(R.string.editor_data_check_quantity),
                        Toast.LENGTH_SHORT).show();
            }
        }
        return quantity;
    }

    private Integer parsePrice(String priceString) {
        boolean parsed = false;
        Integer price = null;
        Float temp;
        try {
            temp = Float.parseFloat(priceString);
            parsed = true;
        } catch (NumberFormatException e) {
            return null;
        } finally {
            if (!parsed) {
                Toast.makeText(this, getString(R.string.editor_data_check_price),
                        Toast.LENGTH_SHORT).show();
            }
        }
        price = (int) (temp * 100);
        return price;
    }

    //checks
    private boolean checkInput(String name, Integer quantity, Integer price) {
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, getString(R.string.editor_data_check_no_name),
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        if (quantity != null && quantity < 0) {
            Toast.makeText(this, getString(R.string.editor_data_check_negative_quantity),
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        if (price != null && price < 0) {
            Toast.makeText(this, getString(R.string.editor_data_check_negative_price),
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean checkIfSaved(Uri uri) {
        if (uri == null) {
            Toast.makeText(this, getString(R.string.editor_toast_product_save_error),
                    Toast.LENGTH_SHORT).show();
            return false;
        } else {
            Toast.makeText(this, getString(R.string.editor_toast_product_save_success)
                    + uri.toString(), Toast.LENGTH_SHORT).show();
            return true;
        }
    }

    private boolean checkIfUpdated(int rowsAffected) {
        if (rowsAffected == 0) {
            Toast.makeText(this, getString(R.string.editor_toast_product_save_error),
                    Toast.LENGTH_SHORT).show();
            return false;
        } else {
            Toast.makeText(this, getString(R.string.editor_toast_product_update_success),
                    Toast.LENGTH_SHORT).show();
            return true;
        }
    }
}
