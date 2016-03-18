package indi.yume.tools.autosharedpref;

import android.content.Context;

import com.google.dexmaker.stock.ProxyBuilder;
import com.google.gson.Gson;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import indi.yume.tools.autosharedpref.model.FieldEntity;
import indi.yume.tools.autosharedpref.util.ReflectCacheUtil;
import indi.yume.tools.autosharedpref.util.ReflectUtil;
import indi.yume.tools.autosharedpref.util.SharedPrefUtil;
import indi.yume.tools.autosharedpref.util.ToStringUtil;

/**
 * Created by yume on 16-3-18.
 */
public class CacheInvocationHandler implements ProxyInvocationHandler {
    private boolean isCreate = true;
    private Context mContext;
    private String PREF_FILE_NAME;

    public CacheInvocationHandler(Context mContext, String PREF_FILE_NAME) {
        this.mContext = mContext;
        this.PREF_FILE_NAME = PREF_FILE_NAME;
    }

    @Override
    public void startProxy(){
        isCreate = false;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result = ProxyBuilder.callSuper(proxy, method, args);
        if(isCreate)
            return result;

        if(method.getName().startsWith("set")) {
            FieldEntity fieldEntity = ReflectCacheUtil.getOneFiledAndValueByMethod(proxy, method);
            if(fieldEntity == null)
                return result;

            SharedPrefUtil.putValue(mContext,
                    PREF_FILE_NAME,
                    fieldEntity.getFieldName(),
                    ToStringUtil.objectToCanSaveObject(fieldEntity, fieldEntity.getValue()));
        }
        return result;
    }
}
