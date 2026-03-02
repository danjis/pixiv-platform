package com.pixiv.common.exception;

import com.pixiv.common.constant.ErrorCode;

/**
 * 资源未找到异常类
 * 用于处理资源不存在的情况（如用户、作品、订单等）
 */
public class ResourceNotFoundException extends BusinessException {

    private static final long serialVersionUID = 1L;

    /**
     * 构造函数 - 使用默认错误码
     */
    public ResourceNotFoundException(String message) {
        super(ErrorCode.RESOURCE_NOT_FOUND, message);
    }

    /**
     * 构造函数 - 使用自定义错误码
     */
    public ResourceNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    /**
     * 构造函数 - 使用自定义错误码和消息
     */
    public ResourceNotFoundException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    /**
     * 构造函数 - 使用默认错误码和原因异常
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(ErrorCode.RESOURCE_NOT_FOUND, message, cause);
    }
}
