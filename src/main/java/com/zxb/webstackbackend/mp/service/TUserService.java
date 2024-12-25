package com.zxb.webstackbackend.mp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zxb.webstackbackend.mp.pojo.TRole;
import com.zxb.webstackbackend.mp.pojo.TUser;

import java.util.List;

/**
 * @author Administrator
 * @description 针对表【t_user(用户表)】的数据库操作Service
 * @createDate 2024-08-02 18:46:21
 */
public interface TUserService extends IService<TUser> {
    TUser findByUserName(String username);

    void register(String username, String password);

    void update(TUser user);

    void updateAvatar(String avatarUrl);

    void updatePwd(String newPwd);

    String findByRoleName(Integer id);

    List<TRole> getRoles();
}
