package com.zhangke.facerecog.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Created by 张可 on 2017/5/22.
 */

public class UiUtil {
    private static Toast toast;

    public static void showToast(final Context context, final String message) {
        showToast(context, message, false);
    }

    public static void showToast(final Context context, final String message, final boolean center) {
        if (toast == null) {
            toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        } else {
            toast.setText(message);
        }
        if (center) {
            toast.setGravity(Gravity.CENTER, 0, 0);
        }
        toast.show();
    }

    /**
     * 获取屏幕宽度
     */
    public static int getScreenWidth(Context context) {
        Resources resources = context.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        return dm.widthPixels;
    }

    /**
     * 获取屏幕高度
     */
    public static int getScreenHeight(Context context) {
        Resources resources = context.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        return dm.heightPixels;
    }

    /**
     * 设置ListView高度充满
     *
     * @param listView
     */
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    /**
     * 设置透明Dialog大小，默认为屏幕宽度的0.8，需要在Dialog.show之后调用
     *
     * @param dialog
     * @param context
     */
    public static void setDialogDefaultSize(Dialog dialog, Context context) {
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        DisplayMetrics d = context.getResources().getDisplayMetrics(); // 获取屏幕宽、高
        lp.width = (int) (d.widthPixels * 0.8); // 宽度设置为屏幕的0.8
        dialogWindow.setAttributes(lp);
    }

    /**
     * 设置状态栏颜色（沉浸式），兼容到android4.4
     *
     * @param activity
     * @param color
     */
    public static void setWindowColorRect(Activity activity, int color) {
        if (Build.VERSION.SDK_INT >= 14) {
            // 设置状态栏透明
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // 设置根布局的参数
            // 生成一个状态栏大小的矩形
            View statusView = createStatusView(activity, color);
            // 添加 statusView 到布局中
            ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
            decorView.addView(statusView);
            ViewGroup rootView = (ViewGroup) ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);
            rootView.setFitsSystemWindows(true);
            rootView.setClipToPadding(true);
        }
    }

    /**
     * 设置Window Title颜色<br/>
     *
     * @param activity activity
     */
    public static void setWindow(Activity activity) {
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = activity.getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
        } else {
//            setWindowColorRect(activity, activity.getResources().getColor(Color.TRANSPARENT));
        }
    }

    /**
     * 生成一个和状态栏大小相同的矩形条
     *
     * @param activity 需要设置的activity
     * @param color    状态栏颜色值
     * @return 状态栏矩形条
     */
    private static View createStatusView(Activity activity, int color) {
        // 获得状态栏高度
        int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        int statusBarHeight = activity.getResources().getDimensionPixelSize(resourceId);

        // 绘制一个和状态栏一样高的矩形
        View statusView = new View(activity);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                statusBarHeight);
        statusView.setLayoutParams(params);
        statusView.setBackgroundColor(activity.getResources().getColor(color));
        return statusView;
    }

    /**
     * 将px值转换为dip或dp值，保证尺寸大小不变
     * <p>
     * （DisplayMetrics类中属性density）
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) ((double) pxValue / (double) scale + 0.5);
    }

    /**
     * 将dip或dp值转换为px值，保证尺寸大小不变
     */
    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) ((double) dipValue * (double) scale + 0.5);
    }

    /**
     * 将px值转换为sp值，保证文字大小不变
     */
    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) ((double) pxValue / (double) fontScale + 0.5);
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     */
    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) ((double) spValue * (double) fontScale + 0.5);
    }

    /**
     * 获取缓存路径
     */
    public static String getDiskCacheDir(Context context) {
        String cachePath = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return cachePath;
    }
}
