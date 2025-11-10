package de.witcom.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import de.witcom.api.command.client.CommandSessionManager;
import de.witcom.api.repo.SessionRepository;
import lombok.extern.log4j.Log4j2;

@SpringBootTest
@Log4j2
public class CommandSessionManagerTest {

    @Autowired
    private CommandSessionManager sessionManager;

    @Autowired
    SessionRepository sessionRepo;

    
    @Test
    void getSessionId() {

        
        sessionRepo.deleteAll();
        
        assertEquals("a-random-sessionid",this.sessionManager.getSessionId());
        assertEquals("a-random-sessionid",this.sessionManager.getSessionId());
        
        
    }
    

}
