package com.example.xyzreader.util;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;

public class ColumnCalculator {
    public static int getOptimalNumberOfColumn(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        if (context != null) {
            ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        }

        int widthDivider = 500;

        if (context != null) {
                int width = displayMetrics.widthPixels;
                int nColumns = width / widthDivider;
                if (nColumns < 2) return 2;
                return nColumns;
        }

        return 2;
    }
}