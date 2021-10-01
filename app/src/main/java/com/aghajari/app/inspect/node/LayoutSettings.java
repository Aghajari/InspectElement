package com.aghajari.app.inspect.node;

import android.os.Build;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.Nullable;

import com.aghajari.app.inspect.Application;
import com.aghajari.app.inspect.views.FloatingView;
import com.aghajari.app.inspect.views.InspectView;

import java.util.ArrayList;
import java.util.List;

import static com.aghajari.app.inspect.utils.AppColors.*;

public class LayoutSettings {

    @Nullable
    public InspectView.AccessibilityNodeInfoData data;
    public AccessibilityNodeInfo root;
    public InspectView view;
    public FloatingView floatingView;
    private final List<OnUpdatedListener> listeners = new ArrayList<>();
    public CharSequence packageName, className;
    public boolean enabled = false;

    public Unit unit = Unit.DP;
    public LayoutType layoutType = LayoutType.LAYOUT;
    public StringsType stringsType = StringsType.ALL;
    public GridMode grid = GridMode.NONE;

    public boolean cropEnabled = false;

    LayoutSettings() {
    }

    private static LayoutSettings instance;

    public static LayoutSettings getInstance() {
        if (instance == null)
            instance = new LayoutSettings();
        return instance;
    }

    public void registerListener(OnUpdatedListener listener) {
        listeners.add(listener);
    }

    public void unregisterListener(OnUpdatedListener listener) {
        listeners.remove(listener);
    }

    public void update(@Nullable InspectView.AccessibilityNodeInfoData data) {
        this.data = data;
        for (OnUpdatedListener listener : listeners)
            listener.onUpdate(data);
    }

    public void forceUpdate() {
        for (OnUpdatedListener listener : listeners)
            listener.onSelected(data);
    }

    public void select(@Nullable InspectView.AccessibilityNodeInfoData data) {
        if (data != null && data.getFinalInfo() != null && data.getFinalInfo().getPackageName() != null) {
            if (packageName == null || !packageName.equals(data.getFinalInfo().getPackageName())) {
                packageName = data.getFinalInfo().getPackageName();
                className = null;
                update(root);
            }
        }
        this.data = data;
        for (OnUpdatedListener listener : listeners)
            listener.onSelected(data);
    }

    public void select(@Nullable NodeInfo data) {
        if (view != null)
            view.select(data);
    }

    public void update(@Nullable AccessibilityNodeInfo root) {
        this.root = root;
        for (OnUpdatedListener listener : listeners)
            listener.onContentUpdated(root);
    }

    public interface OnUpdatedListener {
        void onUpdate(@Nullable InspectView.AccessibilityNodeInfoData data);

        void onSelected(@Nullable InspectView.AccessibilityNodeInfoData data);

        void onContentUpdated(@Nullable AccessibilityNodeInfo info);
    }

    public void invalidate() {
        if (view != null)
            view.invalidate();
    }

    public boolean canCrop() {
        return cropEnabled && layoutType == LayoutType.LAYOUT;
    }

    public boolean canDraw(@Nullable AccessibilityNodeInfo info) {
        if (info == null)
            return false;
        switch (layoutType) {
            case LAYOUT:
            case ELEMENT:
                return true;
            case STRINGS:
                switch (stringsType) {
                    case TEXT:
                        return !TextUtils.isEmpty(info.getText());
                    case CONTENT:
                        return !TextUtils.isEmpty(info.getContentDescription());
                    default:
                        return countOfTexts(info) > 0;
                }
        }
        return false;
    }

