package com.zxb.webstackbackend.mp.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 角色权限表
 * @TableName t_role
 */
@TableName(value ="t_role")
@Data
public class TRole implements Serializable {
    /**
     * 权限id
     */
    @TableId
    private Integer id;

    /**
     * 描述
     */
    private String roleName;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}