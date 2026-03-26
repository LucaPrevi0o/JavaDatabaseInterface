package com.lucaprevioo.jdbi.engine;

import com.lucaprevioo.jdbi.Model;
import com.lucaprevioo.jdbi.Serializer;
import com.lucaprevioo.jdbi.StorageEngine;

import java.io.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NewFileStorageEngine<M extends Model> extends StorageEngine<M> {
    
    private final Serializer<M> serializer;
    private final String filePath;

    public NewFileStorageEngine(Class<M> modelClass, Serializer<M> serializer, String filePath) {

        super(modelClass);
        this.serializer = serializer;
        this.filePath = filePath;

        var file = new File(filePath);
        try {

            var logger = Logger.getLogger(NewFileStorageEngine.class.getName());
            if (file.createNewFile()) logger.log(Level.INFO, "Created new storage file: " + filePath);
            else logger.log(Level.INFO, "Storage engine linked to existing file: " + filePath);
        } catch (IOException e) { throw new RuntimeException("Failed to create file for storage engine: " + e.getMessage(), e); }
    }

    @Override
    public void create(M model) {

        try (var writer = new BufferedWriter(new FileWriter(filePath, true))) {

            writer.write(serializer.serialize(model));
            writer.newLine();
        } catch (IOException e) { throw new RuntimeException("Failed to create model in file storage engine: " + e.getMessage(), e); }
    }

    @Override
    public List<M> read() {

        try (var reader = new BufferedReader(new FileReader(filePath))) { return reader.lines().map(serializer::deserialize).toList(); }
        catch (IOException e) { throw new RuntimeException("Failed to read models from file storage engine: " + e.getMessage(), e); }
    }

    @Override
    public void update(M model, M updatedModel) {

        var models = read();
        try (var writer = new BufferedWriter(new FileWriter(filePath))) {

            for (var m : models) {

                if (m.equals(model)) writer.write(serializer.serialize(updatedModel));
                else writer.write(serializer.serialize(m));
                writer.newLine();
            }
        } catch (IOException e) { throw new RuntimeException("Failed to update model in file storage engine: " + e.getMessage(), e); }
    }

    @Override
    public void delete(M model) {

        var models = read();
        try (var writer = new BufferedWriter(new FileWriter(filePath))) {

            for (var m : models) if (!m.equals(model)) {

                writer.write(serializer.serialize(m));
                writer.newLine();
            }
        } catch (IOException e) { throw new RuntimeException("Failed to delete model in file storage engine: " + e.getMessage(), e); }
    }
}
