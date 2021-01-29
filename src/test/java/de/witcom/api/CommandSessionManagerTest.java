package de.witcom.api;

import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import de.witcom.api.command.client.CommandSessionManager;

@SpringBootTest
public class CommandSessionManagerTest {

	@Autowired
	private CommandSessionManager sessionManager;
	
	@Test
	@Ignore
	void contextLoads() {
		
		this.sessionManager.getSessionId();
		this.sessionManager.getSessionId();
		
	}
	

}
