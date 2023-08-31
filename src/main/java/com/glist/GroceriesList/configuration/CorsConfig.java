package com.glist.GroceriesList.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CorsConfig implements WebMvcConfigurer {
    private final ListPalConfig listPalConfig;
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(listPalConfig.getClientEngine1(), listPalConfig.getClientEngine2(), listPalConfig.getClientEngine())
                .allowedMethods("GET", "POST", "DELETE")
                .allowCredentials(true)
                .maxAge(3600)
        ;
    }
}