package dev.ferrox.core.annotation;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates a Ferrox Framework Application.
 * <p>
 * This meta-annotation enables Spring Boot auto-configuration, component scanning,
 * and additionally scans all "dev.ferrox" components by default.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@SpringBootApplication(scanBasePackages = {"dev.ferrox"})
public @interface FerroxApplication {

    /**
     * Alias for the {@link ComponentScan#basePackages()} attribute.
     * Allows for adding additional custom packages to scan beyond the default "dev.ferrox".
     *
     * @return base packages to scan
     */
    @AliasFor(annotation = SpringBootApplication.class, attribute = "scanBasePackages")
    String[] scanBasePackages() default {"dev.ferrox"};
}
