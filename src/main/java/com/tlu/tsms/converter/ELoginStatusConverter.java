package com.tlu.tsms.converter;

import com.tlu.tsms.common.ELoginStatus;
import com.tlu.tsms.exception.EStatusCode;
import com.tlu.tsms.utils.ErrorUtils;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.apache.logging.log4j.util.Strings;

import java.util.Optional;

@Converter
public class ELoginStatusConverter implements AttributeConverter<ELoginStatus, String> {

    @Override
    public String convertToDatabaseColumn(ELoginStatus eLoginStatus) {
        return Optional.ofNullable(eLoginStatus)
                .map(ELoginStatus::getStatus)
                .orElse(null);
    }

    @Override
    public ELoginStatus convertToEntityAttribute(String source) {
        try {
            if (Strings.isBlank(source)) return null;
            return ELoginStatus.lookup(source);
        } catch (IllegalArgumentException ex) {
            throw ErrorUtils.exception(EStatusCode.SOURCE_NOT_FOUND, "ELoginStatus");
        }
    }
}
