package dev.ferrox.crudgen.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface FerroxField {
    boolean isPrimaryKey() default false;
    boolean isSearchable() default false;
    boolean isEditable() default true;
    boolean isAuditLog() default false;
}
