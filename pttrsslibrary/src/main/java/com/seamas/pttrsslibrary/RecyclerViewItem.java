package com.seamas.pttrsslibrary;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;

public class RecyclerViewItem extends CardView {
    public RecyclerViewItem(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        setMeasuredDimension(getResources().getDisplayMetrics().widthPixels / 2 - 10, getResources().getDisplayMetrics().widthPixels / 2 - 10);
    }
}
