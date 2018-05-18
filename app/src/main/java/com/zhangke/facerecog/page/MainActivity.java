package com.zhangke.facerecog.page;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.arcsoft.facetracking.AFT_FSDKEngine;
import com.arcsoft.facetracking.AFT_FSDKFace;
import com.zhangke.facerecog.common.Config;
import com.zhangke.facerecog.page.base.BaseActivity;
import com.zhangke.facerecog.util.BitmapUtil;
import com.zhangke.facerecog.util.FaceRecogUtil;
import com.zhangke.facerecog.util.FileUtil;
import com.zhangke.facerecog.widget.RectView;
import com.zhangke.zlog.ZLog;
import com.zld.facerecog.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by ZhangKe on 2018/5/18.
 */
public class MainActivity extends BaseActivity {

    private int CAMERA_SELECT = 10012;

    @BindView(R.id.img_face)
    ImageView imgFace;
    @BindView(R.id.rect_view)
    RectView rectView;
    @BindView(R.id.tv_info)
    TextView tvInfo;

    private AFT_FSDKEngine AFT_engine = new AFT_FSDKEngine();

    protected BottomSheetDialog imageDialog;
    private boolean isTakePhoto = false;//是否为拍照获取
    private String selectImagePath;//图片缓存地址

    private CompositeDisposable mDisposable = new CompositeDisposable();

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView(@Nullable Bundle savedInstanceState) {
        ButterKnife.bind(this);

        selectImagePath = getExternalFilesDir(null).getPath() + "/tmp.jpg";

        //人脸检测
        AFT_engine.AFT_FSDK_InitialFaceEngine(Config.appid, Config.ft_key, AFT_FSDKEngine.AFT_OPF_0_HIGHER_EXT, 16, 1);
    }

    @OnClick(R.id.btn_select_photo)
    public void selectOrTakePhoto() {
        checkAndRequestPermission(this::showSelectImageDialog, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA);
    }

    @OnClick(R.id.btn_open_camera)
    public void openCamera() {

    }

    /**
     * 显示选择头像对话框
     */
    private void showSelectImageDialog() {
        if (imageDialog == null) {
            imageDialog = new BottomSheetDialog(this);
            View view = LayoutInflater.from(this).inflate(R.layout.dialog_select_photo, null);

            view.findViewById(R.id.tv_camera).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    imageDialog.cancel();
                    tackPhoto();
                }
            });
            view.findViewById(R.id.tv_album).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    imageDialog.cancel();
                    selectPhoto();
                }
            });
            view.findViewById(R.id.tv_exit).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    imageDialog.cancel();
                }
            });
            imageDialog.setContentView(view);
        }
        imageDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == CAMERA_SELECT) {
            notifyActivityResult(data);
        }
    }

    public void notifyActivityResult(Intent data) {
        Bitmap bitmap;
        try {
            if (isTakePhoto) {
                bitmap = BitmapFactory.decodeFile(selectImagePath);
            } else {
                ContentResolver resolver = getContentResolver();
                Uri imgUri = data.getData();
                bitmap = MediaStore.Images.Media.getBitmap(resolver, imgUri);
            }
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap = BitmapUtil.compressBitmapToTargetSize(bitmap, 2000);
            }
            recogFace(bitmap);
            imgFace.setImageBitmap(bitmap);
        } catch (Exception e) {
            ZLog.e(TAG, "notifyActivityResult()", e);
        }
    }

    private long spendTimeMs;

    private void recogFace(Bitmap faceBitmap) {
        Observable<List<AFT_FSDKFace>> observable = Observable.create(e -> {
            long start = System.currentTimeMillis();
            ZLog.d(TAG, "开始检测人脸");
            byte[] faceData = new byte[faceBitmap.getWidth() * faceBitmap.getHeight() * 3 / 2];
            FaceRecogUtil.convertBitmap(faceBitmap, faceData);
            List<AFT_FSDKFace> faces = new ArrayList<>();
            AFT_engine.AFT_FSDK_FaceFeatureDetect(faceData, faceBitmap.getWidth(), faceBitmap.getHeight(), AFT_FSDKEngine.CP_PAF_NV21, faces);
            spendTimeMs = System.currentTimeMillis() - start;
            ZLog.d(TAG, String.format("人脸检测完成，检测到%s张人脸，耗时%s", faces.size(), spendTimeMs));
            e.onNext(faces);
            e.onComplete();
        });
        mDisposable.add(observable.subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(faces -> {
                    if (faces.isEmpty()) {
                        rectView.clearRect();
                    } else {
                        int width = imgFace.getWidth();
                        int height = imgFace.getHeight();
                        for (AFT_FSDKFace face : faces) {
                            rectView.drawRect(face.getRect(), width, height);
                        }
                    }
                    tvInfo.setText(String.format("人脸检测完成\n检测到%s张人脸\n耗时%s", faces.size(), spendTimeMs));
                }));
    }

    private void selectPhoto() {
        // 相册获取
        isTakePhoto = false;
        Intent cameraIntent = new Intent(Intent.ACTION_GET_CONTENT);
        cameraIntent.setType("image/*");
        startActivityForResult(cameraIntent, CAMERA_SELECT);
    }

    private void tackPhoto() {
        // 拍照获取
        isTakePhoto = true;
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        File file = new File(selectImagePath);
        FileUtil.deleteFile(file);
        Uri photoUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            photoUri = FileProvider.getUriForFile(this, getPackageName() + ".fileProvider", file);
        } else {
            photoUri = Uri.fromFile(file);
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        startActivityForResult(intent, CAMERA_SELECT);
    }

    @Override
    protected void onDestroy() {
        if (mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
        super.onDestroy();
    }
}
