package com.zhangke.facerecog.util;

import android.graphics.Bitmap;

import com.zhangke.zlog.ZLog;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * 文件相关操作类
 * Created by ZhangKe on 2018/4/8.
 */
public class FileUtil {

    private static final String TAG = "FileUtil";

    /**
     * 读取文件内容
     *
     * @return 文件内容文本
     */
    public static String readFile(File file) throws IOException {
        String text = "";
        if (file.exists() && file.isFile()) {
            try (FileInputStream fis = new FileInputStream(file);
                 InputStreamReader isr = new InputStreamReader(fis);
                 BufferedReader bufferedReader = new BufferedReader(isr)) {
                String lineTxt;
                StringBuilder builder = new StringBuilder();
                while ((lineTxt = bufferedReader.readLine()) != null) {
                    builder.append(lineTxt);
                    builder.append("\n");
                }
                text = builder.toString();
            }
        }
        return text;
    }

    /**
     * 讲一个字符串按照 UTF-8 格式写入文件
     *
     * @return 是否写入成功
     */
    public static boolean writeFile(File file, String text) throws IOException {
        boolean success = false;
        boolean canWrite = true;
        if (!file.exists()) {
            if (!file.createNewFile()) {
                canWrite = false;
            }
        }
        if (canWrite) {
            try (FileOutputStream fos = new FileOutputStream(file);
                 OutputStreamWriter osr = new OutputStreamWriter(fos, "UTF-8");
                 BufferedWriter bufferedWriter = new BufferedWriter(osr)) {
                bufferedWriter.write(text);
                success = true;
            }
        }
        return success;
    }

    /**
     * 删除文件
     *
     * @return 是否删除成功
     */
    public static boolean deleteFile(File file) {
        boolean success = false;
        if (file != null && file.exists()) {
            success = file.delete();
        }
        return success;
    }

    /**
     * 删除指定目录下所有文件及文件夹
     *
     * @param dir 需要删除的文件夹（此文件夹也会被删除）
     */
    public static void deleteDir(File dir) {
        if (dir.exists()) {
            if (dir.isDirectory()) {
                String[] children = dir.list();
                for (String itemFile : children) {
                    deleteDir(new File(dir, itemFile));
                }
                if (!dir.delete()) {
                    ZLog.e(TAG, String.format("文件%s删除失败", dir.getPath()));
                }
            } else {
                if (dir.isFile()) {
                    if (!dir.delete()) {
                        ZLog.e(TAG, String.format("文件%s删除失败", dir.getPath()));
                    }
                }
            }
        }
    }

    /**
     * 将图片保存到文件中
     */
    public static boolean saveBitmapToDisk(File file, Bitmap originBitmap) {
        boolean success = false;
        try {
            if (!file.exists()) {
                if (file.createNewFile()) {
                    try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))) {
                        originBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                        success = true;
                    } catch (IOException e) {
                        ZLog.e(TAG, "saveBitmapToDisk: ", e);
                    }
                }
            }
        } catch (Exception e) {
            ZLog.e(TAG, "saveBitmapToDisk: ", e);
        }
        return success;
    }
}
