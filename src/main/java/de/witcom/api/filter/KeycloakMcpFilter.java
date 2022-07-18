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
import de.witcom.api.mcp.McpSessionManager;
import reactor.core.publisher.Mono;

import org.springframework.http.HttpCookie;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class KeycloakMcpFilter extends AbstractGatewayFilterFactory<KeycloakMcpFilter.Config>{

	private String defaultReadRole="read";
	private String defaultWorkRole="work";
	private String defaultDeleteRole="delete";
	
	private static final String WWW_AUTH_HEADER = "WWW-Authenticate";
	
	Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	McpSessionManager sessionManager;
	
	@Autowired
	ApplicationProperties appProperties;
	
	@Autowired
	KeyCloakTokenService tokenService;
	
	public KeycloakMcpFilter() {
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

				if (!this.hasAccess(exchange.getRequest(),accessToken,config)) {
            		logger.warn("No {} access to resource {}",exchange.getRequest().getMethod().name(),exchange.getRequest().getPath());
                	return this.onError(exchange,"No access to the requested resource");
                }
			} catch (KeyCloakFilterException ex) {

                logger.error(ex.toString());
                return this.onError(exchange, ex.getMessage());
            }

            

            String sessionId = sessionManager.getSessionId();
			if (sessionId != null){


            		ServerHttpRequest request = exchange.getRequest().mutate()
                        //remove bearer-auth header
                        .headers(httpHeaders -> httpHeaders.remove("Authorization"))
                        //add api-key to auth-header
						.header("Authorization", "Bearer " + sessionId)
						.build();

            		//logger.info(request.getHeaders().toString());
					//return chain.filter(exchange.mutate().request(request).build());                        
					return chain.filter(exchange.mutate().request(request).build()).then(Mono.fromRunnable(() -> {
						ServerHttpResponse response = exchange.getResponse();
						//If unauthorized -> refresh session so that the next call will be ok
						if (response.getStatusCode().equals(HttpStatus.UNAUTHORIZED)) {
							sessionManager.refreshSession();
						}
					}));

			}
			logger.warn("Got no MCP-Session - API-Call will fail");
            return chain.filter(exchange);
            


            

        };
    }

	private boolean hasAccess(ServerHttpRequest serverHttpRequest,AccessToken accessToken,Config config) {
		
		String audience=config.getResourceId();
		String readRole=this.defaultReadRole;
		String workRole=this.defaultWorkRole;
		String deleteRole=this.defaultDeleteRole;
		if (!StringUtils.isEmpty(config.getRoleRead())) {
			readRole=config.getRoleRead();
		}
		if (!StringUtils.isEmpty(config.getRoleWork())) {
			workRole=config.getRoleWork();
		}
		if (!StringUtils.isEmpty(config.getRoleDelete())) {
			deleteRole=config.getRoleDelete();
		}

		//very simple matching
		//GET Method requires READ role
		//DELETE requires DELETE role
		//PATCH,POST,PUT requires WORK role
		switch (serverHttpRequest.getMethod()){
			case DELETE:
				return this.hasRole(accessToken, audience, deleteRole);
			case GET:
			case HEAD:
				return this.hasRole(accessToken, audience, readRole);
			case PATCH:
			case POST:
			case PUT:
				return this.hasRole(accessToken, audience, workRole);

		}

		//all other methos are currently unimplemented
		return false;

	}	

	private boolean hasRole(AccessToken accessToken,String resource,String role) {
		
		//logger.debug ("Looking for role {} in resource {}",role,resource);
		Map<String,AccessToken.Access> resAccess = accessToken.getResourceAccess();
		if (resAccess.containsKey(resource)){
			Set<String> roles = resAccess.get(resource).getRoles();
			//logger.debug("Found resource-roles in token {}",roles.toString());
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
