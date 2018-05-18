package com.zhangke.facerecog.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;

import com.zhangke.zlog.ZLog;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Bitmap 相关工具类
 * <p>
 * Created by ZhangKe on 2017/11/21.
 */

public class BitmapUtil {

    private static final String TAG = "BitmapUtil";

    /**
     * 压缩图片至指定大小,虽然指定的是KB，但是跟实际差很远，甚至查了十倍
     *
     * @param targetSize 目标大小
     */
    public static Bitmap compressBitmapToTargetSize(Bitmap image, int targetSize) {
        long startTime = System.currentTimeMillis();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, out);

        ZLog.i(TAG, String.format("图片压缩前大小：%sKB", out.toByteArray().length / 1024));

        float zoom = (float) Math.sqrt(targetSize * 1024.0 / (double) (image.getRowBytes() * image.getHeight()));

        if (zoom > 1.0) {
            ZLog.i(TAG, String.format("图片压缩后大小：%sKB，耗时：%sms", out.toByteArray().length / 1024, (System.currentTimeMillis() - startTime)));
            return Bitmap.createBitmap(image);
        }

        Matrix matrix = new Matrix();
        matrix.setScale(zoom, zoom);

        Bitmap result = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);

        out.reset();
        result.compress(Bitmap.CompressFormat.JPEG, 100, out);
        while (out.toByteArray().length > (targetSize * 1024)) {
            matrix.setScale(0.9f, 0.9f);
            result = Bitmap.createBitmap(result, 0, 0, result.getWidth(), result.getHeight(), matrix, true);
            out.reset();
            result.compress(Bitmap.CompressFormat.JPEG, 100, out);
        }
        ZLog.i(TAG, String.format("图片压缩后大小：%sKB，耗时：%sms", out.toByteArray().length / 1024, (System.currentTimeMillis() - startTime)));
        return result;
    }
    /**
     * 压缩图片至指定大小,虽然指定的是KB，但是跟实际差很远，甚至查了十倍
     *
     * @param targetSize 目标大小
     */
    public static Bitmap compressBitmapToTargetSizeAndRecyclerOld(Bitmap image, int targetSize) {
        long startTime = System.currentTimeMillis();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, out);

        ZLog.i(TAG, String.format("图片压缩前大小：%sKB", out.toByteArray().length / 1024));

        float zoom = (float) Math.sqrt(targetSize * 1024.0 / (double) (image.getRowBytes() * image.getHeight()));

        if (zoom > 1.0) {
            ZLog.i(TAG, String.format("图片压缩后大小：%sKB，耗时：%sms", out.toByteArray().length / 1024, (System.currentTimeMillis() - startTime)));
            return Bitmap.createBitmap(image);
        }

        Matrix matrix = new Matrix();
        matrix.setScale(zoom, zoom);

        Bitmap result = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);

        recycleBitmap(image);

        out.reset();
        result.compress(Bitmap.CompressFormat.JPEG, 100, out);
        while (out.toByteArray().length > (targetSize * 1024)) {
            matrix.setScale(0.9f, 0.9f);
            result = Bitmap.createBitmap(result, 0, 0, result.getWidth(), result.getHeight(), matrix, true);
            out.reset();
            result.compress(Bitmap.CompressFormat.JPEG, 100, out);
        }
        ZLog.i(TAG, String.format("图片压缩后大小：%sKB，耗时：%sms", out.toByteArray().length / 1024, (System.currentTimeMillis() - startTime)));
        return result;
    }

    /**
     * 指定宽高压缩图片
     *
     * @param originalBitmap
     */
    public static Bitmap compressBitmap(Bitmap originalBitmap,
                                        final float targetWidth,
                                        final float targetHeight) {
        while (true) {
            int ImgHeight = originalBitmap.getHeight();
            int ImgWidth = originalBitmap.getWidth();
            float hh = targetHeight;
            float ww = targetWidth;
            //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
            float ratio = 1.0F;//ratio=1表示不缩放
            if (ImgWidth > ww) {//如果宽度大的话根据宽度固定大小缩放
                ratio = (float) (ImgWidth / (double) ww);
            } else if (ImgHeight > hh) {//如果高度高的话根据宽度固定大小缩放
                ratio = (float) (ImgHeight / (double) hh);
            }
            int be = Math.round(ratio);
            if (be <= 1)
                break;

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            originalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
            InputStream is = new ByteArrayInputStream(baos.toByteArray());

            BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
            bitmapOptions.inSampleSize = be;
            originalBitmap = BitmapFactory.decodeStream(is, null, bitmapOptions);
        }
        return originalBitmap;
    }

    /**
     * 选择变换
     *
     * @param origin 原图
     * @param alpha  旋转角度，可正可负
     * @return 旋转后的图片
     */
    public static Bitmap rotateBitmap(Bitmap origin, float alpha) {
        if (origin == null) {
            return null;
        }
        int width = origin.getWidth();
        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        matrix.setRotate(alpha);
        // 围绕原地进行旋转
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        recycleBitmap(origin);
        return newBM;
    }

    /**
     * 剪切图片
     */
    public static Bitmap cropBitmap(Bitmap bitmap, Rect rect) {
        return Bitmap.createBitmap(bitmap, rect.left, rect.top, rect.width(), rect.height(), null, false);
    }

    public static byte[] getByteArray(Bitmap bitmap) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        return out.toByteArray();
    }

    /**
     * 将图片保存到文件中
     */
    public static boolean saveBitmapToFile(File file, Bitmap bitmap) {
        boolean success = false;
        try {
            boolean canSave = file.exists() || file.createNewFile();
            if (canSave) {
                try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                    success = true;
                } catch (IOException e) {
                    ZLog.e(TAG, "saveBitmapToDisk: ", e);
                }
            }
        } catch (Exception e) {
            ZLog.e(TAG, "saveBitmapToDisk: ", e);
        }
        return success;
    }

    /**
     * 销毁 Bitmap
     */
    public static void recycleBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }
            bitmap = null;
        }
    }
}
