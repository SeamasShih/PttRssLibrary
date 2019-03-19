package com.seamas.pttrsslibrary;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.widget.ImageView;

public class FloatingImage extends android.support.v7.widget.AppCompatImageView {

    private final int width = Resources.getSystem().getDisplayMetrics().widthPixels / 6;
    private final int height = width;

    public FloatingImage(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        setMeasuredDimension(width, height);
    }

    public int getW() {
        return width;
    }

    public int getH() {
        return height;
    }
}
