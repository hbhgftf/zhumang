package zysy.iflytek.zhumang.user.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import zysy.iflytek.zhumang.common.email.MailMsg;
import zysy.iflytek.zhumang.user.entity.User;
import zysy.iflytek.zhumang.user.service.IUserService;
import zysy.iflytek.zhumang.utils.JwtUtils;
import zysy.iflytek.zhumang.utils.ResponseResult;
import zysy.iflytek.zhumang.common.model.Result;
import zysy.iflytek.zhumang.user.dto.UserInfoDto;
import zysy.iflytek.zhumang.user.dto.VolunteerRegisterDto;
import zysy.iflytek.zhumang.user.dto.EmailLoginDto;
//import zysy.iflytek.zhumang.user.dto.PhoneNumberDto;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;

import zysy.iflytek.zhumang.utils.PasswordUtils;
import zysy.iflytek.zhumang.user.dto.AdminRegisterDto;
import zysy.iflytek.zhumang.user.mapper.IUserMapper;
import zysy.iflytek.zhumang.user.service.ILoginLogService;
import zysy.iflytek.zhumang.user.dto.UserLoginDto;

import zysy.iflytek.zhumang.user.dto.SetPasswordDto;

import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;
import zysy.iflytek.zhumang.common.exception.BusinessException;
import zysy.iflytek.zhumang.utils.GenerateTestUserSig;

@RestController
@RequestMapping("/user")
@Api(tags = "用户接口")
public class UserController {
    @Autowired
    private WxMaService wxMaService;
    @Autowired
    private IUserService userService;
    @Autowired
    private MailMsg mailMsg;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private PasswordUtils passwordUtils;
    @Autowired
    private IUserMapper userMapper;
    @Autowired
    private ILoginLogService loginLogService;

    // 恢复登录相关的常量
    private static final String LOGIN_FAIL_PREFIX = "login:fail:";
    private static final int MAX_FAIL_COUNT = 5;
    private static final long LOCK_TIME = 30; // 锁定时间（分钟）

