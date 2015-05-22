package com.householdplanner.shoppingapp.listeners;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.householdplanner.shoppingapp.R;

/**
 * Created by JuanCarlos on 20/05/2015.
 */
public class RecyclerViewClickListener implements RecyclerView.OnItemTouchListener {

    private final static String LOG_TAG = RecyclerViewClickListener.class.getSimpleName();

    public interface RecyclerViewOnItemClickListener {
        public void onItemClick(View view, int position);

        public void onItemSecondaryActionClick(View view, int position);

        public void onItemLongClick(View view, int position);
    }

    private View mView; //View touched
    private int mPosition; //Position of the view inside the recycler view
    private RecyclerViewOnItemClickListener mListener; //Listener for item events
    private GestureDetector mGestureDetector;

    public RecyclerViewClickListener(Context context, final RecyclerViewOnItemClickListener mListener) {
        this.mListener = mListener;
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                View secondaryActionIcon = mView.findViewById(R.id.imageSecondaryActionIcon);
                if (secondaryActionIcon != null) {
                    //This is a workaround to get the icon coordinates
                    //because secondaryActionIcon is always the placed on the first raw, I don't know why
                    //If it worked as expected, we obtain the Rect using getHitRect(outRect)
                    int left = secondaryActionIcon.getLeft();
                    int right = secondaryActionIcon.getRight();
                    int top = mView.getTop();
                    int bottom = mView.getBottom();
                    Rect outRect = new Rect(left, top, right, bottom);
                    if (outRect.contains((int) e.getX(), (int) e.getY())) {
                        mListener.onItemSecondaryActionClick(secondaryActionIcon, mPosition);
                    } else {
                        mListener.onItemClick(mView, mPosition);
                    }
                } else {
                    mListener.onItemClick(mView, mPosition);
                }
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                mListener.onItemLongClick(mView, mPosition);
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent event) {
        mView = recyclerView.findChildViewUnder(event.getX(), event.getY());
        if ((mView != null) && (mListener != null)) {
            mPosition = recyclerView.getChildLayoutPosition(mView);
            return mGestureDetector.onTouchEvent(event);
        }
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
    }
}
