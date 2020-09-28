package com.rokid.alliancedemo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.hardware.usb.UsbDevice;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.jiangdg.usbcamera.RKGlassDevice;
import com.jiangdg.usbcamera.callback.OnGlassConnectListener;
import com.rokid.alliance.base.BaseLibrary;
import com.rokid.alliance.base.hw.GlassInfo;
import com.rokid.alliance.base.model.RKFaceDO;
import com.rokid.alliance.base.model.RKFaceModel;
import com.rokid.alliance.base.model.face.database.UserInfo;
import com.rokid.alliance.base.pc.bean.ExtractFeatResult;
import com.rokid.alliance.base.pc.bean.FeatFileInfo;
import com.rokid.alliance.base.pc.bean.Person;
import com.rokid.alliance.base.utils.BitmapUtils;
import com.rokid.alliancedemo.util.Utils;
import com.rokid.mcui.utils.Logger;
import com.rokid.mobile.magic.RKAlliance;
import com.rokid.mobile.magic.callback.Callback;
import com.rokid.mobile.magic.callback.PreparedListener;
import com.rokid.mobile.magic.database.FaceDataManager;
import com.rokid.mobile.magic.database.FaceIdManager;
import com.serenegiant.usb.common.AbstractUVCCameraHandler;
import com.serenegiant.usb.widget.UVCCameraTextureView;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: zhangshengwei
 * Date: 2020/9/28
 * 人脸离线识别，包含离线数据导入
 */
public class DemoRKOfflineActivity extends AppCompatActivity {

    private static final String TAG = "DemoRKOffineActivity::";
    private static final String IMAGE_TYPE = "image/*";
    private static int CODE_IMAGE = 10024;

    private EditText etName, etIdCard;
    private ImageView imageHead;
    private  UVCCameraTextureView mPreView;
    private TextView tvNameResult, tvCardResult;
    private ImageView imageHeadResult;
    private Uri faceUri;

    final AbstractUVCCameraHandler.OnPreViewResultListener onPreviewFrameListener = new AbstractUVCCameraHandler.OnPreViewResultListener() {
        @Override
        public void onPreviewResult(byte[] bytes) {
            RKAlliance.getInstance().setData(bytes);
        }
    };

    /**
     * 人脸识别成功监听回调
     */
    final Callback<RKFaceModel> faceCallback = new Callback<RKFaceModel>() {
        @Override
        public void onDataResult(RKFaceModel rkFaceModel, byte[] bytes) {
            if (rkFaceModel.getFaceList() == null) {
                return;
            }
            RKFaceDO faceDO = rkFaceModel.getMaxFace();
            Log.d(TAG, "onDataResult: ::: 【离线识别成功过】");
            if (faceDO.featid != null) {
                tvNameResult.post(() -> {
                    UserInfo userInfo = FaceIdManager.getInstance().getUserInfoByFid(faceDO.featid);
                    // 显示识别到的信息
                    Bitmap bm = FaceIdManager.getInstance().getUserImageByFid(faceDO.featid);
                    bm = BitmapUtils.GetRoundedCornerBitmap(bm);
                    if (bm != null) {
                        imageHeadResult.setImageBitmap(bm);
                    }
                    tvNameResult.setText(userInfo.getName());
                    tvCardResult.setText(userInfo.getCardno());
                });
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_r_k_offline);
        initView();
        initGlass();
    }


    /**
     * 初始化UI空间
     */
    private void initView() {
        etName = findViewById(R.id.et_name);
        etIdCard = findViewById(R.id.et_idcard);
        imageHead = findViewById(R.id.iv_face_image);

        tvNameResult = findViewById(R.id.tv_name_result);
        tvCardResult = findViewById(R.id.tv_card_result);
        imageHeadResult = findViewById(R.id.img_head_result);
        mPreView = findViewById(R.id.uvc_preview);
        imageHead.setOnClickListener(v -> {
            importPictures();
        });
        findViewById(R.id.btn_add_face).setOnClickListener(v -> {
            addImportData();
        });
    }


    /**
     * 初始化Glass
     */
    private void initGlass() {
        BaseLibrary.initialize(getApplication());

        // 初始化usb连接设备，mpreView不能为空，如不想见可设置view为1dp大小

        RKGlassDevice.RKGlassDeviceBuilder.buildRKGlassDevice().build().initUsbDevice(this, mPreView, new OnGlassConnectListener() {
            @Override
            public void onGlassConnected(UsbDevice usbDevice, GlassInfo glassInfo) {

            }

            @Override
            public void onGlassDisconnected() {

            }
        });

        // 加载人脸模型
        RKAlliance.getInstance().loadFaceModel(getApplicationContext(), new PreparedListener() {
            @Override
            public void onPrepared() {
                // 初始化人脸数据库
                FaceIdManager.getInstance().init(getApplicationContext());
                FaceDataManager.getInstance().init(getApplicationContext());
            }
        });

        // 初始化人脸SDK
        RKAlliance.getInstance().initFaceSDK(getApplicationContext(), Utils.getInstance().getRoi(), new PreparedListener() {
            @Override
            public void onPrepared() {
                RKGlassDevice.getInstance().setOnPreviewFrameListener(onPreviewFrameListener);
            }
        });

        // 注册人脸识别监听
        RKAlliance.getInstance().registerFaceListener(faceCallback);
    }



    /**
     * 选择导入的图片
     */
    private void importPictures() {
        // 相册中选取图片
        Intent imageIntent = new Intent();
        if (Build.VERSION.SDK_INT < 19) {
            imageIntent.setAction(Intent.ACTION_GET_CONTENT);
            imageIntent.setType(IMAGE_TYPE);
        } else {
            imageIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imageIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_TYPE);
        }
        startActivityForResult(imageIntent, CODE_IMAGE);
    }

