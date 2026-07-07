package com.huy.b7n.converter;

import com.huy.b7n.common.ERoundStatus;
import com.huy.b7n.exception.EStatusCode;
import com.huy.b7n.utils.ErrorUtils;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.apache.logging.log4j.util.Strings;

import java.util.Optional;

@Converter
public class ERoundStatusConverter implements AttributeConverter<ERoundStatus, String> {

    @Override
    public String convertToDatabaseColumn(ERoundStatus eRoundStatus) {
        return Optional.ofNullable(eRoundStatus)
                .map(ERoundStatus::getStatus)
                .orElse(null);
    }

    @Override
    public ERoundStatus convertToEntityAttribute(String source) {
        try {
            if (Strings.isBlank(source)) return null;
            return ERoundStatus.lookup(source);
        } catch (IllegalArgumentException ex) {
            throw ErrorUtils.exception(EStatusCode.SOURCE_NOT_FOUND, "ERoundStatus");
        }
    }
}