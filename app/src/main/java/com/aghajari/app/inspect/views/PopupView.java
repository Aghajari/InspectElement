package com.aghajari.app.inspect.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Rect;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.aghajari.app.inspect.node.LayoutSettings;
import com.aghajari.app.inspect.utils.AppColors;
import com.aghajari.app.inspect.utils.Utils;

// Well, looks like Spinner doesn't work on WindowManager on Android 11
// So, We're gonna handle it ourselves.
@SuppressWarnings("rawtypes")
public class PopupView extends LinearLayout {

    public static PopupView popupView;
    Rect rect;

    public PopupView(final View view, ArrayAdapter adapter, final AdapterView.OnItemSelectedListener listener) {
        super(view.getContext());
        if (popupView != null)
            popupView.destroy();

        popupView = this;
        setOrientation(VERTICAL);

        for (int i = 0; i < adapter.getCount(); i++) {
            final View v = adapter.getDropDownView(i, null, this);
            System.out.println(v);
            addView(v);
            final int finalI = i;
            v.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onItemSelected(null, v, finalI, 0);
                    destroy();
                }
            });
        }

        setBackgroundColor(AppColors.PRIMARY_COLOR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setElevation(Utils.dp(16));
        }

        InspectParentView parentView = (InspectParentView) LayoutSettings.getInstance().view.getParent();
        int[] l = new int[2];
        view.getLocationOnScreen(l);
        l[0] -= Utils.dp(8);
        l[1] -= Utils.dp(20);

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.leftMargin = l[0];
        lp.topMargin = l[1];
        parentView.addView(this, lp);

        rect = new Rect();
        rect.left = lp.leftMargin;
        rect.top = lp.topMargin;

        animation(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        rect.right = rect.left + w;
        rect.bottom = rect.top + h;
    }

    public void destroy() {
        animation(false);
        popupView = null;
    }

    public void animation(final boolean show) {
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(150);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float value = (float) valueAnimator.getAnimatedValue();
                setAlpha(value);
            }
        });
        if (!show) {
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    removeAllViews();
                    if (getParent() != null)
                        ((ViewGroup) getParent()).removeView(PopupView.this);
                }
            });
        }
        if (show)
            animator.start();
        else
            animator.end();
    }

    public boolean isTouching(MotionEvent event) {
        return event.getAction() == MotionEvent.ACTION_MOVE ||
                rect.contains((int) event.getX(), (int) event.getY());
    }

    public static void create(final View view, final ArrayAdapter adapter, final AdapterView.OnItemSelectedListener listener) {
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new PopupView(view, adapter, listener);
            }
        });
    }
}
