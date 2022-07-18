package de.witcom.api.filter;

import org.keycloak.representations.AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.AbstractNameValueGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.cloud.gateway.support.HasRouteId;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.stereotype.Component;
import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.util.StringUtils;

import de.witcom.api.command.client.CommandSessionManager;
import de.witcom.api.config.ApplicationProperties;
import reactor.core.publisher.Mono;

import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class KeycloakCommandFilter extends AbstractGatewayFilterFactory<KeycloakCommandFilter.Config>{
	
	@Autowired
	private CommandSessionManager sessionManager;
	
	private String defaultReadRole="read";
	private String defaultWorkRole="work";
	
	private static final String WWW_AUTH_HEADER = "WWW-Authenticate";
	
	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	ApplicationProperties appProperties;
	
	@Autowired
	KeyCloakTokenService tokenService;
	
	public KeycloakCommandFilter() {
		super(Config.class);
	}

	@Override
	public GatewayFilter apply(Config config) {
		
		return (exchange, chain) -> {
			if (config==null) {
				logger.error("No keycloak-resource-id configured. Authorization not possible");
                return this.onError(exchange, "No keycloak-resource-id configured. Authorization not possible");
			}
			if (StringUtils.isEmpty(config.getResourceId())) {
				logger.error("No keycloak-resource-id configured. Authorization not possible");
                return this.onError(exchange, "No keycloak-resource-id configured. Authorization not possible");
			}
			
			//check access
			try {
				String token = this.tokenService.extractJWTToken(exchange.getRequest());
				AccessToken accessToken = this.tokenService.extractAccessToken(token);
				
				if (!this.hasAccess(exchange.getRequest().getURI(),accessToken,config)) {
            		logger.warn("No {} access to resource {}",exchange.getRequest().getMethod().name(),exchange.getRequest().getPath());
                	return this.onError(exchange,"No access to the requested resource");
                }
			} catch (KeyCloakFilterException ex) {

                logger.error(ex.toString());
                return this.onError(exchange, ex.getMessage());
            }
			
			String sessionId = sessionManager.getSessionId();
            if (sessionId!=null) {
            	
            	URI uri = exchange.getRequest().getURI();
				StringBuilder query = new StringBuilder();
				String originalQuery = uri.getRawQuery();

				if (StringUtils.hasText(originalQuery)) {
					query.append(originalQuery);
					if (originalQuery.charAt(originalQuery.length() - 1) != '&') {
						query.append('&');
					}
				}
				query.append("sessionId");
				query.append("=");
				query.append(sessionId);

				try {
					URI newUri = UriComponentsBuilder.fromUri(uri).replaceQuery(query.toString()).build(true).toUri();
					logger.debug("URI is now " + newUri.toString());
					ServerHttpRequest request = exchange.getRequest().mutate().uri(newUri).build();
					return chain.filter(exchange.mutate().request(request).build()).then(Mono.fromRunnable(() -> {
						ServerHttpResponse response = exchange.getResponse();
						//If unauthorized -> refresh session so that the next call will be ok
						if (response.getStatusCode().equals(HttpStatus.UNAUTHORIZED)) {
							sessionManager.refreshSession();
						}
					}));
				}
				catch (RuntimeException ex) {
					throw new IllegalStateException("Invalid URI query: \"" + query.toString() + "\"");
				}
            } 
            logger.warn("Got no Command-Session-ID - API-Call will fail");
            return chain.filter(exchange);
        };
	}
	
	private boolean hasAccess(URI uri,AccessToken accessToken,Config config) {
		
		String audience=config.getResourceId();
		String readRole=this.defaultReadRole;
		String workRole=this.defaultWorkRole;
		if (!StringUtils.isEmpty(config.getRoleRead())) {
			readRole=config.getRoleRead();
		}
		if (!StringUtils.isEmpty(config.getRoleWork())) {
			workRole=config.getRoleWork();
		}
		
		String originalPath = uri.getRawPath();
		logger.debug(originalPath);
		String pattern = "";
		//very basic pattern matching
		//basic query operations for all entities
		pattern = "/\\w*/api/rest/entity/(\\w*)/(query.*)";
		boolean queryBasic = Pattern.matches(pattern, originalPath);
		//logger.debug("queryBasic {}",queryBasic);
		//relations - starting with uppercase-letter 
		pattern = "/\\w*/api/rest/entity/(\\w*)/(\\w*)/([A-Z]\\w*)";
		boolean queryRelation = Pattern.matches(pattern, originalPath);
		//logger.debug("queryRelation {}",queryRelation);
		
		if (queryBasic || queryRelation) {
			//logger.debug("Check read-access for {}",originalPath);
			return this.hasRole(accessToken, audience, readRole);
		}
		//all other request require work role.... 
		//logger.debug("Check work-access for {}",originalPath);
		return this.hasRole(accessToken, audience, workRole);

	}
	
	private boolean hasRole(AccessToken accessToken,String resource,String role) {
		
		//logger.debug ("Looking for role {} in resource {}",role,resource);
		Map<String,AccessToken.Access> resAccess = accessToken.getResourceAccess();
		if (resAccess.containsKey(resource)){
			Set<String> roles = resAccess.get(resource).getRoles();
			logger.debug("Found resource-roles in token {}",roles.toString());
			if (roles.contains(role)) {
				return true;
			}
			logger.warn("Required role {} not found in token",role);
			return false;
		} 
		logger.warn("No Resource-Access defined for resource {}",resource);
		
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
	
	public static class Config implements HasRouteId {
		
		private String resourceId;
		private String roleRead;
		private String roleWork;
		private String roleDelete;
		private String routeId;
		
		public String getRoleRead() {
			return roleRead;
		}
		public Config setRoleRead(String roleRead) {
			this.roleRead = roleRead;
			return this;
		}
		public String getRoleWork() {
			return roleWork;
		}
		public Config setRoleWork(String roleWork) {
			this.roleWork = roleWork;
			return this;
		}
		public String getRoleDelete() {
			return roleDelete;
		}
		public Config setRoleDelete(String roleDelete) {
			this.roleDelete = roleDelete;
			return this;
		}
		@Override
		public void setRouteId(String routeId) {
			this.routeId = routeId;
			
		}
		@Override
		public String getRouteId() {
			return routeId;
		}
		public String getResourceId() {
			return resourceId;
		}
		public Config setResourceId(String resourceId) {
			this.resourceId = resourceId;
			return this;
		}
		
		
		
	}

	

}
