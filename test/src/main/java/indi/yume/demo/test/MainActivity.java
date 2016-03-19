package indi.yume.demo.test;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import indi.yume.tools.autosharedpref.AutoSharedPref;
import indi.yume.tools.autosharedpref.model.Action1;
import indi.yume.tools.autosharedpref.model.FieldEntity;
import indi.yume.tools.autosharedpref.util.JsonUtil;
import indi.yume.tools.autosharedpref.util.ReflectUtil;
import indi.yume.tools.autosharedpref.util.SharedPrefUtil;
import indi.yume.tools.autosharedpref.util.ToStringUtil;

public class MainActivity extends AppCompatActivity {
    String sharedFileName = "Test_Shared";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button startButton1 = (Button) findViewById(R.id.start_button1);
        Button startButton2 = (Button) findViewById(R.id.start_button2);
        Button startButton3 = (Button) findViewById(R.id.start_button3);
        Button startButton4 = (Button) findViewById(R.id.start_button4);
        Button startButton5 = (Button) findViewById(R.id.start_button5);
        Button startButton6 = (Button) findViewById(R.id.start_button6);
        startButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AutoSharedPref.setUseCache(false);
                System.out.println("useCache false");
                testShared();
            }
        });
        startButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AutoSharedPref.setUseCache(true);
                System.out.println("useCache");
                testShared();
            }
        });
        startButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("editor.apply()");
                testOrigin();
            }
        });
        startButton4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("SharedPrefUtil.putValue");
                testOrigin2();
            }
        });
        startButton5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("testToString gson");
                testToString(true);
            }
        });
        startButton6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("testToString");
                testToString(false);

//                testStringWrite();

