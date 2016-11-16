package com.apus.sortdemo;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.apus.adapter.AllAppsListAdapter;
import com.apus.adapter.AppAdapter;
import com.apus.bean.AllAppInfo;
import com.apus.bean.AppInfo;
import com.apus.bean.CharacterParser;
import com.apus.bean.CitySortModel;
import com.apus.bean.PinyinComparator;
import com.apus.utils.AppUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AppListActivity extends AppCompatActivity {

    private RecyclerView recycler_view;
    private CharacterParser characterParser;
    private PinyinComparator pinyinComparator;

    private List<AllAppInfo> allAppInfoList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_list_activity);

        recycler_view = (RecyclerView) findViewById(R.id.recycler_view);

        recycler_view.setLayoutManager(new LinearLayoutManager(this));
        AllAppsListAdapter allAppsListAdapter = new AllAppsListAdapter(this);

        List<AppInfo> appList = AppUtils.getAppList(this);
        HashMap<Character, ArrayList<AllAppInfo>> appGroup = getAppGroup(appList);
        allAppsListAdapter.setData(appGroup);
        allAppsListAdapter.notifyDataSetChanged();
    }

    private HashMap<Character, ArrayList<AllAppInfo>> getAppGroup(List<AppInfo> appList) {

        HashMap<Character, ArrayList<AllAppInfo>> maps = new HashMap<>();

        characterParser = CharacterParser.getInstance();
        pinyinComparator = new PinyinComparator();
        AllAppInfo allAppInfo;
        for (int i=0; i< appList.size(); i++){
            allAppInfo = new AllAppInfo();
            allAppInfo.appInfo = appList.get(i);
            String pinyin = characterParser.getSelling(appList.get(i).getAppName());
            String sortString = pinyin.substring(0, 1).toUpperCase();

            if (sortString.matches("[A-Z]"))
                allAppInfo.pinyinString = sortString.toUpperCase();
            else
                allAppInfo.pinyinString = "#";
            allAppInfoList.add(allAppInfo);
        }

        for (int i=0 ; i< allAppInfoList.size(); i++){
            AllAppInfo allApp = allAppInfoList.get(i);
            String pinyin = allApp.pinyinString;
            char c = pinyin.charAt(0);
            if (maps.containsKey(pinyin)){
                ArrayList<AllAppInfo> allAppInList = maps.get(pinyin);
                if (allAppInList == null){
                    allAppInList = new ArrayList<>();
                    allAppInList.add(allApp);
                }else
                    allAppInList.add(allApp);
                maps.put(Character.valueOf(c),allAppInList);
            }else {
                ArrayList<AllAppInfo> allAPPList = new ArrayList<>();
                allAPPList.add(allApp);
                maps.put(Character.valueOf(c),allAPPList);
            }
        }

        return maps;
    }


}
