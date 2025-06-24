package zysy.iflytek.zhumang.user.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import zysy.iflytek.zhumang.user.entity.Feedback;
import zysy.iflytek.zhumang.user.mapper.FeedbackMapper;
import zysy.iflytek.zhumang.user.service.FeedbackService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

@Service
public class FeedbackServiceImpl implements FeedbackService {
    @Autowired
    private FeedbackMapper feedbackMapper;

    @Override
    public void submitFeedback(Feedback feedback) {
        feedbackMapper.insert(feedback);
    }

    @Override
    public IPage<Feedback> pageFeedback(int pageNum, int pageSize, Long userId, String startTime, String endTime) {
        QueryWrapper<Feedback> wrapper = new QueryWrapper<>();
        if (userId != null) wrapper.eq("user_id", userId);
        if (startTime != null && !startTime.isEmpty()) wrapper.ge("create_time", startTime);
        if (endTime != null && !endTime.isEmpty()) wrapper.le("create_time", endTime);
        wrapper.orderByDesc("create_time");
        return feedbackMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public Feedback getFeedbackById(Long id) {
        return feedbackMapper.selectById(id);
    }
} 