package com.rokid.alliancedemo.util;


import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;

import com.rokid.mcui.utils.Logger;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileManager {

    private String fileName;
    private FileOutputStream fileOutputStream;
    private BufferedOutputStream bufferedOutputStream;


    public FileManager(String fileName) {
        this.fileName = fileName;
        try {
            File file = new File(fileName);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            fileOutputStream = new FileOutputStream(file);
            bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveFileData(byte[] data, int offset, int length) {
        try {
            bufferedOutputStream.write(data, offset, length);
            bufferedOutputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveFileData(byte[] data) {
        try {
            bufferedOutputStream.write(data);
            bufferedOutputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeFile() {
        try {
            bufferedOutputStream.close();
            fileOutputStream.close();
        } catch (Exception e) {
        } finally {
            try {
                bufferedOutputStream.close();
                fileOutputStream.close();
            } catch (Exception e1) {
            }
        }
    }

    public void deleteFile() {
        try {
            File file = new File(fileName);
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 读取assets 目录下的json文件
     * @param context
     * @param fileName
     * @return
     */
    public static String getJson(Context context,String fileName) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            AssetManager assetManager = context.getAssets();
            BufferedReader bf = new BufferedReader(new InputStreamReader(
                    assetManager.open(fileName)));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }


    /**
     * 将assets 复制到sd目录下
     * @param context
     * @param srcPath
     * @param dstPath
     */
    /**
     * 将文件从assets目录，考贝到 /data/data/包名/files/ 目录中。assets 目录中的文件，会不经压缩打包至APK包中，使用时还应从apk包中导出来
     * @param fileName 文件名,如aaa.txt
     */
    public static void copyAssetsFile2Phone(Activity activity, String fileName,String newFileName){
        try {
            InputStream inputStream = activity.getAssets().open(fileName);
            //getFilesDir() 获得当前APP的安装路径 /data/data/包名/files 目录
            File file = new File(newFileName);
            if(!file.exists() || file.length()==0) {
                FileOutputStream fos =new FileOutputStream(file);//如果文件不存在，FileOutputStream会自动创建文件
                int len=-1;
                byte[] buffer = new byte[1024];
                while ((len=inputStream.read(buffer))!=-1){
                    fos.write(buffer,0,len);
                }
                fos.flush();//刷新缓存区
                inputStream.close();
                fos.close();
                Logger.i("FileManager::::【模型文件复制完毕】" );
            } else {
                Logger.i("FileManager::::【模型文件已存在，无需复制】" );

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}

