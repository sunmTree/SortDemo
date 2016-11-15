package com.apus.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;

import com.apus.bean.AppInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sunmeng on 2016/11/15.
 */

public class AppUtils {

    public static List<AppInfo> getAppList(Context context) {
        List<AppInfo> appList = new ArrayList<>();
        List<PackageInfo> packages = context.getPackageManager().getInstalledPackages(0);
        final int size = packages.size();
        AppInfo info;
        for (int i = 0; i < size; i++) {
            PackageInfo packageInfo = packages.get(i);
            if ((packageInfo.applicationInfo.flags & packageInfo.applicationInfo.FLAG_SYSTEM) > 0)
                continue;
            String appName = packageInfo.applicationInfo.loadLabel(context.getPackageManager()).toString();
            String packageName = packageInfo.packageName;
            String versionName = packageInfo.versionName;
            int versionCode = packageInfo.versionCode;
            Drawable appIcon = packageInfo.applicationInfo.loadIcon(context.getPackageManager());

            info = new AppInfo(appName,packageName,versionName,versionCode,appIcon);
            appList.add(info);
        }

        return appList;
    }

}
