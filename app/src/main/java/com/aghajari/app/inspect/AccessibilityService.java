package com.aghajari.app.inspect;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;

import com.aghajari.app.inspect.node.LayoutSettings;
import com.aghajari.app.inspect.utils.Utils;
import com.aghajari.app.inspect.views.FloatingView;

public class AccessibilityService extends android.accessibilityservice.AccessibilityService {

    public WindowManager mWindowManager;
    FloatingView mFloatingView;

    public AccessibilityService() {
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        if (accessibilityEvent.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (accessibilityEvent.getPackageName() != null && accessibilityEvent.getClassName() != null) {
                ComponentName componentName = new ComponentName(
                        accessibilityEvent.getPackageName().toString(),
                        accessibilityEvent.getClassName().toString()
                );

                ActivityInfo activityInfo = tryGetActivity(componentName);
                boolean isActivity = activityInfo != null;
                if (isActivity)
                    LayoutSettings.getInstance().className = componentName.flattenToShortString()
                            .replace("/", "").replace("$", ".");
                else
                    LayoutSettings.getInstance().className = null;

                LayoutSettings.getInstance().packageName = accessibilityEvent.getPackageName();
            }
        }
        LayoutSettings.getInstance().update(getRootInActiveWindow());
        LayoutSettings.getInstance().invalidate();
    }

    private ActivityInfo tryGetActivity(ComponentName componentName) {
        try {
            return getPackageManager().getActivityInfo(componentName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }


    @Override
    public void onInterrupt() {

    }

    @Override
    public void onCreate() {
        super.onCreate();

        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mFloatingView = new FloatingView(this);
        mFloatingView.show();

        LayoutSettings.getInstance().enabled = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFloatingView != null)
            mFloatingView.hide();

        LayoutSettings.getInstance().enabled = false;
    }

    public static boolean isAccessibilityServiceEnabled(Context context) {
        ComponentName expectedComponentName = new ComponentName(context, AccessibilityService.class);

        String enabledServicesSetting = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (enabledServicesSetting == null)
            return false;

        TextUtils.SimpleStringSplitter colonSplitter = new TextUtils.SimpleStringSplitter(':');
        colonSplitter.setString(enabledServicesSetting);

        while (colonSplitter.hasNext()) {
            String componentNameString = colonSplitter.next();
            ComponentName enabledService = ComponentName.unflattenFromString(componentNameString);

            if (enabledService != null && enabledService.equals(expectedComponentName))
                return true;
        }

        return false;
    }

    public void destroy() {
        if (mFloatingView != null)
            mFloatingView.hide();
        mFloatingView = null;
        LayoutSettings.getInstance().enabled = false;
        stopSelf();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            disableSelf();
    }

}