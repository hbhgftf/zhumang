package zysy.iflytek.zhumang.policy.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class PolicyCreateDto {
    @NotBlank(message = "政策标题不能为空")
    private String title;
    
    @NotNull(message = "发布日期不能为空")
    private LocalDate publishDate;
    
    @NotBlank(message = "政策级别不能为空")
    private String policyLevel;
    
    private String tags;
    
    @NotBlank(message = "政策内容不能为空")
    private String content;
    
    private String attachmentUrl;
} 