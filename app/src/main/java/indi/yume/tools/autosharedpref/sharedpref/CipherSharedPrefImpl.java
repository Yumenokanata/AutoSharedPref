package indi.yume.tools.autosharedpref.sharedpref;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import indi.yume.tools.autosharedpref.util.LogUtil;
import indi.yume.tools.autosharedpref.util.ToStringUtil;

import static indi.yume.tools.autosharedpref.sharedpref.CipherSharedPrefImpl.DataType.*;

/**
 * Created by yume on 17-3-21.
 */

public class CipherSharedPrefImpl implements SharedPrefService {
    private final CipherAdapter adapter;

    public CipherSharedPrefImpl(CipherAdapter adapter) {
        if (adapter == null)
            throw new NullPointerException();

        this.adapter = adapter;
    }

    public void putValue(Context context, String fileName, String key, Object value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        putValue(editor, key, value);
        editor.apply();
    }

    public void putAll(Context context, String fileName, Map<String, Object> map) {
        if (map == null || map.isEmpty())
            return;
        SharedPreferences sharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for (String key : map.keySet()) {
            Object value = map.get(key);
            putValue(editor, key, value);
        }
        editor.apply();
    }

    public Map<String, Object> getAll(Context context, String filename) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(filename, Context.MODE_PRIVATE);
        Map<String, Object> valueMap = (Map<String, Object>) sharedPreferences.getAll();
        Map<String, Object> rawMap = new HashMap<>();

        for(Map.Entry<String, Object> entry : valueMap.entrySet())
            rawMap.put(entry.getKey(), getRawValue(entry.getValue()));

        return rawMap;
    }

    Object getRawValue(Object entryJson) {
        if (!(entryJson instanceof String))
            return entryJson;

        Entry entry = null;
        try {
            entry = ToStringUtil.fromJson((String) entryJson, Entry.class);
        } catch (Exception e) {
            LogUtil.e(e);
            return entryJson;
        }

        switch (entry.getType()) {
            case NULL:
                return null;
            case STRING_SET:
                return ToStringUtil.fromJson(decrypt(entry.getValue()),
                        new TypeToken<Set<String>>() {}.getType());
            case STRING:
                return decrypt(entry.getValue());
            case INT:
                return Integer.parseInt(decrypt(entry.getValue()));
            case LONG:
                return Long.parseLong(decrypt(entry.getValue()));
            case FLOAT:
                return Float.parseFloat(decrypt(entry.getValue()));
            case BOOL:
                return Boolean.parseBoolean(decrypt(entry.getValue()));
            case DOUBLE:
                return Double.parseDouble(decrypt(entry.getValue()));
            default:
                throw new Error("SharedPref get value Error: type not found: " + entryJson);
        }
    }

    public void putValue(SharedPreferences.Editor editor, String key, Object value) {
        Entry entry = encrypt(value);
        editor.putString(key, entry.encode());
    }

    Entry encrypt(Object value) {
        if (value == null)
            return new Entry(NULL, "");
        else if (value instanceof Set) {
            return new Entry(STRING_SET, encrypt(ToStringUtil.toJson(value)));
        } else if (value instanceof String) {
            return new Entry(STRING, encrypt((String) value));
        }  else if (value instanceof Integer) {
            return new Entry(INT, encrypt(value.toString()));
        } else if(value instanceof Long) {
            return new Entry(LONG, encrypt(value.toString()));
        } else if(value instanceof Float) {
            return new Entry(FLOAT, encrypt(value.toString()));
        } else if(value instanceof Boolean) {
            return new Entry(BOOL, encrypt(value.toString()));
        } else if(value instanceof Double) {
            return new Entry(DOUBLE, encrypt(value.toString()));
        } else {
            throw new Error("SharedPref saving Error: Class not found: " + value.getClass());
        }
    }

    private String encrypt(String raw) {
        return adapter.encrypt(raw);
    }

    private String decrypt(String encryptedData) {
        return adapter.decrypt(encryptedData);
    }

    enum DataType {
        NULL,
        STRING_SET,
        STRING,
        INT,
        LONG,
        FLOAT,
        BOOL,
        DOUBLE
    }

    public static class Entry {
        private final DataType type;
        private final String value;

        public Entry(DataType type, String value) {
            this.type = type;
            this.value = value;
        }

        public DataType getType() {
            return type;
        }

        public String getValue() {
            return value;
        }

        public String encode() {
            return ToStringUtil.toJson(this);
        }

        public static Entry decode(String json) {
            return ToStringUtil.fromJson(json, Entry.class);
        }
    }
}
