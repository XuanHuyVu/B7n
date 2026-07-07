package com.huy.b7n.converter;

import com.huy.b7n.common.ETeamCode;
import com.huy.b7n.exception.EStatusCode;
import com.huy.b7n.utils.ErrorUtils;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.apache.logging.log4j.util.Strings;

import java.util.Optional;

@Converter
public class ETeamCodeConverter implements AttributeConverter<ETeamCode, String> {

    @Override
    public String convertToDatabaseColumn(ETeamCode eTeamCode) {
        return Optional.ofNullable(eTeamCode)
                .map(ETeamCode::getTeamCode)
                .orElse(null);
    }

    @Override
    public ETeamCode convertToEntityAttribute(String source) {
        try {
            if (Strings.isBlank(source)) return null;
            return ETeamCode.lookup(source);
        } catch (IllegalArgumentException ex) {
            throw ErrorUtils.exception(EStatusCode.SOURCE_NOT_FOUND, "ETeamCode");
        }
    }
}