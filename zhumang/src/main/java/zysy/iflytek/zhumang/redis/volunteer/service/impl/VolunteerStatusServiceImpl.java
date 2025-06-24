package zysy.iflytek.zhumang.redis.volunteer.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import zysy.iflytek.zhumang.redis.volunteer.service.VolunteerStatusService;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class VolunteerStatusServiceImpl implements VolunteerStatusService {

    private static final String VOLUNTEER_STATUS_KEY_PREFIX = "volunteer:status:";
    private static final long STATUS_EXPIRE_TIME = 30; // 状态过期时间（分钟）

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void setVolunteerStatus(Long userId, boolean isOnline) {
        String key = VOLUNTEER_STATUS_KEY_PREFIX + userId;
        if (isOnline) {
            redisTemplate.opsForValue().set(key, "online", STATUS_EXPIRE_TIME, TimeUnit.MINUTES);
        } else {
            redisTemplate.delete(key);
        }
    }

    @Override
    public boolean isVolunteerOnline(Long userId) {
        String key = VOLUNTEER_STATUS_KEY_PREFIX + userId;
        Object status = redisTemplate.opsForValue().get(key);
        return status != null && "online".equals(status.toString());
    }

    @Override
    public void refreshVolunteerStatus(Long userId) {
        String key = VOLUNTEER_STATUS_KEY_PREFIX + userId;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            redisTemplate.expire(key, STATUS_EXPIRE_TIME, TimeUnit.MINUTES);
        }
    }

    @Override
    public Long getOnlineVolunteerCount() {
        Set<String> keys = redisTemplate.keys(VOLUNTEER_STATUS_KEY_PREFIX + "*");
        return keys != null ? (long) keys.size() : 0L;
    }

    @Override
    public List<Long> getOnlineVolunteers() {
        Set<String> keys = redisTemplate.keys(VOLUNTEER_STATUS_KEY_PREFIX + "*");
        if (keys == null) {
            return Collections.emptyList();
        }
        return keys.stream()
                .map(key -> Long.parseLong(key.substring(VOLUNTEER_STATUS_KEY_PREFIX.length())))
                .collect(Collectors.toList());
    }
} 