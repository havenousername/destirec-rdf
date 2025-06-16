package org.destirec.destirec.utils;

import lombok.Getter;
import lombok.ToString;
import org.javatuples.Pair;

@ToString
public class TupleContainer<T> {
    @Getter
    private final T item;
    @Getter
    private final Pair<T, T> items;


    public TupleContainer(T item1, T item2) {
        this.item = item1;
        this.items = new Pair<>(item1, item2);
    }
}
