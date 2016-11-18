package com.apus.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;
import android.widget.SectionIndexer;

import com.apus.adapter.AllAppsListAdapter;
import com.apus.sortdemo.R;
import com.apus.utils.ScreenUtils;

/**
 * All Apps 页面右边的滚动条
 *
 * @author hyatt
 */
public class AllAppsIndexScroller extends View {

    private static final boolean DEBUG = true;

    private static final String TAG = "AllAppsIndexScroller";

    public interface OnIndexScrollListener {
        void onIndexScroll(AllAppsIndexScroller scroller);
    }

    private int m_nCurrentSection;
    private int m_nLastSection;
    private float m_fVisibleFractionForTopSection;
    private float m_fVisibleFractionForBottomSection;
    private float m_fItemHeight;
    private float m_fPaddingHeight;

    RecyclerView mAttachedListView;

    SectionIndexer mSectionIndexer;
    Object[] mSectionData;

    private RectF mBackgroundRect = new RectF();
    private Paint mBackgroundPainter;
    private Paint mTextPainter;
    private Paint mHighlightTextPainter;

    private Bitmap mRecentIcon;
    private OnIndexScrollListener onIndexScrollListener;

    private boolean isListViewHaveScrolled = false;

    private Scroller mScroller;

    public AllAppsIndexScroller(Context c, AttributeSet attributeSet) {
        super(c, attributeSet);
        m_nCurrentSection = -1;
        m_nLastSection = -1;
        m_fVisibleFractionForTopSection = 1f;
        m_fVisibleFractionForBottomSection = 1f;
        mAttachedListView = null;
        mSectionIndexer = null;
        mSectionData = null;
        mScroller = new Scroller(c);
        initPainter(c, attributeSet, 0);
    }

    public AllAppsIndexScroller(Context c, AttributeSet attributeSet, int defStyleAttr) {
        super(c, attributeSet, defStyleAttr);
        m_nCurrentSection = -1;
        m_nLastSection = -1;
        m_fVisibleFractionForTopSection = 1f;
        m_fVisibleFractionForBottomSection = 1f;
        mAttachedListView = null;
        mSectionIndexer = null;
        mSectionData = null;
        initPainter(c, attributeSet, defStyleAttr);
    }

    /**
     * 设置与 IndexScroller 绑定的 ListView
     *
     * @param listView 要绑定的 ListView
     */
    public void setListView(RecyclerView listView) {
        mAttachedListView = listView;
        if (listView != null) {
            setAdapter(mAttachedListView.getAdapter());
        } else {
            setAdapter(null);
        }
    }

