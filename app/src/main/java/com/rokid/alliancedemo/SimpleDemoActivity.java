package com.rokid.alliancedemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.rokid.mcui.utils.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * 首页activity 主要配置权限
 */
public class SimpleDemoActivity extends AppCompatActivity {

    private static final String[] REQUIRED_PERMISSION_LIST = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_PHONE_STATE,
    };
    private static final int REQUEST_CODE = 1;
    private List<String> mMissPermissions = new ArrayList<>(12);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_demo);

        initView();

        //权限配置
        if (isVersionM() && !checkAndRequestPermissions()) {
            return;
        }



    }



    private boolean isVersionM() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    private boolean checkAndRequestPermissions() {
        mMissPermissions.clear();
        for (String permission : REQUIRED_PERMISSION_LIST) {
            int result = ContextCompat.checkSelfPermission(this, permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                mMissPermissions.add(permission);
            }
        }
        // check permissions has granted
        if (mMissPermissions.isEmpty()) {
            return true;
        } else {
            ActivityCompat.requestPermissions(this,
                    mMissPermissions.toArray(new String[mMissPermissions.size()]),
                    REQUEST_CODE);
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Logger.d("onRequestPermissionsResult-------->requestCode = " + requestCode);
        if (requestCode == REQUEST_CODE) {
            for (int i = grantResults.length - 1; i >= 0; i--) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    mMissPermissions.remove(permissions[i]);
                }
            }
        }
        // Get permissions success or not
        if (mMissPermissions.isEmpty()) {
        } else {
            for (String mMissPermission : mMissPermissions) {

                Logger.i("required permission : ", mMissPermission);
            }
            Toast.makeText(this, "权限问题", Toast.LENGTH_SHORT).show();
            this.finish();
        }
    }




    private void initView() {


        findViewById(R.id.bt_presentation).setOnClickListener(v -> {
            startActivity(new Intent(this, DemoPresentationActivity.class));//双屏异显Demo
        });


        findViewById(R.id.bt_glass).setOnClickListener((v) -> {
            startActivity(new Intent(this, DemoGlassActivity.class));//按键、传感器、眼镜信息获取
        });


        findViewById(R.id.bt_face_sdk).setOnClickListener(v -> {
            startActivity(new Intent(this, DemoFacePlateActivity.class));//人脸和车牌sdk使用
        });

        findViewById(R.id.bt_online_recg).setOnClickListener(v -> {
            startActivity(new Intent(this, DemoRKOnlineActivity.class));//Rokid在线人脸识别使用Demo

        });

        findViewById(R.id.bt_offline_recog).setOnClickListener(v -> {
            startActivity(new Intent(this,DemoRKOfflineActivity.class));// Rokid离线人脸识别使用Demo
        });

    }


}
