package com.rokid.alliancedemo;

import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
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
import com.rokid.alliance.base.annotation.RecognizeType;
import com.rokid.alliance.base.hw.GlassInfo;
import com.rokid.alliance.base.model.RKFaceDO;
import com.rokid.alliance.base.model.RKFaceModel;
import com.rokid.alliance.base.model.RKLPRDO;
import com.rokid.alliance.base.model.RKLPRModel;
import com.rokid.alliance.base.model.face.database.UserInfo;
import com.rokid.alliance.base.model.plate.PlateInfo;
import com.rokid.alliance.base.model.plate.PlateManager;
import com.rokid.alliance.base.utils.BitmapUtils;
import com.rokid.glass.m_glass.RKGlassUI;
import com.rokid.mobile.magic.RKAlliance;
import com.rokid.mobile.magic.callback.Callback;
import com.rokid.mobile.magic.callback.PreparedListener;
import com.rokid.mobile.magic.database.FaceIdManager;
import com.serenegiant.usb.common.AbstractUVCCameraHandler;

import java.util.ArrayList;
import java.util.List;

import static com.rokid.alliance.base.utils.CameraParams.PREVIEW_HEIGHT;
import static com.rokid.alliance.base.utils.CameraParams.PREVIEW_WIDTH;

/**
 * Author: zhangshengwei
 * Date: 2020/10/19
 */
public class DemoRKOfflinePlateActivity extends AppCompatActivity {

    private static final String TAG = "DemoRKOfflinePlateActivity::";
    private EditText etPlateNum, etBrand, etOwner;
    private TextView tvDBData,tvPlateResult,tvOwnerResult;
    private ImageView imgPlateResult;


    /**
     * 预览的监听回调
     */
    final AbstractUVCCameraHandler.OnPreViewResultListener onPreviewFrameListener = new AbstractUVCCameraHandler.OnPreViewResultListener() {
        @Override
        public void onPreviewResult(byte[] bytes) {
            RKAlliance.getInstance().setData(bytes);
        }
    };

