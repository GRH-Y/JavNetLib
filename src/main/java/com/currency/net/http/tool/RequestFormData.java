package com.currency.net.http.tool;

import util.SpeedReflex;

import java.lang.reflect.Field;

public class RequestFormData {

    private RequestFormData() {
    }

    public static String convert(Object entity) {
        Class cls = entity.getClass();
        SpeedReflex reflex = SpeedReflex.getInstance();
        StringBuilder builder = new StringBuilder();

        while (!cls.isAssignableFrom(Object.class)) {
            Field[] fields = reflex.getAllField(cls);
            for (Field field : fields) {
                String key = field.getName();
                if ("serialVersionUID".equals(key) || "$change".equals(key) || "this$0".equals(key)) {
                    continue;
                }

                try {
                    field.setAccessible(true);
                    Object value = field.get(entity);
                    if (builder.length() > 0) {
                        builder.append("&");
                    }
                    String name = field.getName();
                    builder.append(name);
                    builder.append("=");
                    builder.append(value);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            cls = cls.getSuperclass();
        }
        return builder.toString();
    }

}
