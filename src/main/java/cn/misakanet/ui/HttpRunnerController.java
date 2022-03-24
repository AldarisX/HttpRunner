package cn.misakanet.ui;

import cn.misakanet.Config;
import cn.misakanet.UIException;
import cn.misakanet.util.FileUtil;
import cn.misakanet.util.RequestUtil;
import cn.misakanet.util.ScriptUtil;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.JTextComponent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class HttpRunnerController {
    private static HttpRunnerController httpRunnerController;
    private final Config config = Config.getInstance();
    private final Gson gson = new Gson();
    private final Gson gsonPretty = gson.newBuilder().setPrettyPrinting().create();
    private final ScriptUtil scriptUtil = ScriptUtil.getInstance();
    private final File envFile = new File("./script/env.groovy");
    private final File scriptFile = new File("./script/script.groovy");
    private final File scriptShell = new File("./script/shell");
    private HttpRunnerUI ui;

    private HttpRunnerController() {
    }

    public static HttpRunnerController getInstance() {
        if (httpRunnerController == null) {
            httpRunnerController = new HttpRunnerController();
        }
        return httpRunnerController;
    }

    protected void setUI(HttpRunnerUI ui) {
        this.ui = ui;
    }

    /**
     * 发送请求
     */
    public void send() {
        try {
            String url = ui.tfURL.getText();
            if (StringUtils.isBlank(url)) {
                System.err.println("URL不能为空");
                return;
            }

            RequestUtil requestUtil = RequestUtil.getInstance();
            var httpClient = requestUtil.getClient();

            var before = ui.jtBefore.getLastSelectedPathComponent();
            if (before == null) {
                System.err.println("先选择beforeScript");
                return;
            }
            String beforeScriptPath = scriptUtil.execScript(scriptFile, "getScriptPath", before, "./script/before/");
            var after = ui.jtAfter.getLastSelectedPathComponent();
            if (after == null) {
                System.err.println("先选择afterScript");
                return;
            }
            String afterScriptPath = scriptUtil.execScript(scriptFile, "getScriptPath", after, "./script/after/");


            String data = ui.taData.getText();


            System.out.println();
            ui.btnSend.setEnabled(false);
            try {
                String method = (String) ui.cbMethod.getSelectedItem();
                if (method == null) {
                    throw new UIException("method is null!!!");
                }
                HttpUriRequest request;
                switch (method) {
                    case "POST":
                        request = new HttpPost();
                        break;
                    case "GET":
                        request = new HttpGet();
                        break;
                    default:
                        throw new UIException("method not support");
                }
                String contentType = (String) ui.cbContentType.getSelectedItem();

                Map<String, Object> envMap;
                String env = getCurrentEnv();
                try {
                    envMap = scriptUtil.execScript(envFile, "getEnvVal", env);
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                    return;
                }

                Map<String, Object> param = new HashMap<>();
                param.put("url", url);
                param.put("request", request);
                param.put("data", data);
                param.put("envMap", envMap);
                param.put("contentType", contentType);

                scriptUtil.execScript(beforeScriptPath, param);
                var response = httpClient.execute(request);

                param.put("response", response);
                scriptUtil.execScript(afterScriptPath, param);

                response.close();
            } catch (UIException | IOException e) {
                System.err.println(e.getMessage());
            }

        } catch (UIException | IOException e) {
            System.err.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ui.btnSend.setEnabled(true);
        }
    }

    public void setResult(String data, String type) {
        try {
            if (type.contains("json")) {
                var dataJson = gsonPretty.fromJson(data, JsonElement.class);
                ui.taResult.setText(gsonPretty.toJson(dataJson));
            }
        } catch (Exception e) {
            ui.taResult.setText(data);
        }
    }

    /**
     * 执行脚本
     */
    public void execScript() {
        if (!scriptShell.exists()) {
            scriptShell.mkdirs();
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(scriptShell);
        chooser.setDialogTitle("选择一个脚本");
        chooser.setApproveButtonText("执行");

        FileNameExtensionFilter filter = new FileNameExtensionFilter("Groovy (*.groovy)", "groovy");
        chooser.addChoosableFileFilter(filter);
        chooser.setFileFilter(filter);
        if (chooser.showOpenDialog(ui.rootPanel) == JFileChooser.APPROVE_OPTION) {
            CompletableFuture.runAsync(() -> {
                try {
                    scriptUtil.execScript(chooser.getSelectedFile(), null);
                } catch (UIException | IOException e) {
                    System.err.println(e.getMessage());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    /**
     * 重载环境
     */
    public void reloadEnv() {
        try {
            ui.cbEnv.removeAllItems();

            List<String> envList = scriptUtil.execScript(envFile, "getEnvList");
            envList.forEach(env -> ui.cbEnv.addItem(env));

            var env = config.get(Config.ENV);
            if (StringUtils.isNotBlank(env)) {
                ui.cbEnv.setSelectedItem(env);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 重载脚本
     */
    public void reloadScript() {
        try {
            scriptUtil.execScript(scriptFile, "loadScriptList", ui.jtBefore, "before", "./script/before");
            scriptUtil.execScript(scriptFile, "loadScriptList", ui.jtAfter, "after", "./script/after");
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * 保存配置
     *
     * @param file
     */
    public void saveData(File file) {
        try {
            String ext = FileUtil.getExt(file.getPath());
            if (ext == null) {
                file = new File(file.getAbsolutePath() + ".json");
            }

            System.out.println("保存配置: " + file.getAbsolutePath());

            String url = ui.tfURL.getText();
            if (StringUtils.isBlank(url)) {
                System.err.println("URL不能为空");
            }

            String method = (String) ui.cbMethod.getSelectedItem();
            String contentType = (String) ui.cbContentType.getSelectedItem();
            String data = ui.taData.getText();
            //组装参数
            JsonObject configData = new JsonObject();
            configData.addProperty("url", url);
            configData.addProperty("method", method);
            configData.addProperty("contentType", contentType);
            if (data != null) {
                if (contentType.equals("json")) {
                    configData.add("data", gson.fromJson(data, JsonObject.class));
                } else {
                    configData.addProperty("data", Base64.getEncoder().encodeToString(data.getBytes(StandardCharsets.UTF_8)));
                }
            } else {
                configData.add("data", null);
            }
            var before = ui.jtBefore.getLastSelectedPathComponent();
            if (before == null) {
                System.err.println("先选择beforeScript");
                return;
            }
            configData.addProperty("beforeScript", (String) scriptUtil.execScript(scriptFile, "getScriptPath", before, null));
            var after = ui.jtAfter.getLastSelectedPathComponent();
            if (after == null) {
                System.err.println("先选择afterScript");
                return;
            }
            configData.addProperty("afterScript", (String) scriptUtil.execScript(scriptFile, "getScriptPath", after, null));

            if (file.exists()) {
                if (!file.delete()) {
                    System.err.println("文件删除失败");
                    return;
                }
            }

            FileUtil.save(file, configData.toString());
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.err.println("保存失败");
        }
    }

    /**
     * 加载配置
     *
     * @param file
     * @return
     */
    public Map<String, Object> loadData(File file) {
        if (!file.exists()) {
            throw new UIException("can`t find " + file.getAbsolutePath());
        }
        try {
            var configDataStr = FileUtil.load(file);
            var configData = gson.fromJson(configDataStr, JsonObject.class);
            var url = configData.get("url").getAsString();
            ui.tfURL.setText(url);
            var method = configData.get("method").getAsString();
            ui.cbMethod.setSelectedItem(method);
            var contentType = configData.get("contentType");
            if (contentType != null)
                ui.cbContentType.setSelectedItem(contentType.getAsString());
            var data = configData.get("data");
            if (data != null && !data.isJsonNull()) {
                if (contentType == null || contentType.equals("json")) {
                    ui.taData.setText(gsonPretty.toJson(data));
                } else {
                    ui.taData.setText(new String(Base64.getDecoder().decode(data.getAsString()), StandardCharsets.UTF_8));
                }
            } else {
                ui.taData.setText("");
            }
            var beforeScript = configData.get("beforeScript").getAsString();
            scriptUtil.execScript(scriptFile, "setScriptTree", ui.jtBefore, beforeScript);
            var afterScript = configData.get("afterScript").getAsString();
            scriptUtil.execScript(scriptFile, "setScriptTree", ui.jtAfter, afterScript);

            System.out.println("加载配置: " + file.getAbsolutePath());

            Map<String, Object> result = new HashMap<>();
            result.put("url", url);
            result.put("method", method);
            if (data != null)
                result.put("data", data);
            result.put("beforeScript", beforeScript);
            result.put("afterScript", afterScript);

            return result;
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.err.println("加载失败");
        }

        return null;
    }

    /**
     * 设置groovy的额外class path
     */
    public void changeCP() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("设置一个目录作为Script ClassPath");
        chooser.setApproveButtonText("设置");

        if (chooser.showOpenDialog(ui.rootPanel) == JFileChooser.APPROVE_OPTION) {
            File extCPDir = chooser.getSelectedFile();
            scriptUtil.setExtCP(extCPDir);
            scriptUtil.clearCache();
            config.set(Config.EXT_CP, extCPDir.getAbsolutePath());
        }
    }

    /**
     * 设置与主窗体位置
     *
     * @param window
     */
    public void setLocationRelativeFrame(Window window) {
        window.setLocationRelativeTo(ui.frame);
    }

    /**
     * 清空控制台
     */
    public void clearConsole() {
        ui.tpConsole.setText("");
    }

    /**
     * 获取当前环境
     *
     * @return
     */
    public String getCurrentEnv() {
        return (String) ui.cbEnv.getSelectedItem();
    }

    public void addUndoRedo(JTextComponent component) {
        UndoManager undoManager = new UndoManager();
        component.getDocument().addUndoableEditListener(e -> undoManager.addEdit(e.getEdit()));
        component.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), "Undo");
        component.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), "Redo");
        component.getActionMap().put("Undo", new AbstractAction("Undo") {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (undoManager.canUndo()) {
                        undoManager.undo();
                    }
                } catch (CannotUndoException ex) {
                    ex.printStackTrace();
                }
            }
        });
        component.getActionMap().put("Redo", new AbstractAction("Redo") {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (undoManager.canRedo()) {
                        undoManager.redo();
                    }
                } catch (CannotRedoException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
}
