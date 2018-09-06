package com.homework.zifenghuang.thebrowser;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
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

import static android.content.Context.DOWNLOAD_SERVICE;

public class HistoryIniter {
    private final static String folderName = "/TheBrowser/History";

    private File historyFolder;
    private LinkedList<History> historyList;

    HistoryIniter() {
        historyFolder = new File(Environment.getExternalStorageDirectory() + folderName);
        if (!historyFolder.exists() || !historyFolder.isDirectory()) {
            return;
        }
        historyList = new LinkedList<>();
        loadHistorys();
    }

    public void saveHistorys() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (History temp : historyList) {
                    inputHistory(temp);
                }
            }
        }).start();
    }

    void loadHistorys() {
        final File[] files = historyFolder.listFiles();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (File file : files) {
                        final ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
                        History temp = (History) ois.readObject();
                        if (!historyList.contains(temp))
                            historyList.add(temp);
                        ois.close();
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
                Collections.sort(historyList);
            }
        }).start();
    }

    static void inputHistory(History history) {
        try {
            if (history == null || history.getUrl() == null) {
                return;
            }
            String fileName = history.getUrl().replaceAll("/", "@");
            if (fileName.length() > 40) {
                fileName = fileName.substring(fileName.length() - 41, fileName.length() - 1);
            }
            File historyFile = new File(Environment.getExternalStorageDirectory() + folderName + "/", fileName);
            if (!historyFile.exists())
                historyFile.createNewFile();
            final ObjectOutputStream ops = new ObjectOutputStream(new FileOutputStream(historyFile));
            ops.writeObject(history);
            ops.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void cleanHistory() {
        File historyFolder = new File(Environment.getExternalStorageDirectory() + folderName);
        if (!historyFolder.exists()) {//判断是否待删除目录是否存在
            System.err.println("The dir are not exists!");
            return;
        }

        final File[] files = historyFolder.listFiles();

        for (File file : files) {
            if (file.isFile())
                file.delete();
        }

        //清空链表
        historyList.clear();
    }

    static void deleteHistory(String url) {
        String fileName = url.replaceAll("/", "@");
        if (fileName.length() > 40) {
            fileName = fileName.substring(fileName.length() - 41, fileName.length() - 1);
        }
        File historyFile = new File(Environment.getExternalStorageDirectory() + folderName + "/", fileName);
        if (historyFile.exists())
            historyFile.delete();
    }

    LinkedList<History> getHistoryList() {
        return historyList;
    }
}

class HistoryCardAdapter extends RecyclerView.Adapter<HistoryCardAdapter.ViewHolder> {
    private Context context;
    private LinkedList<History> historyList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView titleText;
        TextView urlText;
        TextView timeText;

        ViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            titleText = (TextView) cardView.findViewById(R.id.history_title);
            urlText = (TextView) cardView.findViewById(R.id.history_url);
            timeText = (TextView) cardView.findViewById(R.id.history_time);
        }
    }

    HistoryCardAdapter(LinkedList<History> historyList) {
        this.historyList = historyList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (context == null) {
            context = parent.getContext();
        }
        View view = LayoutInflater.from(context).inflate(R.layout.historys_card, parent, false);

        final ViewHolder holder = new ViewHolder(view);
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = ((TextView) holder.cardView.findViewById(R.id.history_url)).getText().toString();
                ((HistoryActivity) context).urlToMainActivity(url);
            }
        });

        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("历史记录");
                builder.setMessage("是否删除此条");
                builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String url=((TextView) holder.cardView.findViewById(R.id.history_url)).getText().toString();
                        HistoryIniter.deleteHistory(url);
                        Toast.makeText(context, "已删除", Toast.LENGTH_SHORT).show();
                        historyList.remove(new History(null,url,null));
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
        History history = historyList.get(position);
        holder.titleText.setText(history.getTitle());
        holder.urlText.setText(history.getUrl());
        holder.timeText.setText(history.getTime().toString());
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }
}
