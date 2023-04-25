package de.witcom.api.command.client;

import java.util.Optional;

import javax.annotation.PreDestroy;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import de.witcom.api.command.swagger.model.LoginGetActiveMandatorRequest;
import de.witcom.api.command.swagger.model.LoginGetActiveMandatorResponse;
import de.witcom.api.command.swagger.model.LoginRequest;
import de.witcom.api.command.swagger.model.LoginResponse;
import de.witcom.api.command.swagger.model.ServiceStatusData;
import de.witcom.api.config.properties.ApplicationProperties;
import de.witcom.api.model.Session;
import de.witcom.api.repo.SessionRepository;

@Service
public class CommandSessionManager {
	
	//@Autowired
	private final RestApiClient apiClient;
	
	//@Autowired
	private final LoginApiClient loginClient;
	
	private String lastSession;
	
	private static final String APP_ID = "COMMAND";
	
    //@Autowired
    private final SessionRepository sessionRepo;

	//@Autowired
	private final ApplicationProperties appProperties;
	
	Logger logger = LoggerFactory.getLogger(this.getClass());

	public CommandSessionManager(@Lazy RestApiClient apiClient,@Lazy LoginApiClient loginClient,SessionRepository sessionRepo,ApplicationProperties appProperties){
		this.apiClient = apiClient;
		this.appProperties = appProperties;
		this.sessionRepo = sessionRepo;
		this.loginClient = loginClient;
	}

	public String getSessionId() {
	    
	    Session session = loadSessionFromCache();
	    if (session != null){
	        //Todo: Validierung ob noch gueltig
	    	
	    	//if (this.isSessionActive(session.getSessionId())) {
	    		return session.getSessionId();
	    	//}
	    	//logger.debug("Session is NOT active");
	    }
	    
	    //Keine Session-ID da -> Login
	    logger.debug("Perform login");
	    this.login();
	    
	    session = this.loadSessionFromCache();
	    if (session != null){
	        return session.getSessionId();    
	    }
	    //hier ging was in die hose
	    logger.warn("Unable to get Session-ID");
		return null;
		
	}
	
	private Session loadSessionFromCache(){
		   Optional<Session> session = this.sessionRepo.findById(APP_ID);
		   if (session.isPresent()) {
			   this.lastSession=session.get().getSessionId();
		       return session.get(); 
	       }
	       return null;
		}	
	
	private boolean isSessionActive(String sessionId) {
		logger.debug("Check for session {}",sessionId);
		
		if (!this.isConfigurationValid()) {
			return false;
		}
		@Valid
		LoginGetActiveMandatorRequest body = new LoginGetActiveMandatorRequest();
		
		try {
			LoginGetActiveMandatorResponse loginResponse = this.loginClient.loginGetActiveMandator(sessionId, body );
			
				if (!loginResponse.getStatus().isSuccess()){
					logger.error("Sessioncheck in command  was not successful - got Status : {}",loginResponse.getStatus().getMessage());
					return false;
				}
				//ok so far
				logger.debug("session {} is active",sessionId);
				return true;
		} catch (Exception e) {
			logger.error("Error when trying to check session in command: {}", e.getMessage());
		}
		
		return false;
	}
	
	private boolean isConfigurationValid() {
		
		if (StringUtils.isEmpty(appProperties.getCommandConfig().getGroup())){
	        logger.error("Command group  is empty - unable to login");
	        return false;
        }
   	    if (StringUtils.isEmpty(appProperties.getCommandConfig().getUser())){
	        logger.error("Command User is empty - unable to login");
	        return false;
        }
   	    if (StringUtils.isEmpty(appProperties.getCommandConfig().getPassword())){
	        logger.error("Command Password is empty - unable to login");
	        return false;
        }        
   	    if (StringUtils.isEmpty(appProperties.getCommandConfig().getMandant())){
	        logger.error("Command Mandant is empty - unable to login");
	        return false;
        }	
   	    
   	    return true;
		
	}
	
	private void login() {
		
		if (!this.isConfigurationValid()) {
			return;
		}
		@Valid
		LoginRequest login = new LoginRequest();
		login.setManId(appProperties.getCommandConfig().getMandant());
		login.setUserGroupName(appProperties.getCommandConfig().getGroup());
		login.setUser(appProperties.getCommandConfig().getUser());
		login.setPassword(appProperties.getCommandConfig().getPassword());
		
		try {
			LoginResponse loginResponse = apiClient.login(login);
			if (!loginResponse.getStatus().isSuccess()){
				logger.error("Login to Command  was not successful - got Status : {}",loginResponse.getStatus().getMessage());
				return;
			}
			//ok so far
			this.storeSession(loginResponse.getSessionId());
		} catch (Exception e) {
			logger.error("Error when trying to login to command: {}", e.getMessage());
		}
	}
	
	public void refreshSession() {
	    logger.info("Refreshing session with Command");
	    Session session = loadSessionFromCache();
	    if (session != null){
	        if (isSessionActive(session.getSessionId())) {
			 return;
		    }
	    }
		this.login();
	}
	
	//@Scheduled(cron = "0 0/5 * * * ?")
	@Scheduled(fixedDelayString = "300000", initialDelayString = "${random.int(60000)}")
	private void autoRefreshSession() {

		if (!appProperties.getCommandConfig().isEnabled()){
			return;
		}

	    logger.info("Refreshing session with Command");
	    Session session = loadSessionFromCache();
	    if (session != null){
	        if (isSessionActive(session.getSessionId())) {
			 return;
		    }
	    }
	    logger.warn("Session expired - refresh required");
		this.login();
	}	
	
	private void storeSession(String sessionId) {
	    logger.debug("Storing Session-ID {}",sessionId);
		Session session = new Session(APP_ID,sessionId);
		this.lastSession=sessionId;
		//Todo - add expiration date
	    sessionRepo.save(session);
	}	
	
	@PreDestroy
	private void shutdown() {

		if (!appProperties.getCommandConfig().isEnabled()){
			return;
		}

		logger.debug("Perform logout from command");
		
		String sessionId=this.lastSession;
		if(sessionId != null) {
			try {
				this.apiClient.logout(sessionId);
			} catch (Exception e) {
				logger.error("Error when trying to logout from command: {}", e.getMessage());
			}
		}
	}
	

}
