package zysy.iflytek.zhumang.policy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("blind_policy")
public class BlindPolicy {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private LocalDate publishDate;
    private String policyLevel;
    private String tags;
    private String content;
    private String attachmentUrl;
    private Integer status;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    private Long createdBy;
    private Long updatedBy;
} 