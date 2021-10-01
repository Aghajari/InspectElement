package com.aghajari.app.inspect.pages.element;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ServiceInfo;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.accessibility.AccessibilityNodeInfo;

import com.aghajari.app.inspect.node.LayoutSettings;
import com.aghajari.app.inspect.utils.AppColors;
import com.aghajari.app.inspect.utils.Utils;
import com.aghajari.app.inspect.views.InspectView;

import java.util.ArrayList;
import java.util.List;

import static com.aghajari.app.inspect.utils.AppColors.AREA_COLOR;

public class Element {

    public int bgColor;

    public Element() {
        bgColor = AppColors.BG_WHITE_COLOR;
    }

    public static class ElementHeader extends Element {
        public final String title;

        public ElementHeader(String title) {
            this.title = title;
        }
    }

    public static class ElementValue extends Element {
        public final CharSequence value;

        public ElementValue(CharSequence value) {
            this.value = value;
        }
    }

    public static class ElementKeyValue extends Element {
        public final CharSequence key, value;

        public ElementKeyValue(CharSequence key, CharSequence value) {
            this.key = key;
            this.value = value;
        }
    }

    public static class ElementKeyCheck extends Element {
        public final CharSequence key;
        public final boolean checked;

        public ElementKeyCheck(CharSequence key, boolean checked) {
            this.key = key;
            this.checked = checked;
        }
    }

    private static class Provider {

        List<Element> elements = new ArrayList<>();
        private int position;
        private final List<Element> c1 = new ArrayList<>();
        private final List<Element> c2 = new ArrayList<>();
        private boolean order = false;

        public void addHeader(String title) {
            position = 0;

            Element element = new ElementHeader(title);
            element.bgColor = AppColors.BG_COLOR;
            elements.add(element);
        }

        public void addKeyValue(CharSequence key, CharSequence value) {
            position++;

            Element element = new ElementKeyValue(key, value);
            if (position % 2 == 0)
                element.bgColor = AppColors.BG_WHITE_COLOR;
            else
                element.bgColor = AppColors.alpha(20, AppColors.PRIMARY_COLOR);

            elements.add(element);
        }

        public void addValue(CharSequence value) {
            position++;

            Element element = new ElementValue(value);
            if (position % 2 == 0)
                element.bgColor = AppColors.BG_WHITE_COLOR;
            else
                element.bgColor = AppColors.alpha(20, AppColors.PRIMARY_COLOR);

            elements.add(element);
        }

        public void order() {
            order = true;
            position = 0;
        }

        public void addKeyValue(CharSequence key, boolean checked) {
            Element element = new ElementKeyCheck(key, checked);
            if (order) {
                if (checked)
                    c1.add(element);
                else
                    c2.add(element);
            } else {
                position++;
                if (position % 2 == 0)
                    element.bgColor = AppColors.BG_WHITE_COLOR;
                else
                    element.bgColor = AppColors.alpha(20, AppColors.PRIMARY_COLOR);
                elements.add(element);
            }
        }

        public void build() {
            order = false;
            position = 0;

            int count = c1.size() + c2.size();
            for (int i = 0; i < count; i++) {
                Element element;
                if (i < c1.size())
                    element = c1.get(i);
                else
                    element = c2.get(i - c1.size());

                if ((i + 1) % 2 == 0)
                    element.bgColor = AppColors.BG_WHITE_COLOR;
                else
                    element.bgColor = AppColors.alpha(20, AppColors.PRIMARY_COLOR);
                elements.add(element);
            }
            c1.clear();
            c2.clear();
        }
    }

