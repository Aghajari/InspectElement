package com.aghajari.app.inspect.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

import static com.aghajari.app.inspect.Application.applicationContext;

public final class Utils {

    public static boolean isFullscreen = false;

    public static int dp(int value) {
        return (int) (applicationContext.getResources().getDisplayMetrics().density * value);
    }

    public static float dp(float value) {
        return applicationContext.getResources().getDisplayMetrics().density * value;
    }

    public static boolean isInArea(float x, float y, float PE, float targetX, float targetY) {
        return x - PE <= targetX && x + PE >= targetX && y - PE <= targetY && y + PE >= targetY;
    }

    public static int getDevicePadding() {
        //return getRealStatusBarHeight();
        return 0;
    }

    public static void getBoundsInScreen(AccessibilityNodeInfo info, Rect rect) {
        info.getBoundsInScreen(rect);
        rect.top -= Utils.getRealStatusBarHeight();
        rect.bottom -= Utils.getRealStatusBarHeight();
    }

    private static int getRealStatusBarHeight() {
        if (isFullscreen)
            return 0;

        int result = 0;
        int resourceId = applicationContext.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = applicationContext.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static void layoutFrameLayoutChild(FrameLayout parent, View child, int parentLeft, int parentTop, int parentRight, int parentBottom) {
        final int width = child.getMeasuredWidth();
        final int height = child.getMeasuredHeight();
        layoutFrameLayoutChild(parent, child, parentLeft, parentTop, parentRight, parentBottom, width, height);
    }

    public static void layoutFrameLayoutChild(FrameLayout parent, View child, int parentLeft, int parentTop, int parentRight, int parentBottom, int width, int height) {
        final FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) child.getLayoutParams();
        int childLeft;
        int childTop;
        int gravity = lp.gravity;
        if (gravity == -1) {
            gravity = Gravity.TOP | Gravity.START;
        }
        final int layoutDirection = parent.getLayoutDirection();
        final int absoluteGravity = Gravity.getAbsoluteGravity(gravity, layoutDirection);
        final int verticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;
        switch (absoluteGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
            case Gravity.CENTER_HORIZONTAL:
                childLeft = parentLeft + (parentRight - parentLeft - width) / 2 +
                        lp.leftMargin - lp.rightMargin;
                break;
            case Gravity.RIGHT:
                childLeft = parentRight - width - lp.rightMargin;
                break;
            case Gravity.LEFT:
            default:
                childLeft = parentLeft + lp.leftMargin;
        }
        switch (verticalGravity) {
            case Gravity.TOP:
                childTop = parentTop + lp.topMargin;
                break;
            case Gravity.CENTER_VERTICAL:
                childTop = parentTop + (parentBottom - parentTop - height) / 2 +
                        lp.topMargin - lp.bottomMargin;
                break;
            case Gravity.BOTTOM:
                childTop = parentBottom - height - lp.bottomMargin;
                break;
            default:
                childTop = parentTop + lp.topMargin;
        }
        child.layout(childLeft, childTop, childLeft + width, childTop + height);
    }

    public static void setRippleBackground(View view) {
        TypedValue outValue = new TypedValue();
        view.getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        view.setBackground(ResourcesCompat.getDrawable(view.getResources(), outValue.resourceId, null));
    }

    public static void vibrate(Context context) {
        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 150 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(150, 25));
        } else {
            v.vibrate(150);
        }
    }

    public static int getPreferredHeight(Context context){
        return context.getResources().getDisplayMetrics().heightPixels / 2 - dp(42);
    }
}
