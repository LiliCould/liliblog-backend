package com.lilicould.blog.annotation;

import java.lang.annotation.*;

/**
 * 日志注解 - 用于标记需要记录日志的方法
 */
@Target(ElementType.METHOD) // 作用于方法上
@Retention(RetentionPolicy.RUNTIME) // 运行时生效
@Documented // 生成文档
public @interface Log {
    String value() default "";          // 操作描述
    boolean logParams() default true;   // 是否记录参数
    boolean logResult() default false;  // 默认不记录结果（可能包含敏感信息）
    boolean logTime() default true;     // 是否记录执行时间
    boolean ignore() default false;     // 是否忽略此日志
}
