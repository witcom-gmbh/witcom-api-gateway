package de.witcom.api.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;

import de.witcom.api.serviceplanet.SplSessionManager;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Log4j2
class ServicePlanetFilterTest {

    private ServerWebExchange exchange;
    private GatewayFilterChain filterChain;
    private ArgumentCaptor<ServerWebExchange> captor;


    @BeforeEach
    void setupWebClient() {
        filterChain = mock(GatewayFilterChain.class);
        captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        when(filterChain.filter(captor.capture())).thenReturn(Mono.empty());
    }

    @Test
    void requestShouldAddSessionCookie() {
        MockServerHttpRequest request = MockServerHttpRequest
            .get("http://localhost")
            .build();

        exchange = MockServerWebExchange.from(request);
        SplSessionManager sessionManager = mock(SplSessionManager.class);
        when(sessionManager.getSessionId(anyString())).thenReturn("a-session");
        ServicePlanetFilter.Config config = new ServicePlanetFilter.Config();
        config.setTenant("test01");
        GatewayFilter filter = new ServicePlanetFilter(sessionManager).apply(config);
        StepVerifier.create(filter.filter(exchange, filterChain)).expectSubscription()
                        .verifyComplete();

        verify(filterChain).filter(any());
        assertTrue(captor.getValue().getRequest().getHeaders().containsKey("Cookie"));
        List<String> values = captor.getValue().getRequest().getHeaders().get("Cookie");
        assertFalse(values.isEmpty());
        assertEquals("JSESSIONID=a-session", values.getFirst());
                                
    }


    @Test
    void requestShouldNotAddSessionCookieWithoutSession() {
        MockServerHttpRequest request = MockServerHttpRequest
            .get("http://localhost")
            .build();

        exchange = MockServerWebExchange.from(request);
        SplSessionManager sessionManager = mock(SplSessionManager.class);
        when(sessionManager.getSessionId(anyString())).thenReturn(null);
        ServicePlanetFilter.Config config = new ServicePlanetFilter.Config();
        config.setTenant("test01");
        GatewayFilter filter = new ServicePlanetFilter(sessionManager).apply(config);
        StepVerifier.create(filter.filter(exchange, filterChain)).expectSubscription()
                        .verifyComplete();

        verify(filterChain).filter(any());
        assertFalse(captor.getValue().getRequest().getHeaders().containsKey("Cookie"));
                                
    }    
}
