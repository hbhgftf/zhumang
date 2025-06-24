package zysy.iflytek.zhumang.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import zysy.iflytek.zhumang.user.entity.User;
import zysy.iflytek.zhumang.user.dto.UserManageDto;

public interface IUserService extends IService<User> {
    User findByOpenid(String openid);
    void createUser(User user);

    String loginByEmailCode(String email, String code);

    /**
     * 根据邮箱查询用户
     * @param email 邮箱
     * @return 用户信息
     */
    User findByEmail(String email);

    // 用户管理相关方法
    User createUser(UserManageDto dto);
    User updateUser(UserManageDto dto);
    void deleteUser(Long id);
    IPage<User> pageUsers(Integer pageNum, Integer pageSize, String email, String role, Integer status);
}