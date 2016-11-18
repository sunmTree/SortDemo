package com.apus.sortdemo;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
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
import com.apus.view.AllAppsIndexScroller;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AppListActivity extends AppCompatActivity {

    private static final String TAG = "AppListActivity";

    private RecyclerView recycler_view;
    private CharacterParser characterParser;
    private PinyinComparator pinyinComparator;

    private AllAppsIndexScroller mAllAppsIndexScroller;

    private List<AllAppInfo> allAppInfoList = new ArrayList<>();

    private AllAppsIndexScroller mScroller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_list_activity);

        recycler_view = (RecyclerView) findViewById(R.id.recycler_view);
        mAllAppsIndexScroller = (AllAppsIndexScroller) findViewById(R.id.allAppIndexScroller);

        //设置列表数据和浮动header
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recycler_view.setLayoutManager(layoutManager);
        AllAppsListAdapter allAppsListAdapter = new AllAppsListAdapter(this);
        recycler_view.setAdapter(allAppsListAdapter);
        List<AppInfo> appList = AppUtils.getAppList(this);
        HashMap<Character, ArrayList<AllAppInfo>> appGroup = getAppGroup(appList);
        allAppsListAdapter.setData(appGroup);
        allAppsListAdapter.notifyDataSetChanged();

        mAllAppsIndexScroller.setListView(recycler_view);

        recycler_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int firstVisibleItem = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                if (null != mAllAppsIndexScroller) {
                    mAllAppsIndexScroller.setCurrentSectionForRow(firstVisibleItem);
                }
            }
        });

    }

    private HashMap<Character, ArrayList<AllAppInfo>> getAppGroup(List<AppInfo> appList) {

        HashMap<Character, ArrayList<AllAppInfo>> maps = new HashMap<>();

        characterParser = CharacterParser.getInstance();
        pinyinComparator = new PinyinComparator();
        AllAppInfo allAppInfo;
        for (int i = 0; i < appList.size(); i++) {
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

        for (int i = 0; i < allAppInfoList.size(); i++) {
            AllAppInfo allApp = allAppInfoList.get(i);
            String pinyin = allApp.pinyinString;
            char c = pinyin.charAt(0);
            if (maps.containsKey(c)) {
                ArrayList<AllAppInfo> allAppInList = maps.get(c);
                if (allAppInList == null) {
                    allAppInList = new ArrayList<>();
                    allAppInList.add(allApp);
                } else
                    allAppInList.add(allApp);
                maps.put(Character.valueOf(c), allAppInList);
            } else {
                ArrayList<AllAppInfo> allAPPList = new ArrayList<>();
                allAPPList.add(allApp);
                maps.put(Character.valueOf(c), allAPPList);
            }
        }

        return maps;
    }


}
