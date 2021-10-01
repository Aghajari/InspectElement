package com.aghajari.app.inspect.views;

import android.animation.ValueAnimator;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.widget.ImageViewCompat;

import com.aghajari.app.inspect.AccessibilityService;
import com.aghajari.app.inspect.R;
import com.aghajari.app.inspect.node.LayoutSettings;
import com.aghajari.app.inspect.utils.Utils;

import static com.aghajari.app.inspect.utils.AppColors.*;

public class FloatingView extends FrameLayout {
    static final int CLICK_THRESHOLD = 200;
    static int diffSize;

    public final AccessibilityService service;
    WindowManager.LayoutParams params;

    private final AppCompatImageView image;
    private final InspectParentView inspectView;
    private GradientDrawable drawable;

    private final int realSize;
    private final static float MAX_SCALE = 1.25f;
    private final static float MIN_ALPHA = 0.4f;
    private final static float MAX_ALPHA = 0.8f;

    private int initialX;
    private int initialY;
    private float initialTouchX;
    private float initialTouchY;
    private boolean isTouching = false;
    private boolean enabled = false;

    private final DisplayMetrics displayMetrics = new DisplayMetrics();
    private final Handler handler = new Handler();

    public FloatingView(AccessibilityService service) {
        super(service);
        this.service = service;
        initParams();

        inspectView = new InspectParentView(service);
        inspectView.show();

        service.mWindowManager.getDefaultDisplay().getMetrics(displayMetrics);
        displayMetrics.heightPixels -= Utils.getDevicePadding();
        realSize = Utils.dp(42);
        diffSize = getSize() - realSize;

        //Specify the view position
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = displayMetrics.widthPixels - getSize() + diffSize;
        params.y = displayMetrics.heightPixels / 2;

        image = new AppCompatImageView(service);
        initImageLayout();
        updateUI();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setElevation(Utils.dp(2f));
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getSize(), getSize());
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (event.getPointerCount() == 1 && (translate2 == null || !translate2.isRunning())) {
                    //remember the initial position.
                    initialX = params.x;
                    initialY = params.y;

                    //get the touch location
                    initialTouchX = event.getRawX();
                    initialTouchY = event.getRawY();

                    isTouching = true;

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (isTouching)
                                animation(true);
                        }
                    }, CLICK_THRESHOLD);
                    return true;
                }
            case MotionEvent.ACTION_UP:
                if (!isTouching) return false;

                if (inspectView.removeFloatingView.isInDanger) {
                    service.destroy();
                    return true;
                }

                long duration = event.getEventTime() - event.getDownTime();
                if (duration < CLICK_THRESHOLD) {
                    enabled = !enabled;
                    updateUI();
                } else {

                    int maxX = displayMetrics.widthPixels - getMeasuredWidth() + diffSize;
                    int diffX = maxX - params.x;
                    if (params.x > diffX) {
                        animation(params.x, maxX);
                    } else {
                        animation(params.x, -diffSize);
                    }

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!isTouching)
                                animation(false);
                        }
                    }, (long) (CLICK_THRESHOLD * 1.5));
                }

                inspectView.removeFloatingView.setEnabled(false);
                isTouching = false;
                return true;
            case MotionEvent.ACTION_MOVE:
                if (!isTouching) return false;

                //Calculate the X and Y coordinates of the view.
                params.x = initialX + (int) (event.getRawX() - initialTouchX);
                params.y = initialY + (int) (event.getRawY() - initialTouchY);

                if (params.x + diffSize < 0)
                    params.x = -diffSize;
                if (right() > displayMetrics.widthPixels + diffSize)
                    params.x = displayMetrics.widthPixels - getMeasuredWidth() + diffSize;
                if (params.y < 0)
                    params.y = 0;
                if (bottom() > displayMetrics.heightPixels)
                    params.y = displayMetrics.heightPixels - getMeasuredHeight();

                //Update the layout with new X & Y coordinate
                service.mWindowManager.updateViewLayout(this, params);
                inspectView.removeFloatingView.checkDangerArea(params, this);

                return true;
        }
        return false;
    }

    public void updateUI() {
        if (enabled && isAttachedToWindow()) {
            drawable.setColor(PRIMARY_COLOR);
            inspectView.setInspectEnabled(true);
        } else {
            enabled = false;
            drawable.setColor(Color.BLACK);
            inspectView.setInspectEnabled(false);
        }
        image.setBackground(drawable);


        image.setScaleX(1f);
        image.setScaleY(1f);
        setAlpha(MIN_ALPHA);
    }

    private void initImageLayout() {
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(realSize, realSize);
        lp.gravity = Gravity.CENTER;
        addView(image, lp);

        drawable = new GradientDrawable();
        drawable.setSize(realSize, realSize);
        drawable.setCornerRadius(realSize);

        image.setImageResource(R.drawable.xray);
        int padding = Utils.dp(4);
        image.setPadding(padding, padding, padding, padding);
        ImageViewCompat.setImageTintList(image, ColorStateList.valueOf(Color.WHITE));

        setAlpha(MIN_ALPHA);
    }

    private int right() {
        return (int) (params.x + getMeasuredWidth());
    }

    private int bottom() {
        return (int) (params.y + getMeasuredHeight());
    }


    private ValueAnimator translate = null;
    private boolean lastAnimation = false;

    private void animation(final boolean touching) {
        if (lastAnimation == touching)
            return;
        if (translate != null)
            translate.cancel();

        lastAnimation = touching;

        float from, to;
        if (touching) {
            from = 1;
            to = MAX_SCALE;
        } else {
            from = MAX_SCALE;
            to = 1;
        }
        translate = ValueAnimator.ofFloat(from, to);
        translate.setDuration(100);
        translate.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float scale = Float.parseFloat(animation.getAnimatedValue().toString());
                image.setScaleX(scale);
                image.setScaleY(scale);

                float da = (MAX_ALPHA - MIN_ALPHA) * animation.getAnimatedFraction();
                if (touching)
                    setAlpha(MIN_ALPHA + da);
                else
                    setAlpha(MAX_ALPHA - da);
            }
        });
        translate.start();

        if (touching) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    inspectView.removeFloatingView.setEnabled(isTouching && touching);
                }
            }, CLICK_THRESHOLD);
        }
    }

    private ValueAnimator translate2 = null;

    private void animation(int fromX, int toX) {
        if (translate2 != null)
            translate2.cancel();

        translate2 = ValueAnimator.ofInt(fromX, toX);
        translate2.setDuration(150);
        translate2.setInterpolator(new OvershootInterpolator());
        translate2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                params.x = (int) animation.getAnimatedValue();
                service.mWindowManager.updateViewLayout(FloatingView.this, params);
            }
        });
        translate2.start();
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
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
    }

    public void show() {
        if (!isAttachedToWindow())
            service.mWindowManager.addView(this, params);
        invalidate();
    }

    public void hide() {
        if (isAttachedToWindow())
            service.mWindowManager.removeView(this);
        inspectView.hide();
    }

    protected int getSize() {
        return (int) (MAX_SCALE * realSize);
    }

    public void hideInspect() {
        if (enabled) {
            enabled = false;
            updateUI();
        }
    }

    public void destroy(){
        service.destroy();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        LayoutSettings.getInstance().floatingView = this;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        LayoutSettings.getInstance().floatingView = null;
    }

}
