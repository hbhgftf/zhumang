package zysy.iflytek.zhumang.faq.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("FAQ查询DTO")
public class FAQQueryDto {
    @ApiModelProperty("页码")
    private Integer pageNum = 1;
    @ApiModelProperty("每页数量")
    private Integer pageSize = 10;
    @ApiModelProperty("搜索关键字")
    private String keyword;
}
