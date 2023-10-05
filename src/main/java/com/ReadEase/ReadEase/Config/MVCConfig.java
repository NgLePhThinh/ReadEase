package com.ReadEase.ReadEase.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class MVCConfig implements WebMvcConfigurer {
    @Value("${application.cross-origin}")
    private  String crossOrigin;
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(crossOrigin)
                .allowedMethods("POST", "GET", "PUT", "DELETE");
//                .allowCredentials(false).maxAge(3600);
    }
}
