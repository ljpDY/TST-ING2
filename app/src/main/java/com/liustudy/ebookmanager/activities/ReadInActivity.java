package com.liustudy.ebookmanager.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;

import com.liustudy.ebookmanager.adapters.PageAdapter;
import com.liustudy.ebookmanager.beans.BookPageMakeHelper;
import com.liustudy.ebookmanager.beans.Chapter;
import com.liustudy.ebookmanager.beans.Constant;
import com.liustudy.ebookmanager.utils.BookPageFactory;
import com.liustudy.ebookmanager.utils.EbookReadUtils;
import com.liustudy.ebookmanager.utils.GreenDaoManager;
import com.liustudy.ebookmanager.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
//用于展示选中书籍的单章内容，仅用于竖屏显示，可限制为仅竖屏或添加更多判断方法
public class ReadInActivity extends Activity {
    //handler中用于区分message的标志
    private static final int TAG = 1;
    private static final int TAG4 = 4;

    private int width,height;
    private int step ;
    private GreenDaoManager daoManager;
    private EbookReadUtils eutils;
    private String mPath;
    private List<String> olist;
    private List<Bitmap> bms;
    private PageAdapter adapter;
    private int chapter;
    private ReadReceiver readReceiver;
    private Handler handler;
    private volatile boolean flag;//用于结束死循环线程的flag
    private List<Integer> useds,lastUseds;
    private BookPageMakeHelper bh ;
    private String time,electric_quantity;
    private com.liustudy.ebookmanager.views.PageView pv;

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
        pv = (com.liustudy.ebookmanager.views.PageView)findViewById(R.id.pv);

        useds = new ArrayList<Integer>();
        lastUseds = new ArrayList<Integer>();
        useds.add(0);
        bh = new BookPageMakeHelper(null,false,useds);
        bms = new ArrayList<Bitmap>();
        adapter = new PageAdapter(ReadInActivity.this,bms);
        daoManager = GreenDaoManager.getInstance(ReadInActivity.this);
        eutils = new EbookReadUtils(this);

        SharedPreferences sharedPreferences = ReadInActivity.this.getSharedPreferences(Constant.PHONEINFO,MODE_PRIVATE);
        width = sharedPreferences.getInt("width",0);
        height = sharedPreferences.getInt("height",0);
        height=1280;
        readReceiver = new ReadReceiver();
        readReceiver.register("com.liustudy.ebookmanager.reread");
        readReceiver.register(Intent.ACTION_BATTERY_CHANGED);


        flag = false;

        handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);

                switch (msg.what) {
                    case TAG:
                        try {
                            step=0;
                            setPage(mPath,chapter);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    case TAG4://处理时间线程发来的消息
                        Calendar calendar = Calendar.getInstance();//每一刻的instance都不一样，应该执行该操作
                        final int hour = calendar.get(Calendar.HOUR_OF_DAY);
                        int minute = calendar.get(Calendar.MINUTE);
                        pv.setTime(String.valueOf(hour)+":"+String.valueOf(minute));
                        pv.invalidate();
                        break;
                }
            }
        };
   }

        @SuppressLint("ClickableViewAccessibility")
        private void setView() {


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



            int temp = step;
            step = -1;
            bh = new BookPageMakeHelper(null,false,useds);
            for(int i=0;i<temp+1;i++) {
                try {
                    step++;
                    setPage(mPath, chapter);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            pv.setBitmap(bh.getBitmap());

        pv.setOnTouchListener(new NestedScrollView.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction()==MotionEvent.ACTION_UP&&event.getX()>(width/2+40)&&event.getX()>(width/2-40)&&event.getY()>(height/2+40)&&event.getY()>(height/2-40)){
                    //将路径和章节数反馈给目录界面，然后打开

                Intent intent1 = new Intent(ReadInActivity.this, ShowLIstActivity.class);
                intent1.putExtra("path",path);
                intent1.putExtra("chapter",chapter);
                startActivity(intent1);

                }
                //如果点击的是屏幕下方
               else if(event.getAction()==MotionEvent.ACTION_UP&&event.getY()>(height/2)) {

                    if(!bh.isEnd()){
                        try {
                            step++;
                            setPage(path,chapter);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    //当前章节结束，但还有后续章节
                    else if(chapter<olist.size()){
                        try {
                            Log.i("READ","222222222222");
                            step=0;
                            chapter+=1;
                            bh.recycle();
                            bh = new BookPageMakeHelper(null,false,useds);
                            setPage(path,chapter);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }
                //如果点击的是屏幕上方
                else if(event.getAction()==MotionEvent.ACTION_UP&&event.getY()<height){

                    //如果还没有跳转到开头
                      if(step!=0){
                          step--;
                          try {
                              setPage(path,chapter);
                          } catch (IOException e) {
                              e.printStackTrace();
                          }
                      }

                      //如果已经处于开头并且还有上一章，点击后将flag2设为true，保证显示的是上一章的最后一页
                      else if(chapter!=0) {
                          chapter-=1;
                          step = -1;
                          bh = new BookPageMakeHelper(null,false,useds);
                          try {
                              while (!bh.isEnd()) {
                                  step++;
                                  setPage(path, chapter);
                              }
                          } catch (IOException e) {
                              e.printStackTrace();
                          }
                      }

                }
                //返回true表明不会对滑动屏幕作出响应
                return true;
            }
        });








        //电量显示功能
        Intent intent1 = registerReceiver(readReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        pv.setElectric_quantity(String.valueOf(intent1.getIntExtra(BatteryManager.EXTRA_LEVEL,100)));
        pv.invalidate();
        //日期显示功能
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        pv.setTime(String.valueOf(hour)+":"+String.valueOf(minute));
        pv.invalidate();

        //用于刷新时间的线程
        new Thread(){
            @Override
            public void run() {
                super.run();
                //用于与系统时间同步的死循环，当系统时间秒数为0时退出，或者关闭当前界面时退出
                for(;;){

                    if(flag) {
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

                        if(flag) {
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
      Chapter chapter1 = eutils.convertCodeAndGetText(path,Integer.parseInt(olist.get(chapter)));
      String text = chapter1.getText();
      bh=new BookPageFactory(width,height,bh,Constant.WORDSIZE1).drawPage(text,step);
      pv.setBitmap(bh.getBitmap());
        pv.invalidate();
    }

//关闭界面时，刷新保存的内容，解绑广播接收者，并且将退出标志设为true
    @Override
    protected void onDestroy() {
        reSp();
        unregisterReceiver(readReceiver);
        flag = true;
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
                pv.setElectric_quantity(String.valueOf(intent.getIntExtra(BatteryManager.EXTRA_LEVEL,100)));
               }


        }


    }




}

