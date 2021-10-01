package com.aghajari.app.inspect.utils;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import static com.aghajari.app.inspect.Application.applicationContext;

public final class DrawUtils {


    public static void drawText(CharSequence text, float x, float y, TextPaint paint, Paint backgroundPaint, int maxWidth, int maxHeight, Canvas canvas) {
        if (x <= 0 || y <= 0) return;

        Rect rect = new Rect();
        paint.getTextBounds(text.toString(), 0, text.length(), rect);

        float padding = applicationContext.getResources().getDisplayMetrics().density * 4;
        float left = rect.width() / 2f;
        float top = rect.height() / 2f;

        if (x - left - padding <= 0 || y - top - padding <= 0) return;
        if (x + left + padding >= maxWidth || y + top + padding >= maxHeight) return;

        int alpha = backgroundPaint.getAlpha();
        backgroundPaint.setAlpha(230);
        canvas.drawPath(getRoundedRect(x - left - padding, y - top - padding, x + left + padding, y + top + padding, Utils.dp(8f), Utils.dp(8f), false), backgroundPaint);
        backgroundPaint.setAlpha(alpha);

        canvas.save();
        canvas.translate(x - left, y - top * 1.25f);
        StaticLayout layout = new StaticLayout(text, paint,
                canvas.getWidth(), Layout.Alignment.ALIGN_NORMAL, 1, 0, false);
        layout.draw(canvas);
        canvas.restore();
    }

    public static void drawText(CharSequence text, float x, float y, TextPaint paint, Paint backgroundPaint, Canvas canvas) {
        Rect rect = new Rect();
        paint.getTextBounds(text.toString(), 0, text.length(), rect);

        float left = rect.width() / 2f;
        float top = rect.height() / 2f;

        if (backgroundPaint != null) {
            float padding = applicationContext.getResources().getDisplayMetrics().density * 4;
            int alpha = backgroundPaint.getAlpha();
            backgroundPaint.setAlpha(230);
            canvas.drawPath(getRoundedRect(x - left - padding, y - top - padding, x + left + padding, y + top + padding, Utils.dp(8f), Utils.dp(8f), false), backgroundPaint);
            backgroundPaint.setAlpha(alpha);
        }

        canvas.save();
        canvas.translate(x - left, y - top * 1.25f);
        StaticLayout layout = new StaticLayout(text, paint,
                canvas.getWidth(), Layout.Alignment.ALIGN_NORMAL, 1, 0, false);
        layout.draw(canvas);
        canvas.restore();
        //canvas.drawText(text, 0, text.length(), x - left, y + top / 1.5f, paint);
    }

    public static void drawLeftText(CharSequence text, float x, float y, TextPaint paint, Canvas canvas) {
        Rect rect = new Rect();
        paint.getTextBounds(text.toString(), 0, text.length(), rect);
        float left = rect.width();
        float top = rect.height() / 2f;

        canvas.save();
        canvas.translate(x - left, y - top * 1.25f);
        StaticLayout layout = new StaticLayout(text, paint,
                canvas.getWidth(), Layout.Alignment.ALIGN_NORMAL, 1, 0, false);
        layout.draw(canvas);
        canvas.restore();
        //canvas.drawText(text, 0, text.length(), x - left, y + top / 1.5f, paint);
    }

    public static void drawRightText(CharSequence text, float x, float y, TextPaint paint, Canvas canvas) {
        Rect rect = new Rect();
        paint.getTextBounds(text.toString(), 0, text.length(), rect);
        float top = rect.height() / 2f;

        canvas.save();
        canvas.translate(x, y - top * 1.25f);
        StaticLayout layout = new StaticLayout(text, paint,
                canvas.getWidth(), Layout.Alignment.ALIGN_NORMAL, 1, 0, false);
        layout.draw(canvas);
        canvas.restore();

        //canvas.drawText(text, 0, text.length(), x, y + top / 1.5f, paint);
    }

    public static Path getRoundedRect(float left, float top, float right, float bottom, float rx, float ry, boolean conformToOriginalPost) {
        Path path = new Path();
        if (rx < 0) rx = 0;
        if (ry < 0) ry = 0;
        float width = right - left;
        float height = bottom - top;
        if (rx > width / 2) rx = width / 2;
        if (ry > height / 2) ry = height / 2;
        float widthMinusCorners = (width - (2 * rx));
        float heightMinusCorners = (height - (2 * ry));

        path.moveTo(right, top + ry);
        path.rQuadTo(0, -ry, -rx, -ry);//top-right corner
        path.rLineTo(-widthMinusCorners, 0);
        path.rQuadTo(-rx, 0, -rx, ry); //top-left corner
        path.rLineTo(0, heightMinusCorners);

        if (conformToOriginalPost) {
            path.rLineTo(0, ry);
            path.rLineTo(width, 0);
            path.rLineTo(0, -ry);
        } else {
            path.rQuadTo(0, ry, rx, ry);//bottom-left corner
            path.rLineTo(widthMinusCorners, 0);
            path.rQuadTo(rx, 0, rx, -ry); //bottom-right corner
        }

        path.rLineTo(0, -heightMinusCorners);

        path.close();//Given close, last lineto can be removed.

        return path;
    }

    /**
     * Draw an arrow
     * change internal radius and angle to change appearance
     * - angle : angle in degrees of the arrows legs
     * - radius : length of the arrows legs
     *
     * @author Steven Roelants 2017
     */
    public static void drawArrow(Paint paint, Canvas canvas, float from_x, float from_y, float to_x, float to_y) {
        drawArrow(paint, canvas, from_x, from_y, to_x, to_y, 60, 30);
    }

    public static void drawArrow(Paint paint, Canvas canvas, float from_x, float from_y, float to_x, float to_y, float angle, float radius) {
        float min = applicationContext.getResources().getDisplayMetrics().density * 4;
        if ((Math.abs(from_x - to_x) < min && from_x != to_x) || (Math.abs(from_y - to_y) < min && from_y != to_y))
            return;
        if (from_x == to_x && from_y == to_y)
            return;

        float anglerad, lineangle;

        //some angle calculations
        anglerad = (float) (Math.PI * angle / 180.0f);
        lineangle = (float) (Math.atan2(to_y - from_y, to_x - from_x));

        //tha line
        canvas.drawLine(from_x, from_y, to_x, to_y, paint);

        //tha triangle
        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        path.moveTo(to_x, to_y);
        path.lineTo((float) (to_x - radius * Math.cos(lineangle - (anglerad / 2.0))),
                (float) (to_y - radius * Math.sin(lineangle - (anglerad / 2.0))));
        path.lineTo((float) (to_x - radius * Math.cos(lineangle + (anglerad / 2.0))),
                (float) (to_y - radius * Math.sin(lineangle + (anglerad / 2.0))));
        path.close();

        canvas.drawPath(path, paint);
    }
}
