package zysy.iflytek.zhumang.faq.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import zysy.iflytek.zhumang.faq.entity.FAQ;
import zysy.iflytek.zhumang.faq.mapper.FAQMapper;
import zysy.iflytek.zhumang.faq.service.FAQService;

@Service
public class FAQServiceImpl extends ServiceImpl<FAQMapper, FAQ> implements FAQService {
    @Override
    public IPage<FAQ> pageFAQ(Integer pageNum, Integer pageSize, String keyword) {
        LambdaQueryWrapper<FAQ> wrapper = new LambdaQueryWrapper<>();
        if (org.springframework.util.StringUtils.hasText(keyword)) {
            wrapper.like(FAQ::getQuestion, keyword).or().like(FAQ::getAnswer, keyword);
        }
        wrapper.orderByDesc(FAQ::getUpdateTime);
        return this.page(new Page<>(pageNum, pageSize), wrapper);
    }
}
