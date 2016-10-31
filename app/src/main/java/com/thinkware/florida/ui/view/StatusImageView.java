package com.thinkware.florida.ui.view;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

/**
 * default/pressed상태의 이미지가 붙어있는 리소스를 사용하기 위한 ImageView클래스
 * Pressed상태에 따라
 * default는 image src의 왼쪽 반 pressed상태에는 오른쪽 반을 사용한다
 *
 * Created by Mihoe on 2016-09-05.
 */
public class StatusImageView extends ImageView {
    private boolean pressed = false;
    public StatusImageView(Context context) {
        super(context);
        init();
    }

    public StatusImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StatusImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        recomputeImgMatrix();
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected boolean setFrame(int l, int t, int r, int b) {
        recomputeImgMatrix();
        return super.setFrame(l, t, r, b);
    }

    private void init() {
        setScaleType(ScaleType.MATRIX);
        setClickable(true);

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    pressed = true;
                    break;
                    case MotionEvent.ACTION_UP:
                    pressed = false;
                    break;
                }
                recomputeImgMatrix();
                return false;
            }
        });
    }


    private void recomputeImgMatrix() {
        final Drawable drawable = getDrawable();
        if (drawable == null) {
            return;
        }

        final Matrix matrix = getImageMatrix();

        final int viewWidth = (getWidth() - getPaddingLeft() - getPaddingRight());
        final int viewHeight = getHeight() - getPaddingTop() - getPaddingBottom();
        final int drawableWidth = drawable.getIntrinsicWidth();
        final int drawableHeight = drawable.getIntrinsicHeight();

        RectF endRect = new RectF(drawableWidth / 2, 0, drawableWidth, drawableHeight);
        RectF startRect  = new RectF(0, 0, drawableWidth / 2, drawableHeight);

        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        matrix.setRectToRect(pressed || isSelected() || isFocused() ? endRect : startRect, viewRect, Matrix.ScaleToFit.FILL);

        setImageMatrix(matrix);
        invalidate();
    }

}
