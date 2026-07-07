package com.tlu.tsms.converter;

import com.tlu.tsms.common.ESystemLogLevel;
import com.tlu.tsms.exception.EStatusCode;
import com.tlu.tsms.utils.ErrorUtils;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.apache.logging.log4j.util.Strings;

import java.util.Optional;

@Converter
public class ESystemLogLevelConverter implements AttributeConverter<ESystemLogLevel, String> {

    @Override
    public String convertToDatabaseColumn(ESystemLogLevel eSystemLogLevel) {
        return Optional.ofNullable(eSystemLogLevel)
                .map(ESystemLogLevel::getLevel)
                .orElse(null);
    }

    @Override
    public ESystemLogLevel convertToEntityAttribute(String source) {
        try {
            if(Strings.isBlank(source)) return null;
            return ESystemLogLevel.lookup(source);
        } catch (Exception e) {
            throw ErrorUtils.exception(EStatusCode.SOURCE_NOT_FOUND, "ESystemLogLevel");
        }
    }
}
