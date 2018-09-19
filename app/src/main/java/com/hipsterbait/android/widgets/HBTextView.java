package com.hipsterbait.android.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import com.hipsterbait.android.R;
import com.hipsterbait.android.other.TypefaceManager;

public class HBTextView extends TextView {
    public HBTextView(Context context) {
        super(context);
    }

    public HBTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        parseAttributes(context, attrs);
    }

    public HBTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        parseAttributes(context, attrs);
    }

    private void parseAttributes(Context context, AttributeSet attributeSet) {
        TypedArray values = context.obtainStyledAttributes(attributeSet, R.styleable.HBTextView);

        int typeface = values.getInt(R.styleable.HBTextView_typeface, 0);

        TypefaceManager manager = TypefaceManager.getInstance();

        setTypeface(manager.getTypeface(context, typeface));

        values.recycle();
    }
}
