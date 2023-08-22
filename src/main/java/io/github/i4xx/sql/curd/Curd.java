package io.github.i4xx.sql.curd;

import io.github.i4xx.sql.model.ColumnLambda;
import io.github.i4xx.sql.model.Meta;

import java.util.List;

public class Curd<T, C extends ColumnLambda<T, ?>, Children extends Curd<T, C, Children>> {

    protected Meta<T> meta;
    protected Class<T> clazz;
    protected T entity;
    private boolean safe = false;
    protected final Children children = (Children) this;

    protected static SafeCurd safeCurd;

    protected Curd(Class<T> clazz) {
        this.clazz = clazz;
        this.meta = Meta.getInstance(clazz);
    }

    public Meta<T> getMeta() {
        return meta;
    }

    public T getEntity() {
        return entity;
    }

    public Class<T> getClazz() {
        return clazz;
    }

    public Children skipSafe() {
        this.safe = true;

        return children;
    }

    public Children safe() {
        if (!safe && safeCurd != null) {
            safeCurd.safe(this);
            safe = true;
        }

        return children;
    }

    public void afterInject(ColumnLambda<?, ?> columnLambda) {
    }

}
