package io.github.i4xx.sql.model;

import io.github.i4xx.sql.curd.Select;
import io.github.i4xx.sql.curd.SqlKeyword;

import java.util.List;

public class QueryBuilder {

    private String[] fields;
    private List<Condition> conditions;
    private List<Order> orders;
    private Integer pageIndex;
    private Integer pageSize;

    public static class Condition {

        private String field;
        private SqlKeyword sqlKeyword;
        private Object value;
        private String type;

        private List<Condition> or;

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public SqlKeyword getSqlKeyword() {
            return sqlKeyword;
        }

        public void setSqlKeyword(SqlKeyword sqlKeyword) {
            this.sqlKeyword = sqlKeyword;
        }

        public Object getValue() {
            if ("Long".equals(type)) {
                return Long.valueOf(value.toString());
            }

            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public List<Condition> getOr() {
            return or;
        }

        public void setOr(List<Condition> or) {
            this.or = or;
        }
    }

    public static class Order {

        private String field;
        private SqlKeyword sqlKeyword;

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public SqlKeyword getSqlKeyword() {
            return sqlKeyword;
        }

        public void setSqlKeyword(SqlKeyword sqlKeyword) {
            this.sqlKeyword = sqlKeyword;
        }
    }

    public <T> Select<T> build(Class<T> clazz) {
        Select<T> select = Select.build(clazz).columns(fields);
        if (conditions != null) conditions.forEach(c -> {
            if (c.or == null || c.or.isEmpty()) {
                if ("Field".equals(c.type)) {
                    select.fieldCompare(c.field, c.sqlKeyword, c.value.toString());
                } else {
                    select.addCondition(c.field, c.sqlKeyword, c.getValue());
                }
            } else {
                c.or.add(0, c);
                io.github.i4xx.sql.curd.Condition.OrCondition[] orConditions = new io.github.i4xx.sql.curd.Condition.OrCondition[c.or.size()];
                int i = 0;
                for (Condition condition : c.or) {
                    orConditions[i] = oc -> {
                        if ("Field".equals(condition.type)) {
                            oc.fieldCompare(condition.field, condition.sqlKeyword, condition.value.toString());
                        } else {
                            oc.addCondition(condition.field, condition.sqlKeyword, condition.getValue());
                        }
                    };
                    i++;
                }
                select.or(orConditions);
            }
        });
        if (orders != null) orders.forEach(o -> select.orderBy(o.field, o.sqlKeyword));
        select.page(pageIndex, pageSize);
        return select;
    }

    public String[] getFields() {
        return fields;
    }

    public void setFields(String[] fields) {
        this.fields = fields;
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public Integer getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(Integer pageIndex) {
        this.pageIndex = pageIndex;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}
