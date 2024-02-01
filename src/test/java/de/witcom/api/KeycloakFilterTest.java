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

import com.fasterxml.jackson.annotation.JsonProperty;

import de.witcom.api.config.properties.ApplicationProperties;
import lombok.Data;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class KeycloakFilterTest {

	@Autowired
	private ApplicationContext context;

	@Autowired
	private ApplicationProperties appProperties;

	@LocalServerPort
	private int port;

	@Value("${API_GW_OAUTH_TESTCLIENT_CLIENTSECRET}")
	String testClientClientSecret;
	@Value("${API_GW_OAUTH_TESTCLIENT_CLIENTID}")
	String testClientClientId;

	
	WebTestClient testclient;

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
	void executeUnauthenticatedRequestAndExpectitToFail(){
		testclient.get().uri("/get").exchange().expectStatus().isUnauthorized();
	}

	@Test
	void executeAuthenticatedRequestAndExpectItToSuceed(){

		TokenResponse token = getAccessToken();

		testclient.get()
			.uri("/get")
			.headers(header -> header.setBearerAuth(token.getAccessToken()))
			.exchange()
			.expectStatus()
			.is2xxSuccessful()
			;

	}

	@Test
	void executeAuthenticatedRequestWithNonStandardHeaderAndExpectItToSucceed(){

		TokenResponse token = getAccessToken();
		testclient.get()
			.uri("/get-fail")
			.headers(header -> header.add("Authorization", String.format("bEaReR: %s",token.getAccessToken())))
			.exchange()
			.expectStatus()
			.isUnauthorized()
			;

	}	

	@Test
	void executeAuthenticatedRequestWithMissingRoleAndExpectItToFail(){

		TokenResponse token = getAccessToken();

		testclient.get()
			.uri("/get-fail")
			.headers(header -> header.setBearerAuth(token.getAccessToken()))
			.exchange()
			.expectStatus()
			.isUnauthorized()
			;

	}	

	@Test
	void executeMisconfiguredAuthenticatedRequestAndExpectItToFail(){

		testclient.get()
			.uri("/get-fail")
			.headers(header -> header.add("Authorization", "paul"))
			.exchange()
			.expectStatus()
			.isUnauthorized()
			;

	}		

	@Test
	void executeAuthenticatedWithInvalidTokenRequestAndExpectItToFail(){

		testclient.get()
			.uri("/get-fail")
			.headers(header -> header.setBearerAuth("peter"))
			.exchange()
			.expectStatus()
			.isUnauthorized()
			;

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
