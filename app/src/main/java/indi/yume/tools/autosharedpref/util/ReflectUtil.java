package indi.yume.tools.autosharedpref.util;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import indi.yume.tools.autosharedpref.Ignore;
import indi.yume.tools.autosharedpref.model.FieldEntity;

/**
 * Created by yume on 15/8/25.
 */
public class ReflectUtil {
    public static <T> T setFiledAndValue(Map<String, Object> map, Class<T> clazz) throws IllegalAccessException, InstantiationException {
        return setFiledAndValue(map, clazz.newInstance(), clazz);
    }

    public static <T> T setFiledAndValue(Map<String, Object> map, T object, Class targetClazz) {

        Field[] fields = getDeclaredFields(targetClazz);

        for (Field f : fields)
            if(f.getAnnotation(Ignore.class) == null) {
                try {
                    if(map.containsKey(f.getName()))
                        setObjectValue(object, targetClazz, f.getName(), map.get(f.getName()));
                }catch (NullPointerException e){
                    LogUtil.e(e);
                }
            }

        return object;
    }

    public static List<String> getFiledName(Class<?> clazz){
        Field[] fields = getDeclaredFields(clazz);
        List<String> list = new ArrayList<>();

        for (Field f : fields)
            if(f.getAnnotation(Ignore.class) == null)
                list.add(f.getName());

        return list;
    }

    public static Map<String, FieldEntity> getFiled(Class<?> clazz) {
        Field[] fields = getDeclaredFields(clazz);
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

    public static FieldEntity getOneFiledAndValueByMethod(Object object, Method method) {
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
        Field[] fields = getDeclaredFields(clazz);
        Field field = null;
        fieldFor: for(Field f : fields)
            if(f.getAnnotation(Ignore.class) == null) {
                List<String> nameList = toSetter(f.getName());
                for(String name : nameList)
                    if(name.equals(method.getName())) {
                        field = f;
                        break fieldFor;
                    }
            }

        return field;
    }

    public static Map<String, FieldEntity> getFiledAndValue(Object object) {
        Class clazz = object.getClass();
        Field[] fields = getDeclaredFields(clazz);
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

    public static Object getObjectValue(Class type, Object owner, String fieldname) {
        Class ownerClass = owner.getClass();

        List<String> nameList = null;
        if(type == boolean.class)
            nameList = toIs(fieldname);
        if(nameList == null)
            nameList = new LinkedList<>();
        nameList.addAll(toGetter(fieldname));

        Object object = null;
        for(String getterName : nameList) {
            LogUtil.m(fieldname + "| getterName: " + getterName);
            try {
                Method method = ownerClass.getMethod(getterName);
                object = method.invoke(owner);
                break;
            } catch (NoSuchMethodException e) {
                continue;
            } catch (IllegalAccessException e) {
                continue;
            } catch (IllegalArgumentException e) {
                continue;
            } catch (InvocationTargetException e) {
                break;
            }
        }

        return object;
    }

    public static void setObjectValue(Object owner, Class targetClazz, String fieldname, Object value) {
        Method[] methodList = targetClazz.getMethods();

        List<String> nameList = toSetter(fieldname);
        method: for(Method m : methodList)
            for(String name : nameList)
                if(m.getName().equals(name))
                    try {
                        m.invoke(owner, value);
                        break method;
                    } catch (IllegalArgumentException e){
                        LogUtil.e(e);
                    } catch (IllegalAccessException e) {
                        LogUtil.e(e);
                    } catch (InvocationTargetException e) {
                        LogUtil.e(e);
                    }
    }

    @Nullable
    public static Method getSetterMethod(Class clazz, String fieldname) {
        Method[] methodList = clazz.getMethods();

        List<String> nameList = toSetter(fieldname);
        for(Method m : methodList)
            for(String name : nameList)
                if(m.getName().equals(name) && m.getTypeParameters().length == 1)
                    if(!Modifier.isStatic(m.getModifiers()))
                        return m;

        return null;
    }

    @Nullable
    public static Method getGetterMethod(Class clazz, String fieldName, Class fieldClazz) {
        List<String> nameList = null;
        if(fieldClazz == boolean.class)
            nameList = toIs(fieldName);
        if(nameList == null)
            nameList = new LinkedList<>();
        nameList.addAll(toGetter(fieldName));

        Object object = null;
        for(String getterName : nameList) {
            LogUtil.m(fieldName + "| getterName: " + getterName);
            try {
                return clazz.getMethod(getterName);
            } catch (NoSuchMethodException e) {
                continue;
            }
        }

        return null;
    }

    private static Field[] getDeclaredFields(Class<?> clazz) {
        List<Field> list = new ArrayList<>();
        while (clazz != Object.class) {
            Collections.addAll(list, clazz.getDeclaredFields());
            clazz = clazz.getSuperclass();
        }

        return list.toArray(new Field[list.size()]);
    }

    public static Class getGenericType(Field f){
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

    public static List<String> toGetter(String fieldname) {
        if (fieldname == null || fieldname.length() == 0) {
            return null;
        }

        List<String> nameList = new LinkedList<>();
        /* If the second char is upper, make 'get' + field name as getter name. For example, eBlog -> geteBlog */
        if (fieldname.length() > 1) {
            char second = fieldname.charAt(1);
            if (Character.isUpperCase(second)) {
                nameList.add("get" + fieldname);
            }
        }

        fieldname = "get" + fieldname.substring(0, 1).toUpperCase() + fieldname.substring(1);
        nameList.add(fieldname);

        return nameList;
    }

    public static List<String> toIs(String fieldname) {
        if (fieldname == null || fieldname.length() == 0) {
            return null;
        }

        List<String> nameList = new LinkedList<>();
        if(fieldname.startsWith("is") || fieldname.startsWith("iS") || fieldname.startsWith("IS"))
            nameList.add(fieldname);
        if(fieldname.startsWith("Is"))
            nameList.add("i" + fieldname.substring(1));

        if (fieldname.length() > 1) {
            char second = fieldname.charAt(1);
            if (Character.isUpperCase(second)) {
                nameList.add("is" + fieldname);
            }
        }

        /* Common situation */
        fieldname = "is" + fieldname.substring(0, 1).toUpperCase() + fieldname.substring(1);
        nameList.add(fieldname);

        return nameList;
    }

    public static List<String> toSetter(final String fieldname) {
        if (fieldname == null || fieldname.length() == 0) {
            return null;
        }

        List<String> nameList = new LinkedList<>();
        /* If the second char is upper, make 'set' + field name as getter name. For example, eBlog -> seteBlog */
        if (fieldname.length() > 2) {
            char second = fieldname.charAt(1);
            if (Character.isUpperCase(second)) {
                nameList.add("set" + fieldname);
            }
        }

        /* Common situation */
        nameList.add("set" + fieldname.substring(0, 1).toUpperCase() + fieldname.substring(1));

        if(fieldname.startsWith("is"))
            nameList.add(fieldname.replace("is", "set"));

        return nameList;
    }
}
