package com.mihaibojescu.solvers.branch_and_bound.util;

public class Box<T> {
    private T value;

    public Box(T value) {
        this.value = value;
    }

    public synchronized T getValue() {
        return value;
    }

    public synchronized void setValue(T value) {
        this.value = value;
    }
}
