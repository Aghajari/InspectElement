package com.aghajari.app.inspect;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.Window;
import android.widget.TextView;

import com.aghajari.app.inspect.node.LayoutSettings;
import com.aghajari.app.inspect.utils.AppColors;
import com.aghajari.app.inspect.utils.Utils;

public class MainActivity extends AppCompatActivity {

    TextView view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        view = findViewById(R.id.enable);
        bindGithubText();
        bindInfoText();
    }

    @Override
    protected void onResume() {
        super.onResume();
        update();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, 101);
        } else {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivityForResult(intent, 101);
        }
    }

    private void update() {
        if (LayoutSettings.getInstance().enabled) {
            view.setText("Enabled");
            view.setBackgroundColor(AppColors.PRIMARY_COLOR);
        } else {
            view.setText("Disabled");
            view.setBackgroundColor(AppColors.AREA_COLOR);
        }
    }

    private void bindGithubText() {
        TextView tv = findViewById(R.id.github);
        tv.setTextColor(Color.GRAY);
        String author = getString(R.string.author);

        SpannableStringBuilder builder = new SpannableStringBuilder("See ");
        builder.append(author);
        builder.setSpan(new ForegroundColorSpan(Color.BLACK), builder.length() - author.length(), builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append("'s GitHub");
        tv.setText(builder);
    }

    private void bindInfoText() {
        TextView tv = findViewById(R.id.desc);
        tv.setTextColor(Color.DKGRAY);

        final String desc = getString(R.string.desc);
        SpannableStringBuilder builder = new SpannableStringBuilder(desc);
        int i = desc.indexOf("inspect");
        builder.setSpan(new ForegroundColorSpan(AppColors.BG_COLOR), i, i + 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        i = desc.indexOf("X-Ray");
        builder.setSpan(new ForegroundColorSpan(AppColors.BG_COLOR), i, i + 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        i = desc.indexOf("AccessibilityService");
        builder.setSpan(new ForegroundColorSpan(AppColors.BG_COLOR), i, i + 20, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        i = desc.indexOf("inspection");
        builder.setSpan(new ForegroundColorSpan(AppColors.BG_COLOR), i, i + 10, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv.setText(builder);
    }

    public void enable(android.view.View view) {
        if (LayoutSettings.getInstance().enabled) {
            if (LayoutSettings.getInstance().floatingView != null)
                LayoutSettings.getInstance().floatingView.destroy();
        } else {
            checkPermission();
        }
        update();
    }

    public void github(android.view.View view) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse("https://github.com/Aghajari"));
        startActivity(i);
    }
}