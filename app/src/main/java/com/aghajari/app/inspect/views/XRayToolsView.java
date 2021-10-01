package com.aghajari.app.inspect.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.widget.ImageViewCompat;

import com.aghajari.app.inspect.R;
import com.aghajari.app.inspect.pages.TabLayout;
import com.aghajari.app.inspect.pages.ViewPager;
import com.aghajari.app.inspect.utils.Utils;

import static android.graphics.drawable.GradientDrawable.Orientation.LEFT_RIGHT;
import static android.graphics.drawable.GradientDrawable.Orientation.RIGHT_LEFT;
import static com.aghajari.app.inspect.utils.AppColors.*;
import static com.aghajari.app.inspect.views.FloatingView.CLICK_THRESHOLD;

public class XRayToolsView extends FrameLayout {

    private final int MIN_HEIGHT;
    private final int MAX_HEIGHT;

    private int toolbarHeight;
    FrameLayout toolbar;
    FrameLayout circleColorView;
    private final GradientDrawable backgroundDrawable;
    ViewPager viewPager;

    public XRayToolsView(@NonNull Context context) {
        super(context);
        viewPager = new ViewPager(context);
        initToolbar();

        MIN_HEIGHT = Utils.dp(104) + toolbarHeight;
        MAX_HEIGHT = Math.max(MIN_HEIGHT * 2, Utils.getPreferredHeight(context));

        CustomLayoutParams lp = new CustomLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, MAX_HEIGHT);
        lp.minHeight = MIN_HEIGHT;
        lp.circleSize = Utils.dp(44);
        lp.gravity = Gravity.BOTTOM;
        setLayoutParams(lp);


        circleColorView = new FrameLayout(context);
        circleColorView.setBackgroundColor(PRIMARY_COLOR);
        circleColorView.setVisibility(GONE);

        ImageView atom = new ImageView(context);
        atom.setImageResource(R.drawable.atom);
        FrameLayout.LayoutParams atom_lp = new FrameLayout.LayoutParams(lp.circleSize, lp.circleSize);
        int padding = Utils.dp(8);
        atom.setPadding(padding, padding, padding, padding);
        atom.setColorFilter(Color.WHITE);
        circleColorView.addView(atom, atom_lp);

        addView(circleColorView, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        backgroundDrawable = new GradientDrawable();
        backgroundDrawable.setColor(BG_WHITE_COLOR);
        setBackground(backgroundDrawable);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setClipToOutline(true);
            setElevation(Utils.dp(8));
            circleColorView.setElevation(Utils.dp(8));
        }

        FrameLayout.LayoutParams lp_vp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        addView(viewPager, lp_vp);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        CustomLayoutParams lp = (CustomLayoutParams) getLayoutParams();
        FrameLayout.LayoutParams lp_vp = (LayoutParams) viewPager.getLayoutParams();
        if (lp.gravity == Gravity.TOP) {
            lp_vp.gravity = Gravity.BOTTOM;
            lp_vp.topMargin = 0;
            lp_vp.bottomMargin = toolbarHeight;
        } else {
            lp_vp.gravity = Gravity.TOP;
            lp_vp.topMargin = toolbarHeight;
            lp_vp.bottomMargin = 0;
        }
        if ((!lp.isCircle && !lp.isAnimating) || lp.gravity == Gravity.TOP)
            lp_vp.height = lp.usableHeight - toolbarHeight;
        else if (lp.gravity == Gravity.BOTTOM)
            lp_vp.height = MAX_HEIGHT - toolbarHeight;

