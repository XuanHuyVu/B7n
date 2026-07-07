package com.tlu.tsms.logging.core.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tlu.tsms.common.ESystemLogLevel;
import com.tlu.tsms.logging.AuditLog;
import com.tlu.tsms.logging.ManagerIf;
import com.tlu.tsms.logging.core.helper.ActorInfo;
import com.tlu.tsms.logging.core.helper.ActorResolver;
import com.tlu.tsms.logging.core.helper.RequestInfo;
import com.tlu.tsms.logging.core.helper.RequestInfoResolver;
import com.tlu.tsms.entity.SystemLogEntity;
import com.tlu.tsms.service.SystemLogService;
import com.tlu.tsms.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.lang.reflect.Method;

import static com.tlu.tsms.common.Constant.*;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final SystemLogService systemLogService;
    private final ActorResolver actorResolver;
    private final RequestInfoResolver requestInfoResolver;

    private final ExpressionParser spel = new SpelExpressionParser();
    private final DefaultParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private static final Set<String> SENSITIVE_KEYS = Set.of(
            "password", "pass", "pwd",
            "token", "accessToken", "refreshToken",
            "authorization", "secret", "otp", "pin", "apikey", "apiKey"
    );

    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint proceedingJoinPoint, AuditLog auditLog) throws Throwable {
        long start = System.nanoTime();
        Object result = null;
        Throwable error = null;
        try {
            result = proceedingJoinPoint.proceed();
            return result;
        } catch (Throwable ex) {
            error = ex;
            throw ex;
        } finally {
            long durationMs = (System.nanoTime() - start) / 1_000_000;
            safeWriteLog(proceedingJoinPoint, auditLog, result, error, durationMs);
        }
    }

    private void safeWriteLog(ProceedingJoinPoint pjp, AuditLog auditLog, Object result, Throwable error, long durationMs) {
        try {
            Method method = extractMethod(pjp).orElse(null);
            Object[] args = Optional.ofNullable(pjp.getArgs()).orElseGet(() -> new Object[0]);
            ActorInfo actor = actorResolver.resolve();
            RequestInfo req = requestInfoResolver.resolve();
            StandardEvaluationContext context = buildContext(method, args, result, error, actor, req);
            ManagerIf managerIf = (Objects.isNull(method)) ? null : method.getAnnotation(ManagerIf.class);
            if (Objects.nonNull(managerIf)) {
                String condition = extractManageIfCondition(managerIf);
                if (!Strings.isBlank(condition)) {
                    Boolean ok = spelValue(context, condition, Boolean.class).orElse(Boolean.FALSE);
                    if (!ok) return;
                }
            }

            Long resourceId = evalLong(context, auditLog.resourceId());
            String message = evalString(context, auditLog.message());
            if (Strings.isBlank(message)) message = auditLog.module() + " " + auditLog.action();
            SystemLogEntity e = new SystemLogEntity();
            e.setTimestamp(DateUtils.now());
            e.setModule(auditLog.module());
            e.setAction(auditLog.action());
            e.setResourceType(auditLog.resourceType());
            e.setResourceId(resourceId);

            if (Objects.nonNull(actor)) {
                e.setActorName(actor.getName());
            }

            if (Objects.nonNull(req)) {
                e.setIp(req.getIp());
                e.setTraceId(req.getTraceId());
            }

            if (Objects.isNull(error)) {
                e.setLevel(ESystemLogLevel.INFO);
                e.setMessage(message);
                e.setDetailJson(safeDetailJson(method, args, result, null, actor, req, durationMs));
            } else {
                e.setLevel(ESystemLogLevel.ERROR);
                e.setMessage(message + " | error=" + error.getClass().getSimpleName());
                e.setDetailJson(safeDetailJson(method, args, result, error, actor, req, durationMs));
            }

            systemLogService.write(e);
        } catch (Exception ignore) {
        }
    }

    private Optional<Method> extractMethod(ProceedingJoinPoint pjp) {
        try {
            if (!(pjp.getSignature() instanceof MethodSignature ms)) return Optional.empty();
            return Optional.ofNullable(ms.getMethod());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private StandardEvaluationContext buildContext(Method method, Object[] args, Object result, Throwable error, ActorInfo actor, RequestInfo req) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        if (Objects.nonNull(method)) {
            String[] names = nameDiscoverer.getParameterNames(method);
            if (Objects.nonNull(names)) {
                int len = Math.min(names.length, args.length);
                for (int i = 0; i < len; i++) {
                    context.setVariable(names[i], args[i]);
                }
            }
        }

        for (int i = 0; i < args.length; i++)
            context.setVariable("p" + i, args[i]);
        context.setVariable(RESULT, result);
        context.setVariable(ERROR, error);
        context.setVariable(ACTOR, actor);
        context.setVariable(REQUEST, req);
        return context;
    }

    private String extractManageIfCondition(ManagerIf manageIf) {
        if (!manageIf.enabled()) return FALSE;
        String expr = manageIf.value();
        if (Strings.isBlank(expr)) expr = TRUE;
        return switch (manageIf.when()) {
            case ALWAYS -> expr;
            case SUCCESS -> "(#error == null) && (" + expr + ")";
            case ERROR -> "(#error != null) && (" + expr + ")";
        };
    }

    private String safeDetailJson(
            Method method,
            Object[] args,
            Object result,
            Throwable error,
            ActorInfo actor,
            RequestInfo req,
            long durationMs
    ) {
        try {
            Map<String, Object> detail = new LinkedHashMap<>();
            detail.put("durationMs", durationMs);
            Map<String, Object> invocation = new LinkedHashMap<>();
            invocation.put("method", Objects.nonNull(method)
                    ? method.getDeclaringClass().getSimpleName() + "." + method.getName()
                    : null);
            invocation.put("argsCount", Objects.nonNull(args) ? args.length : 0);
            invocation.put("args", safeArgsMap(method, args));
            invocation.put("resultType", Objects.nonNull(result) ? result.getClass().getName() : null);
            detail.put("invocation", invocation);

            if (Objects.nonNull(actor)) {
                Map<String, Object> a = new LinkedHashMap<>();
                a.put("name", actor.getName());
                detail.put("actor", a);
            }

            if (Objects.nonNull(req)) {
                Map<String, Object> http = new LinkedHashMap<>();
                http.put("ip", req.getIp());
                http.put("traceId", req.getTraceId());
                detail.put("http", http);
            }

            if (Objects.nonNull(error)) {
                Map<String, Object> err = new LinkedHashMap<>();
                err.put("type", error.getClass().getName());
                err.put("message", Objects.toString(error.getMessage(), null));
                Throwable root = rootCause(error);
                if (root != null && root != error) {
                    err.put("rootCauseType", root.getClass().getName());
                    err.put("rootCauseMessage", Objects.toString(root.getMessage(), null));
                }

                err.put("stack", stackTrace(error));
                detail.put("error", err);
            }

            return objectMapper.writeValueAsString(detail);
        } catch (Exception ex) {
            return null;
        }
    }

    private String evalString(StandardEvaluationContext context, String expr) {
        if (Strings.isBlank(expr)) return null;
        try {
            if (!expr.contains("#") && !expr.contains("T(")) return expr;
            return spelValue(context, expr, String.class).orElse(expr);
        } catch (Exception e) {
            return expr;
        }
    }

    private Long evalLong(StandardEvaluationContext context, String expr) {
        if (Strings.isBlank(expr)) return null;
        try {
            Object v = spelValue(context, expr, Object.class).orElse(null);
            if (Objects.isNull(v)) return null;
            if (v instanceof Number n) return n.longValue();
            String s = Objects.toString(v, "").trim();
            return s.isBlank() ? null : Long.parseLong(s);
        } catch (Exception e) {
            return null;
        }
    }

    private <T> Optional<T> spelValue(StandardEvaluationContext context, String expr, Class<T> type) {
        try {
            return Optional.ofNullable(spel.parseExpression(expr).getValue(context, type));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    private Map<String, Object> safeArgsMap(Method method, Object[] args) {
        Map<String, Object> out = new LinkedHashMap<>();
        if (args == null || args.length == 0) return out;

        String[] names = null;
        try {
            if (method != null) names = nameDiscoverer.getParameterNames(method);
        } catch (Exception ignore) {}

        for (int i = 0; i < args.length; i++) {
            String key = (names != null && i < names.length && names[i] != null && !names[i].isBlank())
                    ? names[i]
                    : ("p" + i);

            if (isSensitiveKey(key)) {
                out.put(key, "***");
            } else {
                out.put(key, sanitizeValue(args[i], 0));
            }
        }
        return out;
    }

    private Object sanitizeValue(Object v, int depth) {
        if (v == null) return null;
        if (depth > MAX_DEPTH) return "…";
        if (v instanceof CharSequence cs) return truncate(cs.toString(), MAX_STR_LEN);
        if (v instanceof Number || v instanceof Boolean || v instanceof Enum<?>) return v;
        if (v instanceof UUID) return v.toString();
        if (v instanceof Date) return v.toString();
        if (v instanceof TemporalAccessor) return v.toString();
        if (v instanceof byte[] b) return "byte[length=" + b.length + "]";
        if (v.getClass().isArray()) {
            int len = Array.getLength(v);
            List<Object> arr = new ArrayList<>();
            int take = Math.min(len, MAX_COLLECTION_ITEMS);
            for (int i = 0; i < take; i++) arr.add(sanitizeValue(Array.get(v, i), depth + 1));
            if (len > take) arr.add("…(+" + (len - take) + ")");
            return arr;
        }

        String cn = v.getClass().getName();
        if (cn.startsWith("java.io.") || cn.contains("InputStream") || cn.contains("OutputStream")) return cn;
        if (cn.contains("MultipartFile")) return cn;
        if (v instanceof Map<?, ?> m) {
            Map<String, Object> mm = new LinkedHashMap<>();
            int count = 0;
            for (Map.Entry<?, ?> e : m.entrySet()) {
                if (count++ >= MAX_MAP_ENTRIES) {
                    mm.put("…", "(truncated)");
                    break;
                }
                String key = Objects.toString(e.getKey(), "");
                if (isSensitiveKey(key)) {
                    mm.put(key, "***");
                } else {
                    mm.put(key, sanitizeValue(e.getValue(), depth + 1));
                }
            }
            return mm;
        }

        if (v instanceof Iterable<?> it) {
            List<Object> list = new ArrayList<>();
            int count = 0;
            for (Object x : it) {
                if (count++ >= MAX_COLLECTION_ITEMS) {
                    list.add("…(truncated)");
                    break;
                }
                list.add(sanitizeValue(x, depth + 1));
            }
            return list;
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> asMap = objectMapper.convertValue(v, Map.class);
            return sanitizeValue(asMap, depth + 1);
        } catch (Exception ignore) {
            return truncate(Objects.toString(v), MAX_STR_LEN);
        }
    }

    private boolean isSensitiveKey(String key) {
        if (key == null) return false;
        String k = key.trim();
        if (k.isEmpty()) return false;
        String low = k.toLowerCase(Locale.ROOT);
        for (String s : SENSITIVE_KEYS) {
            if (low.contains(s.toLowerCase(Locale.ROOT))) return true;
        }
        return false;
    }

    private String truncate(String s, int max) {
        if (s == null) return null;
        if (s.length() <= max) return s;
        return s.substring(0, max) + "...(truncated)";
    }

    private Throwable rootCause(Throwable t) {
        if (t == null) return null;
        Throwable cur = t;
        int guard = 0;
        while (cur.getCause() != null && cur.getCause() != cur && guard++ < 10) {
            cur = cur.getCause();
        }
        return cur;
    }

    private String stackTrace(Throwable t) {
        if (t == null) return null;
        try {
            StringWriter sw = new StringWriter();
            t.printStackTrace(new PrintWriter(sw));
            return truncate(sw.toString(), 8000);
        } catch (Exception ex) {
            return null;
        }
    }
}
