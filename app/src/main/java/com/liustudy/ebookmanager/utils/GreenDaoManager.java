package com.liustudy.ebookmanager.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.liustudy.ebookmanager.beans.EbookInfo;
import com.liustudy.ebookmanager.gen.DaoMaster;
import com.liustudy.ebookmanager.gen.DaoSession;
import com.liustudy.ebookmanager.gen.EbookInfoDao;

import java.util.List;

public class GreenDaoManager  {

        private DaoMaster.DevOpenHelper mHelper;//获取Helper对象
        private SQLiteDatabase db;
        private DaoMaster mDaoMaster;
        private DaoSession mDaoSession;
        private Context context;
        private EbookInfoDao ebookInfoDao;

        private static GreenDaoManager mdaoManager;

        //获取单例
        public static GreenDaoManager getInstance(Context context){
            if(mdaoManager == null){
                synchronized (GreenDaoManager.class){
                    if(mdaoManager == null){
                        mdaoManager = new GreenDaoManager(context);
                    }
                }
            }
            return mdaoManager;
        }
        /**
         * 初始化
         * @param context
         */
        private GreenDaoManager(Context context) {
            this.context = context;
            mHelper = new DaoMaster.DevOpenHelper(context,"ebook.db", null);
            mDaoMaster =new DaoMaster(getWritableDatabase());
            mDaoSession = mDaoMaster.newSession();
            ebookInfoDao = mDaoSession.getEbookInfoDao();
        }

        //获取可读数据库
        private SQLiteDatabase getReadableDatabase(){
            if(mHelper == null){
                mHelper = new DaoMaster.DevOpenHelper(context,"ebook.db",null);
            }
            SQLiteDatabase db =mHelper.getReadableDatabase();
            return db;
        }

        //获取可写数据库
        private SQLiteDatabase getWritableDatabase(){
            if(mHelper == null){
                mHelper =new DaoMaster.DevOpenHelper(context,"ebook.db",null);
            }
            SQLiteDatabase db = mHelper.getWritableDatabase();
            return db;
        }

        //插入或替换数据
        public void insertOrReplace(EbookInfo ebookInfo){
            ebookInfoDao.insertOrReplace(ebookInfo);
        }
        //插入数据
        public long insert(EbookInfo ebookInfo){
            return  ebookInfoDao.insert(ebookInfo);
        }
        //更新数据
        public void update(EbookInfo ebookInfo){

                ebookInfoDao.update(ebookInfo);
            }

        //按条件查询数据
        public EbookInfo searchByWhere(String name){
            EbookInfo ebookInfo =  ebookInfoDao.queryBuilder()
                    .where(EbookInfoDao.Properties.Name.eq(name)).build().unique();
            return ebookInfo;
        }

        //查询所有数据
        public List<EbookInfo> searchAll(){
            List<EbookInfo> ebookInfos = ebookInfoDao.queryBuilder().orderAsc(EbookInfoDao.Properties.Time).list();
            return ebookInfos;
        }

        //删除数据
        public void delete(String name){
            ebookInfoDao.queryBuilder().where(EbookInfoDao.Properties.Name.eq(name))
                    .buildDelete().executeDeleteWithoutDetachingEntities();
        }
    }
