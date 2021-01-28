package de.witcom.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAutoConfiguration
@EnableConfigurationProperties
public class WitcomApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(WitcomApiGatewayApplication.class, args);
	}

}
