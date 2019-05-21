package de.witcom.api;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import de.witcom.api.repo.SessionRepository;
import de.witcom.api.model.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import de.witcom.api.model.OIDCCerts;
import de.witcom.api.model.OIDCKey;

@RunWith(SpringRunner.class)
@SpringBootTest
public class WitcomApiGatewayApplicationTests {
    
    @Autowired
    SessionRepository sessionRepo;

	@Test
	@Ignore
	public void contextLoads() {
	    Session session = new Session("my-app","fdsfdsfsdfds");
	    sessionRepo.save(session);
	    final Session retrievedSession = sessionRepo.findById(session.getSessionId()).get();
        assertEquals(session.getSessionId(), retrievedSession.getSessionId());
	}
	
	@Test
	public void webclientTest() {
	    
	    RestTemplate restTemplate = new RestTemplate();
	    OIDCCerts response3 = restTemplate.getForObject("https://auth.dev.witcom.services/auth/realms/witcom/protocol/openid-connect/certs", OIDCCerts.class);
	    /*
	   OIDCCerts response3 = WebClient.RequestBodySpec uri1 = WebClient.create("https://auth.dev.witcom.services2").method(HttpMethod.GET)
            .uri("/auth/realms/witcom/protocol/openid-connect/certs");*/
	   //OIDCCerts response3 = uri1.retrieve().bodyToMono(OIDCCerts.class).block(); 
	   for (OIDCKey key:response3.keys){
	       System.out.println(key.kid);
	       
	   }
	   //System.out.println(response3.toString());
	}

}
