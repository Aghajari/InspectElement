package com.aghajari.app.inspect.pages;

import android.content.Context;

import androidx.annotation.NonNull;

import com.aghajari.app.inspect.node.LayoutSettings;

import static com.aghajari.app.inspect.utils.AppColors.BG_COLOR;

public class ViewPager extends androidx.viewpager.widget.ViewPager {

    public ViewPager(@NonNull Context context) {
        super(context);

        setBackgroundColor(BG_COLOR);
        setPageTransformer(false, new com.aghajari.app.inspect.pages.PageTransformer());
        setAdapter(new PagerAdapter());

        addOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                LayoutSettings.getInstance().layoutType = ((PagerAdapter) getAdapter()).getPageAt(position).getLayoutType();
                LayoutSettings.getInstance().invalidate();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }
}
