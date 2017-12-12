package com.example.android.inventoryapp.data;

/**
 * Created by doc on 10/12/2017.
 */

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.example.android.inventoryapp.R;
import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

import java.security.Provider;
import java.sql.Blob;

/**
 * {@link ContentProvider} for Inventory app.
 */
public class InventoryProvider extends ContentProvider {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = InventoryProvider.class.getSimpleName();

    /**
     * URI matcher code for the content URI for the phones table
     */
    private static final int PHONES = 100;

    /**
     * URI matcher code for the content URI for a single phone in the phones table
     */
    private static final int PHONE_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {

        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_PHONES, PHONES);

        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_PHONES + "/#", PHONE_ID);
    }

    /**
     * Database helper object
     */
    private InventoryDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PHONES:
                cursor = database.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case PHONE_ID:

                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                cursor = database.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // Set notification URI on the Cursor,
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        // Return the cursor
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PHONES:
                return insertPhone(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a phone into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertPhone(Uri uri, ContentValues values) {
        /**
         * Get the context for the Toast
         */
        Context context = getContext();
        // Check that the name is not null
        String name = values.getAsString(InventoryEntry.COLUMN_PHONE_NAME);

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(getContext(), context.getString(R.string.error_name), Toast.LENGTH_SHORT).show();
            return null;
        }

        // Check that the manufacturer is not null
        String manufacturer = values.getAsString(InventoryEntry.COLUMN_PHONE_MANUFACTURER);
        if (TextUtils.isEmpty(manufacturer)) {
            Toast.makeText(getContext(), context.getString(R.string.error_manufacturer), Toast.LENGTH_SHORT).show();
            return null;
        }

        // If the price is not provided or if it's negative throw an exception
        Float price = values.getAsFloat(InventoryEntry.COLUMN_PHONE_PRICE);
        if (price == null || price < 0) {
            Toast.makeText(getContext(), context.getString(R.string.error_price), Toast.LENGTH_SHORT).show();
            return null;
        }

        // If the memory is not provided it defaults to 8. But if it's provided
        // cannot be negative
        Integer memory = values.getAsInteger(InventoryEntry.COLUMN_PHONE_MEMORY);
        if (memory == null || memory < 0) {
            Toast.makeText(getContext(), context.getString(R.string.error_memory), Toast.LENGTH_SHORT).show();
            return null;
        }

        // If the quantity is not provided it defaults to zero. But if it's provided
        // cannot be negative
        Integer quantity = values.getAsInteger(InventoryEntry.COLUMN_PHONE_QUANTITY);
        if (quantity == null || quantity < 0 ) {
            Toast.makeText(getContext(), context.getString(R.string.error_quantity), Toast.LENGTH_SHORT).show();
            return null;
        }

        // If the image is not provided throw an exception
        byte[] image = values.getAsByteArray(InventoryEntry.COLUMN_PHONE_IMAGE);
        if (image == null ) {
            Toast.makeText(getContext(), context.getString(R.string.error_image), Toast.LENGTH_SHORT).show();
            return null;
        }

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new phone with the given values
        long id = database.insert(InventoryEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the phone content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PHONES:
                return updatePhone(uri, contentValues, selection, selectionArgs);
            case PHONE_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updatePhone(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updatePhone(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        /**
         * Get the context for the Toast
         */
        Context context = getContext();
        // Check that the name is not null
        String name = values.getAsString(InventoryEntry.COLUMN_PHONE_NAME);

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(getContext(), context.getString(R.string.error_name), Toast.LENGTH_SHORT).show();
            return 0;
        }

        // Check that the manufacturer is not null
        String manufacturer = values.getAsString(InventoryEntry.COLUMN_PHONE_MANUFACTURER);
        if (TextUtils.isEmpty(manufacturer)) {
            Toast.makeText(getContext(), context.getString(R.string.error_manufacturer), Toast.LENGTH_SHORT).show();
            return 0;
        }

        // If the price is not provided or if it's negative throw an exception
        Float price = values.getAsFloat(InventoryEntry.COLUMN_PHONE_PRICE);
        if (price == null || price < 0) {
            Toast.makeText(getContext(), context.getString(R.string.error_price), Toast.LENGTH_SHORT).show();
            return 0;
        }

        // If the memory is not provided it defaults to 8. But if it's provided
        // cannot be negative
        Integer memory = values.getAsInteger(InventoryEntry.COLUMN_PHONE_MEMORY);
        if (memory == null || memory < 0) {
            Toast.makeText(getContext(), context.getString(R.string.error_memory), Toast.LENGTH_SHORT).show();
            return 0;
        }

        // If the quantity is not provided it defaults to zero. But if it's provided
        // cannot be negative
        Integer quantity = values.getAsInteger(InventoryEntry.COLUMN_PHONE_QUANTITY);
        if (quantity == null || quantity < 0 ) {
            Toast.makeText(getContext(), context.getString(R.string.error_quantity), Toast.LENGTH_SHORT).show();
            return 0;
        }

        // If the image is not provided throw an exception
        byte[] image = values.getAsByteArray(InventoryEntry.COLUMN_PHONE_IMAGE);
        if (image == null ) {
            Toast.makeText(getContext(), context.getString(R.string.error_image), Toast.LENGTH_SHORT).show();
            return 0;
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(InventoryEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PHONES:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PHONE_ID:
                // Delete a single row given by the ID in the URI
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PHONES:
                return InventoryEntry.CONTENT_LIST_TYPE;
            case PHONE_ID:
                return InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}