package com.hipsterbait.android.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.AppCompatRadioButton;
import android.util.AttributeSet;
import android.widget.RadioButton;

import com.hipsterbait.android.R;
import com.hipsterbait.android.other.TypefaceManager;

public class HBRadioButton extends AppCompatRadioButton {

    public HBRadioButton(Context context) {
        super(context);
    }

    public HBRadioButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        parseAttributes(context, attrs);
    }

    public HBRadioButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        parseAttributes(context, attrs);
    }

    private void parseAttributes(Context context, AttributeSet attributeSet) {
        TypedArray values = context.obtainStyledAttributes(attributeSet, R.styleable.HBRadioButton);

        int typeface = values.getInt(R.styleable.HBRadioButton_typeface, 0);

        TypefaceManager manager = TypefaceManager.getInstance();

        setTypeface(manager.getTypeface(context, typeface));

        values.recycle();
    }
}
