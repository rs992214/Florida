package com.thinkware.florida.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by Mihoe on 2016-09-05.
 */
public class CropImageView extends ImageView {
    private int position = -1;

    public CropImageView(Context context) {
        super(context);
        init();
    }

    public CropImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CropImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CropImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setScaleType(ScaleType.MATRIX);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        setPosition(this.position);
    }

    @Override
    protected boolean setFrame(int l, int t, int r, int b) {
        setPosition(this.position);
        return super.setFrame(l, t, r, b);
    }

    public void setPosition(int position) {
        final Drawable drawable = getDrawable();
        if (drawable == null) {
            return;
        }

        this.position = position;

        final Matrix matrix = getImageMatrix();

        final int viewWidth = (getWidth() - getPaddingLeft() - getPaddingRight());
        final int viewHeight = getHeight() - getPaddingTop() - getPaddingBottom();
        final int drawableWidth = drawable.getIntrinsicWidth();
        final int drawableHeight = drawable.getIntrinsicHeight();

        int startX = position * viewWidth;
        int endX = (position + 1) * viewWidth;

        if (startX < 0) {
            startX = 0;
            endX = viewWidth;
        }
        if (endX > drawableWidth) {
            startX = drawableWidth - viewWidth;
            endX = drawableWidth;
        }

        RectF drawableRect = new RectF(startX, 0, endX, drawableHeight);
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        matrix.setRectToRect(drawableRect, viewRect, Matrix.ScaleToFit.FILL);
        setImageMatrix(matrix);

        invalidate();
    }

}
