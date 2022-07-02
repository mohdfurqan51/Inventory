package com.example.inventory;

import static com.example.inventory.Data.InventoryContract.ProductEntry.COLUMN_PRODUCT_IMAGE;
import static com.example.inventory.Data.InventoryContract.ProductEntry.COLUMN_PRODUCT_PRICE;
import static com.example.inventory.Data.InventoryContract.ProductEntry.CONTENT_URI;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.example.inventory.Data.InventoryContract;

import java.io.ByteArrayOutputStream;

public class Detail_Activity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private Uri mCurrentProductUri;
    private static final int EXISTING_PRODUCT_LOADER = 0;

    private boolean mProductHasChanged = false;

    private EditText mNameEditText;
    private EditText mQuantityEditText;
    private EditText mPriceEditText;
    ImageView imageView;
    private ProgressBar spinner;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mProductHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();
        spinner = findViewById(R.id.progressBar);

        if (mCurrentProductUri == null) {
            setTitle("Add a Product");
        }
        else {
            setTitle("Product Details");
            LoaderManager.getInstance(this).initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        mNameEditText = findViewById(R.id.edit_product_name);
        mQuantityEditText = findViewById(R.id.edit_product_quantity);
        mPriceEditText = findViewById(R.id.edit_product_price);
        imageView = findViewById(R.id.productImage);
        spinner.setVisibility(View.INVISIBLE);

        mNameEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);


    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Delete this pet ?");
        builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteProduct();
            }
        });

        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteProduct() {
        if (mCurrentProductUri != null) {
            // Call the ContentResolver to delete the pet at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentPetUri
            // content URI already identifies the pet that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, "delete pet failed",
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, "delete pet successful",
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();

    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        if (mCurrentProductUri != null) {
            MenuItem menuItem = menu.findItem(R.id.action_order);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_order:
                spinner.setVisibility(View.VISIBLE);
                new Thread( new Runnable() { @Override public void run() {
                    Looper.prepare();
                    saveProduct();
                    finish();
                } } ).start();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(Detail_Activity.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(Detail_Activity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton("Discard", discardButtonClickListener);
        builder.setNegativeButton("Keep Editing", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        android.app.AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void saveProduct() {

        String name = mNameEditText.getText().toString().toUpperCase().toLowerCase().trim();
        String quantity = mQuantityEditText.getText().toString().trim();
        String price = mPriceEditText.getText().toString().trim();



        if (mCurrentProductUri == null && TextUtils.isEmpty(name) && TextUtils.isEmpty(quantity)
        && TextUtils.isEmpty(price)) {
            return;
        }

        int mQuantity = Integer.parseInt(quantity);
        int mPrice = Integer.parseInt(price);


        byte[] v = nameToImage(name);
        ContentValues values = new ContentValues();
        values.put(InventoryContract.ProductEntry.COLUMN_PRODUCT_NAME, name);
        values.put(InventoryContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, mQuantity);
        values.put(COLUMN_PRODUCT_IMAGE, v);
        values.put(COLUMN_PRODUCT_PRICE, mPrice);


        if (mCurrentProductUri == null) {

            Uri newUri = getContentResolver().insert(CONTENT_URI,values);

            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, "Insert product failed",
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, "insert product successful",
                        Toast.LENGTH_SHORT).show();
            }
        }
        else {
            int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);

            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, "update product failed",
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, "update product successful",
                        Toast.LENGTH_SHORT).show();


            }
        }
    }


    public byte[] nameToImage(String name) {

        Bitmap b;

        switch (name) {
            case "bread":
                b = BitmapFactory.decodeResource(getResources(), R.drawable.bread);
                break;
            case "eggs":
                b = BitmapFactory.decodeResource(getResources(), R.drawable.eggs);
                break;
            case "jam":
                b = BitmapFactory.decodeResource(getResources(), R.drawable.jam);
                break;
            case "rice":
                b = BitmapFactory.decodeResource(getResources(), R.drawable.rice);
                break;
            case "sugar":
                b = BitmapFactory.decodeResource(getResources(), R.drawable.sugar);
                break;
            case "sanitizer":
                b = BitmapFactory.decodeResource(getResources(), R.drawable.sanitizer);
                break;
            case "shampoo":
                b = BitmapFactory.decodeResource(getResources(), R.drawable.shampoo);
                break;
            case "soap":
                b = BitmapFactory.decodeResource(getResources(), R.drawable.soap);
                break;
            case "tedhemedhe":
                b = BitmapFactory.decodeResource(getResources(), R.drawable.tedhemedhe);
                break;
            case "toffee":
                b = BitmapFactory.decodeResource(getResources(), R.drawable.toffee);
                break;
            default:
                b = BitmapFactory.decodeResource(getResources(), R.drawable.noimage);
        }
        return selectImage(b);
    }

    private byte[] selectImage(Bitmap b) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.PNG, 100, bos);
        byte[] img = bos.toByteArray();
        return img;
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        spinner.setVisibility(View.VISIBLE);
        String[] projection = {
                InventoryContract.ProductEntry._ID,
                COLUMN_PRODUCT_IMAGE,
                InventoryContract.ProductEntry.COLUMN_PRODUCT_NAME,
                InventoryContract.ProductEntry.COLUMN_PRODUCT_QUANTITY,
                COLUMN_PRODUCT_PRICE
        };
        return new CursorLoader(this,
                mCurrentProductUri,projection,null,null,null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_NAME);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_PRICE);

            String productName = cursor.getString(nameColumnIndex);
            int productQuantity = cursor.getInt(quantityColumnIndex);
            int productPrice = cursor.getInt(priceColumnIndex);

            mNameEditText.setText(productName);
            mQuantityEditText.setText(Integer.toString(productQuantity));
            mPriceEditText.setText(Integer.toString(productPrice));
        }

    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mNameEditText.setText("");
        mQuantityEditText.setText("");
        mPriceEditText.setText("");
    }
}
