package zysy.iflytek.zhumang.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("用户账号密码登录请求")
public class UserLoginDto {
    
    @ApiModelProperty(value = "邮箱", required = true)
    private String email;
    
    @ApiModelProperty(value = "密码", required = true)
    private String password;
} 