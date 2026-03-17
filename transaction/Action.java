package com.lucaprevioo.jdbi.transaction;

import com.lucaprevioo.jdbi.Model;
import com.lucaprevioo.jdbi.StorageEngine;
import com.lucaprevioo.jdbi.engine.EngineRegistry;

@FunctionalInterface
public interface Action<M extends Model, S extends StorageEngine<M>> { void execute(S engine, EngineRegistry registry); }
