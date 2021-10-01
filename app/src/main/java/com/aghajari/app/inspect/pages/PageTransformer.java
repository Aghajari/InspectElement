package com.aghajari.app.inspect.pages;

import android.view.View;

import androidx.annotation.NonNull;

public class PageTransformer implements ViewPager.PageTransformer {

    final float SCALE_MAX = 0.8f;
    final float ALPHA_MAX = 0.5f;

    @Override
    public void transformPage(@NonNull View page, float position) {
        float scale = (position < 0)
                ? ((1 - SCALE_MAX) * position + 1)
                : ((SCALE_MAX - 1) * position + 1);
        float alpha = (position < 0)
                ? ((1 - ALPHA_MAX) * position + 1)
                : ((ALPHA_MAX - 1) * position + 1);
        if (position < 0) {
            page.setPivotX(page.getWidth());
            page.setPivotY(page.getHeight() / 2f);
        } else {
            page.setPivotX(0);
            page.setPivotY(page.getHeight() / 2f);
        }
        page.setScaleX(scale);
        page.setScaleY(scale);
        page.setAlpha(Math.abs(alpha));
    }
}