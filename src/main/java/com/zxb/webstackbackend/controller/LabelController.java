package com.zxb.webstackbackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.zxb.webstackbackend.mp.mapper.TLabelMapper;
import com.zxb.webstackbackend.mp.pojo.TLabel;
import com.zxb.webstackbackend.mp.service.TLabelService;
import com.zxb.webstackbackend.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/label")
public class LabelController {

    private final TLabelService tLabelService;
    private final TLabelMapper tLabelMapper;
    Cache<String, String> cache = Caffeine.newBuilder().build();


    public LabelController(TLabelService tLabelService, TLabelMapper tLabelMapper) {
        this.tLabelService = tLabelService;
        this.tLabelMapper = tLabelMapper;
    }


    /**
     * 返回所有标签
     * @return
     */
    @GetMapping
    @Cacheable("TLabel")
    public Result<List<TLabel>> getLabels() {
        List<TLabel> list = tLabelService.list();
        return Result.ok(list);
    }

    /**
     * 根据 page 对象 返回分页数据
     * @param page
     * @return
     */
    @GetMapping("/page")
    Result<Page<TLabel>> selectPageVo(@RequestParam(value = "page", defaultValue = "1") Integer page,
                                      @RequestParam(value = "size", defaultValue = "10") Integer size) {
        Page<TLabel> tLabelPage = tLabelMapper.selectPageVo(new Page<>(page, size));
        return Result.ok(tLabelPage);
    }

    /**
     * 根据id返回标签
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<TLabel> getLabelById(@PathVariable int id) {
        TLabel tLabel = tLabelService.getById(id);
        return Result.ok(tLabel);
    }

    /**
     * 根据id删除标签
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    @CacheEvict(value = "TLabel", allEntries = true)
    public Result<Object> removeLabelById(@PathVariable int id) {
        tLabelService.removeById(id);
        return Result.ok();
    }

    /**
     * 根据 标签 实体类更新
     * @param tLabel
     * @return
     */
    @PostMapping
    @CacheEvict(value = "TLabel", allEntries = true)
    public Result<Object> updateLabel(@RequestBody TLabel tLabel) {
        tLabel.setUpdateTime(LocalDateTime.now());
        tLabelService.updateById(tLabel);
        return Result.ok();
    }

    /**
     * 增加标签
     * @param tLabel
     * @return
     */
    @PutMapping
    @CacheEvict(value = "TLabel", allEntries = true)
    public Result<Object> addLabel(@RequestBody TLabel tLabel) {
        if (tLabel != null) {
            tLabel.setCreateTime(LocalDateTime.now());
            tLabel.setUpdateTime(LocalDateTime.now());
        }
        if (tLabelService.save(tLabel)) {
            return Result.ok();
        } else
            return Result.error("添加失败");
    }

}
