package com.zhangke.facerecog.page.base;

import android.content.Context;

/**
 *  * BaseActivity 及 BaseFragment 必须提供的方法
 * Created by ZhangKe on 2018/5/18.
 */
public interface IBasePage {
    //使用Intent传递数据时的参数
    String INTENT_ARG_01 = "intent_01";
    String INTENT_ARG_02 = "intent_02";
    String INTENT_ARG_03 = "intent_03";
    String INTENT_ARG_04 = "intent_04";
    String INTENT_ARG_05 = "intent_05";

    void showRoundProgressDialog();

    void showRoundProgressDialog(String msg);

    void closeRoundProgressDialog();

    void showToastMessage(String msg);

    /**
     * 如果需要调用此方法，布局文件中必须包含 R.id.coordinator 控件
     */
    void showNoActionSnackbar(String msg);

    Context getContext();
}
