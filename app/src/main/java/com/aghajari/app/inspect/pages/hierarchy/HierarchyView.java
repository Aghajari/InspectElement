package com.aghajari.app.inspect.pages.hierarchy;

import android.content.Context;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aghajari.app.inspect.node.LayoutSettings;
import com.aghajari.app.inspect.pages.ViewPager;
import com.aghajari.app.inspect.views.InspectView;

public class HierarchyView extends FrameLayout implements LayoutSettings.OnUpdatedListener {

    HierarchyRecyclerView recyclerView;

    public HierarchyView(@NonNull Context context) {
        super(context);

        initNewRecycler(LayoutSettings.getInstance().root);
    }

    private void initNewRecycler(AccessibilityNodeInfo root) {
        if (recyclerView != null) {
            recyclerView.removeAllViews();
            removeView(recyclerView);
        }

        recyclerView = new HierarchyRecyclerView(getContext(), root);
        addView(recyclerView);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        LayoutSettings.getInstance().registerListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        LayoutSettings.getInstance().unregisterListener(this);
    }

    @Override
    public void onUpdate(@Nullable InspectView.AccessibilityNodeInfoData data) {
        recyclerView.onUpdate(data);
    }

    @Override
    public void onSelected(@Nullable InspectView.AccessibilityNodeInfoData data) {
        recyclerView.onSelected(data);
    }

    @Override
    public void onContentUpdated(@Nullable AccessibilityNodeInfo info) {
        recyclerView.onContentUpdated(info);
    }
}
