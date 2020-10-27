package com.rokid.alliancedemo;

import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.jiangdg.usbcamera.RKGlassDevice;
import com.jiangdg.usbcamera.callback.OnGlassConnectListener;
import com.rokid.alliance.base.hw.GlassInfo;
import com.rokid.mcui.utils.CollectionUtils;
import com.rokid.mcui.utils.GsonHelper;
import com.rokid.mcui.utils.Logger;
import com.rokid.voice.command.RKOfflineCommandActivity;
import com.rokid.voice.command.RKOfflineWords;
import com.rokid.voice.command.RKVoiceEngine;
import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;
import com.zhy.view.flowlayout.TagFlowLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RKVoiceCommandActivity extends RKOfflineCommandActivity {

    private static final Set<Integer> words = new HashSet<>();
    private TagFlowLayout flowLayout;

    private static final RKVoiceEngine.RKOfflineCommandCallback defaultCallback = new RKVoiceEngine.RKOfflineCommandCallback() {
        @Override
        public void onOfflineCommandEvent(String event) {
            Logger.i("receive default event callback", event);
        }
    };

    private static final RKOfflineWords[] offlineWords = new RKOfflineWords[]{
            new RKOfflineWords("单人核查", "dan ren he cha", defaultCallback),
            new RKOfflineWords("多人布控", "duo ren bu kong", defaultCallback),
            new RKOfflineWords("车牌识别", "che pai shi bie", defaultCallback),
            new RKOfflineWords("云端模式", "yun duan mo shi", defaultCallback),
            new RKOfflineWords("本地模式", "ben di mo shi", defaultCallback),
            new RKOfflineWords("大声点", "da sheng dian", defaultCallback),
            new RKOfflineWords("小声点", "xiao sheng dian", defaultCallback),
            new RKOfflineWords("亮一点", "liang yi dian", defaultCallback),
            new RKOfflineWords("暗一点", "an yi dian", defaultCallback),
            new RKOfflineWords("点亮屏幕", "dian liang ping mu", defaultCallback),
            new RKOfflineWords("熄灭屏幕", "xi mie ping mu", defaultCallback),
            new RKOfflineWords("显示帮助", "xian shi bang zhu", defaultCallback),
            new RKOfflineWords("关闭帮助", "guan bi bang zhu", defaultCallback),
    };


    @Override
    public RKOfflineWords[] getGlobalOfflineCommand() {
        return offlineWords;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rkvoice_command);

        RKGlassDevice.RKGlassDeviceBuilder.buildRKGlassDevice().build().initUsbDevice(this, findViewById(R.id.camera_view), new OnGlassConnectListener() {
            @Override
            public void onGlassConnected(UsbDevice usbDevice, GlassInfo glassInfo) {

            }

            @Override
            public void onGlassDisconnected() {

            }
        });


        EditText etWord = findViewById(R.id.et_word);
        EditText etPinyin = findViewById(R.id.et_pinyin);
        TextView tvCurrent = findViewById(R.id.tv_current);
        tvCurrent.setText(GsonHelper.toJson(offlineWords));

        findViewById(R.id.bt_delete).setOnClickListener(v -> {
            if (null != etWord.getText() && !TextUtils.isEmpty(etWord.getText().toString())) {
                String text = etWord.getText().toString();
                List<String> words = new ArrayList<>();
                words.add(text);
                removeOfflineCommands(words);
                tvCurrent.post(() -> {
                    tvCurrent.setText(GsonHelper.toJson(getCurrentOfflineCommands()));
                });
            }
        });

        findViewById(R.id.bt_replace).setOnClickListener(v -> {
            if (words.size() > 0) {

                Set<RKOfflineWords> cacheWords = new HashSet<>();
                for (Integer pos : words) {
                    cacheWords.add(offlineWords[pos]);
                }

                updateOfflineCommands(CollectionUtils.toArray(cacheWords, RKOfflineWords.class));
                tvCurrent.post(() -> {

                    tvCurrent.setText(GsonHelper.toJson(getCurrentOfflineCommands()));
                });
            }
        });

        findViewById(R.id.bt_add).setOnClickListener(v -> {

            Set<RKOfflineWords> cacheWords = new HashSet<>();
            for (Integer pos : words) {
                cacheWords.add(offlineWords[pos]);
            }
            if (null != etWord.getText() && !TextUtils.isEmpty(etWord.getText())) {
                cacheWords.add(new RKOfflineWords(etWord.getText().toString(), etPinyin.getText().toString(), new RKVoiceEngine.RKOfflineCommandCallback() {
                    @Override
                    public void onOfflineCommandEvent(String event) {
                        Logger.i("receive offline command: ", event);
                    }
                }));
            }
            updateOfflineCommands(CollectionUtils.toArray(cacheWords, RKOfflineWords.class));
            tvCurrent.post(() -> {

                tvCurrent.setText(GsonHelper.toJson(getCurrentOfflineCommands()));
            });
        });

        findViewById(R.id.bt_init).setOnClickListener(v -> {
            RKVoiceEngine.getInstance().init(getApplicationContext());
            RKVoiceEngine.getInstance().openDebug();
            RKVoiceEngine.getInstance().useMobileMic();
        });

        findViewById(R.id.bt_uninit).setOnClickListener(v -> {
            RKVoiceEngine.getInstance().uninit();
            RKVoiceEngine.getInstance().closeDebug();
        });

        flowLayout = findViewById(R.id.flowLayout);
        flowLayout.setAdapter(new TagAdapter<RKOfflineWords>(Arrays.asList(offlineWords)) {
            @Override
            public View getView(FlowLayout parent, int position, RKOfflineWords o) {
                TextView tv = (TextView) LayoutInflater.from(getApplicationContext()).inflate(R.layout.dialog_view_textview,
                        flowLayout, false);
                tv.setText(o.getContent());
                return tv;
            }
        });

        flowLayout.setOnSelectListener(new TagFlowLayout.OnSelectListener() {
            @Override
            public void onSelected(Set<Integer> selectPosSet) {
                words.clear();
                words.addAll(selectPosSet);
            }
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RKGlassDevice.getInstance().deInit();
    }
}
