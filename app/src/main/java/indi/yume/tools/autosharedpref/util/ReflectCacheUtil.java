package indi.yume.tools.autosharedpref.util;

import android.support.annotation.Nullable;

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
import java.util.Objects;
import java.util.Set;

import indi.yume.tools.autosharedpref.Ignore;
import indi.yume.tools.autosharedpref.model.FieldEntity;

/**
 * Created by yume on 16-3-18.
 */
public class ReflectCacheUtil {
    private static Map<Class, FieldCacheEntry> cacheEntryMap = new HashMap<>();

    public static <T> T setFiledAndValue(Map<String, Object> map, Class<T> clazz)
            throws IllegalAccessException, InstantiationException {
        return setFiledAndValue(clazz, clazz.newInstance(), map);
    }

    public static <T> T setFiledAndValue(Class targetClazz, T object, Map<String, Object> map) {
        FieldCacheEntry cacheEntry = cacheEntryMap.get(targetClazz);
        if(cacheEntry == null) {
            cacheEntry = new FieldCacheEntry();
            cacheEntryMap.put(targetClazz, cacheEntry);
        }

        for(Map.Entry<String, Object> entry : map.entrySet()) {
            String fieldName = entry.getKey();

            FieldData fieldData = cacheEntry.getDataByFieldName(entry.getKey());
            if(fieldData == null)
                fieldData = new FieldData(fieldName);

            if(fieldData.getSetterMethod() == null)
                fieldData.setSetterMethod(ReflectUtil.getSetterMethod(targetClazz, fieldName));

            Method setterMethod = fieldData.getSetterMethod();
            if(setterMethod != null)
                try {
                    setterMethod.invoke(object, entry.getValue());
                } catch (IllegalAccessException e) {
                    LogUtil.e(e);
                } catch (InvocationTargetException e) {
                    LogUtil.e(e);
                }

            cacheEntry.putDataByFieldName(fieldName, fieldData);
        }

        return object;
    }

    public static FieldEntity getOneFiledAndValueByMethod(Object object, Method setterMethod) {
        Class clazz = setterMethod.getDeclaringClass();
        FieldCacheEntry cacheEntry = cacheEntryMap.get(clazz);
        if(cacheEntry == null) {
            cacheEntry = new FieldCacheEntry();
            cacheEntryMap.put(clazz, cacheEntry);
        }

        FieldData fieldData = cacheEntry.getFieldBySetter(setterMethod);
        if(fieldData == null) {
            Field field = ReflectUtil.getFiledNameByMethod(clazz, setterMethod);
            fieldData = new FieldData(field.getName());
            fieldData.setField(field);
            fieldData.setSetterMethod(setterMethod);

            cacheEntry.putDataByFieldName(field.getName(), fieldData);
        }
        Field field = fieldData.getField();
        if(field == null) {
            field = ReflectUtil.getFiledNameByMethod(clazz, setterMethod);
            fieldData.setField(field);
        }

        if(field == null)
            return null;

        Method getterMethod = fieldData.getGetterMethod();
        if(getterMethod == null) {
            getterMethod = ReflectUtil.getGetterMethod(clazz, fieldData.getFieldName(), field.getType());
            fieldData.setGetterMethod(getterMethod);
        }

        FieldEntity fe = new FieldEntity();
        fe.setFieldName(field.getName());
        fe.setType(field.getType());
        fe.setGenericType(ReflectUtil.getGenericType(field));

        if(getterMethod != null) {
            try {
                fe.setValue(getterMethod.invoke(object));
            } catch (IllegalAccessException e) {
                LogUtil.e(e);
            } catch (InvocationTargetException e) {
                LogUtil.e(e);
            }
        } else {
            LogUtil.m(ReflectCacheUtil.class, field.getName() + " Getter method not found");
        }

        return fe;
    }

    private static class FieldCacheEntry {
        private Map<String, FieldData> fieldDataMap = new HashMap<>();
        private Map<Method, FieldData> setterDataMap = new HashMap<>();

        public void putDataByFieldName(String fieldName, FieldData fieldData) {
            fieldDataMap.put(fieldName, fieldData);
            if(fieldData.getSetterMethod() != null)
                setterDataMap.put(fieldData.getSetterMethod(), fieldData);
        }

        public FieldData getDataByFieldName(String fieldName) {
            return fieldDataMap.get(fieldName);
        }

        public void putSetter(String field, Method setterMethod) {
            FieldData fieldData = setterDataMap.get(setterMethod);
            if(fieldData == null)
                fieldData = fieldDataMap.get(field);
            if(fieldData == null)
                fieldData = new FieldData(field);
            fieldData.setSetterMethod(setterMethod);
            putDataByFieldName(field, fieldData);
        }

        public FieldData getFieldBySetter(Method setterMethod) {
            return setterDataMap.get(setterMethod);
        }
    }

    private static class FieldData {
        private String fieldName;
        private Field field;
        private Method getterMethod;
        private Method setterMethod;
        private boolean isIgnore = false;

        public FieldData(String fieldName) {
            this.fieldName = fieldName;
        }

        public Field getField() {
            return field;
        }

        public void setField(Field field) {
            this.field = field;
        }

        public String getFieldName() {
            return fieldName;
        }

        public void setFieldName(String fieldName) {
            this.fieldName = fieldName;
        }

        public Method getGetterMethod() {
            return getterMethod;
        }

        public void setGetterMethod(Method getterMethod) {
            this.getterMethod = getterMethod;
        }

        public Method getSetterMethod() {
            return setterMethod;
        }

        public void setSetterMethod(Method setterMethod) {
            this.setterMethod = setterMethod;
        }

        public boolean isIgnore() {
            return isIgnore;
        }

        public void setIgnore(boolean ignore) {
            isIgnore = ignore;
        }
    }
}
