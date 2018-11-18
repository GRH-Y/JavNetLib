package connect.json;


import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;
import util.SpeedReflex;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;


/**
 * 解析Json工具类
 *
 * @author yyz
 * @version 1.0
 */
public class JsonUtils implements IJson {

    private final static String NULL = "";
    private final static String CLASS = "class ";

    public static String toJson(Object entity) {

        if (entity == null) {
            return null;
        }
        Class cls = entity.getClass();
        return builderJson(cls, entity);
    }

    private static String builderJson(Class cls, Object entity) {
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append(objectSatTag);
        Field[] fields = cls.getDeclaredFields();

        while (!cls.isAssignableFrom(Object.class)) {

            for (int index = 0; index < fields.length; index++) {

                Field field = fields[index];
                field.setAccessible(true);

                String key = field.getName();
                if ("serialVersionUID".equals(key) || "$change".equals(key) || "this$0".equals(key)) {
                    continue;
                }

                if (jsonBuilder.length() > 1) {
                    jsonBuilder.append(COA_TAG);
                }

                Object object = null;
                try {
                    object = field.get(entity);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                if (object == null) {
                    continue;
                }

                Class typeClx = field.getType();
                if (isBasicDataType(typeClx)) {
                    builderObjKvJson(jsonBuilder, key, object);
                } else {
                    if (typeClx.isAssignableFrom(List.class)) {
                        String layer = builderListJson(object);
                        builderObjKvJson(jsonBuilder, key, layer);
                    } else {
                        String layer = builderJson(typeClx, object);
                        builderObjKvJson(jsonBuilder, key, layer);
                    }
                }
            }

            //递归格式化实体的参数
            cls = cls.getSuperclass();
            if (!cls.isAssignableFrom(Object.class)) {
                fields = cls.getDeclaredFields();
            }
        }

        jsonBuilder.append(objectEndTag);
        return jsonBuilder.toString();
    }

    //====================================== toJson ===================================================


    public static <T> T toEntity(Class<T> cls, byte[] json) {
        if (json == null || cls == null) {
            return null;
        }
        return toEntity(cls, new String(json));
    }

    public static <T> T toEntity(String className, String json) {
        if (className == null || json == null) {
            return null;
        }
        try {
            Class<T> clx = (Class<T>) Class.forName(className);
            return toEntity(clx, json);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T toNewEntity(Class<T> cls, String json) {
        if (json == null || cls == null) {
            return null;
        }
        T result = null;
        try {
            if (!json.startsWith(objectSatTag) || !json.endsWith(objectEndTag)) {
                return null;
            }

            json = json.trim().replace(" ", NULL).replace("\n", NULL).replace(DQM_SLASH, NULL);
            SpeedReflex reflex = SpeedReflex.getCache();
            reflex.setClass(cls);
            Constructor constructor = cls.getConstructor();
            constructor.setAccessible(true);
            result = (T) constructor.newInstance();

            dismantleJson(cls, result, json);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private static boolean isList(char c) {
        return arraySatTag.charAt(0) == c;
    }

    private static boolean isMap(char c) {
        return objectSatTag.charAt(0) == c;
    }

    private static void dismantleJson(Class cls, Object object, String json) {
        SpeedReflex reflex = SpeedReflex.getCache();
        int baseIndex = 0;

        while (!cls.isAssignableFrom(Object.class)) {
            Field[] fields = reflex.getAllField(cls);

            for (Field field : fields) {
                String key = field.getName();
                if ("serialVersionUID".equals(key) || "$change".equals(key) || "this$0".equals(key)) {
                    continue;
                }

                field.setAccessible(true);
                Class type = field.getType();
                int startIndex = json.indexOf(key, baseIndex);
                boolean isBasicType = isBasicDataType(type);

                int valueIndex = key.length() + startIndex + 1;
                if (isList(json.charAt(valueIndex)) && !isBasicType) {
                    int endIndex = json.indexOf(arrayEndTag, valueIndex) + 1;
                    String listJson = json.substring(valueIndex, endIndex);
                    List itemList = new ArrayList<>();
                    int listPoint = 0;
                    do {
                        int index = listJson.indexOf(objectSatTag, listPoint);
                        if (index < 0) {
                            break;
                        }
                        try {
                            Type genericType = field.getGenericType();
                            Constructor constructor = null;
                            Class actualType = null;
                            if (genericType instanceof ParameterizedTypeImpl) {
                                ParameterizedTypeImpl parameterizedType = (ParameterizedTypeImpl) genericType;
                                actualType = (Class) parameterizedType.getActualTypeArguments()[0];
                                constructor = actualType.getDeclaredConstructor(cls);
                            }
                            if (constructor != null) {
                                constructor.setAccessible(true);
                                Object itemObj = constructor.newInstance(object);
                                int itemEndIndex = listJson.indexOf(objectEndTag, index) + 1;
                                String itemJson = listJson.substring(index, itemEndIndex);
                                dismantleJson(actualType, itemObj, itemJson);
                                itemList.add(itemObj);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        listPoint = endIndex;

                    } while (true);

                    try {
                        field.set(object, itemList);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    if (baseIndex == 0 && startIndex < 4) {
                        baseIndex = endIndex;
                    } else if (baseIndex + 6 <= startIndex) {
                        baseIndex = endIndex;
                    }
                } else if (isMap(json.charAt(valueIndex)) && !isBasicType) {

                    int endIndex = json.indexOf(objectEndTag, valueIndex) + 1;
                    String itemJson = json.substring(valueIndex, endIndex);

                    try {
                        Constructor constructor = type.getDeclaredConstructor(cls);
                        constructor.setAccessible(true);
                        Object itemObj = constructor.newInstance(object);
                        dismantleJson(type, itemObj, itemJson);
                        field.set(object, itemObj);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (baseIndex == 0 && startIndex < 4) {
                        baseIndex = endIndex;
                    } else if (baseIndex + 6 <= startIndex) {
                        baseIndex = endIndex;
                    }
                } else if (isBasicType) {
                    int endIndex = json.indexOf(COA_TAG, valueIndex);
                    if (endIndex < 0) {
                        endIndex = json.indexOf(objectEndTag, valueIndex);
                    }
                    String value = json.substring(valueIndex, endIndex);
                    try {
                        field.set(object, value);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }

                    if (baseIndex == 0 && startIndex < 4) {
                        baseIndex = endIndex;
                    } else if (baseIndex + 6 <= startIndex) {
                        baseIndex = endIndex;
                    }
                }


            }
            cls = cls.getSuperclass();
        }
    }

    public static <T> T toEntity(Class<T> cls, String json) {
        if (json == null || cls == null) {
            return null;
        }
        T result = null;
        try {
            Constructor constructor = cls.getConstructor();
            constructor.setAccessible(true);
            Object entity = constructor.newInstance();
            Stack<KeyValue> stack = analysisJson(json);
            if (stack == null) {
                return null;
            }
            for (int index = stack.size() - 1; index >= 0; index--) {
                KeyValue keyValue = stack.get(index);
                if (!keyValue.isList && keyValue.key == null) {
                    analysisMap(cls, entity, keyValue.value);
                } else {
                    Field keyField = cls.getDeclaredField(keyValue.key);
                    keyField.setAccessible(true);
                    Type type = keyField.getGenericType();
//                    String name = type.toString();
//                    int startTask = name.indexOf("<");
//                    int end = keyValue.isList ? name.length() - 1 : name.length();
//                    String className = name.substring(startTask + 1, end).replace(CLASS, NULL);
                    String typeName = getClassFieldTypeName(type);
                    if (keyValue.isList) {
                        //对象是list
                        //获取内部类
                        List list = new ArrayList<>();
                        keyField.set(entity, list);
                        analysisList(typeName, entity, list, keyValue.value);
                    } else {
                        //对象不是list，而是内部类
//                        Class insideClass = Class.forName(typeName);
//                        Constructor constructor = insideClass.getDeclaredConstructor();
//                        constructor.setAccessible(true);
//                        Object newEntity = constructor.newInstance(entity);
                        keyField.set(entity, keyValue.value);
//                        analysisMap(insideClass, newEntity, keyValue.value);
                    }
                }
            }
            result = (T) entity;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    private static void analysisList(String className, Object entity, List list, String value) throws Exception {
        if (value != null) {
            value = value.replace("},{", ">#<").replace(objectSatTag, NULL).replace(objectEndTag, NULL);
            String[] split = value.split(">#<");
            if (String.class.getName().equals(className) || Long.class.getName().equals(className) || Double.class.getName().equals(className)) {
                split = value.split(COA_TAG);
                for (String str : split) {
                    list.add(str);
                }
            } else {
                boolean isStaticClass = className.indexOf("$") != -1;
                for (String map : split) {
                    Class insideClass = Class.forName(className);
                    //获取内部类构造方法
                    Constructor constructor = insideClass.getDeclaredConstructors()[0];
                    constructor.setAccessible(true);
                    Object newEntity = isStaticClass ? constructor.newInstance() : constructor.newInstance(entity);
                    list.add(newEntity);
                    String[] strings = map.split(COA_TAG);
                    for (String str : strings) {
                        analysisMap(insideClass, newEntity, str);
                    }
                }
            }
        }
    }

    private static void analysisMap(Class cls, Object entity, String value) {
        if (value != null) {
            String[] split = value.split(COA_TAG);
            for (String str : split) {
                String[] strings = str.split(DQM_TAG);
                String tmp = NULL;
                if (strings.length > 2) {
                    tmp = str.substring(strings[0].length() + 1, str.length());
                } else {
                    if (strings.length == 2) {
                        tmp = strings[1];
                    }
                }
                try {
                    Field field = cls.getDeclaredField(strings[0]);
                    setValue(field, entity, tmp);
                } catch (Exception e) {
                    setSuperValue(cls, strings[0], entity, tmp);
                }
            }
        }
    }

    /**
     * 赋值给变量
     *
     * @param field  变量
     * @param entity 实体
     * @param value  值
     */
    private static void setValue(Field field, Object entity, String value) throws IllegalAccessException {
        field.setAccessible(true);
        Type type = field.getGenericType();
        Object obj = classNameToClassValue(type.toString().replace(CLASS, NULL), value);
        field.set(entity, obj);
    }

    /**
     * 给父类变量赋值
     *
     * @param superClass 父类
     * @param name       变量名
     * @param entity     实体
     * @param value      值
     */
    private static void setSuperValue(Class superClass, String name, Object entity, String value) {
        if (superClass != null && !superClass.isAssignableFrom(Object.class)) {
            try {
                Field field = superClass.getDeclaredField(name);
                setValue(field, entity, value);
            } catch (Exception e1) {
                setSuperValue(superClass.getSuperclass(), name, entity, value);
            }
        }
    }


    private static class KeyValue {
        boolean isList;
        String key;
        String value;
        String source;
        int comma;
        int start;
        int end;
    }


    /**
     * 分析json
     *
     * @param json json数据
     * @return 返回分析后的json结构
     */
    private static Stack<KeyValue> analysisJson(String json) {
        Stack<KeyValue> stack = null;
        if (json != null && json.startsWith(objectSatTag) && json.endsWith(objectEndTag)) {
            json = json.trim().replace(" ", NULL).replace("\n", NULL).replace(DQM_SLASH, NULL);
            char[] chars = json.toCharArray();
            stack = new Stack<>();
            int coaIndex = 0;
            //是否检测到list集合
            boolean isHasList = false;
            //是否检测到map集合
            boolean isHasMap = false;
            for (int index = 0; index < chars.length; index++) {
                if (COA_TAG.toCharArray()[0] == chars[index]) {
                    //记录逗号上次出现的索引值
                    coaIndex = index;
                } else if (arraySatTag.toCharArray()[0] == chars[index] && !isHasMap) {
                    isHasList = true;
                    KeyValue value = new KeyValue();
                    value.isList = true;
                    value.start = index;
                    value.comma = coaIndex;
                    value.key = json.substring(coaIndex + 1, index - 1);
                    stack.push(value);
                } else if (objectSatTag.toCharArray()[0] == chars[index] && !isHasList) {
                    //目前没有检测到list集合
                    KeyValue value = new KeyValue();
                    //记录'{' 号出现的索引值
                    value.start = index;
                    //不是第一次运行
                    if (coaIndex > 0) {
                        isHasMap = true;
                        if (arraySatTag.toCharArray()[0] == chars[(index - 1)]) {
                            //如果检测list集合
                            //修正[索引值
                            value.start--;
                            //标记检测到list集合
                            isHasList = true;
                        }
                        //记录上次逗号出现的索引值
                        value.comma = coaIndex;
                        //获取key
                        if (isHasList) {
                            value.key = json.substring(coaIndex + 1, index - 2);
                            value.isList = true;
                        } else {
                            value.key = json.substring(coaIndex + 1, index - 1);
                        }
                    }
                    if (index == 0) {
                        value.end = json.length();
                        value.value = json.substring(value.start + 1, value.end - 1);
                        value.source = json;
                    }
                    stack.push(value);
                } else if (objectEndTag.toCharArray()[0] == chars[index]) {
                    KeyValue firstValue = stack.firstElement();
                    for (int count = stack.size() - 1; count >= 1; count--) {
                        KeyValue v = stack.get(count);
                        if (index + 1 != chars.length) {
                            if (arrayEndTag.toCharArray()[0] == chars[(index + 1)]) {
                                isHasList = false;
                                v.end = index + 1;
                            }
                            if (!isHasList) {
                                isHasMap = false;
                                v.end = index;
                                int newEnd = v.isList ? v.end + 1 : v.end;
                                v.value = json.substring(v.start + 1, newEnd);
                                v.source = json.substring(v.comma, newEnd + 1);
                                firstValue.value = firstValue.value.replace(v.source, "");
                            }
                            break;
                        }
                    }
                } else if (arrayEndTag.toCharArray()[0] == chars[index]) {
                    isHasList = false;
                    KeyValue lastValue = stack.lastElement();
                    lastValue.end = index;
                    lastValue.value = json.substring(lastValue.start + 1, lastValue.end);
                    lastValue.source = json.substring(lastValue.comma, lastValue.end + 1);
                    for (int count = stack.size() - 2; count >= 0; count--) {
                        KeyValue v = stack.get(count);
                        v.value = v.value.replace(lastValue.source, NULL);
                    }
                }
            }
        }
        return stack;
    }

    private static String getClassFieldTypeName(Type type) {
        String name = type.toString();
        boolean isList = name.contains("List");
        int start = name.indexOf("<");
        int end = isList ? name.length() - 1 : name.length();
        return name.substring(start + 1, end).replace(CLASS, NULL);
    }

    private static boolean isBasicDataType(Class clx) {
        return clx == Integer.class || clx == int.class || clx == Long.class || clx == long.class
                || clx == Double.class || clx == double.class || clx == Float.class || clx == float.class
                || clx == Boolean.class || clx == boolean.class || clx == char.class || clx == Character.class
                || clx == String.class;
    }

    /**
     * 字符串转指定类型的数据
     *
     * @param typeSimpleName 指定的类型
     * @param value          要转换的字符串
     * @return 返回转换后的数据类型
     */
    private static Object classNameToClassValue(String typeSimpleName, String value) {
        Object object = null;
        if (Integer.class.getName().equals(typeSimpleName) || int.class.getName().equals(typeSimpleName)) {
            object = Integer.parseInt(value);
        } else if (Double.class.getName().equals(typeSimpleName) || double.class.getName().equals(typeSimpleName)) {
            object = Double.parseDouble(value);
        } else if (Long.class.getName().equals(typeSimpleName) || long.class.getName().equals(typeSimpleName)) {
            object = Long.parseLong(value);
        } else if (Boolean.class.getName().equals(typeSimpleName) || boolean.class.getName().equals(typeSimpleName)) {
            object = Boolean.parseBoolean(value);
        } else if (Float.class.getName().equals(typeSimpleName) || float.class.getName().equals(typeSimpleName)) {
            object = Float.parseFloat(value);
        } else if (String.class.getName().equals(typeSimpleName)) {
            object = value;
        }
        return object;
    }

    public static Class classNameToClass(String typeSimpleName) {
        Class object = null;
        if (Integer.class.getName().equals(typeSimpleName) || int.class.getName().equals(typeSimpleName)) {
            object = Integer.class;
        } else if (Double.class.getName().equals(typeSimpleName) || double.class.getName().equals(typeSimpleName)) {
            object = Double.class;
        } else if (Long.class.getName().equals(typeSimpleName) || long.class.getName().equals(typeSimpleName)) {
            object = Long.class;
        } else if (Boolean.class.getName().equals(typeSimpleName) || boolean.class.getName().equals(typeSimpleName)) {
            object = Boolean.class;
        } else if (Float.class.getName().equals(typeSimpleName) || float.class.getName().equals(typeSimpleName)) {
            object = Float.class;
        } else if (String.class.getName().equals(typeSimpleName)) {
            object = String.class;
        }
        return object;
    }

    private static String builderListJson(Object values) {
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append(arraySatTag);

        List list = (List) values;
        Object[] array = list.toArray();
        for (int index = 0; index < array.length; index++) {
            Object object = array[index];
            String json = builderJson(object.getClass(), object);
            jsonBuilder.append(json);
            if (index + 1 != array.length) {
                jsonBuilder.append(COA_TAG);
            }
        }

        jsonBuilder.append(arrayEndTag);
        return jsonBuilder.toString();
    }

    private static String formatValues(Object values) {
        if (values instanceof String) {
            StringBuilder builder = new StringBuilder();
            String content = (String) values;
            if (content != null) {
                if (content.startsWith(arraySatTag) || content.startsWith(objectSatTag)) {
                    builder.append(values);
                } else {
                    builder.append(DQM_SLASH);
                    builder.append(values);
                    builder.append(DQM_SLASH);
                }
            }
            return builder.toString();
        }
        return String.valueOf(values);
    }

    private static void builderObjKvJson(StringBuilder builder, String key, Object values) {
        builder.append(DQM_SLASH);
        builder.append(key);
        builder.append(DQM_SLASH);
        builder.append(DQM_TAG);
        builder.append(formatValues(values));
    }
}
