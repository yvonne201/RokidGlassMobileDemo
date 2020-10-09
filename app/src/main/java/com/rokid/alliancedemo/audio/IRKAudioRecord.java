package com.rokid.alliancedemo.audio;


/**
 * Author: heshun
 * Date: 2020/9/21 5:33 PM
 * gmail: shunhe1991@gmail.com
 */
public interface IRKAudioRecord {

    void startRecord(RKAudioRecorder.VoiceCallback voiceCallback);


    void release();
}
