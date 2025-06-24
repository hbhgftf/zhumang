package zysy.iflytek.zhumang.user.service;

import zysy.iflytek.zhumang.user.entity.Feedback;
import com.baomidou.mybatisplus.core.metadata.IPage;

public interface FeedbackService {
    void submitFeedback(Feedback feedback);
    IPage<Feedback> pageFeedback(int pageNum, int pageSize, Long userId, String startTime, String endTime);
    Feedback getFeedbackById(Long id);
} 