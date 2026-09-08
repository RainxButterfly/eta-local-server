package cn.eta.team.eta.common;

/**
 * 业务状态码，与 <仓库根目录>/状态码含义.txt 保持一致。
 */
public enum ErrorCode {

    /* ---------- 通用 ---------- */
    OK(200, "成功"),
    BAD_REQUEST(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或令牌已过期"),
    FORBIDDEN(403, "无权限访问该资源"),
    NOT_FOUND(404, "资源不存在"),
    TOO_MANY_REQUESTS(429, "请求过于频繁"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    /* ---------- 认证 10000-10099 ---------- */
    EMAIL_ALREADY_EXISTS(10001, "邮箱已被注册"),
    EMAIL_OR_PASSWORD_ERROR(10002, "邮箱或密码错误"),
    ACCOUNT_DISABLED(10003, "账号已被禁用"),
    TOKEN_EXPIRED(10004, "令牌已过期，请重新登录"),
    VERIFY_CODE_ERROR(10005, "验证码错误或已过期"),

    /* ---------- 任务管理 20000-20099 ---------- */
    TASK_NOT_FOUND(20001, "任务不存在"),
    TASK_TITLE_EMPTY(20002, "任务标题为空"),
    CATEGORY_NOT_FOUND(20003, "任务分类不存在"),
    ILLEGAL_TASK_STATUS(20004, "非法任务状态"),
    CATEGORY_NAME_EXISTS(20005, "分类名已存在"),

    /* ---------- 简历制作 30000-30099 ---------- */
    RESUME_NOT_FOUND(30001, "简历不存在"),
    RESUME_TEMPLATE_NOT_FOUND(30002, "简历模板不存在"),
    RESUME_EXPORT_FAILED(30003, "简历导出失败"),

    /* ---------- 错题本 40000-40099 ---------- */
    ERROR_NOT_FOUND(40001, "错题不存在"),
    ERROR_QUESTION_EMPTY(40002, "题目内容为空"),
    ERROR_SUBJECT_NOT_FOUND(40003, "学科分类不存在"),

    /* ---------- 笔记 50000-50099 ---------- */
    NOTE_NOT_FOUND(50001, "笔记不存在"),
    NOTE_TAG_NOT_FOUND(50002, "标签不存在"),
    NOTE_TAG_NAME_EXISTS(50003, "标签名已存在"),

    /* ---------- 搜索 60000-60099 ---------- */
    SEARCH_KEYWORD_EMPTY(60001, "搜索关键词为空"),

    /* ---------- 控制台·统计 70000-70099 ---------- */
    STATS_RANGE_INVALID(70001, "统计时间范围非法"),

    /* ---------- OCR 130000-130099 ---------- */
    OCR_FAILED(130001, "OCR 识别失败"),
    OCR_IMAGE_TYPE_UNSUPPORTED(130002, "图片格式不支持"),
    OCR_IMAGE_EMPTY(130003, "图片为空");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}