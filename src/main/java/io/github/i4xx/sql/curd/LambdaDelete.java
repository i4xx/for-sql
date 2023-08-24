package io.github.i4xx.sql.curd;

import io.github.i4xx.sql.model.ColumnLambda;

public class LambdaDelete<T> extends LambdaConditions<T, ColumnLambda<T, ?>, LambdaDelete<T>> implements IDelete {

    public static <T> LambdaDelete<T> build(Class<T> clazz) {
        return new LambdaDelete<>(clazz);
    }

    protected LambdaDelete(Class<T> clazz) {
        super(clazz);
    }

}
