package com.example.android.inventoryapp;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract.ProductEntry;

import java.io.IOException;

import static java.lang.Integer.parseInt;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int EXISTING_INVENTORY_LOADER_ID = 1;
    private static final int GET_IMAGE = 0;

    private EditText mNameEditText;
    private EditText mDescriptionEditText;
    private ImageView mThumbnailImageView;
    private EditText mQuantityEditText;
    private EditText mPriceEditText;

    private Uri mCurrItemUri;
    private Uri mImageUri;

    private boolean mProductHasChanged = false;
    private final View.OnTouchListener mTouchListener = new View.OnTouchListener() {
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

        mNameEditText = (EditText) findViewById(R.id.edit_product_name);
        mNameEditText.setOnTouchListener(mTouchListener);
        mDescriptionEditText = (EditText) findViewById(R.id.edit_product_description);
        mDescriptionEditText.setOnTouchListener(mTouchListener);
        mThumbnailImageView = (ImageView) findViewById(R.id.edit_image_preview);
        mQuantityEditText = (EditText) findViewById(R.id.edit_product_quantity);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setText(String.valueOf(0));
        mPriceEditText = (EditText) findViewById(R.id.edit_product_price);
        mPriceEditText.setOnTouchListener(mTouchListener);

        Button imageUriButton = (Button) findViewById(R.id.edit_button_select_image);
        imageUriButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                startActivityForResult(intent, GET_IMAGE);
            }
        });
        imageUriButton.setOnTouchListener(mTouchListener);

        Button increaseQuantityButton = (Button) findViewById(R.id.edit_quantity_increase);
        increaseQuantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int tmp = 0;
                try {
                    tmp = Integer.parseInt(mQuantityEditText.getText().toString().trim());
                } catch (NumberFormatException e) {
                }
                mQuantityEditText.setText(String.valueOf(++tmp));
            }
        });

        Button decreaseQuantityButton = (Button) findViewById(R.id.edit_quantity_decrease);
        decreaseQuantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int tmp = 0;
                try {
                    tmp = parseInt(mQuantityEditText.getText().toString().trim());
                } catch (NumberFormatException e) {
                }
                if (tmp > 0) --tmp;
                mQuantityEditText.setText(String.valueOf(tmp));
            }
        });

        Button orderMoreButton = (Button) findViewById(R.id.edit_order_more);
        orderMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String nameString = mNameEditText.getText().toString().trim();
                Intent mailIntent = new Intent(Intent.ACTION_SEND);
                mailIntent.setType("*/*");
                mailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.mail_intent_subject)
                        + nameString);
                if (mailIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mailIntent);
                }
            }
        });

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
            int nameColIdx = data.getColumnIndex(ProductEntry.COL_PRODUCT_NAME);
            mNameEditText.setText(data.getString(nameColIdx));

            int descriptionColIdx = data.getColumnIndex(ProductEntry.COL_PRODUCT_DESCRIPTION);
            mDescriptionEditText.setText(data.getString(descriptionColIdx));

            int imageUriColIdx = data.getColumnIndex(ProductEntry.COL_PRODUCT_IMG_URI);
            mImageUri = Uri.parse(data.getString(imageUriColIdx));

            int quantityColIdx = data.getColumnIndex(ProductEntry.COL_PRODUCT_QUANTITY);
            mQuantityEditText.setText(String.valueOf(data.getInt(quantityColIdx)));

            int priceColIdx = data.getColumnIndex(ProductEntry.COL_PRODUCT_PRICE);
            float price = (float) data.getInt(priceColIdx);
            if (price > 0) price = price / 100;
            mPriceEditText.setText(String.valueOf(price));

            if (mImageUri != null) refreshThumbnail();

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
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener = new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    }
                };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }
        OnClickListener discardButtonClickListener = new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        };
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GET_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                mImageUri = data.getData();
                //this is only way i found that doesn't cry about errors.
                getContentResolver().takePersistableUriPermission(mImageUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION);
                refreshThumbnail();
            }
        }
    }


    //HELPERS
    private boolean saveProduct() {
        String nameString = mNameEditText.getText().toString().trim();
        if (TextUtils.isEmpty(nameString)) {
            Toast.makeText(this, getString(R.string.editor_data_check_no_name),
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        String descriptionString = mDescriptionEditText.getText().toString().trim();

        if (mImageUri == null) {
            Toast.makeText(this, R.string.editor_data_check_no_image, Toast.LENGTH_SHORT).show();
            return false;
        }

        String quantityString = mQuantityEditText.getText().toString().trim();
        int quantity = 0;
        if (!TextUtils.isEmpty(quantityString)) {
            try {
                quantity = parseInt(quantityString);
            } catch (NumberFormatException e) {
            }
            if (quantity < 0) {
                Toast.makeText(this, getString(R.string.editor_data_check_negative_quantity),
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        String priceString = mPriceEditText.getText().toString().trim();

        int price = 0;
        if (!TextUtils.isEmpty(priceString)) {
            float tmp = 0;
            try {
                tmp = Float.parseFloat(priceString);
            } catch (NumberFormatException e) {
            }
            if (tmp > 0) price = (int) (tmp * 100);
            else if (tmp < 0) {
                Toast.makeText(this, getString(R.string.editor_data_check_negative_price),
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        if (mCurrItemUri == null && TextUtils.isEmpty(nameString)
                && TextUtils.isEmpty(descriptionString) && mImageUri == null
                && TextUtils.isEmpty(quantityString) && TextUtils.isEmpty(priceString)) {
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(ProductEntry.COL_PRODUCT_NAME, nameString);
        values.put(ProductEntry.COL_PRODUCT_DESCRIPTION, descriptionString);
        values.put(ProductEntry.COL_PRODUCT_IMG_URI, mImageUri.toString());
        values.put(ProductEntry.COL_PRODUCT_QUANTITY, quantity);
        values.put(ProductEntry.COL_PRODUCT_PRICE, price);

        if (mCurrItemUri == null) {
            Uri uri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
            if (uri == null) {
                Toast.makeText(this, getString(R.string.editor_toast_product_save_error),
                        Toast.LENGTH_SHORT).show();
                return false;
            } else {
                Toast.makeText(this, getString(R.string.editor_toast_product_save_success)
                        + uri.toString(), Toast.LENGTH_SHORT).show();
                return true;
            }
        } else {
            int rowsAffected = getContentResolver().update(mCurrItemUri, values, null, null);
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

    private void refreshThumbnail() {
        if (mImageUri != null) {
            boolean success = false;
            Bitmap newImage = null;
            try {
                newImage = Utils.getBitmap(this, mImageUri, Utils.DEFAULT_BITMAP_SCALE);
                success = true;
            } catch (IOException e) {
                Toast.makeText(this, "ERROR: Something went wrong", Toast.LENGTH_SHORT).show();
            }
            if (success && newImage != null) mThumbnailImageView.setImageBitmap(newImage);
        }
    }

    //DIALOGS
    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_delete_entry_msg);
        builder.setPositiveButton(R.string.dialog_delete_confirm, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.dialog_delete_cancel, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showUnsavedChangesDialog(final OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_discard_msg);
        builder.setPositiveButton(R.string.dialog_discard_confirm, discardButtonClickListener);
        builder.setNegativeButton(R.string.dialog_discard_cancel, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
