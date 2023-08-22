package io.github.i4xx.sql.curd;

import io.github.i4xx.sql.model.ColumnLambda;

public class Delete<T> extends Condition<T, ColumnLambda<T, ?>, Delete<T>> {

    public static <T> Delete<T> build(Class<T> clazz) {
        return new Delete<>(clazz);
    }

    protected Delete(Class<T> clazz) {
        super(clazz);
    }

}
