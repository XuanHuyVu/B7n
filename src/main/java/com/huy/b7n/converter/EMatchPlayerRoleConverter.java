package com.huy.b7n.converter;

import com.huy.b7n.common.EMatchPlayerRole;
import com.huy.b7n.exception.EStatusCode;
import com.huy.b7n.utils.ErrorUtils;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.apache.logging.log4j.util.Strings;

import java.util.Optional;

@Converter
public class EMatchPlayerRoleConverter implements AttributeConverter<EMatchPlayerRole, String> {

    @Override
    public String convertToDatabaseColumn(EMatchPlayerRole eMatchPlayerRole) {
        return Optional.ofNullable(eMatchPlayerRole)
                .map(EMatchPlayerRole::getRole)
                .orElse(null);
    }

    @Override
    public EMatchPlayerRole convertToEntityAttribute(String source) {
        try {
            if (Strings.isBlank(source)) return null;
            return EMatchPlayerRole.lookup(source);
        } catch (IllegalArgumentException ex) {
            throw ErrorUtils.exception(EStatusCode.SOURCE_NOT_FOUND, "EMatchPlayerRole");
        }
    }
}