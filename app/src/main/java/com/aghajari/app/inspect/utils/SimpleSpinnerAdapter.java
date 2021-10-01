package com.aghajari.app.inspect.utils;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.aghajari.app.inspect.R;

public class SimpleSpinnerAdapter extends ArrayAdapter<String> {

    public SimpleSpinnerAdapter(Context context, String[] items) {
        super(context, android.R.layout.simple_spinner_item, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView view = (TextView) super.getView(position, convertView, parent);
        view.setTextColor(Color.WHITE);
        view.setPadding(0, 0, 0, 0);
        return view;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TextView view = (TextView) super.getView(position, convertView, parent);
        view.setTextColor(Color.WHITE);
        view.setBackground(ResourcesCompat.getDrawable(view.getResources(), R.drawable.spinner, null));
        view.setPadding(Utils.dp(8), Utils.dp(8), Utils.dp(8), Utils.dp(8));
        return view;
    }

}