package connect.json;


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

    public static String toNewJson(Object entity) {
        String json = null;
        if (entity == null) {
            return json;
        }
        StringBuilder builder = new StringBuilder();
        builder.append(objectSatTag);
        Class<?> cls = entity.getClass();
        Field[] fields = cls.getDeclaredFields();
        try {
            do {
                for (int index = 0; index < fields.length; index++) {
                    Field field = fields[index];
                    field.setAccessible(true);
                    String key = field.getName();
                    if ("serialVersionUID".equals(key) || "$change".equals(key)) {
                        continue;
                    }
                    Object object = field.get(entity);
                    if (object == null) {
                        continue;
                    }
                    Type type = field.getGenericType();
                    String name = type.toString();
                    boolean isList = name.contains("List");
                    if (isList) {
                        listToJsonStr(builder, key, object);
                    } else {
                        String className = getClassFieldTypeName(type);
                        mapToJsonStr(builder, className, key, object);
                    }
                }
                cls = cls.getSuperclass();
                if (cls.getName().equals(Object.class.getName())) {
                    break;
                } else {
                    fields = cls.getDeclaredFields();
                }
            } while (true);
            if (builder.indexOf(COA_TAG, builder.length() - 1) != -1) {
                builder.deleteCharAt(builder.length() - 1);
            }
        } catch (Exception e) {
            builder = null;
            e.printStackTrace();
        }

        if (builder != null) {
            builder.append(objectEndTag);
            json = builder.toString();
        }
        return json;
    }

    private static String getClassFieldTypeName(Type type) {
        String name = type.toString();
        boolean isList = name.contains("List");
        int start = name.indexOf("<");
        int end = isList ? name.length() - 1 : name.length();
        return name.substring(start + 1, end).replace(CLASS, NULL);
    }

    public static <T> T toEntity(Class<T> cls, byte[] json) {
        if (json == null || cls == null) {
            return null;
        }
        return toEntity(cls.getName(), new String(json));
    }

    public static <T> T toEntity(Class<T> cls, String json) {
        if (json == null || cls == null) {
            return null;
        }
        return toEntity(cls.getName(), json);
    }

    public static <T> T toEntity(String className, String json) {
        if (className == null || json == null) {
            return null;
        }
        T result = null;
        try {
            Class<?> newClass = Class.forName(className);
            Object entity = newClass.newInstance();
            Stack<KeyValue> stack = analysisJson(json);
            if (stack == null) {
                return null;
            }
            for (int index = stack.size() - 1; index >= 0; index--) {
                KeyValue keyValue = stack.get(index);
                if (!keyValue.isList && keyValue.key == null) {
                    analysisMap(newClass, entity, keyValue.value);
                } else {
                    Field keyField = newClass.getDeclaredField(keyValue.key);
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
                        Class insideClass = Class.forName(typeName);
                        Constructor constructor = insideClass.getDeclaredConstructors()[0];
                        constructor.setAccessible(true);
                        Object newEntity = constructor.newInstance(entity);
                        keyField.set(entity, newEntity);
                        analysisMap(insideClass, newEntity, keyValue.value);
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
        if (superClass != null && !superClass.getName().equals(Object.class.getName())) {
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

    private static void listToJsonStr(StringBuilder builder, Object key, Object values) {
        try {
            if (values instanceof List) {
                builder.append(DQM_SLASH);
                builder.append(key);
                builder.append(DQM_SLASH);
                builder.append(DQM_TAG);
                builder.append(arraySatTag);
                List list = (List) values;
                Object[] array = list.toArray();
                for (Object object : array) {
                    builder.append(objectSatTag);
                    Class clx = object.getClass();
                    Field[] fields = clx.getDeclaredFields();
                    for (Field field : fields) {
                        field.setAccessible(true);
                        String fieldKey = field.getName();
                        Object fieldValue = field.get(object);
                        Type type = field.getGenericType();
                        String className = getClassFieldTypeName(type);
                        mapToJsonStr(builder, className, fieldKey, fieldValue);
                    }
                    builder.deleteCharAt(builder.length() - 1);
                    builder.append(objectEndTag);
                    builder.append(COA_TAG);
                }
                builder.deleteCharAt(builder.length() - 1);
                builder.append(arrayEndTag);
                builder.append(COA_TAG);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static String classNameToJsonStr(String className, Object values) {
        if (String.class.getName().equals(className)) {
            StringBuilder builder = new StringBuilder();
            String str = (String) values;
            if (str == null) {
                builder.append(NULL);
            } else {
                if (str.startsWith(arraySatTag) || str.startsWith(objectSatTag)) {
                    builder.append(values);
                } else {
                    builder.append(DQM_SLASH);
                    builder.append(values);
                    builder.append(DQM_SLASH);
                }
                return builder.toString();
            }
        }
        return String.valueOf(values);
    }

    private static void mapToJsonStr(StringBuilder builder, String className, Object key, Object values) {
        builder.append(DQM_SLASH);
        builder.append(key);
        builder.append(DQM_SLASH);
        builder.append(DQM_TAG);
        builder.append(classNameToJsonStr(className, values));
        builder.append(COA_TAG);
    }
}
