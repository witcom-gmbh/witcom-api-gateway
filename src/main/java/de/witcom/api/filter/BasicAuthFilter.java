package de.witcom.api.filter;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractNameValueGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Component
public class BasicAuthFilter extends AbstractNameValueGatewayFilterFactory {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public GatewayFilter apply(NameValueConfig config) {
		// TODO Auto-generated method stub
		return (exchange, chain) -> {
			String auth = config.getName()+ ":" +config.getValue();
			String authHeader = "Basic " + Base64.getEncoder().encodeToString(auth.getBytes());

			
			
		ServerHttpRequest request = exchange.getRequest().mutate()
				.header(HttpHeaders.AUTHORIZATION, authHeader)
				.build();
        return chain.filter(exchange.mutate().request(request).build());
		};

	}

}
