package com.hipsterbait.android.widgets;

import android.content.Context;

import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.Button;

import com.hipsterbait.android.R;
import com.hipsterbait.android.other.TypefaceManager;

public class HBButton extends Button {

    public HBButton(Context context) {
        super(context);
    }

    public HBButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        parseAttributes(context, attrs);
    }

    public HBButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        parseAttributes(context, attrs);
    }

    private void parseAttributes(Context context, AttributeSet attributeSet) {
        TypedArray values = context.obtainStyledAttributes(attributeSet, R.styleable.HBButton);

        int typeface = values.getInt(R.styleable.HBButton_typeface, 0);

        TypefaceManager manager = TypefaceManager.getInstance();

        setTypeface(manager.getTypeface(context, typeface));

        values.recycle();
    }
}
