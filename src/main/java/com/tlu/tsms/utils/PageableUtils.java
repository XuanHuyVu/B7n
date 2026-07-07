package com.tlu.tsms.utils;

import com.tlu.tsms.common.Constant;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.*;

import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PageableUtils {

    /**
     * Parse sort string dạng: "id,desc" hoặc "email,asc".
     * Nếu lỗi thì fallback về "id,desc".
     */
    public static Sort parseSort(String sort) {
        try {
            if (Objects.isNull(sort) || sort.isBlank()) return Sort.by(Constant.ID).descending();
            String[] parts = sort.split(",");
            String field = parts[0].trim();
            String dir = (parts.length > 1) ? parts[1].trim() : Constant.ASC;
            if (field.isEmpty()) return Sort.by(Constant.ID).descending();
            return Constant.DESC.equalsIgnoreCase(dir)
                    ? Sort.by(field).descending()
                    : Sort.by(field).ascending();
        } catch (Exception e) {
            return Sort.by(Constant.ID).descending();
        }
    }

    public static Pageable pageable(int page, int size, String sort) {
        return PageRequest.of(page, size, parseSort(sort));
    }
}
