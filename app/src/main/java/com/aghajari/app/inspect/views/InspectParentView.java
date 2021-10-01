package com.aghajari.app.inspect.views;

import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.aghajari.app.inspect.AccessibilityService;
import com.aghajari.app.inspect.utils.Utils;

public class InspectParentView extends FrameLayout {

    final AccessibilityService service;
    WindowManager.LayoutParams params;

    final InspectView inspectView;
    final RemoveFloatingView removeFloatingView;
    final XRayToolsView toolsView;

    public InspectParentView(@NonNull AccessibilityService service) {
        super(service);
        this.service = service;
        initParams();

        inspectView = new InspectView(service);
        addView(inspectView);

        removeFloatingView = new RemoveFloatingView(service);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT
                , ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
        lp.topMargin = Utils.dp(32);
        removeFloatingView.setVisibility(GONE);
        addView(removeFloatingView, lp);

        toolsView = new XRayToolsView(service);
        toolsView.setVisibility(GONE);
        addView(toolsView);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        layoutChildren(left, top, right, bottom);
    }

    void layoutChildren(int left, int top, int right, int bottom) {
        final int count = getChildCount();
        final int parentLeft = getPaddingLeft();
        final int parentRight = right - left - getPaddingRight();
        final int parentTop = getPaddingTop();
        final int parentBottom = bottom - top - getPaddingBottom();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child instanceof XRayToolsView) {
                XRayToolsView.layoutSelf((XRayToolsView) child, parentLeft, parentTop, parentRight, parentBottom);
            } else if (child.getVisibility() != View.GONE) {
                Utils.layoutFrameLayoutChild(this, child, parentLeft, parentTop, parentRight, parentBottom);
            }
        }
    }

    protected void initParams() {
        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }
        //Add the view to the window.
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT);

        //Specify the view position
        params.gravity = Gravity.TOP | Gravity.LEFT;        //Initially view will be added to top-left corner
        params.x = 0;
        params.y = 0;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (PopupView.popupView != null) {
            if (!PopupView.popupView.isTouching(ev)) {
                PopupView.popupView.destroy();
                return true;
            } else {
                return super.dispatchTouchEvent(ev);
            }
        }
        return toolsView.onLayoutTouch(ev) || super.dispatchTouchEvent(ev);
    }

    public void setInspectEnabled(boolean enabled) {
        if (enabled) {
            params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH |
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        } else {
            params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;

            toolsView.hide();
        }

        inspectView.setInspectEnabled(enabled);

        if (isAttachedToWindow())
            service.mWindowManager.updateViewLayout(this, params);
    }


    public void show() {
        if (!isAttachedToWindow())
            service.mWindowManager.addView(this, params);
        invalidate();
    }

    public void hide() {
        if (isAttachedToWindow())
            service.mWindowManager.removeView(this);
    }

    public void showXRayTools() {
        toolsView.show();
    }
}
