package com.liustudy.ebookmanager.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.liustudy.ebookmanager.R;
import com.liustudy.ebookmanager.adapters.ChapterListAdapter;

import java.io.IOException;
//展示文本中提取出来的章节目录
public class ShowLIstActivity extends AppCompatActivity {
    private ListView chapter_list;
    private int chapter;//章节,用于目录绘制好后跳转（省略）
    private String path;//文本路径，同时作为sp文件的key值
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_list);

            initView();
        try {
            setView();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //初始化组件和变量
    private void initView()
    {
        Intent intent = getIntent();
        path = intent.getStringExtra("path");
        chapter = intent.getIntExtra("chapter",0);
        chapter_list = (ListView)findViewById(R.id.list_chapter);
    }

    //设置适配器和监听
    private void setView() throws IOException {

        ChapterListAdapter adapter = new ChapterListAdapter(ShowLIstActivity.this,path);
        chapter_list.setAdapter(adapter);
        chapter_list.smoothScrollToPositionFromTop(chapter,0);
        chapter_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //将章节设为点击的章节
                chapter = position;
                finish();
            }
        });
    }

    //finish时发送更新阅读界面的广播，并将章节数发送
    @Override
    protected void onDestroy() {
        Intent intent1 = new Intent();
        intent1.putExtra("chapter",chapter);
        intent1.setAction("com.liustudy.ebookmanager.reread");
        sendBroadcast(intent1);
        super.onDestroy();
    }
}