package zysy.iflytek.zhumang.faq.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import zysy.iflytek.zhumang.faq.entity.FAQ;

public interface FAQService extends IService<FAQ> {
    IPage<FAQ> pageFAQ(Integer pageNum, Integer pageSize, String keyword);
}
