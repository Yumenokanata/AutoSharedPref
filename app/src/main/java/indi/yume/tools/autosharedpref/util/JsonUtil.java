package indi.yume.tools.autosharedpref.util;

import indi.yume.tools.autosharedpref.model.Action1;

/**
 * Created by yume on 16-3-19.
 *
 * 用于生成Json的工具类，特定情况下相比Gson的Json生成速度快20%,相比原生JSONObject快一倍。
 * 并对内存进行了优化，减少GC。
 * 注意，请采用链式调用，否则可能产生无法预料的结果。
 *
 * eg:
 *    json = JsonUtil.start()
 *              .add("key1", true)
 *              .add("key2", 1.0)
 *              .add("key3", -1200l)
 *              .add("key4", "object test")
 *              .addArray("key6", new Action1<JsonUtil.JsonArrayBuilder>() {
 *                                          @Override
 *                                          public void call(JsonUtil.JsonArrayBuilder value) {
 *                                              value.add(false)
 *                                                      .add(9.6)
 *                                                      .add(-199l)
 *                                                      .add("array object test")
 *                                                      .add(new TestModel())
 *                                                      .addJson(new Action1<JsonUtil.JsonBuilder>() {
 *                                                              @Override
 *                                                              public void call(JsonUtil.JsonBuilder value) {
 *                                                                  value.add("sub key", "sub object");
 *                                                              }
 *                                                          });
 *                                          }
 *                     })
 *              .add("key6", 999)
 *              .add("key7", new TestModel())
 *              .end();
 */
public class JsonUtil {
    private JsonUtil(){}

    public static JsonBuilder start() {
        return new JsonBuilder();
    }

    public static JsonArrayBuilder startArray() {
        return new JsonArrayBuilder();
    }

    public static JsonBuilder start(StringBuilder stringBuilder) {
        return new JsonBuilder(stringBuilder);
    }

    public static JsonArrayBuilder startArray(StringBuilder stringBuilder) {
        return new JsonArrayBuilder(stringBuilder);
    }

    public static class JsonArrayBuilder extends Json {
        private JsonArrayBuilder(){
            super(",", "[", "]");
        }

        private JsonArrayBuilder(StringBuilder stringBuilder) {
            super(",", "[", "]", stringBuilder);
        }

        public JsonArrayBuilder add(Json jsonObject) {
            json.append(jsonObject.end())
                    .append(divider);
            return this;
        }

        public JsonArrayBuilder add(boolean bool) {
            if(bool)
                json.append("true");
            else
                json.append("false");
            json.append(divider);
            return this;
        }

        public JsonArrayBuilder add(double dNum) {
            json.append(dNum)
                    .append(divider);
            return this;
        }

        public JsonArrayBuilder add(int intNum) {
            json.append(intNum)
                    .append(divider);
            return this;
        }

        public JsonArrayBuilder add(long longNum) {
            json.append(longNum)
                    .append(divider);
            return this;
        }

        public JsonArrayBuilder add(String string) {
            json.append("\"")
                    .append(string)
                    .append("\"")
                    .append(divider);
            return this;
        }

        public JsonArrayBuilder addArray(Action1<JsonArrayBuilder> doForArray) {
            JsonArrayBuilder arrayBuilder = getInnerArrayBuilder();
            doForArray.call(arrayBuilder);
            arrayBuilder.close();
            json.append(divider);
            return this;
        }

        public JsonArrayBuilder addJson(Action1<JsonBuilder> doForJson) {
            JsonBuilder jsonBuilder = getInnerJsonBuilder();
            doForJson.call(jsonBuilder);
            jsonBuilder.close();
            json.append(divider);
            return this;
        }

        public JsonArrayBuilder add(Object object) {
            if(object instanceof Json)
                return add((Json) object);

            json.append("\"")
                    .append(object.toString())
                    .append("\"")
                    .append(divider);
            return this;
        }
    }

    public static class JsonBuilder extends Json {
        private static final String split = ":";

        private JsonBuilder(){
            super(",", "{", "}");
        }

        private JsonBuilder(StringBuilder stringBuilder) {
            super(",", "{", "}", stringBuilder);
        }

