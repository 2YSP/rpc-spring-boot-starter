package com.github.ship.spi;

import com.github.ship.common.exception.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Custom SPI ExtensionLoader, inspired by Dubbo's extension loader.
 * Loads extension implementations from META-INF/rpc/ files with key=value format.
 */
public class ExtensionLoader<T> {

    private static final Logger logger = LoggerFactory.getLogger(ExtensionLoader.class);

    /** SPI 配置文件目录（参考 Dubbo 的 META-INF/dubro/） */
    private static final String RPC_DIRECTORY = "META-INF/rpc/";

    /** 全局缓存：接口 Class -> ExtensionLoader 实例，保证每个 SPI 接口只有一个 Loader */
    private static final ConcurrentMap<Class<?>, ExtensionLoader<?>> EXTENSION_LOADER_MAP = new ConcurrentHashMap<>();

    /** 当前 ExtensionLoader 负责的 SPI 接口类型 */
    private final Class<T> type;

    /** 持有 扩展名->实现类 映射（Holder + DCL 实现懒加载） */
    private final Holder<Map<String, Class<?>>> cachedClasses = new Holder<>();

    /** 扩展名->单例实例 缓存 */
    private final ConcurrentMap<String, T> cachedInstances = new ConcurrentHashMap<>();

    private ExtensionLoader(Class<T> type) {
        this.type = type;
    }

    public static <T> ExtensionLoader<T> getExtensionLoader(Class<T> type) {
        if (type == null) {
            throw new IllegalArgumentException("Extension type is null");
        }
        if (!type.isInterface()) {
            throw new IllegalArgumentException("Extension type must be an interface: " + type);
        }
        if (!type.isAnnotationPresent(SPI.class)) {
            throw new IllegalArgumentException("Extension type must be annotated with @SPI: " + type);
        }

        ExtensionLoader<T> loader = (ExtensionLoader<T>) EXTENSION_LOADER_MAP.get(type);
        if (loader == null) {
            EXTENSION_LOADER_MAP.putIfAbsent(type, new ExtensionLoader<>(type));
            loader = (ExtensionLoader<T>) EXTENSION_LOADER_MAP.get(type);
        }
        return loader;
    }

    /**
     * Get extension by name.
     */
    public T getExtension(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Extension name is null or empty");
        }
        T instance = cachedInstances.get(name);
        if (instance == null) {
            Class<?> clazz = getExtensionClasses().get(name);
            if (clazz == null) {
                throw new RpcException("No such extension: " + type.getName() + " by name " + name);
            }
            try {
                instance = (T) clazz.newInstance();
            } catch (Exception e) {
                throw new RpcException("Failed to create extension instance: " + clazz.getName() + ", cause: " + e.getMessage());
            }
            T old = cachedInstances.putIfAbsent(name, instance);
            if (old != null) {
                instance = old;
            }
        }
        return instance;
    }

    /**
     * Get all extensions.
     */
    public List<T> getExtensions() {
        Map<String, Class<?>> classes = getExtensionClasses();
        List<T> instances = new ArrayList<>(classes.size());
        for (Map.Entry<String, Class<?>> entry : classes.entrySet()) {
            instances.add(getExtension(entry.getKey()));
        }
        return instances;
    }

    /**
     * Get the default extension (defined by @SPI's value).
     */
    public T getDefaultExtension() {
        SPI spi = type.getAnnotation(SPI.class);
        if (spi == null || spi.value().isEmpty()) {
            return null;
        }
        return getExtension(spi.value());
    }

    private Map<String, Class<?>> getExtensionClasses() {
        Map<String, Class<?>> classes = cachedClasses.get();
        if (classes == null) {
            synchronized (cachedClasses) {
                classes = cachedClasses.get();
                if (classes == null) {
                    classes = loadExtensionClasses();
                    cachedClasses.set(classes);
                }
            }
        }
        return classes;
    }

    private Map<String, Class<?>> loadExtensionClasses() {
        Map<String, Class<?>> classes = new HashMap<>();
        loadFile(classes, RPC_DIRECTORY);
        return classes;
    }

    private void loadFile(Map<String, Class<?>> classes, String dir) {
        String fileName = dir + type.getName();
        try {
            Enumeration<URL> urls;
            ClassLoader classLoader = ExtensionLoader.class.getClassLoader();
            if (classLoader != null) {
                urls = classLoader.getResources(fileName);
            } else {
                urls = ClassLoader.getSystemResources(fileName);
            }
            if (urls != null) {
                while (urls.hasMoreElements()) {
                    URL url = urls.nextElement();
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            line = line.trim();
                            if (line.isEmpty() || line.startsWith("#")) {
                                continue;
                            }
                            parseLine(line, classes);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to load extension file: " + fileName, e);
        }
    }

    private void parseLine(String line, Map<String, Class<?>> classes) {
        String name;
        String clazzName;
        int eqIndex = line.indexOf('=');
        if (eqIndex > 0) {
            name = line.substring(0, eqIndex).trim();
            clazzName = line.substring(eqIndex + 1).trim();
        } else {
            // Fallback to class name only (no key), use simple class name as key
            clazzName = line;
            name = clazzName.substring(clazzName.lastIndexOf('.') + 1);
        }

        if (name.isEmpty() || clazzName.isEmpty()) {
            return;
        }

        try {
            Class<?> clazz = Class.forName(clazzName, true, ExtensionLoader.class.getClassLoader());
            if (!type.isAssignableFrom(clazz)) {
                throw new RpcException(clazzName + " is not a subtype of " + type.getName());
            }
            Class<?> existing = classes.get(name);
            if (existing == null) {
                classes.put(name, clazz);
            } else if (!existing.equals(clazz)) {
                logger.warn("Duplicate extension {} for key {}, {} will override {}",
                        type.getName(), name, clazz.getName(), existing.getName());
                classes.put(name, clazz);
            }
        } catch (ClassNotFoundException e) {
            logger.error("Failed to load extension class: " + clazzName, e);
        }
    }

    private static class Holder<T> {
        private volatile T value;

        public T get() {
            return value;
        }

        public void set(T value) {
            this.value = value;
        }
    }
}