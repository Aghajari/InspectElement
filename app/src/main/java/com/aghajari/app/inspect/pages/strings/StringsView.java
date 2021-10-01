package com.aghajari.app.inspect.pages.strings;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;

import com.aghajari.app.inspect.R;
import com.aghajari.app.inspect.node.LayoutSettings;
import com.aghajari.app.inspect.utils.AppColors;
import com.aghajari.app.inspect.utils.SimpleSpinnerAdapter;
import com.aghajari.app.inspect.views.InspectView;
import com.aghajari.app.inspect.views.PopupView;

public class StringsView extends FrameLayout implements LayoutSettings.OnUpdatedListener {

    final TextView nodeID;
    final TextView nodeText;
    final TextView nodeLength;
    final ImageView copy;

    public StringsView(Context context) {
        super(context);
        inflate(context, R.layout.page_strings, this);

        TextView property = findViewById(R.id.property);
        final String[] property_items = new String[]{"All", "Text", "Content desc."};
        PopupView.create(property,
                new SimpleSpinnerAdapter(context, property_items),
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        LayoutSettings.getInstance().stringsType = LayoutSettings.StringsType.values()[i];
                        property.setText(property_items[i]);
                        LayoutSettings.getInstance().invalidate();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                    }
                });
        property.setText(property_items[LayoutSettings.getInstance().stringsType.ordinal()]);

        nodeID = findViewById(R.id.node_state);
        nodeText = findViewById(R.id.node_text);
        nodeLength = findViewById(R.id.length);
        copy = findViewById(R.id.copy);
        copy.setColorFilter(AppColors.LIGHT_COLOR);
        copy.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("NodeText", nodeText.getText());
                clipboard.setPrimaryClip(clip);
                copy.setVisibility(GONE);
            }
        });

        nodeText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                nodeText.setTextIsSelectable(false);
                nodeText.post(new Runnable() {
                    @Override
                    public void run() {
                        nodeText.setTextIsSelectable(true);
                    }
                });
            }
        });
        onUpdate(LayoutSettings.getInstance().data);
    }

    @Override
    public void onUpdate(@Nullable InspectView.AccessibilityNodeInfoData data) {
        CharSequence text = null;
        if (data != null)
            text = LayoutSettings.getInstance().getText(data.getFinalInfo());

        nodeLength.setText(String.valueOf(text != null ? text.length() : ":("));

        if (data == null) {
            nodeID.setText("Tap on a screen element for inspection!");
            nodeText.setText("");
        } else if (TextUtils.isEmpty(text)) {
            nodeID.setText("No strings!");
            nodeText.setText("");
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
                String str = data.getFinalInfo().getClassName().toString();
                nodeID.setText(str.substring(str.lastIndexOf(".") + 1));
            }
            nodeText.setText(text);
        }

        copy.setVisibility(TextUtils.isEmpty(nodeText.getText()) ? GONE : VISIBLE);
    }

    @Override
    public void onSelected(@Nullable InspectView.AccessibilityNodeInfoData data) {
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
