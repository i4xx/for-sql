package io.github.i4xx.sql.curd;

import io.github.i4xx.sql.model.Meta;

import java.util.ArrayList;
import java.util.List;

public abstract class SqlBuilder<T, Children extends SqlBuilder<T, Children>> extends Curd<T, Children> implements ISelect, IDelete, IUpdate {

    protected final List<Condition> conditions = new ArrayList<>();
    protected List<UpdateColumn<T>> updateColumns;
    private String column;

    protected SqlBuilder(Class<T> clazz) {
        super(clazz);
    }

    public void column(String column) {
        this.column = column;
    }

    public Children eq(Object value) {
        Condition condition = new Condition();
        Meta<T>.Attribute attribute = meta.getAttribute(column);
        condition.setColumn(attribute.getColumn());
        conditions.add(condition);

        condition.setKeyword(SqlKeyword.EQ);
        condition.setValue(value);

        column = null;
        return children;
    }

    public Children set(Object value) {
        UpdateColumn<T> updateColumn = new UpdateColumn<>();
        Meta<T>.Attribute attribute = meta.getAttribute(column);
        updateColumn.setColumn(attribute.getColumn());
        updateColumn.setAttribute(attribute);
        updateColumns.add(updateColumn);

        try {
            attribute.getSetMethod().invoke(entity, value);
        } catch (Exception e) {
            e.printStackTrace();
        }

        updateColumn.setValue(value);
        column = null;
        return children;
    }

    private Condition currentCondition() {
        return conditions.get(conditions.size() - 1);
    }

    private UpdateColumn<T> currentUpdateColumn() {
        return updateColumns.get(updateColumns.size() - 1);
    }

}
