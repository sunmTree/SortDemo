package com.apus.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.apus.sortdemo.MainActivity;
import com.apus.utils.ListUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sunmeng on 2016/11/11.
 */

public class SideBar extends View {

    private final String TAG = "SideBar_TAG";
    private boolean isShowTag = true;

    private OnTouchingLetterChangedListener mOnTouchingLetterChangedListener;

    public  String[] b = {"A", "B", "C", "D", "E", "F", "G", "H", "I",
            "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
            "W", "X", "Y", "Z", "#" };
    private int choose = -1;
    private Paint paint = new Paint();

    private TextView mTextDialog;

    // 字母列表高度
    private int height = 0;

    // 屏幕显示字母
    private List<String> chats = new ArrayList<>();

    /***
     * 为SideBar设置显示字母的TextView
     * @param mTextDialog
     */
    public void setmTextDialog(TextView mTextDialog) {
        this.mTextDialog = mTextDialog;
    }

    public SideBar(Context context) {
        super(context);
    }

    public SideBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 获取焦点改变背景颜色
        final int width = getWidth();
        height = getHeight();
        final int len = b.length;
        final int singleHeight = height / len;
        ListUtils.printList(chats,"SCROLL_SIDEBAR");
        for (int i=0; i < len; i++){
            paint.setColor(Color.rgb(33,65,98));
            paint.setTypeface(Typeface.DEFAULT_BOLD);
            paint.setAntiAlias(true);
            paint.setTextSize(30);
            if (chats.contains(b[i])){
                paint.setColor(Color.parseColor("#3399ff"));
                paint.setFakeBoldText(true);
            }

            // x坐标等于中间-字符串宽度的一半
            float xPos = width/2 - paint.measureText(b[i])/2;
            float yPos = singleHeight * i + singleHeight;
            canvas.drawText(b[i],xPos,yPos,paint);
            paint.reset();  //重置画笔
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        final float y = event.getY(); // 点击Y坐标
        final int oldChoose = choose;
        final OnTouchingLetterChangedListener listener = mOnTouchingLetterChangedListener;
        final int c = (int)(y / getHeight() * b.length);

        switch (action){
            case MotionEvent.ACTION_UP:
                setBackground(new ColorDrawable(0x00000000));
                choose = -1;
                invalidate();
                if (mTextDialog != null)
                    mTextDialog.setVisibility(INVISIBLE);
                break;
            default:
                setBackground(new ColorDrawable(Color.parseColor("#00000000")));
                if (oldChoose != c){
                    if (c >= 0 && c < b.length){
                        if (listener != null){
                            listener.onTouchingLetterChanged(b[c]);
                        }
                        if (mTextDialog != null){
                            mTextDialog.setText(b[c]);
                            mTextDialog.setVisibility(VISIBLE);
                        }

                        choose = c;
                        invalidate();
                    }
                }
                break;
        }
        return true;
    }

    private int count = 0;
    public void setChats(String msg, List<String> chats) {
        if ("OnResume".equals(msg) && count > 0)
            return;
        else
            count++;
        this.chats = chats;
        ListUtils.printList(chats,msg+" SCROLL_SIDEBAR_SET");
        postInvalidate();
    }

    public void setB(String[] b) {
        this.b = b;
    }

    public void setmOnTouchingLetterChangedListener(OnTouchingLetterChangedListener mOnTouchingLetterChangedListener) {
        this.mOnTouchingLetterChangedListener = mOnTouchingLetterChangedListener;
    }

    public interface OnTouchingLetterChangedListener{
        void onTouchingLetterChanged(String s);
    }
}
