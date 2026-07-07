package com.tlu.tsms.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import java.beans.PropertyDescriptor;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * MapperUtils: tập các tiện ích mapping / copy / json cho DTO-Entity-Request-Response.
 * <p>
 * - objectMapper: mapper "thường" (giữ nguyên null)
 * - ignoreNullObjectMapper: mapper cho PATCH (bỏ qua field null của source)
 * <p>
 * Lưu ý: các method convert/read đang trả null khi lỗi (fail-soft) để tránh crash,
 * nhưng bạn nên thống nhất convention trong dự án (fail-fast hay fail-soft).
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MapperUtils {

    private static final ObjectMapper objectMapper;
    private static final ObjectMapper ignoreNullObjectMapper;

    static {
        objectMapper = init(false);
        ignoreNullObjectMapper = init(true);
    }

    private static ObjectMapper init(boolean ignoreNull) {
        ObjectMapper objectMapper = JsonMapper.builder()
                .findAndAddModules()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .defaultDateFormat(new StdDateFormat())
                .build();
        if (ignoreNull)
            objectMapper.setDefaultPropertyInclusion(JsonInclude.Value.construct(JsonInclude.Include.NON_NULL, JsonInclude.Include.NON_NULL));
        return objectMapper;
    }

    /**
     * Convert object bất kỳ sang class đích.
     * Dùng khi muốn map nhanh Map -> DTO, DTO -> Entity, Entity -> DTO...
     *
     * @return object mapped hoặc null nếu input null hoặc mapping lỗi.
     */
    public static <T> T convertValue(Object value, Class<T> clazz) {
        try {
            return Objects.isNull(value) ? null : objectMapper.convertValue(value, clazz);
        } catch (Exception ex) {
            log.error("Error convert value to {}: {}", clazz.getSimpleName(), ex.getMessage(), ex);
            return null;
        }
    }

    /**
     * Convert object sang generic type (List<T>, Map<K,V>...) thông qua TypeReference.
     *
     * @return object mapped hoặc null nếu input null hoặc mapping lỗi.
     */
    public static <T> T convertValue(Object value, TypeReference<T> typeRef) {
        try {
            return Objects.isNull(value) ? null : objectMapper.convertValue(value, typeRef);
        } catch (Exception ex) {
            log.error("Error convert value (TypeReference): {}", ex.getMessage(), ex);
            return null;
        }
    }

    /**
     * Parse JSON string -> object theo class.
     *
     * @return object hoặc null nếu jsonStr null hoặc parse lỗi.
     */
    public static <T> T readValue(String jsonStr, Class<T> clazz) {
        try {
            return Objects.isNull(jsonStr) ? null : objectMapper.readValue(jsonStr, clazz);
        } catch (Exception ex) {
            log.error("Error read value to {}: {}", clazz.getSimpleName(), ex.getMessage(), ex);
            return null;
        }
    }

    /**
     * Parse JSON string -> generic type (List<T>, Map<...>) theo TypeReference.
     *
     * @return object hoặc null nếu jsonStr null hoặc parse lỗi.
     */
    public static <T> T readValue(String jsonStr, TypeReference<T> typeRef) {
        try {
            return Objects.isNull(jsonStr) ? null : objectMapper.readValue(jsonStr, typeRef);
        } catch (Exception ex) {
            log.error("Error read value (TypeReference): {}", ex.getMessage(), ex);
            return null;
        }
    }

    /**
     * Parse JSON từ InputStream -> object theo class.
     * Thường dùng khi đọc file json resource.
     *
     * @return object hoặc null nếu parse lỗi.
     */
    public static <T> T readValue(InputStream is, Class<T> clazz) {
        try {
            return objectMapper.readValue(is, clazz);
        } catch (Exception ex) {
            log.error("Error read value from input stream to {}: {}", clazz.getSimpleName(), ex.getMessage(), ex);
            return null;
        }
    }

    public static ObjectNode createObjectNode() {
        return objectMapper.createObjectNode();
    }

    /**
     * Copy kiểu PATCH:
     * - Chỉ copy các field non-null từ source sang target.
     * - Field null sẽ bị bỏ qua (không overwrite giá trị hiện có của target).
     * <p>
     * Lưu ý: với List/array thường sẽ overwrite toàn bộ list nếu field đó non-null.
     */
    public static <T> void copyNonNull(Object source, T target) {
        if (Objects.isNull(source) || Objects.isNull(target)) return;
        try {
            ignoreNullObjectMapper.updateValue(target, source);
        } catch (Exception ex) {
            throw new RuntimeException("copyNonNull error: " + source.getClass().getSimpleName(), ex);
        }
    }

    /**
     * Copy kiểu PATCH nhưng cho phép ignore một số field:
     * - Chỉ copy field non-null
     * - Bỏ qua các field nằm trong ignoreFields (VD: id, createdAt, createdBy...)
     */
    public static <T> void copyNonNullIgnore(Object source, T target, Set<String> ignoreFields) {
        if (source == null || target == null) return;
        try {
            ObjectNode node = ignoreNullObjectMapper.valueToTree(source);
            if (ignoreFields != null) {
                for (String f : ignoreFields) node.remove(f);
            }
            ignoreNullObjectMapper.updateValue(target, node);
        } catch (Exception ex) {
            throw new RuntimeException("copyNonNullIgnore error: " + source.getClass().getSimpleName(), ex);
        }
    }

    /**
     * Copy toàn bộ properties từ source sang target, nhưng ignore một số field:
     * - Có thể overwrite cả null (BeanUtils copy theo property, null vẫn được copy)
     * - Bỏ qua các field nằm trong ignoreFields
     * <p>
     * Phù hợp: map DTO->Entity trong create/update (khi bạn muốn overwrite có chủ đích).
     */
    public static <T> void copyIgnore(Object source, T target, Set<String> ignoreFields) {
        if (Objects.isNull(source) || Objects.isNull(target)) return;
        try {
            String[] ignoreArr = Objects.isNull(ignoreFields) ? new String[0] : ignoreFields.toArray(new String[0]);
            BeanUtils.copyProperties(source, target, ignoreArr);
        } catch (Exception ex) {
            throw new RuntimeException("copyIgnore error: " + source.getClass().getSimpleName(), ex);
        }
    }

    /**
     * Copy theo whitelist (includeFields):
     * - Chỉ copy đúng các field nằm trong includeFields.
     * - Các field khác không bị đụng tới.
     * <p>
     * Hữu ích: update một phần theo quy ước whitelist.
     */
    public static <T> void copyInclude(Object source, T target, Set<String> includeFields) {
        if (Objects.isNull(source) || Objects.isNull(target)) return;
        if (Objects.isNull(includeFields) || includeFields.isEmpty()) return;
        try {
            List<String> ignore = Arrays.stream(BeanUtils.getPropertyDescriptors(target.getClass()))
                    .map(PropertyDescriptor::getName)
                    .filter(name -> !"class".equals(name))
                    .filter(name -> !includeFields.contains(name))
                    .toList();
            BeanUtils.copyProperties(source, target, ignore.toArray(new String[0]));
        } catch (Exception ex) {
            throw new RuntimeException("copyInclude error: " + source.getClass().getSimpleName(), ex);
        }
    }

    /**
     * Lấy danh sách field name của class + super class (không lấy static/transient).
     * Dùng khi bạn cần thao tác reflection theo field.
     * <p>
     * Lưu ý: method này lấy theo Field (không theo property getter/setter).
     */
    public static List<String> getAllFieldNames(Class<?> clazz) {
        List<String> fields = new ArrayList<>();
        Class<?> c = clazz;
        while (Objects.nonNull(c) && !Objects.equals(c, Object.class)) {
            for (Field field : c.getDeclaredFields()) {
                int modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers)) continue;
                fields.add(field.getName());
            }
            c = c.getSuperclass();
        }
        return fields.stream().distinct().toList();
    }
}
