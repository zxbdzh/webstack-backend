package com.zxb.webstackbackend.mp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zxb.webstackbackend.mp.mapper.TCategoryMapper;
import com.zxb.webstackbackend.mp.pojo.TCategory;
import com.zxb.webstackbackend.mp.service.TCategoryService;
import com.zxb.webstackbackend.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* @author Administrator
* @description 针对表【t_category(分类表)】的数据库操作Service实现
* @createDate 2024-06-30 21:16:39
*/
@Service
@Slf4j
public class TCategoryServiceImpl extends ServiceImpl<TCategoryMapper, TCategory>
    implements TCategoryService{

    @Override
    public Result<List<TCategory>> getCategoryTree() {
        // 查询 father_id 不为空的数据
        List<TCategory> categoriesWithFather = query().ne("father_id", 0).list();

        // 查询 father_id 为空的数据
        List<TCategory> rootCategories = query().eq("father_id", 0).list();

        // 将结果放入 Map 中，便于快速查找
        Map<Long, TCategory> categoryMap = new HashMap<>();
        for (TCategory category : rootCategories) {
            category.setChildren(new ArrayList<>());
            categoryMap.put(category.getId(), category);
        }

        // 将 father_id 不为空的记录根据相应的 fatherId 放到相应的父记录的 children 列表中
        for (TCategory category : categoriesWithFather) {
            TCategory parent = categoryMap.get(category.getFatherId());
            if (parent != null) {
                if (parent.getChildren() == null) {
                    parent.setChildren(new ArrayList<>());
                }
                parent.getChildren().add(category);
            }
        }

        return Result.ok(rootCategories);
    }
}




