package com.apus.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.apus.bean.AppInfo;
import com.apus.sortdemo.R;

import java.util.List;
import java.util.zip.Inflater;

/**
 * Created by sunmeng on 2016/11/15.
 */

public class AppAdapter extends BaseAdapter {

    private List<AppInfo> appList;
    private LayoutInflater mInflater;

    public AppAdapter(Context context, List<AppInfo> appList) {
        this.appList = appList;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return appList.size();
    }

    @Override
    public Object getItem(int position) {
        return appList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null){
            convertView = mInflater.inflate(R.layout.app_item,parent,false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }else
            holder = (ViewHolder) convertView.getTag();

        AppInfo appInfo = appList.get(position);
        holder.app_icon.setImageDrawable(appInfo.getAppIcon());
        holder.app_name.setText(appInfo.getAppName());
        return convertView;
    }

    class ViewHolder{
        private ImageView app_icon;
        private TextView app_name;

        public ViewHolder(View itemView) {
            app_icon = (ImageView) itemView.findViewById(R.id.app_icon);
            app_name = (TextView) itemView.findViewById(R.id.app_name);
        }
    }
}
