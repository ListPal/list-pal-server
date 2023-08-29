package com.glist.GroceriesList.configuration;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "list-pal")
public class ListPalConfig {
    private String engine;
    private String clientEngine1;
    private String clientEngine2;
    private String clientDomain1;
    private String clientDomain2;
    private String secret;
}
