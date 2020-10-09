package com.rokid.alliancedemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.media.AudioAttributes;
import android.media.AudioAttributes.Builder;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import androidx.databinding.DataBindingUtil;

import com.jiangdg.usbcamera.RKGlassDevice;
import com.jiangdg.usbcamera.callback.OnGlassConnectListener;
import com.rokid.alliance.base.hw.GlassInfo;
import com.rokid.alliancedemo.audio.RKAudioRecorder;
import com.rokid.alliancedemo.databinding.ActivityDemoAudioRecordBinding;
import com.rokid.alliancedemo.util.AudioTrackManager;

import java.io.File;

import static android.media.AudioAttributes.CONTENT_TYPE_MUSIC;
import static android.media.AudioAttributes.USAGE_MEDIA;
import static android.media.AudioTrack.MODE_STREAM;

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
    protected void onDestroy(){
        if (isRecording){
            mRecorder.release();
        }
        RKGlassDevice.getInstance().deInit();
    }


}
