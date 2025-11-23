package com.mvp.facilitapay.infra.db;

import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;

public class BaseRepository {

    protected final JdbcTemplate jdbcTemplate;

    public BaseRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    protected <T> Optional<T> findOne(String sql, Object[] params, Class<T> clazz) {
        List<T> result = findAll(sql, params, clazz);
        return result.stream().findFirst();
    }

    protected <T> List<T> findAll(String sql, Object[] params, Class<T> clazz) {
        Object[] safeParams = params != null ? params : new Object[0];
        return jdbcTemplate.query(sql, safeParams, new ReflectionRowMapper<>(clazz));
    }

    protected int executeUpdate(String sql, Object[] params) {
        Object[] safeParams = params != null ? params : new Object[0];
        return jdbcTemplate.update(sql, safeParams);
    }
}
