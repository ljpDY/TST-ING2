package com.liustudy.ebookmanager.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.liustudy.ebookmanager.R;

import java.util.List;

public class PageAdapter extends BaseAdapter {
    private List<Bitmap> bms;
    private Context context;
    private LayoutInflater inflater;
    public PageAdapter(Context context,List<Bitmap> bms){
        this.context = context;
        this.bms = bms;
        inflater = LayoutInflater.from(context);
    }
    @Override
    public int getCount() {
        return bms.size();
    }

    @Override
    public Object getItem(int position) {
        return bms.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.page,null);
        ImageView iv = convertView.findViewById(R.id.iv_page);
        iv.setImageBitmap(bms.get(position));
        return convertView;
    }
}
