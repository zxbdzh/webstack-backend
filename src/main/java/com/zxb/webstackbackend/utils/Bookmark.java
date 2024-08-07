package com.zxb.webstackbackend.utils;

import java.util.List;

public class Bookmark {
    public String name;
    public String url;
    public boolean folder;
    public boolean toolbar;
    public List<Bookmark> children;

    public Bookmark(String name, String url, boolean folder, boolean toolbar, List<Bookmark> children) {
        this.name = name;
        this.url = url;
        this.folder = folder;
        this.toolbar = toolbar;
        this.children = children;
    }
}
