import cn.misakanet.ui.HttpRunnerController
import cn.misakanet.ui.PluginManager
import groovy.swing.SwingBuilder

import javax.swing.*
import java.awt.event.KeyEvent
import java.nio.charset.Charset

void init() {
    def pluginManager = PluginManager.getInstance()
    def controller = HttpRunnerController.getInstance()
    URLCoderPlugin.urlCoderMenu = new JMenu("URLCoder")

    pluginManager.addMenu(URLCoderPlugin.urlCoderMenu)

    var urlCoderItem = new JMenuItem("URLCoder")
    URLCoderPlugin.urlCoderMenu.add(urlCoderItem)

    urlCoderItem.addActionListener(event -> {
        def sb = new SwingBuilder()
        def frame = sb.frame(title: 'URLCoder', defaultCloseOperation: WindowConstants.DISPOSE_ON_CLOSE)

        def panel = sb.panel() {
            vbox() {
                hbox() {
                    rigidArea()
                    comboBox(id: "cbCharset", items: ["UTF-8", "GBK"])
                }
                rigidArea()
                hbox() {
                    label(text: '明文:')
                    rigidArea()
                    scrollPane(verticalScrollBarPolicy: JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, preferredSize: [400, 100]) {
                        textArea(id: 'plainText', lineWrap: true)
                    }
                }
                rigidArea()
                hbox() {
                    label(text: '编码:')
                    rigidArea()
                    scrollPane(verticalScrollBarPolicy: JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                            preferredSize: [400, 100]) {
                        textArea(id: 'encText', lineWrap: true)
                    }
                }
                rigidArea()
                hbox() {
                    button("编码", actionPerformed: {
                        def charset = Charset.forName(cbCharset.getSelectedItem())

                        encText.text = URLEncoder.encode(plainText.text, charset)
                    })
                    rigidArea()
                    button("解密", actionPerformed: {
                        def charset = Charset.forName(cbCharset.getSelectedItem())

                        plainText.text = URLDecoder.decode(encText.text, charset)
                    })
                }
            }
        }

        controller.addUndoRedo(sb."plainText" as JTextArea)
        controller.addUndoRedo(sb."encText" as JTextArea)
        panel.registerKeyboardAction(e -> frame.dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
        frame.getContentPane().add(panel)
        frame.setResizable(false)
        frame.pack()
        controller.setLocationRelativeFrame(frame)
        frame.setVisible(true)
    })
}

void destroy() {
    def pluginManager = PluginManager.getInstance()
    pluginManager.removeMenu(URLCoderPlugin.urlCoderMenu)
}

class URLCoderPlugin {
    static JMenu urlCoderMenu
}