package com.liustudy.ebookmanager.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
;
import android.graphics.Color;
import android.graphics.DrawFilter;
import android.graphics.Paint;
import android.text.BoringLayout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;

import com.liustudy.ebookmanager.beans.BookPageMakeHelper;


import java.util.ArrayList;
import java.util.List;


public class BookPageFactory {
    private int pageWidth;
    private int pageHeight;
    private TextPaint textPaint;
    private BookPageMakeHelper bh;
    private float textSize;

    public BookPageFactory(int pageWidth, int pageHeight,BookPageMakeHelper bh,float textSize) {
        this.pageWidth = pageWidth;
        this.pageHeight = pageHeight;
        this.textSize = textSize;
        textPaint = new TextPaint();
        textPaint.setColor(Color.BLACK);
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(textSize);
        this.bh=bh;
    }

    public BookPageMakeHelper drawPage(String str,int index) {
        Bitmap bitmap = Bitmap.createBitmap(pageWidth, pageHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        List<Integer> useds = new ArrayList<Integer>();
        useds.addAll(bh.getlist());
        Log.i("TAG",String.valueOf(useds.size()));
        int temp = bh.getUsed(index);
        str = str.substring(bh.getUsed(index));

        boolean flag = false;
        for(int i=0;i<pageHeight/textSize;i++){
           drawLine(canvas,str,textSize*i);
           temp+=getUsed(str);
           str = str.substring(getUsed(str));
            if (str.length()==0){
                flag = true;

                break;
            }
        }
        if(temp>useds.get(useds.size()-1));{
            useds.add(temp);
            Log.i("TAG",String.valueOf(useds.get(index+1)));
        }
        return new BookPageMakeHelper(bitmap,flag,useds);
    }


    private void drawLine(Canvas canvas,String str,float textSize){
        Paint.FontMetrics FontMetrics= textPaint.getFontMetrics();
        String text = str.substring(0, getUsed(str));
        canvas.drawText(text, 0,textSize-FontMetrics.top-FontMetrics.bottom, textPaint);
    }

    private int getUsed (String str){
        int subIndex = 0;
        if(str.contains("\n")){
            subIndex = textPaint.breakText(str, 0, str.indexOf("\n")+1, true, pageWidth, null);
        }
        else {
            subIndex = textPaint.breakText(str, 0, str.length(), true, pageWidth, null);
        }
        return subIndex;
    }


}
