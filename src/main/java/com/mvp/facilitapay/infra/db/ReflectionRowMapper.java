package com.mvp.facilitapay.infra.db;

import jakarta.persistence.Column;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.RowMapper;

public class ReflectionRowMapper<T> implements RowMapper<T> {

    private final Class<T> type;

    public ReflectionRowMapper(Class<T> type) {
        this.type = type;
    }

    @Override
    public T mapRow(ResultSet rs, int rowNum) throws SQLException {
        Map<String, Integer> camelIndex = new HashMap<>();
        Map<String, Integer> rawIndex = new HashMap<>();

        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            String label = metaData.getColumnLabel(i);
            if (label == null || label.isBlank()) {
                label = metaData.getColumnName(i);
            }
            String camel = toCamel(label);
            camelIndex.put(camel.toLowerCase(Locale.ROOT), i);
            rawIndex.put(label.toLowerCase(Locale.ROOT), i);
        }

        try {
            Constructor<T> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            T instance = constructor.newInstance();

            for (Field field : getAllFields(type)) {
                field.setAccessible(true);
                Integer columnIndex = resolveColumnIndex(field, camelIndex, rawIndex);
                if (columnIndex == null) {
                    continue;
                }
                Object columnValue = rs.getObject(columnIndex);
                if (columnValue == null) {
                    if (!field.getType().isPrimitive()) {
                        field.set(instance, null);
                    }
                    continue;
                }
                Object converted = convertValue(columnValue, field.getType());
                if (converted != null || !field.getType().isPrimitive()) {
                    field.set(instance, converted);
                }
            }

            return instance;
        } catch (Exception e) {
            throw new SQLException("Failed to map row to " + type.getSimpleName(), e);
        }
    }

    private Integer resolveColumnIndex(Field field, Map<String, Integer> camelIndex, Map<String, Integer> rawIndex) {
        Column column = field.getAnnotation(Column.class);
        List<String> candidates = new ArrayList<>();

        if (column != null && column.name() != null && !column.name().isBlank()) {
            String name = column.name();
            candidates.add(name.toLowerCase(Locale.ROOT));
            candidates.add(toCamel(name).toLowerCase(Locale.ROOT));
        }

        String fieldName = field.getName();
        candidates.add(fieldName.toLowerCase(Locale.ROOT));
        candidates.add(toCamel(fieldName).toLowerCase(Locale.ROOT));

        for (String candidate : candidates) {
            if (camelIndex.containsKey(candidate)) {
                return camelIndex.get(candidate);
            }
            if (rawIndex.containsKey(candidate)) {
                return rawIndex.get(candidate);
            }
        }

        return null;
    }

    private List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            Field[] declared = current.getDeclaredFields();
            for (Field field : declared) {
                fields.add(field);
            }
            current = current.getSuperclass();
        }
        return fields;
    }

    private Object convertValue(Object value, Class<?> targetType) {
        if (targetType.isAssignableFrom(value.getClass())) {
            return value;
        }

        if (targetType == String.class) {
            return value.toString();
        }
        if (targetType == UUID.class) {
            return UUID.fromString(value.toString());
        }
        if (targetType == Instant.class) {
            if (value instanceof Timestamp timestamp) {
                return timestamp.toInstant();
            }
            return Instant.parse(value.toString());
        }
        if (targetType == LocalDateTime.class && value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        if (targetType == BigDecimal.class) {
            if (value instanceof BigDecimal bigDecimal) {
                return bigDecimal;
            }
            return new BigDecimal(value.toString());
        }
        if (targetType.isEnum()) {
            @SuppressWarnings({"unchecked", "rawtypes"})
            Class<Enum> enumType = (Class<Enum>) targetType;
            return Enum.valueOf(enumType, value.toString());
        }

        if (Number.class.isAssignableFrom(targetType) || targetType.isPrimitive()) {
            return convertNumber(value, targetType);
        }

        return null;
    }

    private Object convertNumber(Object value, Class<?> targetType) {
        if (!(value instanceof Number number)) {
            return null;
        }
        if (targetType == Integer.class || targetType == int.class) {
            return number.intValue();
        }
        if (targetType == Long.class || targetType == long.class) {
            return number.longValue();
        }
        if (targetType == Double.class || targetType == double.class) {
            return number.doubleValue();
        }
        if (targetType == Float.class || targetType == float.class) {
            return number.floatValue();
        }
        if (targetType == Short.class || targetType == short.class) {
            return number.shortValue();
        }
        if (targetType == Byte.class || targetType == byte.class) {
            return number.byteValue();
        }
        if (targetType == Boolean.class || targetType == boolean.class) {
            return number.intValue() != 0;
        }
        return null;
    }

    private String toCamel(String name) {
        StringBuilder builder = new StringBuilder();
        boolean nextUpper = false;
        for (char ch : name.toCharArray()) {
            if (ch == '_' || ch == ' ') {
                nextUpper = true;
                continue;
            }
            if (nextUpper) {
                builder.append(Character.toUpperCase(ch));
                nextUpper = false;
            } else {
                builder.append(Character.toLowerCase(ch));
            }
        }
        return builder.toString();
    }
}

