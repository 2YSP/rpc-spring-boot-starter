package com.github.ship.util;

import com.alipay.hessian.generic.exception.ConvertException;
import com.alipay.hessian.generic.model.*;

import java.lang.reflect.Array;
import java.util.*;

/**
 * @author Ship
 * @version 1.0.0
 * @description:
 * @date 2023/06/15 11:13
 */
public class GenericObjectUtil {


    /**
     * 将 GenericObject 转换为具体对象
     *
     * @param genericObject
     *            待转换的GenericObject
     * @return 转换后结果
     */
    @SuppressWarnings("unchecked")
    public static <T> T convertToObject(Object genericObject) {

        try {
            return (T) innerToConvertObject(genericObject, new IdentityHashMap<Object, Object>());
        } catch (Throwable t) {
            throw new ConvertException(t);
        }
    }

    private static Object innerToConvertObject(Object value, Map<Object, Object> map) throws Exception {

        // 判null
        if (value == null) {
            return null;
        }

        // 值为GenericObject类型
        if (value.getClass() == GenericObject.class) {
            GenericObject genericObject = (GenericObject) value;
            return doConvertToObject(genericObject, map);
        }

        // 值为GenericCollection类型
        if (value.getClass() == GenericCollection.class) {
            GenericCollection collection = (GenericCollection) value;
            return doConvertToCollection(collection, map);
        }

        // 值为GenericMap类型
        if (value.getClass() == GenericMap.class) {
            GenericMap genericMap = (GenericMap) value;
            return doConvertToMap(genericMap, map);
        }

        // 值为GenericArray类型
        if (value.getClass() == GenericArray.class) {
            GenericArray genericArray = (GenericArray) value;
            return doConvertToArray(genericArray, map);
        }

        // 值为GenericClass类型
        if (value.getClass() == GenericClass.class) {
            GenericClass genericClass = (GenericClass) value;
            return doConvertToClass(genericClass, map);
        }

        // 说明是jdk类, 处理集合类,将集合类中结果转换
        Object obj = handleCollectionOrMapToObject(value, map);
        return obj;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static Object handleCollectionOrMapToObject(Object value, Map<Object, Object> map) throws Exception {

        // 1. 判null
        if (value == null) {
            return null;
        }

        // 2. 查看缓存的转换记录是否存在转换历史
        if (map.get(value) != null) {
            return map.get(value);
        }

        // 3. 处理Collection实现类情况
        if (Collection.class.isAssignableFrom(value.getClass())) {

            Collection values = (Collection) value;
            Collection result = (Collection) value.getClass().newInstance();
            map.put(value, result);

            for (Object obj : values) {
                result.add(innerToConvertObject(obj, map));
            }

            return result;
        }

        // 4. 处理Map实现类情况
        if (Map.class.isAssignableFrom(value.getClass())) {

            Map<Object, Object> valueMap = (Map<Object, Object>) value;
            Map result = (Map) value.getClass().newInstance();
            map.put(value, result);

            Iterator iter = valueMap.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                result.put(innerToConvertObject(entry.getKey(), map), innerToConvertObject(entry.getValue(), map));
            }

            return result;
        }

        return value;
    }

    private static Object doConvertToObject(GenericObject genericObject, Map<Object, Object> map) throws Exception {

        if (genericObject == null) {
            return genericObject;
        }
        Map<String, Object> result = new HashMap<String, Object>();

        // 如果map中缓存转换结果,直接返回
        Object object = map.get(genericObject);
        if (object != null) {
            return object;
        }

        for (Map.Entry<String, Object> entry : genericObject.getFields().entrySet()) {
            result.put(entry.getKey(), convertToObject(entry.getValue()));
        }

        return result;
    }

    private static Class<? extends Object> loadClassFromTCCL(String clazzName) throws ClassNotFoundException {
        return Class.forName(clazzName, true, Thread.currentThread().getContextClassLoader());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static Object doConvertToCollection(GenericCollection genericCollection, Map<Object, Object> map)
            throws Exception {

        // 如果map中缓存转换结果,直接返回
        Object object = map.get(genericCollection);
        if (object != null) {
            return object;
        }

        // 检测 GenericCollection 是否封装 Collection 实例
        Class clazz = loadClassFromTCCL(genericCollection.getType());
        if (!Collection.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException("GenericCollection实例未封装Collection实例.");
        }

        // 初始化Collection对象,并放入map
        Collection result = (Collection) clazz.newInstance();
        map.put(genericCollection, result);

        // 填充Collection对象
        Collection values = genericCollection.getCollection();
        for (Object value : values) {
            result.add(innerToConvertObject(value, map));
        }

        return result;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static Object doConvertToMap(GenericMap genericMap, Map<Object, Object> map) throws Exception {

        // 如果map中缓存转换结果,直接返回
        Object object = map.get(genericMap);
        if (object != null) {
            return object;
        }

        // 检测 GenericMap 是否封装 Map 实例
        Class clazz = loadClassFromTCCL(genericMap.getType());
        if (!Map.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException("GenericMap实例未封装Map实例.");
        }

        // 初始化对象,并放入map
        Map result = (Map) clazz.newInstance();
        map.put(genericMap, result);

        // 填充map对象
        Iterator iter = genericMap.getMap().entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            result.put(innerToConvertObject(entry.getKey(), map), innerToConvertObject(entry.getValue(), map));
        }

        return result;
    }

    @SuppressWarnings("rawtypes")
    private static Object doConvertToArray(GenericArray genericArray, Map<Object, Object> map) throws Exception {

        // 如果map中缓存转换结果,直接返回
        Object object = map.get(genericArray);
        if (object != null) {
            return object;
        }

        // 初始化数组对象,并放入map
        Class clazz = loadClassFromTCCL(genericArray.getComponentType());
        Object[] objects = genericArray.getObjects();
        Object result = Array.newInstance(clazz, objects.length);
        map.put(genericArray, result);

        // 填充数组对象
        for (int i = 0; i < objects.length; i++) {
            Array.set(result, i, innerToConvertObject(objects[i], map));
        }

        return result;
    }

    private static Object doConvertToClass(GenericClass genericClass, Map<Object, Object> map)
            throws ClassNotFoundException {
        // 如果map中缓存转换结果,直接返回
        Object object = map.get(genericClass);
        if (object != null) {
            return object;
        }

        Object obj = loadClassFromTCCL(genericClass.getClazzName());
        map.put(genericClass, obj);
        return obj;
    }

    @SuppressWarnings("rawtypes")
    public static GenericObject buildGenericObject(String className, Map data) {
        GenericObject object = new GenericObject(className);
        Iterator iterator = data.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            object.putField((String) entry.getKey(), entry.getValue());
        }
        return object;
    }
}
