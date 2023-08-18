package com.github.ship.client.proxy.impl;

import com.github.ship.client.core.MethodInvoker;
import com.github.ship.client.proxy.AbstractClientProxyFactory;
import com.github.ship.common.exception.RpcException;
import com.github.ship.util.ClassUtils;
import com.github.ship.util.CodeGenerateUtils;
import com.github.ship.util.ReflectUtils;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.*;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URI;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;

/**
 * @author Ship
 * @version 1.0.0
 * @description:
 * @date 2023/08/17 13:53
 */
public class JdkCompilerClientProxyFactory extends AbstractClientProxyFactory {

    private static Logger logger = LoggerFactory.getLogger(JdkCompilerClientProxyFactory.class);

    private final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

    private final DiagnosticCollector<JavaFileObject> diagnosticCollector = new DiagnosticCollector<>();

    private final JavaFileManagerImpl javaFileManager;

    private final ClassLoaderImpl classLoader;

    private List<String> options;

    public JdkCompilerClientProxyFactory(MethodInvoker methodInvoker) {
        super(methodInvoker);
        options = new ArrayList<String>();
        options.add("-source");
        options.add("1.8");
        options.add("-target");
        options.add("1.8");
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnosticCollector, null, null);
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        classLoader = AccessController.doPrivileged(new PrivilegedAction<ClassLoaderImpl>() {
            @Override
            public ClassLoaderImpl run() {
                return new ClassLoaderImpl(loader);
            }
        });
        javaFileManager = new JavaFileManagerImpl(fileManager, classLoader);
    }


    @Override
    protected Object newProxyInstance(Class clazz) {
        // 实现类类名
        String fullClassName = clazz.getName() + "Impl";
        int i = fullClassName.lastIndexOf('.');
        // eg: UserServiceImpl
        String className = fullClassName.substring(i);
        String packageName = fullClassName.substring(0, i);
        String sourceCode = generateSourceCode(className, packageName, clazz);
        JavaFileObjectImpl javaFileObject = new JavaFileObjectImpl(className, sourceCode);
        javaFileManager.putFileForInput(StandardLocation.SOURCE_PATH, packageName, className + ClassUtils.JAVA_EXTENSION, javaFileObject);
        JavaCompiler.CompilationTask task = compiler.getTask(null, javaFileManager, diagnosticCollector, options, null, Lists.newArrayList(javaFileObject));
        // 编译代码
        Boolean result = task.call();
        if (result == null || !result) {
            throw new RpcException("Compilation fail. " + diagnosticCollector);
        }
        try {
            Class<?> implClass = classLoader.loadClass(fullClassName);
            Constructor<?> constructor = implClass.getConstructor(MethodInvoker.class);
            Object instance = constructor.newInstance(super.methodInvoker);
            return instance;
        } catch (ClassNotFoundException e) {
            throw new RpcException("Class [" + fullClassName + "] not find");
        } catch (Exception e) {
            throw new RpcException("New class [" + fullClassName + "] instance fail,errorMsg:" + e.getMessage());
        }
    }

    /**
     * 生成实现类源代码
     *
     * @param className
     * @param packageName
     * @param interfaceClazz
     * @return
     */
    private static String generateSourceCode(String className, String packageName, Class interfaceClazz) {
        /**
         * package com.github.ship.client.proxy.impl;
         *
         * import com.github.ship.client.core.MethodInvoker;
         *
         * public class UserServiceImpl implements UserService {
         *
         *     private MethodInvoker methodInvoker;
         *
         *     public UserServiceImpl(MethodInvoker methodInvoker) { this.methodInvoker = methodInvoker;}
         *
         *     @Override
         *     public Object getUser(Long id) {
         *         return methodInvoker.$invoke(UserService.class.getName(), "getUser", new String[]{"java.lang.Long"}, new Object[]{id}, Boolean.FALSE);
         *     }
         * }
         */
        String interfaceClazzName = className.replace("Impl", "");

        final StringBuilder sb = new StringBuilder();
        sb.append("package " + packageName + ";\n");
        sb.append("import com.github.ship.client.core.MethodInvoker;\n");
        sb.append(String.format("public class %s implements %s {", className, interfaceClazzName));
        sb.append("\n");
        sb.append("private MethodInvoker methodInvoker;");
        sb.append("\n");
        sb.append(String.format("public %s(MethodInvoker methodInvoker) { this.methodInvoker = methodInvoker;}", className));
        sb.append("\n");

        Method[] declaredMethods = interfaceClazz.getDeclaredMethods();
        for (Method method : declaredMethods) {
            fillMethodCode(interfaceClazz, sb, method);
            sb.append("\n");
        }
        sb.append("}");
        return sb.toString();
    }