    /**
     * 保存离线人脸数据
     */
    @SuppressLint("ShowToast")
    private void addImportData() {
        if (TextUtils.isEmpty(etName.getText().toString()) || TextUtils.isEmpty(etIdCard.getText().toString())) {
            Toast.makeText(this, "姓名和身份证信息不能为空!!! ", Toast.LENGTH_LONG).show();
            return;
        }
        if (faceUri == null) {
            Toast.makeText(this, "人脸图片不能为空!!! ", Toast.LENGTH_LONG).show();
            return;
        }
        // 数据存入数据库
        Person person = new Person();
        person.setName(etName.getText().toString());
        person.setCardNo(etIdCard.getText().toString());

        // 图片路径
        String faceFilePath = Utils.getRealFilePath(getApplicationContext(), faceUri);
        Log.d(TAG, "addImportData: faceFilePath:::" + faceFilePath);
        final List<FeatFileInfo> featFileInfos = new ArrayList<>();
        // 提取特征的返回值
        final ExtractFeatResult extractFeatResult = FaceDataManager.getInstance().extractFeat(BitmapFactory.decodeFile(faceFilePath));
        if (extractFeatResult.getResultCode() == 0) {
            // 特征提取成功添加到数据库中
            FeatFileInfo featFileInfo = new FeatFileInfo();
            featFileInfo.setFilePath(faceFilePath);
            featFileInfos.add(featFileInfo);
            // 添加离线人脸数据
            FaceDataManager.getInstance().addPerson(person, featFileInfos, null, true);
            Toast.makeText(this, "离线人脸数据添加成功", Toast.LENGTH_LONG).show();
            // 重新加载数据模型
            reloadSdk();
        } else {
            Toast.makeText(this, extractFeatResult.getResultMsg() + "", Toast.LENGTH_LONG).show();
        }
    }


    /**
     * 新增数据时需要重新loadsdk
     */
    private void reloadSdk() {
        RKAlliance.getInstance().releaseFaceSdk();
        RKAlliance.getInstance().loadFaceModel(getApplicationContext(), new PreparedListener() {
            @Override
            public void onPrepared() {

            }
        });

        RKAlliance.getInstance().initFaceSDK(getApplicationContext(), Utils.getInstance().getRoi(), new PreparedListener() {
            @Override
            public void onPrepared() {

            }
        });

        RKAlliance.getInstance().registerFaceListener(faceCallback);
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CODE_IMAGE && null != data && null != data.getData()) {
            Logger.i("commit face path: face Uri:" + data.getData());
            faceUri = data.getData();
            imageHead.setImageURI(faceUri);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        RKAlliance.getInstance().releaseFaceSdk();
        RKGlassDevice.getInstance().removeOnPreviewFrameListener(onPreviewFrameListener);
        RKGlassDevice.getInstance().deInit();
    }
}
