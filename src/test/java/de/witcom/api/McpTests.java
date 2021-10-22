package de.witcom.api;

import org.springframework.boot.test.context.SpringBootTest;

import de.witcom.api.config.ApplicationProperties;
import de.witcom.api.mcp.client.Oauth2Api;
import de.witcom.api.mcp.tron.ApiClient;

import de.witcom.api.mcp.tron.model.OAuth2TokenSerializer;

import java.text.SimpleDateFormat;
import java.util.Locale;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

//@SpringBootTest
public class McpTests {

    //@Autowired
    ApiClient mcpAuthClient;

	//@Autowired
	ApplicationProperties appProperties;    

    @Test
    @Ignore
    void simpleTest(){

        Oauth2Api tokensApi = mcpAuthClient.buildClient(Oauth2Api.class);
        OAuth2TokenSerializer res = tokensApi.oauth2TokensCreate(appProperties.getMcpConfig().getUser(), appProperties.getMcpConfig().getPassword(), "password", null, null, null, null, null, null, null);


    }

    @Test
    void dateFormatter(){

        String dateString = "2021-10-22 09:07:42.764821+00:00";
        String pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS+00:00";
        //new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss.SSSSSS+00:00", Locale.getDefault());
        DateTimeFormatter dtf = DateTimeFormat.forPattern(pattern);
        //SimpleDateFormat dtf = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss.SSSSSS+00:00", Locale.getDefault());
        DateTime dateTime = dtf.parseDateTime(dateString);
        System.out.println(dateTime); 


    }
    
}
