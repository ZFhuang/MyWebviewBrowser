package com.homework.zifenghuang.thebrowser;

import java.io.Serializable;
import java.util.Date;

public class Bookmark implements Serializable, Comparable<Bookmark> {

    private int screenShotID;
    private String title;
    private String url;
    private Date time;

    Bookmark(int screenShotID, String title, String url, Date time) {
        this.screenShotID = screenShotID;
        this.title = title;
        this.url = url;
        this.time = time;
    }

    int getScreenShotID() {
        return screenShotID;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public Date getTime() {
        return time;
    }

    @Override
    public int compareTo(Bookmark another) {
        return -time.compareTo(another.getTime());
    }

    @Override
    public boolean equals(Object o) {
        return ((Bookmark) o).getUrl().equals(url);
    }
}
