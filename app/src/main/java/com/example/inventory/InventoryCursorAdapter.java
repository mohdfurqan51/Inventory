package com.example.inventory;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import com.example.inventory.Data.InventoryContract;

import java.io.ByteArrayOutputStream;

public class InventoryCursorAdapter extends CursorAdapter {

    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c,0);
    }
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item,parent,false);
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ImageView productImageView = (ImageView) view.findViewById(R.id.productImage);
        TextView nameTextView = view.findViewById(R.id.name);
        TextView quantityTextView = view.findViewById(R.id.quantity);
        TextView priceTextView = view.findViewById(R.id.price);


            int imageColumnIndex = cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_IMAGE);
            int nameColumnIndex = cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_NAME);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_PRICE);

            String productName = cursor.getString(nameColumnIndex);
            int productQuantity = cursor.getInt(quantityColumnIndex);
            int productPrice = cursor.getInt(priceColumnIndex);

            byte[] img1 = cursor.getBlob(imageColumnIndex);
            Bitmap b1 = BitmapFactory.decodeByteArray(img1, 0, img1.length);


            if (TextUtils.isEmpty(productName)) {
                productName = "No name";
            }


            productImageView.setImageBitmap(b1);
            nameTextView.setText(productName);
            quantityTextView.setText(Integer.toString(productQuantity));
            priceTextView.setText("Rs. " + productPrice);

                }
        }

