package zysy.iflytek.zhumang.policy.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class PolicyUpdateDto {
    @NotNull(message = "政策ID不能为空")
    private Long id;
    
    private String title;
    private LocalDate publishDate;
    private String policyLevel;
    private String tags;
    private String content;
    private String attachmentUrl;
    private Integer status;
} 