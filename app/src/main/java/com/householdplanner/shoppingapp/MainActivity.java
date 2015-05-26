package com.householdplanner.shoppingapp;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.householdplanner.shoppingapp.asynctasks.BasketTask;
import com.householdplanner.shoppingapp.cross.AppGlobalState;
import com.householdplanner.shoppingapp.cross.AppPreferences;
import com.householdplanner.shoppingapp.cross.OnLoadData;
import com.householdplanner.shoppingapp.cross.ProgressCircle;
import com.householdplanner.shoppingapp.cross.util;
import com.householdplanner.shoppingapp.fragments.FragmentDoShopping;
import com.householdplanner.shoppingapp.fragments.FragmentEnterData;
import com.householdplanner.shoppingapp.fragments.FragmentEnterList;
import com.householdplanner.shoppingapp.repositories.ShoppingListRepository;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements Product.OnSaveProduct,
        OnLoadData {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final String TAG_FRAGMENT_ENTER_DATA = "fragmentEnterData";
    private static final String TAG_FRAGMENT_DO_SHOPPING = "fragmentDoShopping";

    private static final int PRODUCT_ADD = 1;
    private static final int WALLET = 2;
    private static final int SETTINGS = 3;
    private static final int VOICE_RECOGNITION = 4;
    private static final int SELECT_MARKET = 5;
    private static final int EXPENSES_ACTIVITY = 7;
    private static final int SHARING_HELP_ACTIVITY = 9;
    private static boolean mStarted = false;
    private static long mBackPressedTime = 0;
    private static int mCurrentItems = 0;
    private Menu mActionMenu = null;
    private ProgressCircle mProgressDialog = null;
    private ProgressHandler mHandler = null;
    private DrawerLayout mDrawerLayout;
    //List where the items of drawer menu will be drawn
    private ListView mDrawerList;
    //Link action bar to DrawerLayout
    private ActionBarDrawerToggle mDrawerToggle;

    /*
     * For getting log from apk on device:
     * adb -d logcat > logCat.txt
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Configure the toolbar
        initToolbar();
        createNavigationDrawer();
        mStarted = false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mStarted) {
            setFragment();
            mStarted = true;
        } else {
            if (!AppGlobalState.getInstance().isShoppingMode(this)) {
                //TODO: Review it
                //setShoppingMenu();
            }
        }
    }

    /**
     * Create and init the navigation drawer
     */
    private void createNavigationDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.setAdapter(new DrawerListAdapter(this));
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent;
                switch (position) {
                    case DrawerListAdapter.SUPERMARKET_INDEX:
                        intent = new Intent(MainActivity.this, MarketActivity.class);
                        startActivity(intent);
                        break;
                    case DrawerListAdapter.EXPENSES_INDEX:
                        intent = new Intent(MainActivity.this, WalletActivity.class);
                        startActivityForResult(intent, WALLET);
                        break;
                    case DrawerListAdapter.SETTINGS_INDEX:
                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
                            intent = new Intent(MainActivity.this, SettingActivity.class);
                        } else {
                            intent = new Intent(MainActivity.this, AppPreferences.class);
                        }
                        startActivity(intent);
                        break;
                }
                mDrawerLayout.closeDrawers();
            }
        });

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.text_accessibility_drawer_open,
                R.string.text_accessibility_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
                syncState();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
                syncState();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
    }

    /**
     * Executed once instance state has been restored
     *
     * @param savedInstanceState
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newChange) {
        super.onConfigurationChanged(newChange);
        mDrawerToggle.onConfigurationChanged(newChange);
    }

    private void setFragment() {
        if (!AppGlobalState.getInstance().isShoppingMode(this)) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.fragment_container, new FragmentEnterData(), TAG_FRAGMENT_ENTER_DATA);
            transaction.commit();
        } else {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.fragment_container, new FragmentDoShopping(), TAG_FRAGMENT_DO_SHOPPING);
            transaction.commit();
        }
    }

    private void executeActionDoShopping() {
        if (AppGlobalState.getInstance().isShoppingMode(this)) {
            util.showAlertInfoMessage(this, R.string.textAskWriteExpense,
                    R.string.textButtonYes, R.string.textButtonNo,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            boolean hasProducts = hasBasketProducts();
                            Intent intent = new Intent(MainActivity.this, TicketActivity.class);
                            intent.putExtra(TicketActivity.EXTRA_HAS_PRODUCTS, hasProducts);
                            startActivityForResult(intent, EXPENSES_ACTIVITY);
                        }
                    },
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            BasketTask basketTask = new BasketTask(MainActivity.this);
                            if (mProgressDialog == null) {
                                mProgressDialog = new ProgressCircle(MainActivity.this);
                                mProgressDialog.show();
                                mHandler = new ProgressHandler(MainActivity.this);
                            } else {
                                mHandler = null;
                            }
                            basketTask.execute(mHandler);
                            exitShoppingMode();
                        }
                    }, null);
        } else {
            if (moreThanOneSupermarket()) {
                Intent intent = new Intent(this, MarketListActivity.class);
                intent.putExtra(MarketListActivity.IN_EXTRA_SHOW_ALL, false);
                intent.putExtra(MarketListActivity.IN_EXTRA_SHOW_CHECK_NO_MARKET, true);
                startActivityForResult(intent, SELECT_MARKET);
            } else {
                AppGlobalState.getInstance().setMarket(MainActivity.this, 0, null);
                enterShoppingMode();
            }
        }
    }

    private boolean hasBasketProducts() {
        ShoppingListRepository listRepository = new ShoppingListRepository(this);
        Cursor cursor = listRepository.getProductsCommitted();
        if (cursor != null) {
            if (cursor.getCount() > 0) return true;
            else return false;
        }
        return false;
    }

    private void hideActionBarIcons() {
        mActionMenu.findItem(R.id.action_productAddByVoice).setVisible(false);
        mActionMenu.findItem(R.id.action_productAdd).setVisible(false);
        mActionMenu.findItem(R.id.action_doShopping).setVisible(false);
    }

    /**
     * Configure the toolbar
     */
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.productToolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private boolean moreThanOneSupermarket() {
        boolean result = false;
        ShoppingListRepository shoppingListRepository = new ShoppingListRepository(this);
        result = shoppingListRepository.existsProductsInSupermarket();
        shoppingListRepository.close();
        return result;
    }


    @Override
    public void onLoadStart() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressCircle(this);
            mProgressDialog.show();
        }
    }

    @Override
    public void onLoadFinish(int items, String source) {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
        if (!AppGlobalState.getInstance().isShoppingMode(this)) {
            if ((source != null) && (source.equals(FragmentEnterList.TAG_FRAGMENT))) {
                mCurrentItems = items;
                //TODO: Review shopping mode
                //setShoppingMenu();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppGlobalState.getInstance().setActiveValue(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
        if (mActionMenu != null) {
            MenuItem actionItem = mActionMenu.findItem(R.id.action_search);
            if (actionItem != null) {
                MenuItemCompat.collapseActionView(actionItem);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        mActionMenu = menu;
        if (AppGlobalState.getInstance().isShoppingMode(this)) {
            restartShoppingMode();
        } else {
            final MenuItem searchItem = menu.findItem(R.id.action_search);
            SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
            searchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setQueryHint(getString(R.string.text_hint_search_view_product));
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

                @Override
                public boolean onQueryTextSubmit(String s) {
                    if (mProgressDialog == null) {
                        mProgressDialog = new ProgressCircle(MainActivity.this);
                        mProgressDialog.show();
                    }
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    return false;
                }
            });

            searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
                @Override
                public boolean onSuggestionSelect(int i) {
                    return false;
                }

                @Override
                public boolean onSuggestionClick(int i) {
                    MenuItemCompat.collapseActionView(searchItem);
                    showActionBarIcons();
                    return false;
                }
            });

            MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionExpand(MenuItem item) {
                    hideActionBarIcons();
                    return true;
                }

                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                    showActionBarIcons();
                    return true;
                }
            });
        }
        return super.onCreateOptionsMenu(menu);
    }


