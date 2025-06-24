-- 通话记录表
CREATE TABLE IF NOT EXISTS `call_record` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `room_id` varchar(64) NOT NULL COMMENT '通话房间ID',
    `caller_id` bigint(20) NOT NULL COMMENT '主叫用户ID',
    `callee_id` bigint(20) NOT NULL COMMENT '被叫用户ID',
    `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '通话状态：0-等待中，1-进行中，2-已结束，3-已拒绝，4-已取消',
    `start_time` datetime DEFAULT NULL COMMENT '通话开始时间',
    `end_time` datetime DEFAULT NULL COMMENT '通话结束时间',
    `duration` int(11) DEFAULT '0' COMMENT '通话时长（秒）',
    `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_room_id` (`room_id`),
    KEY `idx_caller_id` (`caller_id`),
    KEY `idx_callee_id` (`callee_id`),
    KEY `idx_status` (`status`),
    KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通话记录表'; 