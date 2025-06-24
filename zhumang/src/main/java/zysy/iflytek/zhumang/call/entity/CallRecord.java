package zysy.iflytek.zhumang.call.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("call_record")
public class CallRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String roomId;
    private Long callerId;
    private Long calleeId;
    private Integer status; // 0-等待中，1-进行中，2-已结束，3-已拒绝，4-已取消
    private Date startTime;
    private Date endTime;
    private Integer duration;
    private Date createdTime;
    private Date updatedTime;
} 