    private void initPainter(Context c, AttributeSet attributeSet, int defStyleAttr) {
        try {
            mRecentIcon = BitmapFactory.decodeResource(c.getResources(), R.mipmap.recent);
            if (mRecentIcon == null) {
                mRecentIcon = getBitmapFromDrawable(c, R.mipmap.recent);
            }
            if (mRecentIcon != null) {
                int pixel = (int) ScreenUtils.dip2px(10.0f);
                Bitmap temp = Bitmap.createScaledBitmap(mRecentIcon, pixel, pixel, false);
                if (!mRecentIcon.isRecycled()) {
                    mRecentIcon.recycle();
                }
                mRecentIcon = temp;
            }
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "[catched]", e);
            }
        }
        mBackgroundPainter = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        mTextPainter = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        mHighlightTextPainter = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG | Paint.FAKE_BOLD_TEXT_FLAG);

        mBackgroundPainter.setColor(getStyledColor(c, attributeSet, android.R.attr.background, defStyleAttr));
        Resources ress = getResources();
        mTextPainter.setColor(ress.getColor(R.color.char_white));
        mHighlightTextPainter.setColor(ress.getColor(R.color.c_gray));
        float fTextSize = getStyledDimenPixelSize(c, attributeSet, android.R.attr.textSize, defStyleAttr);
        mTextPainter.setTextSize(40);
        mHighlightTextPainter.setTextSize(40);
    }

    private int getStyledColor(Context c, AttributeSet attributeSet, int style, int defStyleAttr) {
        TypedArray typedArray = c.obtainStyledAttributes(attributeSet, new int[]{style}, defStyleAttr, 0);
        try {
            return typedArray.getColor(0, 0);
        } catch (Throwable e) {
            if (DEBUG) {
                Log.e(TAG, "", e);
            }
        } finally {
            typedArray.recycle();
        }
        return 0;
    }

    private int getStyledDimenPixelSize(Context c, AttributeSet attributeSet, int style, int defStyleAttr) {
        TypedArray typedArray = c.obtainStyledAttributes(attributeSet, new int[]{style}, defStyleAttr, 0);
        try {
            return typedArray.getDimensionPixelSize(0, 15);
        } catch (Throwable e) {
            if (DEBUG) {
                Log.e(TAG, "", e);
            }
        } finally {
            typedArray.recycle();
        }
        return 0;
    }

    private int calcVisible(int visiblePosition, int section) {
        int result = 0;
        int i = visiblePosition - 1;
        while (i >= 0) {
            if (mSectionIndexer.getSectionForPosition(i) != section) {
                break;
            }

            ++result;
            --i;
        }

        return result;
    }

    private boolean isInRange(float y) {
        boolean result;
        if (y < mBackgroundRect.top || y > mBackgroundRect.top + mBackgroundRect.height()) {
            result = false;
        } else {
            result = true;
        }

        return result;
    }

    private boolean isSectionChanged(int section) {
        boolean result;
        if (m_nCurrentSection != section) {
            m_nCurrentSection = section;
            invalidate();
            result = true;
        } else {
            result = false;
        }

        return result;
    }

    private void calcVisibleFraction() {
        m_fVisibleFractionForTopSection = getVisibleFractionForTopSection();
        m_fVisibleFractionForBottomSection = getVisibleFractionForBottomSection();
    }

    private int getSectionForPosition(float f) {
        int section = 0;
        if (mSectionData != null && mSectionData.length != 0 && f >= mBackgroundRect.top) {  // 点击范围在view定义的范围之内
            if (f >= mBackgroundRect.top + mBackgroundRect.height()) {   // 大于定义的范围，就认为在最后一个字母
                section = mSectionData.length - 1;
            } else {
                section = ((int) ((f - mBackgroundRect.top) / (mBackgroundRect.height() / (((float) mSectionData.length)))));
            }
        }

        return section;
    }

    private int getBottomPosition(int position, int section) {
        int result = 0;
        int pos = position + 1;
        while (pos < mAttachedListView.getAdapter().getItemCount()) {
            if (mSectionIndexer.getSectionForPosition(pos) != section) {
                break;
            }

            ++result;
            ++pos;
        }

        return result;
    }

    void calcHeight() {
        if (mSectionData != null) {
            m_fItemHeight = ((float) (getHeight() / (mSectionData.length + 1)));
            m_fPaddingHeight = (m_fItemHeight - (mTextPainter.descent() - mTextPainter.ascent())) / 2f;
        }
    }

    private static final float FULL_ALPHA = 255f;

    @Override
    public void draw(Canvas canvas) {
        int alpha;
        super.draw(canvas);
        canvas.drawRect(mBackgroundRect, mBackgroundPainter); // 绘制边界
        // canvas.drawLine(0f, 0f, 0f, ((float) getHeight()), mTextPainter);
        if (mSectionData != null && mSectionData.length > 0) {
            int i;
            for (i = 0; i < mSectionData.length; ++i) {
                if (i < m_nCurrentSection || i > m_nLastSection) {
                    alpha = 0;
                } else {
                    alpha = 1;
                }

                String s = String.valueOf(mSectionData[i]);
                float x = ((((float) getWidth())) - mTextPainter.measureText(s)) / 2f;
                float y = m_fItemHeight * (((float) i)) + m_fPaddingHeight - mTextPainter.ascent();
                if (String.valueOf(AllAppsListAdapter.ALL_APPS_RECENT_APP).equals(s)) {
                    if (mRecentIcon == null || mRecentIcon.getWidth() <= 0 || mRecentIcon.getHeight() <= 0) {
                        canvas.drawText(" ", x, y, mTextPainter);
                    } else {
                        mTextPainter.setColorFilter(new PorterDuffColorFilter(mTextPainter.getColor(), PorterDuff.Mode.SRC_ATOP));
                        canvas.drawBitmap(mRecentIcon, ((float) getWidth() - mRecentIcon.getWidth()) / 2f,
                                ((m_fItemHeight - mRecentIcon.getHeight()) / 2f), mTextPainter);
                        mTextPainter.setColorFilter(null);
                    }
                } else {
                    canvas.drawText(s, x, y, mTextPainter);
                }
                if (alpha != 0) {
                    if (i == m_nCurrentSection) {
                        alpha = ((int) (m_fVisibleFractionForTopSection * FULL_ALPHA));
                    } else if (i == m_nLastSection) {
                        alpha = ((int) (m_fVisibleFractionForBottomSection * FULL_ALPHA));
                    } else {
                        alpha = 255;
                    }

                    mHighlightTextPainter.setAlpha(alpha);
                    if (String.valueOf(AllAppsListAdapter.ALL_APPS_RECENT_APP).equals(s)) {
                        if (mRecentIcon == null || mRecentIcon.getWidth() <= 0 || mRecentIcon.getHeight() <= 0) {
                            canvas.drawText(" ", x, y, mHighlightTextPainter);
                        } else {
                            mHighlightTextPainter.setColorFilter(new PorterDuffColorFilter(mHighlightTextPainter.getColor(), PorterDuff.Mode.SRC_ATOP));
                            canvas.drawBitmap(mRecentIcon, ((float) getWidth() - mRecentIcon.getWidth()) / 2f,
                                    ((m_fItemHeight - mRecentIcon.getHeight()) / 2f), mHighlightTextPainter);
                            mHighlightTextPainter.setColorFilter(null);
                        }
                    } else {
                        canvas.drawText(s, x, y, mHighlightTextPainter);
                    }
                }
            }
        }
    }

    private float getVisibleFractionForBottomSection() {
        float result = 1f;
        int nFirstVisiblePosition = ((LinearLayoutManager) mAttachedListView.getLayoutManager()).findFirstVisibleItemPosition();
        if (DEBUG) {
            Log.i(TAG, "First visible position = " + nFirstVisiblePosition);
        }
        int nLastVisiblePosition = ((LinearLayoutManager) mAttachedListView.getLayoutManager()).findLastVisibleItemPosition();
        if (DEBUG) {
            Log.i(TAG, "Last visible position = " + nLastVisiblePosition);
        }
        if (nFirstVisiblePosition >= 0 && nLastVisiblePosition >= 0) {
            int section = mSectionIndexer.getSectionForPosition(nLastVisiblePosition);
            if (DEBUG) {
                Log.i(TAG, "Section for last visible position = " + section);
            }
            if (section >= 0 && nFirstVisiblePosition != nLastVisiblePosition) {
                View v1_1 = mAttachedListView.getChildAt(mAttachedListView.getChildCount() - 1);
                if (v1_1 != null) {
                    result = (((float) (v1_1.getBottom() - mAttachedListView.getHeight())))
                            / (((float) v1_1.getHeight()));
                    nFirstVisiblePosition = calcVisible(nLastVisiblePosition, section);
                    result = ((((float) (nFirstVisiblePosition + 1))) - result)
                            / (((float) (nFirstVisiblePosition + getBottomPosition(nLastVisiblePosition, section) + 1)));
                }
            }
        }

        return result;
    }

    private float getVisibleFractionForTopSection() {
        float result = 1f;
        int nFirstVisiblePosition = ((LinearLayoutManager) mAttachedListView.getLayoutManager()).findFirstVisibleItemPosition();  //获取屏幕显示第一条数据的position
        int nLastVisiblePosition = ((LinearLayoutManager) mAttachedListView.getLayoutManager()).findLastVisibleItemPosition();    // 获取屏幕显示的最后一条数据的position
        if (nFirstVisiblePosition >= 0 && nLastVisiblePosition >= 0) {                                                           // 合法检查
            int section = mSectionIndexer.getSectionForPosition(nFirstVisiblePosition);                                          // 获取section列表对应字母的列表位置
            if (nFirstVisiblePosition != nLastVisiblePosition) {
                View view = mAttachedListView.getChildAt(0);
                if (view != null) {
                    result = (((float) (-view.getTop()))) / (((float) view.getHeight()));                                        // 向上滑动的距离占整个item的百分比
                    nLastVisiblePosition = calcVisible(nFirstVisiblePosition, section);
                    nFirstVisiblePosition = getBottomPosition(nFirstVisiblePosition, section);
                    result = ((((float) (nFirstVisiblePosition + 1))) - result)
                            / (((float) (nFirstVisiblePosition + nLastVisiblePosition + 1)));
                }
            }
        }

        return result;
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mBackgroundRect.set(0f, 0f, ((float) w), ((float) h));
        calcHeight();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean bHandled = true;
        float startY = 0.0f;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                // 加入点击验证，在显示范围内的点击，不再触发重绘
                if (isSectionContions(getSectionForPosition(event.getY()))) {
                    return bHandled;
                }

                if (!isSectionChanged(getSectionForPosition(event.getY()))) {  // 判断Section字符是否发生了变化
                    return bHandled;
                }

                mAttachedListView.scrollToPosition(mSectionIndexer.getPositionForSection(m_nCurrentSection));
                if (null != onIndexScrollListener) {
                    onIndexScrollListener.onIndexScroll(this);
                }
                mAttachedListView.post(mScrollRunnable);
                startY = event.getY();
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (!isInRange(event.getY())) {
                    return bHandled;
                }
//                if (isSectionContions(getSectionForPosition(event.getY()))) {
//                    return bHandled;
//                }
                if (!isSectionChanged(getSectionForPosition(event.getY()))) {   // 当event.getY()发生变动时，就会触发重绘的逻辑，但是这个步骤却没考虑到listView可能没发生滑动的情况
                    return bHandled;
                }

                mAttachedListView.scrollToPosition(mSectionIndexer.getPositionForSection(m_nCurrentSection));
//                mAttachedListView.scrollBy(0,);


                if (null != onIndexScrollListener) {
                    onIndexScrollListener.onIndexScroll(this);
                }
                mAttachedListView.post(mScrollRunnable);
                break;
            }
            default: {
                bHandled = false;
                break;
            }
        }

        return bHandled;
    }

    private boolean isSectionContions(int sectionForPosition) {
        if (sectionForPosition <= m_nLastSection && sectionForPosition >= m_nCurrentSection) {
            return true;
        } else
            return false;
    }

    private void setAdapter(RecyclerView.Adapter<?> adapter) {
        if (mSectionIndexer != null) {
            ((RecyclerView.Adapter<?>) mSectionIndexer).unregisterAdapterDataObserver(mDataSetObserver);
        }

        if (adapter != null) {
            mSectionIndexer = (SectionIndexer) adapter;
            adapter.registerAdapterDataObserver(mDataSetObserver);
            mDataSetObserver.onChanged();
        } else {
            mSectionIndexer = null;
        }
    }

    public void setCurrentSectionForRow(int position) {
        if (DEBUG) {
            Log.i(TAG, "Set currect position = " + position);
        }
        if (position >= 0) {
            m_nCurrentSection = mSectionIndexer.getSectionForPosition(position);
            calcVisibleFraction();
            invalidate();
            int n = ((LinearLayoutManager) mAttachedListView.getLayoutManager()).findLastVisibleItemPosition();
            if (n < 0) {
                n = m_nCurrentSection;
            } else {
                n = mSectionIndexer.getSectionForPosition(n);
            }
            m_nLastSection = n;
        }
    }

    private final RecyclerView.AdapterDataObserver mDataSetObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            if (DEBUG) {
                Log.e(TAG, "onChanged");
            }
            mSectionData = mSectionIndexer.getSections();
            if (DEBUG) {
                Log.e(TAG, "Sections = " + mSectionData.length);
            }
            calcHeight();
            setCurrentSectionForRow(((LinearLayoutManager) mAttachedListView.getLayoutManager()).findFirstVisibleItemPosition());
            invalidate();
        }
    };

    /**
     * 滑动 ListView 以保持与 Scroller 的同步
     */
    private final Runnable mScrollRunnable = new Runnable() {

        @Override
        public void run() {
            if (mAttachedListView == null) {
                return;
            }

            int pos = ((LinearLayoutManager) mAttachedListView.getLayoutManager()).findFirstVisibleItemPosition();
            if (pos >= 0) {
                setCurrentSectionForRow(pos);
            }
        }
    };

    private Bitmap getBitmapFromDrawable(Context c, int resId) {
        try {
            BitmapDrawable drawable = (BitmapDrawable) c.getResources().getDrawable(resId);
            return drawable.getBitmap().copy(Config.RGB_565, true);
        } catch (Exception e) {

        }
        return null;
    }

    public void recycle() {
        if (mRecentIcon != null && !mRecentIcon.isRecycled()) {
            mRecentIcon.recycle();
        }
    }

    public void setOnIndexScrollListener(OnIndexScrollListener onIndexScrollListener) {
        this.onIndexScrollListener = onIndexScrollListener;
    }
}
