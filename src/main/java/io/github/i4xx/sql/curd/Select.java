package io.github.i4xx.sql.curd;

import io.github.i4xx.sql.SqlUtils;
import io.github.i4xx.sql.model.ColumnLambda;

import java.util.ArrayList;
import java.util.List;

public class Select<T> extends Condition<T, ColumnLambda<T, ?>, Select<T>> {

    protected List<String> columns;

    protected String target;

    protected Integer pageIndex;
    protected Integer pageSize;

    protected List<String> orders;

    public static <T> Select<T> build(Class<T> clazz) {
        return new Select<>(clazz);
    }

    protected Select(Class<T> clazz) {
        super(clazz);
    }

    public Select<T> page(Integer pageIndex, Integer pageSize) {
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;

        return this;
    }


    public Select<T> orderBy(ColumnLambda<T, ?> columnLambda, SqlKeyword keyword) {
        orders.add(meta.getColumnName(columnLambda.getMethodName()) + " " + keyword);
        return this;
    }

    public Select<T> orderBy(String column, SqlKeyword keyword) {
        String columnName = meta.getColumnName(column);
        if (columnName != null) {
            orders.add(columnName + " " + keyword.getSqlSegment());
        }

        return this;
    }

    public String limit() {
        if (pageIndex == null) {
            return "";
        }

        return String.format(" limit %d, %d", pageIndex * pageSize, pageSize);
    }

    public int getMinIndex() {
        return pageIndex * pageSize;
    }

    public String getSql() {
        return target == null ?
                String.format("select %s from %s %s %s %s", columns(), meta.getTableName(), conditionSql(), getOrders(), limit()) :
                String.format("%s %s %s %s", target, conditionSql(), getOrders(), limit());
    }

    private String columns() {
        if (columns == null) return "*";

        return SqlUtils.splice(columns, ",");
    }

    public String getCountSql() {
        return String.format("select count(*) from %s %s", meta.getTableName(), conditionSql());
    }

    public String getOrders() {
        if (orders.isEmpty()) {
            return "";
        }

        return " order by " + SqlUtils.splice(orders, ",");
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getTarget() {
        return target;
    }

    @SafeVarargs
    public final Select<T> columns(ColumnLambda<T, ?>... columnLambdas) {
        if (columns == null) {
            columns = new ArrayList<>();
        }

        for (ColumnLambda<T, ?> columnLambda : columnLambdas) {
            columns.add(meta.getColumnName(columnLambda.getMethodName()));
        }

        return this;
    }

    public Select<T> columns(ColumnLambda<T, ?> columnLambda) {
        if (columns == null) {
            columns = new ArrayList<>();
        }

        columns.add(meta.getColumnName(columnLambda.getMethodName()));

        return this;
    }

    public Select<T> columns(String[] columns) {
        String columnName;
        for (String column : columns) {
            columnName = meta.getColumnName(column);
            if (columnName != null) {
                if (this.columns == null) this.columns = new ArrayList<>();

                this.columns.add(columnName);
            }
        }

        return this;
    }
}
