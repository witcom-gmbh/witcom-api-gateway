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
import org.springframework.test.context.TestPropertySource;

@RunWith(SpringRunner.class)
@SpringBootTest
//@TestPropertySource(locations="classpath:application.properties")
//@ActiveProfiles("staging")
public class WitcomApiGatewayApplicationTests {

	@Test
	public void contextLoads() {
	}
	

}
