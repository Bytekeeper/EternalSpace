package org.bk;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;

import java.util.Arrays;
import java.util.Iterator;

/**
 * Created by dante on 27.11.2016.
 */
public class PooledArray<T> extends Array<T> {
    private final Class<T> elementType;
    private Array.ArrayIterable<T> iterable;

    protected PooledArray() {
        elementType = null;
    }

    public PooledArray(boolean ordered, int capacity, Class elementType) {
        super(ordered, capacity, elementType);
        this.elementType = elementType;
    }

    public PooledArray(Class elementType) {
        super(elementType);
        this.elementType = elementType;
    }

    public T add() {
        T element = Pools.obtain(elementType);
        super.add(element);
        return element;
    }

    @Override
    public void add(T value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addAll(T[] array, int start, int count) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T removeIndex(int index) {
        T removed = super.removeIndex(index);
        if (removed != null) {
            Pools.free(removed);
        }
        return removed;
    }

    @Override
    public void clear() {
        Pools.freeAll(this);
        super.clear();
    }
}
