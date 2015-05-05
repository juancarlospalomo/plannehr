package com.householdplanner.shoppingapp.stores;

import java.util.ArrayList;
import java.util.Locale;

import com.householdplanner.shoppingapp.cross.util;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class ProductHistoryStore {

	//ProductHistory table
	public static final String TABLE_PRODUCT_HISTORY = "ProductHistory";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_PRODUCT_NAME = "Name";
	public static final String COLUMN_MARKET = "Market";
	public static final String COLUMN_CATEGORY = "Category";
	public static final String COLUMN_SEQUENCE = "Sequence";
	public static final String COLUMN_LAST_DATE = "LastDate";
	public static final String COLUMN_SUGGESTION = "Suggest";

	// Database creation SQL statement
	private static final String SQL_TABLE_CREATE = "create table " 
			+ TABLE_PRODUCT_HISTORY
			+ "(" 
			+ COLUMN_ID + " integer primary key autoincrement, " 
			+ COLUMN_PRODUCT_NAME + " text COLLATE NOCASE, "
			+ COLUMN_MARKET + " text, "
			+ COLUMN_CATEGORY + " integer, "
			+ COLUMN_SEQUENCE + " integer, "
			+ COLUMN_LAST_DATE + " datetime, "
			+ COLUMN_SUGGESTION + " integer);";
	
	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(SQL_TABLE_CREATE);
		insertProducts(database);
	}
	
	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		if (oldVersion<3) {
			onUpgradeV3(database);
		}
	}
	
	public static void createTemp(SQLiteDatabase db) {
		final String SQL_TEMP_TABLE_CREATE = "create temp table History_Temp"
				+ "(" + COLUMN_ID + " integer primary key autoincrement, "
				+ COLUMN_PRODUCT_NAME + " text COLLATE NOCASE, "
				+ COLUMN_MARKET + " text, "
				+ COLUMN_CATEGORY + " integer, "
				+ COLUMN_SEQUENCE + " integer, "
				+ COLUMN_LAST_DATE + " datetime, "
				+ COLUMN_SUGGESTION + " integer);";
		db.execSQL(SQL_TEMP_TABLE_CREATE);
	}
	
	private static void onUpgradeV3(SQLiteDatabase database) {
		String sql = "ALTER TABLE " + TABLE_PRODUCT_HISTORY + " RENAME TO " + TABLE_PRODUCT_HISTORY + "_old;";
		database.execSQL(sql);
		database.execSQL(SQL_TABLE_CREATE);
		database.execSQL("INSERT INTO " + TABLE_PRODUCT_HISTORY + "(Name, Market, Category, Sequence, " 
				+ "LastDate, Suggest) " 
				+ "SELECT Name, Brand, Category, Sequence, " 
				+ "LastDate, Suggest FROM " + TABLE_PRODUCT_HISTORY + "_old;");
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCT_HISTORY + "_old");
		
		//Insert Products
		insertProducts(database);
	}
	
	private static boolean existProduct(String productName, SQLiteDatabase db) {
		boolean result = false;
		String sql = "SELECT " + ProductHistoryStore.COLUMN_ID + " FROM " + ProductHistoryStore.TABLE_PRODUCT_HISTORY
			  + " WHERE " + ProductHistoryStore.COLUMN_PRODUCT_NAME + "='" + productName + "'";

		Cursor cursor = db.rawQuery(sql, null);
		if ((cursor!=null) && (cursor.getCount()>0)) {
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
			productList.add("Jab�n de Mano");
			productList.add("Cepillo de Dientes");
			productList.add("Pasta de Dientes");
			productList.add("Gel de Ba�o");
			productList.add("Champu");
			productList.add("Rollo de Papel de Cocina");
			productList.add("Limpiador Superficie de Ba�o");
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
				sql = getProductInsertSQL(productName, 1, 0);
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
				sql = getProductInsertSQL(productName, 2, 0);
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
				sql = getProductInsertSQL(productName, 3, 0);
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
				sql = getProductInsertSQL(productName, 4, 0);
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
				sql = getProductInsertSQL(productName, 4, 1);
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
				sql = getProductInsertSQL(productName, 5, 0);
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
				sql = getProductInsertSQL(productName, 5, 1);
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
				sql = getProductInsertSQL(productName, 6, 0);
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
				sql = getProductInsertSQL(productName, 6, 1);
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
				sql = getProductInsertSQL(productName, 7, 0);
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
				sql = getProductInsertSQL(productName, 7, 1);
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
				sql = getProductInsertSQL(productName, 8, 0);
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
				sql = getProductInsertSQL(productName, 8, 1);
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
				sql = getProductInsertSQL(productName, 9, 0);
				db.execSQL(sql);
			}
		}
		//Usual
		if (Locale.getDefault().getLanguage().toLowerCase(Locale.getDefault()).equals("es")) {
			productList = new ArrayList<String>();
			productList.add("Tomate Frito");
			for (String productName : productList) {
				if (!existProduct(productName, db)) {
					sql = getProductInsertSQL(productName, 9, 1);
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
	
	private static String getProductInsertSQL(String productName, int categoryId, int suggest) {
		String sql = "INSERT INTO " + TABLE_PRODUCT_HISTORY + "(Name, Category, Sequence, " 
				+ "LastDate, Suggest) VALUES " 
				+ "('" +  productName + "'," + categoryId + ",0,'" + util.getDateTime() + "',"
				+ suggest + ")";
		return sql;
	}
	
}
