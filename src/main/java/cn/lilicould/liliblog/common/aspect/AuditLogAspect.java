package cn.lilicould.liliblog.common.aspect;

import cn.lilicould.liliblog.common.annotation.Audit;
import cn.lilicould.liliblog.common.context.BaseContext;
import cn.lilicould.liliblog.common.util.IpUtil;
import cn.lilicould.liliblog.mapper.AuditLogMapper;
import cn.lilicould.liliblog.model.entity.AuditLog;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private final AuditLogMapper auditLogMapper;
    private final ExpressionParser parser = new SpelExpressionParser();
    private final IpUtil ipUtil;

    @Around("@annotation(audit)")
    public Object recordAuditLog(ProceedingJoinPoint joinPoint, Audit audit) throws Throwable {
        long startTime = System.currentTimeMillis();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Object[] args = joinPoint.getArgs();
        String[] paramNames = signature.getParameterNames(); // 获取参数名称

        AuditLog auditLog = new AuditLog();
        auditLog.setCreateBy(BaseContext.getCurrentUserId());
        auditLog.setUsername(BaseContext.getCurrentUserName());

        // 解析 SpEL 表达式
        EvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < paramNames.length; i++) {
            context.setVariable(paramNames[i], args[i]);
        }

        auditLog.setModule(parseSpel(audit.module(), context));
        auditLog.setOperation(parseSpel(audit.operation(), context));
        auditLog.setDescription(parseSpel(audit.description(), context));
        auditLog.setTargetType(parseSpel(audit.targetType(), context));

        try {
            Object result = joinPoint.proceed();

            // 将返回值加入上下文，支持 #result
            context.setVariable("result", result);

            long executionTime = System.currentTimeMillis() - startTime;
            auditLog.setExecutionTime((int) executionTime);
            auditLog.setStatus(1);

            // 如果 target 是 SpEL，再次解析
            if (!audit.target().isEmpty()) {
                String targetIdStr = parseSpel(audit.target(), context);
                auditLog.setTarget(targetIdStr);
            }

            saveAuditLog(auditLog);

            return result;
        } catch (Throwable e) {
            long executionTime = System.currentTimeMillis() - startTime;
            auditLog.setExecutionTime((int) executionTime);
            auditLog.setStatus(0);
            auditLog.setErrorMessage(e.getMessage());

            saveAuditLog(auditLog);

            throw e;
        }
    }


    /**
     * 解析表达式
     * @param expression 表达式
     * @param context 上下文
     * @return 解析结果
     */
    private String parseSpel(String expression, EvaluationContext context) {
        if (expression == null || expression.isEmpty()) {
            return "";
        }

        // 如果不是 SpEL 表达式，直接返回
        if (!expression.contains("#")) {
            return expression;
        }

        try {
            return parser.parseExpression(expression).getValue(context, String.class);
        } catch (Exception e) {
            log.warn("SpEL 解析失败: {}, 错误: {}", expression, e.getMessage());
            return expression;
        }
    }

    /**
     * 解析 SpEL 表达式
     * @param value 待解析的表达式
     * @return 解析后的结果
     */
    private Long parseLong(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 保存审计日志
     * @param auditLog 审计日志
     */
    private void saveAuditLog(AuditLog auditLog) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                auditLog.setRequestMethod(request.getMethod());
                auditLog.setRequestUri(request.getRequestURI());
                auditLog.setIpAddress(ipUtil.getIpAddress(request));
                auditLog.setUserAgent(ipUtil.getUserAgent(request));
            }

            auditLogMapper.insert(auditLog);
        } catch (Exception e) {
            log.error("保存审计日志失败", e);
        }
    }
}
