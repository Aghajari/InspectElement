package com.aghajari.app.inspect.views;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.Nullable;

import com.aghajari.app.inspect.AccessibilityService;
import com.aghajari.app.inspect.node.NodeInfo;
import com.aghajari.app.inspect.utils.DrawUtils;
import com.aghajari.app.inspect.node.LayoutSettings;
import com.aghajari.app.inspect.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import static com.aghajari.app.inspect.utils.AppColors.*;
import static com.aghajari.app.inspect.views.FloatingView.CLICK_THRESHOLD;

public class InspectView extends View {

    private static final int MAX_SELECTED_ALPHA = 150;
    final AccessibilityService service;

    Paint paint;
    Paint selectedPaint;
    Paint gridPaint;
    Paint helperLinePaint;
    Paint cropPaint;
    TextPaint textPaint;
    Paint textBackgroundPaint;

    private boolean showScreenSize = true;

    @Nullable
    AccessibilityNodeInfoData selected = null;
    @Nullable
    AccessibilityNodeInfoData lastSelect = null;

    public InspectView(AccessibilityService service) {
        super(service);
        this.service = service;

        setEnabled(true);
        setClickable(true);

        float strokeSize = Utils.dp(0.5f);

        paint = new Paint();
        paint.setColor(Color.parseColor("#f8bbd0"));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeSize * 2);

        selectedPaint = new Paint();
        selectedPaint.setColor(AREA_COLOR);
        selectedPaint.setStrokeWidth(strokeSize * 4);

        helperLinePaint = new Paint();
        helperLinePaint.setColor(AREA_COLOR);
        helperLinePaint.setStrokeWidth(strokeSize);
        helperLinePaint.setStyle(Paint.Style.STROKE);
        helperLinePaint.setPathEffect(new DashPathEffect(new float[]{10, 10}, 0));

        cropPaint = new Paint();
        cropPaint.setColor(AREA_COLOR);
        cropPaint.setStrokeWidth(strokeSize * 2);
        cropPaint.setStyle(Paint.Style.STROKE);
        cropPaint.setPathEffect(new DashPathEffect(new float[]{8, 8}, 0));

        gridPaint = new Paint();
        gridPaint.setColor(LIGHT_COLOR);
        gridPaint.setAlpha(100);
        gridPaint.setStrokeWidth(Utils.dp(0.4f));

        textBackgroundPaint = new Paint();
        textBackgroundPaint.setColor(AREA_COLOR);
        textBackgroundPaint.setStrokeWidth(strokeSize * 4);

        textPaint = new TextPaint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(14 * 2 * strokeSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!isEnabled()) return;
        super.onDraw(canvas);

        if (LayoutSettings.getInstance().layoutType == LayoutSettings.LayoutType.LAYOUT) {
            if (LayoutSettings.getInstance().grid != LayoutSettings.GridMode.NONE) {
                int p = Utils.dp(LayoutSettings.getInstance().grid.w);
                for (int iX = p; iX < getMeasuredWidth(); iX += p) {
                    canvas.drawLine(iX, 0, iX, getMeasuredHeight(), gridPaint);
                }
                p = Utils.dp(LayoutSettings.getInstance().grid.h);
                for (int iY = 0; iY < getMeasuredHeight(); iY += p) {
                    canvas.drawLine(0, iY, getMeasuredWidth(), iY, gridPaint);
                }
            }
        }

        AccessibilityNodeInfo info = LayoutSettings.getInstance().root;
        draw(info, canvas);

        int selectedPaintColor = selectedPaint.getColor();

        if (lastSelect != null && lastSelect.alpha > 0) {
            selectedPaint.setStyle(Paint.Style.FILL);
            selectedPaint.setAlpha(lastSelect.alpha);
            if (lastSelect.color != 0 &&
                    LayoutSettings.getInstance().layoutType == LayoutSettings.LayoutType.ELEMENT)
                selectedPaint.setColor(lastSelect.color);
            canvas.drawRect(lastSelect.rect, selectedPaint);
        }

