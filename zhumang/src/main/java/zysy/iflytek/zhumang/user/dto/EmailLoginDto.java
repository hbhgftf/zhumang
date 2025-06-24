package zysy.iflytek.zhumang.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("邮箱验证码登录请求")
public class EmailLoginDto {
    
    @ApiModelProperty(value = "邮箱", required = true)
    private String email;
    
    @ApiModelProperty(value = "验证码", required = true)
    private String code;
} 