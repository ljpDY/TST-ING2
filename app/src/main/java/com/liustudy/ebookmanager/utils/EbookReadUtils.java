package com.liustudy.ebookmanager.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import com.liustudy.ebookmanager.beans.BookMark;
import com.liustudy.ebookmanager.beans.Chapter;
import com.liustudy.ebookmanager.beans.EbookInfo;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import dagger.Module;

//处理书籍内容的工具类
@Module
public class EbookReadUtils {

    private  Context mContext;
    private  ContentResolver mContentResolver;

    public  EbookReadUtils (Context context){
                    mContext = context;
                    mContentResolver = context.getContentResolver();
    }

    /**
     *
     * @param str_filepath 文件路径
     * @param esize 已经提取的文本数量
     * @return 文本信息
     * @throws IOException
     */
        public Chapter convertCodeAndGetText(String str_filepath, long esize) throws IOException {

            File file = new File(str_filepath);
            //已提取全部文本
            if(esize==file.length()){
                return null;
            }

            //初始化
            BufferedReader reader;
            String text = "";
            int lines = 0;
            reader = getBufferedReader(file);//根据文本格式生成reader

            //跳过已读取文本
            reader.skip(esize);
            /**提取第一段内容，由于字符串的length方法和reader的skip方法的数目不同步
             * 每多读取一行要多记一次，所以以lines的方式代替
             * 由于少记录一次换行，所以第一段lines+2，经测试，依旧有些特殊文件会导致不同步
             */
                String str = reader.readLine();
                        text = str;
                        lines+=2;
                        str = reader.readLine();

                //循环读取后续内容
                while (str != null) {

                    text = text + "\n"+str;
                    lines+=1;
                    str = reader.readLine();

                    //读取内容为空，跳出
                    if(str==null){
                        break;
                    }
                    //提取到满足新章节的内容，跳出
                    else if(str.startsWith("第")&&str.contains("章")&&text.length()>100)
                                break;
                    //提取的文本过大（开销太大，会有很高的时延）根据需求减少文本内容
                    else  if(str.endsWith("。")&&text.length()>20000)
                                break;
                }

                //关闭reader
                reader.close();

                //将数据存储到bean中
                Chapter c = new Chapter();
                c.setText(text);
                c.setLines(lines);
                return c;
            }
//根据文本编码格式生成对应的reader

    public BufferedReader getBufferedReader(File file) throws IOException {

        BufferedReader reader;
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream in = new BufferedInputStream(fis);
        in.mark(4);

        byte[] first3bytes = new byte[3];
        in.read(first3bytes);//找到文档的前三个字节并自动判断文档类型。
        in.reset();

        if (first3bytes[0] == (byte) 0xEF && first3bytes[1] == (byte) 0xBB
                && first3bytes[2] == (byte) 0xBF) {// utf-8
            reader = new BufferedReader(new InputStreamReader(in, "utf-8"));
        }
        else if (first3bytes[0] == (byte) 0xFF
                && first3bytes[1] == (byte) 0xFE) {
            reader = new BufferedReader(
                    new InputStreamReader(in, "unicode"));
        }
        else if (first3bytes[0] == (byte) 0xFE
                && first3bytes[1] == (byte) 0xFF) {
            reader = new BufferedReader(new InputStreamReader(in,
                    "utf-16be"));
        }
        else if (first3bytes[0] == (byte) 0xFF
                && first3bytes[1] == (byte) 0xFF) {
            reader = new BufferedReader(new InputStreamReader(in,
                    "utf-16le"));
        }
        else {
            reader = new BufferedReader(new InputStreamReader(in, "GBK"));
        }
        return reader;
    }

//获取文件系统中以“txt”结尾的文件
    public List<EbookInfo> getEbookList() {
                List<EbookInfo> list = new ArrayList<EbookInfo>();
                Cursor cursor = null;
                cursor = mContentResolver.query(MediaStore.Files.getContentUri("external"), new String[]{"_id", "_data", "_size"}, null, null, null);
                int dataindex = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
                while (cursor.moveToNext()) {
                    String path = cursor.getString(dataindex);
                    if (path.endsWith("txt")) {
                        EbookInfo ebookInfo = new EbookInfo(getName(path), path);
                        list.add(ebookInfo);
                    }
                }
                cursor.close();
                return list;
                }

//通过文件路径提取文件名称，根据需求可额外去除.txt的结尾
                public String getName(String path){
                    String [] sz=path.split("/");
                    String name = sz[sz.length-1];
                    return name;
                }


    }
