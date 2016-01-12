package indi.yume.tools.autosharedpref.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import indi.yume.tools.autosharedpref.Ignore;
import indi.yume.tools.autosharedpref.model.FieldEntity;

/**
 * Created by yume on 15/8/25.
 */
public class ReflectUtil {
    public static Object setFiledAndValue(Map<String, Object> map, Class<?> clazz)
            throws SecurityException, IllegalArgumentException, NoSuchMethodException,
            IllegalAccessException, InvocationTargetException, InstantiationException {

        Object object = setFiledAndValue(map, clazz.newInstance(), clazz);

        return object;
    }

    public static <T> T setFiledAndValue(Map<String, Object> map, T object, Class targetClazz)
            throws SecurityException, IllegalArgumentException, NoSuchMethodException,
            IllegalAccessException, InvocationTargetException, InstantiationException {

        Field[] fields = targetClazz.getDeclaredFields();

        for (Field f : fields)
            if(f.getAnnotation(Ignore.class) == null) {
                try {
                    if(map.containsKey(f.getName()))
                        setObjectValue(object, targetClazz, f.getName(), map.get(f.getName()));
                }catch (NullPointerException e){
                    e.printStackTrace();
                }
            }

        return object;
    }

    public static List<String> getFiledName(Class<?> clazz){
        Field[] fields = clazz.getDeclaredFields();
        List<String> list = new ArrayList<>();

        for (Field f : fields)
            if(f.getAnnotation(Ignore.class) == null)
                list.add(f.getName());

        return list;
    }

    public static Map<String, FieldEntity> getFiled(Class<?> clazz)
            throws SecurityException, IllegalArgumentException, NoSuchMethodException,
            IllegalAccessException, InvocationTargetException {
        Field[] fields = clazz.getDeclaredFields();
        Map<String, FieldEntity> map = new HashMap<>();

        for (Field f : fields)
            if(f.getAnnotation(Ignore.class) == null) {
                FieldEntity fe = new FieldEntity();
                fe.setFieldName(f.getName());
                fe.setType(f.getType());
                fe.setGenericType(getGenericType(f));
                map.put(f.getName(), fe);
            }

        return map;
    }

    public static FieldEntity getOneFiledAndValueByMethod(Object object, Method method)
            throws SecurityException, IllegalArgumentException, NoSuchMethodException,
            IllegalAccessException, InvocationTargetException {
//        Class clazz = object.getClass();
        Class clazz = method.getDeclaringClass();
        Field field = getFiledNameByMethod(clazz, method);
        if(field == null)
            return null;
        FieldEntity fe = new FieldEntity();
        fe.setFieldName(field.getName());
        fe.setValue(getObjectValue(field.getType(), object, field.getName()));
        fe.setType(field.getType());
        fe.setGenericType(getGenericType(field));

        return fe;
    }

    public static Field getFiledNameByMethod(Class clazz, Method method){
        Field[] fields = clazz.getDeclaredFields();
        Field field = null;
        for(Field f : fields)
            if(f.getAnnotation(Ignore.class) == null)
                if(toSetter(f.getName()).equals(method.getName()))
                    field = f;

        return field;
    }

    public static Map<String, FieldEntity> getFiledAndValue(Object object)
            throws SecurityException, IllegalArgumentException, NoSuchMethodException,
            IllegalAccessException, InvocationTargetException {
        Class clazz = object.getClass();
        Field[] fields = clazz.getDeclaredFields();
        Map<String, FieldEntity> map = new HashMap<>();

        for (Field f : fields)
            if(f.getAnnotation(Ignore.class) == null) {
                Object resultObject = getObjectValue(f.getType(), object, f.getName());
                FieldEntity fe = new FieldEntity();
                fe.setFieldName(f.getName());
                fe.setValue(resultObject);
                fe.setType(f.getType());
                fe.setGenericType(getGenericType(f));
                map.put(f.getName(), fe);
            }

        return map;
    }

    public static Object getObjectValue(Class type, Object owner, String fieldname)
            throws SecurityException,
            IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Class ownerClass = owner.getClass();

        Method method = null;
        try {
            if (type == boolean.class)
                method = ownerClass.getMethod(toIs(fieldname));
            else
                method = ownerClass.getMethod(toGetter(fieldname));
        } catch (NoSuchMethodException e){
            e.printStackTrace();
            return null;
        }

        Object object = null;
        object = method.invoke(owner);

        return object;
    }

    public static void setObjectValue(Object owner, Class targetClazz, String fieldname, Object value)
            throws SecurityException, NoSuchMethodException,
            IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Method[] method = null;
        method = targetClazz.getMethods();
        for(Method m : method)
            if(m.getName().equals(toSetter(fieldname)))
                try {
                    m.invoke(owner, value);
                } catch (IllegalArgumentException e){
                    e.printStackTrace();
                }
    }

    private static Class getGenericType(Field f){
        if(f.getType().isAssignableFrom(List.class) ||
                f.getType().isAssignableFrom(Set.class)) {
            Type fc = f.getGenericType();
            if(fc == null) return null;
            if(fc instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) fc;
                return (Class)pt.getActualTypeArguments()[0];
            }
        } else if(f.getType().isAssignableFrom(Map.class)) {
            Type fc = f.getGenericType();
            if(fc == null) return null;
            if(fc instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) fc;
                return (Class)pt.getActualTypeArguments()[1];
            }
        }
        return null;
    }

    public static String toGetter(String fieldname) {
        if (fieldname == null || fieldname.length() == 0) {
            return null;
        }

    /* If the second char is upper, make 'get' + field name as getter name. For example, eBlog -> geteBlog */
        if (fieldname.length() > 1) {
            String second = fieldname.substring(1, 2);
            if (second.equals(second.toUpperCase())) {
                return new StringBuffer("get").append(fieldname).toString();
            }
        }

    /* Common situation */
        fieldname = new StringBuffer("get").append(fieldname.substring(0, 1).toUpperCase())
                .append(fieldname.substring(1)).toString();

        return  fieldname;
    }

    public static String toIs(String fieldname) {
        if (fieldname == null || fieldname.length() == 0) {
            return null;
        }

        if(fieldname.startsWith("is") || fieldname.startsWith("iS") || fieldname.startsWith("IS"))
            return fieldname;
        if(fieldname.startsWith("Is"))
            return new StringBuffer("i").append(fieldname.substring(1)).toString();

        if (fieldname.length() > 1) {
            String second = fieldname.substring(1, 2);
            if (second.equals(second.toUpperCase())) {
                return new StringBuffer("is").append(fieldname).toString();
            }
        }

    /* Common situation */
        fieldname = new StringBuffer("is").append(fieldname.substring(0, 1).toUpperCase())
                .append(fieldname.substring(1)).toString();

        return  fieldname;
    }

    public static String toSetter(String fieldname) {
        if (fieldname == null || fieldname.length() == 0) {
            return null;
        }

    /* If the second char is upper, make 'set' + field name as getter name. For example, eBlog -> seteBlog */
        if (fieldname.length() > 2) {
            String second = fieldname.substring(1, 2);
            if (second.equals(second.toUpperCase())) {
                return new StringBuffer("set").append(fieldname).toString();
            }
        }

    /* Common situation */
        fieldname = new StringBuffer("set").append(fieldname.substring(0, 1).toUpperCase())
                .append(fieldname.substring(1)).toString();

        return  fieldname;
    }
}
