package io.github.i4xx.sql.curd;

import java.io.Serializable;

@FunctionalInterface
public interface ISqlSegment extends Serializable {
    String getSqlSegment();
}
