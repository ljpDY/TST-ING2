package com.liustudy.ebookmanager.beans;

import android.content.Context;

import com.liustudy.ebookmanager.utils.EbookReadUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
//书签
public class BookMark {
    private int chapter;//章节号
    private long oldsize;//章节对应的skip数字
    private int step;//步长


    public BookMark(int chapter, long oldsize) {
        this.chapter = chapter;
        this.oldsize = oldsize;
    }

    public BookMark(int chapter, long oldsize , int step) {
        this.chapter = chapter;
        this.oldsize = oldsize;
        this.step = step;
    }

    public int getChapter() {
        return chapter;
    }

    public void setChapter(int chapter) {
        this.chapter = chapter;
    }

    public long getOldsize() {
        return oldsize;
    }

    public void setOldsize(long oldsize) {
        this.oldsize = oldsize;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public BookMark() {
    }

}
