package de.witcom.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import de.witcom.api.repo.SessionRepository;
import de.witcom.api.serviceplanet.SplSessionManager;

@SpringBootTest
public class ServicePlanetSessionManagerTests {

	@Autowired
	SplSessionManager sessionManager;

	@Autowired
    SessionRepository sessionRepo;


	@Test
	void getSessionIdForDefaultTenant() {
		sessionRepo.deleteAll();
		assertEquals("a-random-sessionid",this.sessionManager.getSessionId());
		assertEquals("a-random-sessionid",this.sessionManager.getSessionId());
	}

}
