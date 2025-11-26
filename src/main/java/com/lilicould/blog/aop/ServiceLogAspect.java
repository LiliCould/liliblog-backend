package com.lilicould.blog.aop;

import com.lilicould.blog.annotation.Log;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Service层日志切面 - 专注处理Service层的方法日志
 */
@Component
@Aspect
public class ServiceLogAspect {

    private static final Logger logger = LoggerFactory.getLogger(ServiceLogAspect.class);

    /**
     * 定义切入点 - 所有Service的方法（排除Controller）
     */
    @Pointcut("execution(* com.lilicould.blog.service..*.*(..))")
    public void servicePointcut() {}

    /**
     * 定义切入点 - Service层有@Log注解的方法
     */
    @Pointcut("@annotation(com.lilicould.blog.annotation.Log)")
    public void logAnnotationPointcut() {}

    /**
     * 环绕通知 - 处理Service层通用日志
     * 匹配没有@Log注解的Service方法
     */
    @Around("servicePointcut() && !logAnnotationPointcut()")
    public Object logService(ProceedingJoinPoint joinPoint) throws Throwable {
        return logBasicServiceExecution(joinPoint);
    }

    /**
     * 环绕通知 - 处理有@Log注解的Service方法
     * 提供更详细的日志记录
     */
    @Around("logAnnotationPointcut()")
    public Object logAnnotatedService(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取目标方法
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // 获取目标对象的类
        Class<?> targetClass = joinPoint.getTarget().getClass();

        // 尝试从目标类中获取方法（处理代理情况）
        Method targetMethod = targetClass.getMethod(method.getName(), method.getParameterTypes());

        // 获取@Log注解
        Log logAnnotation = targetMethod.getAnnotation(Log.class);

        // 如果还是null，尝试从接口中获取
        if (logAnnotation == null) {
            Class<?>[] interfaces = targetClass.getInterfaces();
            for (Class<?> iface : interfaces) {
                try {
                    Method interfaceMethod = iface.getMethod(method.getName(), method.getParameterTypes());
                    logAnnotation = interfaceMethod.getAnnotation(Log.class);
                    if (logAnnotation != null) {
                        break;
                    }
                } catch (NoSuchMethodException e) {
                    logger.error("无法获取方法：{}", e.getMessage());
                }
            }
        }
        return logServiceWithAnnotation(joinPoint, logAnnotation);
    }


    /**
     * Service层基础日志记录
     */
    private Object logBasicServiceExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        // 记录方法开始
        logger.debug("[Service] 开始执行 - {}类的{}方法", className, methodName);

        Object result = null;
        try {
            result = joinPoint.proceed();

            long executionTime = System.currentTimeMillis() - startTime;

            logger.info("[Service] 执行完成 - {}类的{}方法，耗时：{}ms",
                    className, methodName, executionTime);
            return result;

        } catch (Throwable throwable) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("[Service] 执行异常 - {}类的{}方法，耗时：{}ms，异常信息：{}",
                    className, methodName, executionTime, throwable.getMessage(), throwable);
            throw throwable;
        }
    }

    /**
     * 使用@Log注解的Service方法详细日志记录
     */
    private Object logServiceWithAnnotation(ProceedingJoinPoint joinPoint, Log logAnnotation) throws Throwable {
        if (logAnnotation == null) {
            return logBasicServiceExecution(joinPoint); // 默认使用基础日志记录
        }

        // 若忽略，则跳过日志记录
        if (logAnnotation.ignore()) {
            return joinPoint.proceed(); // 跳过日志记录
        }

        long startTime = System.currentTimeMillis();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String operation = logAnnotation.value().isEmpty() ? methodName : logAnnotation.value();

        // 记录方法开始
        logger.info("[Service] 开始执行 - {} ({}类的{}方法)", operation, className, methodName);

        // 记录方法参数
        if (logAnnotation.logParams()) {
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                logger.debug("[Service] 方法参数 - {}: {}", operation, Arrays.toString(args));
            }
        }

        Object result;
        try {
            result = joinPoint.proceed();

            // 记录返回结果
            if (logAnnotation.logResult() && result != null) {
                logger.debug("[Service] 返回结果 - {}: {}", operation, result);
            }

            long executionTime = System.currentTimeMillis() - startTime;

            // 记录执行时间
            if (logAnnotation.logTime()) {
                if (executionTime > 1000) {
                    logger.warn("[Service] 执行完成 - {}，耗时：{}ms (性能警告)", operation, executionTime);
                } else {
                    logger.info("[Service] 执行完成 - {}，耗时：{}ms", operation, executionTime);
                }
            }

            return result;

        } catch (Throwable throwable) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("[Service] 执行异常 - {}，耗时：{}ms，异常信息：{}",
                    operation, executionTime, throwable.getMessage(), throwable);
            throw throwable;
        }
    }
}