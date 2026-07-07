package com.tlu.tsms.jwt;

import com.tlu.tsms.common.Constant;
import com.tlu.tsms.exception.EStatusCode;
import com.tlu.tsms.response.ResponseDto;
import com.tlu.tsms.utils.MapperUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull AuthenticationException authException
    ) throws IOException {
        if (response.isCommitted()) return;

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(Constant.APPLICATION_JSON);
        ResponseDto<?> body = ResponseDto.error(EStatusCode.UNAUTHORIZED, authException.getMessage());
        ObjectNode jsonBody = MapperUtils.convertValue(body, ObjectNode.class);
        response.getWriter().write(Objects.requireNonNull(jsonBody).toString());
    }
}