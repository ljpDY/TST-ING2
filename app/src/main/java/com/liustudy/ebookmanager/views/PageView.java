package com.liustudy.ebookmanager.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.EventLog;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;


import com.liustudy.ebookmanager.R;

import androidx.annotation.Nullable;

public class PageView extends View {
    private Bitmap bitmap;
    private float textSize;
//    private String title;
    private Animation animation;
    private String time;
    private String  electric_quantity;
    private Paint mPaint;
    private boolean upClick,downClick,menuClick;

    public PageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs,R.styleable.PageView);
        bitmap = BitmapFactory.decodeResource(getResources(),R.styleable.PageView_page);
//        title = a.getString(R.styleable.PageView_title);
        electric_quantity = a.getString(R.styleable.PageView_electric_quantity);
        time = a.getString(R.styleable.PageView_time);
        textSize = a.getFloat(R.styleable.PageView_android_textSize,40);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint = new Paint();
        mPaint.setTextSize(textSize);
        mPaint.setAntiAlias(true);
        int width = getWidth();
        int height = getHeight();
        int bitmapHight = bitmap.getHeight();
        canvas.drawBitmap(bitmap,0.f,0.f,mPaint);
        int drawHeight = (height-bitmapHight)/2+bitmapHight;
        canvas.drawText("电量：",0.f,drawHeight,mPaint);
        canvas.drawText(electric_quantity,textSize*3,drawHeight,mPaint);
        canvas.drawText(time,width-(textSize*time.length()/2),drawHeight,mPaint);
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public void setElectric_quantity(String electric_quantity) {
        this.electric_quantity = electric_quantity;
    }

    public void setTime(String time){
        this.time = time;
    }

//    public void downAnimation(MotionEvent e){
//        float x = e.getX();
//        float y = e.getY();
//        Path p = getPath(x,y);
//        Canvas canvas = new Canvas();
//        Paint paint = new Paint();
//        paint.setColor(Color.BLACK);
//        Log.i("DRAW","wohuale-------------------");
//        canvas.drawPath(p,paint);
//    }
//
//    public void upAnimation(){
//
//    }
//
//    public void resetAnimation(MotionEvent e){
//
//    }
//
//    public void moveAnimation(MotionEvent e){
//
//    }

//    public Path getPath(float x,float y){
//        int width = bitmap.getWidth();
//        int height = bitmap.getHeight();
//        Path p = new Path();
////        if(y>height/2-20&&y<height/2-20){
////
////        }
//        p.moveTo(x,0);
//        p.lineTo(x,height);
//        p.lineTo(x+(width-x)/2,height);
//        p.lineTo(x+(width-x)/2,0);
//        p.close();
//
//        return p;
//    }
}
