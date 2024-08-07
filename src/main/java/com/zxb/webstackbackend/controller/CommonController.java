package com.zxb.webstackbackend.controller;

import com.zxb.webstackbackend.config.AmazonS3Config;
import com.zxb.webstackbackend.mp.pojo.TCategory;
import com.zxb.webstackbackend.mp.pojo.TLabel;
import com.zxb.webstackbackend.mp.service.TCategoryService;
import com.zxb.webstackbackend.mp.service.TLabelService;
import com.zxb.webstackbackend.utils.AmazonS3Util;
import com.zxb.webstackbackend.utils.Bookmark;
import com.zxb.webstackbackend.utils.Result;
import com.zxb.webstackbackend.utils.FileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.exceptions.TooManyResultsException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.zxb.webstackbackend.utils.BookmarkGenerator.createBookmarksStr;
import static com.zxb.webstackbackend.utils.BookmarkGenerator.saveToFile;


/**
 * Author: Administrator
 * CreateTime: 2024/7/6
 * Project: webstack-backend
 */
@RestController
@Slf4j
@RequestMapping("/common")
@RequiredArgsConstructor
public class CommonController {

    private final AmazonS3Config amazonS3Config;
    private final TCategoryService tCategoryService;
    private final TLabelService tLabelService;
    private final AmazonS3Util amazonS3Util;

    @CacheEvict(value = {"TCategory", "TLabel"}, allEntries = true)
    @PostMapping("/upload")
    public Result<String> handleFileUpload(@RequestParam MultipartFile file) {

        // s3上传文件
        String s = AmazonS3Util.uploadFile(file, "webstack-vue");
        s = amazonS3Config.getS3Url() + "/" + s;

        return Result.ok(s);
    }

    @Transactional
    @PostMapping("/bookmark/upload")
    public Result<Object> handleBookMarkUpload(@RequestBody MultipartFile file) {
        try {
            File resource = FileUtil.convertMultipartFileToFile(file);
            Document doc = Jsoup.parse(resource);
            Elements h3 = doc.selectXpath("//dt/dl/dt/dl/dt/h3");
            if (h3.isEmpty()) return Result.error("书签文件错误！");
            // 存储所有 <a> 标签的列表
            List<Element> allLinks = new ArrayList<>();
            // 存储所有的标签
            ArrayList<TLabel> tLabels = new ArrayList<>();
            for (Element element : h3) {
                // 获取当前 <h3> 元素的下一个兄弟元素中的所有 <a> 标签
                Elements links = Objects.requireNonNull(element.nextElementSibling()).selectXpath("./dt/a");
                log.info("爬取到的分类名：{}", element.text());
                TCategory tCategory = new TCategory();
                tCategory.setCreateTime(LocalDateTime.now());
                tCategory.setUpdateTime(LocalDateTime.now());
                tCategory.setName(element.text());
                if(!tCategoryService.save(tCategory)) return Result.error("保存失败");
                try {
                    tCategory = tCategoryService.query().eq("name", element.text()).select("id").one();
                } catch (TooManyResultsException e) {
                    continue;
                }

                // 将所有 <a> 标签添加到列表中
                allLinks.addAll(links);

                // 输出当前分类下的所有 <a> 标签
                for (Element link : links) {
                    TLabel tLabel = new TLabel();
                    tLabel.setUpdateTime(LocalDateTime.now());
                    tLabel.setCreateTime(LocalDateTime.now());
                    tLabel.setName(link.text());
                    tLabel.setCategoryId(tCategory.getId());
                    tLabel.setDescription("书签导入自动生成");
                    tLabel.setUrl(link.attr("href"));
                    tLabel.setImgUrl("https://picgo.cn-sy1.rains3.com/2024/07/d4edf4c89c89a5be54d52433af143d1a.png");
                    tLabels.add(tLabel);
                }
                log.info("\n");
            }
            if (!tLabelService.saveBatch(tLabels)) return Result.error();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Result.ok();
    }


    @GetMapping("/bookmark/export")
    public ResponseEntity<Resource> testBookMarkExport() {
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
        String fileName = "export_bookmark.html";

        try {
            saveToFile(bookmarksStr, fileName);
            File file = new File(fileName);
            Resource resource = new FileSystemResource(file);

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(file.length())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (IOException e) {
            log.error("error",e);
        }
        return ResponseEntity.noContent().build();
    }
}
