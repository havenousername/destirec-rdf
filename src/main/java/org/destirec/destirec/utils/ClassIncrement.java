package org.destirec.destirec.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ClassIncrement {
    private final Map<String, AtomicInteger> increments;
    private static ClassIncrement instance;

    private ClassIncrement() {
        increments = new HashMap<>();
    }

    public static ClassIncrement getInstance() {
        if (instance == null) {
            instance = new ClassIncrement();
        }

        return instance;
    }

    public AtomicInteger getIncrement(Object object) {
        return increments.get(object.getClass().getName());
    }

    public void addClass(Object object) {
        String className = object.getClass().getName();
        if (!increments.containsKey(className)) {
            increments.put(className, new AtomicInteger());
        }
    }
}
