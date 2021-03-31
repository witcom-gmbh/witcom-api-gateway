package de.witcom.api.filter;

import java.net.URL;
import java.security.PublicKey;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.keycloak.TokenVerifier;
import org.keycloak.common.VerificationException;
import org.keycloak.jose.jwk.JSONWebKeySet;
import org.keycloak.jose.jwk.JWK;
import org.keycloak.jose.jwk.JWKParser;
import org.keycloak.jose.jws.JWSHeader;
import org.keycloak.representations.AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.witcom.api.config.ApplicationProperties;

@Service
public class KeyCloakTokenService {
	
	@Autowired
	ApplicationProperties appProperties;
	Logger logger = LoggerFactory.getLogger(this.getClass());

	public AccessToken extractAccessToken(String tokenString) throws KeyCloakFilterException {
		
		if (tokenString == null) {
			logger.error("ERROR: Access-token is null");
			throw new KeyCloakFilterException("Access-token is null");
		}
		
		try {
			@SuppressWarnings("unchecked")
			TokenVerifier<AccessToken> verfifier = TokenVerifier
					.create(tokenString, AccessToken.class)
					.withChecks(org.keycloak.TokenVerifier.SUBJECT_EXISTS_CHECK,org.keycloak.TokenVerifier.IS_ACTIVE);
			
			return verfifier
					.publicKey(this.retrievePublicKeyFromCertsEndpoint(verfifier.getHeader()))
					.verify()
					.getToken();
			
		} catch (VerificationException e) {
			logger.debug(e.getMessage());
			throw new KeyCloakFilterException("Unable to verify token " +e.getMessage());
		}
	}
	
	public String extractJWTToken(ServerHttpRequest request) throws KeyCloakFilterException
    {
        if (!request.getHeaders().containsKey("Authorization")) {
            throw new KeyCloakFilterException("Authorization header is missing");
        }

        List<String> headers = request.getHeaders().get("Authorization");
        if (headers.isEmpty()) {
            throw new KeyCloakFilterException("Authorization header is empty");
        }

        String credential = headers.get(0).trim();
        String[] components = credential.split("\\s");

        if (components.length != 2) {
            throw new KeyCloakFilterException("Malformed Authorization content");
        }

        if (!components[0].equals("Bearer")) {
            throw new KeyCloakFilterException("Bearer is needed");
        }

        return components[1].trim();
    }
	
	private PublicKey retrievePublicKeyFromCertsEndpoint(JWSHeader jwsHeader) {
		
		try {
	        ObjectMapper om = new ObjectMapper();
	        
	        JSONWebKeySet certInfos = om.readValue(new URL(getRealmCertsUrl()).openStream(),JSONWebKeySet.class);
	        
	        JWK keyInfo = null;
			for ( JWK key:certInfos.getKeys()){
    	        String kid=key.getKeyId();
    	        if (jwsHeader.getKeyId().equals(kid)) {
    			  keyInfo  = key;
    			  break;
    			}
			}
			if (keyInfo == null) {
				logger.error("Unable to get public key from certendpoint "+getRealmCertsUrl());
				return null;
			}
			
			return JWKParser.create(keyInfo).toPublicKey();
	        
		} catch (Exception e) {
			logger.error("Unable to get public key from certendpoint "+getRealmCertsUrl()+ ":" + e.getMessage());
		}
		return null;
	}
	
	private String getRealmUrl() {
	    if (StringUtils.isEmpty(appProperties.getKeycloakConfig().getKeycloakServerUrl())){
			logger.error("No keycloakServerUrl configured - filter won't work");
			return "";
		}
		if (StringUtils.isEmpty(appProperties.getKeycloakConfig().getKeycloakRealmId())){
			logger.error("No keycloakRelam configured - filter won't work");
			return "";
		}
		return appProperties.getKeycloakConfig().getKeycloakServerUrl().trim() + "/realms/" + appProperties.getKeycloakConfig().getKeycloakRealmId().trim();
	}

	private String getRealmCertsUrl() {
		return getRealmUrl() + "/protocol/openid-connect/certs";
	}
}
