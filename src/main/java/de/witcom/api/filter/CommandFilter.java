package de.witcom.api.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.AbstractNameValueGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.stereotype.Component;
import java.net.URI;
import org.springframework.util.StringUtils;

import de.witcom.api.command.client.CommandSessionManager;
import reactor.core.publisher.Mono;

import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class CommandFilter extends AbstractGatewayFilterFactory{
	
	@Autowired
	private CommandSessionManager sessionManager;
	
	Logger logger = LoggerFactory.getLogger(CommandFilter.class);

	@Override
	public GatewayFilter apply(Object config) {
		
		return (exchange, chain) -> {
			
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
					//logger.debug("URI is now " + newUri.toString());
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

	

}
