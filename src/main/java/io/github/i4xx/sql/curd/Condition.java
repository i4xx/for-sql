package io.github.i4xx.sql.curd;

import io.github.i4xx.sql.SqlUtils;

import java.util.Collection;

public class Condition implements ISqlSegment {

    private String column;
    private SqlKeyword keyword;
    private Object value;
    private String valueType;

    @Override
    public String getSqlSegment() {
        StringBuilder sqlSegment = new StringBuilder();
        sqlSegment.append(column).append(" ").append(keyword.getSqlSegment()).append(" ");
        switch (keyword) {
            case EQ:
            case NE:
            case GT:
            case GE:
            case LT:
            case LE:
                if (valueTypeIsColumn()) {
                    sqlSegment.append("?");
                } else {
                    sqlSegment.append(value);
                }
                break;
            case LIKE:
                sqlSegment.append("concat('%',?,'%')");
                break;
            case LIKE_LEFT:
                sqlSegment.append("concat(?,'%')");
                break;
            case LIKE_RIGHT:
                sqlSegment.append("concat('%',?)");
                break;
            case IN:
            case NOT_IN:
                sqlSegment.append(SqlUtils.inHolder(((Collection<?>) value).size()));
                break;
        }

        return sqlSegment.toString();
    }

    public boolean valueTypeIsColumn() {
        return "column".equals(valueType);
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public SqlKeyword getKeyword() {
        return keyword;
    }

    public void setKeyword(SqlKeyword keyword) {
        this.keyword = keyword;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }
}
