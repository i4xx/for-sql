package io.github.i4xx.sql;

import io.github.i4xx.sql.curd.SafeCurd;
import io.github.i4xx.sql.curd.LambdaSelect;
import io.github.i4xx.sql.model.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SingleColumnRowMapper;

import javax.annotation.Resource;
import java.util.List;

public class QueryTemplate {

    @Resource
    protected JdbcTemplate jdbcTemplate;
    @Autowired(required = false)
    protected SafeCurd safeCurd;

    public <T> List<T> list(LambdaSelect<T> select) {
        select.safe();

        return query(select.getSql(), select.conditionParams(), select.getClazz());
    }

    public <T> int count(LambdaSelect<T> select) {
        select.safe();
        Integer count = queryForObject(
                select.getCountSql(),
                select.conditionParams(),
                Integer.class);
        return count == null ? 0 : count;
    }

    public <T> Page<T> page(LambdaSelect<T> select) {
        select.safe();

        Integer count = queryForObject(select.getCountSql(), select.conditionParams(), Integer.class);
        count = count == null ? 0 : count;

        Page<T> page = new Page<>(count);
        if (select.getMinIndex() > count) {
            return page;
        }

        page.setList(list(select));
        return page;
    }

    public <T> List<T> query(String sql, Object[] params, Class<T> clazz) {
        if (Integer.class.equals(clazz) || Long.class.equals(clazz) || Boolean.class.equals(clazz) || String.class.equals(clazz)) {
            return jdbcTemplate.query(
                    sql,
                    params,
                    new SingleColumnRowMapper<>(clazz)
            );
        } else {
            return jdbcTemplate.query(
                    sql,
                    params,
                    new BeanPropertyRowMapper<>(clazz)
            );
        }
    }

    public <T> T queryForObject(String sql, Object[] params, Class<T> clazz) {
        if (Integer.class.equals(clazz) || Long.class.equals(clazz) || Boolean.class.equals(clazz) || String.class.equals(clazz)) {
            return jdbcTemplate.queryForObject(sql, params, clazz);
        } else {
            return jdbcTemplate.queryForObject(sql, params, new BeanPropertyRowMapper<>(clazz));
        }
    }
}
