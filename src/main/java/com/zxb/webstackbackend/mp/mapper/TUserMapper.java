package com.zxb.webstackbackend.mp.mapper;

import com.zxb.webstackbackend.mp.pojo.TUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Update;

/**
* @author Administrator
* @description 针对表【t_user(用户表)】的数据库操作Mapper
* @createDate 2024-08-02 18:46:21
* @Entity com.zxb.webstackbackend.mp.pojo.TUser
*/
public interface TUserMapper extends BaseMapper<TUser> {

    // 更新用户头像
    @Update("update t_user set avatar=#{avatarUrl},update_time=now() where id=#{id}")
    void updateAvatar(String avatarUrl, Integer id);

    // 更新用户头像
    @Update("update t_user set password=#{md5String},update_time=now() where id=#{id}")
    void updatePwd(String md5String, Integer id);
}




