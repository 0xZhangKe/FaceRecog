package com.zhangke.facerecog.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ZhangKe on 2017/12/7.
 */

public class RectView extends View {

    private List<Rect> rectList = new ArrayList<>();
    private Paint mPaint;

    private int mWidth = 0;
    private int mHeight = 0;

    private float scaleX = 0.0F;
    private float scaleY = 0.0F;

    private Point mPreviewPoint;

    private double epsilon = 0.00000001;

    public RectView(Context context) {
        super(context);
        init();
    }

    public RectView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RectView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setColor(Color.GREEN);
        mPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (rectList == null || rectList.isEmpty() || mPreviewPoint == null) return;
        if (mWidth == 0) {
            mWidth = getMeasuredWidth();
            mHeight = getMeasuredHeight();
        }
        if (Math.abs((double) scaleX - 0.0) < epsilon) {
            scaleX = mPreviewPoint.y / (float) mWidth;
            scaleY = mPreviewPoint.x / (float) mHeight;
        }

        canvas.drawColor(Color.TRANSPARENT);
        for (int i = 0; i < rectList.size(); i++) {
            Rect rect = rectList.get(i);
            int left = (int) ((mPreviewPoint.y - rect.bottom) / scaleX);
            int right = left + (int) (rect.width() / scaleX);
            int top = (int) (rect.left / scaleY);
            int bottom = (int) (rect.right / scaleY);
            canvas.drawLine(left, top, right, top, mPaint);
            canvas.drawLine(left, top, left, bottom, mPaint);
            canvas.drawLine(right, top, right, bottom, mPaint);
            canvas.drawLine(left, bottom, right, bottom, mPaint);
        }
    }

    public void drawRect(Rect rect, int color, int width, int height) {
        mPaint.setColor(color);
        mPreviewPoint = new Point(width, height);
        rectList.add(rect);
        invalidate();
    }

    public void drawRect(Rect rect, int width, int height) {
        mPreviewPoint = new Point(width, height);
        rectList.add(rect);
        invalidate();
    }

    public void clearRect() {
        if (!rectList.isEmpty()) {
            rectList.clear();
        }
        invalidate();
    }
}
