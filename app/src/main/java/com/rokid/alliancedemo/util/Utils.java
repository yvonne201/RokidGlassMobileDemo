package com.rokid.alliancedemo.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.MediaStore;

import com.rokid.alliance.base.BaseLibrary;
import com.rokid.alliance.base.utils.CameraParams;

/**
 * Author: zhangshengwei
 * Date: 2020/9/28
 */
public class Utils {

    public static Utils instance;

    public static Utils getInstance(){
        if (instance == null){
            synchronized (Utils.class){
                if (instance == null){
                    instance = new Utils();
                }
            }
        }
        return instance;
    }


    public static String getRealFilePath(Context mContext ,final Uri uri) {
        if (null == uri) {
            return null;
        }
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = mContext.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }


    public Rect getRoi() {
        Rect roiRect = null;
        if (CameraParams.PREVIEW_WIDTH == 1280) {
            roiRect = new Rect(200, 160, 850, 650);
//            roiRect = new Rect(350, 210, 750, 650);
        } else if (CameraParams.PREVIEW_WIDTH == 1920) {
            roiRect = new Rect(525, 515, 1125, 975);
        }
        return roiRect;
    }
}
