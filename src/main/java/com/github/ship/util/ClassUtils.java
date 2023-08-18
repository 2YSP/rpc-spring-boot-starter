package com.github.ship.util;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Ship
 * @version 1.0.0
 * @description:
 * @date 2023/08/18 11:24
 */
public class ClassUtils {

    public static final String CLASS_EXTENSION = ".class";

    public static final String JAVA_EXTENSION = ".java";

    public static URI toURI(String name) {
        try {
            return new URI(name);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }
}
