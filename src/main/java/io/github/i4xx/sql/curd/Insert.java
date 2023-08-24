package io.github.i4xx.sql.curd;

import io.github.i4xx.sql.SqlUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class Insert<T> extends Curd<T, Insert<T>> {

    private String sql;
    private List<Object> params = new ArrayList<>();

    public static <T> Insert<T> build(Class<T> clazz){
        Insert<T> insert = new Insert<>(clazz);
        insert.sql = insert.meta.getInsertSql() + insert.meta.getInsertSqlValue();
        return insert;
    }

    public static <T> Insert<T> build(T t){
        Insert<T> insert = new Insert<>((Class<T>) t.getClass());

        insert.sql = insert.meta.getInsertSql() + insert.meta.getInsertSqlValue();
        insert.addParams(t);

        return insert;
    }

    public static <T> Insert<T> build(List<T> list) {
        Insert<T> insert = new Insert<>((Class<T>) list.get(0).getClass());

        List<String> holder = new ArrayList<>(list.size());
        list.forEach(i -> {
            insert.addParams(i);
            holder.add(insert.meta.getInsertSqlValue());
        });
        insert.sql = insert.meta.getInsertSql() + SqlUtils.splice(holder, ",");

        return insert;
    }

    protected Insert(Class<T> clazz) {
        super(clazz);
    }

    private void addParams(T t) {
        for (Method method : meta.getMethods()) {
            try {
                params.add(method.invoke(t));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getSql() {
        return sql;
    }

    public Object[] getParams() {
        return params.toArray();
    }

    public Object[] getParams(T t) {
        params = new ArrayList<>();
        addParams(t);
        return params.toArray();
    }

    public void safe(List<T> list) {
        if (safeCurd != null) {
            safeCurd.safe(this,list);
        }
    }

}
