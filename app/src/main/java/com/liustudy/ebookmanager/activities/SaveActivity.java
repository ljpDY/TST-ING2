package com.liustudy.ebookmanager.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.liustudy.ebookmanager.beans.Chapter;
import com.liustudy.ebookmanager.beans.Constant;
import com.liustudy.ebookmanager.beans.BookMark;
import com.liustudy.ebookmanager.R;
import com.liustudy.ebookmanager.utils.EbookReadUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//书架前往阅读界面的中转界面，从存储文件中提取已存储的内容，并且在首次打开时将目录对应的数据以字符串的显示储存
public class SaveActivity extends AppCompatActivity {

    private static final int TAG = 1 ;
    private static final int TAG2 = 2;
    private String path;
    private ImageView ivsave;
    private Handler handler ;
    private  int chapter,step;
    private ProgressBar pb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save);
        show();
        save();

        handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);

               switch (msg.what){
                   //处理来自子线程的消息，子线程结束时前往新界面
                   case TAG:go();
                        break;

                   case TAG2:
                       pb.setProgress((int)((long)msg.obj*100/new File(path).length()));
                        break;
               }

            }
        };
    }

    private void show(){
        ivsave = (ImageView)findViewById(R.id.iv_save);
        pb = (ProgressBar)findViewById(R.id.pb_save);

        Glide.with(SaveActivity.this).load(R.drawable.save).into(ivsave);
        Intent intent = getIntent();
        path = intent.getStringExtra("ebookpath");
    }

    private void save() {
        SharedPreferences sp =SaveActivity.this.getSharedPreferences("info", Context.MODE_PRIVATE);
        String allInfo = sp.getString(path,"");
        //如果提取的字符串长度大于0，表明已经存储过数据，直接前往下一个界面
        if(allInfo.length()>0){
            SharedPreferences gsp = SaveActivity.this.getSharedPreferences(Constant.CHAPTERSP, Context .MODE_PRIVATE);
            String str= gsp.getString(path,"0/0");
            String s[] = str.split("/");
            chapter = Integer.parseInt(s[0]);
            step = Integer.parseInt(s[1]);
            go();
        }
        else {
            //调用线程存储目录对应的文件
            new Thread() {
                @Override
                public void run() {
                    Looper.prepare();

                    //初始化变量
                    List<BookMark> bmlist = new ArrayList<BookMark>();
                    BookMark firsteu = new BookMark(0, 0L);//创建第一个书签
                    bmlist.add(firsteu);
                    int chapter_count = 0;//记录当前章节
                    Chapter c = null;

                    //提取第一章内容并跳过默认章节字数的内容
                    try {
                        c = new EbookReadUtils(SaveActivity.this).convertCodeAndGetText(path, firsteu.getOldsize()+Constant.CHAPTERBASESIZE);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                    //循环提取后续章节
                    while (c.getText() != null) {

                        chapter_count++;
                        //计算已提取的字数并添加默认跳过的字数
                        long oldsize = bmlist.get(chapter_count - 1).getOldsize() + c.getText().length() + c.getLines()+Constant.CHAPTERBASESIZE;
                        BookMark bm = new BookMark(chapter_count, oldsize);

                        //发送提取进度
                        if(chapter_count%10==0) {
                            Message message = handler.obtainMessage();
                            message.what = TAG2;
                            message.obj = oldsize;
                            handler.sendMessage(message);
                        }

                        bmlist.add(bm);

                        try {
                            c = new EbookReadUtils(SaveActivity.this).convertCodeAndGetText(path, bm.getOldsize()+Constant.CHAPTERBASESIZE);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                    //将提取的内容保存
                        StringBuffer sb = new StringBuffer();
                        for (BookMark bm : bmlist) {
                            sb.append(bm.getOldsize());
                            sb.append("/");
                        }
                        String allInfo = new String(sb);

                        SharedPreferences sp = getSharedPreferences(Constant.ALLINFO, MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString(path, allInfo);
                        editor.commit();

                        Message message = handler.obtainMessage();
                        message.what = TAG;
                        handler.sendMessage(message);

                        Looper.loop();
                    }
            }.start();
        }
    }

    private  void go(){
            Intent intent = new Intent(SaveActivity.this, ReadInActivity.class);
            intent.putExtra("ebookpath",path);
            intent.putExtra("chapter",chapter);
            intent.putExtra("step",step);
            startActivity(intent);
            finish();
    }
}