        if (selected != null) {
            if (LayoutSettings.getInstance().layoutType == LayoutSettings.LayoutType.ELEMENT)
                selectedPaint.setColor(selected.color != 0 ? selected.color : selectedPaintColor);

            selectedPaint.setStyle(Paint.Style.FILL);
            selectedPaint.setAlpha(selected.alpha);
            canvas.drawRect(selected.rect, selectedPaint);
            selectedPaint.setStyle(Paint.Style.STROKE);
            selectedPaint.setAlpha(255 - MAX_SELECTED_ALPHA + selected.alpha);
            canvas.drawRect(selected.rect, selectedPaint);

            if (LayoutSettings.getInstance().layoutType == LayoutSettings.LayoutType.LAYOUT) {
                textBackgroundPaint.setColor(AREA_COLOR);
                selectedPaint.setStyle(Paint.Style.FILL);

                DrawUtils.drawArrow(selectedPaint, canvas, selected.rect.left, selected.rect.centerY(), 0, selected.rect.centerY());
                DrawUtils.drawText(read(selected.rect.left), selected.rect.left / 2f, selected.rect.centerY(), textPaint, textBackgroundPaint, getMeasuredWidth(), getMeasuredHeight(), canvas);

                DrawUtils.drawArrow(selectedPaint, canvas, selected.rect.right, selected.rect.centerY(), getMeasuredWidth(), selected.rect.centerY());
                DrawUtils.drawText(read(getMeasuredWidth() - selected.rect.right), selected.rect.right + (getMeasuredWidth() - selected.rect.right) / 2f, selected.rect.centerY(), textPaint, textBackgroundPaint, getMeasuredWidth(), getMeasuredHeight(), canvas);

                if (selected.rect.top > Utils.getDevicePadding()) {
                    DrawUtils.drawArrow(selectedPaint, canvas, selected.rect.centerX(), selected.rect.top, selected.rect.centerX(), Utils.getDevicePadding());
                    DrawUtils.drawText(read(selected.rect.top - Utils.getDevicePadding()), selected.rect.centerX(), selected.rect.top / 2f + Utils.getDevicePadding() / 2f, textPaint, textBackgroundPaint, getMeasuredWidth(), getMeasuredHeight(), canvas);
                }

                DrawUtils.drawArrow(selectedPaint, canvas, selected.rect.centerX(), selected.rect.bottom, selected.rect.centerX(), getMeasuredHeight());
                DrawUtils.drawText(read(getMeasuredHeight() - selected.rect.bottom), selected.rect.centerX(), selected.rect.bottom + (getMeasuredHeight() - selected.rect.bottom) / 2f, textPaint, textBackgroundPaint, getMeasuredWidth(), getMeasuredHeight(), canvas);

                canvas.drawLine(0, selected.rect.top, getMeasuredWidth(), selected.rect.top, helperLinePaint);
                canvas.drawLine(0, selected.rect.bottom, getMeasuredWidth(), selected.rect.bottom, helperLinePaint);
                canvas.drawLine(selected.rect.left, 0, selected.rect.left, getMeasuredHeight(), helperLinePaint);
                canvas.drawLine(selected.rect.right, 0, selected.rect.right, getMeasuredHeight(), helperLinePaint);

                if (LayoutSettings.getInstance().canCrop()) {
                    selectedPaint.setStyle(Paint.Style.STROKE);

                    canvas.drawCircle(selected.rect.left, selected.rect.top, Utils.dp(8), findCropPaint(TouchType.POINT_LEFT_TOP));
                    canvas.drawCircle(selected.rect.left, selected.rect.bottom, Utils.dp(8), findCropPaint(TouchType.POINT_LEFT_BOTTOM));
                    canvas.drawCircle(selected.rect.right, selected.rect.top, Utils.dp(8), findCropPaint(TouchType.POINT_RIGHT_TOP));
                    canvas.drawCircle(selected.rect.right, selected.rect.bottom, Utils.dp(8), findCropPaint(TouchType.POINT_RIGHT_BOTTOM));
                }

                DrawUtils.drawText(read(selected.rect.width(), selected.rect.height()), selected.rect.centerX(), selected.rect.centerY(), textPaint, textBackgroundPaint, canvas);
            }
        }

