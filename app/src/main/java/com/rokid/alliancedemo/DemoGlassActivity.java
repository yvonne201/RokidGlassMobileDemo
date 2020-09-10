package com.rokid.alliancedemo;

import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.jiangdg.usbcamera.RKGlassDevice;
import com.jiangdg.usbcamera.callback.OnGlassConnectListener;
import com.rokid.alliance.base.hw.GlassInfo;
import com.rokid.alliance.base.hw.GlassSensorEvent;
import com.rokid.alliance.base.hw.RKGlassTouchEvent;
import com.rokid.alliance.base.hw.listener.KeyEventType;
import com.rokid.alliance.base.hw.listener.RKKeyListener;
import com.rokid.alliancedemo.databinding.ActivityDemoGlassBinding;
import com.serenegiant.usb.common.AbstractUVCCameraHandler;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 眼镜硬件信息、按键、传感器事件获取sdk使用
 */
public class DemoGlassActivity extends AppCompatActivity {

    private ActivityDemoGlassBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_demo_glass);

        initView();
        initGlass();

    }

    private AtomicInteger count = new AtomicInteger(0);
    private final AbstractUVCCameraHandler.OnPreViewResultListener onPreviewFrameListener = new AbstractUVCCameraHandler.OnPreViewResultListener() {
        @Override
        public void onPreviewResult(byte[] data) {
            mBinding.tvPreview.post(() -> {

                mBinding.tvPreview.setText("Camera数据回调:" + count.getAndIncrement());
            });
        }
    };

    private void initView() {
        mBinding.btCameraData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                RKGlassDevice.getInstance().setOnPreviewFrameListener(onPreviewFrameListener);
            }
        });

    }

    private void initGlass() {
        RKGlassDevice.RKGlassDeviceBuilder
                .buildRKGlassDevice()
                .withGlassSensorEvent(new GlassSensorEvent() {

                    /**
                     * 距离传感器你
                     * @param status  true可认为是带上眼镜 唤醒光机 false可认为已摘下眼镜 熄灭光机
                     */
                    @Override
                    public void onPSensorUpdate(final boolean status) {
                        RKGlassDevice.getInstance().setBrightness(status ? 100 : 0);//可以根据距离传感器来判断眼镜是否处于佩戴状态，来调整光机的亮度节能
                        mBinding.lSenSor.post(() -> {

                            mBinding.lSenSor.setProgress(status ? 100 : 0);
                        });
                        mBinding.tvPsensor.post(() -> mBinding.tvPsensor.setText("距离传感器状态: " + status));
                    }


                    /**
                     * 前置光线传感器
                     * @param lux 最小值为0
                     */
                    @Override
                    public void onLSensorUpdate(final int lux) {
                        mBinding.tvLsensor.post(() -> mBinding.tvLsensor.setText("光线传感器: " + lux));
                    }
                })
                .withRKKeyListener(new RKKeyListener() {

                    /**
                     * 电源键键事件
                     * @param eventType {@link KeyEventType}
                     */
                    @Override
                    public void onPowerKeyEvent(final int eventType) {
                        mBinding.tvPower.post(() -> mBinding.tvPower.setText("电源键事件: " + eventToString(eventType)));
                    }

                    /**
                     * 回退键事件
                     * @param eventType {@link KeyEventType}
                     */
                    @Override
                    public void onBackKeyEvent(final int eventType) {

                        mBinding.tvBack.post(() -> mBinding.tvBack.setText("回退键事件: " + eventToString(eventType)));
                    }

                    /**
                     * 触控板事件
                     * @param eventType {@link RKGlassTouchEvent}
                     */
                    @Override
                    public void onTouchKeyEvent(final int eventType) {

                        mBinding.tvTouch.post(() -> mBinding.tvTouch.setText("TouchBar事件: " + eventToString(eventType)));
                    }

                    /**
                     * 触控板向后滑动
                     */
                    @Override
                    public void onTouchSlideBack() {

                        mBinding.tvSlide.post(() -> mBinding.tvSlide.setText("向后滑动"));
                    }

                    /**
                     * 触控板向前滑动
                     */
                    @Override
                    public void onTouchSlideForward() {

                        mBinding.tvSlide.post(() -> mBinding.tvSlide.setText("向前滑动"));
                    }
                })
                .build()
                .initUsbDevice(this, mBinding.preview, new OnGlassConnectListener() {

                    /**
                     * 当用户授权眼镜权限后 将回调
                     * @param usbDevice {@link UsbDevice}
                     * @param glassInfo {@link GlassInfo}
                     */
                    @Override
                    public void onGlassConnected(UsbDevice usbDevice, GlassInfo glassInfo) {
                        mBinding.tvDeviceInfo.post(() -> mBinding.tvDeviceInfo.setText("Glass信息：vendorId:" + usbDevice.getVendorId()
                                + " productId: " + usbDevice.getProductId() + " deviceName: "
                                + usbDevice.getDeviceName() + " SN: " + glassInfo.getSn()));

                        mBinding.lSenSor.post(() -> mBinding.lSenSor.setProgress(RKGlassDevice.getInstance().getBrightness()));
                        mBinding.lSenSor.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                // 调整光机亮度 范围0~100
                                RKGlassDevice.getInstance().setBrightness(progress);
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {

                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {

                            }
                        });
                    }

                    /**
                     * 当眼镜断开连接时
                     */
                    @Override
                    public void onGlassDisconnected() {
                        mBinding.tvDeviceInfo.post(() -> mBinding.tvDeviceInfo.setText("Glass断开连接"));
                    }
                });


    }

    public String eventToString(int eventType) {
        if (eventType == KeyEventType.SINGLE_CLICK) {
            return "单击";
        } else if (eventType == KeyEventType.DOUBLE_CLICK) {
            return "双击";
        } else {
            return "长按";
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RKGlassDevice.getInstance().removeOnPreviewFrameListener(onPreviewFrameListener);
        RKGlassDevice.getInstance().deInit();
    }
}
