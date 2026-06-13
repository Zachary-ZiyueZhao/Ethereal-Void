package com.mjzaymi.etherealvoid.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class ReflectUtil {

    public static List<Field> getStaticFinalFields(Class<?> clazz) {
        List<Field> result = new ArrayList<>();

        for (Field field : clazz.getDeclaredFields()) {
            int mod = field.getModifiers();

            if (Modifier.isStatic(mod) && Modifier.isFinal(mod)) {
                result.add(field);
            }
        }

        return result;
    }
}