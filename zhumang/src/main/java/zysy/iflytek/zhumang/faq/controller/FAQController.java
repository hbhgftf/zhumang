package zysy.iflytek.zhumang.faq.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import zysy.iflytek.zhumang.common.exception.BusinessException;
import zysy.iflytek.zhumang.common.model.Result;
import zysy.iflytek.zhumang.faq.dto.FAQCreateDto;
import zysy.iflytek.zhumang.faq.dto.FAQUpdateDto;
import zysy.iflytek.zhumang.faq.dto.FAQQueryDto;
import zysy.iflytek.zhumang.faq.entity.FAQ;
import zysy.iflytek.zhumang.faq.service.FAQService;
import zysy.iflytek.zhumang.user.entity.User;
import zysy.iflytek.zhumang.user.service.IUserService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Api(tags = "常见问题FAQ管理")
@RestController
@RequestMapping("/faq")
public class FAQController {
    @Autowired
    private FAQService faqService;
    @Resource
    private IUserService userService;

    private void checkAdmin(HttpServletRequest request) {
        Object userIdObj = request.getAttribute("userId");
        if (userIdObj == null) throw new BusinessException("未登录");
        Long userId = Long.valueOf(userIdObj.toString());
        User user = userService.getById(userId);
        if (user == null || !"管理员".equals(user.getRole())) {
            throw new BusinessException("无权限，仅管理员可操作");
        }
    }

    @ApiOperation("分页查询FAQ（支持模糊搜索）")
    @GetMapping("/page")
    public Result<IPage<FAQ>> pageFAQ(FAQQueryDto queryDto) {
        int pageNum = queryDto.getPageNum() == null ? 1 : queryDto.getPageNum();
        int pageSize = queryDto.getPageSize() == null ? 5 : queryDto.getPageSize();
        return Result.success(faqService.pageFAQ(pageNum, pageSize, queryDto.getKeyword()));
    }

    @ApiOperation("新增FAQ")
    @PostMapping
    public Result<FAQ> addFAQ(@RequestBody FAQCreateDto dto, HttpServletRequest request) {
        checkAdmin(request);
        FAQ faq = new FAQ();
        faq.setQuestion(dto.getQuestion());
        faq.setAnswer(dto.getAnswer());
        faqService.save(faq);
        return Result.success(faq);
    }

    @ApiOperation("修改FAQ")
    @PutMapping("/{id}")
    public Result<FAQ> updateFAQ(@PathVariable Integer id, @RequestBody FAQUpdateDto dto, HttpServletRequest request) {
        checkAdmin(request);
        FAQ faq = faqService.getById(id);
        if (faq == null) throw new BusinessException("FAQ不存在");
        faq.setQuestion(dto.getQuestion());
        faq.setAnswer(dto.getAnswer());
        faqService.updateById(faq);
        return Result.success(faq);
    }

    @ApiOperation("删除FAQ")
    @DeleteMapping("/{id}")
    public Result<Void> deleteFAQ(@PathVariable Integer id, HttpServletRequest request) {
        checkAdmin(request);
        faqService.removeById(id);
        return Result.success();
    }

    @ApiOperation("查询所有FAQ（不分页，前端首页用）")
    @GetMapping("/all")
    public Result<java.util.List<FAQ>> listAllFAQ() {
        return Result.success(faqService.list());
    }
}
