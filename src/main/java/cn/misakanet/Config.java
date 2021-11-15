package cn.misakanet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {
    public final static String THEME = "ui.theme";
    public final static String ENV = "ui.env";

    public final static String EXT_CP = "cp.ext";

    private static Config config;
    private final File file = new File("setting.properties");
    private final Properties props;

    private Config() throws IOException {
        if (!file.exists()) {
            file.createNewFile();
        }
        props = new Properties();
        props.load(new FileInputStream(file));
    }

    public static Config getInstance() {
        if (config == null) {
            try {
                config = new Config();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return config;
    }

    public String get(String name) {
        return props.getProperty(name);
    }

    public String get(String name, String defVal) {
        return props.getProperty(name, defVal);
    }

    public void set(String name, String val) {
        props.setProperty(name, val);
        try {
            props.store(new FileOutputStream(file), null);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public void remove(String name) {
        props.remove(name);
        try {
            props.store(new FileOutputStream(file), null);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
