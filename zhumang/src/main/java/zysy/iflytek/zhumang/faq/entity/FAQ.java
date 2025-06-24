package zysy.iflytek.zhumang.faq.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("faq")
public class FAQ {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String question;
    private String answer;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
