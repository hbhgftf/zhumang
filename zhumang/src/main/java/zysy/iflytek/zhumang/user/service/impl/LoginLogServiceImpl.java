package zysy.iflytek.zhumang.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import zysy.iflytek.zhumang.user.entity.LoginLog;
import zysy.iflytek.zhumang.user.mapper.LoginLogMapper;
import zysy.iflytek.zhumang.user.service.ILoginLogService;
import java.util.Date;

@Service
public class LoginLogServiceImpl extends ServiceImpl<LoginLogMapper, LoginLog> implements ILoginLogService {
    
    @Override
    public void recordLoginLog(Long userId, String email, String ip, String userAgent, boolean success, String failReason) {
        LoginLog log = new LoginLog();
        log.setUserId(userId);
        log.setEmail(email);
        log.setIp(ip);
        log.setUserAgent(userAgent);
        log.setSuccess(success);
        log.setFailReason(failReason);
        log.setLoginTime(new Date());
        save(log);
    }
} 