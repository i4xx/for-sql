package io.github.i4xx.sql;

import io.github.i4xx.sql.curd.LambdaDelete;
import io.github.i4xx.sql.curd.Insert;
import io.github.i4xx.sql.curd.LambdaSelect;
import io.github.i4xx.sql.curd.LambdaUpdate;
import io.github.i4xx.sql.model.IdModel;
import io.github.i4xx.sql.model.Meta;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SqlTemplate<T, ID extends Serializable> extends QueryTemplate {

    protected Meta<T> meta;
    protected Class<T> clazz;
    protected Class<ID> idClass;

    public SqlTemplate() {
        ParameterizedType parameterizedType = (ParameterizedType) this.getClass().getGenericSuperclass();
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        this.clazz = (Class<T>) actualTypeArguments[0];
        this.idClass = (Class<ID>) actualTypeArguments[1];
        this.meta = Meta.getInstance(clazz);
    }

    public int insert(T t) {
        Insert<T> insert = Insert.build(t);

        if (t instanceof IdModel && ((IdModel) t).getId() == null) {
            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(insert.getSql(), Statement.RETURN_GENERATED_KEYS);
                int i = 0;
                for (Object p : insert.getParams()) {
                    i++;
                    ps.setObject(i, p);
                }
                return ps;
            }, keyHolder);

            ((IdModel) t).setId(keyHolder.getKey().intValue());

            return 1;
        }

        return update(insert.getSql(), insert.getParams());
    }

    public int batchInsert(List<T> list) {
        if (list == null || list.isEmpty()) {
            return 0;
        }

        Insert<T> insert = Insert.build(clazz);
        List<Object[]> params = new ArrayList<>(list.size());
        list.forEach(i -> params.add(insert.getParams(i)));

        int[] ints = jdbcTemplate.batchUpdate(insert.getSql(), params);
        return Arrays.stream(ints).sum();
    }

    public int update(LambdaUpdate<T> update) {
        update.safe();

        List<Object> conditionParams = update.getParams();
        if (conditionParams == null || conditionParams.isEmpty()) {
            throw new RuntimeException("warning: condition params is empty");
        }

        List<Object> params = new ArrayList<>();
        params.addAll(update.getUpdateParams());
        params.addAll(conditionParams);

        return update(
                String.format("update %s set %s ", meta.getTableName(), update.getUpdateColumns()) + update.conditionSql(),
                params.toArray()

        );
    }

    public int batchUpdate(List<T> list) {
        if (list == null || list.isEmpty()) {
            return 0;
        }

        LambdaUpdate<T> update = LambdaUpdate.build(clazz).ifNullSet(list.get(0));
        List<Object[]> params = new ArrayList<>(list.size());
        list.forEach(i -> params.add(update.getParams(i)));

        int[] ints = jdbcTemplate.batchUpdate(update.getSetSql(), params);
        return Arrays.stream(ints).sum();
    }

    public T select(ID id) {
        return id == null ? null : select(LambdaSelect.build(clazz).id(id));
    }

    public T select(LambdaSelect<T> select) {
        select.safe();
        return queryForObject(select.getSql(), select.conditionParams(), clazz);
    }

    public int delete(ID id) {
        return id == null ? 0 : delete(LambdaDelete.build(clazz).id(id));
    }

    public int delete(LambdaDelete<T> delete) {
        delete.safe();
        return update(String.format("delete from %s %s", meta.getTableName(), delete.conditionSql()),
                delete.conditionParams()
        );
    }

    public int update(String sql, Object[] params) {
        return jdbcTemplate.update(sql, params);
    }


}
