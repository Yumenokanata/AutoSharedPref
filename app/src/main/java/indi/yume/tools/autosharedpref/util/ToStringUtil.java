package indi.yume.tools.autosharedpref.util;

import android.util.Base64;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import indi.yume.tools.autosharedpref.model.Action1;
import indi.yume.tools.autosharedpref.model.FieldEntity;

/**
 * Created by yume on 15/8/25.
 */
public class ToStringUtil {
    private static final String MAP_KEY = "key";
    private static final String MAP_VALUE = "value";
    private static final Gson gson = new Gson();

    private static Map<Class<?>, Convertor<?>> mConvertorMap;

    protected static  <T> void addConvertor(Class<T> clazz, Convertor<T> convertor){
        mConvertorMap.put(clazz, convertor);
    }

    public static void reInitConvertor(){
        mConvertorMap = new HashMap<>();
        initConvertor();
    }

    protected static Map<Class<?>, Convertor<?>> getConvertorMap(){
        if(mConvertorMap == null)
            synchronized (ToStringUtil.class){
                if(mConvertorMap == null) {
                    mConvertorMap = new HashMap<>();
                    initConvertor();
                }
            }
        return mConvertorMap;
    }

    public static Map<FieldEntity, Object> canSaveToMap(Map<FieldEntity, Object> map){
        Map<FieldEntity, Object> newMap = new HashMap<>();
        for(FieldEntity fe : map.keySet())
            newMap.put(fe, canSaveObjectToOriObject(fe, map.get(fe)));
        return newMap;
    }

    public static String encodeBase64(String rawText) {
        return new String(Base64.decode(rawText, Base64.DEFAULT));
    }

    public static String decryptBase64(String base64) {
        return Base64.encodeToString(base64.getBytes(), Base64.DEFAULT);
    }

    public static String toJson(Object object) {
        return gson.toJson(object);
    }

    public static <T> T fromJson(String json, Class<T> classOfT) {
        return gson.fromJson(json, classOfT);
    }

    public static <T> T fromJson(String json, Type typeOfT) {
        return gson.fromJson(json, typeOfT);
    }

    public static Object canSaveObjectToOriObject(FieldEntity valueType, Object value){
        Map<Class<?>, Convertor<?>> convertorMap = getConvertorMap();
        Class<?> fieldType = valueType.getType();
        if(isBaseClass(fieldType))
            return value;

        if(fieldType == Set.class){
            Class genericType = valueType.getGenericType();
            if(genericType == String.class || isBaseClass(value.getClass())) {
                return value;
            }

            Set<Object> set = null;
            try {
                JSONArray jsonArray = new JSONArray((String) value);
                set = new HashSet<>();
                for(int i = 0; i < jsonArray.length(); i++)
                    set.add(gson.fromJson(jsonArray.getString(i), genericType));
            } catch (JSONException e) {
                LogUtil.e(e);
            }
            return set;
        }
        if(fieldType == Map.class){
            Class genericType = valueType.getGenericType();
            if(genericType != String.class) {
                Map<String, Object> m = null;
                try {
                    JSONObject jo = new JSONObject((String) value);
                    m = new HashMap<>();
                    Iterator<String> keys = jo.keys();
                    while (keys.hasNext()) {
                        String k = keys.next();
                        String s = jo.getString(k);
                        m.put(k, gson.fromJson(s, genericType));
                    }
                } catch (JSONException e) {
                    LogUtil.e(e);
                }
                return m;
            }
        }
        if(fieldType == List.class){
            Class genericType = valueType.getGenericType();
            List<Object> list = null;
            try {
                list = new ArrayList<>();
                JSONArray jo = new JSONArray((String) value);
                for(int i = 0; i < jo.length(); i++){
                    list.add(gson.fromJson(jo.getString(i), genericType));
                }
            } catch (JSONException e) {
                LogUtil.e(e);
            }
            return list;
        }

        for(Class<?> c : convertorMap.keySet())
            if(fieldType == c)
                return convertorMap.get(c).string2Object((String) value);

        return gson.fromJson((String) value, fieldType);
    }

