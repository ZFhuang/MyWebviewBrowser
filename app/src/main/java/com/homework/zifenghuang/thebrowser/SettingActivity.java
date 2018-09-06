package com.homework.zifenghuang.thebrowser;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import java.io.*;

public class SettingActivity extends AppCompatActivity {

    File folder = new File(Environment.getExternalStorageDirectory(), "TheBrowser");
    File settingFile;
    Settings settings;
    private Toolbar toolbar;
    private LinearLayout linearLayout;
    private boolean hasChanged = false;
    //private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        initViews();
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        loadSettings();
    }

    //初始化视图
    private void initViews() {
        toolbar = (Toolbar) findViewById(R.id.setting_toolbar);
        toolbar.setTitle("设置");
        linearLayout = (LinearLayout) findViewById(R.id.setting_listView);
    }

    private void loadSettings() {
        try {
            settingFile = new File(folder.getPath(), "Settings");
            if (!settingFile.exists() || settingFile.isDirectory()) {
                saveSettings();
            }
            final ObjectInputStream ois = new ObjectInputStream(new FileInputStream(settingFile));
            settings = (Settings) ois.readObject();
            changeView();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void defaultSettings() {
        settings = new Settings();
        saveSettings();
        loadSettings();
    }

    private void saveSettings() {
        try {
            settingFile = new File(folder.getPath(), "Settings");
            if (!settingFile.exists() || settingFile.isDirectory()) {
                settingFile.createNewFile();
                settings = new Settings();
            }
            final ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(settingFile));
            oos.writeObject(settings);
            hasChanged = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void changeView() {
        if (settings == null) {
            defaultSettings();
        }
        if (settings != null && linearLayout != null) {
            Switch webview_setSupportZoom = (Switch) linearLayout.findViewById(R.id.webview_setSupportZoom);
            webview_setSupportZoom.setChecked(settings.webview_setSupportZoom);
            webview_setSupportZoom.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    settings.webview_setSupportZoom = !settings.webview_setSupportZoom;
                }
            });
            Switch webview_setBuiltInZoomControls = (Switch) linearLayout.findViewById(R.id.webview_setBuiltInZoomControls);
            webview_setBuiltInZoomControls.setChecked(settings.webview_setBuiltInZoomControls);
            webview_setBuiltInZoomControls.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    settings.webview_setBuiltInZoomControls = !settings.webview_setBuiltInZoomControls;
                }
            });
            Switch webview_setUseWideViewPort = (Switch) linearLayout.findViewById(R.id.webview_setUseWideViewPort);
            webview_setUseWideViewPort.setChecked(settings.webview_setUseWideViewPort);
            webview_setUseWideViewPort.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    settings.webview_setUseWideViewPort = !settings.webview_setUseWideViewPort;
                }
            });
            Switch webview_setJavaScriptEnabled = (Switch) linearLayout.findViewById(R.id.webview_setJavaScriptEnabled);
            webview_setJavaScriptEnabled.setChecked(settings.webview_setJavaScriptEnabled);
            webview_setJavaScriptEnabled.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    settings.webview_setJavaScriptEnabled = !settings.webview_setJavaScriptEnabled;
                }
            });
            Switch webview_setDomStorageEnabled = (Switch) linearLayout.findViewById(R.id.webview_setDomStorageEnabled);
            webview_setDomStorageEnabled.setChecked(settings.webview_setDomStorageEnabled);
            webview_setDomStorageEnabled.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    settings.webview_setDomStorageEnabled = !settings.webview_setDomStorageEnabled;
                }
            });
            Switch webview_PCmode = (Switch) linearLayout.findViewById(R.id.webview_PCmode);
            webview_PCmode.setChecked(settings.webSettings_PCmode);
            webview_PCmode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    settings.webSettings_PCmode = !settings.webSettings_PCmode;
                }
            });
            Button homePage=(Button)linearLayout.findViewById(R.id.webview_homePageButtom);
            homePage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    settings.webview_homePage=((EditText)linearLayout.findViewById(R.id.webview_setHomePage)).getText().toString();
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save_setting, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        if (hasChanged) {
            Intent main = new Intent();
            setResult(1, main);
            finish();
        }
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //响应toolbar左边的home键
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_saveSettings:
                saveSettings();
                Toast.makeText(getBaseContext(), "已保存", Toast.LENGTH_SHORT).show();
                onBackPressed();
                break;
        }
        return true;
    }
}
