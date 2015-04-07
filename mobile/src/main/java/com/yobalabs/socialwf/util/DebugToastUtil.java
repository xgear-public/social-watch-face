package com.yobalabs.socialwf.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by andrey on 10/8/14.
 */
public class DebugToastUtil {

    public static void showDebugToast(String msg, Context context) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

}
