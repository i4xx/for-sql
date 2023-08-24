package io.github.i4xx.sql.curd;

import io.github.i4xx.sql.model.Meta;

public class UpdateColumn<T> implements ISqlSegment{

    private String column;
    private Object value;
    private Meta<T>.Attribute attribute;

    @Override
    public String getSqlSegment() {
        return null;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Meta<T>.Attribute getAttribute() {
        return attribute;
    }

    public void setAttribute(Meta<T>.Attribute attribute) {
        this.attribute = attribute;
    }
}
