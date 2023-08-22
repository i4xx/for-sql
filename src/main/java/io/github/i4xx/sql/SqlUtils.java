package io.github.i4xx.sql;

import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.Objects;

public class SqlUtils {

    public static String splice(Iterable<?> iterable, String separator) {
        Iterator<?> iterator = iterable.iterator();
        if (!iterator.hasNext()) {
            return "";
        }

        Object obj = iterator.next();
        if (!iterator.hasNext()) {
            return Objects.toString(obj, "");
        }

        StringBuilder builder = new StringBuilder(256);
        builder.append(obj);

        while (iterator.hasNext()) {
            if (separator != null) {
                builder.append(separator);
            }

            builder.append(iterator.next());
        }

        return builder.toString();


    }

    public static String inHolder(int size) {
        if (size < 1) {
            return "()";
        }

        StringBuilder builder = new StringBuilder("(");
        for (int i = 1; i < size; i++) {
            builder.append("?, ");
        }

        return builder.append("?)").toString();
    }

    public static <T> T newInstance(Class<T> clz) {
        try {
            Constructor<T> constructor = clz.getConstructor();
            return constructor.newInstance();
        } catch (Exception ignored) {
        }
        return null;
    }

}
