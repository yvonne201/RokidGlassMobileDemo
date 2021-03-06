package com.rokid.alliancedemo;

import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.jiangdg.usbcamera.RKGlassDevice;
import com.jiangdg.usbcamera.callback.OnGlassConnectListener;
import com.rokid.alliance.base.BaseLibrary;
import com.rokid.alliance.base.annotation.RecognizeType;
import com.rokid.alliance.base.hw.GlassInfo;
import com.rokid.alliance.base.message.car.ReqCarRecognizeMessage;
import com.rokid.alliance.base.message.car.RespCarRecognizeMessage;
import com.rokid.alliance.base.message.dto.CarRecognizeInfoBean;
import com.rokid.alliance.base.message.dto.FaceInfoBean;
import com.rokid.alliance.base.message.face.ReqOnlineSingleFaceMessage;
import com.rokid.alliance.base.message.face.RespOnlineSingleFaceMessage;
import com.rokid.alliance.base.utils.BitmapUtils;
import com.rokid.glass.m_glass.RKGlassUI;
import com.rokid.mcui.utils.ImageUtils;
import com.rokid.mobile.magic.RKAlliance;
import com.rokid.mobile.magic.online.OnlineRecgHelper;
import com.rokid.mobile.magic.online.OnlineRequest;

/**
 * 在线人脸识别demo
 * 此demo使用Rokid双屏异显sdk，如果需要修改眼镜端ui 可以获取源码进行修改
 */
public class DemoRKOnlineActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_r_k_online);

        initOnlineRecog();

    }

    /**
     * 在线识别
     */
    private void initOnlineRecog() {
        // initUsb设备，需要传入CameraViewInterface，如果不需要该view可见，可以将其宽高大小设置为1dp
        RKGlassDevice.RKGlassDeviceBuilder.buildRKGlassDevice().build().initUsbDevice(this, findViewById(R.id.uvc_preview), new OnGlassConnectListener() {
            @Override
            public void onGlassConnected(UsbDevice usbDevice, GlassInfo glassInfo) {
                // 眼镜连接成功回调
            }

            @Override
            public void onGlassDisconnected() {
                // 眼镜断开连接回调
            }
        });

        BaseLibrary.initialize(getApplication());

        RKGlassUI.getInstance().initGlassUI(getApplicationContext());

        // 设置识别类型,以及是否为在线模式
        // 识别类型支持：：
        // RecognizeType.IS_SINGLE_RECOGNIZE：单人识别
        // RecognizeType.IS_MULTI_RECOGNIZE：多人识别
        // RecognizeType.IS_PLATE_RECOGNIZE：车牌识别
        RKGlassUI.getInstance().recogSettingChanged(RecognizeType.IS_MULTI_RECOGNIZE, true);

        // 加载人脸模型
        RKAlliance.getInstance().loadFaceModel(getApplicationContext(), null);
        // 加载车牌模型
        RKAlliance.getInstance().loadLPRModel(getApplicationContext(), null);

        // 人脸车牌在线识别监听返回结果
        OnlineRecgHelper.getInstance().init(new OnlineRequest() {
            @Override
            public void sendFaceInfo(ReqOnlineSingleFaceMessage reqOnlineSingleFaceMessage) {
                //TODO: 将人脸信息上传云端做比对
                // 此处mock结果返回，实际要比对完成后再返回
                RespOnlineSingleFaceMessage respOnlineSingleFaceMessage = new RespOnlineSingleFaceMessage();
                respOnlineSingleFaceMessage.setServerCode(RespOnlineSingleFaceMessage.ServerErrorCode.OK);  //设置返回码，正常返回OK，异常详见ServerErrorCode定义
                FaceInfoBean faceInfoBean = new FaceInfoBean();
                faceInfoBean.setName("xxx");  //在线识别 人员名字
                faceInfoBean.setNativeplace("浙江.杭州");  // 在线识别 人员籍贯，比如"浙江.杭州"
                faceInfoBean.setCardno("xxxxxxxxxxxxxxxx");  //在线识别 人员身份证信息
                faceInfoBean.setTag("上访人员");    //在线识别 人员标签信息，比如"逃犯"/"可疑人员"/"上访人员"
                faceInfoBean.setAlarm(true);  //是否开启警报音
                Bitmap bm = BitmapUtils.bytes2bitmap(reqOnlineSingleFaceMessage.getFaceImage(), reqOnlineSingleFaceMessage.getWidth(), reqOnlineSingleFaceMessage.getHeight());
                faceInfoBean.setFaceImage(ImageUtils.bitmap2Bytes(bm));   //在线识别后需要眼镜端展示的人员头像图片数据，此处只是mock了从眼镜端截取的图片数据
                respOnlineSingleFaceMessage.setTrackId(reqOnlineSingleFaceMessage.getTrackId());
                respOnlineSingleFaceMessage.setFaceInfoBean(faceInfoBean);
                respOnlineSingleFaceMessage.setServerCode(RespOnlineSingleFaceMessage.ServerErrorCode.OK);
                OnlineRecgHelper.getInstance().onFaceOnlineResp(respOnlineSingleFaceMessage);   //调用此接口将在线识别结果返回给眼镜
            }

            @Override
            public void sendPlateInfo(ReqCarRecognizeMessage reqCarRecognizeMessage) {
                //TODO: 将车牌信息上传云端做比对
                // 此处mock结果返回，实际要比对完成后再返回
                RespCarRecognizeMessage respCarRecognizeMessage = new RespCarRecognizeMessage();
                respCarRecognizeMessage.setErrorCode(0);
                CarRecognizeInfoBean carRecognizeInfoBean = new CarRecognizeInfoBean();
                carRecognizeInfoBean.setPlate("浙ADA0178");  //车牌号
                carRecognizeInfoBean.setOwner("xxxx");  // 车主姓名
                carRecognizeInfoBean.setBrand("BYD");  //品牌
                carRecognizeInfoBean.setColor("红");  //车身颜色
                carRecognizeInfoBean.setTag("xxxxxxx");  //标签信息，比如"违章3次"/"失踪车牌" 等
                respCarRecognizeMessage.setCarRecognizeInfoBean(carRecognizeInfoBean);
                respCarRecognizeMessage.setErrorCode(0);
                OnlineRecgHelper.getInstance().onPlateOnlineResp(respCarRecognizeMessage);  //调用此接口将在线识别结果返回给眼镜
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RKAlliance.getInstance().releasePlateSdk();
        RKAlliance.getInstance().releaseFaceSdk();

        RKGlassDevice.getInstance().deInit();
        OnlineRecgHelper.getInstance().init(null);
        RKGlassUI.getInstance().removeGlassUI();
    }

}
