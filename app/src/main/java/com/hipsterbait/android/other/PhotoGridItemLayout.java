package com.hipsterbait.android.other;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class PhotoGridItemLayout extends FrameLayout {
    public PhotoGridItemLayout(Context context) {
        super(context);
    }

    public PhotoGridItemLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PhotoGridItemLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec); // This is the key that will make the height equivalent to its width
    }
}