/*  @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_productAddByVoice:
                showForVoiceRecognition();
                return true;

            case R.id.action_productAdd:
                intent = new Intent(this, ProductActivity.class);
                startActivityForResult(intent, PRODUCT_ADD);
                return true;

            case R.id.action_doShopping:
                executeActionDoShopping();
                return true;

            case R.id.action_superMarkets:
                intent = new Intent(this, MarketActivity.class);
                startActivity(intent);
                return true;

            case R.id.action_wallet:
                intent = new Intent(this, WalletActivity.class);
                startActivityForResult(intent, WALLET);
                return true;

            case R.id.action_settings:
                intent = new Intent(this, AppPreferences.class);
                startActivityForResult(intent, SETTINGS);
                return true;

            case R.id.action_help:
                intent = new Intent(this, HelpActivityFrame.class);
                intent.putExtra(HelpActivityFrame.EXTRA_INITIAL_CAPSULE, TypeView.EnterProducts.getValue());
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
                    mDrawerLayout.closeDrawer(mDrawerList);
                } else {
                    mDrawerLayout.openDrawer(mDrawerList);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {        // to prevent irritating accidental exit
        long t = System.currentTimeMillis();
        if (t - mBackPressedTime > 2000) {    // 2 secs
            mBackPressedTime = t;
            Toast.makeText(this, R.string.textBackPressed, Toast.LENGTH_SHORT).show();
        } else {    // this guy is serious
            // clean up
            super.onBackPressed();       // bye
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PRODUCT_ADD:
                if (resultCode == RESULT_OK) {
                    String productName = data.getStringExtra(ProductActivity.EXTRA_PRODUCT_NAME);
                    FragmentEnterData fragment = (FragmentEnterData) getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT_ENTER_DATA);
                    fragment.setProductVisible(productName);
                }
                break;

            case VOICE_RECOGNITION:
                if (resultCode == RESULT_OK) {
                    ArrayList<String> words = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    addProductToList(words);
                }
                break;

            case SELECT_MARKET:
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        Bundle args = data.getExtras();
                        int marketId = args.getInt(MarketListActivity.EXTRA_MARKET_ID);
                        String marketName = args.getString(MarketListActivity.EXTRA_MARKET_NAME);
                        AppGlobalState.getInstance().setMarket(this, marketId, marketName);
                        enterShoppingMode();
                    }
                }
                break;

            case EXPENSES_ACTIVITY:
                if (resultCode == Activity.RESULT_OK) {
                    exitShoppingMode();
                }
                break;

            case SHARING_HELP_ACTIVITY:
                Intent intent = new Intent(MainActivity.this, AppPreferences.class);
                startActivity(intent);
                break;
        }
    }

    private void showActionBarIcons() {
        mActionMenu.findItem(R.id.action_productAddByVoice).setVisible(true);
        mActionMenu.findItem(R.id.action_productAdd).setVisible(true);
        mActionMenu.findItem(R.id.action_doShopping).setVisible(true);
    }

    private void showForVoiceRecognition() {
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_PROMPT, R.string.title_product);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        try {
            startActivityForResult(i, VOICE_RECOGNITION);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void addProductToList(ArrayList<String> words) {
        if (words.size() > 0) {
            VoiceInputToken inputToken = new VoiceInputToken(words.get(0));
            inputToken.parse();
            String productName = inputToken.getProductName();
            String market = null;
            String amount = inputToken.getAmount();
            int unitId = inputToken.getUnitId();
            Product product = new Product(this, productName, market, amount, unitId, this);
            product.addProductToList();
        }
    }

    private void reloadProductList() {
        if (!isFinishing()) {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            ViewPager viewPager = (ViewPager) fragment.getView().findViewById(R.id.pagerEnterData);
            viewPager.setCurrentItem(0);
        }
    }

    private void setShoppingMenu() {
        MenuItem menuItem = mActionMenu.findItem(R.id.action_doShopping);
        if (menuItem != null) {
            if (mCurrentItems == 0) {
                menuItem.setVisible(false);
            } else {
                menuItem.setVisible(true);
            }
        }
    }

    private void swapFragmentTo(String tag) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if (tag.equals(TAG_FRAGMENT_ENTER_DATA)) {
            fragmentTransaction.replace(R.id.fragment_container, new FragmentEnterData(), TAG_FRAGMENT_ENTER_DATA);
        } else {
            fragmentTransaction.replace(R.id.fragment_container, new FragmentDoShopping(), TAG_FRAGMENT_DO_SHOPPING);
        }
        fragmentTransaction.commit();
    }

    public void enterShoppingMode() {
        if (mActionMenu != null) {
            mActionMenu.findItem(R.id.action_productAddByVoice).setVisible(false);
            mActionMenu.findItem(R.id.action_productAdd).setVisible(false);
            mActionMenu.findItem(R.id.action_search).setVisible(false);
            MenuItem item = mActionMenu.findItem(R.id.action_doShopping);
            item.setIcon(R.drawable.ic_action_endshopping);
            item.setTitle(R.string.btnEndShopping);
            MenuItemCompat.setShowAsAction(item, MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        }
        AppGlobalState.getInstance().setShoppingMode(this, true);
        swapFragmentTo(TAG_FRAGMENT_DO_SHOPPING);
    }

    private void restartShoppingMode() {
        if (mActionMenu != null) {
            mActionMenu.findItem(R.id.action_productAddByVoice).setVisible(false);
            mActionMenu.findItem(R.id.action_productAdd).setVisible(false);
            mActionMenu.findItem(R.id.action_search).setVisible(false);
            MenuItem item = mActionMenu.findItem(R.id.action_doShopping);
            item.setIcon(R.drawable.ic_action_endshopping);
            item.setTitle(R.string.btnEndShopping);
            MenuItemCompat.setShowAsAction(item, MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        }
        swapFragmentTo(TAG_FRAGMENT_DO_SHOPPING);
    }

    public void exitShoppingMode() {
        if (mActionMenu != null) {
            mActionMenu.findItem(R.id.action_productAddByVoice).setVisible(true);
            mActionMenu.findItem(R.id.action_productAdd).setVisible(true);
            mActionMenu.findItem(R.id.action_search).setVisible(true);
            MenuItem item = mActionMenu.findItem(R.id.action_doShopping);
            item.setIcon(R.drawable.ic_action_doshopping);
            MenuItemCompat.setShowAsAction(item, MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        }
        AppGlobalState.getInstance().setShoppingMode(this, false);
        swapFragmentTo(TAG_FRAGMENT_ENTER_DATA);
    }

    @Override
    public void onSaveProduct() {
        reloadProductList();
    }

    private static class ProgressHandler extends Handler {

        private WeakReference<MainActivity> mActivity;

        public ProgressHandler(MainActivity activity) {
            mActivity = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mActivity.get();
            if (activity != null) {
                if (msg.arg1 == AppGlobalState.AsyncTaskCodes.BasketTask.getValue()) {
                    if (activity.mProgressDialog != null) {
                        activity.mProgressDialog.dismiss();
                        activity.mProgressDialog = null;
                    }
                } else if (msg.arg1 == AppGlobalState.AsyncTaskCodes.SynchronizationTask.getValue()) {
                    boolean result = msg.arg2 != 0;
                    if (result) {
                        activity.reloadProductList();
                    }
                    if (activity.mProgressDialog != null) {
                        activity.mProgressDialog.dismiss();
                        activity.mProgressDialog = null;
                    }
                }
            }
        }
    }

    private class DrawerItem {
        public int iconResId;
        public int textResId;

        private DrawerItem(int iconResId, int textResId) {
            this.iconResId = iconResId;
            this.textResId = textResId;
        }
    }

    private class DrawerListAdapter extends ArrayAdapter<DrawerItem> {

        public final static int SUPERMARKET_INDEX = 0;
        public final static int EXPENSES_INDEX = 1;
        public final static int SETTINGS_INDEX = 2;
        public final static int HELP_INDEX = 3;

        private List<DrawerItem> mDrawerListItems;

        private DrawerListAdapter(Context context) {
            super(context, R.layout.drawer_list_item);
            mDrawerListItems = new ArrayList<>();
            mDrawerListItems.add(new DrawerItem(R.drawable.ic_action_market, R.string.text_item_markets));
            mDrawerListItems.add(new DrawerItem(R.drawable.ic_currency, R.string.text_item_expenses));
            mDrawerListItems.add(new DrawerItem(R.drawable.ic_action_settings, R.string.text_item_settings));
        }

        @Override
        public int getCount() {
            return mDrawerListItems.size();
        }

        @Override
        public DrawerItem getItem(int position) {
            return mDrawerListItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = getLayoutInflater().inflate(R.layout.drawer_list_item, parent, false);
            ImageView imageView = (ImageView) convertView.findViewById(R.id.imageDrawerAction);
            TextView textView = (TextView) convertView.findViewById(R.id.textViewDrawerAction);
            imageView.setImageResource(mDrawerListItems.get(position).iconResId);
            textView.setText(mDrawerListItems.get(position).textResId);
            return convertView;
        }
    }

}

