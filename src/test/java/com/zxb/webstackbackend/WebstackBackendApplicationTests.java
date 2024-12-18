package com.zxb.webstackbackend;

import com.zxb.webstackbackend.mp.pojo.TCategory;
import com.zxb.webstackbackend.mp.pojo.TLabel;
import com.zxb.webstackbackend.mp.service.TCategoryService;
import com.zxb.webstackbackend.mp.service.TLabelService;
import com.zxb.webstackbackend.utils.Bookmark;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.zxb.webstackbackend.utils.BookmarkGenerator.createBookmarksStr;
import static com.zxb.webstackbackend.utils.BookmarkGenerator.saveToFile;

@Slf4j
@SpringBootTest
class WebstackBackendApplicationTests {
    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private TLabelService tLabelService;

    @Autowired
    private TCategoryService tCategoryService;


    @Test
    void tests() {
        try {
            File resource = resourceLoader.getResource("classpath:bookmarks.html").getFile();
            Document doc = Jsoup.parse(resource);
            Elements h3 = doc.selectXpath("//dt/dl/dt/dl/dt/h3");
            // 存储所有 <a> 标签的列表
            List<Element> allLinks = new ArrayList<>();
            for (Element element : h3) {
                // 获取当前 <h3> 元素的下一个兄弟元素中的所有 <a> 标签
                Elements links = Objects.requireNonNull(element.nextElementSibling()).selectXpath("./dt/a");
                log.info("爬取到的分类名：{}", element.text());

                // 将所有 <a> 标签添加到列表中
                allLinks.addAll(links);

                // 输出当前分类下的所有 <a> 标签
                for (Element link : links) {
                    log.info("链接文本：{}", link.text());
                    log.info("链接地址：{}", link.attr("href"));
                }
                log.info("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testBookMarkExport() {
        // Example usage
        List<Bookmark> bookmarks = new ArrayList<>();
        List<Bookmark> subBookmarks = new ArrayList<>();

        // 查询分类
        List<TCategory> tCategories = tCategoryService.query().select("name").list();
        // 查询书签
        List<TLabel> tLabels = tLabelService.query().select("name", "url").list();

        for (TLabel tLabel : tLabels) {
            subBookmarks.add(new Bookmark(tLabel.getName(), tLabel.getUrl(), false, false, null));
        }
        for(TCategory tCategory : tCategories) {
            bookmarks.add(new Bookmark(tCategory.getName(), null, true, false, subBookmarks));
        }

        String bookmarksStr = createBookmarksStr(bookmarks);

        try {
            saveToFile(bookmarksStr, "export_bookmark.html");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
