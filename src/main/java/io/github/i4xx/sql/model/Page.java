package io.github.i4xx.sql.model;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Page<T> {

    private List<T> list;
    private Integer total;

    public Page(Integer total) {
        this.total = total;
    }

    public Page(List<T> list, Integer total) {
        this.list = list;
        this.total = total;
    }

    public <R> Page<R> convert(Function<T, R> function) {
        Page<R> page = new Page<>(total);

        if (list != null) {
            page.list = list.stream().map(function).collect(Collectors.toList());
        }

        return page;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }
}
