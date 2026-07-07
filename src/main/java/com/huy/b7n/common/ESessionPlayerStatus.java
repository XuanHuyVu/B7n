package com.huy.b7n.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum ESessionPlayerStatus {
    AVAILABLE("Sẵn sàng"),
    PLAYING("Đang chơi"),
    RESTING("Đang nghỉ"),
    TEMP_PAUSED("Tạm dừng"),
    INJURED("Chấn thương"),
    LEFT("Đã rời buổi chơi"),
    UNAVAILABLE("Không khả dụng");

    private final String status;

    public static ESessionPlayerStatus lookup(String status) {
        return Strings.isBlank(status) ? null : Arrays.stream(ESessionPlayerStatus.values())
                .filter(s -> status.equals(s.getStatus()))
                .findFirst()
                .orElse(null);
    }
}