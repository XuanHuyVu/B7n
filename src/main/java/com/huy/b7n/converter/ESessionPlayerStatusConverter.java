package com.huy.b7n.converter;

import com.huy.b7n.common.ESessionPlayerStatus;
import com.huy.b7n.exception.EStatusCode;
import com.huy.b7n.utils.ErrorUtils;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.apache.logging.log4j.util.Strings;

import java.util.Optional;

@Converter
public class ESessionPlayerStatusConverter implements AttributeConverter<ESessionPlayerStatus, String> {

    @Override
    public String convertToDatabaseColumn(ESessionPlayerStatus eSessionPlayerStatus) {
        return Optional.ofNullable(eSessionPlayerStatus)
                .map(ESessionPlayerStatus::getStatus)
                .orElse(null);
    }

    @Override
    public ESessionPlayerStatus convertToEntityAttribute(String source) {
        try {
            if (Strings.isBlank(source)) return null;
            return ESessionPlayerStatus.lookup(source);
        } catch (IllegalArgumentException ex) {
            throw ErrorUtils.exception(EStatusCode.SOURCE_NOT_FOUND, "ESessionPlayerStatus");
        }
    }
}