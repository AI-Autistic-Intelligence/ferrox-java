package dev.ferrox.crudgen.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface FerroxEntity {
    String table() default "";
    String roleRead() default "User";
    String roleWrite() default "Admin";
    boolean adminGrid() default true;
    boolean adminKanban() default false;
}
