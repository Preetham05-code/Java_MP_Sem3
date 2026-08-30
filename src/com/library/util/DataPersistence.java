package com.library.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Simple file-based persistence layer built on Java's built-in object
 * serialization. This keeps the whole project pure Java (no external
 * database or driver required) while still letting data survive between
 * runs of the program.
 */
public class DataPersistence {

    public static final String DATA_DIR = "librarydata";

    static {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    private DataPersistence() {
    }

    public static void save(String filename, Object obj) {
        File target = new File(DATA_DIR + File.separator + filename);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(target))) {
            oos.writeObject(obj);
        } catch (IOException e) {
            System.out.println("[WARN] Could not save " + filename + ": " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T load(String filename, Class<T> type) {
        File source = new File(DATA_DIR + File.separator + filename);
        if (!source.exists()) {
            return null;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(source))) {
            return (T) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("[WARN] Could not load " + filename + ": " + e.getMessage());
            return null;
        }
    }
}
