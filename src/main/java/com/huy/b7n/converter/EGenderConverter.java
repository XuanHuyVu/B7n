package com.huy.b7n.converter;

import com.huy.b7n.common.EGender;
import com.huy.b7n.exception.EStatusCode;
import com.huy.b7n.utils.ErrorUtils;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.apache.logging.log4j.util.Strings;

import java.util.Optional;

@Converter
public class EGenderConverter implements AttributeConverter<EGender, String> {

    @Override
    public String convertToDatabaseColumn(EGender eAcademicRank) {
        return Optional.ofNullable(eAcademicRank)
                .map(EGender::getGender)
                .orElse(null);
    }

    @Override
    public EGender convertToEntityAttribute(String source) {
        try {
            if (Strings.isBlank(source)) return null;
            return EGender.lookup(source);
        } catch (IllegalArgumentException ex) {
            throw ErrorUtils.exception(EStatusCode.SOURCE_NOT_FOUND, "EGender");
        }
    }
}