//    public static void main(String[] args) {
//        String code = generateSourceCode("UserServiceImpl", "com.github.ship.client.proxy.impl", UserService.class);
//        System.out.println(code);
//    }

    /**
     * 填充方法实现代码
     *
     * @param interfaceClazz
     * @param sb
     * @param method
     */
    private static void fillMethodCode(Class interfaceClazz, StringBuilder sb, Method method) {
        sb.append("@Override\n");
        // 方法名
        String methodName = method.getName();
        // 返回值类型
        Class<?> returnType = method.getReturnType();
        sb.append(String.format("public %s %s(", returnType.getName(), methodName));
        int paramLength = method.getParameterCount();
        if (paramLength == 0) {
            throw new RpcException("at last one parameter required");
        }
        Parameter[] parameters = method.getParameters();
        String[] parameterTypeNames = ReflectUtils.getParameterTypeNames(method);
        for (int i = 0; i < parameterTypeNames.length; i++) {
            sb.append(parameterTypeNames[i] + " " + parameters[i].getName());
            if (i != parameterTypeNames.length - 1) {
                sb.append(",");
            }
        }
        sb.append(")");
        String methodBody = CodeGenerateUtils.genJavaCompilerMethodBody(interfaceClazz, method);
        sb.append(methodBody);
    }


    /**
     *
     */
    private static final class JavaFileObjectImpl extends SimpleJavaFileObject {
        private final CharSequence source;
        private ByteArrayOutputStream bytecode;

        public JavaFileObjectImpl(String baseName, final CharSequence source) {
            super(ClassUtils.toURI(baseName + ClassUtils.JAVA_EXTENSION), Kind.SOURCE);
            this.source = source;
        }

        public JavaFileObjectImpl(String name, final Kind kind) {
            super(ClassUtils.toURI(name), kind);
            this.source = null;
        }

        protected JavaFileObjectImpl(URI uri, Kind kind) {
            super(uri, kind);
            this.source = null;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            if (source == null) {
                throw new UnsupportedOperationException("source == null");
            }
            return source;
        }

        @Override
        public InputStream openInputStream() throws IOException {
            return new ByteArrayInputStream(getByteCodes());
        }

        @Override
        public OutputStream openOutputStream() throws IOException {
            bytecode = new ByteArrayOutputStream();
            return bytecode;
        }

        public byte[] getByteCodes() {
            return bytecode.toByteArray();
        }
    }


    /**
     * 重写ClassLoader
     */
    private final class ClassLoaderImpl extends ClassLoader {
        private final Map<String, JavaFileObject> classes = new HashMap<>();

        public ClassLoaderImpl(ClassLoader parent) {
            super(parent);
        }

        Collection<JavaFileObject> files() {
            return Collections.unmodifiableCollection(classes.values());
        }

        /**
         * findClass 方法用于查找指定名称的类。如果类已经被加载过，可以直接返回已加载的类；
         * 否则，需要使用动态编译生成类的字节码，并通过 defineClass 方法将其加载到 JVM 中执行。
         *
         * @param qualifiedClassName
         * @return
         * @throws ClassNotFoundException
         */
        @Override
        protected Class<?> findClass(String qualifiedClassName) throws ClassNotFoundException {
            JavaFileObject javaFileObject = classes.get(qualifiedClassName);
            if (javaFileObject != null) {
                byte[] bytes = ((JavaFileObjectImpl) javaFileObject).getByteCodes();
                return defineClass(qualifiedClassName, bytes, 0, bytes.length);
            }
            return getClass().getClassLoader().loadClass(qualifiedClassName);
        }

        void add(final String qualifiedClassName, final JavaFileObject javaFile) {
            classes.put(qualifiedClassName, javaFile);
        }

        @Override
        protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            return super.loadClass(name, resolve);
        }

        @Override
        public InputStream getResourceAsStream(String name) {
            if (name.endsWith(ClassUtils.CLASS_EXTENSION)) {
                String qualifiedClassName = name.substring(0, name.length() - ClassUtils.CLASS_EXTENSION.length()).replace('/', '.');
                JavaFileObjectImpl file = (JavaFileObjectImpl) classes.get(qualifiedClassName);
                if (file != null) {
                    return new ByteArrayInputStream(file.getByteCodes());
                }
            }
            return super.getResourceAsStream(name);
        }
    }


    private static final class JavaFileManagerImpl extends ForwardingJavaFileManager<JavaFileManager> {

        private final ClassLoaderImpl classLoader;

        private final Map<URI, JavaFileObject> fileObjectMap = new HashMap<>();

        public JavaFileManagerImpl(JavaFileManager fileManager, ClassLoaderImpl classLoader) {
            super(fileManager);
            this.classLoader = classLoader;
        }

        @Override
        public FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException {
            JavaFileObject javaFileObject = fileObjectMap.get(uri(location, packageName, relativeName));
            if (javaFileObject != null) {
                return javaFileObject;
            }
            return super.getFileForInput(location, packageName, relativeName);
        }

        public void putFileForInput(StandardLocation location, String packageName, String relativeName, JavaFileObject javaFileObject) {
            fileObjectMap.put(uri(location, packageName, relativeName), javaFileObject);
        }


        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
            JavaFileObjectImpl javaFileObject = new JavaFileObjectImpl(className, kind);
            classLoader.add(className, javaFileObject);
            return javaFileObject;
        }

        @Override
        public ClassLoader getClassLoader(Location location) {
            return classLoader;
        }

        @Override
        public Iterable<JavaFileObject> list(Location location, String packageName, Set<JavaFileObject.Kind> kinds, boolean recurse) throws IOException {
            Iterable<JavaFileObject> result = super.list(location, packageName, kinds, recurse);

            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            List<URL> urlList = new ArrayList<URL>();
            Enumeration<URL> e = contextClassLoader.getResources("com");
            while (e.hasMoreElements()) {
                urlList.add(e.nextElement());
            }

            ArrayList<JavaFileObject> files = new ArrayList<JavaFileObject>();

            if (location == StandardLocation.CLASS_PATH && kinds.contains(JavaFileObject.Kind.CLASS)) {
                for (JavaFileObject file : fileObjectMap.values()) {
                    if (file.getKind() == JavaFileObject.Kind.CLASS && file.getName().startsWith(packageName)) {
                        files.add(file);
                    }
                }

                files.addAll(classLoader.files());
            } else if (location == StandardLocation.SOURCE_PATH && kinds.contains(JavaFileObject.Kind.SOURCE)) {
                for (JavaFileObject file : fileObjectMap.values()) {
                    if (file.getKind() == JavaFileObject.Kind.SOURCE && file.getName().startsWith(packageName)) {
                        files.add(file);
                    }
                }
            }
            for (JavaFileObject file : result) {
                files.add(file);
            }
            return files;
        }

        @Override
        public String inferBinaryName(Location location, JavaFileObject file) {
            if (file instanceof JavaFileObjectImpl) {
                return file.getName();
            }
            return super.inferBinaryName(location, file);
        }

        private URI uri(Location location, String packageName, String relativeName) {
            return ClassUtils.toURI(location.getName() + '/' + packageName + '/' + relativeName);
        }
    }


}
