package com.aghajari.app.inspect.pages.app;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.aghajari.app.inspect.R;
import com.aghajari.app.inspect.node.HierarchyUtils;
import com.aghajari.app.inspect.node.LayoutSettings;
import com.aghajari.app.inspect.node.NodeInfo;
import com.aghajari.app.inspect.pages.element.ElementAdapter;
import com.aghajari.app.inspect.utils.AppColors;
import com.aghajari.app.inspect.views.InspectView;

public class AppView extends FrameLayout implements LayoutSettings.OnUpdatedListener {

    final ImageView appIcon;
    final TextView tvName;
    final TextView tvPackage;
    final RecyclerView recyclerView;
    final ImageView appInfo;
    private CharSequence loadedClass = "", loadedPackage = "";

    public AppView(Context context) {
        super(context);
        inflate(context, R.layout.page_app, this);

        tvName = findViewById(R.id.app_name);
        tvPackage = findViewById(R.id.app_package);
        appIcon = findViewById(R.id.app_icon);
        recyclerView = findViewById(R.id.rv);
        recyclerView.setAdapter(new ElementAdapter());

        appInfo = findViewById(R.id.app_info);
        appInfo.setColorFilter(AppColors.LIGHT_COLOR);
        appInfo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(LayoutSettings.getInstance().packageName) && LayoutSettings.getInstance().floatingView != null) {
                    LayoutSettings.getInstance().floatingView.hideInspect();

                    Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + LayoutSettings.getInstance().packageName));
                    myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
                    myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getContext().startActivity(myAppSettings);
                }
            }
        });

        onContentUpdated(null);
    }

    @Override
    public void onSelected(@Nullable InspectView.AccessibilityNodeInfoData data) {
    }

    @Override
    public void onUpdate(@Nullable InspectView.AccessibilityNodeInfoData data) {
    }

    @Override
    public void onContentUpdated(@Nullable AccessibilityNodeInfo info) {
        if (TextUtils.isEmpty(LayoutSettings.getInstance().packageName)) {
            notFound();
        } else {
            if (loadedClass.equals(LayoutSettings.getInstance().className) &&
                    loadedPackage.equals(LayoutSettings.getInstance().packageName))
                return;

            try {
                appInfo.setVisibility(VISIBLE);
                PackageInfo appInfo = getContext().getPackageManager().getPackageInfo(LayoutSettings.getInstance().packageName.toString(), 0);
                CharSequence name = appInfo.applicationInfo.loadLabel(getContext().getPackageManager());
                tvName.setText(name);
                tvPackage.setText(appInfo.packageName);
                appIcon.setImageDrawable(appInfo.applicationInfo.loadIcon(getContext().getPackageManager()));
                ((ElementAdapter) recyclerView.getAdapter()).createElements(appInfo, name);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                notFound();
            }
        }
    }

    private void notFound() {
        appInfo.setVisibility(GONE);
        tvName.setText("");
        tvPackage.setText("Couldn't find app info :(");
        appIcon.setImageDrawable(null);
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
