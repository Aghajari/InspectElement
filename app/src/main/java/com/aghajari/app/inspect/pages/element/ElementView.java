package com.aghajari.app.inspect.pages.element;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.recyclerview.widget.RecyclerView;

import com.aghajari.app.inspect.R;
import com.aghajari.app.inspect.node.HierarchyUtils;
import com.aghajari.app.inspect.node.LayoutSettings;
import com.aghajari.app.inspect.node.NodeInfo;
import com.aghajari.app.inspect.utils.AppColors;
import com.aghajari.app.inspect.utils.SimpleSpinnerAdapter;
import com.aghajari.app.inspect.views.InspectView;

public class ElementView extends FrameLayout implements LayoutSettings.OnUpdatedListener {

    final View nodeID_background;
    final TextView nodeID;
    final RecyclerView recyclerView;

    public ElementView(Context context) {
        super(context);
        inflate(context, R.layout.page_element, this);

        nodeID = findViewById(R.id.node_id);
        nodeID_background = findViewById(R.id.node_id_bg);
        recyclerView = findViewById(R.id.rv);
        recyclerView.setAdapter(new ElementAdapter());
        onSelected(LayoutSettings.getInstance().data);
    }

    @Override
    public void onSelected(@Nullable InspectView.AccessibilityNodeInfoData data) {
        if (data == null || data.getFinalInfo() == null) {
            nodeID_background.setBackgroundColor(AppColors.BG_COLOR);
            nodeID.setText("Tap on a screen element for inspection!");

            ((ElementAdapter) recyclerView.getAdapter()).createElements(getContext(), null);
        } else {
            NodeInfo info = HierarchyUtils.findNodeInfo(data.getFinalInfo(), LayoutSettings.getInstance().root);

            if (info == null) {
                nodeID_background.setBackgroundColor(AppColors.BG_COLOR);
                String str = data.getFinalInfo().getClassName().toString();
                nodeID.setText(str.substring(str.lastIndexOf(".") + 1));
            } else {
                nodeID_background.setBackgroundColor(info.bgColor);
                data.color = info.bgSelectorColor;

                info.findTexts(true);
                SpannableStringBuilder builder = new SpannableStringBuilder(info.text);
                if (info.text2.length() > 0) {
                    builder.append(" ");
                    builder.append(info.text2);
                }
                nodeID.setSelected(true);
                nodeID.setText(builder);
            }

            ((ElementAdapter) recyclerView.getAdapter()).createElements(getContext(), data.getFinalInfo());
        }
    }

    @Override
    public void onUpdate(@Nullable InspectView.AccessibilityNodeInfoData data) {
    }

    @Override
    public void onContentUpdated(@Nullable AccessibilityNodeInfo info) {
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

}
