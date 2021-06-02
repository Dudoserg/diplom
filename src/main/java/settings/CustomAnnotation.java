package settings;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)     // чтобы использовать для класса
@Retention(RetentionPolicy.RUNTIME)  // хотим чтобы наша аннотация дожила до рантайма
public @interface CustomAnnotation {
    public String key() default "";
}
