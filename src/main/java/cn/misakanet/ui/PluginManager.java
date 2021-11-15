package cn.misakanet.ui;

import groovy.lang.GroovyShell;
import groovy.lang.Script;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PluginManager {
    private static final GroovyShell groovyShell = new GroovyShell();
    private static final Map<String, Script> scriptCache = new ConcurrentHashMap<>();
    private static PluginManager pluginManager;
    private boolean isLoad = false;
    private HttpRunnerUI ui;

    private PluginManager() {
    }

    public synchronized static PluginManager getInstance() {
        if (pluginManager == null) {
            pluginManager = new PluginManager();
        }

        return pluginManager;
    }

    protected synchronized void loadPlugin() {
        if (isLoad) {
            System.err.println("plugin already load");
            return;
        }
        isLoad = true;
        File pluginDir = new File("./script/plugins");
        System.out.println("load plugins...");
        try {
            Files.walk(Path.of(pluginDir.toURI()))
                    .filter(path -> path.toString().endsWith("groovy")).forEach(path -> {
                        try {
                            var pluginFile = path.toFile();
                            System.out.println("load plugin " + pluginFile.getName());
                            execScript(pluginFile, "init", null);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("load plugins done");
    }

    protected synchronized void unloadPlugin() {
        if (!isLoad) {
            System.err.println("plugin not load");
            return;
        }
        File pluginDir = new File("./script/plugins");
        System.out.println("unload plugins...");
        try {
            Files.walk(Path.of(pluginDir.toURI()))
                    .filter(path -> path.toString().endsWith("groovy")).forEach(path -> {
                        try {
                            var pluginFile = path.toFile();
                            System.out.println("unload plugin " + pluginFile.getName());
                            execScript(pluginFile, "destroy", null);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("unload plugins done");
        scriptCache.clear();
        isLoad = false;
    }

    public <T> T execScript(File file, String method, Object... args) throws IOException {
        Script script;
        if (scriptCache.containsKey(file.getAbsolutePath())) {
            script = scriptCache.get(file.getAbsolutePath());
        } else {
//            System.out.println("load plugin " + file.getAbsolutePath());
            script = groovyShell.parse(file);
            scriptCache.put(file.getAbsolutePath(), script);
        }
        return (T) script.invokeMethod(method, args);
    }

    public void setUI(HttpRunnerUI ui) {
        this.ui = ui;
    }

    public void addMenu(JMenu menu) {
        ui.menuPlugins.add(menu);
    }

    public void addMenuItem(JMenuItem menuItem) {
        ui.menuPlugins.add(menuItem);
    }

    public void removeMenu(JMenu menu) {
        ui.menuPlugins.remove(menu);
    }

    public void removeMenuItem(JMenuItem menuItem) {
        ui.menuPlugins.remove(menuItem);
    }
}
