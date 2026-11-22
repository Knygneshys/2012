package com.example.server.model;

public abstract class GameIterator<T> {
    public abstract boolean hasNext();

    public abstract T next();
}
