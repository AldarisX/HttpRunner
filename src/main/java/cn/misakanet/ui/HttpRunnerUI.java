package cn.misakanet.ui;

import cn.misakanet.Config;
import cn.misakanet.UIConsoleOutputStream;
import cn.misakanet.util.ScriptUtil;
import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.google.gson.Gson;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class HttpRunnerUI {
    private static final Config config = Config.getInstance();
    private static ScriptUtil scriptUtil;
    private static PluginManager pluginManager;
    private static HttpRunnerController controller;
    private final File dataDir = new File("./data/");
    protected JFrame frame;
    protected JPanel rootPanel;
    protected JTextField tfURL;
    protected JButton btnSend;
    protected JTextArea taData;
    protected JTextArea taResult;
    protected JTree jtBefore;
    protected JTree jtAfter;
    protected JTextPane tpConsole;
    protected JButton btnClearScriptCache;
    protected JComboBox<String> cbMethod;
    protected JComboBox<String> cbEnv;
    protected JButton btnScriptReload;
    protected JButton btnLoad;
    protected JButton btnSave;
    protected JMenuBar menu;
    protected JMenu menuFile;
    protected JMenu menuScript;
    protected JMenu menuConsole;
    protected JMenu menuMode;
    protected JMenuItem menuFileLoad;
    protected JMenuItem menuFileSave;
    protected JMenuItem menuScriptReload;
    protected JMenuItem menuScriptClean;
    protected JMenuItem menuConsoleClear;
    protected JMenuItem menuEnvReload;
    protected JMenu menuTheme;
    protected JMenuItem menuThemeIntelliJ;
    protected JMenuItem menuThemeLight;
    protected JMenuItem menuThemeDarcula;
    protected JMenuItem menuThemeDark;
    protected JMenuItem menuThemeSys;
    protected JMenu menuAbout;
    protected JMenuItem menuAboutInfo;
    protected JMenuItem menuAboutSource;
    protected JMenuItem menuModeShell;
    protected JMenuItem menuModeCTJ;
    protected JMenuItem menuModeExtCP;
    protected JMenu menuPlugins;
    protected JMenu menuPlugin;
    protected JMenuItem menuPluginReload;
    protected JMenuItem menuPluginUnload;
    private UIConsoleOutputStream sysOut;
    private UIConsoleOutputStream errOut;
    private Gson gson;
    private Gson gsonPretty;

    public static void main(String[] args) {
        long start = System.currentTimeMillis();

        try {
            String theme = config.get(Config.THEME);
            UIManager.setLookAndFeel(Objects.requireNonNullElseGet(theme, UIManager::getSystemLookAndFeelClassName));
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                ex.printStackTrace();
            }
        }

        var ui = new HttpRunnerUI();

        ui.frame = new JFrame("HttpRunner");
        ui.frame.setContentPane(ui.rootPanel);
        ui.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ui.frame.setJMenuBar(ui.menu);
        ui.frame.pack();
        ui.frame.setLocationRelativeTo(null);
        ui.frame.getRootPane().setDefaultButton(ui.btnSend);

        ui.frame.setVisible(true);

        long end = System.currentTimeMillis();
        System.out.println("startup in " + (end - start) + "ms");
    }

    // 系统输入输出重定向
    private void sysOut() {
        sysOut = new UIConsoleOutputStream(tpConsole, UIManager.getColor("Label.foreground"), Color.LIGHT_GRAY, System.out);
        errOut = new UIConsoleOutputStream(tpConsole, UIManager.getColor("Actions.Red"), Color.RED, System.err);
        System.setOut(new PrintStream(sysOut, true));
        System.setErr(new PrintStream(errOut, true));

        System.out.println("init");
    }

    private void btnSaveClick(ActionEvent event) {
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        JFileChooser chooser = new JFileChooser(dataDir);
        chooser.setDialogTitle("保存当前配置");
        chooser.setApproveButtonText("保存");

        FileNameExtensionFilter filter = new FileNameExtensionFilter("json配置文件 (*.json)", "json");
        chooser.addChoosableFileFilter(filter);
        chooser.setFileFilter(filter);
        if (chooser.showOpenDialog(rootPanel) == JFileChooser.APPROVE_OPTION) {
            controller.saveData(chooser.getSelectedFile());
        }
    }

    private void btnLoadClick(ActionEvent event) {
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        JFileChooser chooser = new JFileChooser(dataDir);
        chooser.setDialogTitle("加载一个配置");
        chooser.setApproveButtonText("加载");

        FileNameExtensionFilter filter = new FileNameExtensionFilter("json配置文件 (*.json)", "json");
        chooser.addChoosableFileFilter(filter);
        chooser.setFileFilter(filter);

        if (chooser.showOpenDialog(rootPanel) == JFileChooser.APPROVE_OPTION) {
            controller.loadData(chooser.getSelectedFile());
        }
    }

    private void cbEnvClick(ItemEvent event) {
        if (event.getStateChange() == ItemEvent.SELECTED) {
            config.set(Config.ENV, (String) event.getItem());
        }
    }

    private <T extends LookAndFeel> void changeTheme(Class<T> theme) {
        String themeName = null;
        if (theme != null) {
            themeName = theme.getName();
            config.set(Config.THEME, themeName);
        } else {
            config.remove(Config.THEME);
        }

        try {
            if (theme != null) {
                UIManager.setLookAndFeel(themeName);
            } else {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }

            sysOut.setColor(UIManager.getColor("Label.foreground"), Color.LIGHT_GRAY);
            errOut.setColor(UIManager.getColor("Actions.Red"), Color.RED);

            SwingUtilities.updateComponentTreeUI(frame);
        } catch (UnsupportedLookAndFeelException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void initData() {
        cbMethod.addItem("POST");
        cbMethod.addItem("GET");

        controller.reloadEnv();
    }

    private void initBtnEvent() {
        // 注册菜单
        menuFileSave.addActionListener(this::btnSaveClick);
        menuFileLoad.addActionListener(this::btnLoadClick);
        menuEnvReload.addActionListener(event -> controller.reloadEnv());
        menuScriptClean.addActionListener(event -> scriptUtil.clearCache());
        menuScriptReload.addActionListener(event -> controller.reloadScript());
        menuConsoleClear.addActionListener(event -> controller.clearConsole());
        menuPluginUnload.addActionListener(event -> CompletableFuture.runAsync(() -> pluginManager.unloadPlugin()));
        menuPluginReload.addActionListener(event -> CompletableFuture.runAsync(() -> {
            pluginManager.unloadPlugin();
            pluginManager.loadPlugin();
        }));
        menuModeShell.addActionListener(event -> controller.execScript());
        menuModeExtCP.addActionListener(event -> controller.changeCP());
        menuModeCTJ.addActionListener(event -> {
            var clazzGet = new ClassGetUI(frame);
            var clazz = clazzGet.getVal();
            if (clazz == null) {
                return;
            }
            try {
                var obj = clazz.getConstructor().newInstance();
                for (Field field : clazz.getDeclaredFields()) {
                    if (Modifier.isFinal(field.getModifiers())) {
                        continue;
                    }
                    field.setAccessible(true);
                    var fieldClazz = field.getType();
                    if (fieldClazz == String.class) {
                        field.set(obj, field.getName());
                    } else if (fieldClazz == Boolean.class) {
                        field.set(obj, false);
                    } else if (fieldClazz == Integer.class) {
                        field.set(obj, 0);
                    } else if (fieldClazz == Long.class) {
                        field.set(obj, 0L);
                    } else if (fieldClazz == Double.class) {
                        field.set(obj, 0D);
                    } else if (fieldClazz == BigDecimal.class) {
                        field.set(obj, BigDecimal.ZERO);
                    }
                }
                taData.setText(gsonPretty.toJson(obj));
            } catch (NoSuchMethodException e) {
                System.err.println("NoSuchMethod: " + e.getMessage());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        });

        // 注册按钮
        btnSave.addActionListener(this::btnSaveClick);
        btnLoad.addActionListener(this::btnLoadClick);
        btnClearScriptCache.addActionListener(event -> scriptUtil.clearCache());
        btnScriptReload.addActionListener(event -> controller.reloadScript());
        btnSend.addActionListener(event -> {
            btnSend.setEnabled(false);
            CompletableFuture.runAsync(() -> controller.send());
        });

        // 注册主题
        menuThemeIntelliJ.addActionListener(event -> changeTheme(FlatIntelliJLaf.class));
        menuThemeLight.addActionListener(event -> changeTheme(FlatLightLaf.class));
        menuThemeDarcula.addActionListener(event -> changeTheme(FlatDarculaLaf.class));
        menuThemeDark.addActionListener(event -> changeTheme(FlatDarkLaf.class));
        menuThemeSys.addActionListener(event -> changeTheme(null));

        // 下拉事件
        cbEnv.addItemListener(this::cbEnvClick);
    }

    public HttpRunnerUI() {
        // 重定向控制台输出
        sysOut();

        // 异步初始化
        CompletableFuture.runAsync(() -> {
            gson = new Gson();
            gsonPretty = gson.newBuilder().setPrettyPrinting().create();

            // 初始为null
            jtBefore.setModel(null);
            jtAfter.setModel(null);

            // 初始化脚本引擎,速度很慢
            scriptUtil = ScriptUtil.getInstance();
            controller = HttpRunnerController.getInstance();
            controller.setUI(this);

            // 初始化UI事件与数据
            CompletableFuture.runAsync(() -> {
                // 初始化按钮事件
                initBtnEvent();
                // 初始化数据
                initData();
            });
            CompletableFuture.runAsync(() -> controller.reloadScript());

            // 加载插件
            CompletableFuture.runAsync(() -> {
                pluginManager = PluginManager.getInstance();
                pluginManager.setUI(this);
                pluginManager.loadPlugin();
            });

            // 非关键的ui操作
            CompletableFuture.runAsync(() -> {
                taResult.setCursor(new Cursor(Cursor.TEXT_CURSOR));
                taResult.addFocusListener(new FocusListener() {
                    @Override
                    public void focusGained(FocusEvent e) {
                        taResult.getCaret().setVisible(true);
                    }

                    @Override
                    public void focusLost(FocusEvent e) {
                        taResult.getCaret().setVisible(true);
                    }
                });

                // 控制台的光标设置
                tpConsole.setCursor(new Cursor(Cursor.TEXT_CURSOR));
                tpConsole.addFocusListener(new FocusListener() {
                    @Override
                    public void focusGained(FocusEvent e) {
                        tpConsole.getCaret().setVisible(true);
                    }

                    @Override
                    public void focusLost(FocusEvent e) {
                        tpConsole.getCaret().setVisible(true);
                    }
                });

                controller.addUndoRedo(tfURL);
                controller.addUndoRedo(taData);
            });

        });
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        rootPanel = new JPanel();
        rootPanel.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.setMinimumSize(new Dimension(960, 540));
        rootPanel.setPreferredSize(new Dimension(1280, 720));
        menu = new JMenuBar();
        menu.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        rootPanel.add(menu, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        menuFile = new JMenu();
        menuFile.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        menuFile.setText("文件");
        menu.add(menuFile);
        menuFile.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        menuFileLoad = new JMenuItem();
        menuFileLoad.setEnabled(true);
        menuFileLoad.setText("加载配置");
        menuFile.add(menuFileLoad);
        menuFileSave = new JMenuItem();
        menuFileSave.setEnabled(true);
        menuFileSave.setText("保存配置");
        menuFile.add(menuFileSave);
        menuEnvReload = new JMenuItem();
        menuEnvReload.setText("重载环境");
        menuFile.add(menuEnvReload);
        menuScript = new JMenu();
        menuScript.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        menuScript.setText("脚本");
        menu.add(menuScript);
        menuScript.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        menuScriptReload = new JMenuItem();
        menuScriptReload.setText("列表重载");
        menuScript.add(menuScriptReload);
        menuScriptClean = new JMenuItem();
        menuScriptClean.setText("清空缓存");
        menuScript.add(menuScriptClean);
        menuMode = new JMenu();
        menuMode.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        menuMode.setText("Mode");
        menu.add(menuMode);
        menuMode.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        menuModeShell = new JMenuItem();
        menuModeShell.setText("执行脚本");
        menuMode.add(menuModeShell);
        menuModeCTJ = new JMenuItem();
        menuModeCTJ.setText("类转Json");
        menuMode.add(menuModeCTJ);
        menuModeExtCP = new JMenuItem();
        menuModeExtCP.setEnabled(true);
        menuModeExtCP.setText("额外脚本ClassPath");
        menuMode.add(menuModeExtCP);
        menuAbout = new JMenu();
        menuAbout.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        menuAbout.setSelected(false);
        menuAbout.setText("关于");
        menuAbout.setVerticalAlignment(1);
        menuAbout.setVerticalTextPosition(0);
        menu.add(menuAbout);
        menuAbout.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        menuAboutInfo = new JMenuItem();
        menuAboutInfo.setText("关于");
        menuAbout.add(menuAboutInfo);
        menuAboutSource = new JMenuItem();
        menuAboutSource.setText("Github");
        menuAbout.add(menuAboutSource);
        menuConsole = new JMenu();
        menuConsole.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        menuConsole.setText("Console");
        menu.add(menuConsole);
        menuConsole.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        menuConsoleClear = new JMenuItem();
        menuConsoleClear.setText("清空控制台");
        menuConsole.add(menuConsoleClear);
        menuTheme = new JMenu();
        menuTheme.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        menuTheme.setText("主题");
        menuConsole.add(menuTheme);
        menuThemeIntelliJ = new JMenuItem();
        menuThemeIntelliJ.setText("IntelliJLaf");
        menuTheme.add(menuThemeIntelliJ);
        menuThemeLight = new JMenuItem();
        menuThemeLight.setText("LightLaf");
        menuTheme.add(menuThemeLight);
        menuThemeDarcula = new JMenuItem();
        menuThemeDarcula.setText("DarculaLaf");
        menuTheme.add(menuThemeDarcula);
        menuThemeDark = new JMenuItem();
        menuThemeDark.setText("DarkLaf");
        menuTheme.add(menuThemeDark);
        menuThemeSys = new JMenuItem();
        menuThemeSys.setText("System");
        menuTheme.add(menuThemeSys);
        menuPlugin = new JMenu();
        menuPlugin.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        menuPlugin.setText("插件");
        menuConsole.add(menuPlugin);
        menuPluginReload = new JMenuItem();
        menuPluginReload.setText("重载");
        menuPlugin.add(menuPluginReload);
        menuPluginUnload = new JMenuItem();
        menuPluginUnload.setText("卸载");
        menuPlugin.add(menuPluginUnload);
        menuPlugins = new JMenu();
        menuPlugins.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        menuPlugins.setText("Plugins");
        menu.add(menuPlugins);
        menuPlugins.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(4, 1, new Insets(6, 6, 6, 6), -1, -1));
        rootPanel.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tfURL = new JTextField();
        tfURL.putClientProperty("html.disable", Boolean.TRUE);
        panel2.add(tfURL, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, -1), null, 0, false));
        btnSend = new JButton();
        btnSend.setText("Send");
        panel2.add(btnSend, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cbMethod = new JComboBox();
        panel2.add(cbMethod, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(80, -1), new Dimension(80, -1), 0, false));
        cbEnv = new JComboBox();
        panel2.add(cbEnv, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(110, -1), new Dimension(150, -1), 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 5, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        btnClearScriptCache = new JButton();
        btnClearScriptCache.setText("清空脚本缓存");
        panel3.add(btnClearScriptCache, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel3.add(spacer1, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        btnSave = new JButton();
        btnSave.setEnabled(true);
        btnSave.setText("保存");
        panel3.add(btnSave, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        btnLoad = new JButton();
        btnLoad.setEnabled(true);
        btnLoad.setText("加载");
        panel3.add(btnLoad, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        btnScriptReload = new JButton();
        btnScriptReload.setText("刷新脚本列表");
        panel3.add(btnScriptReload, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(2, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel4, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(-1, 200), null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel4.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(250, -1), new Dimension(250, -1), 0, false));
        jtBefore = new JTree();
        scrollPane1.setViewportView(jtBefore);
        final JScrollPane scrollPane2 = new JScrollPane();
        panel4.add(scrollPane2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(250, -1), new Dimension(250, -1), 0, false));
        jtAfter = new JTree();
        scrollPane2.setViewportView(jtAfter);
        final JScrollPane scrollPane3 = new JScrollPane();
        scrollPane3.setAutoscrolls(true);
        scrollPane3.setVerifyInputWhenFocusTarget(true);
        scrollPane3.setVerticalScrollBarPolicy(22);
        panel4.add(scrollPane3, new GridConstraints(0, 1, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(500, -1), null, 0, false));
        taData = new JTextArea();
        taData.putClientProperty("html.disable", Boolean.TRUE);
        scrollPane3.setViewportView(taData);
        final JScrollPane scrollPane4 = new JScrollPane();
        scrollPane4.setAutoscrolls(true);
        scrollPane4.setVerticalScrollBarPolicy(22);
        panel4.add(scrollPane4, new GridConstraints(0, 2, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, new Dimension(500, -1), null, 0, false));
        taResult = new JTextArea();
        taResult.setEditable(false);
        scrollPane4.setViewportView(taResult);
        final JScrollPane scrollPane5 = new JScrollPane();
        scrollPane5.setAutoscrolls(true);
        scrollPane5.setHorizontalScrollBarPolicy(30);
        scrollPane5.setVerticalScrollBarPolicy(22);
        panel1.add(scrollPane5, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(960, 180), null, 0, false));
        tpConsole = new JTextPane();
        tpConsole.setEditable(false);
        tpConsole.setText("");
        tpConsole.putClientProperty("charset", "");
        tpConsole.putClientProperty("html.disable", Boolean.FALSE);
        scrollPane5.setViewportView(tpConsole);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return rootPanel;
    }

}