        if (showScreenSize && LayoutSettings.getInstance().layoutType == LayoutSettings.LayoutType.LAYOUT) {
            textBackgroundPaint.setColor(PRIMARY_COLOR);
            DrawUtils.drawText(read(getMeasuredWidth(), getMeasuredHeight()), getMeasuredWidth() / 2f, getMeasuredHeight() / 2f, textPaint, textBackgroundPaint, canvas);
        }

        selectedPaint.setColor(selectedPaintColor);
        LayoutSettings.getInstance().update(selected);
    }

    private Paint findCropPaint(TouchType touchType) {
        if (isMoving) return selectedPaint;
        if (!isCropping) return cropPaint;

        if (this.touchType == null || !this.touchType.contains(touchType)) {
            return cropPaint;
        } else {
            return selectedPaint;
        }
    }

    private CharSequence read(float w, float h) {
        return LayoutSettings.getInstance().unit.getText((int) w, (int) h, Color.WHITE, Color.LTGRAY);
    }

    private CharSequence read(float size) {
        return LayoutSettings.getInstance().unit.getText((int) size, Color.WHITE, Color.LTGRAY);
    }

    private final Rect rect = new Rect();

    private void draw(AccessibilityNodeInfo v, Canvas canvas) {
        if (v == null)
            return;

        if (selected == null || !v.equals(selected.getFinalInfo())) {
            if (LayoutSettings.getInstance().canDraw(v)) {
                Utils.getBoundsInScreen(v, rect);
                if (selected == null || !rect.equals(selected.rect))
                    canvas.drawRect(rect, paint);
            }
        }

        if (v.getChildCount() > 0) {
            for (int i = 0; i < v.getChildCount(); i++) {
                draw(v.getChild(i), canvas);
            }
        }
    }

    public void setInspectEnabled(boolean enabled) {
        showScreenSize = true;
        selected = null;
        setEnabled(enabled);
        invalidate();
    }

    private boolean isCropping;
    private boolean isMoving;
    private int initialX;
    private int initialY;
    private float initialTouchX;
    private float initialTouchY;

    private TouchType touchType;

    private enum TouchType {
        POINT_LEFT_TOP, POINT_LEFT_BOTTOM, POINT_RIGHT_TOP, POINT_RIGHT_BOTTOM,
        LINE_LEFT, LINE_TOP, LINE_RIGHT, LINE_BOTTOM;

        public TouchType reverse(boolean x, boolean y) {
            switch (this) {
                case LINE_LEFT:
                case LINE_TOP:
                case LINE_RIGHT:
                case LINE_BOTTOM:
                    return reverseLine(x);
                default:
                    return reversePoint(x, y);
            }
        }

        public TouchType reversePoint(boolean x, boolean y) {
            if (x && y) {
                switch (this) {
                    case POINT_LEFT_TOP:
                        return POINT_RIGHT_BOTTOM;
                    case POINT_LEFT_BOTTOM:
                        return POINT_RIGHT_TOP;
                    case POINT_RIGHT_TOP:
                        return POINT_LEFT_BOTTOM;
                    case POINT_RIGHT_BOTTOM:
                        return POINT_LEFT_TOP;
                }
            } else if (x) {
                switch (this) {
                    case POINT_LEFT_TOP:
                        return POINT_RIGHT_TOP;
                    case POINT_LEFT_BOTTOM:
                        return POINT_RIGHT_BOTTOM;
                    case POINT_RIGHT_TOP:
                        return POINT_LEFT_TOP;
                    case POINT_RIGHT_BOTTOM:
                        return POINT_LEFT_BOTTOM;
                }
            } else if (y) {
                switch (this) {
                    case POINT_LEFT_TOP:
                        return POINT_LEFT_BOTTOM;
                    case POINT_LEFT_BOTTOM:
                        return POINT_LEFT_TOP;
                    case POINT_RIGHT_TOP:
                        return POINT_RIGHT_BOTTOM;
                    case POINT_RIGHT_BOTTOM:
                        return POINT_RIGHT_TOP;
                }
            }
            return this;
        }

        public TouchType reverseLine(boolean x) {
            if (x) {
                switch (this) {
                    case LINE_LEFT:
                        return LINE_RIGHT;
                    case LINE_RIGHT:
                        return LINE_LEFT;
                }
            } else {
                switch (this) {
                    case LINE_TOP:
                        return LINE_BOTTOM;
                    case LINE_BOTTOM:
                        return LINE_TOP;
                }
            }
            return this;
        }

        public boolean contains(TouchType type) {
            if (this == type) return true;

            switch (type) {
                case POINT_LEFT_TOP:
                    return this == LINE_LEFT || this == LINE_TOP;
                case POINT_LEFT_BOTTOM:
                    return this == LINE_LEFT || this == LINE_BOTTOM;
                case POINT_RIGHT_TOP:
                    return this == LINE_RIGHT || this == LINE_TOP;
                case POINT_RIGHT_BOTTOM:
                    return this == LINE_RIGHT || this == LINE_BOTTOM;
                case LINE_LEFT:
                    return this == POINT_LEFT_BOTTOM || this == POINT_LEFT_TOP;
                case LINE_TOP:
                    return this == POINT_RIGHT_TOP || this == POINT_LEFT_TOP;
                case LINE_RIGHT:
                    return this == POINT_RIGHT_BOTTOM || this == POINT_RIGHT_TOP;
                case LINE_BOTTOM:
                    return this == POINT_LEFT_BOTTOM || this == POINT_RIGHT_BOTTOM;
            }
            return false;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (isAttachedToWindow() && isEnabled() && event.getAction() == MotionEvent.ACTION_UP) {
            long duration = event.getEventTime() - event.getDownTime();
            if (duration < CLICK_THRESHOLD) {

                if (showScreenSize &&
                        LayoutSettings.getInstance().layoutType == LayoutSettings.LayoutType.LAYOUT &&
                        Utils.isInArea(getMeasuredWidth() / 2f,
                                getMeasuredHeight() / 2f,
                                Utils.dp(8),
                                event.getX(), event.getY())) {
                    showScreenSize = false;
                } else {
                    lastSelect = selected;
                    selected = findSelectedInfo(event.getX(), event.getY());

                    LayoutSettings.getInstance().select(selected);

                    if ((selected != null || lastSelect != null) && lastSelect != selected) {
                        if (lastSelect == null || selected == null || !lastSelect.rect.equals(selected.rect)) {
                            startAlphaAnimation();
                        } else {
                            lastSelect.alpha = 0;
                            selected.alpha = MAX_SELECTED_ALPHA;
                        }
                    }

                    if (selected != null)
                        ((InspectParentView) getParent()).showXRayTools();
                }
                invalidate();
                return true;
            }
        }

        if (LayoutSettings.getInstance().canCrop() && selected != null) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (event.getPointerCount() == 1) {
                        float PE = Utils.dp(8f);

                        if (Utils.isInArea(selected.rect.left, selected.rect.top, PE, event.getX(), event.getY())) {
                            initialX = selected.rect.left;
                            initialY = selected.rect.top;
                            touchType = TouchType.POINT_LEFT_TOP;
                            isCropping = true;
                        } else if (Utils.isInArea(selected.rect.left, selected.rect.bottom, PE, event.getX(), event.getY())) {
                            initialX = selected.rect.left;
                            initialY = selected.rect.bottom;
                            touchType = TouchType.POINT_LEFT_BOTTOM;
                            isCropping = true;
                        } else if (Utils.isInArea(selected.rect.right, selected.rect.top, PE, event.getX(), event.getY())) {
                            initialX = selected.rect.right;
                            initialY = selected.rect.top;
                            touchType = TouchType.POINT_RIGHT_TOP;
                            isCropping = true;
                        } else if (Utils.isInArea(selected.rect.right, selected.rect.bottom, PE, event.getX(), event.getY())) {
                            initialX = selected.rect.right;
                            initialY = selected.rect.bottom;
                            touchType = TouchType.POINT_RIGHT_BOTTOM;
                            isCropping = true;
                        } else {
                            touchType = findTouchingLine(PE, event.getX(), event.getY());
                            isCropping = touchType != null;
                        }

                        if (isCropping) {
                            //get the touch location
                            initialTouchX = event.getRawX();
                            initialTouchY = event.getRawY();
                        } else {
                            isMoving = selected.rect.contains((int) event.getX(), (int) event.getY());
                            if (isMoving) {
                                initialX = selected.rect.centerX();
                                initialY = selected.rect.centerY();
                                initialTouchX = event.getRawX();
                                initialTouchY = event.getRawY();
                            }
                        }

                        if (isCropping || isMoving) {
                            return true;
                        }
                    }
                case MotionEvent.ACTION_UP:
                    if (isCropping || isMoving) {
                        isCropping = false;
                        isMoving = false;
                        invalidate();
                        return true;
                    }
                case MotionEvent.ACTION_MOVE:
                    if (isCropping) {
                        int x = initialX + (int) (event.getRawX() - initialTouchX);
                        int y = initialY + (int) (event.getRawY() - initialTouchY);

                        switch (touchType) {
                            case POINT_LEFT_TOP:
                                selected.rect.left = x;
                                selected.rect.top = y;
                                break;
                            case POINT_LEFT_BOTTOM:
                                selected.rect.left = x;
                                selected.rect.bottom = y;
                                break;
                            case POINT_RIGHT_TOP:
                                selected.rect.right = x;
                                selected.rect.top = y;
                                break;
                            case POINT_RIGHT_BOTTOM:
                                selected.rect.right = x;
                                selected.rect.bottom = y;
                                break;
                            case LINE_LEFT:
                                selected.rect.left = x;
                                break;
                            case LINE_TOP:
                                selected.rect.top = y;
                                break;
                            case LINE_RIGHT:
                                selected.rect.right = x;
                                break;
                            case LINE_BOTTOM:
                                selected.rect.bottom = y;
                                break;
                        }

                        selected.updated = true;
                        fixTouchType(event.getRawX(), event.getRawY(), (int) event.getX(), (int) event.getY());
                        invalidate();
                        return true;
                    } else if (isMoving) {
                        int x = initialX + (int) (event.getRawX() - initialTouchX);
                        int y = initialY + (int) (event.getRawY() - initialTouchY);
                        int w = selected.rect.width();
                        int h = selected.rect.height();
                        selected.rect.left = x - w / 2;
                        selected.rect.right = x + w / 2;
                        selected.rect.top = y - h / 2;
                        selected.rect.bottom = y + h / 2;
                        selected.updated = true;

                        invalidate();
                        return true;
                    }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    private TouchType findTouchingLine(float PE, float x, float y) {
        int l = selected.rect.left;
        int t = selected.rect.top;
        int r = selected.rect.right;
        int b = selected.rect.bottom;

        // LEFT & RIGHT
        if (t + PE < y && b - PE > y) {
            if (l - PE < x && l + PE > x) {
                initialX = l;
                return TouchType.LINE_LEFT;
            }
            if (r - PE < x && r + PE > x) {
                initialX = r;
                return TouchType.LINE_RIGHT;
            }
        }

        // TOP & BOTTOM
        if (l + PE < x && r - PE > x) {
            if (t - PE < y && t + PE > y) {
                initialY = t;
                return TouchType.LINE_TOP;
            }
            if (b - PE < y && b + PE > y) {
                initialY = b;
                return TouchType.LINE_BOTTOM;
            }
        }

        return null;
    }

    private void fixTouchType(float rawX, float rawY, int x, int y) {
        int l = selected.rect.left;
        int t = selected.rect.top;
        int r = selected.rect.right;
        int b = selected.rect.bottom;

        boolean updateX = l > r;
        boolean updateY = t > b;

        if (updateX) {
            selected.rect.left = r;
            selected.rect.right = l;
        }

        if (updateY) {
            selected.rect.top = b;
            selected.rect.bottom = t;
        }

        if (updateX || updateY) {
            touchType = touchType.reverse(updateX, updateY);
            initialTouchX = rawX;
            initialTouchY = rawY;
            initialX = x;
            initialY = y;
        }
    }

    @Nullable
    private AccessibilityNodeInfoData findSelectedInfo(float x, float y) {
        List<AccessibilityNodeInfoData> list = new ArrayList<>();
        find(LayoutSettings.getInstance().root, false, list, x, y);

        AccessibilityNodeInfoData selected = null;
        for (AccessibilityNodeInfoData v : list) {
            if (selected == null)
                selected = v;
            if (selected.rect.width() * selected.rect.height() > v.rect.width() * v.rect.height())
                selected = v;
        }

        if (selected != null && selected.equals(this.selected) && this.selected.getFinalInfo().getParent() != null) {
            selected.loadedInfo = this.selected.getFinalInfo().getParent();
            Utils.getBoundsInScreen(selected.getFinalInfo(), selected.rect);
        }
        return selected;
    }

    private void find(AccessibilityNodeInfo v, boolean check, List<AccessibilityNodeInfoData> list, float x, float y) {
        if (v == null) return;
        if (check && LayoutSettings.getInstance().canDraw(v)) {
            Rect rect = new Rect();
            Utils.getBoundsInScreen(v, rect);
            if ((x > rect.left && x < rect.right) && (y > rect.top && y < rect.bottom)) {
                list.add(new AccessibilityNodeInfoData(v, rect));
            }
        }

        if (v.getChildCount() > 0) {
            for (int i = 0; i < v.getChildCount(); i++) {
                find(v.getChild(i), true, list, x, y);
            }
        }
    }

    public void select(NodeInfo info) {
        lastSelect = selected;

        if (info == null) {
            selected = null;
        } else {
            Rect rect = new Rect();
            Utils.getBoundsInScreen(info.info, rect);
            selected = new AccessibilityNodeInfoData(info.info, rect);
        }
        LayoutSettings.getInstance().select(selected);

        if (selected != null || lastSelect != null)
            startAlphaAnimation();
        if (selected != null)
            ((InspectParentView) getParent()).showXRayTools();
    }

    public static class AccessibilityNodeInfoData {

        public AccessibilityNodeInfo info;
        public AccessibilityNodeInfo loadedInfo;
        public Rect rect;
        public boolean updated = false;
        public int alpha = 0;
        public int color = 0;

        private AccessibilityNodeInfoData(AccessibilityNodeInfo info, Rect rect) {
            this.info = info;
            this.rect = rect;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AccessibilityNodeInfoData that = (AccessibilityNodeInfoData) o;
            if (updated || that.updated) return false;
            return info != null ? info.equals(that.info) : that.info == null;
        }

        public AccessibilityNodeInfo getFinalInfo() {
            return loadedInfo != null ? loadedInfo : info;
        }
    }

    private ValueAnimator valueAnimator;

    private void startAlphaAnimation() {
        if (valueAnimator != null)
            valueAnimator.end();
        valueAnimator = ValueAnimator.ofInt(0, MAX_SELECTED_ALPHA);
        valueAnimator.setDuration(200);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if (selected != null) {
                    selected.alpha = (int) valueAnimator.getAnimatedValue();
                }
                if (lastSelect != null) {
                    lastSelect.alpha = MAX_SELECTED_ALPHA - (int) valueAnimator.getAnimatedValue();
                }
                invalidate();
            }
        });
        valueAnimator.start();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        LayoutSettings.getInstance().view = this;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        LayoutSettings.getInstance().view = null;
    }
}
