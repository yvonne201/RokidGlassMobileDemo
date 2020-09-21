package com.rokid.alliancedemo;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.jiangdg.usbcamera.RKGlassDevice;
import com.jiangdg.usbcamera.callback.OnGlassConnectListener;
import com.rokid.alliance.base.BaseLibrary;
import com.rokid.alliance.base.hw.GlassInfo;
import com.rokid.alliance.base.model.RKFaceDO;
import com.rokid.alliance.base.model.RKFaceModel;
import com.rokid.alliance.base.model.RKLPRDO;
import com.rokid.alliance.base.model.RKLPRModel;
import com.rokid.alliance.base.utils.BitmapUtils;
import com.rokid.alliance.base.utils.CameraParams;
import com.rokid.alliance.base.utils.FaceRectUtils;
import com.rokid.alliancedemo.databinding.ActivityDemoFacePlateBinding;
import com.rokid.mcui.utils.Logger;
import com.rokid.mobile.magic.RKAlliance;
import com.rokid.mobile.magic.callback.Callback;
import com.rokid.mobile.magic.callback.PreparedListener;
import com.serenegiant.usb.common.AbstractUVCCameraHandler;

import static com.rokid.alliance.base.utils.CameraParams.PREVIEW_HEIGHT;
import static com.rokid.alliance.base.utils.CameraParams.PREVIEW_WIDTH;

/**
 * 人脸车牌识别sdk使用
 */
public class DemoFacePlateActivity extends AppCompatActivity {

    private ActivityDemoFacePlateBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_demo_face_plate);


        init();
    }

    private final AbstractUVCCameraHandler.OnPreViewResultListener onPreviewFrameListener = new AbstractUVCCameraHandler.OnPreViewResultListener() {
        @Override
        public void onPreviewResult(byte[] data) {

            Logger.i("onPreviewView data:" + data.length);
            if (RKAlliance.getInstance().isFaceOpen() || RKAlliance.getInstance().isPlateOpen()) {
                RKAlliance.getInstance().setData(data);
            } else {
                Logger.i("face and plate is disabled");
            }
        }
    };

    private void init() {

        BaseLibrary.initialize(getApplication());

        RKGlassDevice.RKGlassDeviceBuilder.buildRKGlassDevice().build().initUsbDevice(this, mBinding.uvcPreview, new OnGlassConnectListener() {
            @Override
            public void onGlassConnected(UsbDevice usbDevice, GlassInfo glassInfo) {

            }

            @Override
            public void onGlassDisconnected() {

            }
        });


        RKGlassDevice.getInstance().setOnPreviewFrameListener(onPreviewFrameListener);

        /**
         * TODO
         */
        Rect roi = getRoi();

        /**
         * 初始化sdk 人脸sdk和车牌sdk可以分别进行初始化
         *
         * sdk初始化分为两部分，分别是1.加载模型；2.开启识别,且1需优先于2调用，这部分已由RKAlliance类处理，外部使用不需要关心顺序
         */
        RKAlliance.getInstance().loadFaceModel(getApplicationContext(), new PreparedListener() {
            @Override
            public void onPrepared() {

            }
        });

        RKAlliance.getInstance().loadLPRModel(getApplicationContext(), new PreparedListener() {
            @Override
            public void onPrepared() {

            }
        });


        RKAlliance.getInstance().initFaceSDK(getApplicationContext(), roi, new PreparedListener() {
            @Override
            public void onPrepared() {

            }
        });


        RKAlliance.getInstance().initPlateSDK(new PreparedListener() {
            @Override
            public void onPrepared() {
            }
        });


        RKAlliance.getInstance().registerFaceListener(faceCallback);


        RKAlliance.getInstance().registerLPRCallback(lprCallback);


    }

    private final Callback<RKFaceModel> faceCallback = new Callback<RKFaceModel>() {
        @Override
        public void onDataResult(RKFaceModel rkFaceModel, byte[] bytes) {
            Logger.i("onFaceCallback");
            if (null == rkFaceModel || null == rkFaceModel.getFaceList() || rkFaceModel.getFaceList().size() == 0) {
                return;
            }

            /**当检测到多人时，这里只展示第一个超过40分的人脸 实际使用为了避免后续业务阻塞导致处理的帧率下降，请注意拷贝一份byte[]数据在其他线程进行处理*/
            for (RKFaceDO rkFaceDO : rkFaceModel.getFaceList()) {
                if (rkFaceDO.quality < 40) continue;
                final Bitmap clipFace = getClipFace(rkFaceDO, bytes);
                mBinding.ivCrop.post(() -> {

                    mBinding.ivCrop.setImageBitmap(clipFace);
                });
                return;
            }
        }
    };

    private final Callback<RKLPRModel> lprCallback = new Callback<RKLPRModel>() {
        @Override
        public void onDataResult(RKLPRModel lprModel, byte[] bytes) {
            if (null == lprModel || null == lprModel.lps || lprModel.lps.size() == 0) {
                return;
            }

            /**车牌的处理参考上面人脸处理注意勿阻塞，此处和人脸处理一样简单展示车牌抠图 */
            final RKLPRDO lprdo = lprModel.lps.get(0);
            final Bitmap clipPlate = getClipPlate(lprdo, bytes);
            mBinding.ivPlateCrop.post(() -> {
                mBinding.ivPlateCrop.setImageBitmap(clipPlate);
            });
        }
    };


    private Rect getRoi() {
        Rect roiRect = null;
        if (CameraParams.PREVIEW_WIDTH == 1280) {
            roiRect = new Rect(200, 160, 850, 650);
//            roiRect = new Rect(350, 210, 750, 650);
        } else if (CameraParams.PREVIEW_WIDTH == 1920) {
            roiRect = new Rect(525, 515, 1125, 975);
        }
        return roiRect;
    }


    private Bitmap getClipPlate(RKLPRDO lprdo, byte[] rawData) {
        final Rect faceRect = lprdo.toRect(PREVIEW_WIDTH, PREVIEW_HEIGHT);
        final Rect finalRect = FaceRectUtils.scaleRect(faceRect, PREVIEW_WIDTH, PREVIEW_HEIGHT, 1.6f);
        Logger.i("finalRect:" + finalRect);
        return BitmapUtils.nv21ToBitmap(rawData, PREVIEW_WIDTH, PREVIEW_HEIGHT, finalRect);
    }

    /**
     * 根据targetFace人脸的位置，对原始数据进行人脸抠图
     *
     * @param targetFace
     * @param rawData
     * @return
     */
    private Bitmap getClipFace(RKFaceDO targetFace, byte[] rawData) {

        final Rect faceRect = targetFace.toRect(PREVIEW_WIDTH, PREVIEW_HEIGHT);
        final Rect finalRect = FaceRectUtils.scaleRect(faceRect, PREVIEW_WIDTH, PREVIEW_HEIGHT, 1.6f);
        Logger.i("finalRect:" + finalRect);
        return BitmapUtils.nv21ToBitmap(rawData, PREVIEW_WIDTH, PREVIEW_HEIGHT, finalRect);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RKAlliance.getInstance().unregisterFaceListener(faceCallback);
        RKAlliance.getInstance().unregisterLPRCallback(lprCallback);
        RKAlliance.getInstance().releaseFaceSdk();
        RKAlliance.getInstance().releasePlateSdk();

        RKGlassDevice.getInstance().removeOnPreviewFrameListener(onPreviewFrameListener);
        RKGlassDevice.getInstance().deInit();
    }
}
