package org.destirec.destirec.utils;

import org.destirec.destirec.rdf4j.interfaces.ContainerVisitor;

import java.util.Iterator;
import java.util.List;

public class ValueContainer<T> implements Iterator<T> {
    private final T item;
    private final List<T> items;

    private int index = 0;

    public ValueContainer(T item) {
        this.item = item;
        items = null;
    }

    public ValueContainer(List<T> items) {
        this.items = items;
        item = null;
    }

    public static <T> ValueContainer<T> single(T item) {
        return new ValueContainer<>(item);
    }

    public static <T> ValueContainer<T> multiple(List<T> items) {
        return new ValueContainer<>(items);
    }

    public void accept(ContainerVisitor<T> visitor) {
        if (item == null) {
            visitor.visit(items);
        } else if (items == null) {
            visitor.visit(item);
        } else {
            throw new IllegalStateException("Cannot enter this state. Items or item fields cannot be both null");
        }
    }

    private int size() {
        if (item != null) {
            return 1;
        } else if (items != null) {
            return items.size();
        } else {
            throw new IllegalStateException("Cannot enter this state. Items or item fields cannot be both null");
        }
    }

    @Override
    public boolean hasNext() {
        return size() > index;
    }

    @Override
    public T next() {
        if (item != null) {
            ++index;
            return item;
        } else if (items != null) {
            return items.get(index++);
        } else {
            throw new IllegalStateException("Cannot enter this state. Items or item fields cannot be both null");
        }
    }
}
