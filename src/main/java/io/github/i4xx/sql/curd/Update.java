package io.github.i4xx.sql.curd;

import io.github.i4xx.sql.SqlUtils;
import io.github.i4xx.sql.model.ColumnLambda;
import io.github.i4xx.sql.model.Meta;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class Update<T> extends Condition<T, ColumnLambda<T, ?>, Update<T>> {

    private final List<String> updateColumns = new ArrayList<>();
    private final List<Object> updateParams = new ArrayList<>();
    private List<Method> setMethods;

    public static <T> Update<T> build(Class<T> clazz) {
        return new Update<>(clazz);
    }

    public static <T> Update<T> build(T t) {
        Update<T> update = new Update<>((Class<T>) t.getClass());
        update.set(t);
        return update;
    }

    protected Update(Class<T> clazz) {
        super(clazz);
    }

    private Update<T> set(T t) {
        entity = t;
        List<Method> methods = meta.getMethods();
        Method method;
        for (int i = 0; i < methods.size(); i++) {
            try {
                method = methods.get(i);
                Object obj = method.invoke(t);
                if (obj == null) {
                    continue;
                }

                updateColumns.add(meta.getColumnName(method.getName()) + "=?");
                updateParams.add(obj);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Object id = meta.getId(t);
        if (id != null) id((Serializable) id);

        return this;
    }

    public Update<T> ifNullSet(T t) {
        setMethods = new ArrayList<>();
        List<Method> methods = meta.getMethods();
        Method method;
        for (int i = 0; i < methods.size(); i++) {
            try {
                method = methods.get(i);
                Object obj = method.invoke(t);
                if (obj == null) {
                    continue;
                }

                setMethods.add(method);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return this;
    }

    public String getSetSql() {
        Method method;
        for (int i = 0; i < setMethods.size(); i++) {
            method = setMethods.get(i);
            updateColumns.add(meta.getColumnName(method.getName()) + "=?");
        }

        return String.format("update %s set %s", meta.getTableName(), SqlUtils.splice(updateColumns, ","));
    }

    public Update<T> set(ColumnLambda<T, ?> columnLambda, Object obj) {
        if (entity == null) entity = SqlUtils.newInstance(clazz);

        Meta<T>.Attribute attribute = meta.getAttribute(columnLambda.getMethodName());
        try {
            attribute.getSetMethod().invoke(entity, obj);
        } catch (Exception e) {
            e.printStackTrace();
        }

        updateColumns.add(attribute.getColumn() + "=?");
        updateParams.add(obj);

        return this;
    }

    public String getUpdateColumns() {
        return SqlUtils.splice(updateColumns, ",");
    }

    public List<Object> getUpdateParams() {
        return updateParams;
    }

    public Object[] getParams(T t) {
        List<Object> params = new ArrayList<>();
        Method method;
        for (int i = 0; i < setMethods.size(); i++) {
            try {
                method = setMethods.get(i);
                Object obj = method.invoke(t);
                params.add(obj);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        params.add(meta.getId(t));
        return params.toArray();
    }

    public void safe(List<T> list) {
        if (safeCurd != null) {
            safeCurd.safe(this,list);
        }
    }

}
