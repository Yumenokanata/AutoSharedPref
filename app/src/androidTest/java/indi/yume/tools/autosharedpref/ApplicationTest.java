package indi.yume.tools.autosharedpref;

import android.app.Application;
import android.test.ApplicationTestCase;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.io.StringWriter;

import indi.yume.tools.autosharedpref.model.Action1;
import indi.yume.tools.autosharedpref.util.JsonUtil;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    @Test
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
                    .addArray("key6", new Action1<JsonUtil.JsonArrayBuilder>() {
                        @Override
                        public void call(JsonUtil.JsonArrayBuilder value) {
                            value.add(false)
                                    .add(9.6)
                                    .add(-199l)
                                    .add("array object test")
                                    .add(new TestModel2())
                                    .addJson(new Action1<JsonUtil.JsonBuilder>() {
                                        @Override
                                        public void call(JsonUtil.JsonBuilder value) {
                                            value.add("sub key", "sub object");
                                        }
                                    });
                        }
                    })
                    .add("key6", 999)
                    .add("key7", new TestModel2())
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
                            .put(new TestModel2())
                            .put(new JSONObject().put("sub key", "sub object")))
                    .put("key6", 999)
                    .put("key7", new TestModel2())
                    .toString();
        }
        long jsonObjectTime = System.currentTimeMillis() - time;
        System.out.println("spend " + jsonObjectTime);
        System.out.println("=============================");
        System.out.println(json);

        JSONObject jsonObject = new JSONObject(json);
    }

    @Test
    public void testStringWrite() {
        long time = System.currentTimeMillis();
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0; i < 20000; i++)
            stringBuilder.append("e");
        String s = stringBuilder.toString();
        long jsonUtilTime = System.currentTimeMillis() - time;
        System.out.println("StringBuilder spend " + jsonUtilTime);
        System.out.println("=============================");

        time = System.currentTimeMillis();
        StringWriter stringWriter = new StringWriter();
        for(int i = 0; i < 20000; i++)
            stringWriter.write("e");
        s = stringWriter.toString();
        long jsonObjectTime = System.currentTimeMillis() - time;
        System.out.println("StringWriter spend " + jsonObjectTime);
        System.out.println("=============================");
    }
}