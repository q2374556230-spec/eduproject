package com.edu.common.core.result;

import lombok.Getter;

/**
 * 统一响应状态码枚举
 */
@Getter
public enum ResultCode {

    // 通用
    SUCCESS(200, "操作成功"),
    FAIL(500, "操作失败"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未认证，请先登录"),
    FORBIDDEN(403, "权限不足"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    TOO_MANY_REQUESTS(429, "请求过于频繁，请稍后重试"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // 用户模块 (1000-1099)
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_ALREADY_EXISTS(1002, "用户已存在"),
    USER_PASSWORD_ERROR(1003, "密码错误"),
    USER_DISABLED(1004, "账户已被禁用"),
    USER_TOKEN_EXPIRED(1005, "Token已过期，请重新登录"),
    USER_TOKEN_INVALID(1006, "Token无效"),

    // 课程模块 (1100-1199)
    COURSE_NOT_FOUND(1101, "课程不存在"),
    COURSE_ALREADY_EXISTS(1102, "课程已存在"),
    CATEGORY_NOT_FOUND(1103, "课程分类不存在"),

    // 订单模块 (1200-1299)
    ORDER_NOT_FOUND(1201, "订单不存在"),
    ORDER_ALREADY_EXISTS(1202, "已购买该课程"),
    ORDER_STATUS_ERROR(1203, "订单状态异常"),
    ORDER_PAY_FAIL(1204, "支付失败");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
