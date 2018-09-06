package com.homework.zifenghuang.thebrowser;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class HistoryActivity extends AppCompatActivity {

    private FloatingActionButton fab;
    private Toolbar toolbar;
    private HistoryIniter historyIniter;
    private RecyclerView recyclerView;
    private HistoryCardAdapter adapter;
    //private SearchView searchView;
    //private MenuItem searchItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        initViews();
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    //初始化视图
    private void initViews() {
        historyIniter = new HistoryIniter();
        fab = (FloatingActionButton) findViewById(R.id.history_fab);
        toolbar = (Toolbar) findViewById(R.id.history_toolbar);
        recyclerView = (RecyclerView) findViewById(R.id.history_recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new HistoryCardAdapter(historyIniter.getHistoryList());
        recyclerView.setAdapter(adapter);

        toolbar.setTitle("历史记录");

        //悬浮按钮响应
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //清空历史记录
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("历史记录");
                builder.setMessage("是否清空");
                builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        historyIniter.cleanHistory();
                        historyIniter.loadHistorys();
                        Toast.makeText(HistoryActivity.this, "已清空", Toast.LENGTH_SHORT).show();
                        adapter.notifyDataSetChanged();
                    }
                });
                builder.setNegativeButton("否", new DialogInterface.OnClickListener() {
                    // 忽略
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();

            }
        });
    }

    void urlToMainActivity(String url) {
        if (url != null) {
            Intent main = new Intent();
            main.putExtra("url", url);
            setResult(1, main);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //响应toolbar左边的home键
            case android.R.id.home:
                onBackPressed();
        }
        return true;
    }
}
