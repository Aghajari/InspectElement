package com.aghajari.app.inspect.pages;

import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.collection.SparseArrayCompat;

import com.aghajari.app.inspect.pages.app.AppPageItem;
import com.aghajari.app.inspect.pages.element.ElementPageItem;
import com.aghajari.app.inspect.pages.hierarchy.HierarchyPageItem;
import com.aghajari.app.inspect.pages.layout.LayoutPageItem;
import com.aghajari.app.inspect.pages.strings.StringsPageItem;

import static com.aghajari.app.inspect.utils.AppColors.BG_WHITE_COLOR;

public class PagerAdapter extends androidx.viewpager.widget.PagerAdapter {

    private final List<PageItem> pages;
    private final SparseArrayCompat<WeakReference<View>> holder;

    public PagerAdapter() {
        pages = init();
        this.holder = new SparseArrayCompat<>(pages.size());
    }

    private static List<PageItem> init() {
        List<PageItem> items = new ArrayList<>();

        items.add(new AppPageItem());
        items.add(new StringsPageItem());
        items.add(new LayoutPageItem());
        items.add(new ElementPageItem());
        items.add(new HierarchyPageItem());
        return items;
    }

    @Override
    public int getCount() {
        return pages.size();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        final WeakReference<View> weakRefItem = holder.get(position);
        View view = (weakRefItem != null && getPageAt(position).recyclable()) ? weakRefItem.get() : null;

        if (view == null)
            view = pages.get(position).createView(container);

        view.setBackgroundColor(BG_WHITE_COLOR);
        container.addView(view);
        holder.put(position, new WeakReference<>(view));
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        holder.remove(position);
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return object == view;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return pages.get(position).getTitle();
    }

    public PageItem getPageAt(int position) {
        return pages.get(position);
    }
}
