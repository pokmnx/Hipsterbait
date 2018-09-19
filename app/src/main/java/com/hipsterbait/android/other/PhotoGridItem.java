package com.hipsterbait.android.other;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class PhotoGridItem extends ImageView {
    public PhotoGridItem(Context context) {
        super(context);
    }

    public PhotoGridItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PhotoGridItem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec); // This is the key that will make the height equivalent to its width
    }
}