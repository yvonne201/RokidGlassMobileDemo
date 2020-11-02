package com.rokid.alliancedemo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDevice;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.service.autofill.UserData;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.jiangdg.usbcamera.RKGlassDevice;
import com.jiangdg.usbcamera.callback.OnGlassConnectListener;
import com.rokid.alliance.base.BaseLibrary;
import com.rokid.alliance.base.annotation.RecognizeType;
import com.rokid.alliance.base.hw.GlassInfo;
import com.rokid.alliance.base.model.RKFaceDO;
import com.rokid.alliance.base.model.RKFaceModel;
import com.rokid.alliance.base.model.face.database.UserInfo;
import com.rokid.alliance.base.pc.ErrorCode;
import com.rokid.alliance.base.pc.bean.BatchPersons;
import com.rokid.alliance.base.pc.bean.DeployInfo;
import com.rokid.alliance.base.pc.bean.DeployTask;
import com.rokid.alliance.base.pc.bean.DeployTaskListInfo;
import com.rokid.alliance.base.pc.bean.ExtractFeatResult;
import com.rokid.alliance.base.pc.bean.FeatFileInfo;
import com.rokid.alliance.base.pc.bean.Person;
import com.rokid.alliance.base.utils.BitmapUtils;
import com.rokid.alliancedemo.util.FileManager;
import com.rokid.alliancedemo.util.Utils;
import com.rokid.glass.m_glass.RKGlassUI;
import com.rokid.mcui.utils.Logger;
import com.rokid.mobile.magic.RKAlliance;
import com.rokid.mobile.magic.callback.Callback;
import com.rokid.mobile.magic.callback.PreparedListener;
import com.rokid.mobile.magic.database.FaceDataManager;
import com.rokid.mobile.magic.database.FaceIdManager;
import com.serenegiant.usb.common.AbstractUVCCameraHandler;

