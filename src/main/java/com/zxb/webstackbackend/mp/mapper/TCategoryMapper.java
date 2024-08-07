package com.zxb.webstackbackend.mp.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zxb.webstackbackend.mp.pojo.TCategory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zxb.webstackbackend.mp.pojo.TLabel;
import org.apache.ibatis.annotations.Select;

/**
* @author Administrator
* @description 针对表【t_category(分类表)】的数据库操作Mapper
* @createDate 2024-06-30 21:16:39
* @Entity com.zxb.webstackbackend.mp.pojo.TCategory
*/
public interface TCategoryMapper extends BaseMapper<TCategory> {

    @Select("SELECT * FROM t_category")
    Page<TLabel> selectPageVo(Page<?> page);
}




