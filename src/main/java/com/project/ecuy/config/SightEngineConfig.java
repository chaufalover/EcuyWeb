package com.project.ecuy.config;

import com.project.ecuy.EcuyApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class SightEngineConfig {
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    
    
    public String getUrl() {
        
        return "https://api.sightengine.com/1.0/check.json";
    }
    
    public String getKey() {
        return EcuyApplication.SIGHTENGINE_API_KEY;
    }
    
    public String getSecret() {
        return EcuyApplication.SIGHTENGINE_API_SECRET;
    }
}
