package com.liustudy.ebookmanager.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.GridView;

import com.liustudy.ebookmanager.beans.Constant;
import com.liustudy.ebookmanager.beans.EbookInfo;
import com.liustudy.ebookmanager.utils.GreenDaoManager;
import com.liustudy.ebookmanager.R;
import com.liustudy.ebookmanager.adapters.ShelfAdapter;

import java.util.List;
//书架界面
public class MainActivity extends AppCompatActivity {
    private GridView bookShelf;
    private List<EbookInfo> ebookInfoList;
    private GreenDaoManager mDaoManager;
    private ShelfAdapter adapter;
    private ShelfReceiver receiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        initView();
        setView();
    }
    //初始化组件和变量
    private void initView() {
        bookShelf = (GridView) findViewById(R.id.bookShelf);
        mDaoManager = GreenDaoManager.getInstance(MainActivity.this);
        ebookInfoList = mDaoManager.searchAll();
        adapter = new ShelfAdapter(ebookInfoList,MainActivity.this);
        receiver = new ShelfReceiver(MainActivity.this,ebookInfoList,adapter);
        receiver.register("com.liustudy.ebookmanager.reshelf");
        addmore(ebookInfoList);

    }
//添加用于跳转添加书籍界面的按钮，如果选择在其他位置添加该功能，除修改标红位置外还应修改监听器中的内容
    private void addmore(List<EbookInfo> list) {
        EbookInfo add = new EbookInfo("继续添加",null);
        add.setIv(R.drawable.add);
        list.add(add);
    }

    private void setView() {

        SharedPreferences sp = getSharedPreferences("info", Context.MODE_PRIVATE);
        int width = sp.getInt("width",0);
        bookShelf.setColumnWidth(width/3);


        bookShelf.setAdapter(adapter);
        //设置长按监听器，用于删除选中的书籍，根据一般需求删除选中的一本即可
        bookShelf.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //将选中书籍的底色改为黑色，实际效果根据需求修改
             view.setBackgroundColor(Color.BLACK);
             //获得选中的对象
             EbookInfo ef = (EbookInfo)parent.getItemAtPosition(position);
             //发送通知
               setAlertDialog(ef);
             //设置是否同时响应长点击和点击事件，设为true为不响应
                return true;
            }
        });

        bookShelf.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                // TODO Auto-generated method stub
                //点击的是添加新文件的位置，跳转添加文件界面
                 if(arg2==ebookInfoList.size()-1) {
                    Intent intent = new Intent(MainActivity.this, AddEbookActivity.class);
                    startActivity(intent);

                }
                 //点击的是正常文件的界面，携带文件路径跳转中转界面
                else{
                    Intent intent = new Intent(MainActivity.this, SaveActivity.class);
                    intent.putExtra("ebookpath",ebookInfoList.get(arg2).getPath());
                    startActivity(intent);

                }
            }
        });

    }


    private void setAlertDialog(final EbookInfo name) {
        AlertDialog .Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示").setMessage("是否删除选中的文本？").setPositiveButton("是", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mDaoManager.delete(name.getName());//根据名称删除数据库中的对应内容
                // adapter.notifyDataSetChanged()监听的是绑定的list指向的地址，new出来的list将修改地而无法通知adapter，必须通过clear和add的方式更新
                ebookInfoList.clear();
                List<EbookInfo> list = mDaoManager.searchAll();
                //同样记得添加按钮
                addmore(list);
                ebookInfoList.addAll(list);
                adapter.notifyDataSetChanged();
                //清除sp文件中的数据
                SharedPreferences sp = getSharedPreferences(Constant.ALLINFO,MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.remove(name.getPath());
                editor.commit();
            }
        }).setNegativeButton("否", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //选择否表明放弃操作
            }
        });
        builder.create().show();
    }

    //用于更新书架的广播接收者
    public class ShelfReceiver extends BroadcastReceiver{
            private Context context;
            private List<EbookInfo> list;
            private ShelfAdapter adapter;
            private GreenDaoManager daoManager;

           public ShelfReceiver(Context context,List<EbookInfo> list,ShelfAdapter adapter){
                this.context = context;
                this.list = list;
                this.adapter = adapter;
                daoManager = GreenDaoManager.getInstance(context);
            }
        //用于动态绑定广播
        public void register(String action) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(action);
            registerReceiver(this, filter);
        }

        //接收从添加新书籍界面发送的广播，后续操作同删除书籍操作
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals("com.liustudy.ebookmanager.reshelf")){
                    List<EbookInfo> nlist = daoManager.searchAll();
                    addmore(nlist);
                    list.clear();
                    list.addAll(nlist);
                    adapter.notifyDataSetChanged();
                }
            }


        }

//动态绑定广播后必须的解绑操作，否则可能导致oom
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }
}
