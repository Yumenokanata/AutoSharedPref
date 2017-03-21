package indi.yume.tools.autosharedpref.sharedpref;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import java.util.Map;

/**
 * Created by yume on 17-3-21.
 */

public interface SharedPrefService {

    void putValue(Context context, String fileName, String key, Object value);

    void putAll(Context context, String fileName, Map<String, Object> map);

    Map<String, Object> getAll(Context context, String filename);

    void putValue(SharedPreferences.Editor editor, String key, Object value);
}
