package com.hipsterbait.android.other;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import com.hipsterbait.android.R;

public class ProgressCircleMenu extends View {
    private static final int START_ANGLE_POINT = -90;

    private final Paint paint;
    private final RectF rect;

    private float angle;

    public ProgressCircleMenu(Context context, AttributeSet attrs) {
        super(context, attrs);

        final float scale = getResources().getDisplayMetrics().density;
        final float strokeWidth = 4 * scale;

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
        paint.setColor(ContextCompat.getColor(context, R.color.hbBlue));

        int pixelSize = (int) (44 * scale + 0.5f);

        rect = new RectF(0, 0, pixelSize, pixelSize);

        angle = 0;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawArc(rect, START_ANGLE_POINT, angle, false, paint);
    }

    public void setAngle(float progress) {
        this.angle = 360 * progress;
        this.invalidate();
    }
}
