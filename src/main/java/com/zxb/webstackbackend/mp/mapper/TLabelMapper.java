package com.zxb.webstackbackend.mp.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zxb.webstackbackend.mp.pojo.TLabel;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

/**
* @author Administrator
* @description 针对表【t_label(标签表)】的数据库操作Mapper
* @createDate 2024-06-30 21:16:51
* @Entity com.zxb.webstackbackend.mp.pojo.TLabel
*/
public interface TLabelMapper extends BaseMapper<TLabel> {
    @Select("SELECT * FROM t_label")
    Page<TLabel> selectPageVo(Page<?> page);
}




