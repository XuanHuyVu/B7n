package com.tlu.tsms.converter;

import com.tlu.tsms.common.EUserRole;
import com.tlu.tsms.exception.EStatusCode;
import com.tlu.tsms.utils.ErrorUtils;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.apache.logging.log4j.util.Strings;

import java.util.Optional;

@Converter
public class EUserRoleConverter implements AttributeConverter<EUserRole, String> {
    @Override
    public String convertToDatabaseColumn(EUserRole eUserRole) {
        return Optional.ofNullable(eUserRole)
                .map(EUserRole::getRole)
                .orElse(null);
    }

    @Override
    public EUserRole convertToEntityAttribute(String source) {
        try {
            if (Strings.isBlank(source)) return null;
            return EUserRole.lookup(source);
        } catch (IllegalArgumentException ex) {
            throw ErrorUtils.exception(EStatusCode.SOURCE_NOT_FOUND, "EUserRole");
        }
    }
}
