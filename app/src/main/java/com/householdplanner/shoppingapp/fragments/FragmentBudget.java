package com.householdplanner.shoppingapp.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatSpinner;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;

import com.applilandia.widget.ValidationField;
import com.householdplanner.shoppingapp.R;
import com.householdplanner.shoppingapp.models.Budget;
import com.householdplanner.shoppingapp.repositories.WalletRepository;
import com.householdplanner.shoppingapp.usecases.UseCaseBudget;

import java.util.Calendar;

/**
 * Created by JuanCarlos on 26/05/2015.
 */
public class FragmentBudget extends Fragment {

    private static final String LOG_TAG = FragmentBudget.class.getSimpleName();

    //Arguments Keys
    private static final String KEY_ID = "id";
    private static final String KEY_MONTH_ID = "month_id";
    private static final String KEY_AVAILABLE = "available";
    private static final String KEY_TARGET = "target";

    private View.OnClickListener mOnClickListener;
    private Budget mBudget;
    //Views
    private AppCompatSpinner mSpinnerBudget;
    private ValidationField mValidationFieldAvailable;
    private ValidationField mValidationFieldTarget;
    private AppCompatButton mButtonOk;

    /**
     * Create the fragment for budget
     *
     * @param id        budget identifier
     * @param monthId   month number starting by 0
     * @param available available money for the month
     * @param target    target expense money for the month
     * @return FragmentBudget
     */
    public static FragmentBudget newInstance(int id, int monthId, float available, float target) {
        FragmentBudget fragmentBudget = new FragmentBudget();
        Bundle args = new Bundle();
        args.putInt(KEY_ID, id);
        args.putInt(KEY_MONTH_ID, monthId);
        args.putFloat(KEY_AVAILABLE, available);
        args.putFloat(KEY_TARGET, target);
        fragmentBudget.setArguments(args);
        return fragmentBudget;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_budget, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        //Get the param values
        loadArguments();
        //Inflate Views
        inflateViews();
        //Init view handlers
        createButtonHandlers();
        //Show the budget data if they already exist
        displayBudgetData();
    }

    /**
     * Inflate the views in the fragment
     */
    private void inflateViews() {
        mSpinnerBudget = (AppCompatSpinner) getView().findViewById(R.id.spinnerMonth);
        mValidationFieldAvailable = (ValidationField) getView().findViewById(R.id.validationFieldAvailable);
        mValidationFieldTarget = (ValidationField) getView().findViewById(R.id.validationFieldTarget);
        mButtonOk = (AppCompatButton) getView().findViewById(R.id.btnSaveBudget);
    }

    /**
     * Recover the arguments to build them into an object
     */
    private void loadArguments() {
        Bundle args = getArguments();
        mBudget = new Budget();
        if (args != null) {
            mBudget._id = args.getInt(KEY_ID);
            mBudget.monthId = args.getInt(KEY_MONTH_ID);
            mBudget.available = args.getInt(KEY_AVAILABLE);
            mBudget.target = args.getInt(KEY_TARGET);
        }
    }

    /**
     * Set the callback for the button
     *
     * @param l
     */
    public void setButtonOnClick(View.OnClickListener l) {
        mOnClickListener = l;
    }

    /**
     * Create the handlers for the button in the activity
     */
    private void createButtonHandlers() {
        mButtonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mValidationFieldAvailable.setError("");
                mValidationFieldTarget.setError("");
                if (validate()) {
                    mBudget.available = Float.parseFloat(mValidationFieldAvailable.getText());
                    mBudget.target = Float.parseFloat(mValidationFieldTarget.getText());
                    UseCaseBudget useCaseBudget = new UseCaseBudget(getActivity());
                    if (useCaseBudget.createOrUpdateBudget(mBudget)) {
                        if (mOnClickListener != null) {
                            mOnClickListener.onClick(mButtonOk);
                        }
                    }
                }
            }
        });
    }

    /**
     * Display the budget data into the views
     */
    private void displayBudgetData() {
        mSpinnerBudget.setOnItemSelectedListener(new MonthOnItemSelectedListener());
        if (mBudget._id != 0) {
            //Existing month
            mSpinnerBudget.setSelection(mBudget.monthId);
            mValidationFieldAvailable.setText(String.valueOf(mBudget.available));
            mValidationFieldTarget.setText(String.valueOf(mBudget.target));
        } else {
            //New month
            mSpinnerBudget.setSelection(getMonthToSelect());
        }
    }

    /**
     * Calculate the month to display
     *
     * @return month number starting by 0
     */
    private int getMonthToSelect() {
        int monthId = mBudget.monthId;
        if (monthId == -1) {
            monthId = Calendar.getInstance().get(Calendar.MONTH);
            WalletRepository budgetRepository = new WalletRepository(getActivity());
            int lastMonthId = budgetRepository.getLastBudgetMonth();
            while (!isNextMonth(monthId, lastMonthId)) {
                if (monthId == 11) {
                    monthId = 0;
                } else {
                    monthId++;
                }
            }
        }
        return monthId;
    }

    public boolean isNextMonth(int currentMonth, int lastMonth) {
        boolean result = false;

        if (currentMonth > lastMonth) result = true;
        else {
            if (currentMonth == 0) {
                if (lastMonth == 11) {
                    result = true;
                }
            }
        }
        return result;
    }

    /**
     * Validate the input data
     *
     * @return true if they are valid
     */
    private boolean validate() {
        boolean valid = true;

        if (mValidationFieldAvailable.isEmpty()) {
            mValidationFieldAvailable.setError(R.string.error_text_budget_available_mandatory);
            valid = false;
        }

        if (mValidationFieldTarget.isEmpty()) {
            mValidationFieldTarget.setError(R.string.error_text_budget_target_mandatory);
            valid = false;
        }

        return valid;
    }

    /**
     * Listener for Month Spinner
     */
    public class MonthOnItemSelectedListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos,
                                   long id) {
            mBudget.monthId = pos;
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }


}
