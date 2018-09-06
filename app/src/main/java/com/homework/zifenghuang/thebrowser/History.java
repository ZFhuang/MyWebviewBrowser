package com.homework.zifenghuang.thebrowser;

import java.io.Serializable;
import java.util.Date;

public class History implements Serializable, Comparable<History> {

    private String title;
    private String url;
    private Date time;

    public History(String title, String url, Date time) {
        this.title = title;
        this.url = url;
        this.time = time;
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
    public int compareTo(History another) {
        return -time.compareTo(another.getTime());
    }

    @Override
    public boolean equals(Object o) {
        return ((History) o).getUrl().equals(url);
    }
}
