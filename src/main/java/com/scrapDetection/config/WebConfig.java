package com.scrapDetection.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                        "http://localhost:3000",           // Next.js
                        "http://127.0.0.1:3000",           // Alternative localhost
                        "http://localhost:8080",           // Swagger UI
                        "http://10.0.2.2:8080",            // Android Emulator
                        "http://localhost:19006",          // Expo Web
                        "exp://192.168.1.105:19000"       // Expo Go
                )
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/")
                .resourceChain(false);

        registry.addResourceHandler("/api-docs/**")
                .addResourceLocations("classpath:/META-INF/resources/");
    }
}