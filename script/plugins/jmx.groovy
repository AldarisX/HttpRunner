import cn.misakanet.ui.HttpRunnerController
import cn.misakanet.ui.PluginManager
import groovy.swing.SwingBuilder
import org.apache.commons.lang3.StringUtils

import javax.management.ObjectName
import javax.management.remote.JMXConnectorFactory
import javax.management.remote.JMXServiceURL
import javax.swing.*
import java.awt.event.KeyEvent
import java.lang.management.*

void init() {
    def pluginManager = PluginManager.getInstance()
    def controller = HttpRunnerController.getInstance()
    JMXPlugin.jmxMenu = new JMenu("JMX")

    pluginManager.addMenu(JMXPlugin.jmxMenu)

    def jmxLocalMenuItem = new JMenuItem("查询当前JVM信息")
    JMXPlugin.jmxMenu.add(jmxLocalMenuItem)
    def jmxMenuItem = new JMenuItem("查询远程JMX信息")
    JMXPlugin.jmxMenu.add(jmxMenuItem)

    // 当前jvm信息事件
    jmxLocalMenuItem.addActionListener(event -> JMXPlugin.queryLocalJVMInfo())
    // 远程jvm信息事件
    jmxMenuItem.addActionListener(event -> {
        JButton btnOK = new JButton("OK")
        def sb = new SwingBuilder()
        def dialog = sb.dialog(title: '输入JMX地址', modal: true)
        def panel = sb.panel() {
            vbox() {
                hbox(preferredSize: [250, 20]) {
                    label(text: '例 127.0.0.1:8541')
                    hglue()
                }
                rigidArea()
                hbox() {
                    label(text: 'URI:')
                    rigidArea()
                    textField(id: 'jmxHost', preferredSize: [200, 20])
                    label(text: '/')
                    textField(id: 'jmxContext', text: 'jmxrmi')
                }
                rigidArea()
                hbox() {
                    hglue()
                    button(btnOK, actionPerformed: {
                        println ""
                        JMXPlugin.queryRemoteJVMInfo(jmxHost.text, jmxContext.text)
                        dialog.dispose()
                    })
                    rigidArea()
                    button('Cancel', actionPerformed: {
                        dialog.dispose()
                    })
                }
            }
        }

        panel.registerKeyboardAction(e -> dialog.dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
        dialog.getRootPane().setDefaultButton(btnOK)
        dialog.getContentPane().add(panel)
        dialog.setResizable(false)
        dialog.pack()
        controller.setLocationRelativeFrame(dialog)
        dialog.setVisible(true)
    })

    JMXPlugin.jmxMenu.updateUI()
}

void destroy() {
    def pluginManager = PluginManager.getInstance()
    pluginManager.removeMenu(JMXPlugin.jmxMenu)
}

class JMXPlugin {
    static JMenu jmxMenu

    static queryRemoteJVMInfo(String url, String context) {
        if (StringUtils.isBlank(url)) {
            return
        }
        def serverUrl = "service:jmx:rmi:///jndi/rmi://$url/$context"

        println "remote $serverUrl"

        def serverConnector = JMXConnectorFactory.connect(new JMXServiceURL(serverUrl))
        def server = serverConnector.MBeanServerConnection

        def os = ManagementFactory.newPlatformMXBeanProxy(server, ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME, OperatingSystemMXBean.class)
        try {
            println """OPERATING SYSTEM: 
\tOS architecture = $os.arch 
\tOS name = $os.name 
\tOS version = $os.version 
\tOS processors = $os.availableProcessors 
\tOS load average = $os.systemLoadAverage 
"""

            def rt = ManagementFactory.newPlatformMXBeanProxy(server, ManagementFactory.RUNTIME_MXBEAN_NAME, RuntimeMXBean.class)
            println """RUNTIME: 
   \tRuntime name = $rt.name 
   \tRuntime spec name = $rt.specName 
   \tRuntime vendor = $rt.specVendor 
   \tRuntime spec version = $rt.specVersion 
   \tRuntime management spec version = $rt.managementSpecVersion 
   """
            def mem = ManagementFactory.newPlatformMXBeanProxy(server, ManagementFactory.MEMORY_MXBEAN_NAME, MemoryMXBean.class)
            def heapUsage = mem.heapMemoryUsage
            def nonHeapUsage = mem.nonHeapMemoryUsage
            println """MEMORY: 
   HEAP STORAGE: 
      \tMemory committed = $heapUsage.committed 
      \tMemory init = $heapUsage.init 
      \tMemory max = $heapUsage.max 
      \tMemory used = $heapUsage.used 
   NON-HEAP STORAGE: 
      \tNon-heap memory committed = $nonHeapUsage.committed 
      \tNon-heap memory init = $nonHeapUsage.init 
      \tNon-heap memory max = $nonHeapUsage.max 
      \tNon-heap memory used = $nonHeapUsage.used 
   """
            println()
            for (ObjectName name : server.queryNames(new ObjectName(ManagementFactory.GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE + ",*"), null)) {
                def gc = ManagementFactory.newPlatformMXBeanProxy(server, name.getCanonicalName(), GarbageCollectorMXBean.class).each { gc ->
                    println "\tname = $gc.name"
                    println "\t\tcollection count = $gc.collectionCount"
                    println "\t\tcollection time = $gc.collectionTime"
                    String[] mpoolNames = gc.memoryPoolNames
                    mpoolNames.each {
                        mpoolName -> println "\t\tmpool name = $mpoolName"
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace()
        } finally {
            serverConnector.close()
        }
    }

    static queryLocalJVMInfo() {
        println "local jvm info"
        def os = ManagementFactory.operatingSystemMXBean
        println """OPERATING SYSTEM: 
\tOS architecture = $os.arch 
\tOS name = $os.name 
\tOS version = $os.version 
\tOS processors = $os.availableProcessors 
\tOS load average = $os.systemLoadAverage 
"""
        def rt = ManagementFactory.runtimeMXBean
        println """RUNTIME: 
   \tRuntime name = $rt.name 
   \tRuntime spec name = $rt.specName 
   \tRuntime vendor = $rt.specVendor 
   \tRuntime spec version = $rt.specVersion 
   \tRuntime management spec version = $rt.managementSpecVersion 
   """
        def mem = ManagementFactory.memoryMXBean
        def heapUsage = mem.heapMemoryUsage
        def nonHeapUsage = mem.nonHeapMemoryUsage
        println """MEMORY: 
   HEAP STORAGE: 
      \tMemory committed = $heapUsage.committed 
      \tMemory init = $heapUsage.init 
      \tMemory max = $heapUsage.max 
      \tMemory used = $heapUsage.used 
   NON-HEAP STORAGE: 
      \tNon-heap memory committed = $nonHeapUsage.committed 
      \tNon-heap memory init = $nonHeapUsage.init 
      \tNon-heap memory max = $nonHeapUsage.max 
      \tNon-heap memory used = $nonHeapUsage.used 
   """
        println "GARBAGE COLLECTION:"
        ManagementFactory.garbageCollectorMXBeans.each { gc ->
            println "\tname = $gc.name"
            println "\t\tcollection count = $gc.collectionCount"
            println "\t\tcollection time = $gc.collectionTime"
            String[] mpoolNames = gc.memoryPoolNames
            mpoolNames.each {
                mpoolName -> println "\t\tmpool name = $mpoolName"
            }
        }
    }
}