package com.householdplanner.shoppingapp.stores;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.householdplanner.shoppingapp.data.ShoppingListContract;

import java.util.ArrayList;
import java.util.Locale;

public class ProductHistoryStore {

    // Database creation SQL statement
    private static final String SQL_TABLE_CREATE = "CREATE TABLE "
            + ShoppingListContract.ProductHistoryEntry.TABLE_NAME
            + " (" + ShoppingListContract.ProductHistoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + ShoppingListContract.ProductHistoryEntry.COLUMN_PRODUCT_NAME + " TEXT COLLATE NOCASE, "
            + ShoppingListContract.ProductHistoryEntry.COLUMN_MARKET_ID + " INTEGER, "
            + ShoppingListContract.ProductHistoryEntry.COLUMN_PHOTO_NAME + " TEXT);";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(SQL_TABLE_CREATE);
        insertProducts(database);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            onUpgradeV3(database);
        }
        if (oldVersion < 5) {
            onUpgradeV5(database);
        }
        if (oldVersion < 6) {
            onUpgradeV6(database);
        }
    }

    /**
     * Upgrade Table structure to V3 version
     *
     * @param database
     */
    private static void onUpgradeV3(SQLiteDatabase database) {
        String sql = "ALTER TABLE " + ShoppingListContract.ProductHistoryEntry.TABLE_NAME
                + " RENAME TO " + ShoppingListContract.ProductHistoryEntry.TABLE_NAME + "_old;";
        database.execSQL(sql);
        database.execSQL(SQL_TABLE_CREATE);
        database.execSQL("INSERT INTO " + ShoppingListContract.ProductHistoryEntry.TABLE_NAME
                + " (" + ShoppingListContract.ProductHistoryEntry.COLUMN_PRODUCT_NAME + ",Market,Category "
                + "SELECT " + ShoppingListContract.ProductHistoryEntry.COLUMN_PRODUCT_NAME + ", Brand, Category "
                + "FROM " + ShoppingListContract.ProductHistoryEntry.TABLE_NAME + "_old;");

        database.execSQL("DROP TABLE IF EXISTS " + ShoppingListContract.ProductHistoryEntry.TABLE_NAME + "_old");
        //Insert Products
        insertProducts(database);
    }

    /**
     * Upgrade model to V5 Version
     *
     * @param database
     */
    private static void onUpgradeV5(SQLiteDatabase database) {
        String sql = "ALTER TABLE " + ShoppingListContract.ProductHistoryEntry.TABLE_NAME
                + " RENAME TO " + ShoppingListContract.ProductHistoryEntry.TABLE_NAME + "_old;";
        database.execSQL(sql);
        database.execSQL(SQL_TABLE_CREATE);
        database.execSQL("INSERT INTO " + ShoppingListContract.ProductHistoryEntry.TABLE_NAME
                + " (" + ShoppingListContract.ProductHistoryEntry.COLUMN_PRODUCT_NAME + ",Market, Category) "
                + "SELECT " + ShoppingListContract.ProductHistoryEntry.COLUMN_PRODUCT_NAME + ", Market, Category "
                + "FROM " + ShoppingListContract.ProductHistoryEntry.TABLE_NAME + "_old;");

        database.execSQL("DROP TABLE IF EXISTS " + ShoppingListContract.ProductHistoryEntry.TABLE_NAME + "_old");
    }

    /**
     * Upgrade model to V6 Version:
     * Remove Category column and add MarketID column
     *
     * @param database
     */
    private static void onUpgradeV6(SQLiteDatabase database) {
        String sql = "ALTER TABLE " + ShoppingListContract.ProductHistoryEntry.TABLE_NAME
                + " RENAME TO " + ShoppingListContract.ProductHistoryEntry.TABLE_NAME + "_old;";
        database.execSQL(sql);
        database.execSQL(SQL_TABLE_CREATE);

        Cursor cursor = getVersionLessThan6Rows(database);

        if ((cursor != null) & (cursor.moveToFirst())) {
            while (!cursor.isAfterLast()) {
                sql = "INSERT INTO " + ShoppingListContract.ProductHistoryEntry.TABLE_NAME + " ("
                        + ShoppingListContract.ProductHistoryEntry.COLUMN_PRODUCT_NAME + ","
                        + ShoppingListContract.ProductHistoryEntry.COLUMN_MARKET_ID + ") VALUES ('"
                        + cursor.getString(cursor.getColumnIndex(ShoppingListContract.ProductHistoryEntry.COLUMN_PRODUCT_NAME)) + "',"
                        + cursor.getInt(cursor.getColumnIndex(ShoppingListContract.MarketEntry._ID)) + ")";

                database.execSQL(sql);
                cursor.moveToNext();
            }
        }

        if (cursor != null) cursor.close();
        database.execSQL("DROP TABLE IF EXISTS " + ShoppingListContract.ProductHistoryEntry.TABLE_NAME + "_old");
    }

    /**
     * Get Rows from Catalog doing an INNER JOIN with Markets to get the MarketId
     *
     * @param database
     * @return Cursor
     */
    private static Cursor getVersionLessThan6Rows(SQLiteDatabase database) {
        String sql = "SELECT " + ShoppingListContract.MarketEntry.TABLE_NAME + "." + ShoppingListContract.MarketEntry._ID + ","
                + ShoppingListContract.ProductHistoryEntry.TABLE_NAME + "_old." + ShoppingListContract.ProductHistoryEntry.COLUMN_PRODUCT_NAME
                + " FROM " + ShoppingListContract.ProductHistoryEntry.TABLE_NAME + "_old" + " LEFT JOIN "
                + ShoppingListContract.MarketEntry.TABLE_NAME
                + " ON " + ShoppingListContract.MarketEntry.TABLE_NAME + "." + ShoppingListContract.MarketEntry.COLUMN_MARKET_NAME + "="
                + ShoppingListContract.ProductHistoryEntry.TABLE_NAME + "_old.Market";

        Cursor cursor = database.rawQuery(sql, null);
        return cursor;
    }

    private static boolean existProduct(String productName, SQLiteDatabase db) {
        boolean result = false;
        String sql = "SELECT " + ShoppingListContract.ProductHistoryEntry._ID
                + " FROM " + ShoppingListContract.ProductHistoryEntry.TABLE_NAME
                + " WHERE " + ShoppingListContract.ProductHistoryEntry.COLUMN_PRODUCT_NAME + "='" + productName + "'";

        Cursor cursor = db.rawQuery(sql, null);
        if ((cursor != null) && (cursor.getCount() > 0)) {
            result = true;
        }
        return result;
    }

    private static void insertCleaningProducts(SQLiteDatabase db) {
        String sql;
        //Unusual
        ArrayList<String> productList = new ArrayList<String>();
        if (Locale.getDefault().getLanguage().toLowerCase(Locale.getDefault()).equals("es")) {
            productList.add("Papel Higienico");
            productList.add("Servilletas");
            productList.add("Detergente Lavadora");
            productList.add("Jabón de Mano");
            productList.add("Cepillo de Dientes");
            productList.add("Pasta de Dientes");
            productList.add("Gel de Baño");
            productList.add("Champu");
            productList.add("Rollo de Papel de Cocina");
            productList.add("Limpiador Superficie de Baño");
            productList.add("Limpiador Superficie de Cocina");
            productList.add("Suavizante Lavadora");
            productList.add("Lavavajilla Maquina");
            productList.add("Lavavajilla Manual");
            productList.add("Quitagrasas");
            productList.add("Limpiador de Vitroceramica");
        } else {
            productList.add("Toilet Tissue");
            productList.add("Napkins");
            productList.add("Washing Machine Cleaner");
            productList.add("Washing Up Liquid");
            productList.add("Dishwasher Product");
        }
        for (String productName : productList) {
            if (!existProduct(productName, db)) {
                sql = getProductInsertSQL(productName);
                db.execSQL(sql);
            }
        }
    }

    private static void insertBeveragesProducts(SQLiteDatabase db) {
        String sql;
        //Unusual
        ArrayList<String> productList = new ArrayList<String>();
        if (Locale.getDefault().getLanguage().toLowerCase(Locale.getDefault()).equals("es")) {
            productList.add("Vino Blanco");
            productList.add("Agua");
            productList.add("Cerveza");
            productList.add("Refresco");
            productList.add("Patatas Fritas");
        } else {
            productList.add("Wine");
            productList.add("Cola");
            productList.add("Still Water");
            productList.add("Beer");
        }
        for (String productName : productList) {
            if (!existProduct(productName, db)) {
                sql = getProductInsertSQL(productName);
                db.execSQL(sql);
            }
        }

    }

    private static void insertMeatProducts(SQLiteDatabase db) {
        String sql;
        //Unusual
        ArrayList<String> productList = new ArrayList<String>();
        if (Locale.getDefault().getLanguage().toLowerCase(Locale.getDefault()).equals("es")) {
            productList.add("Hamburguesa de Pollo");
            productList.add("Hamburguesa de Vacuno");
            productList.add("Chorizo");
            productList.add("Morcillo");
            productList.add("Jamon Serrano");
            productList.add("Jamon Cocido");
            productList.add("Salchichon");
            productList.add("Pollo");
        } else {
            productList.add("Beef Brisket");
            productList.add("Smoked bacon");
            productList.add("Lamb");
            productList.add("Pork");
            productList.add("Sausages");
            productList.add("Chicken");
        }
        for (String productName : productList) {
            if (!existProduct(productName, db)) {
                sql = getProductInsertSQL(productName);
                db.execSQL(sql);
            }
        }
    }

    private static void insertMilkProducts(SQLiteDatabase db) {
        String sql;
        //Unusual
        ArrayList<String> productList = new ArrayList<String>();
        if (Locale.getDefault().getLanguage().toLowerCase(Locale.getDefault()).equals("es")) {
            productList.add("Queso");
            productList.add("Queso Fresco");
        } else {
            productList.add("Cheese");
        }
        for (String productName : productList) {
            if (!existProduct(productName, db)) {
                sql = getProductInsertSQL(productName);
                db.execSQL(sql);
            }
        }
        //Usual
        productList = new ArrayList<String>();
        if (Locale.getDefault().getLanguage().toLowerCase(Locale.getDefault()).equals("es")) {
            productList.add("Leche Entera");
            productList.add("Yogur Natural");
            productList.add("Yogur Natural Azucarado");
            productList.add("Yogur de Macedonia");
            productList.add("Huevos");
        } else {
            productList.add("Yogurt");
            productList.add("Eggs");
            productList.add("Milk");
        }
        for (String productName : productList) {
            if (!existProduct(productName, db)) {
                sql = getProductInsertSQL(productName);
                db.execSQL(sql);
            }
        }
    }

    private static void insertFishProducts(SQLiteDatabase db) {
        String sql;
        //Unusual
        ArrayList<String> productList = new ArrayList<String>();
        if (Locale.getDefault().getLanguage().toLowerCase(Locale.getDefault()).equals("es")) {
            productList.add("Salmon");
            productList.add("Lubina");
            productList.add("Chirlas");
            productList.add("Anillas de Calamar");
            productList.add("Dorada");
            productList.add("Atun");
            productList.add("Lenguado");
        } else {
            productList.add("Cod");
            productList.add("Seafood Sticks");
            productList.add("Mackerel");
        }
        for (String productName : productList) {
            if (!existProduct(productName, db)) {
                sql = getProductInsertSQL(productName);
                db.execSQL(sql);
            }
        }
        //Usual
        productList = new ArrayList<String>();
        if (Locale.getDefault().getLanguage().toLowerCase(Locale.getDefault()).equals("es")) {
            productList.add("Merluza");
        } else {
            productList.add("Smoked Salmon");
        }
        for (String productName : productList) {
            if (!existProduct(productName, db)) {
                sql = getProductInsertSQL(productName);
                db.execSQL(sql);
            }
        }
    }

    private static void insertFruitProducts(SQLiteDatabase db) {
        String sql;
        //Unusual
        ArrayList<String> productList = new ArrayList<String>();
        if (Locale.getDefault().getLanguage().toLowerCase(Locale.getDefault()).equals("es")) {
            productList.add("Zanahoria");
            productList.add("Pimiento Rojo");
            productList.add("Puerro");
            productList.add("Patata");
            productList.add("Ajo");
            productList.add("Perejil");
        } else {
            productList.add("Cabbage");
            productList.add("Broccoli");
            productList.add("Cauliflower");
            productList.add("Asparagus");
        }
        for (String productName : productList) {
            if (!existProduct(productName, db)) {
                sql = getProductInsertSQL(productName);
                db.execSQL(sql);
            }
        }
        //Usual
        productList = new ArrayList<String>();
        if (Locale.getDefault().getLanguage().toLowerCase(Locale.getDefault()).equals("es")) {
            productList.add("Calabacin");
            productList.add("Pimiento Verde");
            productList.add("Pepino");
            productList.add("Naranja");
            productList.add("Mandarina");
            productList.add("Manzana");
            productList.add("Platanos");
            productList.add("Cebolla");
            productList.add("Cebolleta");
            productList.add("Calabaza");
            productList.add("Tomates");
            productList.add("Lechuga");
        } else {
            productList.add("Carrot");
            productList.add("Leaf Greens");
            productList.add("Cooking Onions");
            productList.add("Potatoes");
            productList.add("Corn");
            productList.add("Beans");
        }
        for (String productName : productList) {
            if (!existProduct(productName, db)) {
                sql = getProductInsertSQL(productName);
                db.execSQL(sql);
            }
        }
    }

    private static void insertBakeryProducts(SQLiteDatabase db) {
        String sql;
        //Unusual
        ArrayList<String> productList = new ArrayList<String>();
        if (Locale.getDefault().getLanguage().toLowerCase(Locale.getDefault()).equals("es")) {
            productList.add("Harina");
            productList.add("Fideos");
            productList.add("Obleas de Empanadilla");
        } else {
            productList.add("Bagels");
            productList.add("Croissants");
        }
        for (String productName : productList) {
            if (!existProduct(productName, db)) {
                sql = getProductInsertSQL(productName);
                db.execSQL(sql);
            }
        }
        //Usual
        productList = new ArrayList<String>();
        if (Locale.getDefault().getLanguage().toLowerCase(Locale.getDefault()).equals("es")) {
            productList.add("Pasta en Espirales");
            productList.add("Macarrones");
            productList.add("Espaguetis");
            productList.add("Pan de Molde");
        } else {
            productList.add("Bread");
            productList.add("Butter");
        }
        for (String productName : productList) {
            if (!existProduct(productName, db)) {
                sql = getProductInsertSQL(productName);
                db.execSQL(sql);
            }
        }
    }

    private static void insertVegetablesProducts(SQLiteDatabase db) {
        String sql;
        //Unusual
        ArrayList<String> productList = new ArrayList<String>();
        if (Locale.getDefault().getLanguage().toLowerCase(Locale.getDefault()).equals("es")) {
            productList.add("Garbanzos");
            productList.add("Maiz");
            productList.add("Atun en Aceite");
        } else {
            productList.add("Spicy Sauce");
            productList.add("Pepper Sauce");
            productList.add("Curry Paste");
            productList.add("Breadcrumb");
            productList.add("Salt");
        }
        for (String productName : productList) {
            if (!existProduct(productName, db)) {
                sql = getProductInsertSQL(productName);
                db.execSQL(sql);
            }
        }
        //Usual
        productList = new ArrayList<String>();
        if (Locale.getDefault().getLanguage().toLowerCase(Locale.getDefault()).equals("es")) {
            productList.add("Judias Blancas");
            productList.add("Lentejas");
            productList.add("Arroz");
            productList.add("Aceitunas Negras");
        } else {
            productList.add("Cereals");
            productList.add("Muesli");
            productList.add("Baked Beans");
        }
        for (String productName : productList) {
            if (!existProduct(productName, db)) {
                sql = getProductInsertSQL(productName);
                db.execSQL(sql);
            }
        }
    }

    private static void insertOilProducts(SQLiteDatabase db) {
        String sql;
        //Unusual
        ArrayList<String> productList = new ArrayList<String>();
        if (Locale.getDefault().getLanguage().toLowerCase(Locale.getDefault()).equals("es")) {
            productList.add("Sal");
            productList.add("Azucar");
            productList.add("Aceite de Oliva");
            productList.add("Aceite de Girasol");
            productList.add("Pimenton");
            productList.add("Colorante");
            productList.add("Vinagre");
            productList.add("Miel");
            productList.add("Mayonesa");
        } else {
            productList.add("Soap");
            productList.add("Toothbrush");
            productList.add("Toothpaste");
            productList.add("Shower Gel");
            productList.add("Shampoo");
        }
        for (String productName : productList) {
            if (!existProduct(productName, db)) {
                sql = getProductInsertSQL(productName);
                db.execSQL(sql);
            }
        }
        //Usual
        if (Locale.getDefault().getLanguage().toLowerCase(Locale.getDefault()).equals("es")) {
            productList = new ArrayList<String>();
            productList.add("Tomate Frito");
            for (String productName : productList) {
                if (!existProduct(productName, db)) {
                    sql = getProductInsertSQL(productName);
                    db.execSQL(sql);
                }
            }
        }
    }

    private static void insertProducts(SQLiteDatabase db) {
        //Unusual
        insertCleaningProducts(db);
        insertBeveragesProducts(db);
        insertMeatProducts(db);
        insertMilkProducts(db);
        insertFishProducts(db);
        insertFruitProducts(db);
        insertBakeryProducts(db);
        insertVegetablesProducts(db);
        insertOilProducts(db);
    }

    /**
     * Build sql to Insert a product in the table
     *
     * @param productName product name
     * @return SQL
     */
    private static String getProductInsertSQL(String productName) {
        String sql = "INSERT INTO " + ShoppingListContract.ProductHistoryEntry.TABLE_NAME
                + "(" + ShoppingListContract.ProductHistoryEntry.COLUMN_PRODUCT_NAME + ") VALUES "
                + "('" + productName + "')";
        return sql;
    }

}
