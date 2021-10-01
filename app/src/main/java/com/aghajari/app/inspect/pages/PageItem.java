package com.aghajari.app.inspect.pages;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.aghajari.app.inspect.node.LayoutSettings;

public interface PageItem {

    View createView(@NonNull ViewGroup container);

    CharSequence getTitle();

    LayoutSettings.LayoutType getLayoutType();

    boolean recyclable();
}
