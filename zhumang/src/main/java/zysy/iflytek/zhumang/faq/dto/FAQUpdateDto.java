package zysy.iflytek.zhumang.faq.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("FAQ更新DTO")
public class FAQUpdateDto {
    @ApiModelProperty("主键ID")
    private Integer id;
    @ApiModelProperty("问题内容")
    private String question;
    @ApiModelProperty("答案内容")
    private String answer;
}
