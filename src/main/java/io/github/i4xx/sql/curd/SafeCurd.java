package io.github.i4xx.sql.curd;

import io.github.i4xx.sql.annotation.DataPermission;
import io.github.i4xx.sql.enums.Scope;
import io.github.i4xx.sql.model.Meta;

import java.util.ArrayList;
import java.util.List;

public abstract class SafeCurd {

    public SafeCurd() {
        Curd.safeCurd = this;
    }

    /**
     * 超级管理员拥有使用权限
     *
     * @return
     */
    public abstract boolean isSuperAdmin();

    /**
     * 获取登录用户ID
     *
     * @return
     */
    public abstract Object getOwner();

    /**
     * 获取组成员ID
     *
     * @return
     */
    public abstract List<Object> getGroupMembers();

    public abstract Object getDepartment();

    public abstract Object getDepartment(String domain);

    public abstract List<Object> getDepartmentAndSub();

    public abstract List<Object> getCategory(Class<?> clazz);

    public abstract List<?> getRole();

    public abstract List<Object> getMenu();

    public <T> void safe(Curd<T, ?> curd) {
        Meta<T> meta = curd.getMeta();
        List<Meta<T>.Attribute> permissionAttributes = meta.getPermissionAttributes();
        if (permissionAttributes == null || permissionAttributes.isEmpty()) return;

        if (isSuperAdmin()) return;

        for (Meta<T>.Attribute permissionAttribute : permissionAttributes) {
            safeForQuery(curd, permissionAttribute);
            safeForModify(curd, permissionAttribute);
        }
    }

    private <T> void safeForQuery(Curd<T, ?> curd, Meta<T>.Attribute permissionAttribute) {
        if (curd instanceof Insert) {
            return;
        }

        DataPermission dataPermission = permissionAttribute.getDataPermission();
        Scope[] scopes = dataPermission.query();
        if (scopes.length == 1) {
            injectForQuery(curd, permissionAttribute, scopes[0]);
        } else if (scopes.length > 1) {
            LambdaConditions.OrCondition[] orConditions = inject(curd, permissionAttribute, scopes);
            ((LambdaConditions) curd).or(orConditions);
        }

        curd.afterInject(permissionAttribute.getColumnLambda());
    }

    private <T> void safeForModify(Curd<T, ?> curd, Meta<T>.Attribute permissionAttribute) {
        if (curd instanceof LambdaSelect) {
            return;
        }

        DataPermission dataPermission = permissionAttribute.getDataPermission();
        Scope[] scopes = dataPermission.query();
        for (Scope scope : scopes) {
            injectForModify(curd, permissionAttribute, scope);
        }
    }

    public <T> void safe(Curd<T, ?> curd, List<T> list) {
        if (list == null || list.isEmpty()) {
            return;
        }

        if (isSuperAdmin()) return;

        List<Meta<T>.Attribute> permissionAttributes = curd.getMeta().getPermissionAttributes();

        if (permissionAttributes == null || permissionAttributes.isEmpty()) {
            return;
        }

        for (T t : list) {
            curd.entity = t;
            safe(curd);
        }
    }


    public <T> void injectForModify(Curd<T, ?> curd, Meta<T>.Attribute permissionAttribute, Scope scope) {
        Object value = null;
        try {
            value = permissionAttribute.getGetMethod().invoke(curd.entity);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (value == null) return;

        List<Object> values = getScopeValues(curd, scope);

        if (!values.contains(value)) {
            throw new RuntimeException("no permission");
        }
    }

    public <T> void injectForQuery(Curd<T, ?> curd, Meta<T>.Attribute permissionAttribute, Scope scope) {
        List<Object> values = getScopeValues(curd, scope);

        if (values.size() == 1) {
            ((LambdaConditions) curd).addCondition(permissionAttribute.getColumn(), SqlKeyword.EQ, values.get(0));
        } else {
            ((LambdaConditions) curd).addCondition(permissionAttribute.getColumn(), SqlKeyword.IN, values);
        }
    }

    public <T> LambdaConditions.OrCondition[] inject(Curd<T, ?> curd, Meta<T>.Attribute permissionAttribute, Scope[] scopes) {
        LambdaConditions.OrCondition[] orConditions = new LambdaConditions.OrCondition[scopes.length];
        int i = 0;
        for (Scope scope : scopes) {
            List<Object> values = getScopeValues(curd, scope);

            if (values.size() == 1) {
                orConditions[i] = condition -> condition.addCondition(permissionAttribute.getColumn(), SqlKeyword.EQ, values.get(0));
            } else {
                orConditions[i] = condition -> condition.addCondition(permissionAttribute.getColumn(), SqlKeyword.IN, values);
            }

            i++;
        }

        return orConditions;
    }

    private <T> List<Object> getScopeValues(Curd<T, ?> curd, Scope scope) {
        List<Object> values = new ArrayList<>();

        switch (scope) {
            case OWNER:
                values.add(getOwner());
                break;
            case GROUP:
                values.addAll(getGroupMembers());
                break;
            case DEPARTMENT:
                values.add(getDepartment(curd.getClazz().getSimpleName()));
                break;
            case DEPARTMENT_AND_SUB:
                values.addAll(getDepartmentAndSub());
                break;
            case CATEGORY:
                values.addAll(getCategory(curd.clazz));
                break;
            case ROLE:
                values.addAll(getRole());
                break;
        }

        if (values.isEmpty()) {
            throw new RuntimeException("no permission");
        }

        return values;
    }

}
