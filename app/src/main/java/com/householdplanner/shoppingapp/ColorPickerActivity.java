package com.householdplanner.shoppingapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.view.View;

import com.applilandia.widget.ColorPicker;
import com.applilandia.widget.OpacityBar;
import com.applilandia.widget.SaturationBar;

/**
 * Created by JuanCarlos on 21/05/2015.
 */
public class ColorPickerActivity extends AppCompatActivity {

    private static final String LOG_TAG = ColorPickerActivity.class.getSimpleName();

    //Extras
    public static final String EXTRA_OLD_COLOR = "OldColor";
    public static final String EXTRA_CURRENT_COLOR = "CurrentColor";
    public static final String EXTRA_MARKET_ID = "MarketId";

    //Hold the market that the color belongs to
    private int mMarketId = 0;
    //Hold the old color
    private int mOldColor = 0;
    //Color picker view
    private ColorPicker mColorPicker;
    private OpacityBar mOpacityBar;
    private SaturationBar mSaturationBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_picker);
        //Get extras
        loadExtras();
        //Set the color picker initial state
        initColorPicker();
        createButtonHandlers();
    }

    /**
     * Init the color picker view
     */
    private void initColorPicker() {
        mColorPicker = (ColorPicker) findViewById(R.id.colorPickerMarket);
        mColorPicker.setOldCenterColor(mOldColor);
        //Bars
        mOpacityBar = (OpacityBar) findViewById(R.id.opacityBarMarket);
        mSaturationBar = (SaturationBar) findViewById(R.id.saturationBarMarket);
        //Connect bars to picker
        mColorPicker.addOpacityBar(mOpacityBar);
        mColorPicker.addSaturationBar(mSaturationBar);
    }

    /**
     * Create the handlers for the buttons in the activity
     */
    private void createButtonHandlers() {
        AppCompatButton buttonOk = (AppCompatButton) findViewById(R.id.buttonOk);
        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra(EXTRA_CURRENT_COLOR, mColorPicker.getColor());
                intent.putExtra(EXTRA_MARKET_ID, mMarketId);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    /**
     * Load the passed extras
     */
    private void loadExtras() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mOldColor = bundle.getInt(EXTRA_OLD_COLOR);
            mMarketId = bundle.getInt(EXTRA_MARKET_ID);
        }
    }
}
