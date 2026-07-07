package com.tlu.tsms.logging;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface ManagerIf {

    /**
     * Bật/tắt nhanh annotation mà không cần xoá.
     */
    boolean enabled() default true;

    /**
     * Điều kiện SpEL. Mặc định "true" nghĩa là luôn cho phép log (nếu enabled).
     * Bạn có thể dùng các biến trong AuditAspect:
     *  - #result, #error, #actor, #req, và các params của method (theo tên param)
     */
    String value() default "true";

    /**
     * Khi nào mới log:
     *  - ALWAYS: luôn xét theo value()
     *  - SUCCESS: chỉ log khi #error == null
     *  - ERROR: chỉ log khi #error != null
     */
    When when() default When.ALWAYS;

    enum When {
        ALWAYS,
        SUCCESS,
        ERROR
    }
}
