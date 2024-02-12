package de.witcom.api;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import de.witcom.api.KeycloakFilterTest.TokenResponse;
import de.witcom.api.config.properties.ApplicationProperties;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CommandSessionIdToHeaderFilterTest {

	@LocalServerPort
	private int port;

	@Autowired
	private ApplicationProperties appProperties;

	WebTestClient testclient;

	@Value("${API_GW_OAUTH_TESTCLIENT_CLIENTSECRET}")
	String testClientClientSecret;
	@Value("${API_GW_OAUTH_TESTCLIENT_CLIENTID}")
	String testClientClientId;

	@BeforeEach
	void setupWebClient(){
		testclient = WebTestClient.bindToServer().baseUrl("http://localhost:"+port).build();
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
	void testCommandSession2Header(){
		testclient.post().uri("/soapproxy/somews").exchange().expectStatus().isOk();
	}

	
	@Test
	void testCommandSession2HeaderWithAuth(){
		TokenResponse token = getAccessToken();
		testclient.post()
			.uri("/soapproxy-auth/somews")
			.headers(header -> header.setBearerAuth(token.getAccessToken()))
			.exchange()
			.expectStatus()
			.isOk();
	}


}
