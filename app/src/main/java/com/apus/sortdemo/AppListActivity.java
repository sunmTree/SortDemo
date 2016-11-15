package com.apus.sortdemo;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.apus.adapter.AppAdapter;
import com.apus.bean.AppInfo;
import com.apus.utils.AppUtils;

import java.util.List;

public class AppListActivity extends AppCompatActivity {

    private ListView app_list;
    private List<AppInfo> appList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_list_activity);

        app_list = (ListView) findViewById(R.id.app_list);
        appList = AppUtils.getAppList(this);
        AppAdapter appAdapter = new AppAdapter(this, appList);
        app_list.setAdapter(appAdapter);
        app_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = getPackageManager().getLaunchIntentForPackage(appList.get(position).getPackageName());
                startActivity(intent);
            }
        });
    }
}
