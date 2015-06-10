package com.householdplanner.shoppingapp.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by JuanCarlos on 21/05/2015.
 */
public class ShoppingListContract {
    //Content authority is a name for the entire content provider. A good choice
    //to use as this string is the package name of the app, which is unique on
    //the device
    public static final String CONTENT_AUTHORITY = "com.householdplanner.shoppingapp";
    //Base for all URI´s in the app
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    //Paths to be added to the base content URI for setting the URI´s
    //For instance, content://com.productivity.letmeknow/task will be a valid path
    public static final String PATH_PRODUCT = "product";
    public static final String PATH_PRODUCT_HISTORY = "product_history";
    public static final String PATH_MARKET = "market";
    public static final String PATH_BUDGET = "budget";

    /**
     * inner class that defines the table contents of the List table
     */
    public static final class ProductEntry implements BaseColumns {
        //content URI for Product Entities
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_PRODUCT).build();
        //MIME format for cursor
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" +
                "vnd." + CONTENT_AUTHORITY + "." + PATH_PRODUCT;
        //MIME format for a cursor item
        public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/" +
                "vnd." + CONTENT_AUTHORITY + "." + PATH_PRODUCT;
        //Table name
        public static final String TABLE_NAME = "List";
        /**
         * Columns
         */
        //Product name
        public static final String COLUMN_PRODUCT_NAME = "Name";
        //Product amount
        public static final String COLUMN_PRODUCT_AMOUNT = "Amount";
        //Product measure unit id
        public static final String COLUMN_UNIT_ID = "UnitId";
        //Product category id
        public static final String COLUMN_CATEGORY_ID = "Category";
        //Market name that product belongs to
        public static final String COLUMN_MARKET = "Market";
        //Is Product in the basket?
        public static final String COLUMN_COMMITTED = "Comitted";
    }

    /**
     * inner class that defines the table contents of the ProductHistory table
     */
    public static final class ProductHistoryEntry implements BaseColumns {
        //content URI for Product History Entities
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_PRODUCT_HISTORY).build();
        //MIME format for cursor
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" +
                "vnd." + CONTENT_AUTHORITY + "." + PATH_PRODUCT_HISTORY;
        //MIME format for a cursor item
        public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/" +
                "vnd." + CONTENT_AUTHORITY + "." + PATH_PRODUCT_HISTORY;
        //Table name
        public static final String TABLE_NAME = "ProductHistory";
        /**
         * Columns
         */
        //Product name
        public static final String COLUMN_PRODUCT_NAME = "Name";
        //Product category id
        public static final String COLUMN_CATEGORY_ID = "Category";
        //Market name that product belongs to
        public static final String COLUMN_MARKET = "Market";

        /**
         *Alias for fields if they are needed
         */
        public static final String ALIAS_ID = TABLE_NAME + "_" + _ID;

        //Uri functions to manage the parameters
        /**
         * extract suggestion name string from uri
         * @param uri to match
         * @return partial name pattern string
         */
        public static String getUriSuggestionName(Uri uri) {
            return uri.getLastPathSegment();
        }
    }

    /**
     * inner class that defines the table contents of the Market table
     */
    public static final class MarketEntry implements BaseColumns {
        //content URI for Market Entities
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MARKET).build();
        //MIME format for cursor
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" +
                "vnd." + CONTENT_AUTHORITY + "." + PATH_MARKET;
        //MIME format for a cursor item
        public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/" +
                "vnd." + CONTENT_AUTHORITY + "." + PATH_MARKET;

        //Table name
        public static final String TABLE_NAME = "Market";
        /**
         * Table columns
         */
        //Generated Market Id
        public static final String COLUMN_MARKET_ID = "MarketId";
        //Market name
        public static final String COLUMN_MARKET_NAME = "Name";
        //Color for the market
        public static final String COLUMN_COLOR = "Color";
    }

    /**
     * inner class that defines the table contents Budget table
     */
    public static final class BudgetEntry implements BaseColumns {
        //content URI for Budget Entities
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_BUDGET).build();
        //MIME format for cursor
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" +
                "vnd." + CONTENT_AUTHORITY + "." + PATH_BUDGET;
        //MIME format for a cursor item
        public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/" +
                "vnd." + CONTENT_AUTHORITY + "." + PATH_BUDGET;
        //Table name
        public static final String TABLE_NAME = "Budget";
        /**
         * Table columns
         */
        //Month number
        public static final String COLUMN_MONTH = "Month";
        //Quantity available for the month
        public static final String COLUMN_AVAILABLE = "Available";
        //Target expense
        public static final String COLUMN_TARGET = "Target";
        //WithDrawn money
        public static final String COLUMN_WITHDRAWN = "WithDrawn";
        public static final String COLUMN_LAST_WITHDRAWN = "LWithDrawn";
        //Wallet money
        public static final String COLUMN_WALLET = "Wallet";
        public static final String COLUMN_LAST_WALLET = "LWallet";
    }
}
