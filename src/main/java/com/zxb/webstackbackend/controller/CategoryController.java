package com.zxb.webstackbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zxb.webstackbackend.mp.mapper.TCategoryMapper;
import com.zxb.webstackbackend.mp.pojo.TCategory;
import com.zxb.webstackbackend.mp.pojo.TLabel;
import com.zxb.webstackbackend.mp.service.TCategoryService;
import com.zxb.webstackbackend.mp.service.TLabelService;
import com.zxb.webstackbackend.utils.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/category")
public class CategoryController {

    private final TCategoryService tCategoryService;
    private final TLabelService tLabelService;
    private final TCategoryMapper tCategoryMapper;

    @GetMapping
    @Cacheable("TCategory")
    public Result<List<TCategory>> getCategory() {
        return tCategoryService.getCategoryTree();
    }

    @GetMapping("/father/{fatherId}")
    public Result<List<TCategory>> getCategoryByFatherId(@PathVariable Integer fatherId) {
        List<TCategory> tCategorys = tCategoryService.query().eq("id", fatherId).list();
        return Result.ok(tCategorys);
    }

    /**
     * 根据 page 对象 返回分页数据
     * @param page
     * @return
     */
    @GetMapping("/page")
    Result<Page<TLabel>> selectPageVo(@RequestParam(value = "page", defaultValue = "1") Integer page,
                                      @RequestParam(value = "size", defaultValue = "10") Integer size) {
        Page<TLabel> tCategoryPage = tCategoryMapper.selectPageVo(new Page<>(page, size));
        return Result.ok(tCategoryPage);
    }

    @GetMapping("/name")
    public Result<List<TCategory>> getCategoryName() {
        List<TCategory> list = tCategoryService.query().select("name").list();
        return Result.ok(list);
    }

    /**
     * 根据 id 获取名称
     *
     * @param id
     * @return
     */
    @GetMapping("/name/{id}")
    public Result<List<TCategory>> getCategoryNameById(@PathVariable int id) {
        List<TCategory> list = tCategoryService.query().eq("id", id).select("name").list();
        return Result.ok(list);
    }

    /**
     * 根据 id 删除分类
     *
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    @CacheEvict(value = "TCategory", allEntries = true)
    public Result<Object> removeCategoryById(@PathVariable int id) {
        TCategory tCategory = tCategoryService.getById(id);
        if (tCategory.getFatherId() != 0) {
            return Result.error("该分类下存在其父类！");
        }
        List<TCategory> sons = tCategoryService.query().eq("father_id", id).list();
        if (!sons.isEmpty()) {
            return Result.error("该分类下存在其子类！");
        }

        // 删除对应标签
        if (!tLabelService.remove(new QueryWrapper<TLabel>().eq("category_id", id))) return Result.error();
        else if (tCategoryService.removeById(id)) return Result.ok();
        else return Result.error("无法删除该分类！");
    }

    /**
     * 修改分类
     *
     * @param tCategory
     * @return
     */
    @PostMapping
    @CacheEvict(value = "TCategory", allEntries = true)
    public Result<Object> updateCategory(@RequestBody TCategory tCategory) {
        if (tCategory != null) {
            tCategory.setUpdateTime(LocalDateTime.now());
        }
        if (tCategoryService.updateById(tCategory)) {
            return Result.ok();
        }
        return Result.error("修改分类失败！");
    }

    /**
     * 新增分类
     *
     * @param tCategory
     * @return
     */
    @PutMapping
    @CacheEvict(value = "TCategory", allEntries = true)
    public Result<Object> addCategory(@RequestBody TCategory tCategory) {
        if (tCategory != null) {
            tCategory.setCreateTime(LocalDateTime.now());
            tCategory.setUpdateTime(LocalDateTime.now());
        }
        if (tCategoryService.save(tCategory)) {
            return Result.ok();
        } else return Result.error("添加分类失败");
    }

}