//                System.out.println("testToJson");
//                try {
//                    testToJson();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
            }
        });
    }

    public void testStringWrite() {
        long time = System.currentTimeMillis();
        StringBuilder stringBuilder = new StringBuilder(20000);
        for(int i = 0; i < 20000; i++)
            stringBuilder.append("e");
        String s = stringBuilder.toString();
        long jsonUtilTime = System.currentTimeMillis() - time;
        System.out.println("StringBuilder spend " + jsonUtilTime);
        System.out.println("=============================");

        time = System.currentTimeMillis();
        StringWriter stringWriter = new StringWriter(20000);
        for(int i = 0; i < 20000; i++)
            stringWriter.write("e");
        s = stringWriter.toString();
        long jsonObjectTime = System.currentTimeMillis() - time;
        System.out.println("StringWriter spend " + jsonObjectTime);
        System.out.println("=============================");
    }

    public void testToJson() throws Exception {
//        JSONObject jsonObject = new JSONObject();
//
//        JSONArray jsonArray = new JSONArray();
//        for(int i = 0; i < 4; i++) {
//            JSONObject jo = new JSONObject();
//            jo
//        }
        String json = "";
        long time = System.currentTimeMillis();
        for(int i = 0; i < 500; i++)
            json = JsonUtil.start()
                    .add("key1", true)
                    .add("key2", 1.0)
                    .add("key3", -1200l)
                    .add("key4", "object test")
                    .addArray("key5", new Action1<JsonUtil.JsonArrayBuilder>() {
                        @Override
                        public void call(JsonUtil.JsonArrayBuilder value) {
                            value.add(false)
                                    .add(9.6)
                                    .add(-199l)
                                    .add("array object test")
                                    .add(new TestModel())
                                    .addJson(new Action1<JsonUtil.JsonBuilder>() {
                                        @Override
                                        public void call(JsonUtil.JsonBuilder value) {
                                            value.add("sub key", "sub object");
                                        }
                                    });
                        }
                    })
                    .add("key6", 999)
                    .add("key7", new TestModel())
                    .end();
        long jsonUtilTime = System.currentTimeMillis() - time;
        System.out.println("spend " + jsonUtilTime);
        System.out.println("=============================");
        System.out.println(json);

        time = System.currentTimeMillis();
        for(int i = 0; i < 500; i++) {
            JSONObject jsonObject = new JSONObject();
            json = jsonObject.put("key1", true)
                    .put("key2", 1.0)
                    .put("key3", -1200l)
                    .put("key4", "object test")
                    .put("key5", new JSONArray()
                            .put(false)
                            .put(9.6)
                            .put(-199l)
                            .put("array object test")
                            .put(new TestModel())
                            .put(new JSONObject().put("sub key", "sub object")))
                    .put("key6", 999)
                    .put("key7", new TestModel())
                    .toString();
        }
        long jsonObjectTime = System.currentTimeMillis() - time;
        System.out.println("spend " + jsonObjectTime);
        System.out.println("=============================");
        System.out.println(json);

        JSONObject jsonObject = new JSONObject(json);
    }

    private void testToString(boolean isGson) {
        Map<String, String> map = new HashMap<>();
        for(int i = 0; i < 400; i++)
            map.put("key" + i, "value1" + i);
        FieldEntity fieldEntity = new FieldEntity();
        fieldEntity.setFieldName("map");
        fieldEntity.setType(Map.class);
        fieldEntity.setGenericType(String.class);

        Map<Integer, String> intMap = new HashMap<>();
        for(int i = 0; i < 400; i++)
            intMap.put(i, "value1" + i);
        FieldEntity intFieldEntity = new FieldEntity();
        intFieldEntity.setFieldName("map");
        intFieldEntity.setType(Map.class);
        intFieldEntity.setGenericType(Integer.class);

        long time = System.currentTimeMillis();
        if(isGson)
            for(int i = 0; i < 500; i++)
                ToStringUtil.objectToCanSaveObject(intFieldEntity, intMap);
        else
            for(int i = 0; i < 500; i++)
                ToStringUtil.objectToCanSaveObject(fieldEntity, map);

        System.out.println("spend: " + (System.currentTimeMillis() - time));
        System.out.println("============");
    }

    private void test() {
        System.out.println("============================================================");
        for(int i = 0; i < 2; i++) {
            AutoSharedPref.setUseCache(false);
            System.out.println("useCache false");
            testShared();
            AutoSharedPref.setUseCache(true);
            System.out.println("useCache");
            testShared();
            System.out.println("editor.apply()");
            testOrigin();
            System.out.println("SharedPrefUtil.putValue");
            testOrigin2();
            System.out.println("testOrigin1");
            testOrigin1();
            System.out.println("============================================================");
//            System.out.println("editor.commit()");
//            testOrigin3();
        }
    }

    private void testShared() {
        long time = System.currentTimeMillis();
        TestModel testModel = AutoSharedPref.newModel(this, TestModel.class, sharedFileName);

        for(int i = 0; i < 1000; i++) {
            testModel.setF_name(i + "Test F_Name");
            testModel.setFName(i + "Test_FName");
            testModel.setName(i + "Test_Name");
            testModel.setLName(i + "Test_LName");
            testModel.setIsMan(true);
            testModel.setIsWomen(false);
            testModel.setMen(true);
            testModel.setSex(false);
        }
//        System.out.println("============");
        System.out.println("spend: " + (System.currentTimeMillis() - time));
        System.out.println("============");

//        Map<String, FieldEntity> map = ReflectUtil.getFiledAndValue(testModel);
//        for(Map.Entry<String, FieldEntity> entry : map.entrySet())
//            System.out.println(entry.getKey() + "   |  " + entry.getValue().getValue());
//
//        System.out.println("------------");
//        Map<String, Object> data = SharedPrefUtil.getAll(this, sharedFileName);
//        for(Map.Entry<String, Object> entry : data.entrySet())
//            System.out.println(entry.getKey() + "   |  " + entry.getValue().toString());
//        System.out.println("============");
    }

    private void testOrigin() {
        long time = System.currentTimeMillis();
        SharedPreferences sharedPreferences = getSharedPreferences(sharedFileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        for(int i = 0; i < 1000; i++) {
            editor.putString("f_name", i + "Test F_Name");
            editor.apply();
            editor.putString("fName", i + "Test_FName");
            editor.apply();
            editor.putString("name", i + "Test_Name");
            editor.apply();
            editor.putString("LName", i + "Test_LName");
            editor.apply();
            editor.putBoolean("isMan", true);
            editor.apply();
            editor.putBoolean("isWomen", false);
            editor.apply();
            editor.putBoolean("men", true);
            editor.apply();
            editor.putBoolean("sex", false);
            editor.apply();
        }

//        System.out.println("============");
        System.out.println("spend: " + (System.currentTimeMillis() - time));
        System.out.println("============");
    }

    private void testOrigin1() {
        long time = System.currentTimeMillis();
        SharedPreferences sharedPreferences = getSharedPreferences(sharedFileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        for(int i = 0; i < 1000; i++) {
            editor.putString("f_name", i + "Test F_Name");
            editor.putString("fName", i + "Test_FName");
            editor.putString("name", i + "Test_Name");
            editor.putString("LName", i + "Test_LName");
            editor.putBoolean("isMan", true);
            editor.putBoolean("isWomen", false);
            editor.putBoolean("men", true);
            editor.putBoolean("sex", false);
            editor.apply();
        }

//        System.out.println("============");
        System.out.println("spend: " + (System.currentTimeMillis() - time));
        System.out.println("============");
    }

    private void testOrigin2() {
        long time = System.currentTimeMillis();

        for(int i = 0; i < 1000; i++) {
            SharedPrefUtil.putValue(this, sharedFileName, "f_name", i + "Test F_Name");
            SharedPrefUtil.putValue(this, sharedFileName, "f_name", i + "Test F_Name");
            SharedPrefUtil.putValue(this, sharedFileName, "fName", i + "Test_FName");
            SharedPrefUtil.putValue(this, sharedFileName, "name", i + "Test_Name");
            SharedPrefUtil.putValue(this, sharedFileName, "LName", i + "Test_LName");
            SharedPrefUtil.putValue(this, sharedFileName, "isMan", true);
            SharedPrefUtil.putValue(this, sharedFileName, "isWomen", false);
            SharedPrefUtil.putValue(this, sharedFileName, "men", true);
            SharedPrefUtil.putValue(this, sharedFileName, "sex", false);
        }

//        System.out.println("============");
        System.out.println("spend: " + (System.currentTimeMillis() - time));
        System.out.println("============");
    }

    private void testOrigin4() {
        long time = System.currentTimeMillis();

        for(int i = 0; i < 1000; i++) {
            SharedPrefUtil.putValue(this, sharedFileName, "f_name",  i + "Test F_Name");
            SharedPrefUtil.putValue(this, sharedFileName, "f_name",  i + "Test F_Name");
            SharedPrefUtil.putValue(this, sharedFileName, "fName",   i + "Test_FName");
            SharedPrefUtil.putValue(this, sharedFileName, "name",    i + "Test_Name");
            SharedPrefUtil.putValue(this, sharedFileName, "LName",   i + "Test_LName");
            SharedPrefUtil.putValue(this, sharedFileName, "isMan",   true);
            SharedPrefUtil.putValue(this, sharedFileName, "isWomen", false);
            SharedPrefUtil.putValue(this, sharedFileName, "men",     true);
            SharedPrefUtil.putValue(this, sharedFileName, "sex",     false);
        }

//        System.out.println("============");
        System.out.println("spend: " + (System.currentTimeMillis() - time));
        System.out.println("============");
    }

    private void testOrigin3() {
        long time = System.currentTimeMillis();
        SharedPreferences sharedPreferences = getSharedPreferences(sharedFileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        for(int i = 0; i < 100; i++) {
            editor.putString("f_name", i + "Test F_Name");
            editor.commit();
            editor.putString("fName", i + "Test_FName");
            editor.commit();
            editor.putString("name", i + "Test_Name");
            editor.commit();
            editor.putString("LName", i + "Test_LName");
            editor.commit();
            editor.putBoolean("isMan", true);
            editor.commit();
            editor.putBoolean("isWomen", false);
            editor.commit();
            editor.putBoolean("men", true);
            editor.commit();
            editor.putBoolean("sex", false);
            editor.commit();
        }

//        System.out.println("============");
        System.out.println("spend: " + (System.currentTimeMillis() - time));
        System.out.println("============");
    }
}
