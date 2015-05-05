package com.householdplanner.shoppingapp.fragments;

import java.io.File;
import java.util.ArrayList;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.support.v4.app.LoaderManager;
import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.householdplanner.shoppingapp.MarketActivity;
import com.householdplanner.shoppingapp.MarketListActivity;
import com.householdplanner.shoppingapp.ProductActivity;
import com.householdplanner.shoppingapp.R;
import com.householdplanner.shoppingapp.help.HelpActivityFrame;
import com.householdplanner.shoppingapp.cross.AppPreferences;
import com.householdplanner.shoppingapp.cross.OnFragmentProgress;
import com.householdplanner.shoppingapp.cross.OnLoadData;
import com.householdplanner.shoppingapp.cross.font;
import com.householdplanner.shoppingapp.cross.util;
import com.householdplanner.shoppingapp.repositories.ShoppingListRepository;
import com.householdplanner.shoppingapp.stores.ShoppingListStore;
import com.householdplanner.shoppingapp.views.HelpView;
import com.householdplanner.shoppingapp.views.HelpView.OnHelpViewClick;
import com.householdplanner.shoppingapp.views.HelpView.TypeView;
import com.householdplanner.shoppingapp.repositories.MarketRepository;

public class FragmentEnterList extends SherlockFragment implements LoaderManager.LoaderCallbacks<Cursor>,
							OnItemClickListener, OnFragmentProgress {

	public static final String TAG_FRAGMENT = "fragmentEnterList";
	
	private static final int LOADER_ID = 1;
	private static final int SELECT_MARKET_FOR_MOVE = 2;
	private static final int EDIT_PRODUCT = 3;
	private static final String KEY_ITEMS_SELECTED = "items"; 
	private static final String KEY_SELECT_ITEM = "selectItem";
	
	private static String mSelectItemName = null;
	private EnterListAdapter mAdapter;	
	private ListView mListView;
	private ActionMode mActionMode;
	private ArrayList<Integer> mItemsSelected = null;
	private OnLoadData mCallback = null;
    /**
     * Hold a reference to the current animator, so that it can be canceled mid-way.
     */
    private Animator mCurrentAnimator;

    /**
     * The system "short" animation time duration, in milliseconds. This duration is ideal for
     * subtle animations or animations that occur very frequently.
     */
    private int mShortAnimationDuration;
    
	public FragmentEnterList() {
		super();
	}
	
    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	return inflater.inflate(R.layout.fragment_enter_list, container, false);
	}
	
    @Override
	public void onActivityCreated (Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (savedInstanceState!=null) {
			mItemsSelected = savedInstanceState.getIntegerArrayList(KEY_ITEMS_SELECTED);
			mSelectItemName = savedInstanceState.getString(KEY_SELECT_ITEM);
		}
		LoadProductList();
	}

    @Override
    public void onStart() {
    	super.onStart();
    	if (mSelectItemName!=null) {
    		getLoaderManager().restartLoader(LOADER_ID, null, this);
    	}
    }
    
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode) {
		case SELECT_MARKET_FOR_MOVE:
			if (resultCode == Activity.RESULT_OK) {
				Bundle bundle = data.getExtras();
				String marketName = bundle.getString(MarketListActivity.EXTRA_MARKET_NAME);
				moveSelectedToTarget(marketName);
			}
			break;
		}
	}
	
	@Override
	public void setOnLoadData(OnLoadData callback) {
		mCallback = callback;
	}
	
	private void actionMoveSelectedToTarget() {
		showTargetMarket();
	}
	
	private ArrayList<Integer> getProductsSelected() {
		SparseBooleanArray selected = mAdapter.getSelectedIds();
		ArrayList<Integer> items = null;
		Cursor cursor = mAdapter.getCursor();
		if (cursor!=null) {
			int total = cursor.getCount();
			if (selected.size() >0) items = new ArrayList<Integer>();
			for (int index=0; index<total; index++) {
				cursor.moveToPosition(index);
				if (selected.get(index)) {
					int id = cursor.getInt(cursor.getColumnIndex(ShoppingListStore.COLUMN_ID));
					items.add((Integer)id);
				}
			}
		}
		return items;
	}
	
	private int getItemPosition(Cursor cursor, String name) {
		int result = 0;
		if (cursor!=null) {
			cursor.moveToFirst();
			while(!cursor.isAfterLast()) {
				String productName = cursor.getString(cursor.getColumnIndex(ShoppingListStore.COLUMN_PRODUCT_NAME));
				if (productName.equals(name)) {
					result = cursor.getPosition();
					break;
				}
				cursor.moveToNext();
			}
		}
		return result;
	}
	
	private void DeleteProduct(int position) {
		Cursor cursor = (Cursor)mAdapter.getItem(position);
		int id = cursor.getInt(cursor.getColumnIndex(ShoppingListStore.COLUMN_ID));
		ConfirmationDialog confirmationDialog = new ConfirmationDialog();
		Bundle args = new Bundle();
		args.putInt("id",id);
		String message = getResources().getString(R.string.textProductDeleteWarning);
		args.putString("message", message);
		String title = getResources().getString(R.string.deleteProduct);
		title+= " " + cursor.getString(cursor.getColumnIndex(ShoppingListStore.COLUMN_PRODUCT_NAME));
		args.putString("title", title);
		confirmationDialog.setArguments(args);
		confirmationDialog.callback = this;
		confirmationDialog.show(getFragmentManager(), "dialog");
	}
	
	public void doPositiveClick(int id) {
		ShoppingListRepository shoppingListRepository = new ShoppingListRepository(getActivity());
		if (shoppingListRepository.deleteProductItem(id)) {
			getActivity().getContentResolver().notifyChange(AppPreferences.URI_HISTORY_TABLE, null);
			getActivity().getContentResolver().notifyChange(AppPreferences.URI_LIST_TABLE, null);
		}
		shoppingListRepository.close();
	}
	
	private void EditProduct(int position) {
		Cursor cursor = (Cursor)mAdapter.getItem(position);
		if (cursor!=null) {
			Intent intent = new Intent(this.getActivity(), ProductActivity.class);
			intent.putExtra(ProductActivity.EXTRA_PRODUCT_ID, cursor.getInt(cursor.getColumnIndex(ShoppingListStore.COLUMN_ID)));
			intent.putExtra(ProductActivity.EXTRA_PRODUCT_NAME, cursor.getString(cursor.getColumnIndex(ShoppingListStore.COLUMN_PRODUCT_NAME)));
			intent.putExtra(ProductActivity.EXTRA_MARKET_NAME, cursor.getString(cursor.getColumnIndex(ShoppingListStore.COLUMN_MARKET)));
			intent.putExtra(ProductActivity.EXTRA_AMOUNT, cursor.getString(cursor.getColumnIndex(ShoppingListStore.COLUMN_AMOUNT)));
			intent.putExtra(ProductActivity.EXTRA_UNIT_ID, cursor.getInt(cursor.getColumnIndex(ShoppingListStore.COLUMN_UNIT_ID)));
			intent.putExtra(ProductActivity.EXTRA_CATEGORY, cursor.getInt(cursor.getColumnIndex(ShoppingListStore.COLUMN_CATEGORY)));
			startActivityForResult(intent, EDIT_PRODUCT);
		}
	}
	
	private void LoadProductList() {
        String[] fields = new String[] { ShoppingListStore.COLUMN_PRODUCT_NAME };
        int[] listViewColumns = new int[] { R.id.label };
                
        try {
        	mListView = (ListView) getView().findViewById(R.id.listview01);
        	LoaderManager loaderManager = getLoaderManager();
    		loaderManager.initLoader(LOADER_ID, null, this);
        	mAdapter = new EnterListAdapter(this.getActivity(), R.layout.rowlayout, null, 
        		fields, listViewColumns);
        	mListView.setAdapter(mAdapter);
        	mListView.setOnItemClickListener(this);
        	mListView.setOnItemLongClickListener(new OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> parent,
						View view, int position, long id) {
					if (mActionMode==null) {
						onListItemSelect(position);
						return true;
					} else {
						return false;
					}
				}
			});
        	
        } catch (Exception e) {

        }
	}

	private void moveSelectedToTarget(String targetMarket) {
		if (mItemsSelected!=null) {
			ShoppingListRepository listRepository = new ShoppingListRepository(getActivity());
			for (int index=0; index<mItemsSelected.size(); index++) {
				int id = mItemsSelected.get(index).intValue();
				listRepository.moveToSupermaket(id, targetMarket);
			}
			listRepository.close();
			mItemsSelected = null;
		}
		getLoaderManager().restartLoader(LOADER_ID, null, this);
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (mActionMode!=null) {
			onListItemSelect(position);
		}
	}
	
    private void onListItemSelect(int position) {
    	mAdapter.toggleSelection(position);
        boolean hasCheckedItems = mAdapter.getSelectedCount() > 0;
 
        if (hasCheckedItems && mActionMode == null)
            // there are some selected items, start the actionMode
            mActionMode = getSherlockActivity().startActionMode(new ActionModeCallback());
        else if (!hasCheckedItems && mActionMode != null)
            // there no selected items, finish the actionMode
            mActionMode.finish();
 
        if (mActionMode != null)
            mActionMode.setTitle(String.valueOf(mAdapter.getSelectedCount()) + " " + getResources().getString(R.string.textSelected));
    }
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		if (mCallback!=null) mCallback.onLoadStart();
		return new ShoppingListCursorLoader(getActivity());
	}


	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (loader.getId()==LOADER_ID) {
			mAdapter.swapCursor(cursor);
			if (mSelectItemName!=null) {
				final int position = getItemPosition(cursor, mSelectItemName);
				mListView.post(new Runnable() {
					//push at the end of queue message and will be processed
					//after listview is loaded.
					@Override
					public void run() {
						mListView.setSelection(position);
					}
				});
				mSelectItemName = null;
			}
		}
		if (mCallback!=null) {
			int items = 0;
			if (cursor!=null) { 
				items = cursor.getCount();
				HelpView helpView = (HelpView) getView().findViewById(R.id.viewCapsules);
				if (items==0) {
					helpView.setVisibility(View.VISIBLE);
					helpView.setOnHelpViewClick(new OnHelpViewClick() {
						@Override
						public void onCapsuleClick(TypeView capsule) {
							Intent intent = new Intent(getActivity(), HelpActivityFrame.class);
							intent.putExtra(HelpActivityFrame.EXTRA_INITIAL_CAPSULE, capsule.getValue());
							startActivity(intent);
						}
					});
				} else {
					helpView.setVisibility(View.GONE);	
				}
			}
			mCallback.onLoadFinish(items, TAG_FRAGMENT);
		}
	}


	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}
	
	@Override
	public void onSaveInstanceState (Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mItemsSelected!=null) {
			outState.putIntegerArrayList(KEY_ITEMS_SELECTED, mItemsSelected);
		} 
		if (mSelectItemName!=null) {
			outState.putString(KEY_SELECT_ITEM, mSelectItemName);
		}
	}
	
	public void setProductVisible(String name) {
		mSelectItemName = name;
	}
	
	private void showTargetMarket() {
		Intent intent = new Intent(getActivity(), MarketListActivity.class);
		intent.putExtra(MarketListActivity.IN_EXTRA_SHOW_ALL, true);
		intent.putExtra(MarketListActivity.IN_EXTRA_SHOW_CHECK_NO_MARKET, false);
		startActivityForResult(intent, SELECT_MARKET_FOR_MOVE); 
	}
	
	private void zoomImageFromThumb(final View thumbView, int sdk) {
		if (sdk<Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
	        final ImageView expandedImageView = (ImageView) getActivity().findViewById(R.id.expanded_image);
	        File file = new File(getActivity().getFilesDir() + File.separator + MarketActivity.BITMAP_FILE_NAME);
	        if (file.exists()) {
	        	Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
	        	expandedImageView.setImageBitmap(bitmap);
	        	expandedImageView.setVisibility(View.VISIBLE);
	        	expandedImageView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						v.setVisibility(View.GONE);
					}
				});
	        }
			
		} else {
			zoomImageFromThumb(thumbView);
		}
	}
	
    /**
     * "Zooms" in a thumbnail view by assigning the high resolution image to a hidden "zoomed-in"
     * image view and animating its bounds to fit the entire activity content area. More
     * specifically:
     *
     * <ol>
     *   <li>Assign the high-res image to the hidden "zoomed-in" (expanded) image view.</li>
     *   <li>Calculate the starting and ending bounds for the expanded view.</li>
     *   <li>Animate each of four positioning/sizing properties (X, Y, SCALE_X, SCALE_Y)
     *       simultaneously, from the starting bounds to the ending bounds.</li>
     *   <li>Zoom back out by running the reverse animation on click.</li>
     * </ol>
     *
     * @param thumbView  The thumbnail view to zoom in.
     * @param imageResId The high-resolution version of the image represented by the thumbnail.
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private void zoomImageFromThumb(final View thumbView) {
        // If there's an animation in progress, cancel it immediately and proceed with this one.
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        // Load the high-resolution "zoomed-in" image.
        final ImageView expandedImageView = (ImageView) getActivity().findViewById(R.id.expanded_image);
        File file = new File(getActivity().getFilesDir() + File.separator + MarketActivity.BITMAP_FILE_NAME);
        if (file.exists()) {
        	Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        	expandedImageView.setImageBitmap(bitmap);

        	// Calculate the starting and ending bounds for the zoomed-in image. This step
            // involves lots of math. Yay, math.
            final Rect startBounds = new Rect();
            final Rect finalBounds = new Rect();
            final Point globalOffset = new Point();

            // The start bounds are the global visible rectangle of the thumbnail, and the
            // final bounds are the global visible rectangle of the container view. Also
            // set the container view's offset as the origin for the bounds, since that's
            // the origin for the positioning animation properties (X, Y).
            thumbView.getGlobalVisibleRect(startBounds);
            getActivity().findViewById(R.id.layoutEnterList).getGlobalVisibleRect(finalBounds, globalOffset);
            startBounds.offset(-globalOffset.x, -globalOffset.y);
            finalBounds.offset(-globalOffset.x, -globalOffset.y);

            // Adjust the start bounds to be the same aspect ratio as the final bounds using the
            // "center crop" technique. This prevents undesirable stretching during the animation.
            // Also calculate the start scaling factor (the end scaling factor is always 1.0).
            float startScale;
            if ((float) finalBounds.width() / finalBounds.height()
                    > (float) startBounds.width() / startBounds.height()) {
                // Extend start bounds horizontally
                startScale = (float) startBounds.height() / finalBounds.height();
                float startWidth = startScale * finalBounds.width();
                float deltaWidth = (startWidth - startBounds.width()) / 2;
                startBounds.left -= deltaWidth;
                startBounds.right += deltaWidth;
            } else {
                // Extend start bounds vertically
                startScale = (float) startBounds.width() / finalBounds.width();
                float startHeight = startScale * finalBounds.height();
                float deltaHeight = (startHeight - startBounds.height()) / 2;
                startBounds.top -= deltaHeight;
                startBounds.bottom += deltaHeight;
            }

            // Hide the thumbnail and show the zoomed-in view. When the animation begins,
            // it will position the zoomed-in view in the place of the thumbnail.
            thumbView.setAlpha(0f);
            expandedImageView.setVisibility(View.VISIBLE);

            // Set the pivot point for SCALE_X and SCALE_Y transformations to the top-left corner of
            // the zoomed-in view (the default is the center of the view).
            expandedImageView.setPivotX(0f);
            expandedImageView.setPivotY(0f);

            // Construct and run the parallel animation of the four translation and scale properties
            // (X, Y, SCALE_X, and SCALE_Y).
            AnimatorSet set = new AnimatorSet();
            set
                    .play(ObjectAnimator.ofFloat(expandedImageView, View.X, startBounds.left,
                            finalBounds.left))
                    .with(ObjectAnimator.ofFloat(expandedImageView, View.Y, startBounds.top,
                            finalBounds.top))
                    .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X, startScale, 1f))
                    .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_Y, startScale, 1f));
            set.setDuration(mShortAnimationDuration);
            set.setInterpolator(new DecelerateInterpolator());
            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mCurrentAnimator = null;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    mCurrentAnimator = null;
                }
            });
            set.start();
            mCurrentAnimator = set;

            // Upon clicking the zoomed-in image, it should zoom back down to the original bounds
            // and show the thumbnail instead of the expanded image.
            final float startScaleFinal = startScale;
            expandedImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mCurrentAnimator != null) {
                        mCurrentAnimator.cancel();
                    }

                    // Animate the four positioning/sizing properties in parallel, back to their
                    // original values.
                    AnimatorSet set = new AnimatorSet();
                    set
                            .play(ObjectAnimator.ofFloat(expandedImageView, View.X, startBounds.left))
                            .with(ObjectAnimator.ofFloat(expandedImageView, View.Y, startBounds.top))
                            .with(ObjectAnimator
                                    .ofFloat(expandedImageView, View.SCALE_X, startScaleFinal))
                            .with(ObjectAnimator
                                    .ofFloat(expandedImageView, View.SCALE_Y, startScaleFinal));
                    set.setDuration(mShortAnimationDuration);
                    set.setInterpolator(new DecelerateInterpolator());
                    set.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            thumbView.setAlpha(1f);
                            expandedImageView.setVisibility(View.GONE);
                            mCurrentAnimator = null;
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                            thumbView.setAlpha(1f);
                            expandedImageView.setVisibility(View.GONE);
                            mCurrentAnimator = null;
                        }
                    });
                    set.start();
                    mCurrentAnimator = set;
                }
            });
        }
    }

	
	
	static class ViewHolder {
		public ImageView imageMarket;
		public TextView text;
		public ImageView image;
		public ImageView imageDelete;
	}
	
    public class EnterListAdapter extends SimpleCursorAdapter {
    	
    	Context mContext;
    	Cursor mCursor;
    	private SparseBooleanArray mSelectedItems;
    	
		public EnterListAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
    		super(context, layout, c, from, to, 0);
    		mContext = context;
    		mCursor = c;
    		mSelectedItems = new SparseBooleanArray();
    	}
		
		@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
		@Override
    	public View getView(int position, View convertView, ViewGroup parent) {
    		
    		final ViewHolder viewHolder;
    		final int pos = position;
    		if (convertView == null) {
    			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    			convertView = inflater.inflate(R.layout.rowlayout, parent, false);
    			viewHolder = new ViewHolder();
    			viewHolder.imageMarket = (ImageView) convertView.findViewById(R.id.imageSuperMarket);
    			viewHolder.text = (TextView) convertView.findViewById(R.id.label);
    			viewHolder.image = (ImageView) convertView.findViewById(R.id.imageRightArrow);
   				viewHolder.imageDelete = (ImageView) convertView.findViewById(R.id.imageDelete);
    			convertView.setTag(viewHolder);
    		} else {
    			viewHolder = (ViewHolder) convertView.getTag();
    		}

			viewHolder.image.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					EditProduct(pos);
				}
			});
			
			viewHolder.imageDelete.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					DeleteProduct(pos);
				}
			});
			
    		mCursor.moveToPosition(position);
    		String marketName = mCursor.getString(mCursor.getColumnIndex(ShoppingListStore.COLUMN_MARKET));
    		viewHolder.imageMarket.setVisibility(View.VISIBLE);
    		if (marketName!=null) {
    			MarketRepository marketRepository = new MarketRepository(getActivity());
    			Integer color = marketRepository.getMarketColor(marketName);
    			marketRepository.close();
    			if (color!=null) {
    				viewHolder.imageMarket.setImageDrawable(null);
    				GradientDrawable gradientDrawable = (GradientDrawable) getResources().getDrawable(R.drawable.square_white);
    				gradientDrawable.mutate();
    				gradientDrawable.setColor(color);
    				viewHolder.imageMarket.setImageDrawable(gradientDrawable);
    			} else {
    				viewHolder.imageMarket.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_market));
    			}
    			
    			viewHolder.imageMarket.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
				        // Retrieve and cache the system's default "short" animation time.
				        mShortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
				        zoomImageFromThumb(v, Build.VERSION.SDK_INT);
					}
				});
    			
    		} else {
    			viewHolder.imageMarket.setImageDrawable(null);
    			GradientDrawable gradientDrawable = (GradientDrawable) getResources().getDrawable(R.drawable.square_white);
    			viewHolder.imageMarket.setImageDrawable(gradientDrawable);
    		}

    		viewHolder.text.setText(util.getCompleteProductRow(mContext, mCursor, false));
			viewHolder.image.setImageResource(R.drawable.ic_action_edit);
			viewHolder.imageDelete.setVisibility(View.VISIBLE);
			if (mSelectedItems.get(position)) {
				viewHolder.text.setTextColor(getResources().getColor(android.R.color.white));
				viewHolder.text.setTypeface(font.getListItemSelectedFont(getActivity()));  
				convertView.setBackgroundColor(getResources().getColor(R.color.rowSelected));
			} else {
				viewHolder.text.setTextColor(getResources().getColor(android.R.color.black));
	    		viewHolder.text.setTypeface(font.getListItemFont(getActivity()));
				convertView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
			}
    		return convertView;
    	}
    	
    	@Override
    	public Cursor swapCursor(Cursor c) {
    		mCursor = c;
    		return super.swapCursor(c);
    	}

        public void toggleSelection(int position) {
            selectView(position, !mSelectedItems.get(position));
        }
    	
        public void removeSelection() {
        	mSelectedItems = new SparseBooleanArray();
            notifyDataSetChanged();
        }
    	
    	public void selectView(int position, boolean value) {
    		if (value) 
    			mSelectedItems.put(position, value);
    		else 
    			mSelectedItems.delete(position);
            notifyDataSetChanged();
    	}
    	
    	public int getSelectedCount() {
    		return mSelectedItems.size();
    	}
    	
    	public SparseBooleanArray getSelectedIds() {
    		return mSelectedItems;
    	}
    }
		
	private static class ShoppingListCursorLoader extends CursorLoader {
		
		// We hold a reference to the Loader�s data here.
		private Cursor mCursor = null;
		ShoppingListRepository mShoppingListRepository = null;
		final ForceLoadContentObserver mObserver;
		
		public ShoppingListCursorLoader(Context context) {
			// Loaders may be used across multiple Activitys (assuming they aren't
		    // bound to the LoaderManager), so NEVER hold a reference to the context
		    // directly. Doing so will cause you to leak an entire Activity's context.
		    // The superclass constructor will store a reference to the Application
		    // Context instead, and can be retrieved with a call to getContext().
			super(context);
			this.mObserver = new ForceLoadContentObserver();
		}
		
		/****************************************************/
		/** (1) A task that performs the asynchronous load **/
		/****************************************************/
	    @Override
		public Cursor loadInBackground() {
		    // This method is called on a background thread and should generate a
		    // new set of data to be delivered back to the client.
	        mShoppingListRepository = new ShoppingListRepository(this.getContext());
	        mCursor = mShoppingListRepository.getProductsNoCommittedByMarket(null);
	        if (mCursor!=null) {
	            // Ensure the cursor window is filled
	        	mCursor.getCount();
	            // this is to force a reload when the content change
	        	mCursor.registerContentObserver(this.mObserver);
	            // this make sure this loader will be notified when
	            // a notifyChange is called on the URI_MY_TABLE
	            mCursor.setNotificationUri(getContext().getContentResolver(), AppPreferences.URI_LIST_TABLE);
	        }
			return mCursor;
		}
		
		/********************************************************/
		/** (2) Deliver the results to the registered listener **/
		/********************************************************/
		@Override
		public void deliverResult(Cursor cursor) {
			if (isReset()) {
				// The Loader has been reset; ignore the result and invalidate the data.
				if (cursor!=null) {
					ReleaseResources(cursor);
				}
				return;
			}
			// Hold a reference to the old data so it doesn't get garbage collected.
		    // We must protect it until the new data has been delivered.
			Cursor oldCursor = mCursor;
			mCursor = cursor;
			
			if (isStarted()) {
			    // If the Loader is in a started state, deliver the results to the
			    // client. The superclass method does this for us.
				super.deliverResult(cursor);
			}
			// Invalidate the old data as we don't need it any more
			if (oldCursor!=null && oldCursor!=cursor) {
				ReleaseResources(oldCursor);
			}
		}

	    /*********************************************************/
	    /** (3) Implement the Loader�s state-dependent behavior **/
	    /*********************************************************/

	    @Override
	    protected void onStartLoading() {
	    	if (mCursor != null) {
		    	// Deliver any previously loaded data immediately.
		    	deliverResult(mCursor);
	    	}
		    if (takeContentChanged() || mCursor == null) {
			    // When the observer detects a change, it should call onContentChanged()
			    // on the Loader, which will cause the next call to takeContentChanged()
			    // to return true. If this is ever the case (or if the current data is
			    // null), we force a new load.
			    forceLoad();
		    }
	    }
	    
	    @Override
	    protected void onStopLoading() {
		    // The Loader is in a stopped state, so we should attempt to cancel the 
		    // current load (if there is one).
		    cancelLoad();
		    // Note that we leave the observer as is. Loaders in a stopped state
		    // should still monitor the data source for changes so that the Loader
		    // will know to force a new load if it is ever started again.
	    }
		
	    @Override
	    public void onCanceled(Cursor cursor) {
	    	// Attempt to cancel the current asynchronous load.
	        super.onCanceled(mCursor);
	        // The load has been canceled, so we should release the resources
	        // associated with 'data'.
	        ReleaseResources(cursor);
	    }
	    
	    @Override
	    protected void onReset() {
	    	// Ensure the loader has been stopped.
	        onStopLoading();
	        // At this point we can release the resources associated with 'mData'.
	        if (mCursor != null) {
	        	ReleaseResources(mCursor);
	        	mCursor = null;
	        }
	    }
    
		private void ReleaseResources(Cursor cursor) {
			if (cursor!=null) {
				cursor.close();
				mShoppingListRepository.close();
			}
		}
	}

	private class ActionModeCallback implements ActionMode.Callback {
		 
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // inflate contextual menu
            mode.getMenuInflater().inflate(R.menu.current_list, menu);
            return true;
        }
 
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }
 
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
            case R.id.moveProduct:
            	mItemsSelected = getProductsSelected();
            	actionMoveSelectedToTarget();
            	mode.finish();
            	return true;
            default:
                return false;
            }
 
        }
 
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            // remove selection
        	mAdapter.removeSelection();
            mActionMode = null;
        }
    }

	public static class ConfirmationDialog extends SherlockDialogFragment {
		
		public SherlockFragment callback;
		
		public ConfirmationDialog() {
			super();
		}
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final int id = getArguments().getInt("id");
			String message = getArguments().getString("message");
			String title = getArguments().getString("title");
			Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
			alertDialogBuilder.setTitle(title);
			alertDialogBuilder.setMessage(message);
			alertDialogBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					((FragmentEnterList) callback).doPositiveClick(id);					
				}
			});
			alertDialogBuilder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					getDialog().dismiss();
				}
			});
			
			return alertDialogBuilder.create();
		}
		
	}

}
