package net.bteuk.uk121.mod;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class Config {

    protected Properties properties;
    protected File config;

    //Default values
    public int yMin = -256;
    public int height = 2288;
    public int seaLevel = 1;

    public Config() {

        Path directory = Path.of(System.getProperty("user.dir") + "/uk121/");
        Path elevations = Path.of(System.getProperty("user.dir") + "/uk121/Elevation/");
        Path osm = Path.of(System.getProperty("user.dir") + "/uk121/Ways/");

        if (!Files.exists(directory)){
            try {
                Files.createDirectory(directory);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!Files.exists(elevations)){
            try {
                Files.createDirectory(elevations);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!Files.exists(osm)){
            try {
                Files.createDirectory(osm);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        config = directory.resolve("config.properties").toFile();
        properties = new Properties();

    }

    public void load() {

        try (InputStream stream = new FileInputStream(config)) {
            properties.load(stream);
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            e.printStackTrace();
        }

        yMin = getInt("y_min", yMin);
        height = getInt("height", height);
        seaLevel = getInt("sea_level", seaLevel);

        config.getParentFile().mkdirs();
        try (OutputStream output = new FileOutputStream(config)) {
            properties.store(output, "Don't put comments; they get removed");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    protected String getString(String key, String def) {
        if (def == null) {
            def = "";
        }
        String val = properties.getProperty(key);
        if (val == null) {
            properties.setProperty(key, def);
            return def;
        } else {
            return val;
        }
    }

    /**
     * Get a boolean value.
     *
     * @param key the key
     * @param def the default value
     * @return the value
     */
    protected boolean getBool(String key, boolean def) {
        String val = properties.getProperty(key);
        if (val == null) {
            properties.setProperty(key, def ? "true" : "false");
            return def;
        } else {
            return val.equalsIgnoreCase("true")
                    || val.equals("1");
        }
    }

    /**
     * Get an integer value.
     *
     * @param key the key
     * @param def the default value
     * @return the value
     */
    protected int getInt(String key, int def) {
        String val = properties.getProperty(key);
        if (val == null) {
            properties.setProperty(key, String.valueOf(def));
            return def;
        } else {
            try {
                return Integer.parseInt(val);
            } catch (NumberFormatException e) {
                properties.setProperty(key, String.valueOf(def));
                return def;
            }
        }
    }

    /**
     * Get a double value.
     *
     * @param key the key
     * @param def the default value
     * @return the value
     */
    protected double getDouble(String key, double def) {
        String val = properties.getProperty(key);
        if (val == null) {
            properties.setProperty(key, String.valueOf(def));
            return def;
        } else {
            try {
                return Double.parseDouble(val);
            } catch (NumberFormatException e) {
                properties.setProperty(key, String.valueOf(def));
                return def;
            }
        }
    }
}
