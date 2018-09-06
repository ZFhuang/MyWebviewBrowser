package com.homework.zifenghuang.thebrowser;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Calendar;

public class BookmarkActivity extends AppCompatActivity {

    private FloatingActionButton fab;
    private Toolbar toolbar;
    private BookmarkIniter bookmarkIniter;
    private RecyclerView recyclerView;
    private BookmarkCardAdapter adapter;
    //private SearchView searchView;
    //private MenuItem searchItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmark);
        initViews();
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    //初始化视图
    private void initViews() {
        bookmarkIniter = new BookmarkIniter();
        fab = (FloatingActionButton) findViewById(R.id.bookmark_fab);
        toolbar = (Toolbar) findViewById(R.id.bookmark_toolbar);
        recyclerView = (RecyclerView) findViewById(R.id.bookmark_recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new BookmarkCardAdapter(bookmarkIniter.getBookmarkList());
        recyclerView.setAdapter(adapter);

        toolbar.setTitle("收藏夹");

        //悬浮按钮响应
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater factory = LayoutInflater.from(getBaseContext());
                final View textEntryView = factory.inflate(R.layout.bookmarks_dialog, null);
                final EditText bookmarkTitle = (EditText) textEntryView.findViewById(R.id.bookmark_dialog_title);
                final EditText bookmarkUrl = (EditText) textEntryView.findViewById(R.id.bookmark_dialog_url);
                AlertDialog.Builder ad1 = new AlertDialog.Builder(BookmarkActivity.this);
                ad1.setTitle("是否添加书签");
                ad1.setView(textEntryView);
                ad1.setPositiveButton("是", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int i) {
                        Bookmark temp = new Bookmark(0, bookmarkTitle.getText().toString(), bookmarkUrl.getText().toString(), Calendar.getInstance().getTime());
                        BookmarkIniter.inputBookmark(temp);
                        adapter.addBookmark(temp);
                        bookmarkIniter.saveBookmarks();
                        bookmarkIniter.loadBookmarks();
                        adapter.notifyDataSetChanged();
                        Toast.makeText(getBaseContext(), "添加成功", Toast.LENGTH_SHORT).show();
                    }
                });
                ad1.setNegativeButton("否", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int i) {
                    }
                });
                ad1.show();// 显示对话框
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
