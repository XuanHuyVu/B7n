package com.huy.b7n.converter;

import com.huy.b7n.common.EPlayerEventType;
import com.huy.b7n.exception.EStatusCode;
import com.huy.b7n.utils.ErrorUtils;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.apache.logging.log4j.util.Strings;

import java.util.Optional;

@Converter
public class EPlayerEventTypeConverter implements AttributeConverter<EPlayerEventType, String> {

    @Override
    public String convertToDatabaseColumn(EPlayerEventType ePlayerEventType) {
        return Optional.ofNullable(ePlayerEventType)
                .map(EPlayerEventType::getType)
                .orElse(null);
    }

    @Override
    public EPlayerEventType convertToEntityAttribute(String source) {
        try {
            if (Strings.isBlank(source)) return null;
            return EPlayerEventType.lookup(source);
        } catch (IllegalArgumentException ex) {
            throw ErrorUtils.exception(EStatusCode.SOURCE_NOT_FOUND, "EPlayerEventType");
        }
    }
}