        private final void put(String key, String value) {
            json.append("\"")
                    .append(key)
                    .append("\"")
                    .append(split)
                    .append(value)
                    .append(divider);
        }

        private final void putString(String key, String value) {
            json.append("\"")
                    .append(key)
                    .append("\"")
                    .append(split)
                    .append("\"")
                    .append(value)
                    .append("\"")
                    .append(divider);
        }

        public JsonBuilder add(String key, Json jsonObject) {
            put(key, jsonObject.end());
            return this;
        }

        public JsonBuilder add(String key, boolean bool) {
            if(bool)
                put(key, "true");
            else
                put(key, "false");
            return this;
        }

        public JsonBuilder add(String key, double dNum) {
            json.append("\"")
                    .append(key)
                    .append("\"")
                    .append(split)
                    .append(dNum)
                    .append(divider);
            return this;
        }

        public JsonBuilder add(String key, int intNum) {
            json.append("\"")
                    .append(key)
                    .append("\"")
                    .append(split)
                    .append(intNum)
                    .append(divider);
            return this;
        }

        public JsonBuilder add(String key, long longNum) {
            json.append("\"")
                    .append(key)
                    .append("\"")
                    .append(split)
                    .append(longNum)
                    .append(divider);
            return this;
        }

        public JsonBuilder add(String key, String value) {
            putString(key, value);
            return this;
        }

        public JsonBuilder addArray(String key, Action1<JsonArrayBuilder> doForArray) {
            json.append("\"")
                    .append(key)
                    .append("\"")
                    .append(split);
            JsonArrayBuilder arrayBuilder = getInnerArrayBuilder();
            doForArray.call(arrayBuilder);
            arrayBuilder.close();
            json.append(divider);
            return this;
        }

        public JsonBuilder addJson(String key, Action1<JsonBuilder> doForJson) {
            json.append("\"")
                    .append(key)
                    .append("\"")
                    .append(split);
            JsonBuilder jsonBuilder = getInnerJsonBuilder();
            doForJson.call(jsonBuilder);
            jsonBuilder.close();
            json.append(divider);
            return this;
        }

        public JsonBuilder add(String key, Object object) {
            if(object instanceof Json)
                return add(key, (Json) object);

            putString(key, object.toString());
            return this;
        }
    }

    public static abstract class Json {
        protected StringBuilder json;
        protected String divider;
        protected String startChar;
        protected String endChar;

        private JsonBuilder innerJsonBuilder;
        private JsonArrayBuilder innerArrayBuilder;

        private Json(String divider, String startChar, String endChar, StringBuilder stringBuilder) {
            if(divider.length() != endChar.length() || divider.length() != 1)
                throw new RuntimeException("divider and startChar must be one char");

            this.divider = divider;
            this.startChar = startChar;
            this.endChar = endChar;

            json = stringBuilder.append(startChar);
        }

        protected Json(String divider, String startChar, String endChar) {
            this(divider, startChar, endChar, new StringBuilder());
        }

        protected void restart(StringBuilder stringBuilder) {
            json = stringBuilder.append(startChar);
            if(innerJsonBuilder != null)
                innerJsonBuilder.json = json;
            if(innerArrayBuilder != null)
                innerArrayBuilder.json = json;
        }

        protected JsonBuilder getInnerJsonBuilder() {
            if(innerJsonBuilder == null)
                innerJsonBuilder = new JsonBuilder(json);
            return innerJsonBuilder;
        }

        protected JsonArrayBuilder getInnerArrayBuilder() {
            if(innerArrayBuilder == null)
                innerArrayBuilder = new JsonArrayBuilder(json);
            return innerArrayBuilder;
        }

        public String end() {
            if(json.length() > 1) {
                json.replace(json.length() - 1, json.length(), endChar);
                String jsonString = json.toString();
                json.replace(json.length() - 1, json.length(), divider);
                return jsonString;
            }
            String jsonString = json.append(endChar).toString();
            json.deleteCharAt(json.length() - 1);
            return jsonString;
        }

        protected void close() {
            json.replace(json.length() - 1, json.length(), endChar);
        }
    }
}
