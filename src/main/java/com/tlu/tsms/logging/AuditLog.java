package com.tlu.tsms.logging;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditLog {
    String module();
    String action();
    String resourceType() default "";
    String resourceId() default "";
    String message() default "";
}
