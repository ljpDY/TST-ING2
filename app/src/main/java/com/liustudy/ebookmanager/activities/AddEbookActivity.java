package com.liustudy.ebookmanager.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.liustudy.ebookmanager.beans.EbookInfo;
import com.liustudy.ebookmanager.utils.EbookReadUtils;
import com.liustudy.ebookmanager.utils.GreenDaoManager;
import com.liustudy.ebookmanager.adapters.AddAdapter;
import com.liustudy.ebookmanager.R;

import java.util.List;
//用于添加新书籍的activity
public class AddEbookActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private ListView book_list_view;
    private List<EbookInfo> ebookInfoList;
    private GreenDaoManager mDaoManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ebook);
        initView();
        setView();
    }


    private void initView() {
        //初始化listview
        book_list_view = (ListView)findViewById(R.id.lv_add_eook);
        //利用EbookReadUtils的getEbookList()方法获取list
        ebookInfoList = new EbookReadUtils(AddEbookActivity.this).getEbookList();
        //初始化数据库管理器
        mDaoManager = GreenDaoManager.getInstance(AddEbookActivity.this);
    }

    private void setView() {
        //初始化并设置list需要的配置器
        AddAdapter adapter = new AddAdapter(AddEbookActivity.this,ebookInfoList);
        book_list_view.setAdapter(adapter);
        //设置监听器
        book_list_view.setOnItemClickListener(this);
    }



    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //根据位置获得EbookInfo对象
        final EbookInfo ebookInfo = (EbookInfo)parent.getItemAtPosition(position);
        //点击书籍时更新时间
        ebookInfo.setTime(System.currentTimeMillis());
        //向数据库插入改对象
        mDaoManager.insertOrReplace(ebookInfo);
        Toast.makeText(AddEbookActivity.this,"添加成功！",Toast.LENGTH_SHORT).show();
        //更新书籍的添加状态，通过position可以恢复listview进度
        setView();
    }

    @Override
    //关闭界面时通知书架刷新
    protected void onDestroy() {
        Intent intent = new Intent();
        intent.setAction("com.liustudy.ebookmanager.reshelf");
        sendBroadcast(intent);
        super.onDestroy();
    }
}