import java.io.File;
import java.lang.reflect.Type;
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
    private TextView tvNameResult, tvCardResult, tvDBdata;
    private ImageView imageHeadResult;
    private Uri faceUri;

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
        initOfflineGlass();
    }


    /**
     * 初始化UI
     */
    private void initView() {
        etName = findViewById(R.id.et_name);
        etIdCard = findViewById(R.id.et_idcard);
        imageHead = findViewById(R.id.iv_face_image);
        tvNameResult = findViewById(R.id.tv_name_result);
        tvCardResult = findViewById(R.id.tv_card_result);
        imageHeadResult = findViewById(R.id.img_head_result);

        tvDBdata = findViewById(R.id.tv_db_data);

        imageHead.setOnClickListener(v -> {
            importPictures();
        });
        findViewById(R.id.btn_add_face).setOnClickListener(v -> {
            addData();
        });

        findViewById(R.id.btn_query).setOnClickListener(v -> {
            queryData();
        });

        findViewById(R.id.btn_update).setOnClickListener(v -> {
            updateData();
        });

        findViewById(R.id.btn_delete).setOnClickListener(v -> {
            deleteData();
        });

        findViewById(R.id.btn_batch_add).setOnClickListener(v -> {
            // 批量添加数据
            batchAddData();
        });
    }


    /**
     * 初始化离线识别
     */
    private void initOfflineGlass() {
        BaseLibrary.initialize(getApplication());


        RKGlassUI.getInstance().initGlassUI(getApplicationContext());
        // 单人离线识别
        RKGlassUI.getInstance().recogSettingChanged(RecognizeType.IS_SINGLE_RECOGNIZE, false);
        // 多人离线识别
//        RKGlassUI.getInstance().recogSettingChanged(RecognizeType.IS_MULTI_RECOGNIZE, false);

        // 初始化usb连接设备，mpreView不能为空，如不想见可设置view为1dp大小
        RKGlassDevice.RKGlassDeviceBuilder.buildRKGlassDevice().build().initUsbDevice(this, findViewById(R.id.uvc_preview), new OnGlassConnectListener() {
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
     * 查询数据
     */
    private void queryData() {
        // 获取离线数据库保存的数据总数
        int dataCount = FaceIdManager.getInstance().getAllUserNum();

        // 关键词[关键词是根据姓名或者身份证号信息进行模糊查询],数据库起始位置，查询的数量
        DeployTaskListInfo deployTaskList = FaceDataManager.getInstance().getDeployTaskListByOffset(null, 0, dataCount);
        List<DeployTask> deployTasks = deployTaskList.getDeployTasks();

        StringBuffer stringBuffer = new StringBuffer();
        for (DeployTask deployTask : deployTasks) {
            // 查询到的姓名,身份证号
            stringBuffer.append(deployTask.getName() + "," + deployTask.getCardNo() + "\n");
            // 根据特征id 来返回用户图片；【特征id为人脸图片存入数据库时自动生成的】
            Bitmap bitmap = FaceIdManager.getInstance().getUserImageByFid(deployTask.getCoverId());
            if (bitmap != null) {
                imageHead.setImageBitmap(bitmap);
            }
        }
        tvDBdata.setText("数据库查询结果为：：\n " + stringBuffer.toString());
    }

    /**
     * 更新数据
     */
    private void updateData() {
        // demo默认只修改第一条数据
        DeployTaskListInfo deployTaskList = FaceDataManager.getInstance().getDeployTaskListByOffset(null, 0, 1);
        List<DeployTask> deployTasks = deployTaskList.getDeployTasks();
        if (deployTasks.size() > 0) {
            Person person = new Person();
            person.setName("新名字");
            person.setCardNo("77");
            // uuid 为数据添加时自动生成的唯一随机id；
            String uuid = deployTasks.get(0).getId();
            // 更新数据, 【如需要修改图片，则可以参考addData()中的featFileInfos 作为第三个参数传入】
            ErrorCode errorCode = FaceDataManager.getInstance().updatePerson(uuid, person, null);
            if (errorCode.getCode() == 0) {
                Toast.makeText(this, "离线人脸数据添加成功", Toast.LENGTH_LONG).show();
                queryData();
            }
        }
    }

    /**
     * 删除数据
     */
    private void deleteData() {
        List<String> deleteUidList = new ArrayList<>();

        DeployTaskListInfo deployTaskList = FaceDataManager.getInstance().getDeployTaskListByOffset(null, 0, 1);
        List<DeployTask> deployTasks = deployTaskList.getDeployTasks();
        for (DeployTask item : deployTasks) {
            // uuid 为数据添加时自动生成的唯一随机id；
            deleteUidList.add(item.getId());
        }

        ErrorCode errorCode = FaceDataManager.getInstance().deletePersons(deleteUidList);
        if (errorCode.getCode() == 0) {
            Toast.makeText(this, "离线人脸数据删除成功", Toast.LENGTH_LONG).show();
            queryData();
        }
    }


    /**
     * 添加离线人脸数据
     */
    private void addData() {
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
     * 批量添加人脸数据
     */
    private void batchAddData() {
        // 此为mock布控包数据
        DeployInfo deployInfo = new DeployInfo();
        deployInfo.setName("布控包名");
        deployInfo.setExpireTime(System.currentTimeMillis() + 60 * 60 * 24 * 7 * 1000); // 布控包有效期
        deployInfo.setNewFriendAlarm(true);
        deployInfo.setNewFriendDesc("");

        // 人脸图片压缩文件  图片名称需要与Json文件中的图片名一致。 此外，请注意如果images.zip文件是有images文件夹压缩的，则json中图片路径需要添加images/xxx.jpg
        String fileName = "images.zip";
        String newFilePath = this.getFilesDir().getAbsolutePath() + File.separator + fileName;
        Logger.i("FileManager:: newFilePath:" + newFilePath);

        FileManager.copyAssetsFile2Phone(this, fileName, newFilePath);

        String batchDataJson = FileManager.getJson(this, "batchPerson.json");

        List<BatchPersons> batchPersonsList = new Gson().fromJson(batchDataJson, new TypeToken<List<BatchPersons>>() {
        }.getType());

        // 批量添加人脸数据信息
        FaceDataManager.getInstance().addPersons(deployInfo, newFilePath, batchPersonsList, true);
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
        RKGlassUI.getInstance().removeGlassUI();
    }
}
