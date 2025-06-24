package zysy.iflytek.zhumang.policy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import zysy.iflytek.zhumang.common.exception.BusinessException;
import zysy.iflytek.zhumang.policy.dto.PolicyCreateDto;
import zysy.iflytek.zhumang.policy.dto.PolicyUpdateDto;
import zysy.iflytek.zhumang.policy.entity.BlindPolicy;
import zysy.iflytek.zhumang.policy.entity.PolicyFavorite;
import zysy.iflytek.zhumang.policy.mapper.BlindPolicyMapper;
import zysy.iflytek.zhumang.policy.mapper.PolicyFavoriteMapper;
import zysy.iflytek.zhumang.policy.service.BlindPolicyService;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BlindPolicyServiceImpl extends ServiceImpl<BlindPolicyMapper, BlindPolicy> implements BlindPolicyService {

    @Resource
    private PolicyFavoriteMapper favoriteMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BlindPolicy createPolicy(PolicyCreateDto dto, Long userId) {
        BlindPolicy policy = new BlindPolicy();
        BeanUtils.copyProperties(dto, policy);
        policy.setStatus(1);
        policy.setCreatedBy(userId);
        policy.setUpdatedBy(userId);
        save(policy);
        return policy;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BlindPolicy updatePolicy(PolicyUpdateDto dto, Long userId) {
        BlindPolicy policy = getById(dto.getId());
        if (policy == null) {
            throw new BusinessException("政策不存在");
        }
        BeanUtils.copyProperties(dto, policy);
        policy.setUpdatedBy(userId);
        updateById(policy);
        return policy;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePolicy(Long id) {
        removeById(id);
        // 删除相关的收藏记录
        LambdaQueryWrapper<PolicyFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PolicyFavorite::getPolicyId, id);
        favoriteMapper.delete(wrapper);
    }

    @Override
    public IPage<BlindPolicy> pagePolicies(Integer pageNum, Integer pageSize, String title,
                                         String policyLevel, String tags, Integer status) {
        LambdaQueryWrapper<BlindPolicy> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(title), BlindPolicy::getTitle, title)
                .eq(StringUtils.hasText(policyLevel), BlindPolicy::getPolicyLevel, policyLevel)
                .like(StringUtils.hasText(tags), BlindPolicy::getTags, tags)
                .eq(status != null, BlindPolicy::getStatus, status)
                .orderByDesc(BlindPolicy::getCreatedTime);
        
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public BlindPolicy getPolicyDetail(Long id) {
        BlindPolicy policy = getById(id);
        if (policy == null) {
            throw new BusinessException("政策不存在");
        }
        return policy;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void favoritePolicy(Long policyId, Long userId) {
        // 检查政策是否存在
        if (!getById(policyId).getStatus().equals(1)) {
            throw new BusinessException("政策不存在或已失效");
        }
        
        // 检查是否已收藏
        if (isFavorited(policyId, userId)) {
            throw new BusinessException("已经收藏过该政策");
        }
        
        PolicyFavorite favorite = new PolicyFavorite();
        favorite.setPolicyId(policyId);
        favorite.setUserId(userId);
        favoriteMapper.insert(favorite);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfavoritePolicy(Long policyId, Long userId) {
        LambdaQueryWrapper<PolicyFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PolicyFavorite::getPolicyId, policyId)
                .eq(PolicyFavorite::getUserId, userId);
        favoriteMapper.delete(wrapper);
    }

    @Override
    public boolean isFavorited(Long policyId, Long userId) {
        LambdaQueryWrapper<PolicyFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PolicyFavorite::getPolicyId, policyId)
                .eq(PolicyFavorite::getUserId, userId);
        return favoriteMapper.selectCount(wrapper) > 0;
    }

    @Override
    public IPage<BlindPolicy> getUserFavorites(Long userId, Integer pageNum, Integer pageSize) {
        // 先查询用户的收藏记录
        LambdaQueryWrapper<PolicyFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PolicyFavorite::getUserId, userId)
                .orderByDesc(PolicyFavorite::getCreatedTime);
        Page<PolicyFavorite> favoritePage = new Page<>(pageNum, pageSize);
        IPage<PolicyFavorite> favoriteIPage = favoriteMapper.selectPage(favoritePage, wrapper);
        
        // 获取收藏的政策ID列表
        List<Long> policyIds = favoriteIPage.getRecords().stream()
                .map(PolicyFavorite::getPolicyId)
                .collect(Collectors.toList());
        
        if (policyIds.isEmpty()) {
            return new Page<>();
        }
        
        // 查询政策详情
        LambdaQueryWrapper<BlindPolicy> policyWrapper = new LambdaQueryWrapper<>();
        policyWrapper.in(BlindPolicy::getId, policyIds)
                .eq(BlindPolicy::getStatus, 1);
        List<BlindPolicy> policies = list(policyWrapper);
        
        // 构建返回结果
        Page<BlindPolicy> resultPage = new Page<>(pageNum, pageSize, favoriteIPage.getTotal());
        resultPage.setRecords(policies);
        return resultPage;
    }
} 