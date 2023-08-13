package com.diraapp.ui.components;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Аккуратно спизжено с ответов
 */
public class DrawingView extends View {

    private static final float TOUCH_TOLERANCE = 4;
    private final Path path;
    private final Paint bitmapPaint;
    private final Paint circlePaint;
    private final Path circlePath;
    private final int maxSize = 30;
    public int width;
    public int height;
    public PorterDuffXfermode clear = new PorterDuffXfermode(PorterDuff.Mode.DST_OUT);
    Context context;
    private Bitmap bitmap;
    private Canvas canvas;
    private Paint paint;
    private boolean isErasing = false;
    private int size = 20;
    private int color = Color.BLACK;
    private boolean isActive = false;
    private float mX, mY;

    public DrawingView(Context c) {
        super(c);
        context = c;
        path = new Path();
        bitmapPaint = new Paint(Paint.DITHER_FLAG);
        circlePaint = new Paint();
        circlePath = new Path();
        circlePaint.setAntiAlias(true);
        circlePaint.setColor(Color.BLUE);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeJoin(Paint.Join.MITER);
        circlePaint.setStrokeWidth(4f);
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(12);
    }

    public DrawingView(Context c, AttributeSet attributeSet) {
        super(c, attributeSet);
        context = c;
        path = new Path();
        bitmapPaint = new Paint(Paint.DITHER_FLAG);
        circlePaint = new Paint();
        circlePath = new Path();
        circlePaint.setAntiAlias(true);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeJoin(Paint.Join.MITER);
        circlePaint.setStrokeWidth(0f);
        setBrush();
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public void setEraser() {
        // The most important
        paint.setMaskFilter(null);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        isErasing = true;
    }

    public void setMaxSize(int size) {
        this.size = size;
    }

    public void setSizePercentage(double percent) {

        int newSize = (int) (maxSize * (1 + percent));
        System.out.println("new size " + newSize);
        this.size = newSize;
        setBrush();
    }

    public void setMinSize(int size) {
        this.size = size;
    }

    public void setColor(int color) {
        this.color = color;
        setBrush();
    }

    public void setBrush() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(size);
        isErasing = false;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!isActive) return;
        canvas.drawColor(Color.TRANSPARENT);
        canvas.drawBitmap(bitmap, 0, 0, bitmapPaint);
        if (!isErasing) {
            canvas.drawPath(path, paint);
        } else {

        }
    }

    private void onTouchStart(float x, float y) {
        if (!isActive) return;
        path.reset();
        path.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void onTouchMove(float x, float y) {
        if (!isActive) return;
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            path.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
            if (isErasing) {
                path.lineTo(mX, mY);
                canvas.drawPath(path, paint);
            }
            circlePath.reset();
            circlePath.addCircle(mX, mY, 30, Path.Direction.CW);
        }
    }

    private void onTouchUp() {
        if (!isActive) return;
        path.lineTo(mX, mY);
        circlePath.reset();
        // commit the path to our offscreen
        canvas.drawPath(path, paint);
        // kill this so we don't double draw
        path.reset();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                onTouchStart(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                onTouchMove(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                onTouchUp();
                invalidate();
                break;
        }
        return true;
    }
}
