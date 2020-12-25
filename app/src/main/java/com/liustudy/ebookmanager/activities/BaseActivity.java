package com.liustudy.ebookmanager.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;

import com.liustudy.ebookmanager.R;
import com.liustudy.ebookmanager.beans.Constant;

import java.util.ArrayList;
import java.util.List;
//用于申请权限，根据需求可以作为冷启动过度界面
public class BaseActivity extends AppCompatActivity {

    private List<String> permissionList;//权限列表

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        initPermission();
        getPermission();
        saveInfo();
    }
//根据权限获取状态向权限列表添加未通过的权限，读写权限为同一权限，可仅申请一个
    private void initPermission() {
        permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    private void getPermission(){
        //判断是否有未通过的权限
        if(permissionList.size()>0){
            //将未通过的权限转化为数组并发出请求，requestCode与onRequestPermissionsResult对应
            String[] permissions = (String[]) permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(BaseActivity.this, permissions, 1);
        }else {
            //如果权限全部同过则跳转下一个界面，并关闭该界面，也可以根据需求进行一定时间的停留
            Intent intent = new Intent(BaseActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
    //对用户操作进行反馈
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            //判断系统版本是否大于6.0，可前置
           if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
               //判断第一个权限申请结果，有多余权限可继续添加
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    //权限未通过，通知用户手动改为允许（具体根据该权限的必要性决定，也可以在必须要使用该权限是另行通知）
                    showDialog();
                } else {
                    //权限全部通过，进行上一个方法中同样的操作，根据需求可独立成一个方法
                    Intent intent = new Intent(BaseActivity.this,MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }
    }

    private void showDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("存储权限不可用").setMessage("前往设置界面手动设置，拒绝将退出").setPositiveButton("前往设置", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
          //用户接受手动设置，跳转设置界面
                goToAppSetting();
            }
        }).setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });//用户拒绝根据权限必要性关闭软件或运行用户继续使用

        builder.create().show();
    }

    private void goToAppSetting(){
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);//根据手机系统进行调整，部分手机设置界面无权限设置功能
        //用于跳转到改软件设置界面
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
        finish();
    }
//保存手机信息
    private void saveInfo(){
        Resources resources = this.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        float density = dm.density;
        int width = dm.widthPixels;
        int height = dm.heightPixels;

        SharedPreferences sp = getSharedPreferences(Constant.PHONEINFO, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("width", width);
        editor.putInt("height",height);
        editor.commit();


    }

}