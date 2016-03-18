package indi.yume.tools.autosharedpref;

import android.content.Context;

import com.google.dexmaker.dx.rop.code.Exceptions;
import com.google.dexmaker.stock.ProxyBuilder;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import indi.yume.tools.autosharedpref.model.FieldEntity;
import indi.yume.tools.autosharedpref.util.LogUtil;
import indi.yume.tools.autosharedpref.util.ReflectCacheUtil;
import indi.yume.tools.autosharedpref.util.ReflectUtil;
import indi.yume.tools.autosharedpref.util.SharedPrefUtil;
import indi.yume.tools.autosharedpref.util.ToStringUtil;

/**
 * Created by yume on 15/8/25.
 */
public class AutoSharedPref {
    private static boolean useCache = false;

    private AutoSharedPref(){}

    public static <T> T newModel(Context context, Class<T> modelClazz, String sharedPrefFileName) {
        context = context.getApplicationContext();
        try {
            ProxyInvocationHandler proxy;
            if (useCache)
                proxy = new CacheInvocationHandler(context, sharedPrefFileName);
            else
                proxy = new NormalInvocationHandler(context, sharedPrefFileName);

            T object = ProxyBuilder.forClass(modelClazz)
                    .dexCache(context.getDir("dx", Context.MODE_PRIVATE))
                    .handler(proxy)
                    .build();

            Map<String, Object> sharedValueMap = SharedPrefUtil.getAll(context, sharedPrefFileName);
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

    public static void saveModel(Context context, Object object, String prefFileName){
        Map<String, FieldEntity> fieldMap = ReflectUtil.getFiled(object.getClass());
        for(String key : fieldMap.keySet()) {
            FieldEntity fieldEntity = fieldMap.get(key);
            SharedPrefUtil.putValue(context,
                    prefFileName,
                    fieldEntity.getFieldName(),
                    ToStringUtil.objectToCanSaveObject(fieldEntity, fieldEntity.getValue()));
        }
    }

    public static void setLog(boolean open) {
        LogUtil.setDebug(open);
    }
}
