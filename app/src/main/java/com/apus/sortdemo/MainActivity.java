package com.apus.sortdemo;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.apus.adapter.SortAdapter;
import com.apus.bean.CharacterParser;
import com.apus.bean.CitySortModel;
import com.apus.bean.PinyinComparator;
import com.apus.view.SideBar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, AbsListView.OnScrollListener {

    private ListView recyclerView;
    private SideBar sideBar;
    private TextView dialog;

    private SortAdapter adapter;

    private CharacterParser characterParser;
    private List<CitySortModel> sourceDataList;

    private PinyinComparator pinyinComparator;
    private static final String TAG = "SCROLL_TAG";

    // 用来记录字母个数的HashMap
    private HashMap<String, Integer> charMaps;
    private int screenHeight;
    private List<String> letters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {
        recyclerView = (ListView) findViewById(R.id.recycler_view);
        sideBar = (SideBar) findViewById(R.id.side_bar);
        dialog = new TextView(this);
        dialog.setTextSize(20);
        dialog.setTextColor(Color.parseColor("#ffffff"));
        sideBar.setmTextDialog(dialog);

        sideBar.setmOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                int position = adapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    recyclerView.setSelection(position);
                }
            }
        });

        characterParser = CharacterParser.getInstance();
        pinyinComparator = new PinyinComparator();

        recyclerView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(MainActivity.this, ((CitySortModel) adapter.getItem(i)).getCityName(), Toast.LENGTH_SHORT).show();
            }
        });
        sourceDataList = filledData(getResources().getStringArray(R.array.provinces));
        Collections.sort(sourceDataList, pinyinComparator);
        letters = new ArrayList<>();
        charMaps = new HashMap<>();
        for (CitySortModel model : sourceDataList) {
            String sortLetters = model.getSortLetters();
            // TODO 记录字母和对应的个数
            if (charMaps.containsKey(sortLetters)) {
                int num = charMaps.get(sortLetters);
                charMaps.put(sortLetters, num + 1);
            } else {
                charMaps.put(sortLetters, 1);
            }

            if (!letters.contains(sortLetters)) {
                letters.add(sortLetters);
            }
        }
        int size = letters.size();
        String[] sorts = new String[size];
        for (int i = 0; i < size; i++) {
            sorts[i] = letters.get(i);
        }
        sideBar.setB(sorts);
        adapter = new SortAdapter(sourceDataList, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setOnItemClickListener(this);
        recyclerView.setOnScrollListener(this);

        Display defaultDisplay = getWindowManager().getDefaultDisplay();
        int width = defaultDisplay.getWidth();
        screenHeight = defaultDisplay.getHeight();
        Log.d(TAG, "width: " + width + " height: " + screenHeight);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // TODO 计算屏幕宽度内存在的字母
        ViewTreeObserver observer = this.getWindow().getDecorView().getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                sideBar.setChats(getLenChars(screenHeight));
            }
        });
    }

    /***
     * 根据长度计算cLength对应的字母集合
     * @param cLength  目标长度
     * @return  字母集合
     */
    public List<String> getLenChars(int cLength) {
        List<String> stringList = new ArrayList<String>();
        int lenCount = 0;
        final int cSize = letters.size();
        for (int i = 0; i < cSize; i++) {

            int value = charMaps.get(letters.get(i));
            View childAt = recyclerView.getChildAt(0);

            if (childAt != null) {
                int height = childAt.getHeight();
                lenCount += height;
                if (value > 1) {
                    lenCount += (value - 1) * height / 2;
                }
            }
            if (lenCount < cLength) {
                stringList.add(letters.get(i));
            }

        }
        Log.d(TAG,"all char lenCount: "+lenCount);
        return stringList;
    }

    public Integer getLengthFromNum(int number){
        int lenCount = 0;
        int charLength = 0;
        final int cSize = letters.size();
        for (int i=0; i< cSize; i++){
            int value = charMaps.get(letters.get(i));
            lenCount += value;
            if (lenCount <= number){
                View childAt = recyclerView.getChildAt(0);

                if (childAt != null) {
                    int height = childAt.getHeight();
                    charLength += height;
                    if (value > 1) {
                        charLength += (value - 1) * height / 2;
                    }
                }

            }else
                break;
        }
        Log.d(TAG," charLength: "+charLength+" number: "+number+" lenCount: "+lenCount);
        return charLength;
    }

    private List<CitySortModel> filledData(String[] date) {
        List<CitySortModel> models = new ArrayList<>(date.length);
        for (int i = 0; i < date.length; i++) {
            CitySortModel citySortModel = new CitySortModel();
            citySortModel.setCityName(date[i]);
            String pinyin = characterParser.getSelling(date[i]);
            String sortString = pinyin.substring(0, 1).toUpperCase();

            if (sortString.matches("[A-Z]"))
                citySortModel.setSortLetters(sortString.toUpperCase());
            else
                citySortModel.setSortLetters("#");
            models.add(citySortModel);
        }
        Log.d(TAG, "dataSize: " + models.size());
        return models;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        Intent intent = new Intent(MainActivity.this, QuickSideBarActivity.class);
//        startActivity(intent);
    }

    private boolean scrollStateChanged = false; // 判断用户滑动过的标志
    private boolean haveScroll = false;         // 判断已经滑动过的标志

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
//        Log.d("SCROLL_TAG","scrollState: "+scrollState);
        if (scrollState > 0)
            scrollStateChanged = true;
        else
            scrollStateChanged = false;
    }

    private static int currentItem = 0;

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        Log.d("SCROLL_TAG", "firstVisibleItem: " + firstVisibleItem + " visibleItemCount: " + visibleItemCount + " totalItemCount: " + totalItemCount+ " scrollY: " + getScrollY());
        if (firstVisibleItem != currentItem) {
            haveScroll = true;
            currentItem = firstVisibleItem;
        } else {
            haveScroll = false;
        }

        List<String> lenChars = getLenChars(getScrollY() + screenHeight);
        List<String> lenChars1 = getLenChars(getScrollY());
        printList(lenChars);
        printList(lenChars1);
        lenChars.removeAll(lenChars1);
        sideBar.setChats(lenChars);
    }

    public void printList(List<String> strings){
        StringBuilder sb = new StringBuilder();
        for (int i=0; i< strings.size(); i++){
            sb.append(strings.get(i)+"\t");
        }
        Log.d(TAG,"class "+strings.getClass().getSimpleName()+" "+sb.toString());
    }

    public int getScrollY() {
        View c = recyclerView.getChildAt(0);
        if (c == null) {
            return 0;
        }
        int firstVisiblePosition = recyclerView.getFirstVisiblePosition();
        int top = c.getTop();
        Log.d("SCROLL_TAG", "top: " + top + " height: " + getLengthFromNum(firstVisiblePosition));
        return -top + getLengthFromNum(firstVisiblePosition);
    }
}
