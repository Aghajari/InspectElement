package com.aghajari.app.inspect.pages;

import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.aghajari.app.inspect.R;
import com.ogaclejapan.smarttablayout.SmartTabLayout;

public class TabLayout extends FrameLayout {

    public TabLayout(ViewPager viewPager) {
        super(viewPager.getContext());
        inflate(viewPager.getContext(), R.layout.tab_layout,this);

        SmartTabLayout tabLayout = findViewById(R.id.viewpagertab);
        tabLayout.setViewPager(viewPager);

        tabLayout.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });

        viewPager.setCurrentItem(2, false);
    }

}
