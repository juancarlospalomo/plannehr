package com.householdplanner.shoppingapp;

import com.householdplanner.shoppingapp.repositories.ProductHistoryRepository;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

public class ProductProvider extends ContentProvider {

	 // fields for my content provider
	static final String PROVIDER_NAME = "com.householdplanner.shoppingapp.ProductProvider";
	static final String URL = "content://" + PROVIDER_NAME + "/Products";
	static final Uri CONTENT_URI = Uri.parse(URL);
	
	// fields for the database
	static final String COLUMN_ID = "_id";
	static final String COLUMN_PRODUCT_NAME = "Name";
	
    // integer values used in content URI
	static final int PRODUCTS = 1;
	static final int PRODUCT_ID = 2;
	static final int PRODUCT_NAME = 3;
	static final int SUGGESTION = 4;
	
	static final UriMatcher uriMatcher;
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(PROVIDER_NAME, "Products", PRODUCTS);
		uriMatcher.addURI(PROVIDER_NAME, "Products/#", PRODUCT_ID);
		uriMatcher.addURI(PROVIDER_NAME, "Products/Name/*", PRODUCT_NAME);
		uriMatcher.addURI(PROVIDER_NAME, "Products/" + SearchManager.SUGGEST_URI_PATH_QUERY, SUGGESTION);
		uriMatcher.addURI(PROVIDER_NAME, "Products/" + SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SUGGESTION);
	}
	
	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		return 0;
	}

	@Override
	public String getType(Uri arg0) {
		return null;
	}

	@Override
	public Uri insert(Uri arg0, ContentValues arg1) {
		return null;
	}

	@Override
	public boolean onCreate() {
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
		ProductHistoryRepository productHistoryRepository = new ProductHistoryRepository(getContext());
		int code = uriMatcher.match(uri);
		switch(code) {
			case PRODUCTS:
				return productHistoryRepository.getAllProductItem();
			case SUGGESTION:
				selection = uri.getLastPathSegment();
				return productHistoryRepository.getProductSuggestions(selection);
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
		return 0;
	}

	
	
}
