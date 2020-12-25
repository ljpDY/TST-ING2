package com.liustudy.ebookmanager.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.liustudy.ebookmanager.beans.EbookInfo;
import com.liustudy.ebookmanager.utils.GreenDaoManager;
import com.liustudy.ebookmanager.R;

import java.io.File;
import java.util.List;

public class AddAdapter extends BaseAdapter {

    private List<EbookInfo> mlist;
    private LayoutInflater minflater;
    private Context context;
    private GreenDaoManager mDaoManager;

    public AddAdapter(Context context, List<EbookInfo> list){
        this.context = context;
        mDaoManager = GreenDaoManager.getInstance(context);
        mlist = list;
        minflater = LayoutInflater.from(context);
    }
    @Override
    public int getCount() {
        return mlist.size();
    }

    @Override
    public Object getItem(int position) {
        return mlist.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = minflater.inflate(R.layout.ebookinfolayout,
                    null);
            holder = new ViewHolder();
            holder.view1 = (ImageView) convertView.findViewById(R.id.iv_add_icon);
            holder.view2 = (TextView) convertView.findViewById(R.id.tv_add_name);
            holder.view3 = (TextView) convertView.findViewById(R.id.tv_size);
            holder.view4 = (TextView)convertView.findViewById(R.id.tv_flag);
            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder) convertView.getTag();
        }
        EbookInfo ei = mlist.get(position);
        //设置图片
        holder.view1.setImageResource(ei.getIv());
        //设置名称
        holder.view2.setText(ei.getName());
        //设置文件大小
        holder.view3.setText(getSize((int) new File(ei.getPath()).length()));
        //设置文件添加状态
        if(mDaoManager.searchByWhere(ei.getName())==null){
            holder.view4.setText("添加");
        }
        else{
            holder.view4.setText("已添加");
        }

        return convertView;
    }

    static class ViewHolder{
        ImageView view1;
        TextView view2;
        TextView view3;
        TextView view4;
    }
//根据需求生成对应文本大小的字符串
    public String getSize(int size){
        return String.valueOf(size/1024/1024)+"MB";
    }
}