    public static List<Element> create(Context context, AccessibilityNodeInfo i) {
        Provider provider = new Provider();
        provider.addHeader("Runtime state");

        if (!TextUtils.isEmpty(i.getViewIdResourceName())) {
            String str = i.getViewIdResourceName();
            str = str.substring(str.indexOf(":id/") + 4);
            SpannableStringBuilder builder = new SpannableStringBuilder();
            builder.append("id/");
            builder.setSpan(new ForegroundColorSpan(AREA_COLOR), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.append(str);

            provider.addKeyValue("Id", builder);
        }

        if (!TextUtils.isEmpty(i.getPackageName()))
            provider.addKeyValue("Application package", i.getPackageName());

        if (!TextUtils.isEmpty(i.getClassName())) {
            provider.addKeyValue("Class name", className(i.getClassName().toString()));
        }

        if (!TextUtils.isEmpty(i.getText()))
            provider.addKeyValue("Text", i.getText());

        if (!TextUtils.isEmpty(i.getContentDescription()))
            provider.addKeyValue("Content desc.", i.getContentDescription());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (!TextUtils.isEmpty(i.getPaneTitle()))
                provider.addKeyValue("Pane title", i.getPaneTitle());

            if (!TextUtils.isEmpty(i.getTooltipText()))
                provider.addKeyValue("Tooltip text", i.getTooltipText());

        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!TextUtils.isEmpty(i.getHintText()))
                provider.addKeyValue("Hint text", i.getHintText());
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!TextUtils.isEmpty(i.getStateDescription()))
                provider.addKeyValue("State desc.", i.getStateDescription());
        }

        provider.addHeader("Layout");

        Rect rect = new Rect();
        Utils.getBoundsInScreen(i, rect);

        provider.addKeyValue("Width", read(rect.width()));
        provider.addKeyValue("Height", read(rect.height()));
        provider.addKeyValue("CenterX", read(rect.centerX()));
        provider.addKeyValue("CenterY", read(rect.centerY()));
        provider.addKeyValue("Left", read(rect.left));
        provider.addKeyValue("Top", read(rect.top));
        provider.addKeyValue("Right", read(rect.right));
        provider.addKeyValue("Bottom", read(rect.bottom));
        InspectView view = LayoutSettings.getInstance().view;
        if (view != null) {
            provider.addKeyValue("Right margin", read(view.getMeasuredWidth() - rect.right));
            provider.addKeyValue("Bottom margin", read(view.getMeasuredHeight() - rect.bottom));
        }

        provider.addHeader("Flags");
        provider.order();
        provider.addKeyValue("Enabled", i.isEnabled());
        provider.addKeyValue("Selected", i.isSelected());
        provider.addKeyValue("Focusable", i.isFocusable());
        provider.addKeyValue("Focused", i.isFocused());
        provider.addKeyValue("Checkable", i.isCheckable());
        provider.addKeyValue("Checked", i.isChecked());
        provider.addKeyValue("Scrollable", i.isScrollable());
        provider.addKeyValue("Clickable", i.isClickable());
        provider.addKeyValue("LongClickable", i.isLongClickable());
        provider.addKeyValue("Dismissable", i.isDismissable());
        provider.addKeyValue("Editable", i.isEditable());
        provider.addKeyValue("MultiLine", i.isMultiLine());
        provider.addKeyValue("Password", i.isPassword());
        provider.build();

        // TO-DO: match resources using viewID and context.getPackageManager().getResourcesForApplication().

        return provider.elements;
    }

    public static List<Element> create(PackageInfo info, CharSequence name) {
        Provider provider = new Provider();
        if (info == null)
            return provider.elements;

        provider.addKeyValue("Application name", name);
        provider.addKeyValue("Package name", info.packageName);
        provider.addKeyValue("Version name", info.versionName);
        provider.addKeyValue("Version code", String.valueOf(info.versionCode));

        if (!TextUtils.isEmpty(LayoutSettings.getInstance().className)) {
            provider.addValue(className(LayoutSettings.getInstance().className.toString()));
        }
        return provider.elements;
    }

    private static SpannableStringBuilder className(String str) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(str.substring(0, str.lastIndexOf(".") + 1));
        builder.setSpan(new ForegroundColorSpan(Color.GRAY), 0, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setSpan(new AbsoluteSizeSpan(Utils.dp(14)), 0, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(str.substring(str.lastIndexOf(".") + 1));
        return builder;
    }

    private static CharSequence read(float size) {
        return LayoutSettings.getInstance().unit.getText((int) size, Color.BLACK, Color.GRAY);
    }
}
