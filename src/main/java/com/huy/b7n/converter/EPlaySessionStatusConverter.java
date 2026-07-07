package com.huy.b7n.converter;

import com.huy.b7n.common.EPlaySessionStatus;
import com.huy.b7n.exception.EStatusCode;
import com.huy.b7n.utils.ErrorUtils;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.apache.logging.log4j.util.Strings;

import java.util.Optional;

@Converter
public class EPlaySessionStatusConverter implements AttributeConverter<EPlaySessionStatus, String> {

    @Override
    public String convertToDatabaseColumn(EPlaySessionStatus ePlaySessionStatus) {
        return Optional.ofNullable(ePlaySessionStatus)
                .map(EPlaySessionStatus::getStatus)
                .orElse(null);
    }

    @Override
    public EPlaySessionStatus convertToEntityAttribute(String source) {
        try {
            if (Strings.isBlank(source)) return null;
            return EPlaySessionStatus.lookup(source);
        } catch (IllegalArgumentException ex) {
            throw ErrorUtils.exception(EStatusCode.SOURCE_NOT_FOUND, "EPlaySessionStatus");
        }
    }
}