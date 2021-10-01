package com.aghajari.app.inspect.pages.hierarchy;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.aghajari.app.inspect.pages.PageItem;
import com.aghajari.app.inspect.node.LayoutSettings;

public class HierarchyPageItem implements PageItem {
    @Override
    public View createView(@NonNull ViewGroup container) {
        return new HierarchyView(container.getContext());
    }

    @Override
    public CharSequence getTitle() {
        return "Hierarchy";
    }

    @Override
    public LayoutSettings.LayoutType getLayoutType() {
        return LayoutSettings.LayoutType.ELEMENT;
    }

    @Override
    public boolean recyclable() {
        return false;
    }
}
