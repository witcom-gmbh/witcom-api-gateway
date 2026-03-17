package de.witcom.api.filter;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import de.witcom.api.command.client.CommandSessionManager;
import de.witcom.api.config.properties.ApplicationProperties;
import de.witcom.api.serviceplanet.SplSessionManager;
import lombok.Data;

@SpringBootTest(
        webEnvironment = RANDOM_PORT,
        properties = {
            "application.spl-config.tenants[0].spl-base-url=http://localhost:${wiremock.server.port}",
            "application.spl-config.tenants[0].tenant-name=TEST01",
            "application.spl-config.tenants[0].required-resource-role=rmdb-resource-server:read",
            "application.spl-config.tenants[1].spl-base-url=http://localhost:${wiremock.server.port}",
            "application.spl-config.tenants[1].tenant-name=TEST02",
            "application.spl-config.tenants[1].required-resource-role=rmdb-resource-server:read",
            "application.command-config.base-url=http://localhost:${wiremock.server.port}",
        }
        )
@AutoConfigureWebTestClient
@DirtiesContext
@AutoConfigureWireMock(port = 0)
class GatewayConfigurationTest {
    @Autowired
    WebTestClient webTestClient;

    @Autowired
    WireMockServer wireMockServer;

    @MockitoBean
    SplSessionManager splSessionManager;

    @MockitoBean
    CommandSessionManager commandSessionManager;

    @Autowired
    private ApplicationProperties appProperties;    

    @Value("${API_GW_OAUTH_TESTCLIENT_CLIENTSECRET}")
    String testClientClientSecret;
    @Value("${API_GW_OAUTH_TESTCLIENT_CLIENTID}")
    String testClientClientId;

    private static final String SPL_STUB_URL = "/serviceplanet/remote/service/dummy";
    private static final String SPL_REQUEST_URL = "/test02/dummy";
    private static final String SPL_AUTH_COOKIE = "JSESSIONID";    

    private static final String CMD_STUB_URL = "/axis/api/rest/entity/floor/dummy";
    private static final String CMD_REQUEST_URL = "/rmdb/api/rest/entity/floor/dummy";


    @AfterEach
    void afterEach() {
        wireMockServer.resetAll();
    }

    private TokenResponse getAccessToken(){

        assertNotNull(testClientClientId);
        assertNotNull(testClientClientSecret);

        // get an access token
        String kcBaseUrl = String.format("%s/realms/%s",
                        appProperties.getKeycloakConfig().getKeycloakServerUrl(),
                        appProperties.getKeycloakConfig().getKeycloakRealmId());
        WebClient kcClient = WebClient.builder()
                        .baseUrl(kcBaseUrl)
                        .defaultHeaders(header -> header.setBasicAuth(testClientClientId,
                                        testClientClientSecret))
                        .defaultHeader(HttpHeaders.CONTENT_TYPE,
                                        MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .build();
        return kcClient.post()
            .uri("/protocol/openid-connect/token")
            .body(BodyInserters.fromFormData("grant_type", "client_credentials"))
            .retrieve()
            .bodyToMono(TokenResponse.class)
            .block()
            ;
    }
    
    @Test
    void shouldAddSessionParameterForCommand() {

        when(commandSessionManager.getSessionId()).thenReturn("my-session-id");
        TokenResponse token = getAccessToken();
        wireMockServer.stubFor(
                        WireMock.get(urlPathEqualTo(CMD_STUB_URL)).willReturn(WireMock.ok()));
        webTestClient.get()
            .uri(CMD_REQUEST_URL)
            .headers(header -> header.setBearerAuth(token.getAccessToken()))
            .exchange()
            .expectStatus()
                        .isEqualTo(HttpStatusCode.valueOf(200));

        verify(commandSessionManager).getSessionId();

        wireMockServer.verify(
            getRequestedFor(urlPathEqualTo(CMD_STUB_URL))
            .withQueryParam("sessionId", equalTo("my-session-id"))
        );

    }

    @Test
    void shouldAddSessionCookieForServiceplanet() {

        when(splSessionManager.getSessionId(anyString())).thenReturn("my-session-id");
        TokenResponse token = getAccessToken();

        wireMockServer.stubFor(WireMock.get(SPL_STUB_URL).willReturn(WireMock.ok()));
        webTestClient.get()
            .uri(SPL_REQUEST_URL)
            .headers(header -> header.setBearerAuth(token.getAccessToken()))
            .exchange()
            .expectStatus()
                        .isEqualTo(HttpStatusCode.valueOf(200));

        verify(splSessionManager).getSessionId("TEST02");

        wireMockServer.verify(
            getRequestedFor(urlEqualTo(SPL_STUB_URL))
            .withCookie(SPL_AUTH_COOKIE, equalTo("my-session-id"))
        );

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
