package com.glist.GroceriesList.configuration;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "list-pal")
public class ListPalConfig {
    private String env;
    private String clientEngine;
    private String serverDomain;
    private String secret;
}
