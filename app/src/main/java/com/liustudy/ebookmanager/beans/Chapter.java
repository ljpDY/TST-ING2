package com.liustudy.ebookmanager.beans;

public class Chapter {
    private String text ="";//正文内容
    private int lines =0;//为了使数据对应设置的属性，大致上相当于行数

    public int getLines() {
        return lines;
    }

    public void setLines(int lines) {
        this.lines = lines;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
