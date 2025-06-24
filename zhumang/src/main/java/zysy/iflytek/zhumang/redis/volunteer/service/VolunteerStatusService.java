package zysy.iflytek.zhumang.redis.volunteer.service;

import java.util.List;

public interface VolunteerStatusService {
    /**
     * 设置志愿者在线状态
     * @param userId 志愿者用户ID
     * @param isOnline 是否在线
     */
    void setVolunteerStatus(Long userId, boolean isOnline);

    /**
     * 检查志愿者是否在线
     * @param userId 志愿者用户ID
     * @return 是否在线
     */
    boolean isVolunteerOnline(Long userId);

    /**
     * 刷新志愿者心跳
     * @param userId 志愿者用户ID
     */
    void refreshVolunteerStatus(Long userId);

    /**
     * 获取在线志愿者数量
     * @return 在线志愿者数量
     */
    Long getOnlineVolunteerCount();

    /**
     * 获取所有在线志愿者的ID列表
     * @return 在线志愿者ID列表
     */
    List<Long> getOnlineVolunteers();
} 