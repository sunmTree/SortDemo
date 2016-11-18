package com.apus.adapter;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.apus.bean.AllAppInfo;
import com.apus.bean.AppInfo;
import com.apus.sortdemo.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by sunmeng on 2016/11/16.
 */

public class AllAppsListAdapter extends RecyclerView.Adapter<AllAppsListAdapter.AppIconContainerViewHolder> implements SectionIndexer, View.OnClickListener, View.OnLongClickListener {


    private static final String TAG = "AllAppsListAdapter";
    private static final boolean DEBUG = true;
    private static final float LEADING_CHAR_FONT_SIZE_FACTOR = 0.55f;
    public static final int APP_COUNT_PER_ROW = 4;
    public static final int FACTOR_APP_ICON_SIZE = 80;
    public static final Character ALL_APPS_RECENT_APP = '!';
    public static final Character ALL_APPS_RECOMMEND_APP = ' ';

    @Override
    public void onClick(View v) {
        Object tag = v.getTag();
        // 启动应用
        if (tag != null && tag instanceof AllAppInfo) {
            try {
                final AllAppInfo info = (AllAppInfo) tag;
                openAppInfo(v, mContext, info.appInfo);
            } catch (Throwable e) {
                if (DEBUG) {
                    Log.e(TAG, "", e);
                }
            }
        }
    }

    private void openAppInfo(View v, Context mContext, AppInfo appInfo) {
        String packageName = appInfo.getPackageName();
        Intent intent = mContext.getPackageManager().getLaunchIntentForPackage(packageName);
        mContext.startActivity(intent);
    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }

    static class RowItem {

        /**
         * 这行所属的字母分类
         */
        Character sectionChar;

        /**
         * 这行是否要显示字母？
         */
        boolean bShowSectionChar;

        /**
         * 这行要显示的 App
         */
        List<AllAppInfo> appList;

        /**
         * 下一行还有属于这个字母的应用吗？
         */
        boolean hasMore = false;

        Drawable sectionCharDrawable;

        boolean isUseSectionCharDrawable;
    }

    private final Context mContext;

    private final ArrayList<RowItem> mRowItemList = new ArrayList<RowItem>();

    /**
     * 首字母索引，同时也作为 List 的 Section
     */
    private final ArrayList<Character> mAppIndex = new ArrayList<Character>();

    private final LayoutInflater mInflater;

    private final int IMAGE_SIZE = 80;

    private boolean mHasSearchResult = false;

    private Bitmap mRecentIcon;

    private Animator mAnimator;

