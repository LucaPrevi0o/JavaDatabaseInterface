package com.lucaprevioo.jdbi.transaction;

import com.lucaprevioo.jdbi.Model;
import com.lucaprevioo.jdbi.StorageEngine;

@FunctionalInterface
public interface Action<M extends Model> { void execute(StorageEngine<M> engine); }
