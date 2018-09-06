package com.homework.zifenghuang.thebrowser;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;

import java.io.*;
import java.util.Collections;
import java.util.LinkedList;

class BookmarkIniter {
    private final static String folderName = "/TheBrowser/Bookmarks";

    private File bookmarkFolder;
    private LinkedList<Bookmark> bookmarkList;

    BookmarkIniter() {
        bookmarkFolder = new File(Environment.getExternalStorageDirectory() + folderName);
        if (!bookmarkFolder.exists() || !bookmarkFolder.isDirectory()) {
            return;
        }
        bookmarkList = new LinkedList<>();
        loadBookmarks();
    }

    void saveBookmarks() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (Bookmark temp : bookmarkList) {
                    inputBookmark(temp);
                }
            }
        }).start();
    }

    void loadBookmarks() {
        final File[] files = bookmarkFolder.listFiles();
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (File file : files) {
                    try {
                        final ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
                        Bookmark temp = (Bookmark) ois.readObject();
                        if (!bookmarkList.contains(temp)) {
                            bookmarkList.add(temp);
                        }
                        ois.close();
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                Collections.sort(bookmarkList);
            }
        }).start();
    }

    static void inputBookmark(Bookmark bookmark) {
        try {
            if (bookmark == null) {
                return;
            } else if (bookmark.getUrl() == null) {
                return;
            }
            String fileName = bookmark.getUrl().replaceAll("/", "@");
            if (fileName.length() > 40) {
                fileName = fileName.substring(fileName.length() - 41, fileName.length() - 1);
            }
            File bookmarkFile = new File(Environment.getExternalStorageDirectory() + folderName + "/", fileName);
            if (bookmarkFile.exists())
                return;
            else
                bookmarkFile.createNewFile();
            final ObjectOutputStream ops = new ObjectOutputStream(new FileOutputStream(bookmarkFile));
            ops.writeObject(bookmark);
            ops.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static boolean existBookmark(String url) {
        if (url == null) {
            return false;
        }
        String fileName = url.replaceAll("/", "@");
        if (fileName.length() > 40) {
            fileName = fileName.substring(fileName.length() - 41, fileName.length() - 1);
        }
        File bookmarkFile = new File(Environment.getExternalStorageDirectory() + folderName + "/", fileName);
        return bookmarkFile.exists();
    }

    static void deleteBookmark(String url) {
        String fileName = url.replaceAll("/", "@");
        if (fileName.length() > 40) {
            fileName = fileName.substring(fileName.length() - 41, fileName.length() - 1);
        }
        File bookmarkFile = new File(Environment.getExternalStorageDirectory() + folderName + "/", fileName);
        if (bookmarkFile.exists())
            bookmarkFile.delete();
    }

    LinkedList<Bookmark> getBookmarkList() {
        return bookmarkList;
    }
}

class BookmarkCardAdapter extends RecyclerView.Adapter<BookmarkCardAdapter.ViewHolder> {
    private Context context;
    private LinkedList<Bookmark> bookmarkList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView imageView;
        TextView titleText;
        TextView urlText;

        ViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            imageView = (ImageView) cardView.findViewById(R.id.bookmark_image);
            titleText = (TextView) cardView.findViewById(R.id.bookmark_title);
            urlText = (TextView) cardView.findViewById(R.id.bookmark_url);
        }
    }

    BookmarkCardAdapter(LinkedList<Bookmark> bookmarkList) {
        this.bookmarkList = bookmarkList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (context == null) {
            context = parent.getContext();
        }
        View view = LayoutInflater.from(context).inflate(R.layout.bookmarks_card, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = ((TextView) holder.cardView.findViewById(R.id.bookmark_url)).getText().toString();
                ((BookmarkActivity) context).urlToMainActivity(url);
            }
        });

        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(((TextView) holder.cardView.findViewById(R.id.bookmark_title)).getText().toString());
                builder.setMessage("是否删除此条");
                builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String url=((TextView) holder.cardView.findViewById(R.id.bookmark_url)).getText().toString();
                        BookmarkIniter.deleteBookmark(url);
                        Toast.makeText(context, "已删除", Toast.LENGTH_SHORT).show();
                        bookmarkList.remove(new Bookmark(0,null,url,null));
                        notifyDataSetChanged();
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
                return false;
            }
        });

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Bookmark bookmark = bookmarkList.get(position);
        holder.titleText.setText(bookmark.getTitle());
        holder.urlText.setText(bookmark.getUrl());
        //Glide库的图像加载
        Glide.with(context).load(bookmark.getScreenShotID()).into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return bookmarkList.size();
    }

    public void addBookmark(Bookmark input){
        bookmarkList.add(input);
        Collections.sort(bookmarkList);
    }
}
