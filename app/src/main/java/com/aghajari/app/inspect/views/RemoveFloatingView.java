package com.aghajari.app.inspect.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.aghajari.app.inspect.R;
import com.aghajari.app.inspect.utils.Utils;

public class RemoveFloatingView extends LinearLayout {
    private final static float MIN_ALPHA = 0f;
    private final static float MAX_ALPHA = 1f;

    final GradientDrawable drawable;
    boolean isInDanger = false;

    public RemoveFloatingView(Context context) {
        super(context);
        inflate(context, R.layout.remove_view, this);

        drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setCornerRadii(new float[]{0, 0, 0, 0, 0, 0, 0, 0});
        drawable.setAlpha(120);
        drawable.setColor(Color.BLACK);
        setBackground(drawable);
        setEnabled(false);
    }

    private final Rect rect = new Rect();

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        rect.left = ((InspectParentView) getParent()).getMeasuredWidth() / 2 - w / 2;
        rect.right = rect.left + w;
        rect.top = ((FrameLayout.LayoutParams) getLayoutParams()).topMargin - Utils.getDevicePadding();
        rect.bottom = rect.top + h;
    }

    private ValueAnimator valueAnimator;

    @Override
    public void setEnabled(final boolean enabled) {
        if (isEnabled() == enabled)
            return;
        super.setEnabled(enabled);

        if (valueAnimator != null)
            valueAnimator.end();

        isInDanger = false;
        drawable.setColor(Color.BLACK);

        setVisibility(VISIBLE);

        valueAnimator = ValueAnimator.ofFloat(0.2f, 1f);
        valueAnimator.setDuration(140);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float scale = Float.parseFloat(animation.getAnimatedValue().toString());
                setScaleX(scale);
                setScaleY(scale);

                float da = (MAX_ALPHA - MIN_ALPHA) * animation.getAnimatedFraction();
                setAlpha(MIN_ALPHA + da);

                invalidate();
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (enabled)
                    setVisibility(VISIBLE);
                else
                    setVisibility(GONE);
            }
        });

        if (enabled) {
            valueAnimator.start();
        } else {
            valueAnimator.reverse();
        }
    }

    public void setDangerAreaEnabled(boolean enabled) {
        if (isInDanger == enabled || !isEnabled())
            return;
        isInDanger = enabled;

        if (valueAnimator != null)
            valueAnimator.end();

        valueAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), Color.BLACK, Color.RED);
        valueAnimator.setDuration(120);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                drawable.setColor((int) animator.getAnimatedValue());
                invalidate();
            }
        });

        if (enabled)
            valueAnimator.start();
        else
            valueAnimator.reverse();
    }

    public void checkDangerArea(WindowManager.LayoutParams lp, View view) {
        if (!isEnabled())
            return;

        setDangerAreaEnabled(rect.contains(lp.x, lp.y) ||
                rect.contains(lp.x + view.getMeasuredWidth(), lp.y) ||
                rect.contains(lp.x + view.getMeasuredWidth(), lp.y + view.getMeasuredHeight()) ||
                rect.contains(lp.x, lp.y + view.getMeasuredHeight()));
    }

    public boolean isInDanger() {
        return isInDanger;
    }
}
