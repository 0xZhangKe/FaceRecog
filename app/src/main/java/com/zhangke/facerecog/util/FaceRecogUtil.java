package com.zhangke.facerecog.util;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;

import com.arcsoft.facedetection.AFD_FSDKEngine;
import com.arcsoft.facedetection.AFD_FSDKError;
import com.arcsoft.facedetection.AFD_FSDKFace;
import com.arcsoft.facedetection.AFD_FSDKVersion;
import com.arcsoft.facerecognition.AFR_FSDKEngine;
import com.arcsoft.facerecognition.AFR_FSDKFace;
import com.arcsoft.facetracking.AFT_FSDKEngine;
import com.arcsoft.facetracking.AFT_FSDKFace;
import com.guo.android_extend.image.ImageConverter;
import com.zhangke.facerecog.common.Config;

import java.util.ArrayList;
import java.util.List;

/**
 * 人脸识别工具类
 * <p>
 * Created by ZhangKe on 2017/11/8.
 */

public class FaceRecogUtil {

    private static final String TAG = "FaceRecogUtil";

    /**
     * 检测人脸
     *
     * @return null:FD人脸检测引擎初始化失败；empty:未检测到人脸
     */
    public static List<AFD_FSDKFace> detectionFace(byte[] bytes, int width, int height) {
        List<AFD_FSDKFace> AFD_Result_List = new ArrayList<AFD_FSDKFace>();
        AFD_FSDKEngine AFD_Engine = new AFD_FSDKEngine();   //人脸检测引擎
        AFD_FSDKVersion AFD_Version = new AFD_FSDKVersion();
        AFD_FSDKError AFD_Rerr = AFD_Engine.AFD_FSDK_InitialFaceEngine(Config.appid, Config.fd_key, AFD_FSDKEngine.AFD_OPF_0_HIGHER_EXT, 16, 5);

        Log.d(TAG, "AFD_FSDK_InitialFaceEngine = " + AFD_Rerr.getCode());

        if (AFD_Rerr.getCode() != AFD_FSDKError.MOK) {
            return null;
        }
        AFD_Rerr = AFD_Engine.AFD_FSDK_GetVersion(AFD_Version);
        Log.d(TAG, "AFD_FSDK_GetVersion =" + AFD_Version.toString() + ", " + AFD_Rerr.getCode());
        AFD_Rerr = AFD_Engine.AFD_FSDK_StillImageFaceDetection(bytes, width, height, AFD_FSDKEngine.CP_PAF_NV21, AFD_Result_List);
        Log.d(TAG, "AFD_FSDK_StillImageFaceDetection =" + AFD_Rerr.getCode() + "<" + AFD_Result_List.size());

        return AFD_Result_List;
    }

    /**
     * 格式转换
     */
    public static void convertBitmap(Bitmap bitmap, byte[] bytes) {
        ImageConverter convert = new ImageConverter();
        convert.initial(bitmap.getWidth(), bitmap.getHeight(), ImageConverter.CP_PAF_NV21);
        if (convert.convert(bitmap, bytes)) {
            Log.d(TAG, "convert ok!");
        }
        convert.destroy();
    }

    public static boolean findAndPairFace(Bitmap bitmap, AFR_FSDKFace sdkFace) {
        AFT_FSDKEngine AFT_engine = new AFT_FSDKEngine();
        AFT_engine.AFT_FSDK_InitialFaceEngine(Config.appid, Config.ft_key, AFT_FSDKEngine.AFT_OPF_0_HIGHER_EXT, 10, 1);
        byte[] bytes = new byte[bitmap.getWidth() * bitmap.getHeight() * 3 / 2];
        FaceRecogUtil.convertBitmap(bitmap, bytes);

        List<AFT_FSDKFace> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            list.clear();
            AFT_engine.AFT_FSDK_FaceFeatureDetect(bytes, bitmap.getWidth(), bitmap.getHeight(), AFT_FSDKEngine.CP_PAF_NV21, list);
            if (!list.isEmpty()) break;
        }
        if (!list.isEmpty()) {
            AFT_FSDKFace face = list.get(0).clone();
            facePair(bitmap, face.getRect(), face.getDegree(), sdkFace);
            list.clear();
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取人脸特征值
     */
    private static void facePair(Bitmap bitmap, Rect rect, int degree, AFR_FSDKFace face) {
        AFR_FSDKEngine AFR_engine = new AFR_FSDKEngine();
        AFR_engine.AFR_FSDK_InitialEngine(Config.appid, Config.fr_key);
        byte[] bytes = new byte[bitmap.getWidth() * bitmap.getHeight() * 3 / 2];
        FaceRecogUtil.convertBitmap(bitmap, bytes);
        AFR_engine.AFR_FSDK_ExtractFRFeature(bytes,
                bitmap.getWidth(),
                bitmap.getHeight(),
                AFR_FSDKEngine.CP_PAF_NV21,
                rect,
                degree,
                face);
    }

    /**
     * 判断该特征值是否正确
     */
    public static boolean isFaceFeature(byte[] bytes) {
        if (bytes.length != 22020) return false;
        for (int i = 0; i < 100; i++) {
            if (bytes[i] != 0) {
                return true;
            }
        }
        return false;
    }
}
