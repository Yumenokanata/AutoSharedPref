package indi.yume.tools.autosharedpref.model;

/**
 * Created by yume on 15/8/25.
 */
public class FieldEntity {
    private String fieldName;
    private Object value;
    private Class type;
    private Class genericType;

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Class getType() {
        return type;
    }

    public void setType(Class type) {
        this.type = type;
    }

    public Class getGenericType() {
        return genericType;
    }

    public void setGenericType(Class genericType) {
        this.genericType = genericType;
    }
}
