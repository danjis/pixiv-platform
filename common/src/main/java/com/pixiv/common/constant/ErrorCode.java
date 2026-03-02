package com.pixiv.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 错误码枚举
 * 定义系统中所有的错误码和错误消息
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    // ========== 通用错误码 (1000-1999) ==========
    SUCCESS(200, "操作成功"),
    SYSTEM_ERROR(1000, "系统错误"),
    PARAM_ERROR(1001, "参数错误"),
    PARAM_MISSING(1002, "缺少必要参数"),
    PARAM_INVALID(1003, "参数格式不正确"),
    REQUEST_METHOD_NOT_SUPPORTED(1004, "请求方法不支持"),
    MEDIA_TYPE_NOT_SUPPORTED(1005, "媒体类型不支持"),
    
    // ========== 认证授权错误码 (2000-2999) ==========
    UNAUTHORIZED(2000, "未登录或登录已过期"),
    TOKEN_INVALID(2001, "令牌无效"),
    TOKEN_EXPIRED(2002, "令牌已过期"),
    FORBIDDEN(2003, "没有访问权限"),
    USERNAME_OR_PASSWORD_ERROR(2004, "用户名或密码错误"),
    ACCOUNT_DISABLED(2005, "账户已被禁用"),
    ACCOUNT_LOCKED(2006, "账户已被锁定"),
    
    // ========== 用户相关错误码 (3000-3999) ==========
    USER_NOT_FOUND(3000, "用户不存在"),
    USERNAME_EXISTS(3001, "用户名已存在"),
    EMAIL_EXISTS(3002, "邮箱已被注册"),
    USER_NOT_ARTIST(3003, "用户不是画师"),
    ARTIST_APPLICATION_EXISTS(3004, "已有待审核的画师申请"),
    ARTIST_APPLICATION_NOT_FOUND(3005, "画师申请不存在"),
    ALREADY_FOLLOWING(3006, "已经关注该画师"),
    NOT_FOLLOWING(3007, "未关注该画师"),
    CANNOT_FOLLOW_SELF(3008, "不能关注自己"),
    
    // ========== 作品相关错误码 (4000-4999) ==========
    ARTWORK_NOT_FOUND(4000, "作品不存在"),
    ARTWORK_DELETED(4001, "作品已被删除"),
    NOT_ARTWORK_OWNER(4002, "不是作品所有者"),
    ALREADY_LIKED(4003, "已经点赞过该作品"),
    NOT_LIKED(4004, "未点赞该作品"),
    ALREADY_FAVORITED(4005, "已经收藏过该作品"),
    NOT_FAVORITED(4006, "未收藏该作品"),
    COMMENT_NOT_FOUND(4007, "评价不存在"),
    NOT_COMMENT_OWNER(4008, "不是评价所有者"),
    TAG_NOT_FOUND(4009, "标签不存在"),
    
    // ========== 约稿相关错误码 (5000-5999) ==========
    COMMISSION_NOT_FOUND(5000, "约稿订单不存在"),
    NOT_COMMISSION_PARTICIPANT(5001, "不是约稿参与者"),
    COMMISSION_STATUS_ERROR(5002, "约稿状态错误"),
    ARTIST_NOT_ACCEPTING_COMMISSIONS(5003, "画师暂不接受约稿"),
    CANNOT_COMMISSION_SELF(5004, "不能向自己发起约稿"),
    PROJECT_NOT_FOUND(5005, "企划不存在"),
    PROJECT_ENDED(5006, "企划已结束"),
    NOT_PROJECT_OWNER(5007, "不是企划所有者"),
    ALREADY_SUBMITTED_TO_PROJECT(5008, "已经参与过该企划"),
    
    // ========== 文件相关错误码 (6000-6999) ==========
    FILE_UPLOAD_ERROR(6000, "文件上传失败"),
    FILE_TYPE_NOT_SUPPORTED(6001, "文件类型不支持"),
    FILE_SIZE_EXCEEDED(6002, "文件大小超过限制"),
    FILE_NOT_FOUND(6003, "文件不存在"),
    IMAGE_PROCESS_ERROR(6004, "图片处理失败"),
    
    // ========== 通知相关错误码 (7000-7999) ==========
    NOTIFICATION_NOT_FOUND(7000, "通知不存在"),
    NOT_NOTIFICATION_OWNER(7001, "不是通知所有者"),
    
    // ========== 管理员相关错误码 (8000-8999) ==========
    ADMIN_NOT_FOUND(8000, "管理员不存在"),
    ADMIN_USERNAME_EXISTS(8001, "管理员用户名已存在"),
    NOT_ADMIN(8002, "不是管理员"),
    
    // ========== 业务逻辑错误码 (9000-9999) ==========
    RESOURCE_NOT_FOUND(9000, "资源不存在"),
    OPERATION_NOT_ALLOWED(9001, "操作不允许"),
    DUPLICATE_OPERATION(9002, "重复操作"),
    RATE_LIMIT_EXCEEDED(9003, "请求过于频繁"),
    SERVICE_UNAVAILABLE(9004, "服务暂时不可用"),
    EXTERNAL_SERVICE_ERROR(9005, "外部服务调用失败");

    /**
     * 错误码
     */
    private final Integer code;

    /**
     * 错误消息
     */
    private final String message;
}
