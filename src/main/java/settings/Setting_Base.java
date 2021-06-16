package settings;

import java.lang.reflect.Field;

public class Setting_Base {
    protected void setField(String fieldName, String value, Class classs) throws IllegalAccessException {
        for (Field f : classs.getDeclaredFields()) {
            CustomAnnotation column = f.getAnnotation(CustomAnnotation.class);
            if (column != null) {
                String key = column.key();
                if (key.equals(fieldName)) {
                    Class<?> type = f.getType();
                    if (type.equals(Integer.class)) {
                        f.set(this, Integer.valueOf(value));
                    } else if (type.equals(Double.class)) {
                        f.set(this, Double.parseDouble(value));
                    } else if (type.equals(String.class)) {
                        f.set(this, value);
                    }
                }
            }
        }
    }
}
