package zysy.iflytek.zhumang.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("管理员注册请求")
public class AdminRegisterDto {
    
    @ApiModelProperty(value = "用户名", required = true)
    private String username;
    
    @ApiModelProperty(value = "邮箱", required = true)
    private String email;
    
    @ApiModelProperty(value = "密码", required = true)
    private String password;
} 