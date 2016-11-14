package com.apus.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.apus.bean.CitySortModel;
import com.apus.sortdemo.R;

import java.util.List;

/**
 * Created by sunmeng on 2016/11/11.
 */

public class SortAdapter extends BaseAdapter implements SectionIndexer{

    private List<CitySortModel> modelList = null;
    private Context mContext;

    public SortAdapter(List<CitySortModel> modelList, Context mContext) {
        this.modelList = modelList;
        this.mContext = mContext;
    }

    public void updateListView(List<CitySortModel> list){
        this.modelList = list;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return this.modelList.size();
    }

    @Override
    public Object getItem(int i) {
        return modelList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        final CitySortModel mContent = modelList.get(i);
        if (view == null){
            view = LayoutInflater.from(mContext).inflate(R.layout.model_item,viewGroup,false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }else {
            holder = (ViewHolder) view.getTag();
        }

        // 根据position获取分类的首字母的char ascii值
        int section = getSectionForPosition(i);
        // 如果当前位置等于该分类首字母的Char的位置，则认为是第一次出现
        if (i == getPositionForSection(section)){
            holder.model_title.setVisibility(View.VISIBLE);
            holder.model_title.setText(mContent.getSortLetters());
        }else
            holder.model_title.setVisibility(View.GONE);

        holder.model_content.setText(mContent.getCityName());
        return view;
    }

    @Override
    public Object[] getSections() {
        return new Object[0];
    }

    /***
     * 根据分类的首字母的char ascii值获取其第一次出现该首字母的位置
     * @param section
     * @return
     */
    @Override
    public int getPositionForSection(int section) {
        for (int i=0; i< getCount(); i++){
            String sortStr = modelList.get(i).getSortLetters();
            char firstChar = sortStr.toUpperCase().charAt(0);
            if (firstChar == section)
                return i;
        }
        return -1;
    }

    /**
     * 根据listView的当前位置设置获取分类的首字母的char ascii值
     * @param i
     * @return
     */
    @Override
    public int getSectionForPosition(int i) {
        return modelList.get(i).getSortLetters().charAt(0);
    }

    class ViewHolder{
        private TextView model_title, model_content;

        public ViewHolder(View itemView) {
            model_title = (TextView) itemView.findViewById(R.id.model_title);
            model_content = (TextView) itemView.findViewById(R.id.model_content);
        }
    }
}
