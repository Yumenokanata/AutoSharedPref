package indi.yume.demo.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yume on 16/3/18.
 */
public class TestModel {
    private String fName = "fName";
    private String f_name = "f_name";
    private String name = "name";
    private String LName = "<name>";
    private boolean sex = true;
    private boolean isMan = false;
    private Boolean isWomen = true;
    private Boolean men = false;
    private Map<String, String> map = new HashMap<String, String>() {
        {
            for(int i = 0; i < 400; i++)
                put("key" + i, "value1" + i);
        }
    };
    private List<String> list = new ArrayList<String>() {
        {
            for(int i = 0; i < 400; i++)
                add("listItem" + i);
        }
    };

    public String getF_name() {
        return f_name;
    }

    public void setF_name(String f_name) {
        this.f_name = f_name;
    }

    public String getFName() {
        return fName;
    }

    public void setFName(String fName) {
        this.fName = fName;
    }

    public String getfName() {
        return fName;
    }

    public void setfName(String fName) {
        this.fName = fName;
    }

    public boolean isMan() {
        return isMan;
    }

    public void setIsMan(boolean isMan) {
        this.isMan = isMan;
    }

    public Boolean getIsWomen() {
        return isWomen;
    }

    public void setIsWomen(Boolean isWomen) {
        this.isWomen = isWomen;
    }

    public Boolean getMen() {
        return men;
    }

    public void setMen(Boolean men) {
        this.men = men;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLName() {
        return LName;
    }

    public void setLName(String LName) {
        this.LName = LName;
    }

    public boolean isSex() {
        return sex;
    }

    public void setSex(boolean sex) {
        this.sex = sex;
    }

    public Map<String, String> getMap() {
        return map;
    }

    public void setMap(Map<String, String> map) {
        this.map = map;
    }

    public List<String> getList() {
        return list;
    }

    public void setList(List<String> list) {
        this.list = list;
    }
}
