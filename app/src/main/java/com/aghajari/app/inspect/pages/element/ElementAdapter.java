package com.aghajari.app.inspect.pages.element;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aghajari.app.inspect.R;
import com.aghajari.app.inspect.utils.AppColors;

import java.util.ArrayList;
import java.util.List;

public class ElementAdapter extends RecyclerView.Adapter<ElementAdapter.ViewHolder> {

    List<Element> elements;

    public ElementAdapter() {
        elements = new ArrayList<>();
    }

    public void createElements(Context context, AccessibilityNodeInfo info) {
        elements.clear();
        if (info != null)
            elements.addAll(Element.create(context, info));
        notifyDataSetChanged();
    }

    public void createElements(PackageInfo info, CharSequence name) {
        elements.clear();
        if (info != null)
            elements.addAll(Element.create(info, name));
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutRes = R.layout.element_item;
        if (viewType == 1)
            layoutRes = R.layout.element_header;
        else if (viewType == 3)
            layoutRes = R.layout.element_checkbox;
        else if (viewType == 4)
            layoutRes = R.layout.element_center_item;

        return new ElementAdapter.ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(layoutRes, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Element element = elements.get(position);
        holder.itemView.setBackgroundColor(element.bgColor);

        if (element instanceof Element.ElementHeader) {
            holder.tvKey.setText(((Element.ElementHeader) element).title);
        } else if (element instanceof Element.ElementKeyValue) {
            holder.tvKey.setText(((Element.ElementKeyValue) element).key);
            holder.tvValue.setText(((Element.ElementKeyValue) element).value);
        } else if (element instanceof Element.ElementKeyCheck) {
            holder.tvKey.setText(((Element.ElementKeyCheck) element).key);
            holder.setChecked(((Element.ElementKeyCheck) element).checked);
        } else if (element instanceof Element.ElementValue) {
            holder.tvValue.setText(((Element.ElementValue) element).value);
            holder.tvValue.setSelected(true);
        }
    }

    @Override
    public int getItemCount() {
        return elements.size();
    }

    @Override
    public int getItemViewType(int position) {
        Element element = elements.get(position);
        if (element instanceof Element.ElementHeader)
            return 1;
        else if (element instanceof Element.ElementKeyValue)
            return 2;
        else if (element instanceof Element.ElementKeyCheck)
            return 3;
        else if (element instanceof Element.ElementValue)
            return 4;

        return super.getItemViewType(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        final TextView tvKey;
        final TextView tvValue;
        final ImageView checkbox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvKey = itemView.findViewById(R.id.item_key);
            tvValue = itemView.findViewById(R.id.item_value);
            checkbox = itemView.findViewById(R.id.item_check);
        }

        public void setChecked(boolean checked) {
            if (checked) {
                checkbox.setImageResource(R.drawable.check);
                checkbox.setColorFilter(AppColors.PRIMARY_COLOR);
            } else {
                checkbox.setImageResource(R.drawable.uncheck);
                checkbox.setColorFilter(AppColors.AREA_COLOR);
            }
        }
    }
}
