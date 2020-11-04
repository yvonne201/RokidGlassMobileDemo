package com.rokid.alliancedemo.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;

import com.rokid.alliance.base.agc.RKAgc;
import com.rokid.alliancedemo.util.FileManager;
import com.rokid.mcui.utils.Logger;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Author: heshun
 * Date: 2020/9/21 5:32 PM
 * gmail: shunhe1991@gmail.com
 */
public class RKAudioRecorder implements IRKAudioRecord {

    public static final int AUDIO_SAMPLE_SIZE = 16_000;
    public static final int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    public static final int AUDIO_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO;
    public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    public static final int AUDIO_MIN_BUFFER_SIZE = 1280;
    private AudioRecord mAudioRecord;
    private Thread mRecordThread;
    private VoiceCallback mCallback;
    private AtomicBoolean isReleased = new AtomicBoolean(false);
    private AtomicBoolean isPause = new AtomicBoolean(false);
    private AtomicBoolean isDebug = new AtomicBoolean(false);
    public static final String DEBUG_FILE_NAME = Environment.getExternalStorageDirectory().getAbsolutePath() + "/debug_audio.pcm";
    private RKAgc rkAgc;

    private final Object syncObject = new Object();

    private Runnable mRecordTask = new Runnable() {
        @Override
        public void run() {
            doRecord();
        }
    };
    private FileManager mFileManager;

    private void doRecord() {
        mAudioRecord = new AudioRecord(AUDIO_SOURCE, AUDIO_SAMPLE_SIZE,
                AUDIO_CHANNEL_CONFIG, AUDIO_FORMAT, AUDIO_MIN_BUFFER_SIZE);


        byte[] buffer = new byte[AUDIO_MIN_BUFFER_SIZE];
        mAudioRecord.startRecording();
        while (!isReleased.get()) {
            int readBytes = mAudioRecord.read(buffer, 0, AUDIO_MIN_BUFFER_SIZE);
            if (readBytes == AudioRecord.ERROR_INVALID_OPERATION || readBytes == AudioRecord.ERROR_BAD_VALUE) {
                continue;
            }

            if (readBytes < AUDIO_MIN_BUFFER_SIZE) {
                readBytes = readBytes / 640 * 640;
            }
            Logger.e("encodeBytes:", String.valueOf(readBytes));
            byte[] output = new byte[readBytes];
            int result = rkAgc.processData(buffer, 2, 320, output);
            if (result != 0) {
                Logger.e("agc process data frame count:", String.valueOf(result));
            }
            if (isDebug.get()) {
                if (null == mFileManager) {
                    Logger.d("debug file is ", DEBUG_FILE_NAME);
                    mFileManager = new FileManager(DEBUG_FILE_NAME);
                }

                mFileManager.saveFileData(output);
            }
            if (null != mCallback) {
                mCallback.onVoiceData(output, readBytes);
            }

        }
        synchronized (syncObject) {
            if (null != mAudioRecord) {

                mAudioRecord.stop();
                mAudioRecord.release();
                mAudioRecord = null;
            }
        }

    }

    @Override
    public void startRecord(VoiceCallback voiceCallback) {
        mCallback = voiceCallback;
        if (null == mRecordThread) {

            mRecordThread = new Thread(mRecordTask);
            mRecordThread.start();
        }
        if (null == rkAgc){
            rkAgc = new RKAgc();
            rkAgc.initAgc(AUDIO_SAMPLE_SIZE, 160, 2);
            rkAgc.setAgcConfig(20, 2);
        }
    }

    @Override
    public void release() {
        isReleased.set(true);
        synchronized (syncObject) {
            if (null != mAudioRecord) {
                mAudioRecord.stop();
                mAudioRecord.release();
                mAudioRecord = null;
            }
        }

        if (null != mRecordThread) {
            try {
                mRecordThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (null != rkAgc){
            rkAgc.releaseAgc();
            rkAgc = null;
        }
    }

    public void startDebug() {
        if (!isDebug.get()) {

            isDebug.set(true);

        }
    }

    public void stopDebug() {

        if (isDebug.get()) {
            isDebug.set(false);
            if (null != mFileManager) {
                mFileManager.closeFile();
            }
        }
    }

    public interface VoiceCallback {

        void onVoiceData(byte[] data, int length);
    }
}
