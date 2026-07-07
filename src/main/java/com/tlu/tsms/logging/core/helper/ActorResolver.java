package com.tlu.tsms.logging.core.helper;

import com.tlu.tsms.logging.BaseLogging;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.tlu.tsms.common.Constant.ANONYMOUS_USER;

@Component
@RequiredArgsConstructor
public class ActorResolver {

    private final BaseLogging logging;

    public ActorInfo resolve() {
        return logging.getAdCode()
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .filter(s -> !ANONYMOUS_USER.equalsIgnoreCase(s))
                .map(adCode -> ActorInfo.builder()
                        .name(adCode)
                        .build())
                .orElse(null);
    }
}
