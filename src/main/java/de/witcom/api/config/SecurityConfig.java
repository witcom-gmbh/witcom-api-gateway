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

	@Bean
	public KeycloakProperties initKeyCloak(@Value("${KEYCLOAK_SERVER_URL}") String keycloakServerUrl, @Value("${KEYCLOAK_REALM_ID}") String keycloakRealmId) throws KeyCloakFilterException{
		
		logger.debug("Init keycloak....");
		if (StringUtils.isEmpty(keycloakServerUrl)){
			logger.error("No keycloakServerUrl configured");
			throw new KeyCloakFilterException("No keycloakServerUrl configured");
		}
		if (StringUtils.isEmpty(keycloakRealmId)){
			logger.error("No keycloakRealmId configured");
			throw new KeyCloakFilterException("No keycloakRealmId configured");
		}
		return new KeycloakProperties().keycloakRealmId(keycloakRealmId).keycloakServerUrl(keycloakServerUrl);
	}

}
