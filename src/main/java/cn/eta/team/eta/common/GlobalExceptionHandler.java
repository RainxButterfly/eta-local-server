// SPDX-FileCopyrightText: 2026 RainxButterfly 
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.common;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局异常处理：将各类异常统一转换为 { code, message, data } 响应。
 *
 * @author StarLeaf-Roxy
 * @since 2026-08-24
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 业务异常（返回的业务 code，HTTP 统一 200？不，业务错误用 200 + 非 200 code 更贴近现有约定，这里返回 200 由 body.code 表达） */
    @ExceptionHandler(BizException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handleBiz(BizException e) {
        return Result.fail(e.getCode(), e.getMessage());
    }

    /** 参数校验失败（@RequestBody 上的 @Valid） */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValid(MethodArgumentNotValidException e) {
        FieldError fe = e.getBindingResult().getFieldError();
        String msg = fe == null ? "参数错误" : fe.getField() + " " + fe.getDefaultMessage();
        return Result.fail(ErrorCode.BAD_REQUEST.getCode(), msg);
    }

    /** 表单/参数绑定失败 */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBind(BindException e) {
        FieldError fe = e.getBindingResult().getFieldError();
        String msg = fe == null ? "参数错误" : fe.getField() + " " + fe.getDefaultMessage();
        return Result.fail(ErrorCode.BAD_REQUEST.getCode(), msg);
    }

    /** 单参数校验失败 */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleConstraint(ConstraintViolationException e) {
        return Result.fail(ErrorCode.BAD_REQUEST.getCode(), e.getMessage());
    }

    /** 参数类型不匹配，例如路径变量传了字符串 */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return Result.fail(ErrorCode.BAD_REQUEST.getCode(), "参数类型不正确：" + e.getName());
    }

    /** 未认证（由 Security 抛出，通常是过滤器内） */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleAuth(AuthenticationException e) {
        return Result.fail(ErrorCode.UNAUTHORIZED.getCode(), e.getMessage());
    }

    /** 无权限 */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleDenied(AccessDeniedException e) {
        return Result.fail(ErrorCode.FORBIDDEN);
    }

    /** 请求的资源不存在 */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleNoResource(NoResourceFoundException e) {
        return Result.fail(ErrorCode.NOT_FOUND);
    }

    /** 兜底异常 */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleOther(Exception e) {
        log.error("未处理异常", e);
        return Result.fail(ErrorCode.INTERNAL_ERROR);
    }
}