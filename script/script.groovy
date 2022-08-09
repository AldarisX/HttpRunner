import javax.swing.*
import java.awt.event.ActionListener

/**
 * 加载script列表
 * @param target
 * @param rootName
 * @param scriptDir
 */
void loadScriptList(JMenuItem target, String rootName, String scriptLocation, ActionListener onclick) {
    target.removeAll()
    File scriptDir = new File(scriptLocation)
    if (!scriptDir.exists()) {
        System.err.println("script folder not exist: ${scriptDir.getAbsolutePath()}")
    }

    dirToMenu(target, rootName, scriptDir, onclick)
    println("load ${rootName} done")
}

void setScriptSelect(JMenu target, JMenuItem menuItem) {
    for (def i = 0; i < target.getItemCount(); i++) {
        def item = target.getItem(i)
        if (item instanceof JMenu) {
            setScriptSelect(item, menuItem)
        } else {
            item.setSelected(false)
        }
    }
    menuItem.setSelected(true)
}

void setScriptSelect(JMenu target, File file) {
    for (def i = 0; i < target.getItemCount(); i++) {
        def item = target.getItem(i)
        if (item instanceof JMenu) {
            setScriptSelect(item, file)
        } else {
            File scriptFile = item.getClientProperty("file") as File
            if (scriptFile.getPath() == file.getPath()) {
                item.setSelected(true)
            } else {
                item.setSelected(false)
            }
        }
    }
}

/**
 * 目录转TreeNode，此方法只作为脚本内部用
 * @param dir
 * @param rootName
 * @return
 */
private void dirToMenu(JMenuItem target, String rootName, File dir, ActionListener onclick) {
    for (File file : dir.listFiles()) {
        if (file.isFile()) {
            JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(file.getName())
            menuItem.addActionListener(onclick)
            menuItem.putClientProperty("root", rootName)
            menuItem.putClientProperty("file", file)
            target.add(menuItem)
        } else {
            JMenu menu = new JMenu(file.getName())
            target.add(menu)
            dirToMenu(menu, rootName, file, onclick)
        }
    }
}