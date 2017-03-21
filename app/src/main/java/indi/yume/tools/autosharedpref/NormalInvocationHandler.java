package indi.yume.tools.autosharedpref;

import android.content.Context;

import com.google.dexmaker.stock.ProxyBuilder;

import java.lang.reflect.Method;

import indi.yume.tools.autosharedpref.model.FieldEntity;
import indi.yume.tools.autosharedpref.sharedpref.SharedPrefService;
import indi.yume.tools.autosharedpref.util.ReflectUtil;
import indi.yume.tools.autosharedpref.sharedpref.SharedPrefImpl;
import indi.yume.tools.autosharedpref.util.ToStringUtil;

/**
 * Created by yume on 16-3-18.
 */
public class NormalInvocationHandler implements ProxyInvocationHandler {
    private boolean isCreate = true;
    private Context mContext;
    private String fileName;
    private SharedPrefService prefService;

    public NormalInvocationHandler(Context mContext, String fileName, SharedPrefService prefService) {
        this.mContext = mContext;
        this.fileName = fileName;
        this.prefService = prefService;
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
            FieldEntity fieldEntity = ReflectUtil.getOneFiledAndValueByMethod(proxy, method);
            if(fieldEntity == null)
                return result;

            prefService.putValue(mContext,
                    fileName,
                    fieldEntity.getFieldName(),
                    ToStringUtil.objectToCanSaveObject(fieldEntity, fieldEntity.getValue()));
        }
        return result;
    }
}