    public AllAppsListAdapter(Context c) {
        mContext = c;
        mInflater = LayoutInflater.from(mContext);
        try {
            mRecentIcon = BitmapFactory.decodeResource(c.getResources(), R.mipmap.recent);
            if (mRecentIcon == null) {
                mRecentIcon = getBitmapFromDrawable(c, R.mipmap.recent);
            }
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "[catched]", e);
            }
        }
    }

    public void setData(HashMap<Character, ArrayList<AllAppInfo>> appGroup) {
        initAppIndex(appGroup);
        initRowItemList(appGroup);
    }

    void setSearchResult(boolean hasSearchResult) {
        mHasSearchResult = hasSearchResult;
    }

    private void initAppIndex(HashMap<Character, ArrayList<AllAppInfo>> appGroup) {
        mAppIndex.clear();
        Set<Character> keySet = appGroup.keySet();
        for (Character ch : keySet) {
            mAppIndex.add(ch);
        }
        // 按 #, A, B, ... 排序
        Collections.sort(mAppIndex);
    }

    /***
     * 初始化数据
     *
     * @param appGroup
     */
    private void initRowItemList(HashMap<Character, ArrayList<AllAppInfo>> appGroup) {
        mRowItemList.clear();
        for (Character ch : mAppIndex) {
            ArrayList<AllAppInfo> apps = appGroup.get(ch);
            if (DEBUG) {
                Log.i(TAG, "[" + ch + "] " + apps.toString());
            }

            int n = apps.size();
            if (n > APP_COUNT_PER_ROW) {
                // 首行要显示字母
                RowItem lastRowItem = appendRowItemList(ch, apps.subList(0, APP_COUNT_PER_ROW), true);
                // 剩下的行不显示字母
                int startPos = APP_COUNT_PER_ROW;
                do {
                    lastRowItem.hasMore = true;
                    int endPos = startPos + APP_COUNT_PER_ROW;
                    if (endPos > n) {
                        endPos = n;
                    }
                    lastRowItem = appendRowItemList(ch, apps.subList(startPos, endPos), false);
                    startPos = endPos;
                } while (startPos < n);
            } else {
                appendRowItemList(ch, apps, true);
            }
        }
    }


    /***
     * 创建RowItem  对象
     *
     * @param key
     * @param appList
     * @param showSectionChar
     * @return
     */
    private RowItem appendRowItemList(Character key, List<AllAppInfo> appList, boolean showSectionChar) {
        RowItem rowItem = new RowItem();
        rowItem.sectionChar = key;
        rowItem.bShowSectionChar = showSectionChar;
        rowItem.appList = appList;
        String sectionChar = String.valueOf(rowItem.sectionChar);
        if (String.valueOf(ALL_APPS_RECENT_APP).equals(sectionChar) || String.valueOf(ALL_APPS_RECOMMEND_APP).equals(sectionChar)) {
            rowItem.sectionCharDrawable = createSectionCharDrawable(rowItem.sectionChar);
            rowItem.isUseSectionCharDrawable = true;
        }

        mRowItemList.add(rowItem);
        return rowItem;
    }

    private final Rect textBounds = new Rect();
    private Paint LEADING_CHAR_PAINT;
    private Map<Character, Drawable> sectionCharDrawableMap = new HashMap<Character, Drawable>();

    /***
     * 绘制字母drawable
     *
     * @param sectionChar 字母
     * @return
     */
    private Drawable createSectionCharDrawable(Character sectionChar) {
        Drawable d = sectionCharDrawableMap.get(sectionChar);
        if (d == null) {
            final int BMP_SIZE = IMAGE_SIZE;
            Bitmap bmp = Bitmap.createBitmap(BMP_SIZE, BMP_SIZE, Bitmap.Config.ARGB_8888);
            if (null == LEADING_CHAR_PAINT) {
                LEADING_CHAR_PAINT = new Paint();
                LEADING_CHAR_PAINT.setAntiAlias(true);
                LEADING_CHAR_PAINT.setColor(Color.WHITE);
                LEADING_CHAR_PAINT.setTextSize(IMAGE_SIZE * LEADING_CHAR_FONT_SIZE_FACTOR);
                LEADING_CHAR_PAINT.setTypeface(Typeface.SANS_SERIF);
                LEADING_CHAR_PAINT.setTextAlign(Paint.Align.CENTER);
            }

            Canvas c = new Canvas(bmp);
            float baseX = BMP_SIZE / 2;
            float baseY = BMP_SIZE / 2;
//            Drawable rectDrawable = mContext.getResources().getDrawable(R.drawable.all_app_app_leading_char_rect);
//            rectDrawable.setBounds(0, 0, BMP_SIZE, BMP_SIZE);
//            rectDrawable.draw(c);
            // 绘制文本
            String s = String.valueOf(sectionChar);
            LEADING_CHAR_PAINT.getTextBounds(s, 0, 1, textBounds);//get text bounds,
            // that can get the text width and height
            int textHeight = textBounds.bottom - textBounds.top;
            if (String.valueOf(ALL_APPS_RECENT_APP).equals(s)) {
                if (mRecentIcon == null || mRecentIcon.getWidth() <= 0 || mRecentIcon.getHeight() <= 0
                        || mRecentIcon.isRecycled()) {
                    c.drawText(s, baseX, baseY + textHeight / 2, LEADING_CHAR_PAINT);
                } else {
                    c.drawBitmap(mRecentIcon, baseX - mRecentIcon.getWidth() / 2, baseY - mRecentIcon.getHeight() / 2,
                            LEADING_CHAR_PAINT);
                }
            } else {
                c.drawText(s, baseX, baseY + textHeight / 2, LEADING_CHAR_PAINT);
            }
            d = new BitmapDrawable(mContext.getResources(), bmp);
            d.setBounds(0, 0, IMAGE_SIZE, IMAGE_SIZE);

            sectionCharDrawableMap.put(sectionChar, d);
        }
        d.setColorFilter(mContext.getResources().getColor(R.color.char_white), PorterDuff.Mode.MULTIPLY);
        return d;
    }

    @Override
    public AppIconContainerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        FrameLayout rootView = (FrameLayout) mInflater.inflate(R.layout.allapps_list_row, null);
        if (rootView == null) {
            if (DEBUG) {
                Log.e(TAG, "Can not inflate allapps_list_row");
            }
            return null;
        }

        FrameLayout appIconContainer = (FrameLayout) rootView.findViewById(R.id.app_icon_container);

        if (!createAppItemView(appIconContainer)) {
            return null;
        }

        setAppItemPosition(appIconContainer);

        setContainerPadding(viewType, appIconContainer);

        return new AppIconContainerViewHolder(rootView);
    }

    private void setContainerPadding(int viewType, FrameLayout appIconContainer) {
        if (viewType == (getItemCount() - 1)) {
            appIconContainer.setPadding(0, 0, 0, 60);
        } else {
            appIconContainer.setPadding(0, 0, 0, 0);
        }
    }

    private void setAppItemPosition(FrameLayout appIconContainer) {
        int allAppsItemMarginLeft = mContext.getResources().getDimensionPixelOffset(R.dimen.dp_6);

        int childCount = appIconContainer.getChildCount();
        for (int i = 1; i < childCount; i++) {
            View appItemView = appIconContainer.getChildAt(i);
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) appItemView.getLayoutParams();
            params.leftMargin = allAppsItemMarginLeft + (200 * (i));  // i-1 的情况下，第一个APP图标和文字之间不存在间隔，改正为i
            appItemView.setLayoutParams(params);
        }
    }

    private boolean createAppItemView(FrameLayout appIconContainer) {

        // 列首的字母
        TextView charView = (TextView) mInflater.inflate(R.layout.allapps_app_icon, null);
        charView.setCompoundDrawablePadding(0);
        charView.setEnabled(false);
        appIconContainer.addView(charView);

        // 应用图标，一行最多 APP_COUNT_PER_ROW 个应用图标；显示不满也占个位置，对齐用
        for (int i = 0; i < APP_COUNT_PER_ROW; i++) {
            ImageView appItemView = new ImageView(mContext);
            FrameLayout.LayoutParams appParams = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            appIconContainer.addView(appItemView, appParams);
            appItemView.setOnClickListener(this);
            appItemView.setOnLongClickListener(this);
        }
        return true;
    }

    @Override
    public void onBindViewHolder(AppIconContainerViewHolder holder, int position) {
        if (holder instanceof AppIconContainerViewHolder) {
            AppIconContainerViewHolder appIconContainerViewHolder = (AppIconContainerViewHolder) holder;
            FrameLayout appIconContainer = appIconContainerViewHolder.appIconContainer;
            bindAppItemView(appIconContainer, position);
        }
    }

    private void bindAppItemView(FrameLayout appIconContainer, int position) {
        View itemView = appIconContainer.getChildAt(0);
        if (itemView instanceof FrameLayout){
            FrameLayout frameLayout = (FrameLayout) itemView;
            itemView = frameLayout.getChildAt(0);
            appIconContainer = frameLayout;
        }
        TextView charView = null;
        if (itemView instanceof TextView) {
            charView = (TextView) itemView;
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) charView.getLayoutParams();
            params.width = mContext.getResources().getDimensionPixelOffset(R.dimen.dp_36);
            params.height = IMAGE_SIZE;
            charView.setPadding(charView.getPaddingLeft(), 6, 0, 0);
            charView.setLayoutParams(params);
            charView.setGravity(Gravity.CENTER);
        }

        // TODO  将数据和View 绑定
        RowItem rowItem = mRowItemList.get(position);
        String firstCharacter = String.valueOf(rowItem.sectionChar);

        // 标签
        if (charView != null) {
            if (rowItem.bShowSectionChar) {
                if (rowItem.isUseSectionCharDrawable && rowItem.sectionCharDrawable != null) {
                    //若是recent的图标，使用Drawable，否则均用字体
                    charView.setCompoundDrawables(null, rowItem.sectionCharDrawable, null, null);
                    charView.setText("");
                } else {
                    charView.setCompoundDrawables(null, null, null, null);
                    charView.setText(firstCharacter);
                }
                charView.setVisibility(View.VISIBLE);
            } else {
                charView.setVisibility(View.INVISIBLE);
            }
        }

        // 图标
        int n = rowItem.appList.size();
        for (int i = 0; i < n; i++) {
            ImageView image = (ImageView) appIconContainer.getChildAt(i + 1);
            if (image != null) {
                AllAppInfo appInfo = rowItem.appList.get(i);
                Log.d(TAG,"section: "+firstCharacter+" appInfo: "+appInfo.appInfo.getAppName());
                if (appInfo == null || appInfo.appInfo == null) {
                    continue;
                }
                image.setImageDrawable(appInfo.appInfo.getAppIcon());
                image.setTag(appInfo);
            }
        }

        for (int i = n; i < APP_COUNT_PER_ROW; i++){
            ImageView image = (ImageView) appIconContainer.getChildAt(i + 1);
            if (image != null) {
                image.setImageDrawable(null);
                image.setTag(null);
            }
        }
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "AllAppsListAdapter count: " + mRowItemList.size());
        return mRowItemList.size();
    }

    @Override
    public Object[] getSections() {
        return mAppIndex.toArray();
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        if (mAppIndex.size() <= sectionIndex) {
            if (DEBUG) {
                Log.e(TAG, "section = " + sectionIndex + ", size = " + mAppIndex.size());
            }
            return -1;
        }
        Character ch = mAppIndex.get(sectionIndex);
        int n = mRowItemList.size();
        for (int i = 0; i < n; i++) {
            RowItem rowItem = mRowItemList.get(i);
            if (ch.equals(rowItem.sectionChar)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int getSectionForPosition(int pos) {
        if (mRowItemList.size() <= pos) {
            if (DEBUG) {
                Log.e(TAG, "Pos = " + pos + ", size = " + mRowItemList.size());
            }
            return -1;
        }
        Character sectionChar = mRowItemList.get(pos).sectionChar;
        if (DEBUG) {
            Log.i(TAG, "Section Char = " + sectionChar + " for #" + pos);
        }
        return mAppIndex.indexOf(sectionChar);
    }

    private Bitmap getBitmapFromDrawable(Context c, int resId) {
        try {
            BitmapDrawable drawable = (BitmapDrawable) c.getResources().getDrawable(resId);
            return drawable.getBitmap().copy(Bitmap.Config.ARGB_8888, true);
        } catch (Exception e) {

        }
        return null;
    }

    static class AppIconContainerViewHolder extends RecyclerView.ViewHolder {

        FrameLayout appIconContainer;

        public AppIconContainerViewHolder(FrameLayout appIconContainer) {
            super(appIconContainer);
            this.appIconContainer = appIconContainer;
        }
    }
}
