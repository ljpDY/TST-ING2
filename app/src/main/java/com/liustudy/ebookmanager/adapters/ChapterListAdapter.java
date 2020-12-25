package com.liustudy.ebookmanager.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.liustudy.ebookmanager.utils.EbookReadUtils;
import com.liustudy.ebookmanager.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
//目录的适配器

    public class ChapterListAdapter extends BaseAdapter {

        private List<String> olist;
        private String path;
        private LayoutInflater mInflater;
        private Context mContext;

        public ChapterListAdapter(Context context,String path) throws IOException {
            this.path = path;
            this.mContext = context;
            //从存储文件中提取章节数对应的数据，通过分割生成字符串列表
            SharedPreferences sp = mContext.getSharedPreferences("info", Context .MODE_PRIVATE);
            String allInfo = sp.getString(path,"");
            olist = Arrays.asList(allInfo.split("/"));
            mInflater = LayoutInflater.from(context);
        }


        @Override
        public int getCount() {
            return olist.size()-1;
        }

        @Override
        public Object getItem(int position) {
            return olist.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = mInflater.inflate(R.layout.chapter,null);
            TextView tv = convertView.findViewById(R.id.tv_chapter);
            try {
                //利用工具类生成符合文件编码格式的reader，通过skip将reader移动到章节标题的位置
                BufferedReader reader = new EbookReadUtils(mContext).getBufferedReader(new File(path));
                reader.skip(Long.parseLong(olist.get(position)));
                //提取章节名称
                String title = reader.readLine();
                //部分文件章节格式与预设的格式不符，满足条件则直接设为章节名称
                if((title.startsWith("第")&&title.contains("章"))||title.length()<10){
                    tv.setText(quickcut(title,17));
                }
                else{
                    //格式不符则根据需求设置名称
                    tv.setText("章节("+position+")");
                }
                //关闭reader
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return convertView;
        }

        /**
         *
         * @param str 标题名称
         * @param max 目录字数限制
         * @return 适配后标题
        */
        private String quickcut(String str,int max){
            if(str.length()<=max){
                return str;
            }
            else{
                   String result = str.substring(0,max);
                   return  result;
            }
        }

    }

