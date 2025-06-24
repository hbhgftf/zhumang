package zysy.iflytek.zhumang.policy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("policy_favorite")
public class PolicyFavorite {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long policyId;
    private LocalDateTime createdTime;
} 