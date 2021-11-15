import javax.swing.*
import java.nio.file.*

/**
 * 查询类
 * @param targetClazzName
 * @param target
 * @return
 */
DefaultListModel<String> queryClass(String targetClazzName, DefaultListModel<String> target) {
    String pkgName = targetClazzName
    String pkgPath = pkgName.replace(".", "/")
    GroovyClassLoader gcl = (GroovyClassLoader) getClass().getClassLoader()
    var classURIEnum = gcl.getResources(pkgPath)
    if (classURIEnum == null) {
        return new DefaultListModel<String>()
    }

    classURIEnum.asIterator().forEachRemaining(classURI -> {
        var pkg = classURI.toURI()

        Path root
        if (pkg.getScheme() == "jar") {
            try {
//                root = FileSystems.getFileSystem(pkg).getPath(pkgPath)
                root = Paths.get(pkg)
            } catch (final FileSystemNotFoundException e) {
                root = FileSystems.newFileSystem(pkg, Collections.emptyMap()).getPath(pkgPath)
            }
        } else {
            root = Paths.get(pkg)
        }

        final String extension = ".class"
        try (var allPaths = Files.walk(root)) {
            allPaths.filter(Files::isRegularFile).forEach(file -> {
                String clazzName = null
                try {
                    final String path = file.toString().replace('/', '.')
                    final String name = path.substring(path.indexOf(pkgName), path.length() - extension.length())
                    clazzName = name.replaceAll("\\\\", ".")
                    if (clazzName.contains(targetClazzName)) {
                        target.addElement(getClass().getClassLoader().loadClass(clazzName).getName())
                        if (target.size() >= 250) {
                            throw new RuntimeException("stop")
                        }
                    }
                } catch (NoClassDefFoundError | ClassNotFoundException | StringIndexOutOfBoundsException | ArrayIndexOutOfBoundsException | NullPointerException ignored) {
                    //println(e)
                } catch (ClassFormatError e) {
                    System.err.println("${e} \r\n ${clazzName}")
                }
            })
        } catch (RuntimeException e) {
            if (e.getMessage() != "stop") {
                throw e
            }
        }
    })


    return target
}

/**
 * 获取类
 * @param clazzName
 * @return
 */
Class<?> getClazz(String clazzName) {
    if (clazzName == null) {
        return null
    }
    return getClass().getClassLoader().loadClass(clazzName)
}