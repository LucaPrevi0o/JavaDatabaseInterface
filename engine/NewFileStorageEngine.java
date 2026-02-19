package com.lucaprevioo.jdbi.engine;

import com.lucaprevioo.jdbi.Model;
import com.lucaprevioo.jdbi.Serializer;
import com.lucaprevioo.jdbi.StorageEngine;

import java.io.*;
import java.util.List;

public class NewFileStorageEngine<M extends Model> extends StorageEngine<M> {
    
    private final Serializer<M> serializer;
    private final String filePath;

    // i need a file to store the data
    // create operation does an append to the file
    // read operation reads the whole file and deserializes it into a list of models
    // update operation does this:
    // - reads the whole file
    // - deserializes every line
    // - for every deserialized object, checks if it is equal to the provided model
    // - if it is, it replaces it with the updated model, then it writes everything back to the file
    // - if it is not, just writes back the line
    // delete operation does this:
    // - reads the whole file
    // - deserializes every line
    // - for every deserialized object, checks if it is equal to the provided model
    // - if it is, it skips it
    // - if it is not, just writes back the line

    public NewFileStorageEngine(Serializer<M> serializer, String filePath) {

        this.serializer = serializer;
        this.filePath = filePath;

        var file = new File(filePath);
        try { if (file.createNewFile()) System.out.println("File created: " + filePath); }
        catch (IOException e) { throw new RuntimeException("Failed to create file for storage engine: " + e.getMessage(), e); }
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
