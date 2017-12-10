package com.example.android.inventoryapp;

/**
 * Created by doc on 10/12/2017.
 */

/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.ContentValues;
import android.net.Uri;
import android.util.Log;
import android.widget.Button;
import android.widget.CursorAdapter;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.inventoryapp.data.InventoryContract;
import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

/**
 * {@link InventoryCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of pet data as its data source. This adapter knows
 * how to create list items for each row of pet data in the {@link Cursor}.
 */
public class InventoryCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link InventoryCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the pet data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current pet can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);

        // Find the columns of phone attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PHONE_NAME);
        int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PHONE_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PHONE_QUANTITY);

        // Read the phone attributes from the Cursor for the current phone
        String phoneName = cursor.getString(nameColumnIndex);
        String phonePrice = cursor.getString(priceColumnIndex);
        // Declare these variable as final in order to use them inside the inner class
        final String phoneQuantity = cursor.getString(quantityColumnIndex);
        final Cursor daCursor = cursor;
        final Context daContext = context;

        // Update the TextViews with the attributes for the current phone
        nameTextView.setText(phoneName);
        priceTextView.setText(phonePrice);
        quantityTextView.setText(phoneQuantity);

        // Setup Sale Button to open DetailActivity
        Button saleButton = (Button) view.findViewById(R.id.sale_button);
        // set thebutton tag to the position inthe ListView
        int pos = cursor.getPosition();
        saleButton.setTag(pos);
        if (saleButton != null) {
            saleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // get the correct position of the button in the ListView
                    Integer pos = (Integer) view.getTag() + 1;
                    // get the quantity
                    int quant = Integer.parseInt(phoneQuantity);
                    ContentValues values = new ContentValues();
                    values.put(InventoryEntry.COLUMN_PHONE_QUANTITY, (quant - 1));
                    String mCurrentPhoneUri = InventoryEntry.CONTENT_URI + "/" + pos;
                    int rowsAffected = daContext.getContentResolver().update(Uri.parse(mCurrentPhoneUri), values, null, null);

                }
            });
        } else {
            Log.v("Button sale", "Sale button is null");


        }

    }
}