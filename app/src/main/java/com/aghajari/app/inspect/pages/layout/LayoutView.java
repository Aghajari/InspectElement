package com.aghajari.app.inspect.pages.layout;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.content.res.ResourcesCompat;

import com.aghajari.app.inspect.R;
import com.aghajari.app.inspect.node.LayoutSettings;
import com.aghajari.app.inspect.utils.SimpleSpinnerAdapter;
import com.aghajari.app.inspect.views.InspectView;
import com.aghajari.app.inspect.views.PopupView;

public class LayoutView extends FrameLayout implements LayoutSettings.OnUpdatedListener {

    final TextView nodeID;
    final TextView nodeSize;
    final NodeSizeView nodeSizeView;

    public LayoutView(Context context) {
        super(context);
        inflate(context, R.layout.page_layout, this);

        final TextView units = findViewById(R.id.units_spinner);
        final String[] units_items = new String[]{"dp", "sp", "px"};
        PopupView.create(units,
                new SimpleSpinnerAdapter(context, units_items),
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        LayoutSettings.getInstance().unit = LayoutSettings.Unit.values()[i];
                        units.setText(units_items[i]);
                        LayoutSettings.getInstance().invalidate();
                        LayoutSettings.getInstance().forceUpdate();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                    }
                });
        units.setText(units_items[LayoutSettings.getInstance().unit.ordinal()]);

        final TextView grid = findViewById(R.id.grid_spinner);
        final String[] grid_items = new String[]{"none", "8 x 8", "16 x 16", "32 x 32"};
        PopupView.create(grid,
                new SimpleSpinnerAdapter(context, grid_items),
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        LayoutSettings.getInstance().grid = LayoutSettings.GridMode.values()[i];
                        grid.setText(grid_items[i]);
                        LayoutSettings.getInstance().invalidate();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                    }
                });
        grid.setText(grid_items[LayoutSettings.getInstance().grid.ordinal()]);

        TextView select = findViewById(R.id.select_spinner);
        PopupView.create(select,
                new CustomSpinnerAdapter(context,
                        new Item("View", "Tap on a given view to select it's borders"),
                        new Item("Crop", "Select custom area using draggable guides")),
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        LayoutSettings.getInstance().cropEnabled = i == 1;
                        LayoutSettings.getInstance().invalidate();
                        select.setText(LayoutSettings.getInstance().cropEnabled ? "Crop" : "View");
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                    }
                });

        select.setText(LayoutSettings.getInstance().cropEnabled ? "Crop" : "View");

        nodeID = findViewById(R.id.node_id);
        nodeSize = findViewById(R.id.node_size);
        nodeSizeView = findViewById(R.id.node_size_view);
    }

    @Override
    public void onUpdate(@Nullable InspectView.AccessibilityNodeInfoData data) {
        nodeSizeView.invalidate();

        if (data == null) {
            nodeID.setText("Tap on a screen element for inspection!");
            nodeSize.setText("");
        } else {
            if (data.updated) {
                nodeID.setText("Cropped Area");
            } else {
                if (!TextUtils.isEmpty(data.getFinalInfo().getViewIdResourceName())) {
                    String str = data.getFinalInfo().getViewIdResourceName();
                    str = str.substring(str.indexOf(":id/") + 4);
                    SpannableStringBuilder builder = new SpannableStringBuilder();
                    builder.append("id/");
                    builder.setSpan(new ForegroundColorSpan(Color.LTGRAY), 0, 3, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                    builder.append(str);
                    builder.setSpan(new ForegroundColorSpan(Color.WHITE), 3, builder.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                    nodeID.setText(builder);
                } else {
                    if (!TextUtils.isEmpty(data.getFinalInfo().getText())) {
                        nodeID.setText(data.getFinalInfo().getText());
                    } else if (!TextUtils.isEmpty(data.getFinalInfo().getContentDescription())) {
                        nodeID.setText(String.format("{%s}", data.getFinalInfo().getContentDescription()));
                    } else {
                        String str = data.getFinalInfo().getClassName().toString();
                        nodeID.setText(str.substring(str.lastIndexOf(".") + 1));
                    }
                }
            }
            nodeSize.setText(LayoutSettings.getInstance().unit.getText(data.rect.width(), data.rect.height(), Color.WHITE, Color.LTGRAY));
        }
    }

    @Override
    public void onContentUpdated(@Nullable AccessibilityNodeInfo info) {
    }

    @Override
    public void onSelected(@Nullable InspectView.AccessibilityNodeInfoData data) {
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        LayoutSettings.getInstance().registerListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        LayoutSettings.getInstance().unregisterListener(this);
    }

    private static class CustomSpinnerAdapter extends ArrayAdapter<Item> {
        LayoutInflater inflater;

        public CustomSpinnerAdapter(Context context, Item... list) {
            super(context, android.R.layout.simple_spinner_item, list);
            inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(android.R.layout.simple_spinner_item, parent, false);
            }
            Item item = getItem(position);
            TextView view = (TextView) convertView;
            view.setPadding(0, 0, 0, 0);
            view.setTextColor(Color.WHITE);
            view.setText(item.title);
            return convertView;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.spinner_layout, parent, false);
            }

            convertView.setBackground(ResourcesCompat.getDrawable(convertView.getResources(), R.drawable.spinner, null));

            Item item = getItem(position);
            TextView title = convertView.findViewById(R.id.title);
            title.setText(item.title);
            TextView desc = convertView.findViewById(R.id.desc);
            desc.setText(item.desc);
            return convertView;
        }
    }

    private static class Item {
        final String title;
        final String desc;

        Item(String title, String desc) {
            this.title = title;
            this.desc = desc;
        }
    }
}