    // 微信登录接口
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestParam("code") String code) {
        System.out.println("接收到的 code: " + code);
        try {
            WxMaJscode2SessionResult session = wxMaService.getUserService().getSessionInfo(code);
            System.out.println("获取到的会话信息: " + session);
            User user = userService.findByOpenid(session.getOpenid());
            if (user == null) {
                user = new User();
                user.setOpenid(session.getOpenid());
                user.setSessionKey(session.getSessionKey());
                user.setCreatedTime(new Date());
                user.setRole("视障用户"); // 设置默认角色
                userService.createUser(user);
            }
            String token = JwtUtils.generateToken(user.getId());

            // 创建包含用户信息的返回对象
            UserInfoForLogin userInfo = new UserInfoForLogin(
                token,
                    user.getId(),
                user.getOpenid(),
                user.getNickname(),
                user.getAvatarUrl(),
                user.getRole()
            );
            System.out.println("nickname: " + user.getNickname());
            System.out.println("avatarUrl: " + user.getAvatarUrl());
            System.out.println("role: " + user.getRole());

            Map<String, Object> data = new HashMap<>();
            data.put("userInfo", userInfo);
            return Result.success("登录成功", data);

        } catch (WxErrorException e) {
            System.out.println("微信登录失败: " + e.getMessage());
            return Result.error("微信登录失败: " + e.getMessage());
        }
    }

    @PostMapping("/login/email")
    @ApiOperation("邮箱验证码登录")
    public Result<Map<String, Object>> loginByEmail(@RequestBody EmailLoginDto loginDto, HttpServletRequest request) {
        try {
            String ip = request.getRemoteAddr();
            String userAgent = request.getHeader("User-Agent");

            if (loginDto.getEmail() == null || loginDto.getEmail().trim().isEmpty()) {
                return Result.error("邮箱不能为空");
            }
            if (loginDto.getCode() == null || loginDto.getCode().trim().isEmpty()) {
                return Result.error("验证码不能为空");
            }

            // 从Redis获取验证码
            String storedCode = redisTemplate.opsForValue().get(loginDto.getEmail());
            if (storedCode == null) {
                return Result.error("验证码不存在或已过期，请重新获取验证码");
            }
            
            if (!loginDto.getCode().equals(storedCode)) {
                return Result.error("验证码错误");
            }

            // 验证码正确，查找或创建用户
            User user = userService.findByEmail(loginDto.getEmail());
            if (user == null) {
                // 创建新用户
                user = new User();
                user.setEmail(loginDto.getEmail());
                user.setCreatedTime(new Date());
                user.setRole("视障用户"); // 设置默认角色
                user.setStatus(1); // 新用户默认状态为正常
                userService.save(user);
                // 重新查询用户以获取ID
                user = userService.findByEmail(loginDto.getEmail());
            }

            // 检查用户状态
            if (user.getStatus() != null && user.getStatus() == 0) {
                loginLogService.recordLoginLog(user.getId(), loginDto.getEmail(), ip, userAgent, false, "账号被禁用");
                throw new BusinessException("账号已被禁用，请联系管理员");
            }

            // 生成JWT token
            String token = JwtUtils.generateToken(user.getId());
            String refreshToken = JwtUtils.generateRefreshToken(user.getId());

            // 删除已使用的验证码
            redisTemplate.delete(loginDto.getEmail());

            // 创建包含用户信息的返回对象
            UserInfoForLogin userInfo = new UserInfoForLogin(
                token,
                user.getId(),
                user.getOpenid(),
                user.getNickname(),
                user.getAvatarUrl(),
                user.getRole()
            );

            // 记录登录日志
            loginLogService.recordLoginLog(user.getId(), loginDto.getEmail(), ip, userAgent, true, null);

            Map<String, Object> data = new HashMap<>();
            data.put("userInfo", userInfo);
            data.put("refreshToken", refreshToken);
            data.put("hasPassword", user.getPassword() != null);

            return Result.success("登录成功", data);

        } catch (RuntimeException e) {
            System.out.println("登录异常: " + e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            System.out.println("登录失败: " + e.getMessage());
            return Result.error("登录失败：" + e.getMessage());
        }
    }

    @PostMapping("/set-password")
    @ApiOperation("设置密码")
    public Result<Void> setPassword(@RequestBody SetPasswordDto setPasswordDto, HttpServletRequest request) {
        try {
            if (setPasswordDto.getEmail() == null || setPasswordDto.getEmail().trim().isEmpty()) {
                return Result.error("邮箱不能为空");
            }
            if (setPasswordDto.getCode() == null || setPasswordDto.getCode().trim().isEmpty()) {
                return Result.error("验证码不能为空");
            }
            if (setPasswordDto.getPassword() == null || setPasswordDto.getPassword().trim().isEmpty()) {
                return Result.error("密码不能为空");
            }

            // 从Redis获取验证码
            String storedCode = redisTemplate.opsForValue().get(setPasswordDto.getEmail());
            if (storedCode == null) {
                return Result.error("验证码不存在或已过期，请重新获取验证码");
            }
            
            if (!setPasswordDto.getCode().equals(storedCode)) {
                return Result.error("验证码错误");
            }

            // 查找用户
            User user = userService.findByEmail(setPasswordDto.getEmail());
            if (user == null) {
                return Result.error("用户不存在");
            }

            // 更新密码
            LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(User::getId, user.getId())
                    .set(User::getPassword, PasswordUtils.encode(setPasswordDto.getPassword()));

            boolean success = userService.update(updateWrapper);
            if (!success) {
                return Result.error("密码设置失败，请重试");
            }

            // 删除已使用的验证码
            redisTemplate.delete(setPasswordDto.getEmail());

            // 记录操作日志
            String ip = request.getRemoteAddr();
            String userAgent = request.getHeader("User-Agent");
            loginLogService.recordLoginLog(user.getId(), setPasswordDto.getEmail(), ip, userAgent, true, "设置密码成功");

            return Result.success("密码设置成功", null);

        } catch (Exception e) {
            System.out.println("设置密码失败: " + e.getMessage());
            return Result.error("设置密码失败：" + e.getMessage());
        }
    }

    @ApiOperation(value = "发送邮箱验证码")
    @GetMapping(value = "/sendEmail/{email}")
    public String sendCode(@PathVariable String email) {
        try {
            // 直接发送新的验证码，覆盖旧的验证码
            boolean b = mailMsg.mail(email);
            if (b) {
                return "验证码发送成功！";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "邮箱不正确或为空！";
    }



    /**
     * 增加登录失败次数
     */
    private void incrementFailCount(String failKey) {
        String count = redisTemplate.opsForValue().get(failKey);
        if (count == null) {
            redisTemplate.opsForValue().set(failKey, "1", LOCK_TIME, TimeUnit.MINUTES);
        } else {
            redisTemplate.opsForValue().increment(failKey);
        }
    }

    @PostMapping("/admin/refresh-token")
    @ApiOperation("刷新访问令牌")
    public Result<Map<String, Object>> refreshToken(@RequestParam String refreshToken) {
        try {
            if (JwtUtils.isTokenExpired(refreshToken)) {
                return Result.error("刷新令牌已过期，请重新登录");
            }
            Long userId = JwtUtils.getUserIdFromToken(refreshToken);
            String newAccessToken = JwtUtils.generateToken(userId);

            Map<String, Object> data = new HashMap<>();
            data.put("accessToken", newAccessToken);
            return Result.success("令牌刷新成功", data);
        } catch (Exception e) {
            return Result.error("令牌刷新失败: " + e.getMessage());
        }
    }

    @PostMapping("/admin/register")
    @ApiOperation("管理员注册")
    public Result<Void> registerAdmin(@RequestBody AdminRegisterDto registerDto) {
        try {
            if (registerDto.getUsername() == null || registerDto.getUsername().trim().isEmpty() ||
                registerDto.getPassword() == null || registerDto.getPassword().trim().isEmpty() ||
                registerDto.getEmail() == null || registerDto.getEmail().trim().isEmpty()) {
                return Result.error("用户名、密码和邮箱均不能为空");
            }

            User existingUserByUsername = userService.lambdaQuery()
                    .eq(User::getUsername, registerDto.getUsername())
                    .one();
            if (existingUserByUsername != null) {
                return Result.error("用户名已存在");
            }

            User existingUserByEmail = userService.lambdaQuery()
                    .eq(User::getEmail, registerDto.getEmail())
                    .one();
            if (existingUserByEmail != null) {
                return Result.error("邮箱已被注册");
            }

            User admin = new User();
            admin.setUsername(registerDto.getUsername());
            admin.setPassword(PasswordUtils.encode(registerDto.getPassword()));
            admin.setEmail(registerDto.getEmail());
            admin.setRole("管理员");
            admin.setCreatedTime(new Date());
            admin.setStatus(1); // 管理员默认状态为正常
            userService.save(admin);
            return Result.success("管理员注册成功", null);
        } catch (Exception e) {
            System.out.println("管理员注册失败: " + e.getMessage());
            return Result.error("注册失败：" + e.getMessage());
        }
    }

    @Data
    static class UserInfoForLogin {
        private String token;
        private Long id;
        private String openid;
        private String nickname;
        private String avatarUrl;
        private String role;

        public UserInfoForLogin(String token, Long id, String openid, String nickname, String avatarUrl, String role) {
            this.token = token;
            this.id = id;
            this.openid = openid;
            this.nickname = nickname;
            this.avatarUrl = avatarUrl;
            this.role = role;
        }
    }

    // 用户信息保存接口
    @PostMapping("/saveUserInfo")
    public Result<Void> saveUserInfo(@RequestBody UserInfoDto userInfo) {
        // 只校验 id 参数
        if (userInfo.getId() == null) {
            return Result.error("用户ID为必填参数");
        }

        // 使用 LambdaUpdateWrapper 直接更新
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, userInfo.getId());
        
        // 如果传入了昵称，则更新昵称
        if (userInfo.getNickname() != null) {
            updateWrapper.set(User::getNickname, userInfo.getNickname());
        }
        
        // 如果传入了头像URL，则更新头像
        if (userInfo.getAvatarUrl() != null) {
            updateWrapper.set(User::getAvatarUrl, userInfo.getAvatarUrl());
        }

        boolean success = userService.update(updateWrapper);
        
        if (success) {
            return Result.success("用户信息保存成功", null);
        } else {
            return Result.error("用户信息保存失败");
        }
    }

    // 通用用户信息修改接口
    @PostMapping("/updateUserInfo")
    public Result<Void> updateUserInfo(@RequestBody zysy.iflytek.zhumang.user.dto.UserUpdateDto userInfo) {
        if (userInfo.getId() == null) {
            return Result.error("用户ID为必填参数");
        }
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, userInfo.getId());
        if (userInfo.getNickname() != null) updateWrapper.set(User::getNickname, userInfo.getNickname());
        if (userInfo.getAvatarUrl() != null) updateWrapper.set(User::getAvatarUrl, userInfo.getAvatarUrl());
        if (userInfo.getPhone() != null) updateWrapper.set(User::getPhone, userInfo.getPhone());
        if (userInfo.getUsualAddress() != null) updateWrapper.set(User::getUsualAddress, userInfo.getUsualAddress());
        if (userInfo.getVoiceSettings() != null) updateWrapper.set(User::getVoiceSettings, userInfo.getVoiceSettings());
        if (userInfo.getServiceHours() != null) updateWrapper.set(User::getServiceHours, userInfo.getServiceHours());
        if (userInfo.getServiceRating() != null) updateWrapper.set(User::getServiceRating, userInfo.getServiceRating());
        if (userInfo.getOrganization() != null) updateWrapper.set(User::getOrganization, userInfo.getOrganization());
        if (userInfo.getCertificationTime() != null) updateWrapper.set(User::getCertificationTime, userInfo.getCertificationTime());
        if (userInfo.getUsername() != null) updateWrapper.set(User::getUsername, userInfo.getUsername());
        // 不允许 role 和 permission 修改
        boolean success = userService.update(updateWrapper);
        if (success) {
            return Result.success("用户信息修改成功", null);
        } else {
            return Result.error("用户信息修改失败");
        }
    }

    // 用户基本信息查询接口
    @GetMapping("/getUserInfo")
    public Result<Map<String, Object>> getUserInfo(@RequestParam("id") Long id) {
        if (id == null) {
            return Result.error("用户ID为必填参数");
        }
        User user = userService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        // 只返回基本信息
        zysy.iflytek.zhumang.user.dto.UserUpdateDto userInfo = new zysy.iflytek.zhumang.user.dto.UserUpdateDto();
        userInfo.setId(user.getId());
        userInfo.setNickname(user.getNickname());
        userInfo.setAvatarUrl(user.getAvatarUrl());
        userInfo.setPhone(user.getPhone());
        userInfo.setUsualAddress(user.getUsualAddress());
        userInfo.setVoiceSettings(user.getVoiceSettings());
        userInfo.setServiceHours(user.getServiceHours());
        userInfo.setServiceRating(user.getServiceRating());
        userInfo.setOrganization(user.getOrganization());
        userInfo.setCertificationTime(user.getCertificationTime());
        userInfo.setUsername(user.getUsername());

        Map<String, Object> data = new HashMap<>();
        data.put("userInfo", userInfo);
        // 不返回 role 和 permission
        return Result.success("查询成功", data);
    }

    // 基于邮箱的用户基本信息查询接口
    // 志愿者注册接口
    @PostMapping("/volunteer/register")
    public Result<Void> registerVolunteer(@RequestBody VolunteerRegisterDto registerDto) {
        try {
            if (registerDto.getOpenid() == null || registerDto.getOpenid().trim().isEmpty() ||
                registerDto.getNickname() == null || registerDto.getNickname().trim().isEmpty() ||
                registerDto.getAvatarUrl() == null || registerDto.getAvatarUrl().trim().isEmpty() ||
                registerDto.getPhone() == null || registerDto.getPhone().trim().isEmpty() ||
                registerDto.getIdCard() == null || registerDto.getIdCard().trim().isEmpty()) {
                return Result.error("OpenId, 昵称, 头像URL, 手机号, 身份证号均为必填参数");
            }

            User existingUser = userService.findByOpenid(registerDto.getOpenid());
            if (existingUser != null) {
                return Result.error("该OpenId已注册");
        }

            // 检查手机号是否已存在
            User existingUserByPhone = userService.lambdaQuery()
                    .eq(User::getPhone, registerDto.getPhone())
                    .one();
            if (existingUserByPhone != null) {
                return Result.error("手机号已被注册");
            }

            User volunteer = new User();
            volunteer.setOpenid(registerDto.getOpenid());
            volunteer.setNickname(registerDto.getNickname());
            volunteer.setAvatarUrl(registerDto.getAvatarUrl());
            volunteer.setPhone(registerDto.getPhone());
            volunteer.setIdCard(registerDto.getIdCard());
            volunteer.setRole("志愿者");
            volunteer.setCreatedTime(new Date());
            volunteer.setStatus(1); // 志愿者默认状态为正常
            userService.save(volunteer);
            return Result.success("志愿者注册成功", null);
        } catch (Exception e) {
            System.out.println("志愿者注册失败: " + e.getMessage());
            return Result.error("注册失败：" + e.getMessage());
        }
    }

    @PostMapping("/login/password")
    @ApiOperation("用户密码登录")
    public Result<Map<String, Object>> loginByPassword(@RequestBody UserLoginDto loginDto, HttpServletRequest request) {
        try {
            if (loginDto.getEmail() == null || loginDto.getEmail().trim().isEmpty()) {
                return Result.error("邮箱不能为空");
            }
            if (loginDto.getPassword() == null || loginDto.getPassword().trim().isEmpty()) {
                return Result.error("密码不能为空");
            }

            String email = loginDto.getEmail().trim();
            String ip = request.getRemoteAddr();
            String userAgent = request.getHeader("User-Agent");

            // 检查是否被锁定
            String failKey = LOGIN_FAIL_PREFIX + email;
            String failCount = redisTemplate.opsForValue().get(failKey);
            if (failCount != null && Integer.parseInt(failCount) >= MAX_FAIL_COUNT) {
                loginLogService.recordLoginLog(null, email, ip, userAgent, false, "账号已被锁定");
                return Result.error("登录失败次数过多，请" + LOCK_TIME + "分钟后再试");
            }

            // 查找用户（不再排除管理员账号）
            User user = userService.lambdaQuery()
                    .eq(User::getEmail, email)
                    .one();

        if (user == null) {
                // 记录失败次数
                incrementFailCount(failKey);
                loginLogService.recordLoginLog(null, email, ip, userAgent, false, "用户不存在");
                return Result.error("邮箱或密码错误");
        }

            // 验证密码
            if (!PasswordUtils.matches(loginDto.getPassword(), user.getPassword())) {
                // 记录失败次数
                incrementFailCount(failKey);
                loginLogService.recordLoginLog(user.getId(), email, ip, userAgent, false, "密码错误");
                return Result.error("邮箱或密码错误");
            }

            // 检查用户状态
            if (user.getStatus() != null && user.getStatus() == 0) {
                loginLogService.recordLoginLog(user.getId(), email, ip, userAgent, false, "账号被禁用");
                throw new BusinessException("账号已被禁用，请联系管理员");
            }

            // 登录成功，清除失败记录
            redisTemplate.delete(failKey);

            // 生成访问令牌和刷新令牌
            String accessToken = JwtUtils.generateToken(user.getId());
            String refreshToken = JwtUtils.generateRefreshToken(user.getId());

            // 创建包含用户信息的返回对象
            UserInfoForLogin userInfo = new UserInfoForLogin(
                accessToken,
                user.getId(),
                user.getOpenid(),
                user.getNickname(),
                user.getAvatarUrl(),
                user.getRole()
            );

            // 记录成功日志
            loginLogService.recordLoginLog(user.getId(), email, ip, userAgent, true, null);

            Map<String, Object> data = new HashMap<>();
            data.put("userInfo", userInfo);
            data.put("refreshToken", refreshToken);

            return Result.success("登录成功", data);

        } catch (Exception e) {
            System.out.println("用户登录失败: " + e.getMessage());
            return Result.error("登录失败：" + e.getMessage());
        }
    }

    @ApiOperation("获取腾讯云UserSig")
    @GetMapping("/userSig")
    public Result<Map<String, Object>> getUserSig(@RequestParam("userID") String userID) {
        try {
            String userSig = GenerateTestUserSig.generateUserSig(userID, 604800L); // 7天有效期
            Map<String, Object> data = new HashMap<>();
            data.put("userSig", userSig);
            return Result.success("获取UserSig成功", data);
        } catch (Exception e) {
            return Result.error("获取UserSig失败: " + e.getMessage());
        }
    }

    @Data
    static class UserSigResponse {
        public long sdkAppID;
        public String userSig;

        public UserSigResponse(long sdkAppID, String userSig) {
            this.sdkAppID = sdkAppID;
            this.userSig = userSig;
    }
}
}



