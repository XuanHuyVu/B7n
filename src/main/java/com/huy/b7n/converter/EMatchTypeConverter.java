package com.huy.b7n.converter;

import com.huy.b7n.common.EMatchType;
import com.huy.b7n.common.EMatchType;
import com.huy.b7n.exception.EStatusCode;
import com.huy.b7n.utils.ErrorUtils;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.apache.logging.log4j.util.Strings;

import java.util.Optional;

@Converter
public class EMatchTypeConverter implements AttributeConverter<EMatchType, String> {

    @Override
    public String convertToDatabaseColumn(EMatchType eMatchStatus) {
        return Optional.ofNullable(eMatchStatus)
                .map(EMatchType::getMatchType)
                .orElse(null);
    }

    @Override
    public EMatchType convertToEntityAttribute(String source) {
        try {
            if (Strings.isBlank(source)) return null;
            return EMatchType.lookup(source);
        } catch (IllegalArgumentException ex) {
            throw ErrorUtils.exception(EStatusCode.SOURCE_NOT_FOUND, "EMatchType");
        }
    }
}