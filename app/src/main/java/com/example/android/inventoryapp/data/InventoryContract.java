package com.example.android.inventoryapp.data;

/**
 * Created by doc on 10/12/2017.
 */

import android.net.Uri;

import android.net.Uri;
import android.content.ContentResolver;
import android.provider.BaseColumns;

/**
 * API Contract for the Inventory app.
 */
public final class InventoryContract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private InventoryContract() {
    }


    public static final String CONTENT_AUTHORITY = "com.example.android.phones";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_PHONES = "phones";

    public static final class InventoryEntry implements BaseColumns {
        /**
         * The content URI
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PHONES);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of phones.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PHONES;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single phone.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PHONES;

        /**
         * Name of database table for phones
         */
        public final static String TABLE_NAME = "phones";

        /**
         * Unique ID number for the phone (only for use in the database table).
         * <p>
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Name of the phone.
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_PHONE_NAME = "name";

        /**
         * Manufacturer of the phone.
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_PHONE_MANUFACTURER = "manufacturer";

        /**
         * Memory of the phone.
         * Type: INTEGER
         */
        public final static String COLUMN_PHONE_MEMORY = "memory";

        /**
         * Price of the phone.
         * <p>
         * Type: Float
         */
        public final static String COLUMN_PHONE_PRICE = "price";

        /**
         * Quantity of the phone.
         * <p>
         * Type: INTEGER
         */
        public final static String COLUMN_PHONE_QUANTITY = "quantity";
    }
}