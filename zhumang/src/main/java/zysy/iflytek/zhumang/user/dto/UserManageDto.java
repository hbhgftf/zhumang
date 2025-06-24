package zysy.iflytek.zhumang.user.dto;

import lombok.Data;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class UserManageDto {
    private Long id;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Pattern(regexp = "^(视障用户|管理员)$", message = "角色只能是'视障用户'或'管理员'")
    private String role;

    private String password;  // 可选，如果提供则更新密码

    private Integer status;   // 1-正常，0-禁用
} 