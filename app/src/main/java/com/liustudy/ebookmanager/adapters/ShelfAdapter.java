package com.liustudy.ebookmanager.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.liustudy.ebookmanager.beans.EbookInfo;
import com.liustudy.ebookmanager.R;

import java.util.List;

public class ShelfAdapter extends BaseAdapter {
    private List<EbookInfo> mList;
    private Context mContext;
    private LayoutInflater mInflater;

    public ShelfAdapter(List<EbookInfo> list, Context context){
        mList = list;
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
    }
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mList.size() ;
    }

    @Override
    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        return mList.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return arg0;
    }

    @Override
    public View getView(int position, View contentView, ViewGroup arg2) {
        // TODO Auto-generated method stub
           contentView = mInflater.inflate(R.layout.ebook,null);
           TextView tv =  (TextView)contentView.findViewById(R.id.tv_book_name);
           if(position<mList.size()){
               //设置书籍名称
               tv.setText(mList.get(position).getName());
               //设置图片
               tv.setBackgroundResource(mList.get(position).getIv());
           }
               else{

           }
           return contentView;
    }
}

