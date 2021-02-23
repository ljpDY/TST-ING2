package com.liustudy.ebookmanager.beans;

import android.graphics.Bitmap;

import java.util.List;

public class BookPageMakeHelper {
    private Bitmap bitmap;
    private volatile boolean end ;
    private List<Integer> useds;

    public boolean isEnd() {
        return end;
    }

    public BookPageMakeHelper(Bitmap bitmap, boolean end,List<Integer> list) {
        this.bitmap = bitmap;
        this.end = end;
        this.useds = list;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }


    public  void recycle(){
        if(bitmap!=null||!bitmap.isRecycled())
        bitmap.recycle();
    }

    public int getUsed(int index){
        return useds.get(index);
    }

    public List<Integer> getlist(){
        return useds;
    }

}
