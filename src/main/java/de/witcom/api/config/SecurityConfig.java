package de.witcom.api.config;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.witcom.api.filter.KeyCloakFilterException;

@Configuration
public class SecurityConfig {
	
	Logger logger = LoggerFactory.getLogger(this.getClass());
	@Value("${KEYCLOAK_SERVER_URL:#{null}}")
	String keycloakServerUrl;
	
	@Value("${KEYCLOAK_REALM_ID:#{null}}")
	String keycloakRealmId;
	

	@Bean
	public KeycloakProperties initKeyCloak() throws KeyCloakFilterException{
		
		logger.debug("Init keycloak....");
		if (StringUtils.isEmpty(keycloakServerUrl)){
			logger.warn("No keycloakServerUrl configured");
		}
		if (StringUtils.isEmpty(keycloakRealmId)){
			logger.warn("No keycloakRealmId configured");
		}
		return new KeycloakProperties().keycloakRealmId(keycloakRealmId).keycloakServerUrl(keycloakServerUrl);
	}

}
