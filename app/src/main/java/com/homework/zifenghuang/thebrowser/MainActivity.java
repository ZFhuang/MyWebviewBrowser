package com.homework.zifenghuang.thebrowser;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.WebView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, SearchView.OnQueryTextListener {

    private WebView nowWebView;
    private FloatingActionButton fab;
    private DrawerLayout drawer;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private SearchView searchView;
    private MenuItem searchItem;
    private Settings settings;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static void verifyStoragePermissions(Activity activity) {//储存访问权限
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupSoftware();
        initViews();
        setSupportActionBar(toolbar);
    }

    private Handler myHandler = new Handler() {//接收线程传来的命令
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    nowWebView.loadUrl((String) msg.obj);
                    break;
                default:
                    break;
            }
        }
    };

    //初始化文件
    private void setupSoftware() {
        verifyStoragePermissions(MainActivity.this);//调用访问权限
        File folder = new File(Environment.getExternalStorageDirectory(), "TheBrowser");
        if (!folder.exists() || !folder.isDirectory()) {
            folder.mkdir();
        }
        File bookmarksFolder = new File(folder, "Bookmarks");
        if (!bookmarksFolder.exists() || !bookmarksFolder.isDirectory()) {
            bookmarksFolder.mkdir();
        }
        File historyFolder = new File(folder, "History");
        if (!historyFolder.exists() || !historyFolder.isDirectory()) {
            historyFolder.mkdir();
        }
    }

    //初始化视图
    private void initViews() {
        fab = (FloatingActionButton) findViewById(R.id.main_fab);
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        toolbar.setTitle("欢迎");

        //悬浮按钮响应
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nowWebView.post(new Runnable() {//使用post等待响应
                    @Override
                    public void run() {
                        nowWebView.scrollTo(0, 0);//滚回顶部
                    }
                });
            }
        });

        try {
            File settingFile;
            File folder = new File(Environment.getExternalStorageDirectory(), "TheBrowser");
            settingFile = new File(folder.getPath(), "Settings");
            final ObjectInputStream ois = new ObjectInputStream(new FileInputStream(settingFile));
            settings = (Settings) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        //抽屉初始化
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        //首页
        if (nowWebView == null) {
            WebView webView = (NestedWebView) findViewById(R.id.nested_webview);
            webView = WebViewIniter.init(this, webView);
            nowWebView = webView;
        }
        String url = settings.webview_homePage;
        if (url != null) {
            nowWebView.loadUrl(url);
        } else {
            nowWebView.loadUrl("https://www.baidu.com");
        }
    }

    //返回键响应
    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (nowWebView.canGoBack()) {
            nowWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }


    //菜单响应器
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        MenuItem bookmark = menu.findItem(R.id.nav_newbookmark);
        if (bookmark != null) {
            menu.findItem(R.id.nav_newbookmark).setChecked(true);
            bookmark.setTitle("");
        }
        //设置搜索输入框的步骤

        //查找指定的MemuItem
        searchItem = menu.findItem(R.id.action_search);

        View view = MenuItemCompat.getActionView(searchItem);
        if (view != null) {
            searchView = (SearchView) view;
            //设置SearchView 的查询回调接口
            searchView.setOnQueryTextListener(this);
            searchView.setQueryHint("输入想要搜索的内容或网址");
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem bookmark = menu.findItem(R.id.nav_newbookmark);
        if (bookmark != null) {
            menu.findItem(R.id.nav_newbookmark).setChecked(true);

            if (BookmarkIniter.existBookmark(nowWebView.getUrl())) {
                bookmark.setTitle("删除此收藏");
            } else {
                bookmark.setTitle("添加到收藏夹");
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    //抽屉菜单响应器
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            nowWebView.loadUrl("https://www.baidu.com");
        } else if (id == R.id.nav_gallery) {
            nowWebView.loadUrl("https://www.bilibili.com");
        } else if (id == R.id.nav_slideshow) {
            nowWebView.loadUrl("https://www.zhihu.com/question/31316646");
        } else if (id == R.id.nav_manage) {
            nowWebView.loadUrl("https://www.weibo.com/mrquin33");
        } else if (id == R.id.nav_newtab) {
            nowWebView.loadUrl("https://www1.szu.edu.cn/szu.asp");
        } else if (id == R.id.nav_newbookmark) {
            if (item.getTitle().equals("添加到收藏夹")) {
                Bookmark bookmark = new Bookmark(0, nowWebView.getTitle(), nowWebView.getUrl(), Calendar.getInstance().getTime());
                BookmarkIniter.inputBookmark(bookmark);
            } else if (item.getTitle().equals("删除此收藏")) {
                BookmarkIniter.deleteBookmark(nowWebView.getUrl());
            }
        } else if (id == R.id.nav_share) {
            ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            // 将文本内容放到系统剪贴板里。
            cm.setText(nowWebView.getUrl());
            Toast.makeText(this, "复制成功", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, "链接");
            intent.putExtra(Intent.EXTRA_TEXT, nowWebView.getUrl());//extraText为文本的内容
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//为Activity新建一个任务栈
            startActivity(Intent.createChooser(intent, "分享"));//R.string.action_share同样是标题
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //当输入框确认时
    @Override
    public boolean onQueryTextSubmit(final String query) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                URL url;
                try {
                    url = new URL(query);
                    URLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    int state = ((HttpURLConnection) urlConnection).getResponseCode();
                    if (state == 200) {
                        Message message = null;
                        message = myHandler.obtainMessage();
                        message.what = 0;
                        message.obj = query;
                        myHandler.sendMessage(message);
                    } else {
                        String get = "https://www.baidu.com/s?wd=" + query;
                        Message message = null;
                        message = myHandler.obtainMessage();
                        message.what = 0;
                        message.obj = get;
                        myHandler.sendMessage(message);
                    }
                } catch (IOException e) {
                    String get = "https://www.baidu.com/s?wd=" + query;
                    Message message = null;
                    message = myHandler.obtainMessage();
                    message.what = 0;
                    message.obj = get;
                    myHandler.sendMessage(message);
                    e.printStackTrace();
                }
            }
        }).start();
        searchView.clearFocus();  //收起键盘
        searchView.onActionViewCollapsed();    //收起SearchView
        if (searchItem != null)
            searchItem.collapseActionView();
        return true;
    }

    //输入框文字改变时
    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    //toolbar元素被选中时
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //响应toolbar左边的home键
            case android.R.id.home:
                drawer.openDrawer(GravityCompat.START);
                onPrepareOptionsMenu(navigationView.getMenu());
                break;
            case R.id.action_bookmark:
                Intent bookmark = new Intent(MainActivity.this, BookmarkActivity.class);
                startActivityForResult(bookmark, 1);
                break;
            case R.id.action_history:
                Intent history = new Intent(MainActivity.this, HistoryActivity.class);
                startActivityForResult(history, 1);
                break;
            case R.id.action_settings:
                Intent setting = new Intent(MainActivity.this, SettingActivity.class);
                startActivityForResult(setting, 2);
                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1://来自Bookmark或History的响应，跳转到所点的网址
                if (resultCode == 1) {
                    String url;
                    url = data.getStringExtra("url");
                    if (url != null) {
                        nowWebView.loadUrl(url);
                    }
                }
                break;
            case 2://来自Setting的响应，重新载入WebView
                if (resultCode == 1) {
                    nowWebView = WebViewIniter.changeSetting(nowWebView);
                    nowWebView.reload();//rua
                }
                break;
        }
    }
}