    public CharSequence getText(AccessibilityNodeInfo info) {
        if (info == null)
            return null;

        switch (stringsType) {
            case TEXT:
                return info.getText();
            case CONTENT:
                return info.getContentDescription();
            default:
                int count = countOfTexts(info);
                if (count == 0)
                    return null;
                boolean inputType = !(count == 1 && !TextUtils.isEmpty(info.getText()));

                SpannableStringBuilder builder = new SpannableStringBuilder();
                if (!TextUtils.isEmpty(info.getText())) {
                    if (inputType)
                        append(builder, "Text :");
                    builder.append(info.getText());
                }
                if (!TextUtils.isEmpty(info.getContentDescription())) {
                    if (inputType)
                        append(builder, "Content Description :");
                    builder.append(info.getContentDescription());
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    if (!TextUtils.isEmpty(info.getPaneTitle())) {
                        if (inputType)
                            append(builder, "Pane Title :");
                        builder.append(info.getPaneTitle());
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (!TextUtils.isEmpty(info.getHintText())) {
                        if (inputType)
                            append(builder, "Hint Text :");
                        builder.append(info.getHintText());
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    if (!TextUtils.isEmpty(info.getTooltipText())) {
                        if (inputType)
                            append(builder, "Tooltip Text :");
                        builder.append(info.getTooltipText());
                    }
                }
                return builder;
        }
    }

    private void append(SpannableStringBuilder builder, String text) {
        if (builder.length() > 0)
            builder.append("\n");
        builder.append(text);
        builder.setSpan(new ForegroundColorSpan(PRIMARY_COLOR), builder.length() - text.length(), builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append("\n");
    }

    private int countOfTexts(AccessibilityNodeInfo info) {
        if (info == null)
            return 0;
        int count = 0;

        if (!TextUtils.isEmpty(info.getText()))
            count++;

        if (!TextUtils.isEmpty(info.getContentDescription()))
            count++;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
                !TextUtils.isEmpty(info.getPaneTitle()))
            count++;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                !TextUtils.isEmpty(info.getHintText()))
            count++;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
                !TextUtils.isEmpty(info.getTooltipText()))
            count++;

        return count;
    }

    public enum LayoutType {
        LAYOUT,
        STRINGS,
        ELEMENT
    }

    public enum StringsType {
        ALL,
        TEXT,
        CONTENT
    }

    public enum GridMode {
        NONE(0, 0),
        GRID8x8(8, 8),
        GRID16x16(16, 16),
        GRID32x32(32, 32);

        public final int w;
        public final int h;

        GridMode(int w, int h) {
            this.w = w;
            this.h = h;
        }

    }

    public enum Unit {
        DP("dp") {
            @Override
            public float getDensity() {
                return Application.applicationContext.getResources().getDisplayMetrics().density;
            }
        },
        SP("sp") {
            @Override
            public float getDensity() {
                return Application.applicationContext.getResources().getDisplayMetrics().scaledDensity;
            }
        },
        PX("px");

        private final String type;

        Unit(String type) {
            this.type = type;
        }

        public float getDensity() {
            return 1;
        }

        public String getText(int size) {
            return (int) (size / getDensity()) + type;
        }

        public SpannableStringBuilder getText(int size, int color1, int color2) {
            String s = String.valueOf((int) (size / getDensity()));
            SpannableStringBuilder builder = new SpannableStringBuilder();
            builder.append(s);
            builder.setSpan(new ForegroundColorSpan(color1), 0, builder.length(), SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.append(type);
            builder.setSpan(new ForegroundColorSpan(color2), builder.length() - type.length(), builder.length(), SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
            return builder;
        }

        public String getText(int w, int h) {
            float d = getDensity();
            int w1 = (int) (w / d);
            int h1 = (int) (h / d);
            return w1 + "x" + h1 + type;
        }

        public SpannableStringBuilder getText(int w, int h, int color1, int color2) {
            float d = getDensity();
            String w1 = String.valueOf((int) (w / d));
            String h1 = String.valueOf((int) (h / d));
            SpannableStringBuilder builder = new SpannableStringBuilder();
            builder.append(w1);
            builder.setSpan(new ForegroundColorSpan(color1), 0, builder.length(), SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.append("x");
            builder.setSpan(new ForegroundColorSpan(color2), builder.length() - 1, builder.length(), SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.append(h1);
            builder.setSpan(new ForegroundColorSpan(color1), builder.length() - h1.length(), builder.length(), SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.append(type);
            builder.setSpan(new ForegroundColorSpan(color2), builder.length() - type.length(), builder.length(), SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
            return builder;
        }
    }
}
