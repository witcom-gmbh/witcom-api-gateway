package de.witcom.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import de.witcom.api.command.client.CommandSessionManager;
import lombok.extern.log4j.Log4j2;

@SpringBootTest
@Log4j2
public class CommandSessionManagerTest {

	@Autowired
	private CommandSessionManager sessionManager;
	
	@Test
	void contextLoads() {

		log.debug("Hello World"); 
		
		// this.sessionManager.getSessionId();
		// this.sessionManager.getSessionId();
		
	}
	

}
