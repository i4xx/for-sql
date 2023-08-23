package io.github.i4xx.sql.model;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.util.function.Function;

@FunctionalInterface
public interface ColumnLambda<T, R> extends Function<T, R>, Serializable {

    default String getFieldName() {
        String methodName = getMethodName();
        return methodName.substring(3, 4).toLowerCase() + methodName.substring(4);
    }

    default String getMethodName() {
        return getSerializedLambda().getImplMethodName();
    }

    default String getImplClassName() {
        return getSerializedLambda().getImplClass().replace('/', '.');
    }

    default Class<T> getImplClass() {
        try {
            return (Class<T>) Class.forName(getImplClassName());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("getImplClass error");
        }
    }

    default SerializedLambda getSerializedLambda() {
        try {
            Method method = getClass().getDeclaredMethod("writeReplace");
            AccessibleObject accessibleObject = (AccessibleObject) AccessController.doPrivileged(new SetAccessibleAction(method));
            return (SerializedLambda) ((Method) accessibleObject).invoke(this);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("getSerializedLambda error");
        }
    }
}
