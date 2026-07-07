package com.huy.b7n.converter;

import com.huy.b7n.common.EMatchStatus;
import com.huy.b7n.exception.EStatusCode;
import com.huy.b7n.utils.ErrorUtils;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.apache.logging.log4j.util.Strings;

import java.util.Optional;

@Converter
public class EMatchStatusConverter implements AttributeConverter<EMatchStatus, String> {

    @Override
    public String convertToDatabaseColumn(EMatchStatus eMatchStatus) {
        return Optional.ofNullable(eMatchStatus)
                .map(EMatchStatus::getStatus)
                .orElse(null);
    }

    @Override
    public EMatchStatus convertToEntityAttribute(String source) {
        try {
            if (Strings.isBlank(source)) return null;
            return EMatchStatus.lookup(source);
        } catch (IllegalArgumentException ex) {
            throw ErrorUtils.exception(EStatusCode.SOURCE_NOT_FOUND, "EMatchStatus");
        }
    }
}