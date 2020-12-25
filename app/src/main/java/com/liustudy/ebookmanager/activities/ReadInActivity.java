package com.liustudy.ebookmanager.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;

import com.liustudy.ebookmanager.beans.Chapter;
import com.liustudy.ebookmanager.beans.Constant;
import com.liustudy.ebookmanager.utils.EbookReadUtils;
import com.liustudy.ebookmanager.utils.GreenDaoManager;
import com.liustudy.ebookmanager.R;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
//用于展示选中书籍的单章内容，仅用于竖屏显示，可限制为仅竖屏或添加更多判断方法
public class ReadInActivity extends Activity {
    //handler中用于区分message的标志
    private static final int TAG = 1;
    private static final int TAG2 = 2;
    private static final int TAG3 = 3;
    private static final int TAG4 = 4;

    private TextView showebook ,showblank,tveq,tvtime;
    private NestedScrollView scrollView;
    private Button btlist;
    private int step ,height,maxheight,scrollheight;//步长，书籍界面的高度，用于更好展示书籍得到的最大高度（用于确认步长）
    private GreenDaoManager daoManager;
    private EbookReadUtils eutils;
    private String mPath;
    private List<String> olist;
    private int chapter;
    private ReadReceiver readReceiver;
    private Handler handler;
    private volatile boolean flag , flag2 ,flag3;//避免多次刷新界面的flag，用于判断是否返回上一章，用于结束死循环线程的flag3

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_read_in);

        initView();
        setView();

    }

    @SuppressLint("HandlerLeak")
    private void initView() {
        showebook = (TextView)findViewById(R.id.show_ebook);
        showblank = (TextView)findViewById(R.id.tv_blank);
        tveq = (TextView)findViewById(R.id.tv_en);
        tvtime = (TextView)findViewById(R.id.tv_time);
        scrollView = (NestedScrollView)findViewById(R.id.scrollView);
        btlist = (Button)findViewById(R.id.bt_list);
        daoManager = GreenDaoManager.getInstance(ReadInActivity.this);
        eutils = new EbookReadUtils(this);
        height = 0;
        maxheight = 0;
        scrollheight=0;

        SharedPreferences sp = ReadInActivity.this.getSharedPreferences(Constant.PHONEINFO, Context .MODE_PRIVATE);
        int phoneheight = sp.getInt("height",0);//提取手机高度

        //获得一个合适的界面高度
        for(;;){
            scrollheight+=Constant.WORDSIZE1;
            if(phoneheight-scrollheight<100){
                scrollheight-=Constant.WORDSIZE1;
                break;
            }
        }

        readReceiver = new ReadReceiver();
        readReceiver.register("com.liustudy.ebookmanager.reread");
        readReceiver.register(Intent.ACTION_BATTERY_CHANGED);

        flag2 = false;
        flag3 = false;

        handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);

                switch (msg.what) {

                    case TAG://处理广播发来的消息，源头来自目录界面的操作
                        try {
                            setPage(mPath, chapter);
                            step = 0;
                            scrollView.scrollTo(0, step);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;

                    case TAG2://处理书籍界面绘制好后发来的消息
                        if(flag) {
                            height = showebook.getMeasuredHeight();//获得绘制的高度
                            maxheight = (height % 1280 == 0) ? height : 1280 * (height / 1280 + 1);//保证具体展示的界面为1280的倍数
                            flag=false;//仅当章节数修改或初次打开时绘制后flag为true，修改为false后避免其他情况下重复之前的操作
                            if (flag2) {//当操作为回到上一章时，进行下面的操作
                                Message m = handler.obtainMessage();
                                m.what = TAG3;
                                handler.sendMessage(m);
                            }
                        }
                        //获取完数据后调整展示内容
                        scrollView.scrollTo(0,step);
                        break;

                    case TAG3:
                        //将步长设为展示最后一页内容
                        step = (maxheight==height)?maxheight:maxheight-scrollheight;
                        //跳转到改位置
                        scrollView.scrollTo(0,step);
                        flag2=false;//修改flag2，避免前往下一章是也执行该操作
                        break;

                    case TAG4://处理时间线程发来的消息
                        Calendar calendar = Calendar.getInstance();//每一刻的instance都不一样，应该执行该操作
                        final int hour = calendar.get(Calendar.HOUR_OF_DAY);
                        int minute = calendar.get(Calendar.MINUTE);
                        tvtime.setText(String.valueOf(hour)+":"+String.valueOf(minute));
                        break;
                }
            }
        };
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setView() {
        LinearLayout.LayoutParams ll = (LinearLayout.LayoutParams) scrollView.getLayoutParams();
        ll.height = scrollheight;//设置滚动条高度

        showebook.setTextSize(Constant.WORDSIZE1);//设置字体大小

         Intent intent = getIntent();
         final String path = intent.getStringExtra("ebookpath");
         step = intent.getIntExtra("step",0);
         chapter = intent.getIntExtra("chapter",0);
         mPath = path;//path定义为final，为避免麻烦，要再次使用的话，将其复制一份

        //通过path将“info”文件下对于的字符串提取出来
        SharedPreferences sp2 = ReadInActivity.this.getSharedPreferences(Constant.ALLINFO, Context .MODE_PRIVATE);
        String allInfo = sp2.getString(path,"");
        //通过“/”作为分隔符将字符串分割为ArrayList
        olist = Arrays.asList(allInfo.split("/"));

        try {
            setPage(path,chapter);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //如果有记录的步长，将跳转到该位置，相当于书签
        scrollView.scrollTo(0,step);

        scrollView.setOnTouchListener(new NestedScrollView.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //如果点击的是屏幕下方
                if(event.getAction()==MotionEvent.ACTION_UP&&event.getY()>(scrollheight/2)) {

                    //step+1280<height相当于当前章节未结束，可以继续相同的操作
                    if(step+scrollheight<height){
                        step+=scrollheight;
                        scrollView.scrollTo(0,step);
                        Log.i("GET",String.valueOf(height)+"-------"+String.valueOf(step));
                    }

                    //当前章节结束，但还有后续章节
                    else if(chapter<olist.size()){
                        try {
                            chapter+=1;
                            step=0;
                            setPage(path,chapter);
                            scrollView.scrollTo(0,step);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }
                //如果点击的是屏幕上方
                else if(event.getAction()==MotionEvent.ACTION_UP&&event.getY()<scrollheight/2){

                    //如果还没有跳转到开头
                      if(step!=0){
                          step-=scrollheight;
                          scrollView.scrollTo(0,step);
                      }

                      //如果已经处于开头并且还有上一章，点击后将flag2设为true，保证显示的是上一章的最后一页
                      else if(chapter!=0) {
                          chapter-=1;
                          try {
                              setPage(path,chapter);
                              flag2 = true;//进行跳转上一章的操作，将flag2设为true
                          } catch (IOException e) {
                              e.printStackTrace();
                          }
                      }

                }
                //返回true表明不会对滑动屏幕作出响应
                return true;
            }
        });


        //将路径和章节数反馈给目录界面，然后打开
        btlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(ReadInActivity.this, ShowLIstActivity.class);
                intent1.putExtra("path",path);
                intent1.putExtra("chapter",chapter);
                startActivity(intent1);
            }
        });

