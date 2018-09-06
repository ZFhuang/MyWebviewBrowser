package com.homework.zifenghuang.thebrowser;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Environment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.*;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Calendar;

import static android.content.Context.DOWNLOAD_SERVICE;

class WebViewIniter {
    @SuppressLint("ResourceAsColor")
    static WebView init(final Activity target, WebView input) {
        final WebView webView = input;
        final SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) webView.getParent();
        final Toolbar toolbar = (Toolbar) target.findViewById(R.id.main_toolbar);

        swipeRefreshLayout.setColorSchemeColors(R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                webView.reload();
            }
        });
        swipeRefreshLayout.setProgressViewOffset(true, 0, 20);
        swipeRefreshLayout.setDistanceToTriggerSync(5000);

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {//处理ERR_UNKNOWN_URL_SCHEME 问题，例如B站
                if (URLUtil.isNetworkUrl(url)) {
                    return false;
                } else {
                    try {
                        PackageManager pm = target.getPackageManager();
                        pm.getPackageInfo(url, PackageManager.GET_ACTIVITIES);
                        return true;
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                    return true;
                }
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
            }

            @Override
            public void onReceivedSslError(WebView webView, SslErrorHandler sslErrorHandler, SslError sslError) {
                if (sslError.getPrimaryError() == android.net.http.SslError.SSL_INVALID) {// 校验过程遇到了bug
                    sslErrorHandler.proceed();
                } else {
                    sslErrorHandler.cancel();
                }
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (!swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(true);
                }
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                if (!title.equals("")) {
                    toolbar.setTitle(title);
                    History history = new History(view.getTitle(), view.getUrl(), Calendar.getInstance().getTime());
                    HistoryIniter.inputHistory(history);
                }
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }

                super.onProgressChanged(view, newProgress);
            }
        });

        // 长按点击事件
        webView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                final WebView.HitTestResult hitTestResult = webView.getHitTestResult();
                // 如果是图片类型或者是带有图片链接的类型
                if (hitTestResult.getType() == WebView.HitTestResult.IMAGE_TYPE ||
                        hitTestResult.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
                    // 弹出保存图片的对话框
                    AlertDialog.Builder builder = new AlertDialog.Builder(target);
                    builder.setTitle("图片");
                    builder.setMessage("是否下载到本地");
                    builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String url = hitTestResult.getExtra();
                            DownloadManager downloadManager = (DownloadManager) target.getSystemService(DOWNLOAD_SERVICE);
                            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                            //下载到download文件夹
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).mkdir();
                            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, url.split("/")[url.split("/").length - 1]);
                            request.allowScanningByMediaScanner();
                            //显示状态
                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                            //开始下载并获取id
                            long id = downloadManager.enqueue(request);
                            Toast.makeText(target.getApplicationContext(), "下载中",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                    builder.setNegativeButton("否", new DialogInterface.OnClickListener() {
                        // 自动忽略
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                return true;
            }
        });

        //下载监听
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(final String url, final String userAgent, final String contentDisposition, final String mimetype, long contentLength) {
                AlertDialog.Builder builder = new AlertDialog.Builder(target);
                builder.setTitle("网页请求");
                builder.setMessage("是否下载到本地");
                builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //初始化下载器
                        DownloadManager downloadManager = (DownloadManager) target.getSystemService(DOWNLOAD_SERVICE);
                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

                        request.setMimeType(mimetype);
                        String cookies = CookieManager.getInstance().getCookie(url);
                        request.addRequestHeader("cookie", cookies);
                        request.addRequestHeader("User-Agent", userAgent);
                        request.setDescription("下载中...");
                        request.setTitle(URLUtil.guessFileName(url, contentDisposition,
                                mimetype));

                        //下载到download文件夹
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).mkdir();
                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, contentDisposition, mimetype));
                        request.allowScanningByMediaScanner();

                        //显示状态
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

                        //开始下载并获取id
                        long id = downloadManager.enqueue(request);
                        Toast.makeText(target.getApplicationContext(), "下载中",
                                Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton("否", new DialogInterface.OnClickListener() {
                    // 自动忽略
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        WebSettings webSettings = webView.getSettings();
        changeSetting(webView);
        webSettings.setBlockNetworkImage(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            //同时兼容https与http，解决部分图片不显示的问题
        }

        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        return webView;
    }

    static WebView changeSetting(WebView webView) {
        WebSettings webSettings = webView.getSettings();
        File settingFile = new File(Environment.getExternalStorageDirectory() + "/" + "TheBrowser", "Settings");
        if (!settingFile.exists() || settingFile.isDirectory()) {
            return webView;
        }
        final ObjectInputStream ois;
        try {
            ois = new ObjectInputStream(new FileInputStream(settingFile));
            Settings settings = (Settings) ois.readObject();
            if (settings == null) {
                settings = new Settings();
            }
            webSettings.setSupportZoom(settings.webview_setSupportZoom);//允许缩放
            webSettings.setBuiltInZoomControls(settings.webview_setBuiltInZoomControls);//打开缩放按钮
            webSettings.setUseWideViewPort(settings.webview_setUseWideViewPort);//广域打开
            webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);//自适应画面
            webSettings.setDomStorageEnabled(settings.webview_setDomStorageEnabled);
            webSettings.setJavaScriptEnabled(settings.webview_setJavaScriptEnabled);
            if (settings.webSettings_PCmode)
                webSettings.setUserAgentString("PC");//可以使用PC请求打开
            else
                webSettings.setUserAgentString("Mozilla/5.0 (Linux; Android 6.0; Android SDK built for x86 Build/MASTER; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/44.0.2403.119 Mobile Safari/537.36");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return webView;
    }
}
