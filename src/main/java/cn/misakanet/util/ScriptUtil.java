package cn.misakanet.util;

import cn.misakanet.Config;
import cn.misakanet.UIException;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ScriptUtil {
    private static final GroovyShell groovyShell = new GroovyShell();
    private static final Map<String, Script> scriptCache = new ConcurrentHashMap<>();
    private static ScriptUtil scriptUtil;
    private final File scriptFile = new File("./script/script.groovy");
    private final Config config = Config.getInstance();
    private File extCPDir;

    private ScriptUtil() {
        setExtCP(new File("./libs"));
        // 加载额外的class path
        String extCPPath = config.get(Config.EXT_CP);
        if (extCPPath != null) {
            File extCPDir = new File(extCPPath);
            if (extCPDir.exists()) {
                setExtCP(extCPDir);
            } else {
                config.remove(Config.EXT_CP);
            }
        }
    }

    public synchronized static ScriptUtil getInstance() {
        if (scriptUtil == null) {
            scriptUtil = new ScriptUtil();
            groovyShell.parse("println(\"init groovy script\")").run();
        }
        return scriptUtil;
    }


    /**
     * 设置额外的class path
     *
     * @param dir class path路径
     */
    public void setExtCP(File dir) {
        extCPDir = dir;
        try {
            groovyShell.getClassLoader().addURL(dir.toURI().toURL());

            File[] childFiles = extCPDir.listFiles();
            if (childFiles == null) {
                return;
            }
            for (File cpFile : childFiles) {
                if (cpFile.getName().endsWith(".jar")) {
                    groovyShell.getClassLoader().addURL(cpFile.toURI().toURL());
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public void clearCache() {
        scriptCache.clear();
        System.out.println("clear cache done");
    }

    public <T> T execScript(File file, Map<String, Object> props) throws IOException {
        Script script = getScript(file);
        if (props != null) {
            props.keySet().forEach(key -> script.setProperty(key, props.get(key)));
        }
        return (T) script.run();
    }

    public <T> T execScript(File file, String method, Object... args) throws IOException {
        Script script = getScript(file);
        return (T) script.invokeMethod(method, args);
    }

    public <T> T execScript(String scriptPath, Map<String, Object> props) throws IOException {
        Script script = getScript(scriptPath);
        if (props != null) {
            props.keySet().forEach(key -> script.setProperty(key, props.get(key)));
        }
        return (T) script.run();
    }

    public <T> T execScript(String scriptPath, String method, Object... args) throws IOException {
        Script script = getScript(scriptPath);
        return (T) script.invokeMethod(method, args);
    }

    private Script getScript(File file) throws IOException {
        if (!file.exists()) {
            throw new UIException("can`t find script: " + file.getAbsolutePath());
        }
        Script script;
        if (scriptCache.containsKey(file.getAbsolutePath())) {
            script = scriptCache.get(file.getAbsolutePath());
        } else {
            System.out.println("load script " + file.getAbsolutePath());
            script = groovyShell.parse(file);
            scriptCache.put(file.getAbsolutePath(), script);
        }
        return script;
    }

    private Script getScript(String scriptPath) throws IOException {
        Object scriptData = execScript(scriptFile, "getScript", scriptPath);
        if (scriptData == null) {
            throw new UIException("script data is null!!!");
        }
        if (scriptData instanceof File) {
            File scriptFile = (File) scriptData;
            if (!scriptFile.exists()) {
                throw new UIException("can`t find script: " + scriptFile.getAbsolutePath());
            }
            return parse(scriptPath, scriptFile);
        } else if (scriptData instanceof String) {
            String scriptStr = (String) scriptData;
            if (StringUtils.isBlank(scriptStr)) {
                throw new UIException("script is blank");
            }
            return parse(scriptPath, scriptStr);
        } else if (scriptData instanceof Reader) {
            Reader reader = (Reader) scriptData;
            return parse(scriptPath, reader);
        }
        throw new UIException("script data not support: " + scriptData.getClass().getName());
    }

    private Script parse(String scriptPath, File file) throws IOException {
        if (scriptCache.containsKey(scriptPath)) {
            return scriptCache.get(scriptPath);
        } else {
            System.out.println("load script " + file.getAbsolutePath());
            Script script = groovyShell.parse(file);
            scriptCache.put(scriptPath, script);
            return script;
        }
    }

    private Script parse(String scriptPath, String scriptStr) {
        if (scriptCache.containsKey(scriptPath)) {
            return scriptCache.get(scriptPath);
        } else {
            System.out.println("load script " + scriptPath);
            Script script = groovyShell.parse(scriptStr);
            scriptCache.put(scriptPath, script);
            return script;
        }
    }

    private Script parse(String scriptPath, Reader reader) {
        if (scriptCache.containsKey(scriptPath)) {
            return scriptCache.get(scriptPath);
        } else {
            System.out.println("load script " + scriptPath);
            Script script = groovyShell.parse(reader);
            scriptCache.put(scriptPath, script);
            return script;
        }
    }
}
