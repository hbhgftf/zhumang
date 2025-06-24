package zysy.iflytek.zhumang.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("login_log")
public class LoginLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private String email;
    
    private String ip;
    
    private String userAgent;
    
    private Boolean success;
    
    private String failReason;
    
    private Date loginTime;
} 