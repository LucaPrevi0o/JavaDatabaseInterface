package com.lucaprevioo.jdbi.engine;

import com.lucaprevioo.jdbi.Model;
import com.lucaprevioo.jdbi.StorageEngine;

import java.util.HashMap;
import java.util.Map;

public class EngineRegistry {

    private final Map<Class<?>, StorageEngine<?>> engines = new HashMap<>();

    public <M extends Model, S extends StorageEngine<M>> void register(Class<M> type, S engine) { engines.put(type, engine); }

    @SuppressWarnings("unchecked")
    public <M extends Model, S extends StorageEngine<M>> S get(Class<?> type) { return (S) engines.get(type); }
}