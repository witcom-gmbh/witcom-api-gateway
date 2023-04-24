package de.witcom.api.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.witcom.api.config.properties.ApplicationProperties;
import de.witcom.api.mcp.tron.ApiClient;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Configuration
public class McpApiConfiguration {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
	ApplicationProperties appProperties;


    @Bean
    public ApiClient apiClient(){

        String pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS+00:00";
        
        logger.info("MCP Base-URL set to " + appProperties.getMcpConfig().getBaseUrl());
        ApiClient apiClient = new ApiClient();
        //apiClient.getFeignBuilder().logLevel(feign.Logger.Level.FULL);
        apiClient.setBasePath(appProperties.getMcpConfig().getBaseUrl() + "/tron");


        return apiClient;
    }

    
}
