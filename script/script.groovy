import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeNode
import javax.swing.tree.TreePath

/**
 * 加载script列表
 * @param target
 * @param rootName
 * @param scriptDir
 */
void loadScriptList(JTree target, String rootName, String scriptLocation) {
    File scriptDir = new File(scriptLocation)
    if (!scriptDir.exists()) {
        System.err.println("script folder not exist: ${scriptDir.getAbsolutePath()}")
    }
    target.setSelectionRow(-1)
    DefaultMutableTreeNode data = dirToTreeNode(scriptDir, rootName)
    var model = new DefaultTreeModel(data)
    target.setModel(model)
    println("load ${rootName} done")
}

/**
 * 目录转TreeNode，此方法只作为脚本内部用
 * @param dir
 * @param rootName
 * @return
 */
private DefaultMutableTreeNode dirToTreeNode(File dir, String rootName) {
    DefaultMutableTreeNode data = new DefaultMutableTreeNode(rootName)
    for (File file : dir.listFiles()) {
        if (file.isFile()) {
            var node = new DefaultMutableTreeNode(file.getName())
            data.add(node)
        } else {
            data.add(dirToTreeNode(file, file.getName()))
        }
    }
    return data
}

/**
 * 使用TreeNode获取脚本路径
 * @param node
 * @param prefix
 * @return
 */
String getScriptPath(DefaultMutableTreeNode node, String prefix) {
    var path = node.getPath()
    StringBuilder filePath = new StringBuilder()
    for (int i = 1; i < path.length; i++) {
        if (i != 1) {
            filePath.append("/")
        }
        filePath.append(path[i].toString())
    }
    if (prefix == null) {
        return filePath.toString()
    } else {
        return prefix + filePath.toString()
    }
}

/**
 * 使用脚本路径获取脚本
 * @param scriptPath
 * @return 可以为File,String,Reader
 */
File getScript(String scriptPath) {
    return new File(scriptPath)
}

/**
 * 设置script树，UI初始化时调用
 * @param target
 * @param scriptPath
 */
void setScriptTree(JTree target, String scriptPath) {
    var model = (DefaultTreeModel) target.getModel()
    var targetTreeNode = getTreeNode((DefaultMutableTreeNode) model.getRoot(), scriptPath)
    if (targetTreeNode != null) {
        var treeNode = model.getPathToRoot(targetTreeNode)
        var path = new TreePath(treeNode)
        target.setSelectionPath(path)
    }
}

private TreeNode getTreeNode(DefaultMutableTreeNode target, String path) {
    String[] pathArray = path.split("/")
    var root = target
    for (var i = 0; i < pathArray.length; i++) {
        for (var j = 0; j < root.getChildCount(); j++) {
            var child = (DefaultMutableTreeNode) root.getChildAt(j)
            if (child.getUserObject().equals(pathArray[i])) {
                if (i == pathArray.length - 1) {
                    return child
                } else {
                    i++
                    j = -1
                    root = child
                }
            }
        }
    }
    return null
}