package zysy.iflytek.zhumang.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import zysy.iflytek.zhumang.user.entity.LoginLog;

public interface ILoginLogService extends IService<LoginLog> {
    void recordLoginLog(Long userId, String email, String ip, String userAgent, boolean success, String failReason);
} 