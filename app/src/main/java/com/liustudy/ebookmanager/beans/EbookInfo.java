package com.liustudy.ebookmanager.beans;

import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.liustudy.ebookmanager.R;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Transient;
import org.greenrobot.greendao.annotation.Unique;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class EbookInfo {
    @Id(autoincrement = true)
    private Long id;//greendao规定的必要id
    @Unique
    @Property(nameInDb = "name")//书籍名称
    private String name;
    @Unique
    @Property(nameInDb = "path")//书籍路径，必须设为唯一，指@Unique
    private String path;
    @Property(nameInDb = "time")//书籍存储的时间，方便排序
    private Long time;
    @Property(nameInDb = "step")//效率太低，已经无效，但是删除比较麻烦
    private int step;
    @Transient
    private int iv =  R.drawable.ebook;//书籍基本图片


    @Generated(hash = 1527033157)
    public EbookInfo(Long id, String name, String path, Long time, int step) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.time = time;
        this.step = step;
    }
    @Generated(hash = 1447690785)
    public EbookInfo() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getPath() {
        return this.path;
    }
    public void setPath(String path) {
        this.path = path;
    }

    public EbookInfo(String name, String path) {
        this.name = name;
        this.path = path;
    }

    public int getIv() {
        return iv;
    }

    public void setIv(int iv){
        this.iv = iv;
    }
    public Long getTime() {
        return this.time;
    }
    public void setTime(Long time) {
        this.time = time;
    }
    public int getStep() {
        return this.step;
    }
    public void setStep(int step) {
        this.step = step;
    }
}
