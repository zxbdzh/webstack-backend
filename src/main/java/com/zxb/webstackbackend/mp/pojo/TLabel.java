package com.zxb.webstackbackend.mp.pojo;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;

/**
 * 标签表
 * @TableName t_label
 */
@TableName(value ="t_label")
@Data
public class TLabel implements Serializable {
    /**
     * 标签id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 分类id
     */
//    @TableId
    private Long categoryId;

    /**
     * 标签名称
     */
    private String name;

    /**
     * 标签描述
     */
    private String description;

    /**
     * 标签链接
     */
    private String url;

    /**
     * 标签图片链接
     */
    private String imgUrl;

    /**
     * 创建时间
     */
    @TableField(fill= FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill= FieldFill.INSERT_UPDATE)
    private LocalDateTime  updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
