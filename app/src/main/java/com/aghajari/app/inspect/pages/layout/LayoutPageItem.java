package com.aghajari.app.inspect.pages.layout;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.aghajari.app.inspect.pages.PageItem;
import com.aghajari.app.inspect.node.LayoutSettings;

public class LayoutPageItem implements PageItem {
    @Override
    public View createView(@NonNull ViewGroup container) {
        return new LayoutView(container.getContext());
    }

    @Override
    public CharSequence getTitle() {
        return "Layout";
    }

    @Override
    public LayoutSettings.LayoutType getLayoutType() {
        return LayoutSettings.LayoutType.LAYOUT;
    }

    @Override
    public boolean recyclable() {
        return true;
    }
}
