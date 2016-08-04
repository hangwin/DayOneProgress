package com.hang.study.dayoneprogress.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by hang on 16/8/4.
 */
public class SPUtil {
    public static void setInt(Context context,String key,int val) {
        SharedPreferences sp=context.getSharedPreferences("progress", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sp.edit();
        editor.putInt(key,val);
        editor.commit();
    }
    public static int getInt(Context context,String key) {
        SharedPreferences sp=context.getSharedPreferences("progress",Context.MODE_PRIVATE);
        return sp.getInt(key,-1);
    }
}
