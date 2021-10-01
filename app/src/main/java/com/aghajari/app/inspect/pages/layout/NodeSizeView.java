package com.aghajari.app.inspect.pages.layout;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.aghajari.app.inspect.utils.DrawUtils;
import com.aghajari.app.inspect.node.LayoutSettings;
import com.aghajari.app.inspect.utils.Utils;
import com.aghajari.app.inspect.views.InspectView;

import static com.aghajari.app.inspect.utils.AppColors.*;

public class NodeSizeView extends View {

    private Paint paint;
    private Paint dividerPaint;
    private TextPaint textPaint;

    public NodeSizeView(Context context) {
        this(context, null);
    }

    public NodeSizeView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NodeSizeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(AREA_COLOR);

        dividerPaint = new Paint();
        dividerPaint.setStyle(Paint.Style.FILL);
        dividerPaint.setStrokeWidth(Utils.dp(2));

        textPaint = new TextPaint();
        textPaint.setColor(Color.DKGRAY);
        textPaint.setTextSize(Utils.dp(16));

    }

    private final Rect area = new Rect();
    private final Rect shadow = new Rect();

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final InspectView.AccessibilityNodeInfoData data = LayoutSettings.getInstance().data;
        final InspectView view = LayoutSettings.getInstance().view;
        if (data == null || view == null)
            return;

        int size = getMeasuredWidth() / 2;
        int height = Utils.dp(124);
        int dividerSize = Utils.dp(48) / 2;
        dividerSize *= 1.5f;
        dividerSize -= Utils.dp(4);

        // parent layout
        int top = Utils.dp(44);
        int centerX = getMeasuredWidth() / 2;
        int centerY = height / 2 + top;
        int left = centerX - size / 2;
        int right = centerX + size / 2;
        int bottom = top + height;

        area.set(left + dividerSize,
                top + dividerSize,
                right - dividerSize,
                bottom - dividerSize);

        // Shadows
        dividerPaint.setColor(LIGHT_COLOR);

        shadow.set(centerX - dividerSize,
                top,
                centerX + dividerSize,
                top + dividerSize / 2);
        prepareShadow(0, shadow.top, 0, shadow.bottom);
        canvas.drawRect(shadow, dividerPaint);


        shadow.set(centerX - dividerSize,
                bottom - dividerSize / 2,
                centerX + dividerSize,
                bottom);
        prepareShadow(0, shadow.bottom, 0, shadow.top);
        canvas.drawRect(shadow, dividerPaint);

        shadow.set(left,
                centerY - dividerSize,
                left + dividerSize / 2,
                centerY + dividerSize);
        prepareShadow(shadow.left, 0, shadow.right, 0);
        canvas.drawRect(shadow, dividerPaint);

        shadow.set(right - dividerSize / 2,
                centerY - dividerSize,
                right,
                centerY + dividerSize);
        prepareShadow(shadow.right, 0, shadow.left, 0);
        canvas.drawRect(shadow, dividerPaint);

        // Dividers
        dividerPaint.setColor(PRIMARY_COLOR);
        dividerPaint.setShader(null);
        canvas.drawLine(centerX - dividerSize, top, centerX + dividerSize, top, dividerPaint);
        canvas.drawLine(centerX - dividerSize, bottom, centerX + dividerSize, bottom, dividerPaint);
        canvas.drawLine(left, centerY - dividerSize, left, centerY + dividerSize, dividerPaint);
        canvas.drawLine(right, centerY - dividerSize, right, centerY + dividerSize, dividerPaint);


        paint.setStrokeWidth(0);
        paint.setAlpha(60);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(area, paint);

        paint.setStrokeWidth(Utils.dp(2));
        paint.setStyle(Paint.Style.STROKE);
        paint.setAlpha(255);
        canvas.drawRect(area, paint);

        paint.setStyle(Paint.Style.FILL);

        DrawUtils.drawArrow(paint, canvas, area.left, area.centerY(), left + dividerPaint.getStrokeWidth(), area.centerY(), 60, 25);
        DrawUtils.drawArrow(paint, canvas, area.right, area.centerY(), right - dividerPaint.getStrokeWidth(), area.centerY(), 60, 25);
        DrawUtils.drawArrow(paint, canvas, area.centerX(), area.top, area.centerX(), top + dividerPaint.getStrokeWidth(), 60, 25);
        DrawUtils.drawArrow(paint, canvas, area.centerX(), area.bottom, area.centerX(), bottom - dividerPaint.getStrokeWidth(), 60, 25);

        // SIZE
        DrawUtils.drawText(
                read(data.rect.width(), data.rect.height()),
                area.centerX(),
                area.centerY(),
                textPaint, null, canvas);

        // TOP
        DrawUtils.drawText(
                read(data.rect.top - Utils.getDevicePadding()),
                area.centerX(),
                top - Utils.dp(16),
                textPaint, null, canvas);

        // BOTTOM
        DrawUtils.drawText(
                read(view.getMeasuredHeight() - data.rect.bottom),
                area.centerX(),
                bottom + Utils.dp(16),
                textPaint, null, canvas);

        // LEFT
        DrawUtils.drawLeftText(
                read(data.rect.left),
                left - Utils.dp(8),
                area.centerY(),
                textPaint, canvas);

        // RIGHT
        DrawUtils.drawRightText(
                read(view.getMeasuredWidth() - data.rect.right),
                right + Utils.dp(8),
                area.centerY(),
                textPaint, canvas);
    }

    private CharSequence read(float w, float h) {
        return LayoutSettings.getInstance().unit.getText((int) w, (int) h, Color.BLACK, Color.GRAY);
    }

    private CharSequence read(float size) {
        return LayoutSettings.getInstance().unit.getText((int) size, Color.BLACK, Color.GRAY);
    }

    private void prepareShadow(float x0, float y0, float x1, float y1) {
        dividerPaint.setShader(new LinearGradient(x0, y0, x1, y1,
                new int[]{LIGHT_COLOR, Color.TRANSPARENT, Color.TRANSPARENT}
                , null, Shader.TileMode.CLAMP));
    }
}
