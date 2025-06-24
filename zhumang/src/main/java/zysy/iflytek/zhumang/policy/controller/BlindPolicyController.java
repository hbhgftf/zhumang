package zysy.iflytek.zhumang.policy.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import zysy.iflytek.zhumang.common.model.Result;
import zysy.iflytek.zhumang.policy.dto.PolicyCreateDto;
import zysy.iflytek.zhumang.policy.dto.PolicyUpdateDto;
import zysy.iflytek.zhumang.policy.entity.BlindPolicy;
import zysy.iflytek.zhumang.policy.service.BlindPolicyService;

import javax.annotation.Resource;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Api(tags = "助盲政策管理")
@RestController
@RequestMapping("/policy")
public class BlindPolicyController {

    private static final Logger logger = LoggerFactory.getLogger(BlindPolicyController.class);

    @Resource
    private BlindPolicyService blindPolicyService;

    @ApiOperation("创建政策")
    @PostMapping
    public Result<BlindPolicy> createPolicy(@RequestBody @Valid PolicyCreateDto dto,
                                          @RequestAttribute Long userId) {
        return Result.success(blindPolicyService.createPolicy(dto, userId));
    }

    @ApiOperation("更新政策")
    @PutMapping("/update/{id}")
    public Result<BlindPolicy> updatePolicy(@PathVariable Long id,
                                          @RequestBody @Valid PolicyUpdateDto dto,
                                          @RequestAttribute Long userId) {
        dto.setId(id);
        return Result.success(blindPolicyService.updatePolicy(dto, userId));
    }

    @ApiOperation("删除政策")
    @DeleteMapping("/delete/{id}")
    public Result<Void> deletePolicy(@PathVariable Long id) {
        blindPolicyService.deletePolicy(id);
        return Result.success();
    }

    @ApiOperation("分页查询政策")
    @GetMapping("/page")
    public Result<IPage<BlindPolicy>> pagePolicies(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "5") Integer pageSize,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String policyLevel,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) Integer status) {
        return Result.success(blindPolicyService.pagePolicies(pageNum, pageSize, title, policyLevel, tags, status));
    }

    @ApiOperation("获取政策详情")
    @GetMapping("/detail/{id}")
    public Result<BlindPolicy> getPolicyDetail(@PathVariable Long id) {
        return Result.success(blindPolicyService.getPolicyDetail(id));
    }

    @ApiOperation("收藏政策")
    @PostMapping("/favorite/{id}")
    public Result<Void> favoritePolicy(@PathVariable Long id,
                                     @RequestAttribute Long userId) {
        blindPolicyService.favoritePolicy(id, userId);
        return Result.success();
    }

    @ApiOperation("取消收藏")
    @DeleteMapping("/favorite/{id}")
    public Result<Void> unfavoritePolicy(@PathVariable Long id,
                                       @RequestAttribute Long userId) {
        blindPolicyService.unfavoritePolicy(id, userId);
        return Result.success();
    }

    @ApiOperation("检查是否已收藏")
    @GetMapping("/favorite/{id}")
    public Result<Boolean> isFavorited(@PathVariable Long id,
                                     @RequestAttribute Long userId) {
        return Result.success(blindPolicyService.isFavorited(id, userId));
    }

    @ApiOperation("获取用户的收藏列表")
    @GetMapping("/favorites")
    public Result<IPage<BlindPolicy>> getUserFavorites(
            @RequestAttribute Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(blindPolicyService.getUserFavorites(userId, pageNum, pageSize));
    }
} 