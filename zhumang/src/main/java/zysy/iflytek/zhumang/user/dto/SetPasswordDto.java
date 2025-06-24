package zysy.iflytek.zhumang.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("设置密码请求")
public class SetPasswordDto {
    
    @ApiModelProperty(value = "邮箱", required = true)
    private String email;
    
    @ApiModelProperty(value = "验证码", required = true)
    private String code;
    
    @ApiModelProperty(value = "新密码", required = true)
    private String password;
} 