        layoutChildren(left, top, right, bottom);
    }

    void layoutChildren(int left, int top, int right, int bottom) {
        CustomLayoutParams plp = (CustomLayoutParams) getLayoutParams();
        final int count = getChildCount();
        final int parentLeft = getPaddingLeft();
        final int parentRight = right - left - getPaddingRight();
        final int parentTop = getPaddingTop();
        final int parentBottom = bottom - top - getPaddingBottom();
        if (plp.isCircle && !plp.isAnimating && circleColorView != null) {
            circleColorView.layout(0, 0, getMeasuredWidth(), getMeasuredHeight());
            return;
        }

        if (!plp.isCircle && !plp.isAnimating && circleColorView != null)
            circleColorView.setVisibility(GONE);

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child instanceof ViewPager) {
                final FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) child.getLayoutParams();
                Utils.layoutFrameLayoutChild(this, child, parentLeft, parentTop, parentRight, parentBottom, child.getMeasuredWidth(), lp.height);
            } else {
                Utils.layoutFrameLayoutChild(this, child, parentLeft, parentTop, parentRight, parentBottom);
            }
        }


        // Update viewPager content height as fast as it's possible!
        // ignore requestLayout() improperly called
        if (!plp.isOpening && viewPager.getChildCount() > 0) {
            final View v = viewPager.getChildAt(0);
            if (v.getMeasuredHeight() != viewPager.getLayoutParams().height) {
                v.requestLayout();
            }
        }
    }

    public static void layoutSelf(XRayToolsView child, int parentLeft, int parentTop, int parentRight, int parentBottom) {
        final XRayToolsView.CustomLayoutParams lp = (XRayToolsView.CustomLayoutParams) child.getLayoutParams();
        int width = child.getMeasuredWidth();
        final int height = lp.height;
        int childLeft = lp.leftMargin;
        int childTop;

        final int verticalGravity = lp.gravity & Gravity.VERTICAL_GRAVITY_MASK;

        if (lp.y == 0) {
            if (verticalGravity == Gravity.BOTTOM) {
                childTop = parentBottom - height - lp.topMargin;
            } else {
                childTop = parentTop + lp.topMargin;
            }
            lp.y = childTop;
        }

        if (lp.x == 0)
            lp.x = childLeft;
        childLeft = lp.x;

        int b;
        if (verticalGravity == Gravity.TOP) {
            childTop = Math.max(0, lp.y - height);
            b = lp.y;
        } else {
            childTop = lp.y;
            b = Math.min(parentBottom, childTop + height);
        }

        if (!lp.isCircle && !lp.isAnimating) {
            if (childLeft < 0) childLeft = 0;
            if (childLeft > parentRight - lp.circleSize * 2) {
                childLeft = parentRight - lp.circleSize * 2;
                child.showAsCircle(true);
            }

            if (childTop < Utils.getDevicePadding())
                childTop = Utils.getDevicePadding();
            if (b - childTop < lp.minHeight) {
                if (verticalGravity == Gravity.TOP) {
                    childTop = Utils.getDevicePadding();
                    b = lp.minHeight + Utils.getDevicePadding();
                } else {
                    childTop = parentBottom - lp.minHeight;
                    b = parentBottom;
                }

                if (childLeft > parentRight / 2)
                    child.showAsCircle(true);
            }

            lp.showingHeight = b - childTop;
            if (!lp.isMoving) {
                if (verticalGravity == Gravity.TOP) {
                    childTop = Utils.getDevicePadding();
                    b = childTop + lp.showingHeight;
                    lp.y = b;
                } else {
                    b = parentBottom;
                    childTop = b - lp.showingHeight;
                    lp.y = childTop;
                }

                if (childLeft < Utils.dp(28))
                    childLeft = 0;

                lp.x = childLeft;
            }

            lp.showingHeight = b - childTop;
            lp.showingWidth = width;
        } else {
            width = lp.showingWidth;
            if (verticalGravity == Gravity.TOP) {
                b = lp.y;
                childTop = b - lp.showingHeight;
            } else {
                b = childTop + lp.showingHeight;
            }

            if (childTop < Utils.getDevicePadding())
                childTop = Utils.getDevicePadding();

            if (childLeft < 0) childLeft = 0;
            if (childLeft > parentRight - lp.circleSize + FloatingView.diffSize)
                childLeft = parentRight - lp.circleSize + FloatingView.diffSize;

            if (!lp.isAnimating) {
                if (verticalGravity == Gravity.TOP && b - childTop < lp.circleSize) {
                    childTop = Utils.getDevicePadding();
                    b = lp.circleSize + Utils.getDevicePadding();
                } else if (verticalGravity == Gravity.BOTTOM && b > parentBottom) {
                    childTop = parentBottom - lp.circleSize;
                    b = parentBottom;
                }
            }

            final int maxPossibleHeight;
            if (verticalGravity == Gravity.TOP) {
                maxPossibleHeight = b - Utils.getDevicePadding();
            } else {
                maxPossibleHeight = parentBottom - childTop;
            }

            if (childLeft < parentRight - lp.circleSize * 2 && maxPossibleHeight > lp.minHeight) {
                ((XRayToolsView) child).showAsCircle(false);
            } else if (!lp.isMoving && !lp.isAnimating) {
                childLeft = parentRight - lp.circleSize + FloatingView.diffSize;
                lp.x = childLeft;
            }

            if (lp.isAnimating) {
                if (verticalGravity == Gravity.TOP && b < Utils.getDevicePadding() + lp.circleSize) {
                    childTop = Utils.getDevicePadding();
                    b = Utils.getDevicePadding() + lp.circleSize;
                } else if (verticalGravity == Gravity.BOTTOM && parentBottom - childTop < lp.circleSize) {
                    childTop = parentBottom - lp.circleSize;
                }
            }

        }

        lp.x = childLeft;
        if (verticalGravity == Gravity.TOP) {
            lp.y = b;
        } else {
            lp.y = childTop;
        }
        lp.top = childTop;
        lp.parentBottom = parentBottom;
        lp.currentLeft = childLeft;
        lp.usableHeight = b - childTop;
        child.layout(childLeft, childTop, childLeft + width, b);
    }

    private void initToolbar() {
        toolbarHeight = Utils.dp(36);

        toolbar = new FrameLayout(getContext());
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, toolbarHeight);
        addView(toolbar, lp);

        FrameLayout.LayoutParams tab_lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                , ViewGroup.LayoutParams.MATCH_PARENT);
        tab_lp.setMargins(toolbarHeight, Utils.dp(4), toolbarHeight, Utils.dp(4));
        toolbar.addView(new TabLayout(viewPager), tab_lp);

        addShadow(true);
        addShadow(false);

        AppCompatImageView move = new AppCompatImageView(getContext());
        FrameLayout.LayoutParams move_lp = new FrameLayout.LayoutParams(toolbarHeight, toolbarHeight);

        move.setImageResource(R.drawable.move);
        int padding = Utils.dp(8);
        move.setPadding(padding, padding, padding, padding);
        ImageViewCompat.setImageTintList(move, ColorStateList.valueOf(Color.WHITE));
        toolbar.addView(move, move_lp);

        toolbar.setBackgroundColor(BAR_COLOR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.setElevation(Utils.dp(4));
        }
    }

    private void addShadow(boolean left) {
        View shadow = new View(getContext());
        GradientDrawable cd = cd = new GradientDrawable(left ? LEFT_RIGHT : RIGHT_LEFT,
                new int[]{BAR_COLOR, Color.TRANSPARENT});
        shadow.setBackground(cd);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(toolbarHeight, ViewGroup.LayoutParams.MATCH_PARENT);
        if (left) {
            lp.gravity = Gravity.START;
            lp.setMarginStart(toolbarHeight);
        } else {
            lp.gravity = Gravity.END;
            lp.setMarginEnd(toolbarHeight);
        }
        toolbar.addView(shadow, lp);
    }

    public void show() {
        if (getVisibility() == VISIBLE) return;

        if (((CustomLayoutParams) getLayoutParams()).isCircle) {
            backgroundDrawable.setCornerRadius(((CustomLayoutParams) getLayoutParams()).circleSize);
        } else {
            backgroundDrawable.setCornerRadius(0);
        }
        setVisibility(VISIBLE);

        /*if (fromTop) {
            ((LayoutParams) toolbar.getLayoutParams()).gravity = Gravity.BOTTOM;
            ((LayoutParams) getLayoutParams()).gravity = Gravity.TOP;
        } else {
            ((LayoutParams) toolbar.getLayoutParams()).gravity = Gravity.TOP;
            ((LayoutParams) getLayoutParams()).gravity = Gravity.BOTTOM;
        }*/

        anim(true);
    }

    public void hide() {
        if (getVisibility() != VISIBLE) return;
        anim(false);
    }

    private void anim(final boolean show) {
        ((CustomLayoutParams) getLayoutParams()).isOpening = true;

        int cx = ((View) getParent()).getMeasuredWidth() / 2;
        int cy;
        if (((LayoutParams) getLayoutParams()).gravity == Gravity.BOTTOM) {
            cy = (((View) getParent()).getMeasuredHeight() * 2 - ((LayoutParams) getLayoutParams()).height) / 2;
        } else {
            cy = 0;
        }

        int finalRadius = ((View) getParent()).getMeasuredHeight();

        final Animator animator;

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
                !((CustomLayoutParams) getLayoutParams()).isCircle) {
            if (show) {
                animator = ViewAnimationUtils.createCircularReveal(this, cx, cy, 0, finalRadius);
            } else {
                animator = ViewAnimationUtils.createCircularReveal(this, cx, cy, finalRadius, 0);
            }
            animator.setDuration(400);
            animator.start();
        } else {
            if (((CustomLayoutParams) getLayoutParams()).isCircle) {
                int circleSize = ((CustomLayoutParams) getLayoutParams()).circleSize;
                ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(this, "translationX", circleSize, 0);
                animator = objectAnimator;
                objectAnimator.setDuration(150);
                if (show) {
                    objectAnimator.setInterpolator(new OvershootInterpolator());
                    objectAnimator.start();
                } else {
                    objectAnimator.reverse();
                }
            } else {
                ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(this, "scale", 0, 1f);
                animator = objectAnimator;
                objectAnimator.setDuration(150);
                if (show)
                    objectAnimator.start();
                else
                    objectAnimator.reverse();
            }
        }

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (!show)
                    setVisibility(GONE);
                ((CustomLayoutParams) getLayoutParams()).isOpening = false;
            }
        });
    }

    public void setScale(float scale) {
        setScaleX(scale);
        setScaleY(scale);
    }

    private int initialX;
    private int initialY;
    private float initialTouchX;
    private float initialTouchY;
    private boolean isTouching = false;

    public boolean onLayoutTouch(MotionEvent event) {
        if (getVisibility() != VISIBLE) return false;
        CustomLayoutParams params = (CustomLayoutParams) getLayoutParams();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (event.getPointerCount() == 1 && canStartMoving(event)) {
                    //remember the initial position.
                    initialX = params.x;
                    initialY = params.y;

                    //get the touch location
                    initialTouchX = event.getRawX();
                    initialTouchY = event.getRawY();
                    params.isMoving = true;

                    isTouching = true;
                    Utils.vibrate(getContext());
                    return true;
                }
            case MotionEvent.ACTION_UP:
                if (isTouching) {

                    long duration = event.getEventTime() - event.getDownTime();
                    if (duration < CLICK_THRESHOLD) {

                    }

                    params.isMoving = false;
                    isTouching = false;
                    requestLayout();
                    return true;
                }
            case MotionEvent.ACTION_MOVE:
                if (isTouching) {
                    params.isMoving = true;

                    if (params.gravity == Gravity.TOP) {
                        if (params.y > params.parentBottom - params.minHeight * 1.5) {
                            initialY -= Utils.getDevicePadding();
                            params.gravity = Gravity.BOTTOM;
                            ((LayoutParams) toolbar.getLayoutParams()).gravity = Gravity.TOP;
                        }
                    } else {
                        if (params.y < (params.minHeight * 1.5) + Utils.getDevicePadding()) {
                            initialY += Utils.getDevicePadding();
                            params.gravity = Gravity.TOP;
                            ((LayoutParams) toolbar.getLayoutParams()).gravity = Gravity.BOTTOM;
                        }
                    }

                    //Calculate the X and Y coordinates of the view.
                    params.x = initialX + (int) (event.getRawX() - initialTouchX);
                    params.y = initialY + (int) (event.getRawY() - initialTouchY);

                    requestLayout();
                    return true;
                }
        }

        return false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        super.dispatchTouchEvent(ev);
        return true;
    }

    private boolean canStartMoving(MotionEvent event) {
        if (((CustomLayoutParams) getLayoutParams()).isCircle) {
            int PE = ((CustomLayoutParams) getLayoutParams()).circleSize / 2;

            return Utils.isInArea(((CustomLayoutParams) getLayoutParams()).x + PE,
                    ((CustomLayoutParams) getLayoutParams()).top + PE
                    , PE, event.getX(), event.getY());
        } else {
            return Utils.isInArea(((CustomLayoutParams) getLayoutParams()).x,
                    ((CustomLayoutParams) getLayoutParams()).y
                    , toolbarHeight, event.getX(), event.getY());
        }
    }

    ValueAnimator circleAnimator;
    private boolean isShowingAsCircle = false;

    public void showAsCircle(final boolean show) {
        if ((circleAnimator != null && circleAnimator.isRunning()) || isShowingAsCircle == show)
            return;

        Utils.vibrate(getContext());

        isShowingAsCircle = show;

        ((CustomLayoutParams) getLayoutParams()).isCircle = show;
        ((CustomLayoutParams) getLayoutParams()).isAnimating = true;
        requestLayout();

        final int showingHeight, showingWidth;
        if (show) {
            showingHeight = ((CustomLayoutParams) getLayoutParams()).showingHeight;
            showingWidth = ((CustomLayoutParams) getLayoutParams()).showingWidth;
            ((CustomLayoutParams) getLayoutParams()).saveState();
        } else {
            showingHeight = MAX_HEIGHT;
            showingWidth = ((CustomLayoutParams) getLayoutParams()).savedWidth;
        }

        circleColorView.setVisibility(VISIBLE);
        final int circleSize = ((CustomLayoutParams) getLayoutParams()).circleSize;

        circleAnimator = ValueAnimator.ofFloat(0f, 1f);
        circleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float d = (float) valueAnimator.getAnimatedValue();

                if (show) {
                    ((CustomLayoutParams) getLayoutParams()).showingHeight = showingHeight - (int) ((showingHeight - circleSize) * d);
                    ((CustomLayoutParams) getLayoutParams()).showingWidth = showingWidth - (int) ((showingWidth - circleSize) * d);

                    circleColorView.setAlpha(d);
                    backgroundDrawable.setCornerRadius(((CustomLayoutParams) getLayoutParams()).showingHeight * d);
                } else {
                    ((CustomLayoutParams) getLayoutParams()).showingHeight = circleSize + (int) ((showingHeight - circleSize) * d);
                    ((CustomLayoutParams) getLayoutParams()).showingWidth = circleSize + (int) ((showingWidth - circleSize) * d);

                    circleColorView.setAlpha(1 - d);
                    backgroundDrawable.setCornerRadius(((CustomLayoutParams) getLayoutParams()).showingHeight * (1 - d));
                }
                requestLayout();
            }
        });
        circleAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                ((CustomLayoutParams) getLayoutParams()).isAnimating = false;
                if (!show)
                    circleColorView.setVisibility(GONE);
            }
        });
        circleAnimator.setDuration(250);
        circleAnimator.start();
    }

    public static class CustomLayoutParams extends FrameLayout.LayoutParams {
        int x, y;
        int top;
        int parentBottom;
        int showingHeight;
        int showingWidth;
        int minHeight;
        int currentLeft;
        int circleSize;
        int usableHeight;

        int savedHeight;
        int savedWidth;

        boolean isMoving = false;
        boolean isCircle = false;
        boolean isAnimating = false;
        boolean isOpening = false;

        public void saveState() {
            savedWidth = showingWidth;
            savedHeight = showingHeight;
        }

        public CustomLayoutParams(int width, int height) {
            super(width, height);
        }
    }
}
