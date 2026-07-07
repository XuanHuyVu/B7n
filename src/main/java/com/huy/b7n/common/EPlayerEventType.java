package com.huy.b7n.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum EPlayerEventType {
    JOINED("Tham gia"),
    LEFT("Rời buổi chơi"),
    TEMP_PAUSED("Tạm nghỉ"),
    RESUMED("Tiếp tục chơi"),
    INJURED("Chấn thương"),
    SUBSTITUTED_IN("Vào thay"),
    SUBSTITUTED_OUT("Bị thay ra"),
    MANUAL_CHANGED("Điều chỉnh thủ công");

    private final String type;

    public static EPlayerEventType lookup(String type) {
        return Strings.isBlank(type) ? null : Arrays.stream(EPlayerEventType.values())
                .filter(s -> type.equals(s.getType()))
                .findFirst()
                .orElse(null);
    }
}