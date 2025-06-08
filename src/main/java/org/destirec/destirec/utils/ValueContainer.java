package org.destirec.destirec.utils;

import lombok.Getter;
import lombok.ToString;
import org.destirec.destirec.rdf4j.interfaces.ContainerVisitor;
import org.destirec.destirec.rdf4j.interfaces.functionalVisitors.ListVisitor;
import org.destirec.destirec.rdf4j.interfaces.functionalVisitors.SingleVisitor;

import java.util.Iterator;
import java.util.List;

@ToString
public class ValueContainer<T> implements Iterator<T> {
    @Getter
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

    public void accept(SingleVisitor<T> visitor) {
        if (item == null) {
            return;
        }
        visitor.visit(item);
    }

    public void acceptList(ListVisitor<T> visitor) {
        if (items == null) {
            return;
        }
        visitor.visit(items);
    }


    private int size() {
        if (item != null) {
            return 1;
        } else if (items != null) {
            return items.size();
        } else {
            return 0;
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
