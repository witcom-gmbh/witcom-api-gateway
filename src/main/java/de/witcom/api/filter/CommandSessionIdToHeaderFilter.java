package de.witcom.api.filter;

import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.support.HasRouteId;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import de.witcom.api.command.client.CommandSessionManager;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

@Component
@Log4j2
public class CommandSessionIdToHeaderFilter
		extends AbstractGatewayFilterFactory<CommandSessionIdToHeaderFilter.Config> {

	private final CommandSessionManager sessionManager;

	public CommandSessionIdToHeaderFilter(CommandSessionManager sessionManager){
		super(Config.class);
		this.sessionManager = sessionManager;
	}

	@Override
	public GatewayFilter apply(Config config) {

		return (exchange, chain) -> {
			if (config == null || StringUtils.isBlank(config.getSessionHeader())) {
				log.error("No header-name for command-session-id configured");
				return this.onError(exchange, HttpStatusCode.valueOf(400),
						"No header-name for command-session-id configured");
			}

			String sessionId = sessionManager.getSessionId();

			// if we have a session-id
			if (StringUtils.isNotBlank(sessionId)) {
				// add Session-ID to header
				ServerHttpRequest request = exchange.getRequest().mutate()
					.header(config.getSessionHeader(), sessionId)
					.build();

				// log.debug(request.getHeaders().toString());
				return chain.filter(exchange.mutate().request(request).build()).then(Mono.fromRunnable(() -> {
					ServerHttpResponse response = exchange.getResponse();
					//If unauthorized -> refresh session so that the next call will be ok
					if (response.getStatusCode().equals(HttpStatus.UNAUTHORIZED)) {
						sessionManager.refreshSession();
					}
				}));					

			}

			log.warn("Got no Command-Session-ID - API-Call will fail");
			return chain.filter(exchange);

		};

	}

	private Mono<Void> onError(ServerWebExchange exchange, HttpStatusCode status, String err) {
		ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(status);
		response.getHeaders().add("x-error-message", err);
		return response.setComplete();
	}

	@Data
	public static class Config implements HasRouteId {

		private String routeId;
		private String sessionHeader;

	}

}
