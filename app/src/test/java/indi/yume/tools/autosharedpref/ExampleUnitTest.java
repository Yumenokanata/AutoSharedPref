package indi.yume.tools.autosharedpref;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import indi.yume.tools.autosharedpref.model.FieldEntity;
import indi.yume.tools.autosharedpref.util.JsonUtil;
import indi.yume.tools.autosharedpref.util.ReflectUtil;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    @Test
    public void testReflectUtil() throws Exception {
        TestModel testModel = new TestModel();
        Map<String, FieldEntity> map = ReflectUtil.getFiledAndValue(testModel);

        String testS = "test ";
        Map<String, Object> valueMap = new HashMap<>();
        for(Map.Entry<String, FieldEntity> entry : map.entrySet()) {
            System.out.println(entry.getKey() + "      |         " + entry.getValue().getValue());
            Object value = entry.getValue().getValue();
            if(value instanceof String) {
                value = testS + value;
            } else if(value instanceof Boolean){
                value = !((Boolean)value);
            }
            valueMap.put(entry.getKey(), value);
        }
        ReflectUtil.setFiledAndValue(valueMap, testModel, TestModel.class);

        map = ReflectUtil.getFiledAndValue(testModel);
        for(Map.Entry<String, FieldEntity> entry : map.entrySet())
            System.out.println(entry.getKey() + "      |         " + entry.getValue().getValue());
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

        long time = System.currentTimeMillis();
        String json = "";
        for(int i = 0; i < 500; i++)
            json = JsonUtil.start()
                .add("key1", true)
                .add("key2", 1.0)
                .add("key3", -1200l)
                .add("key4", "object test")
                .add("key5", JsonUtil.startArray()
                        .add(false)
                        .add(9.6)
                        .add(-199l)
                        .add("array object test")
                        .add(new TestModel())
                        .add(JsonUtil.start()
                                .add("sub key", "sub object"))
                )
                .add("key6", 999)
                .add("key7", new TestModel())
                .end();
        System.out.println("spend " + (System.currentTimeMillis() - time));
        System.out.println("=============================");
        System.out.println(json);

//        time = System.currentTimeMillis();
//        for(int i = 0; i < 500; i++) {
//            JSONObject jsonObject = new JSONObject();
//            json = jsonObject.put("key1", true)
//                    .put("key2", 1.0)
//                    .put("key3", -1200l)
//                    .put("key4", "object test")
//                    .put("key5", new JSONArray()
//                            .put(false)
//                            .put(9.6)
//                            .put(-199l)
//                            .put("array object test")
//                            .put(new TestModel())
//                            .put(new JSONObject().put("sub key", "sub object")))
//                    .put("key6", 999)
//                    .put("key7", new TestModel())
//                    .toString();
//        }
//        System.out.println("spend " + (System.currentTimeMillis() - time));
//        System.out.println("=============================");
//        System.out.println(json);

        JSONObject jsonObject = new JSONObject(json);
    }

    @Test
    public void testStringWrite() {
        long time = System.currentTimeMillis();
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0; i < 20000; i++)
            stringBuilder.append("e");
        String s = stringBuilder.toString();
        System.out.println("StringBuilder spend " + (System.currentTimeMillis() - time));
        System.out.println("=============================");

        time = System.currentTimeMillis();
        StringWriter stringWriter = new StringWriter();
        for(int i = 0; i < 20000; i++)
            stringWriter.write("e");
        s = stringWriter.toString();
        System.out.println("StringWriter spend " + (System.currentTimeMillis() - time));
        System.out.println("=============================");
    }
}