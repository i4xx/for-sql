package io.github.i4xx.sql.curd;

import io.github.i4xx.sql.SqlUtils;
import io.github.i4xx.sql.model.ColumnLambda;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class LambdaConditions<T, C extends ColumnLambda<T, ?>, Children extends LambdaConditions<T, C, Children>> extends Curd<T, Children> implements IConditions{

    private List<String> conditions = new ArrayList<>();
    private List<Object> params = new ArrayList<>();

    protected LambdaConditions(Class<T> clazz) {
        super(clazz);
    }

    private LambdaConditions<T, C, Children> build() {
        return new LambdaConditions<>(clazz);
    }

    public Children id(Serializable val) {
        conditions.add(String.format("%s=?", getColumnName(meta.getIdMethod().getName())));
        params.add(val);

        return children;
    }

    public Children eq(C column, Object val) {
        return addCondition(column, SqlKeyword.EQ, val);
    }

    public Children ne(C column, Object val) {
        return addCondition(column, SqlKeyword.NE, val);
    }

    public Children gt(C column, Object val) {
        return addCondition(column, SqlKeyword.GT, val);
    }

    public Children ge(C column, Object val) {
        return addCondition(column, SqlKeyword.GE, val);
    }

    public Children lt(C column, Object val) {
        return addCondition(column, SqlKeyword.LT, val);
    }

    public Children le(C column, Object val) {
        return addCondition(column, SqlKeyword.LE, val);
    }

    public Children like(C column, Object val) {
        return addCondition(column, SqlKeyword.LIKE, val);
    }

    public Children likeLeft(C column, Object val) {
        return addCondition(column, SqlKeyword.LIKE_LEFT, val);
    }

    public Children likeRight(C column, Object val) {
        return addCondition(column, SqlKeyword.LIKE_RIGHT, val);
    }

    public Children in(C column, Collection<?> collection) {
        return addCondition(column, SqlKeyword.IN, collection);
    }

    public Children notIn(C column, Collection<?> collection) {
        return addCondition(column, SqlKeyword.NOT_IN, collection);
    }

    public Children addCondition(C column, SqlKeyword keyword, Object val) {
        return addCondition_(getColumnName(column), keyword, val);
    }

    public Children fieldCompare(C column1, SqlKeyword keyword, C column2) {
        return fieldCompare(column1.getMethodName(), keyword, column2.getMethodName());
    }

    public Children fieldCompare(String field1, SqlKeyword keyword, String field2) {
        switch (keyword) {
            case EQ:

            case NE:

            case GT:

            case GE:

            case LT:

            case LE:
                conditions.add(String.format("%s %s %s",
                        meta.getColumnName(field1),
                        keyword.getSqlSegment(),
                        meta.getColumnName(field2)));
                break;
        }

        return this.children;
    }

    public Children addCondition(String field, SqlKeyword keyword, Object val) {
        String columnName = meta.getColumnName(field);
        if (columnName != null) addCondition_(columnName, keyword, val);

        return children;
    }

    private Children addCondition_(String column, SqlKeyword keyword, Object val) {
        if (val == null && this instanceof LambdaSelect) {
            return this.children;
        }

        switch (keyword) {
            case EQ:
                conditions.add(String.format("%s=?", column));
                params.add(val);
                break;
            case NE:
                conditions.add(String.format("%s != ?", column));
                params.add(val);
                break;
            case GT:
                conditions.add(String.format("%s > ?", column));
                params.add(val);
                break;
            case GE:
                conditions.add(String.format("%s >= ?", column));
                params.add(val);
                break;
            case LT:
                conditions.add(String.format("%s < ?", column));
                params.add(val);
                break;
            case LE:
                conditions.add(String.format("%s <= ?", column));
                params.add(val);
                break;
            case LIKE:
                conditions.add(column + " like concat('%',?,'%')");
                params.add(val);
                break;
            case LIKE_LEFT:
                conditions.add(column + " like concat(?,'%')");
                params.add(val);
                break;
            case LIKE_RIGHT:
                conditions.add(column + " like concat('%',?)");
                params.add(val);
                break;
            case IN:
                conditions.add(String.format("%s in %s", column, SqlUtils.inHolder(((Collection<?>) val).size())));
                params.addAll((Collection<?>) val);
                break;
            case NOT_IN:
                conditions.add(String.format("%s not in %s", column, SqlUtils.inHolder(((Collection<?>) val).size())));
                params.addAll((Collection<?>) val);
                break;
        }

        return this.children;
    }

    public Children or(OrCondition... orConditions) {
        LambdaConditions<T, C, Children> condition = this.build();
        for (OrCondition orCondition : orConditions) {
            orCondition.apply(condition);
        }

        conditions.add("(" + SqlUtils.splice(condition.conditions, " or ") + ")");
        params.add(condition.params);

        return children;
    }

    @FunctionalInterface
    public interface OrCondition {
        void apply(LambdaConditions condition);
    }

    protected String getColumnName(C column) {
        return meta.getColumnName(column.getMethodName());
    }

    protected String getColumnName(String methodName) {
        return meta.getColumnName(methodName);
    }

    public String conditionSql() {
        if (conditions.isEmpty()) {
            return "";
        }

        return "where " + SqlUtils.splice(conditions, " and ");
    }

    public Object[] conditionParams() {
        return params.toArray();
    }

    public List<Object> getParams() {
        return params;
    }

    public Children setParams(List<Object> params) {
        this.params = params;

        return children;
    }

    public Children addParams(Object... params) {
        if (params != null) {
            this.params.addAll(Arrays.asList(params));
        }

        return children;
    }

}
