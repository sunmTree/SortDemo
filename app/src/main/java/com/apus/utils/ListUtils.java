package com.apus.utils;

import android.util.Log;

import java.util.List;

/**
 * Created by sunmeng on 2016/11/15.
 */

public class ListUtils {

    public static void printList(List<String> strings) {
        printList(strings,"SCROLL_TAG");
    }

    public static void printList(List<String> strings, String TAG) {
        if (strings == null)
            return;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < strings.size(); i++) {
            sb.append(strings.get(i) + "\t");
        }
        Log.d(TAG, "class " + strings.getClass().getSimpleName() + " " + sb.toString());
    }

}
