package de.witcom.api.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractNameValueGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URL;
import java.security.PublicKey;
import java.util.List;
import java.util.Arrays;
import java.util.stream.Stream;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.keycloak.common.VerificationException;
import org.keycloak.jose.jws.JWSHeader;
import org.keycloak.representations.AccessToken;


import org.keycloak.TokenVerifier;
import org.keycloak.jose.jwk.JSONWebKeySet;
import org.keycloak.jose.jwk.JWK;
import org.keycloak.jose.jwk.JWKParser;
import de.witcom.api.config.ApplicationProperties;
import reactor.core.publisher.Mono;

@Component
public class KeyCloakFilter extends AbstractNameValueGatewayFilterFactory {
	
	private static final String WWW_AUTH_HEADER = "WWW-Authenticate";
	private static final String X_JWT_SUB_HEADER = "X-jwt-client";
	
	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	ApplicationProperties appProperties;
	
	@Autowired
	KeyCloakTokenService tokenService;	

	@Override
	public GatewayFilter apply(NameValueConfig config) {
		return (exchange, chain) -> {

            try {
                String token = this.tokenService.extractJWTToken(exchange.getRequest());
                AccessToken accessToken = this.tokenService.extractAccessToken(token);

                if (!StringUtils.isEmpty(config.getValue()) && config.getName().equals("requiredRole") ) {
                	if (!this.hasAccess(accessToken,config.getValue())) {
                		logger.warn("No {} access to resource {}",exchange.getRequest().getMethod().name(),exchange.getRequest().getPath());
                    	return this.onError(exchange,"No access to the requested resource");
                    }
                }

                ServerHttpRequest request = exchange.getRequest().mutate().
                        header(X_JWT_SUB_HEADER, accessToken.getIssuedFor()).
                        build();
                return chain.filter(exchange.mutate().request(request).build());

            } catch (KeyCloakFilterException ex) {

                logger.error(ex.toString());
                return this.onError(exchange, ex.getMessage());
            }
		};
	}
	
	private boolean hasAccess(AccessToken accessToken,String requiredRole) {
		String clientId = accessToken.getIssuedFor();
		//get resource-access
		//split required role in audience and role
		String audience=clientId;
		String role=null;
		List<String> roleElements = Stream.of(requiredRole)
            .map(w -> w.split(":")).flatMap(Arrays::stream)
            .collect(Collectors.toList());
		
		if (roleElements.size()==1){
		    role = roleElements.get(0);
		} else if (roleElements.size()==2){
		    audience = roleElements.get(0);
		    role = roleElements.get(1);
	    } else {
	        logger.warn("Malformed role {} -  either provide role or audience:role",requiredRole);
	        return false;
	    }
	    logger.debug ("Looking for role {} in audience {}",role,audience);

		Map<String,AccessToken.Access> resAccess = accessToken.getResourceAccess();
		if (resAccess.containsKey(audience)){
			Set<String> roles = resAccess.get(audience).getRoles();
			logger.debug("Found resource-roles in token {}",roles.toString());
			if (roles.contains(role)) {
				return true;
			}
			logger.warn("Required role {} not found in token",requiredRole);
			return false;
		} 
		logger.info("No Resource-Access defined for audience {}",audience);
		return false;
	}

	private Mono<Void> onError(ServerWebExchange exchange, String err)
    {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add(WWW_AUTH_HEADER, this.formatErrorMsg(err));

        return response.setComplete();
    }
	
	private String formatErrorMsg(String msg)
    {
        return String.format("Bearer realm=\""+appProperties.getKeycloakConfig().getKeycloakRealmId().trim()+"\", " +
                "error=\"https://tools.ietf.org/html/rfc7519\", " +
                "error_description=\"%s\" ",  msg);
    }	
	
	

}
