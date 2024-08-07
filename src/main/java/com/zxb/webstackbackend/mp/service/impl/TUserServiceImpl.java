package com.zxb.webstackbackend.mp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zxb.webstackbackend.mp.mapper.TRoleMapper;
import com.zxb.webstackbackend.mp.mapper.TUserMapper;
import com.zxb.webstackbackend.mp.pojo.TUser;
import com.zxb.webstackbackend.mp.service.TRoleService;
import com.zxb.webstackbackend.mp.service.TUserService;
import com.zxb.webstackbackend.utils.Md5Util;
import com.zxb.webstackbackend.utils.ThreadLocalUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author Administrator
 * @description 针对表【t_user(用户表)】的数据库操作Service实现
 * @createDate 2024-08-02 18:46:21
 */
@Service
public class TUserServiceImpl extends ServiceImpl<TUserMapper, TUser>
        implements TUserService {

    final TUserMapper tUserMapper;

    final TRoleService tRoleService;

    public TUserServiceImpl(TRoleMapper tRoleMapper, TUserMapper tUserMapper, TRoleService tRoleService) {
        this.tUserMapper = tUserMapper;
        this.tRoleService = tRoleService;
    }

    /**
     * 根据用户名查询用户
     *
     * @param username
     * @return
     */
    public TUser findByUserName(String username) {
        return query().eq("username", username).one();
    }

    /**
     * 注册
     *
     * @param username
     * @param password
     */
    public void register(String username, String password) {
        TUser tUser = new TUser();
        tUser.setUpdateTime(LocalDateTime.now());
        tUser.setCreateTime(LocalDateTime.now());
        tUser.setUsername(username);
        tUser.setPassword(Md5Util.getMD5String(password));
        tUser.setAvatar("https://picgo.cn-sy1.rains3.com/2024/08/a3afdbb7f0c3ada619fdfe7d16692fab.jpg");
        save(tUser);
    }

    /**
     * 更新用户
     *
     * @param user
     */
    @Override
    public void update(TUser user) {
        user.setUpdateTime(LocalDateTime.now());
        updateById(user);
    }

    /**
     * 更新用户头像
     *
     * @param avatarUrl
     */
    @Override
    public void updateAvatar(String avatarUrl) {
        Map<String, Object> map = ThreadLocalUtil.get();
        Integer id = (Integer) map.get("id");
        tUserMapper.updateAvatar(avatarUrl, id);
    }

    /**
     * 更新密码
     *
     * @param newPwd
     */
    @Override
    public void updatePwd(String newPwd) {
        Map<String, Object> map = ThreadLocalUtil.get();
        Integer id = (Integer) map.get("id");
        tUserMapper.updatePwd(Md5Util.getMD5String(newPwd), id);
    }

    @Override
    public String findByRoleName(Integer id) {
        String desc = tRoleService.query().eq("id", id).select("role_name").one().getRoleName();
        return desc != null ? desc : "游客";
    }
}




