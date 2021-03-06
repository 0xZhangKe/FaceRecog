package com.zhangke.facerecog.page.base;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;

import com.zhangke.facerecog.util.UiUtil;
import com.zhangke.facerecog.widget.RoundProgressDialog;
import com.zld.facerecog.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Activity 基类
 * Created by ZhangKe on 2018/5/18.
 */
public abstract class BaseActivity  extends AppCompatActivity implements IBasePage {

    protected final String TAG = this.getClass().getSimpleName();

    private Snackbar snackbar;

    private RoundProgressDialog roundProgressDialog;

    private Handler mHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());
        roundProgressDialog = new RoundProgressDialog(this);
        mHandler = new Handler();
        initView(savedInstanceState);
    }

    protected abstract int getLayoutResId();

    protected abstract void initView(@Nullable Bundle savedInstanceState);

    /**
     * 全屏，隐藏状态栏
     */
    protected void fullScreen() {
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    protected void initToolbar(Toolbar toolbar, String title, boolean showBackBtn) {
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(showBackBtn);
        if (showBackBtn) {
            toolbar.setNavigationOnClickListener((View v) -> {
                onBackPressed();
            });
        }
    }

    @Override
    public void showToastMessage(final String msg) {
        runOnUiThread(() -> {
            UiUtil.showToast(BaseActivity.this, msg);
        });
    }

    /**
     * 显示圆形加载对话框，默认消息（请稍等...）
     */
    @Override
    public void showRoundProgressDialog() {
        runOnUiThread(() -> {
            if (roundProgressDialog != null && !roundProgressDialog.isShowing()) {
                roundProgressDialog.showProgressDialog();
            }
        });
    }

    /**
     * 显示圆形加载对话框
     *
     * @param msg 提示消息
     */
    @Override
    public void showRoundProgressDialog(final String msg) {
        runOnUiThread(() -> {
            if (roundProgressDialog != null && !roundProgressDialog.isShowing()) {
                roundProgressDialog.showProgressDialog(msg);
            }
        });
    }

    /**
     * 关闭圆形加载对话框
     */
    @Override
    public void closeRoundProgressDialog() {
        runOnUiThread(() -> {
            if (roundProgressDialog != null && roundProgressDialog.isShowing()) {
                roundProgressDialog.closeProgressDialog();
            }
        });
    }

    @Override
    public void showNoActionSnackbar(String msg) {
        snackbar = Snackbar.make(findViewById(R.id.coordinator), msg, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    @Override
    public Context getContext() {
        return this;
    }

    private Map<Short, Runnable> requestPermissionMap = new HashMap<>();

    /**
     * 判断是否具有改权限，不具备则申请
     *
     * @param runnable   获取到权限之后做的事情
     * @param permission 权限列表
     */
    protected void checkAndRequestPermission(Runnable runnable, String... permission) {
        List<String> permissionList = new ArrayList<>();
        for (String item : permission) {
            if (isLacksOfPermission(this, item)) {
                permissionList.add(item);
            }
        }
        if (permissionList.isEmpty()) {
            mHandler.post(runnable);
        } else {
            short requestCode = getRequestCode();
            requestPermissionMap.put(requestCode, runnable);
            ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]), requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Set<Short> requestCodeSet = requestPermissionMap.keySet();
        if (!requestCodeSet.isEmpty()) {
            for (Short key : requestCodeSet) {
                if (requestCode == (int) key) {
                    boolean get = true;
                    for (int i : grantResults) {
                        if (i != PackageManager.PERMISSION_GRANTED) {
                            get = false;
                            break;
                        }
                    }
                    if (get) {
                        Runnable runnable = requestPermissionMap.get(key);
                        if (runnable != null) {
                            mHandler.post(runnable);
                        }
                        requestPermissionMap.remove(key);
                        break;
                    }
                }
            }
        }
    }

    private short getRequestCode() {
        short requestCode = 1963;
        Set<Short> requestCodeSet = requestPermissionMap.keySet();
        if (!requestCodeSet.isEmpty()) {
            while (requestCodeSet.contains(requestCode)) {
                requestCode = (short) (Math.random() * 100);
            }
        }
        return requestCode;
    }

    private boolean isLacksOfPermission(Context context, String permission) {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) && ContextCompat.checkSelfPermission(
                context, permission) == PackageManager.PERMISSION_DENIED;
    }
}
