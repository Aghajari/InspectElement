package com.aghajari.app.inspect.pages.app;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.aghajari.app.inspect.node.LayoutSettings;
import com.aghajari.app.inspect.pages.PageItem;
import com.aghajari.app.inspect.pages.element.ElementView;

public class AppPageItem implements PageItem {
    @Override
    public View createView(@NonNull ViewGroup container) {
        return new AppView(container.getContext());
    }

    @Override
    public CharSequence getTitle() {
        return "App";
    }

    @Override
    public LayoutSettings.LayoutType getLayoutType() {
        return LayoutSettings.LayoutType.ELEMENT;
    }

    @Override
    public boolean recyclable() {
        return true;
    }
}
