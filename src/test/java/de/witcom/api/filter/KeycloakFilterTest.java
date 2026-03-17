package de.witcom.api.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractNameValueGatewayFilterFactory.NameValueConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.witcom.api.config.properties.ApplicationProperties;
import de.witcom.api.filter.KeyCloakFilter;
import de.witcom.api.filter.KeyCloakTokenService;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Log4j2
public class KeycloakFilterTest {

	private ServerWebExchange exchange;

	private GatewayFilterChain filterChain;

	private ArgumentCaptor<ServerWebExchange> captor;    

    @Autowired
    private ApplicationContext context;

    @Autowired
    private ApplicationProperties appProperties;

    @Autowired
    KeyCloakTokenService tokenService;

    @LocalServerPort
    private int port;

    @Value("${API_GW_OAUTH_TESTCLIENT_CLIENTSECRET}")
    String testClientClientSecret;
    @Value("${API_GW_OAUTH_TESTCLIENT_CLIENTID}")
    String testClientClientId;
    
    WebTestClient testclient;

    @BeforeEach
    void setupWebClient() {
        // testclient = WebTestClient.bindToServer().baseUrl("http://localhost:"+port).build();
        filterChain = mock(GatewayFilterChain.class);
        captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        when(filterChain.filter(captor.capture())).thenReturn(Mono.empty());
    }

    private TokenResponse getAccessToken(){

        assertNotNull(testClientClientId);
        assertNotNull(testClientClientSecret);

        // get an access token
        String kcBaseUrl = String.format("%s/realms/%s", appProperties.getKeycloakConfig().getKeycloakServerUrl(),appProperties.getKeycloakConfig().getKeycloakRealmId());
        WebClient kcClient = WebClient.builder()
            .baseUrl(kcBaseUrl)
            .defaultHeaders(header -> header.setBasicAuth(testClientClientId, testClientClientSecret))
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .build();
        TokenResponse token = kcClient.post()
            .uri("/protocol/openid-connect/token")
            .body(BodyInserters.fromFormData("grant_type", "client_credentials"))
            .retrieve()
            .bodyToMono(TokenResponse.class)
            .block()
            ;
        return token;

    }

    @Test
    void executeUnauthenticatedRequestAndExpectitToFail(){
        MockServerHttpRequest request = MockServerHttpRequest
            .get("http://localhost")
            .build();

        exchange = MockServerWebExchange.from(request);

        GatewayFilter filter = new KeyCloakFilter(appProperties, tokenService)
                        .apply(new NameValueConfig().setName("requiredRole")
                                        .setValue("rmdb-resource-server:read"));
        StepVerifier.create(filter.filter(exchange, filterChain)).verifyComplete();
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verifyNoInteractions(filterChain);

    }

    @Test
    void executeAuthenticatedRequestAndExpectItToSuceed() {

        TokenResponse token = getAccessToken();
        MockServerHttpRequest request = MockServerHttpRequest
            .get("http://localhost")
            .header("Authorization", String.format("Bearer %s", token.getAccessToken()))
            .build();

        exchange = MockServerWebExchange.from(request);
        GatewayFilter filter = new KeyCloakFilter(appProperties, tokenService)
                        .apply(new NameValueConfig().setName("requiredRole")
                                        .setValue("rmdb-resource-server:read"));
        StepVerifier.create(filter.filter(exchange, filterChain)).expectSubscription()
                        .verifyComplete();
        verify(filterChain).filter(any());

    }

    @Test
    void executeAuthenticatedRequestWithNonStandardHeaderAndExpectItToSucceed(){

        TokenResponse token = getAccessToken();
        MockServerHttpRequest request = MockServerHttpRequest
            .get("http://localhost")
            .header("Authorization", String.format("bEaRer %s", token.getAccessToken()))
            .build();

        exchange = MockServerWebExchange.from(request);
        GatewayFilter filter = new KeyCloakFilter(appProperties, tokenService)
                        .apply(new NameValueConfig().setName("requiredRole")
                                        .setValue("rmdb-resource-server:read"));
        StepVerifier.create(filter.filter(exchange, filterChain)).expectSubscription()
                        .verifyComplete();
        verify(filterChain).filter(any());

    }

    @Test
    void executeAuthenticatedRequestWithMissingRoleAndExpectItToFail(){

        TokenResponse token = getAccessToken();

        MockServerHttpRequest request = MockServerHttpRequest
            .get("http://localhost")
            .header("Authorization", String.format("Bearer %s", token.getAccessToken()))
            .build();

        exchange = MockServerWebExchange.from(request);
        GatewayFilter filter = new KeyCloakFilter(appProperties, tokenService)
                        .apply(new NameValueConfig().setName("requiredRole")
                                        .setValue("i-do-not-exist"));
        StepVerifier.create(filter.filter(exchange, filterChain)).expectSubscription()
                        .verifyComplete();
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verifyNoInteractions(filterChain);


    }

    @Test
    void executeMisconfiguredAuthenticatedRequestAndExpectItToFail(){

        MockServerHttpRequest request = MockServerHttpRequest
            .get("http://localhost")
            .header("Authorization", "Bearer paul")
            .build();

        exchange = MockServerWebExchange.from(request);
        GatewayFilter filter = new KeyCloakFilter(appProperties, tokenService)
                        .apply(new NameValueConfig().setName("requiredRole")
                                        .setValue("i-do-not-exist"));
        StepVerifier.create(filter.filter(exchange, filterChain)).expectSubscription()
                        .verifyComplete();
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verifyNoInteractions(filterChain);

    }

    @Data
    protected static class TokenResponse {

        @JsonProperty("access_token")
        String accessToken;
        @JsonProperty("refresh_token")
        String refreshToken;
        @JsonProperty("expires_in")
        int expiresIn;
        @JsonProperty("token_type")
        String tokenType;
    }

}