//在文本显示界面绘制好后发送message，保证得到的是正确的值
        showebook.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if(flag){
                    Message msg = handler.obtainMessage();
                    msg.what = TAG2;
                    handler.sendMessage(msg);
                }
            }
        });

        //根据组件设置的字体大小，生成一条足够长的字符串用于填满至少一个屏幕
        String str = "\n"+"\n"+"\n"+"\n"+"\n"+"\n"+"\n"+"\n"+"\n"+"\n"+"\n"+"\n"+"\n"+"\n"+"\n"+"\n"+"\n"+"\n"+"\n"+"\n"+"\n"+"\n"+"\n"+"\n"+"\n"+"\n"+"\n"+"\n"+"\n"+"\n"+"\n"+"\n"+"\n";
        showblank.setText(str);

        //电量显示功能
        Intent intent1 = registerReceiver(readReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        tveq.setText(String.valueOf(intent1.getIntExtra(BatteryManager.EXTRA_LEVEL,100)));

        //日期显示功能
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        tvtime.setText(String.valueOf(hour)+":"+String.valueOf(minute));

        //用于刷新时间的线程
        new Thread(){
            @Override
            public void run() {
                super.run();
                //用于与系统时间同步的死循环，当系统时间秒数为0时退出，或者关闭当前界面时退出
                for(;;){

                    if(flag3) {
                        break;
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    Calendar calendar = Calendar.getInstance();
                    if(calendar.get(Calendar.SECOND)==0){
                       break;
                    }
                }

                //用于通知handler修改时间，每隔一分钟触发一次
                for (; ; ) {
                    try {

                        if(flag3) {
                            break;
                        }

                        Message message = handler.obtainMessage();
                        message.what = TAG4;
                        handler.sendMessage(message);
                        Thread.sleep(60000);

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();

    }





    //用于第一次加载或者加载新章节的方法
    public void setPage(String path, int chapter) throws IOException {
        flag = true;//表明第一次加载或者加载新的章节
        Long oldsize = Long.parseLong(olist.get(chapter));
        Chapter c = new EbookReadUtils(ReadInActivity.this).convertCodeAndGetText(path, oldsize);
        showebook.setText(c.getText());
    }

//关闭界面时，刷新保存的内容，解绑广播接收者，并且将退出标志设为true
    @Override
    protected void onDestroy() {
        reSp();
        unregisterReceiver(readReceiver);
        flag3 = true;
         super.onDestroy();
    }

    //刷新所读文件章节和步长，相当于存书签
    private void reSp(){
        SharedPreferences sp = getSharedPreferences(Constant.CHAPTERSP, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        String str = String.valueOf(chapter)+"/"+String.valueOf(step);
        editor.putString(mPath, str);
        editor.commit();
    }

    //处理章节选择以及时间显示的广播接收者
    public class ReadReceiver extends BroadcastReceiver {
        public ReadReceiver(){
        }

        public void register(String action) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(action);
            registerReceiver(this, filter);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("com.liustudy.ebookmanager.reread")){
                    chapter = intent.getIntExtra("chapter",0);
                    Message msg = handler.obtainMessage();
                    msg.what = TAG;
                    handler.sendMessage(msg);
            }

            if(intent.getAction()==Intent.ACTION_BATTERY_CHANGED){
                tveq.setText(String.valueOf(intent.getIntExtra(BatteryManager.EXTRA_LEVEL,100)));
               }


        }


    }



}

