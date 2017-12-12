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
package com.example.android.inventoryapp;

/**
 * Created by doc on 10/12/2017.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Allows user to create a new pet or edit an existing one.
 */
public class DetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the pet data loader
     */
    private static final int EXISTING_PHONE_LOADER = 0;

    private Uri mCurrentPhoneUri;

    /**
     * EditText field to enter the phone's name
     */
    private EditText mNameEditText;

    /**
     * EditText field to enter the phone's manifacturer
     */
    private EditText mManufacturerEditText;

    /**
     * EditText field to enter the phone's price
     */
    private EditText mPriceEditText;

    /**
     * EditText field to enter the phone's memory size
     */
    private EditText mMemoryEditText;

    /**
     * EditText field to enter the phone's quantity
     */
    private EditText mQuantityEditText;

    /**
     * ImageView to enter the phone's image
     */
    private ImageView mImageView;

    /**
     * Boolean flag that keeps track of whether the phone has been edited (true) or not (false)
     */
    private boolean mPhoneHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mPhoneHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mPhoneHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new phone or editing an existing one.
        Intent intent = getIntent();
        mCurrentPhoneUri = intent.getData();

        // If the intent DOES NOT contain a phone content URI, then we know that we are
        // creating a new phone.
        if (mCurrentPhoneUri == null) {
            // This is a new phone, so change the app bar to say "Add a Phone"
            setTitle(getString(R.string.detail_activity_title_new_phone));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a pet that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing phone, so change app bar to say "Display/Edit Phone"
            setTitle(getString(R.string.detail_activity_title_edit_phone));

            // Initialize a loader to read the phone data from the database
            // and display the current values in the detail
            getLoaderManager().initLoader(EXISTING_PHONE_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_phone_name);
        mManufacturerEditText = (EditText) findViewById(R.id.edit_phone_manufacturer);
        mPriceEditText = (EditText) findViewById(R.id.edit_phone_price);
        mMemoryEditText = (EditText) findViewById(R.id.edit_phone_memory);
        mQuantityEditText = (EditText) findViewById(R.id.quantity_value);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the detail without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mManufacturerEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mMemoryEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);

        // Populate the quantity choice spinner
        final Spinner spinner = (Spinner) findViewById(R.id.spinner_quantity);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.predefined_quantity, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);


        //final String selected_quantity_offset = spinner.getSelectedItem().toString();

        Button addQuantity = (Button) findViewById(R.id.add_quantity);
        addQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String selected_quantity_offset = spinner.getSelectedItem().toString();
                //get the current quantity
                if (TextUtils.isEmpty(mQuantityEditText.getText())) {
                    mQuantityEditText.setText("0");
                }
                String quantityString = mQuantityEditText.getText().toString().trim();
                int newQuantity = Integer.parseInt(quantityString) + Integer.parseInt(selected_quantity_offset);
                mQuantityEditText.setText(newQuantity + "");

            }
        });

        Button removeQuantity = (Button) findViewById(R.id.remove_quantity);
        removeQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get the current quantity
                String selected_quantity_offset = spinner.getSelectedItem().toString();
                String quantityString = mQuantityEditText.getText().toString().trim();
                if (!TextUtils.isEmpty(mQuantityEditText.getText())) {

                    int newQuantity = (Integer.parseInt(quantityString) - Integer.parseInt(selected_quantity_offset));
                    if (newQuantity >= 0) {
                        mQuantityEditText.setText(newQuantity + "");
                    } else {
                        mQuantityEditText.setText(0 + "");
                    }
                }
            }
        });

        //** Add an image from the phone gallery **/

        Button addImage = (Button) findViewById(R.id.add_image);
        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickImage();
            }
        });




}

    // ** Picture management ** //
    Bitmap bp;
    public static final int PICK_IMAGE = 1;
    private void pickImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE) {
            // get a bitmap version of the image
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                try {
                    bp = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                    // set this image to the ImageView
                    mImageView = (ImageView) findViewById(R.id.phone_image);
                    mImageView.setImageBitmap(bp);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    /**
     * Get user input from detail and save phone into database.
     */
    private int savePhone() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String manufacturerString = mManufacturerEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String memoryString = mMemoryEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        // Convert the bitmap image into byte[]
        // convert from bitmap to byte array
        byte[] image = null;
        if (bp != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bp.compress(Bitmap.CompressFormat.PNG, 0, stream);
            image = stream.toByteArray();
        }


        // Check if th is is supposed to be a new pet
        // and check if all the fields in the detail are blank
        if (mCurrentPhoneUri == null &&
                (bp == null) && TextUtils.isEmpty(nameString) && TextUtils.isEmpty(manufacturerString) &&
                TextUtils.isEmpty(priceString) && TextUtils.isEmpty(memoryString) && TextUtils.isEmpty(quantityString)) {
            // Since no fields were modified, we can return early without creating a new phone.
            return 1;
        }

        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_PHONE_NAME, nameString);
        values.put(InventoryEntry.COLUMN_PHONE_MANUFACTURER, manufacturerString);
        values.put(InventoryEntry.COLUMN_PHONE_PRICE, priceString);
        values.put(InventoryEntry.COLUMN_PHONE_IMAGE, image);

        // Set the default value of quantity and memory if the user
        // doesn't provide a value
        int quantity = 0;
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }
        values.put(InventoryEntry.COLUMN_PHONE_QUANTITY, quantity);

        // the default value for memory is 8GB
        int memory = 8;
        if (!TextUtils.isEmpty(memoryString)) {
            memory = Integer.parseInt(memoryString);
        }

        values.put(InventoryEntry.COLUMN_PHONE_MEMORY, memory);


        // Determine if this is a new or existing phone by checking if mCurrentPhoneUri is null or not
        if (mCurrentPhoneUri == null) {
            // This is a NEW phone, so insert a new phone into the provider,
            // returning the content URI for the new phone.
            Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                return 1;
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.detail_insert_phone_successful),
                        Toast.LENGTH_SHORT).show();
                return 0;
            }
        } else {
            // Otherwise this is an EXISTING phone, so update the phone with content URI: mCurrentPhoneUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentPhoneUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentPhoneUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.detail_update_phone_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.detail_update_phone_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        return 0;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_detail.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new phone, hide the "Delete" menu item.
        if (mCurrentPhoneUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save phone to database
                int result = savePhone();
                // Exit activity
                if (result == 0) {
                    finish();
                }
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mPhoneHasChanged) {
                    NavUtils.navigateUpFromSameTask(DetailActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(DetailActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mPhoneHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
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

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the detail shows all phone attributes, define a projection that contains
        // all columns from the pet table
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_PHONE_NAME,
                InventoryEntry.COLUMN_PHONE_MANUFACTURER,
                InventoryEntry.COLUMN_PHONE_PRICE,
                InventoryEntry.COLUMN_PHONE_IMAGE,
                InventoryEntry.COLUMN_PHONE_MEMORY,
                InventoryEntry.COLUMN_PHONE_QUANTITY
        };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentPhoneUri,         // Query the content URI for the current phone
                projection,               // Columns to include in the resulting Cursor
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of pet attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PHONE_NAME);
            int manufacturerColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PHONE_MANUFACTURER);
            int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PHONE_PRICE);
            int imageColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PHONE_IMAGE);
            int memoryColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PHONE_MEMORY);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PHONE_QUANTITY);


            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String manufacturer = cursor.getString(manufacturerColumnIndex);
            float price = cursor.getFloat(priceColumnIndex);
            byte[] image = cursor.getBlob(imageColumnIndex);
            int memory = cursor.getInt(memoryColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);


            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mManufacturerEditText.setText(manufacturer);
            mPriceEditText.setText(Float.toString(price));
            mMemoryEditText.setText(Integer.toString(memory));
            mQuantityEditText.setText(Integer.toString(quantity));
            // ** transform the image into bitmap
            // convert from byte array to bitmap
            Bitmap image_bp = BitmapFactory.decodeByteArray(image, 0, image.length);
            if (image_bp != null) {
                ImageView imageView = (ImageView) findViewById(R.id.phone_image);
                imageView.setImageBitmap(image_bp);
            }else{
                Log.v("**********", image + "");
            }


        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mManufacturerEditText.setText("");
        mPriceEditText.setText("");
        mMemoryEditText.setText("");
        mQuantityEditText.setText("");

    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the detail.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the phone.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Prompt the user to confirm that they want to delete this phone.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deletePhone();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the pet in the database.
     */
    private void deletePhone() {
        // Only perform the delete if this is an existing phone.
        if (mCurrentPhoneUri != null) {
            // Call the ContentResolver to delete the phone at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentPetUri
            // content URI already identifies the phone that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentPhoneUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.detail_delete_phone_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.detail_delete_phone_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }
}