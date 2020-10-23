package com.rokid.alliancedemo;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import androidx.databinding.DataBindingUtil;

import com.jiangdg.usbcamera.RKGlassDevice;
import com.jiangdg.usbcamera.callback.OnGlassConnectListener;
import com.rokid.alliance.base.hw.GlassInfo;
import com.rokid.alliancedemo.audio.RKAudioRecorder;
import com.rokid.alliancedemo.databinding.ActivityDemoAudioRecordBinding;
import com.rokid.alliancedemo.util.AudioTrackManager;

import java.io.File;


/**
 * 注意 Android10以下无法进行多路Mic数据采集，只能多路共用一个AudioRecord，maven库中已经提供了一个解决方案{@link com.rokid.alliance.base.audiorecord.RKAudioRecordManager}
 */
public class DemoAudioRecordActivity extends AppCompatActivity {

    ActivityDemoAudioRecordBinding mBinding;
    private RKAudioRecorder mRecorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_demo_audio_record);

        initView();
        AudioTrackManager.getInstance().initConfig();

        RKGlassDevice.getInstance().initUsbDevice(this, mBinding.cameraView, new OnGlassConnectListener() {
            @Override
            public void onGlassConnected(UsbDevice usbDevice, GlassInfo glassInfo) {

            }

            @Override
            public void onGlassDisconnected() {

            }
        });
    }

    private boolean isRecording;
    private void initView() {
        mBinding.btStart.setOnClickListener(v ->{
            if (isRecording)  return;
            isRecording = true;
            mRecorder = new RKAudioRecorder();
            mRecorder.startDebug();
            mRecorder.startRecord(null);
        });

        mBinding.btStop.setOnClickListener(v -> {
            if (!isRecording) return;
            if (null != mRecorder) mRecorder.release();
            isRecording = false;
        });

        mBinding.btPlayRecord.setOnClickListener( v -> {
            if (isRecording) mRecorder.release();

            String debugFileName = RKAudioRecorder.DEBUG_FILE_NAME;
            if (!new File(debugFileName).exists()) return;
            AudioTrackManager.getInstance().play(debugFileName);
        });
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isRecording) {
            mRecorder.release();
        }
        RKGlassDevice.getInstance().deInit();
    }


}
