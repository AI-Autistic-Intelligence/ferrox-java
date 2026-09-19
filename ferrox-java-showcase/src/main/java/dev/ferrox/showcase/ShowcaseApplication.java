package dev.ferrox.showcase;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import dev.ferrox.web.FerroxAutoConfiguration;

@SpringBootApplication
@Import(FerroxAutoConfiguration.class)
public class ShowcaseApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShowcaseApplication.class, args);
    }
}
