package zysy.iflytek.zhumang.policy.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import zysy.iflytek.zhumang.policy.dto.PolicyCreateDto;
import zysy.iflytek.zhumang.policy.dto.PolicyUpdateDto;
import zysy.iflytek.zhumang.policy.entity.BlindPolicy;

public interface BlindPolicyService extends IService<BlindPolicy> {
    // 创建政策
    BlindPolicy createPolicy(PolicyCreateDto dto, Long userId);
    
    // 更新政策
    BlindPolicy updatePolicy(PolicyUpdateDto dto, Long userId);
    
    // 删除政策
    void deletePolicy(Long id);
    
    // 分页查询政策
    IPage<BlindPolicy> pagePolicies(Integer pageNum, Integer pageSize, String title, 
                                   String policyLevel, String tags, Integer status);
    
    // 获取政策详情
    BlindPolicy getPolicyDetail(Long id);
    
    // 收藏政策
    void favoritePolicy(Long policyId, Long userId);
    
    // 取消收藏
    void unfavoritePolicy(Long policyId, Long userId);
    
    // 检查是否已收藏
    boolean isFavorited(Long policyId, Long userId);
    
    // 获取用户的收藏列表
    IPage<BlindPolicy> getUserFavorites(Long userId, Integer pageNum, Integer pageSize);
} 