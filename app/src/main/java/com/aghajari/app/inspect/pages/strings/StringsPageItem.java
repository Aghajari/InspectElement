package com.aghajari.app.inspect.pages.strings;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.aghajari.app.inspect.pages.PageItem;
import com.aghajari.app.inspect.node.LayoutSettings;

public class StringsPageItem implements PageItem {
    @Override
    public View createView(@NonNull ViewGroup container) {
        return new StringsView(container.getContext());
    }

    @Override
    public CharSequence getTitle() {
        return "Strings";
    }

    @Override
    public LayoutSettings.LayoutType getLayoutType() {
        return LayoutSettings.LayoutType.STRINGS;
    }

    @Override
    public boolean recyclable() {
        return true;
    }
}
