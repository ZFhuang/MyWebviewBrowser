package com.homework.zifenghuang.thebrowser;

import java.io.Serializable;

class Settings implements Serializable {
    private static final long serialVersionUID = 1L;

    //首页
    String webview_homePage="https://www.baidu.com";
    //开启放大
    boolean webview_setSupportZoom = true;
    //开启放大组件
    boolean webview_setBuiltInZoomControls = true;
    //开启自适应屏幕
    boolean webview_setUseWideViewPort = true;
    //开启js脚本
    boolean webview_setJavaScriptEnabled = true;
    //设置用户模式：PC
    boolean webSettings_PCmode = false;
    //开启页面缓存
    boolean webview_setDomStorageEnabled = true;
}
