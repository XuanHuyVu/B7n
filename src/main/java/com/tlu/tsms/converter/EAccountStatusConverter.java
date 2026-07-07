package com.tlu.tsms.converter;

import com.tlu.tsms.common.EAccountStatus;
import com.tlu.tsms.exception.EStatusCode;
import com.tlu.tsms.utils.ErrorUtils;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.apache.logging.log4j.util.Strings;

import java.util.Optional;

@Converter
public class EAccountStatusConverter implements AttributeConverter<EAccountStatus, String> {

    @Override
    public String convertToDatabaseColumn(EAccountStatus eAccountStatus) {
        return Optional.ofNullable(eAccountStatus)
                .map(EAccountStatus::getStatus)
                .orElse(null);
    }

    @Override
    public EAccountStatus convertToEntityAttribute(String source) {
        try {
            if (Strings.isBlank(source)) return null;
            return EAccountStatus.lookup(source);
        } catch (IllegalArgumentException ex) {
            throw ErrorUtils.exception(EStatusCode.SOURCE_NOT_FOUND, "EAccountStatus");
        }
    }
}
