import cn.misakanet.Config
import cn.misakanet.encoder.Encoder
import cn.misakanet.ui.HttpRunnerController
import cn.misakanet.ui.PluginManager
import cn.misakanet.util.RequestUtil
import groovy.swing.SwingBuilder

import javax.swing.*
import java.awt.event.KeyEvent

/**
 * 加载插件时会被调用，记得注册菜单
 */
void init() {
    println "proxy plugin init"
    def controller = HttpRunnerController.getInstance()
    def pluginManager = PluginManager.getInstance()
    def config = Config.getInstance()
    def requestUtil = RequestUtil.getInstance()

    ProxyPlugin.pluginMenu = new JMenu("Proxy")

    pluginManager.addMenu(ProxyPlugin.pluginMenu)

    var newMenuItem = new JMenuItem("SetProxy")
    ProxyPlugin.pluginMenu.add(newMenuItem)

    newMenuItem.addActionListener(event -> {
        def sb = new SwingBuilder()
        def frame = sb.frame(title: 'Proxy设置', defaultCloseOperation: WindowConstants.DISPOSE_ON_CLOSE)

        def panel = sb.panel() {
            vbox() {
                hbox() {
                    label(text: '类型:')
                    rigidArea()
                    comboBox(id: "cbProxyType", items: ["http", "https"])
                }
                rigidArea()
                hbox() {
                    label(text: 'Host:')
                    rigidArea(width: 17)
                    textField(id: 'proxyHost', preferredSize: [400, 20])

                }
                rigidArea()
                hbox() {
                    label(text: 'Port:')
                    rigidArea()
                    textField(id: 'proxyPort', preferredSize: [400, 20])
                }
                rigidArea()
                hbox() {
                    button("确定", actionPerformed: {
                        config.set("http.proxy.type", cbProxyType.getSelectedItem())
                        config.set("http.proxy.host", proxyHost.text)
                        config.set("http.proxy.port", proxyPort.text)

                        requestUtil.close()

                        frame.dispose()
                    })
                }
            }
        }

        (sb."proxyHost" as JTextField).text = config.get("http.proxy.host")
        (sb."proxyPort" as JTextField).text = config.get("http.proxy.port")
        (sb.cbProxyType as JComboBox<String>).setSelectedItem(config.get("http.proxy.type", "http"))
        controller.addUndoRedo(sb."proxyHost" as JTextField)
        controller.addUndoRedo(sb."proxyPort" as JTextField)
        panel.registerKeyboardAction(e -> frame.dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
        frame.getContentPane().add(panel)
        frame.setResizable(false)
        frame.pack()
        controller.setLocationRelativeFrame(frame)
        frame.setVisible(true)
    })

    ProxyPlugin.pluginMenu.updateUI()
}

/**
 * 卸载插件时会被调用，记得清理菜单
 */
void destroy() {
    println "proxy plugin destroy"

    var pluginManager = PluginManager.getInstance()
    pluginManager.removeMenu(ProxyPlugin.pluginMenu)
}

class ProxyPlugin {
    static JMenu pluginMenu
}