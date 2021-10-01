package com.aghajari.app.inspect.pages.element;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.aghajari.app.inspect.node.LayoutSettings;
import com.aghajari.app.inspect.pages.PageItem;

public class ElementPageItem implements PageItem {
    @Override
    public View createView(@NonNull ViewGroup container) {
        return new ElementView(container.getContext());
    }

    @Override
    public CharSequence getTitle() {
        return "Element";
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
