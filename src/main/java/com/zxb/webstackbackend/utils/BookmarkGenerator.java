package com.zxb.webstackbackend.utils;

/**
 * Author: Administrator
 * CreateTime: 2024/7/18
 * Project: webstack-backend
 */

import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class BookmarkGenerator {

    public static String createBookmarksStr(List<Bookmark> bookmarks) {

        String str = "<!DOCTYPE NETSCAPE-Bookmark-file-1>\n" +
                "<!-- This is an automatically generated file. It will be read and overwritten. DO NOT EDIT! -->\n" +
                "<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=UTF-8\">\n" +
                "<TITLE>Bookmarks</TITLE>\n" +
                "<H1>Bookmarks</H1>\n" +
                "<H3 personal_toolbar_folder=\"true\">webstack导入</H3>\n" +
                "<DL>\n<p>\n" +
                loop(bookmarks) +
                "</DL>\n<p>\n";

        return str;
    }

    private static String loop(List<Bookmark> root) {
        StringBuilder str = new StringBuilder();
        for (Bookmark item : root) {
            if (item.folder) {
                str.append("<DT>\n")
                        .append("<H3")
                        .append(item.toolbar ? " PERSONAL_TOOLBAR_FOLDER=\"true\"" : "")
                        .append(">").append(item.name).append("</H3>\n")
                        .append("<DL>\n<p>\n");

                str.append(loop(item.children));

                str.append("</DL>\n<p>\n");
            } else {
                str.append("<DT><A HREF=\"").append(item.url).append("\">")
                        .append(item.name).append("</A>\n");
            }
        }
        return str.toString();
    }

    public static void saveToFile(String content, String filePath) throws IOException {
        // BufferedWriter 写入字符串到文件
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(content);
        }
    }

}
