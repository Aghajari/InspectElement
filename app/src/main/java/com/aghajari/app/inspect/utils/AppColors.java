package com.aghajari.app.inspect.utils;

import android.graphics.Color;

import androidx.core.content.ContextCompat;

import com.aghajari.app.inspect.Application;
import com.aghajari.app.inspect.R;

public final class AppColors {

    public static int AREA_COLOR;
    public static int PRIMARY_COLOR;
    public static int LIGHT_COLOR;
    public static int BG_COLOR;
    public static int BAR_COLOR;
    public static int BG_WHITE_COLOR;

    public static void init(){
        AREA_COLOR = get(R.color.area);
        PRIMARY_COLOR = get(R.color.primary);
        BG_COLOR = get(R.color.primary_dark);
        LIGHT_COLOR = get(R.color.primary_light);
        BG_WHITE_COLOR = get(R.color.white2);
        BAR_COLOR = PRIMARY_COLOR;
    }

    private static int get(int resID){
        return ContextCompat.getColor(Application.applicationContext, resID);
    }

    public static int generate(int percent, int max, int alpha) {
        if (percent > max) {
            boolean r = false;
            while (percent > max) {
                percent -= max;
                r = !r;
            }
            if (r)
                percent = max - percent;
        }

        int color1 = AppColors.AREA_COLOR;
        int color2 = AppColors.PRIMARY_COLOR;

        float[] color1HSV = new float[3];
        Color.colorToHSV(color1, color1HSV);

        float[] color2HSV = new float[3];
        Color.colorToHSV(color2, color2HSV);

        color2HSV[0] += percent * (color1HSV[0] - color2HSV[0]) / max;
        color2HSV[1] += percent * (color1HSV[1] - color2HSV[1]) / max;
        color2HSV[2] += percent * (color1HSV[2] - color2HSV[2]) / max;

        return Color.HSVToColor(alpha, color2HSV);
    }

    public static int alpha(int alpha, int color) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }
}
