package com.zxb.webstackbackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zxb.webstackbackend.mp.mapper.TUserMapper;
import com.zxb.webstackbackend.mp.pojo.TUser;
import com.zxb.webstackbackend.mp.service.TUserService;
import com.zxb.webstackbackend.utils.JwtUtil;
import com.zxb.webstackbackend.utils.Md5Util;
import com.zxb.webstackbackend.utils.Result;
import com.zxb.webstackbackend.utils.ThreadLocalUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import org.hibernate.validator.constraints.URL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Author: Administrator
 * CreateTime: 2024/8/2
 * Project: webstack-backend
 */
@RestController
@RequestMapping("/user")
@Validated
@Tag(name = "用户相关")
@AllArgsConstructor
public class UserController {
    private final TUserMapper tUserMapper;
    @Autowired
    private TUserService userService;

    @Operation(summary = "注册用户")
    @PostMapping("/register")
    public Result register(@Parameter(name = "用户名") @Pattern(regexp = "^\\S{5,16}$") String username, @Parameter(name = "密码") @Pattern(regexp = "^\\S{5,16}$") String password) {
        // 查询用户
        TUser u = userService.findByUserName(username);
        if (u == null) {
            // 没有占用
            //注册
            userService.register(username, password);
            return Result.ok();
        } else {
            // 占用
            return Result.error("用户名已被占用");
        }
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result login(@Parameter(name = "用户名") String username, @Parameter(name = "密码") String password) {
        // 查询用户
        TUser loginUser = userService.findByUserName(username);
        if (loginUser == null) {
            return Result.error("用户名错误");
        }
        if (Md5Util.getMD5String(password).equals(loginUser.getPassword())) {
            // 登录成功
            // 生成token
            Map<String, Object> claims = new HashMap<>();
            claims.put("id", loginUser.getId());
            claims.put("username", loginUser.getUsername());
            claims.put("role", loginUser.getRole());
            String token = JwtUtil.genToken(claims);
            return Result.ok(token);
        }

        return Result.error("密码错误");
    }

    @Operation(summary = "获取用户信息")
    @GetMapping("/userInfo")
    public Result<TUser> userInfo() {
        // 根据用户名查询用户
        Map<String, Object> map = ThreadLocalUtil.get();
        String username = (String) map.get("username");

        TUser user = userService.findByUserName(username);
        user.setRoleName(userService.findByRoleName(user.getRole()));

        return Result.ok(user);
    }

    /**
     * 根据 page 对象 返回分页数据
     *
     * @param page
     * @return
     */
    @Operation(summary = "分页查询用户信息")
    @GetMapping("/page")
    Result<List<TUser>> selectPageVo(@RequestParam(value = "page", defaultValue = "1") Integer page,
                                     @RequestParam(value = "size", defaultValue = "10") Integer size) {
        Page<TUser> tLabelPage = tUserMapper.selectPageVo(new Page<>(page, size));
        List<TUser> records = tLabelPage.getRecords();
        records = records.stream().peek(tUser -> tUser.setRoleName(userService.findByRoleName(tUser.getRole()))).collect(Collectors.toList());
        return Result.ok(records);
    }

    @Operation(summary = "更新用户信息")
    @PutMapping("/update")
    public Result update(@RequestBody @Validated TUser user) {
        userService.update(user);
        return Result.ok();
    }

    @Operation(summary = "更新用户头像")
    @PutMapping("/updateAvatar")
    public Result updateAvatar(@RequestParam @URL String avatarUrl) {
        userService.updateAvatar(avatarUrl);
        return Result.ok();
    }

    @Operation(summary = "更新用户密码")
    @PutMapping("/updatePwd")
    public Result updatePwd(@RequestBody Map<String, String> params) {
        // 1. 校验参r数
        String oldPwd = params.get("old_pwd");
        String newPwd = params.get("new_pwd");
        String rePwd = params.get("re_pwd");

        if (!StringUtils.hasLength(oldPwd) || !StringUtils.hasLength(newPwd) || !StringUtils.hasLength(rePwd)) {
            return Result.error("缺少必要的参数");
        }

        // 原密码是否正确
        // 调用userService根据用户名拿到原密码，再和old_pwd对比
        Map<String, Object> map = ThreadLocalUtil.get();
        String username = (String) map.get("username");
        TUser loginUser = userService.findByUserName(username);
        if (!loginUser.getPassword().equals(Md5Util.getMD5String(oldPwd))) {
            return Result.error("原密码填写不正确");
        }

        // newPwd 和 rePwd是否一样
        if (!rePwd.equals(newPwd)) {
            return Result.error("两次填写密码不一致");
        }

        // 2. 调用service 完成密码更新
        userService.updatePwd(newPwd);
        return Result.ok();
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        userService.removeById(id);
        return Result.ok();
    }

    @Operation(summary = "获取职称集合")
    @GetMapping("/role/list")
    public Result getRoles() {
        return Result.ok(userService.getRoles());
    }

}
