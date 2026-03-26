package com.edu.order.mq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单支付成功消息事件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPaidEvent implements Serializable {

    /** 订单ID */
    private Long orderId;
    /** 订单号 */
    private String orderNo;
    /** 用户ID */
    private Long userId;
    /** 用户名 */
    private String username;
    /** 课程ID */
    private Long courseId;
    /** 课程标题 */
    private String courseTitle;
    /** 支付金额 */
    private BigDecimal amount;
    /** 支付时间 */
    private LocalDateTime paidAt;
}
