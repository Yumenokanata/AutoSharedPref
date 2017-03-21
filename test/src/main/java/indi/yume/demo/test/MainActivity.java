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
import java.util.HashMap;
import java.util.Map;

import indi.yume.tools.autosharedpref.AutoSharedPref;
import indi.yume.tools.autosharedpref.model.Action1;
import indi.yume.tools.autosharedpref.model.FieldEntity;
import indi.yume.tools.autosharedpref.sharedpref.SharedPrefImpl;
import indi.yume.tools.autosharedpref.util.JsonUtil;
import indi.yume.tools.autosharedpref.util.ToStringUtil;

public class MainActivity extends AppCompatActivity {
    String sharedFileName = "Test_Shared";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        testShared();
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

        System.out.println("spend: " + (System.currentTimeMillis() - time));
        System.out.println("============");
    }
}
