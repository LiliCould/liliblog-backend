package cn.lilicould.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Audit {
    String module() default "";

    String operation() default "";

    String description() default "";

    String targetType() default "";

    String target() default "";
}
