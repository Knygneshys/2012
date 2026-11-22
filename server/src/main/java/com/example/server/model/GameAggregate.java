package com.example.server.model;

public abstract class GameAggregate<T> {
    public abstract GameIterator<T> createIterator();
}
