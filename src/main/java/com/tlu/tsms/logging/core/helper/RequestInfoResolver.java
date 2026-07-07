package com.tlu.tsms.logging.core.helper;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;
import java.util.Optional;

import static com.tlu.tsms.common.Constant.*;

@Component
public class RequestInfoResolver {

    public RequestInfo resolve() {
        HttpServletRequest req = currentRequest().orElse(null);
        return RequestInfo.builder()
                .ip(Objects.nonNull(req) ? getClientIp(req) : null)
                .traceId(MDC.get(MDC_TRACE_ID))
                .build();
    }

    private Optional<HttpServletRequest> currentRequest() {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .filter(ServletRequestAttributes.class::isInstance)
                .map(ServletRequestAttributes.class::cast)
                .map(ServletRequestAttributes::getRequest);
    }

    private String getClientIp(HttpServletRequest req) {
        if (Strings.isNotBlank(req.getHeader(X_FORWARDED_FOR)))
            return req.getHeader(X_FORWARDED_FOR)
                    .split(COMMA)[0]
                    .trim();
        if (Strings.isNotBlank(req.getHeader(X_REAL_IP)))
            return req.getHeader(X_REAL_IP).trim();
        return req.getRemoteAddr();
    }
}

