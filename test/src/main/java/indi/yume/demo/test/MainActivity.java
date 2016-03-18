package indi.yume.demo.test;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Objects;

import indi.yume.tools.autosharedpref.AutoSharedPref;
import indi.yume.tools.autosharedpref.model.FieldEntity;
import indi.yume.tools.autosharedpref.util.ReflectUtil;
import indi.yume.tools.autosharedpref.util.SharedPrefUtil;

public class MainActivity extends AppCompatActivity {
    String sharedFileName = "Test_Shared";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        System.out.println("============");
        for(int i = 0; i < 10; i++) {
            AutoSharedPref.setUseCache(false);
            System.out.println("useCache false");
            testShared();
            AutoSharedPref.setUseCache(true);
            System.out.println("useCache");
            testShared();
        }
    }

    private void testShared() {
        long time = System.currentTimeMillis();
        TestModel testModel = AutoSharedPref.newModel(this, TestModel.class, sharedFileName);

        for(int i = 0; i < 10; i++) {
            testModel.setF_name("Test F_Name");
            testModel.setFName("Test_FName");
            testModel.setName("Test_Name");
            testModel.setLName("Test_LName");
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
//        System.out.println("============");
//        Map<String, Object> data = SharedPrefUtil.getAll(this, sharedFileName);
//        for(Map.Entry<String, Object> entry : data.entrySet())
//            System.out.println(entry.getKey() + "   |  " + entry.getValue().toString());
//        System.out.println("============");
    }
}
