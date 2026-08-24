// SPDX-FileCopyrightText: 2026 RainxButterfly 
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.common;

/**
 * 业务异常：在 Service 层抛出，用于携带业务状态码。
 *
 * @author StarLeaf-Roxy
 * @since 2026-08-24
 */
public class BizException extends RuntimeException {

    private final int code;
    private final ErrorCode errorCode;

    public BizException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.code = errorCode.getCode();
    }

    public BizException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.code = errorCode.getCode();
    }

    public int getCode() {
        return code;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}