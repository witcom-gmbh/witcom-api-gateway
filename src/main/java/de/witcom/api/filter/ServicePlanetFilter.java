package de.witcom.api.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.AbstractNameValueGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.cloud.gateway.support.HasRouteId;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;


@Component
public class ServicePlanetFilter extends AbstractGatewayFilterFactory<ServicePlanetFilter.Config>{
	
	@Autowired
	SplSessionManager sessionManager;
	
	Logger logger = LoggerFactory.getLogger(ServicePlanetFilter.class);

	public ServicePlanetFilter() {
		super(Config.class);
	}  

	@Override
	public GatewayFilter apply(Config config) {
		
		return (exchange, chain) -> {
            //ServerHttpRequest request = exchange.getRequest();
            //Hier koennte man eienn mechanismus einbauen, der auf basis eines headers einen refresh forciert
			
			String sessionId = null;
			if (config.getTenant()!=null){
				sessionId = sessionManager.getSessionId(config.getTenant());
			} else {
				//default tenant
				sessionId = sessionManager.getSessionId();
			}
            if (sessionId!=null) {
	            ServerHttpRequest request = exchange.getRequest().mutate()
						.header("Cookie", "JSESSIONID=" + sessionId)
						.build();
	            return chain.filter(exchange.mutate().request(request).build());
            } 
            logger.warn("Got no SPL-Session-ID - API-Call will fail");
            return chain.filter(exchange);
        };
		
	
	}

    public static class Config implements HasRouteId {
		
		@Getter
		@Setter
		private String tenant;
		private String routeId;
		
		@Override
		public void setRouteId(String routeId) {
			this.routeId = routeId;
			
		}
		@Override
		public String getRouteId() {
			return routeId;
		}
		
	}


}
