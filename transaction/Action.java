package com.lucaprevioo.jdbi.transaction;

import com.lucaprevioo.jdbi.Model;
import com.lucaprevioo.jdbi.StorageEngine;

@FunctionalInterface
public interface Action<M extends Model, S extends StorageEngine<M>> { <FK extends Model, FKS extends StorageEngine<FK>> void execute(S engine, FKS foreignKeyEngine); }
