package indi.yume.tools.autosharedpref.sharedpref;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;
import java.util.Set;

/**
 * Created by yume on 15/8/25.
 */
public class SharedPrefImpl implements SharedPrefService {

    public void putValue(Context context, String fileName, String key, Object value){
        SharedPreferences sharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        putValue(editor, key, value);
        editor.apply();
    }

    public void putAll(Context context, String fileName, Map<String, Object> map){
        if(map == null)
            return;
        SharedPreferences sharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for(String key : map.keySet()) {
            Object value = map.get(key);
            putValue(editor, key, value);
        }
        editor.apply();
    }

    public Map<String, Object> getAll(Context context, String filename){
        SharedPreferences sharedPreferences = context.getSharedPreferences(filename, Context.MODE_PRIVATE);
        return (Map<String, Object>) sharedPreferences.getAll();
    }

    public void putValue(SharedPreferences.Editor editor, String key, Object value){
        if(value == null)
            editor.putString(key, "");
        else if(value instanceof Set)
            editor.putStringSet(key, (Set<String>) value);
        else if(value instanceof String)
            editor.putString(key, (String) value);
        else if(value instanceof Integer)
            editor.putInt(key, (Integer) value);
        else if(value instanceof Long)
            editor.putLong(key, (Long) value);
        else if(value instanceof Float)
            editor.putFloat(key, (Float) value);
        else if(value instanceof Boolean)
            editor.putBoolean(key, (Boolean) value);
        else if(value instanceof Double)
            editor.putFloat(key, ((Double)value).floatValue());
        else
            throw new Error("SharedPref saving Error: Class not found: " + value.getClass());
    }
}
