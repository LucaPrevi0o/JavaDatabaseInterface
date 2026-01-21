package com.lucaprevioo.jdbi;

@FunctionalInterface
public interface Action<M extends Model> { void execute(StorageEngine<M> engine); }
