# AutoSharedPref
android的SharedPreferences的代理Model生成工具

## 添加到Android studio
Step1: 在根build.gradle中添加仓库：
```groovy
allprojects {
	repositories {
        jcenter()
		maven { url "https://jitpack.io" }
	}
}
```

Step2: 在工程中添加依赖：
```groovy
dependencies {
    compile 'com.github.Yumenokanata:AutoSharedPref:1.2.1'
}
```

## 使用方法
1. 定义一个POJO：
```java
public class SharedPrefModel {
    @Ignore
    private String text;

    private List<ServiceEntity> list = new ArrayList<>();
    
    public List<ServiceEntity> getList() {
        return list;
    }

    public void setList(List<ServiceEntity> list) {
        this.list = list;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
```
此POJO类中即定义了SharedPreferences中保存的字段
类型目前支持基本类型、String与List、Map、Set三种容器类型（其中容器中的类型也必须为基本类型或String），如若不是以上类型则会尝试使用Gson进行序列化和反序列化。（如有需要以后可能会开放TypeConvertor接口）

注：本工具是通过反射setter和getter方法对属性值进行获取的，所以私有属性、setter、getter三者缺一不可

2.生成代理类
```java
TestModel model = AutoSharedPref.newModel(context, TestModel.class, "sharedPrefFileName")
```
通过此方法会生成指定了类的一个动态代理类实例，当通过此实例的setter方法对属性值进行设置时会触发保存，工作流程如下：

调用setter方法 -> 触发SharedPreferences保存 -> 通过setter方法获取getter方法名 -> 通过getter方法获取属性值 -> 对属性值进行序列化
-> 将序列化结果保存到SharedPreferences

注：因此setter与getter方法中的代码会被执行


###License
<pre>
Copyright 2015 Yumenokanata

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
</pre>
