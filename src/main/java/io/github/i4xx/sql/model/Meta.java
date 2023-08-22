package io.github.i4xx.sql.model;

import io.github.i4xx.sql.SqlUtils;
import io.github.i4xx.sql.annotation.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class Meta<T> {

    private final static Map<String, Meta<?>> META_CACHE = new HashMap<>();

    private final Class<T> clazz;
    private String tableName;
    private final String insertSql;
    private final String insertSqlValue;

    private Method getIdMethod;
    private final List<Method> methods = new ArrayList<>();
    private final List<Attribute> permissionAttributes = new ArrayList<>();

    private final Map<String, Attribute> attributes = new HashMap<>();

    public String getColumnName(String key) {
        Attribute attribute = attributes.get(key);
        return attribute == null ? null : attribute.getColumn();
    }

    public static <T> Meta<T> getInstance(Class<T> clazz) {
        Meta<T> meta = (Meta<T>) META_CACHE.get(clazz.getName());

        if (meta == null) {
            meta = new Meta<>(clazz);
        }
        return meta;
    }

    private Meta(Class<T> clazz) {
        this.clazz = clazz;
        Table table = clazz.getAnnotation(Table.class);
        if (table != null) this.tableName = table.name();

        Field[] fields = clazz.getDeclaredFields();
        List<String> insertColumns = new ArrayList<>();

        for (Field field : fields) {
            String modifier = Modifier.toString(field.getModifiers());
            if (modifier.contains("static") || modifier.contains("final") || field.getAnnotation(Transient.class) != null) {
                continue;
            }

            Attribute attribute = new Attribute(field);

            if (attribute.dataPermission != null) {
                permissionAttributes.add(attribute);
            }

            insertColumns.add(attribute.column);

            if (attribute.idField) {
                this.getIdMethod = attribute.getMethod;
            } else {
                this.methods.add(attribute.getMethod);
            }
        }

        this.insertSql = String.format("insert into %s (%s) values ", this.tableName, SqlUtils.splice(insertColumns, ","));
        this.insertSqlValue = SqlUtils.inHolder(insertColumns.size());
        META_CACHE.put(clazz.getName(), this);
    }

    public String getInsertSql() {
        return insertSql;
    }

    public String getInsertSqlValue() {
        return insertSqlValue;
    }

    public List<Method> getMethods() {
        return methods;
    }

    public String getTableName() {
        return tableName;
    }

    public Class<T> getClazz() {
        return clazz;
    }

    public List<Attribute> getPermissionAttributes() {
        return permissionAttributes;
    }

    public class Attribute {
        private final boolean idField;
        private Field field;
        private String column;
        private Method getMethod;
        private Method setMethod;
        private DataPermission dataPermission;
        private ColumnLambda<?, ?> columnLambda;

        public Attribute(Field field) {
            Id idAnnotation = field.getAnnotation(Id.class);
            this.idField = idAnnotation != null;

            this.field = field;
            Class<?> clazz = field.getDeclaringClass();
            String name = field.getName();

            Column columnAnnotation = field.getAnnotation(Column.class);
            if (columnAnnotation == null) {
                column = name;
            } else {
                column = columnAnnotation.name();
            }

            try {
                this.getMethod = clazz.getMethod("get" + name.substring(0, 1).toUpperCase(Locale.ENGLISH) + name.substring(1));
                this.setMethod = clazz.getMethod("set" + name.substring(0, 1).toUpperCase(Locale.ENGLISH) + name.substring(1), field.getType());
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }

            this.dataPermission = field.getAnnotation(DataPermission.class);
            this.columnLambda = new ColumnLambda<Object, Object>() {
                @Override
                public Object apply(Object o) {
                    try {
                        return getMethod.invoke(o);
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException("get value error");
                    }
                }

                @Override
                public String getMethodName() {
                    return getMethod.getName();
                }

                @Override
                public String getFieldName() {
                    return field.getName();
                }
            };

            attributes.put(field.getName(), this);
            attributes.put(getMethod.getName(), this);
            attributes.put(setMethod.getName(), this);
            attributes.put(column, this);
        }

        public Field getField() {
            return field;
        }

        public void setField(Field field) {
            this.field = field;
        }

        public String getColumn() {
            return column;
        }

        public void setColumn(String column) {
            this.column = column;
        }

        public Method getGetMethod() {
            return getMethod;
        }

        public void setGetMethod(Method getMethod) {
            this.getMethod = getMethod;
        }

        public Method getSetMethod() {
            return setMethod;
        }

        public void setSetMethod(Method setMethod) {
            this.setMethod = setMethod;
        }

        public DataPermission getDataPermission() {
            return dataPermission;
        }

        public void setDataPermission(DataPermission dataPermission) {
            this.dataPermission = dataPermission;
        }

        public ColumnLambda<?, ?> getColumnLambda() {
            return columnLambda;
        }

        public void setColumnLambda(ColumnLambda<?, ?> columnLambda) {
            this.columnLambda = columnLambda;
        }
    }

    public Object getId(T t) {
        try {
            return getIdMethod.invoke(t);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public Method getIdMethod(){
        return this.getIdMethod;
    }

    public Attribute getAttribute(String key) {
        return attributes.get(key);
    }
}
