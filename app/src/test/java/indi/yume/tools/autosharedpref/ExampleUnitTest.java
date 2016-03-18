package indi.yume.tools.autosharedpref;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import indi.yume.tools.autosharedpref.model.FieldEntity;
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
}