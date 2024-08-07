package com.zxb.webstackbackend.mp.service;

import com.zxb.webstackbackend.mp.pojo.TCategory;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zxb.webstackbackend.utils.Result;

import java.util.List;

/**
* @author Administrator
* @description 针对表【t_category(分类表)】的数据库操作Service
* @createDate 2024-06-30 21:16:39
*/
public interface TCategoryService extends IService<TCategory> {

    Result<List<TCategory>> getCategoryTree();
}