    /**
     * 车牌识别成功监听回调
     */
    final Callback<RKLPRModel> plateCallBack = new Callback<RKLPRModel>() {

        @Override
        public void onDataResult(RKLPRModel lprModel, byte[] bytes) {
            if (lprModel.lps == null || lprModel.lps.size() == 0){
                return;
            }
            Log.d(TAG, "onDataResult: ::: 【车牌离线识别成功过】");
            for (RKLPRDO lprItem: lprModel.lps) {
                if (lprItem.score > 0.97){
                    tvPlateResult.post(() -> {
                        String plateNum = lprItem.licensePlate;
                        tvPlateResult.setText(plateNum);
                        Bitmap bm = BitmapUtils.nv21ToBitmap(bytes, PREVIEW_WIDTH, PREVIEW_HEIGHT, lprItem.toRect(PREVIEW_WIDTH, PREVIEW_HEIGHT));
                        imgPlateResult.setImageBitmap(bm);

                        PlateInfo plateInfo = PlateManager.getInstance().queryPlateInfo(plateNum);
                        if (plateInfo !=null){
                            tvOwnerResult.setText("品牌：" + plateInfo.getBrand() + "， 车主：" + plateInfo.getOwner());
                        }else{
                            tvOwnerResult.setText("离线数据库暂无其他信息");
                        }

                    });


                }
            }
        }
    };



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_r_k_offline_plate);
        initView();
        initGlassView();
    }


    private void initView() {
        etPlateNum = findViewById(R.id.et_plate_num);
        etBrand = findViewById(R.id.et_brand);
        etOwner = findViewById(R.id.et_owner);
        tvDBData = findViewById(R.id.tv_db_data);
        imgPlateResult = findViewById(R.id.img_plate_result);
        tvOwnerResult = findViewById(R.id.tv_owner_result);

        tvPlateResult = findViewById(R.id.tv_plate_result);

        findViewById(R.id.btn_add_plate).setOnClickListener(v -> {
            addPlate();
        });

        findViewById(R.id.btn_query).setOnClickListener(v -> {
            queryPlate();
        });

        findViewById(R.id.btn_update).setOnClickListener(v -> {
            updatePlate(etPlateNum.getText().toString());
        });

        findViewById(R.id.btn_delete).setOnClickListener(v -> {
            deletePlate(etPlateNum.getText().toString());
        });

    }


    private void initGlassView() {
        BaseLibrary.initialize(getApplication());

        // 设置为离线车牌识别
        BaseLibrary.getInstance().setRecogType(RecognizeType.IS_PLATE_RECOGNIZE);
        BaseLibrary.getInstance().setIsOnline(false);
        RKGlassUI.getInstance().initGlassUI(getApplicationContext());

        // 初始化usb连接设备，mpreView不能为空，如不想见可设置view为1dp大小
        RKGlassDevice.RKGlassDeviceBuilder.buildRKGlassDevice().build().initUsbDevice(this, findViewById(R.id.uvc_preview), new OnGlassConnectListener() {
            @Override
            public void onGlassConnected(UsbDevice usbDevice, GlassInfo glassInfo) {

            }

            @Override
            public void onGlassDisconnected() {

            }
        });


        RKAlliance.getInstance().registerLPRCallback(plateCallBack);

        // 加载车牌模型
        RKAlliance.getInstance().loadLPRModel(getApplicationContext(), new PreparedListener() {
            @Override
            public void onPrepared() {

            }
        });

        // 初始化车牌sdk
        RKAlliance.getInstance().initPlateSDK(new PreparedListener() {
            @Override
            public void onPrepared() {

            }
        });

    }



    private void addPlate() {
        String plateNumStr = etPlateNum.getText().toString();
        String brandStr = etBrand.getText().toString();
        String ownerStr = etOwner.getText().toString();

        if (TextUtils.isEmpty(plateNumStr)) {
            Toast.makeText(this, "车牌号不能为空", Toast.LENGTH_LONG).show();
            return;
        }

        // com.rokid.alliance.base.model.plate.PlateInfo;
        PlateInfo plateInfo = new PlateInfo();
        plateInfo.setPlate(plateNumStr);
        plateInfo.setBrand(!TextUtils.isEmpty(brandStr) ? brandStr : "");
        plateInfo.setOwner(!TextUtils.isEmpty(ownerStr) ? ownerStr : "");

        int insertResult = PlateManager.getInstance().addPlateInfo(plateInfo);
        String insertMessage = "";
        if (insertResult == 0) {
            insertMessage = "车牌数据插入成功";
        } else {
            insertMessage = "车牌数据插入失败";
        }

        Toast.makeText(this, insertMessage, Toast.LENGTH_LONG).show();
    }


    /**
     * 查询
     */
    private void queryPlate() {
        int plateDbCount = PlateManager.getInstance().getPlateCount();
        List<PlateInfo> plateInfoList = PlateManager.getInstance().queryPlateInfoListByOffset(0, plateDbCount, null);
        StringBuffer plateInforStr = new StringBuffer();
        for (PlateInfo plateItem : plateInfoList) {
            plateInforStr.append(plateItem.getPlate() + ", " + plateItem.getBrand() + ", " + plateItem.getOwner() + "\n");
        }

        tvDBData.setText(plateInforStr.toString());
    }

    /**
     * 删除
     * @param plate
     */
    private void deletePlate(String plate) {
        if (TextUtils.isEmpty(plate)) {
            Toast.makeText(this, "请输入要删除的车牌号", Toast.LENGTH_LONG).show();
            return;
        }

        PlateInfo plateInfo = PlateManager.getInstance().queryPlateInfo(plate);
        if (plateInfo == null){
            Toast.makeText(this, "没有该车牌数据信息", Toast.LENGTH_LONG).show();
            return;
        }
        List<String> deleteIdList = new ArrayList<>();
        deleteIdList.add(plateInfo.getPlate());
        int deleteResult = PlateManager.getInstance().deletePlates(false, deleteIdList);
        String deleteMessage;
        if (deleteResult == 0){
            deleteMessage = "车牌数据删除成功";
        }else{
            deleteMessage = "车牌数据删除失败";
        }
        Toast.makeText(this, deleteMessage, Toast.LENGTH_SHORT).show();
    }


    /**
     * 更新
     */
    private void updatePlate(String plate) {
        if (TextUtils.isEmpty(plate)) {
            Toast.makeText(this, "请输入要更新的车牌号", Toast.LENGTH_LONG).show();
            return;
        }
        PlateInfo plateInfo = PlateManager.getInstance().queryPlateInfo(plate);
        if (plateInfo == null){
            Toast.makeText(this, "没有该车牌数据信息", Toast.LENGTH_LONG).show();
            return;
        }
        plateInfo.setBrand(etBrand.getText().toString());
        plateInfo.setOwner(etOwner.getText().toString());
        int updateResult = PlateManager.getInstance().updatePlateInfo(plateInfo);
        String updateMessage;
        if (updateResult == 0){
            updateMessage = "车牌数据更新成功";
        }else{
            updateMessage = "车牌数据更新失败";
        }
        Toast.makeText(this, updateMessage, Toast.LENGTH_SHORT).show();

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        RKAlliance.getInstance().releasePlateSdk();
        RKGlassDevice.getInstance().removeOnPreviewFrameListener(onPreviewFrameListener);
        RKGlassDevice.getInstance().deInit();
        RKGlassUI.getInstance().removeGlassUI();

    }
}
