package zysy.iflytek.zhumang.user.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import zysy.iflytek.zhumang.user.entity.User;
import zysy.iflytek.zhumang.user.mapper.IUserMapper;
import zysy.iflytek.zhumang.user.service.IUserService;
import zysy.iflytek.zhumang.utils.JwtUtils;
import java.util.Date;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import zysy.iflytek.zhumang.common.exception.BusinessException;
import zysy.iflytek.zhumang.user.dto.UserManageDto;
import zysy.iflytek.zhumang.utils.PasswordUtils;

@Service
public class UserServiceImpl extends ServiceImpl<IUserMapper, User>
        implements IUserService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String EMAIL_CODE_PREFIX = "email:code:";
    private static final long CODE_EXPIRE_MINUTES = 5;

    @Override
    public User findByOpenid(String openid) {
        return baseMapper.selectByOpenid(openid);
    }

    @Override
    public void createUser(User user) {
        baseMapper.insert(user);
    }

    @Override
    public String loginByEmailCode(String email, String code) {
        // 1. 从Redis获取验证码
        String savedCode = redisTemplate.opsForValue().get(email);
        if (savedCode == null) {
            throw new RuntimeException("验证码已过期");
        }

        // 2. 验证码校验
        if (!savedCode.equals(code)) {
            throw new RuntimeException("验证码错误");
        }

        // 3. 查找或创建用户
        User user = lambdaQuery().eq(User::getEmail, email).one();
        if (user == null) {
            // 创建新用户
            user = new User();
            user.setEmail(email);
            user.setCreatedTime(new Date());
            user.setRole("视障用户"); // 设置默认角色
            // 保存用户并获取ID
            save(user);
            // 立即刷新用户对象以获取ID
            user = getById(user.getId());
            if (user == null) {
                throw new RuntimeException("用户创建失败");
            }
        }

        // 4. 生成JWT token
        String token = JwtUtils.generateToken(user.getId());

        // 5. 删除已使用的验证码
        redisTemplate.delete(email);

        return token;
    }

    @Override
    public User findByEmail(String email) {
        return getOne(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, email)
                .last("LIMIT 1"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User createUser(UserManageDto dto) {
        // 检查邮箱是否已存在
        if (findByEmail(dto.getEmail()) != null) {
            throw new BusinessException("邮箱已被使用");
        }

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setRole(dto.getRole());
        user.setCreatedTime(new Date());
        user.setStatus(dto.getStatus() != null ? dto.getStatus() : 1); // 默认状态为正常

        // 如果提供了密码，则加密存储
        if (StringUtils.hasText(dto.getPassword())) {
            user.setPassword(PasswordUtils.encode(dto.getPassword()));
        }

        save(user);
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User updateUser(UserManageDto dto) {
        User user = getById(dto.getId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 如果修改了邮箱，需要检查新邮箱是否已被使用
        if (!user.getEmail().equals(dto.getEmail())) {
            User existingUser = findByEmail(dto.getEmail());
            if (existingUser != null && !existingUser.getId().equals(dto.getId())) {
                throw new BusinessException("邮箱已被使用");
            }
            user.setEmail(dto.getEmail());
        }

        user.setRole(dto.getRole());
        if (dto.getStatus() != null) {
            user.setStatus(dto.getStatus());
        }

        // 如果提供了新密码，则更新密码
        if (StringUtils.hasText(dto.getPassword())) {
            user.setPassword(PasswordUtils.encode(dto.getPassword()));
        }

        updateById(user);
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        User user = getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        removeById(id);
    }

    @Override
    public IPage<User> pageUsers(Integer pageNum, Integer pageSize, String email, String role, Integer status) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(email), User::getEmail, email)
                .eq(StringUtils.hasText(role), User::getRole, role)
                .eq(status != null, User::getStatus, status)
                .orderByDesc(User::getCreatedTime);
        
        return page(new Page<>(pageNum, pageSize), wrapper);
    }
}