    public static Map<FieldEntity, Object> mapObjectToCanSave(Map<FieldEntity, Object> map){

        Map<FieldEntity, Object> newMap = new HashMap<>();
        for(FieldEntity fe : map.keySet())
            newMap.put(fe, objectToCanSaveObject(fe, map.get(fe)));
        return newMap;
    }

    public static Object objectToCanSaveObject(FieldEntity valueType, Object value){
        Map<Class<?>, Convertor<?>> convertorMap = getConvertorMap();
        Class<?> clazz = valueType.getType();
        if(isBaseClass(clazz)) {
            return value;
        }

        if(clazz == Set.class){
            Class genericType = valueType.getGenericType();
            if(genericType == String.class || value.getClass().isPrimitive()) {
                return value;
            }

            return gson.toJson(value);
        }
        if(clazz == Map.class){
            Class genericType = valueType.getGenericType();
            if(genericType != String.class) {
                return gson.toJson(value);
            }
        }
        if(clazz == List.class){
            return gson.toJson(value);
        }

        for(Class<?> c : convertorMap.keySet())
            if(clazz == c)
                return convertorMap.get(c).object2String(value);

        return gson.toJson(value);
    }

    private static boolean isBaseClass(Class clazz){
        return clazz.isPrimitive() ||
                clazz == Integer.class ||
                clazz == Long.class ||
                clazz == Float.class ||
                clazz == Double.class ||
                clazz == Boolean.class ||
                clazz == String.class;
    }

    private static void initConvertor(){
        addDateConvertor();
        addMapConvertor();
    }

    private static void addDateConvertor(){
        addConvertor(Date.class,
                new Convertor<Date>() {
                    @Override
                    public String object2String(Object object) {
                        return String.valueOf(((Date) object).getTime());
                    }

                    @Override
                    public Date string2Object(String string) {
                        Date date = new Date();
                        if(string != null && !string.equals(""))
                            date.setTime(Long.parseLong(string));
                        return date;
                    }
                });
    }

    private static void addMapConvertor(){
        class MapAction implements Action1<JsonUtil.JsonBuilder> {
            String key;
            String value;
            public void set(String key, String value) {
                this.key = key;
                this.value = value;
            }

            @Override
            public void call(JsonUtil.JsonBuilder builder) {
                builder.add(MAP_KEY, key)
                        .add(MAP_VALUE, value);
            }
        }
        final MapAction action1 = new MapAction();
        addConvertor(Map.class,
                new Convertor<Map>() {
                    @Override
                    public String object2String(Object object) {
                        Map<String, String> map = (Map<String, String>) object;

                        if(map == null || map.size() == 0)
                            return "";
                        JsonUtil.JsonArrayBuilder arrayBuilder = JsonUtil.startArray();
                        for(final Map.Entry<String, String> entry : map.entrySet()) {
                            action1.set(entry.getKey(), entry.getValue());
                            arrayBuilder.addJson(action1);
                        }
                        return arrayBuilder.end();
//                        JSONArray jsonArray = new JSONArray();
//                        for(String key : map.keySet()){
//                            JSONObject jo = new JSONObject();
//                            try {
//                                jo.put(MAP_KEY, key);
//                                jo.put(MAP_VALUE, map.get(key));
//                            } catch (JSONException e) {
//                                LogUtil.e(e);
//                                continue;
//                            }
//                            jsonArray.put(jo);
//                        }
//                        return jsonArray.toString();
                    }

                    @Override
                    public Map string2Object(String string) {
                        Map<String, String> map = new HashMap<>();
                        try {
                            if(string == null)
                                string = "";
                            JSONArray jsonArray = new JSONArray(string);
                            for(int i = 0; i < jsonArray.length(); i++){
                                JSONObject jo = jsonArray.getJSONObject(i);
                                map.put(jo.getString(MAP_KEY), jo.optString(MAP_VALUE));
                            }
                        } catch (JSONException e) {
                            LogUtil.e(e);
                        }
                        return map;
                    }
                });
    }

    public static interface Convertor<T>{
        String object2String(Object object);
        T string2Object(String string);
    }
}
