package com.artyhacker.popularmovies.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * Created by dh on 16-12-14.
 */

public class UnScrollListView extends ListView {
    public UnScrollListView(Context context) {
        super(context);
    }

    public UnScrollListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public UnScrollListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
