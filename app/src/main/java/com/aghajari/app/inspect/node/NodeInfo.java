package com.aghajari.app.inspect.node;

import android.graphics.Color;
import android.os.Build;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.accessibility.AccessibilityNodeInfo;

import com.aghajari.app.inspect.utils.AppColors;

import static com.aghajari.app.inspect.utils.AppColors.AREA_COLOR;

public class NodeInfo {
    public final int selectorColor, bgSelectorColor, bgColor;
    public final int childIndex;
    public final AccessibilityNodeInfo info;
    public CharSequence text, text2;

    public NodeInfo(int position, AccessibilityNodeInfo info, int childIndex) {
        bgColor = AppColors.generate(position, 30, 160);
        selectorColor = AppColors.alpha(100, bgColor);
        bgSelectorColor = AppColors.alpha(20, bgColor);
        this.childIndex = childIndex;
        this.info = info;
    }


    public void findTexts(boolean selected) {
         getTitle();

        if (!TextUtils.isEmpty(info.getViewIdResourceName())) {
            String str = info.getViewIdResourceName();
            str = str.substring(str.indexOf(":id/") + 4);
            SpannableStringBuilder builder = new SpannableStringBuilder();
            builder.append("id/");
            builder.setSpan(new ForegroundColorSpan(selected ? Color.LTGRAY : AREA_COLOR), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.append(str);
            text2 = builder;
        } else {
            if (!TextUtils.isEmpty(info.getText())) {
                text2 = info.getText();
            } else if (!TextUtils.isEmpty(info.getContentDescription())) {
                SpannableStringBuilder builder = new SpannableStringBuilder();
                builder.append("{ ");
                builder.setSpan(new ForegroundColorSpan(selected ? Color.LTGRAY : AREA_COLOR), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                builder.append(info.getContentDescription());
                builder.append(" }");
                builder.setSpan(new ForegroundColorSpan(selected ? Color.LTGRAY : AREA_COLOR), builder.length() - 1, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                text2 = builder;
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && !TextUtils.isEmpty(info.getPaneTitle())) {
                SpannableStringBuilder builder = new SpannableStringBuilder();
                builder.append("(");
                builder.setSpan(new ForegroundColorSpan(selected ? Color.LTGRAY : AREA_COLOR), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                builder.append(info.getPaneTitle());
                builder.append(")");
                builder.setSpan(new ForegroundColorSpan(selected ? Color.LTGRAY : AREA_COLOR), builder.length() - 1, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                text2 = builder;
            } else {
                text2 = "";
            }
        }

        if (text2 == null)
            text2 = "";

        if (text2.length() > 0)
            text = text + ":";
    }

    public boolean isParentOfNode(AccessibilityNodeInfo node) {
        if (node == null)
            return false;

        if (node.equals(info))
            return true;

        AccessibilityNodeInfo n = node;
        while (n.getParent() != null) {
            n = n.getParent();
            if (n.equals(info))
                return true;
        }
        return false;
    }

    public boolean isSelected(AccessibilityNodeInfo node) {
        return info.equals(node);
    }

    public CharSequence getTitle(){
        if (info == null || info.getClassName() == null) return "Unknown";
        String c = info.getClassName().toString();
        this.text = c.substring(c.lastIndexOf(".") + 1);

        if (childIndex > 0)
            text = text + " (" + childIndex + ")";
        return text;
    }
}
