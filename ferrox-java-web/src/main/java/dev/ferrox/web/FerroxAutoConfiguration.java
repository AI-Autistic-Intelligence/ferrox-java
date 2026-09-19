package dev.ferrox.web;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration for the Ferrox Web Framework.
 */
@Configuration
@ComponentScan(basePackages = {"dev.ferrox.security", "dev.ferrox.cqrs", "dev.ferrox.data", "dev.ferrox.web"})
public class FerroxAutoConfiguration {
}
