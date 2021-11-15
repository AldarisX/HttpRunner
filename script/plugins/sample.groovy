import cn.misakanet.ui.PluginManager

import javax.swing.*

/**
 * 加载插件时会被调用，记得注册菜单
 */
void init() {
    println "Sample plugin init"
    var pluginManager = PluginManager.getInstance()
    SamplePlugin.pluginMenu = new JMenu("Sample")

    pluginManager.addMenu(SamplePlugin.pluginMenu)

    var newMenuItem = new JMenuItem("Sample Click")
    SamplePlugin.pluginMenu.add(newMenuItem)

    newMenuItem.addActionListener(event -> println "sample menu click")

    SamplePlugin.pluginMenu.updateUI()
}

/**
 * 卸载插件时会被调用，记得清理菜单
 */
void destroy() {
    println "sample plugin destroy"

    var pluginManager = PluginManager.getInstance()
    pluginManager.removeMenu(SamplePlugin.pluginMenu)
}

class SamplePlugin {
    static JMenu pluginMenu
}