package com.rokid.alliancedemo.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;

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
    public static final int AUDIO_MIN_BUFFER_SIZE = AudioRecord.getMinBufferSize(AUDIO_SAMPLE_SIZE, AUDIO_CHANNEL_CONFIG, AUDIO_FORMAT);
    private AudioRecord mAudioRecord;
    private Thread mRecordThread;
    private VoiceCallback mCallback;
    private AtomicBoolean isReleased = new AtomicBoolean(false);
    private AtomicBoolean isPause = new AtomicBoolean(false);
    private AtomicBoolean isDebug = new AtomicBoolean(false);
    public static final String DEBUG_FILE_NAME = Environment.getExternalStorageDirectory().getAbsolutePath() + "/debug_audio.pcm";

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
            int length = mAudioRecord.read(buffer, 0, AUDIO_MIN_BUFFER_SIZE);
            if (length == AudioRecord.ERROR_INVALID_OPERATION || length == AudioRecord.ERROR_BAD_VALUE) {
                continue;
            }
            if (isDebug.get()) {
                if (null == mFileManager) {
                    Logger.d("debug file is ", DEBUG_FILE_NAME);
                    mFileManager = new FileManager(DEBUG_FILE_NAME);
                }
                mFileManager.saveFileData(buffer);
            }
            if (null != mCallback) {
                mCallback.onVoiceData(buffer, length);
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
