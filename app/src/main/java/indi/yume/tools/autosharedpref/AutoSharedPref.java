package indi.yume.tools.autosharedpref;

import android.content.Context;

import com.google.dexmaker.stock.ProxyBuilder;

import java.util.HashMap;
import java.util.Map;

import indi.yume.tools.autosharedpref.model.FieldEntity;
import indi.yume.tools.autosharedpref.sharedpref.CipherAdapter;
import indi.yume.tools.autosharedpref.sharedpref.CipherSharedPrefImpl;
import indi.yume.tools.autosharedpref.sharedpref.SharedPrefService;
import indi.yume.tools.autosharedpref.util.LogUtil;
import indi.yume.tools.autosharedpref.util.ReflectCacheUtil;
import indi.yume.tools.autosharedpref.util.ReflectUtil;
import indi.yume.tools.autosharedpref.sharedpref.SharedPrefImpl;
import indi.yume.tools.autosharedpref.util.ToStringUtil;

/**
 * Created by yume on 15/8/25.
 */
public class AutoSharedPref {
    private static boolean useCache = false;
    private static SharedPrefService DEFAULT_PREF_SERVICE = new SharedPrefImpl();

    private AutoSharedPref(){}

    public static <T> T newModel(Context context,
                                 Class<T> modelClazz,
                                 String sharedPrefFileName) {
        return newModel(context, modelClazz, sharedPrefFileName, DEFAULT_PREF_SERVICE);
    }

    public static <T> T newModel(Context context,
                                 Class<T> modelClazz,
                                 String sharedPrefFileName,
                                 CipherAdapter adapter) {
        return newModel(context, modelClazz, sharedPrefFileName, new CipherSharedPrefImpl(adapter));
    }

    private static <T> T newModel(Context context,
                                 Class<T> modelClazz,
                                 String sharedPrefFileName,
                                 SharedPrefService prefService) {
        context = context.getApplicationContext();
        try {
            ProxyInvocationHandler proxy;
            if (useCache)
                proxy = new CacheInvocationHandler(context, sharedPrefFileName, prefService);
            else
                proxy = new NormalInvocationHandler(context, sharedPrefFileName, prefService);

            T object = ProxyBuilder.forClass(modelClazz)
                    .dexCache(context.getDir("dx", Context.MODE_PRIVATE))
                    .handler(proxy)
                    .build();

            Map<String, Object> sharedValueMap = prefService.getAll(context, sharedPrefFileName);
            Map<String, FieldEntity> fieldMap = ReflectUtil.getFiled(modelClazz);
            Map<FieldEntity, Object> objectValueMap = new HashMap<>();
            for (String key : fieldMap.keySet()) {
                Object value = sharedValueMap.get(key);
                if (value != null)
                    objectValueMap.put(fieldMap.get(key), value);
            }
            objectValueMap = ToStringUtil.canSaveToMap(objectValueMap);

            Map<String, Object> keyWithValueMap = new HashMap<>();
            for (FieldEntity fe : objectValueMap.keySet())
                keyWithValueMap.put(fe.getFieldName(), objectValueMap.get(fe));

            if (useCache)
                ReflectCacheUtil.setFiledAndValue(modelClazz, object, keyWithValueMap);
            else
                ReflectUtil.setFiledAndValue(keyWithValueMap, object, modelClazz);

            proxy.startProxy();
            return object;
        } catch (Exception e) {
            LogUtil.e(e);
        }

        return null;
    }

    public static void setUseCache(boolean use) {
        useCache = use;
    }

    public static void saveModel(Context context,
                                 Object model,
                                 String prefFileName){
        saveModel(context, model, prefFileName, DEFAULT_PREF_SERVICE);
    }

    public static void saveModel(Context context,
                                 Object model,
                                 String prefFileName,
                                 CipherAdapter adapter){
        saveModel(context, model, prefFileName, new CipherSharedPrefImpl(adapter));
    }

    public static void saveModel(Context context,
                                 Object model,
                                 String prefFileName,
                                 SharedPrefService prefService){
        Map<String, FieldEntity> fieldMap = ReflectUtil.getFiled(model.getClass());
        for(String key : fieldMap.keySet()) {
            FieldEntity fieldEntity = fieldMap.get(key);
            DEFAULT_PREF_SERVICE.putValue(context,
                    prefFileName,
                    fieldEntity.getFieldName(),
                    ToStringUtil.objectToCanSaveObject(fieldEntity, fieldEntity.getValue()));
        }
    }

    public static void setLog(boolean open) {
        LogUtil.setDebug(open);
    }
}
