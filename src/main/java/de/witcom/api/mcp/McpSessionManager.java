package de.witcom.api.mcp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.Optional;

import javax.annotation.PreDestroy;

import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.witcom.api.config.properties.ApplicationProperties;
import de.witcom.api.mcp.client.Oauth2Api;
import de.witcom.api.mcp.client.Oauth2ApiClient;
import de.witcom.api.mcp.tron.ApiClient;
import de.witcom.api.mcp.tron.model.LoginInfoSerializer;
import de.witcom.api.mcp.tron.model.OAuth2TokenSerializer;
import de.witcom.api.model.Session;
import de.witcom.api.repo.SessionRepository;

@Service
public class McpSessionManager {

    private String lastSession;
	
	private static final String APP_ID = "MCP";
	

    @Autowired
    private ApiClient mcpAuthClient;

    private Oauth2Api tokensApi;

    @Autowired
    SessionRepository sessionRepo;

	@Autowired
	ApplicationProperties appProperties;
	
	Logger logger = LoggerFactory.getLogger(this.getClass());

    public McpSessionManager(ApiClient mcpAuthClient){

        this.tokensApi= mcpAuthClient.buildClient(Oauth2Api.class);

    }

	public String getSessionId() {
	    
	    Session session = loadSessionFromCache();
	    if (session != null){
	        //Todo: Validierung ob noch gueltig
			if (this.isSessionActive(session.getSessionId())) {
	    		return session.getSessionId();
	    	}
	    	logger.debug("Session is NOT active");
	    }
	    
	    //Keine Session-ID da -> Login
	    logger.debug("Perform login to MCP");
	    this.login();
	    
	    session = this.loadSessionFromCache();
	    if (session != null){
	        return session.getSessionId();    
	    }
	    //hier ging was in die hose
	    logger.warn("Unable to get MCP Token");
		return null;
		
	}

	private void logoutSession(String sessionId){

		String url = appProperties.getMcpConfig().getBaseUrl() + "/tron/api/v1/tokens/" + sessionId;
		RestTemplate restTemplate = new RestTemplate();

		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.setContentType(MediaType.APPLICATION_JSON);
		requestHeaders.set("Authorization", "Bearer " + sessionId);
		HttpEntity<Void> request = new HttpEntity<>(requestHeaders);


		try {
			
			ResponseEntity<LoginInfoSerializer> response = restTemplate.exchange(url, HttpMethod.DELETE, request,
					LoginInfoSerializer.class);
			
			if (!response.getStatusCode().equals(HttpStatus.NO_CONTENT)) {
				logger.error("Deleting session in MCP was not successful - got Status : {}", response.getStatusCode());
			}
		} catch (Exception e) {
			logger.error("Error when trying to delete session in MCP {} : {}", url, e.getMessage());
		}


	}
    
    private boolean isSessionActive(String sessionId) {
		logger.debug("Check for MCP session {}",sessionId);
		
		if (!this.isConfigurationValid()) {
			return false;
		}

		String url = appProperties.getMcpConfig().getBaseUrl() + "/tron/api/v1/login-info";
		RestTemplate restTemplate = new RestTemplate();

		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.setContentType(MediaType.APPLICATION_JSON);
		requestHeaders.set("Authorization", "Bearer " + sessionId);
		HttpEntity<Void> request = new HttpEntity<>(requestHeaders);

		try {
			
			ResponseEntity<LoginInfoSerializer> response = restTemplate.exchange(url, HttpMethod.GET, request,
					LoginInfoSerializer.class);
			
			if (!response.getStatusCode().equals(HttpStatus.OK)) {
				logger.error("Session validation in MCP was not successful - got Status : {}", response.getStatusCode());
			} else {
				return true;
			}
		} catch (Exception e) {
			logger.error("Error when trying to validate session in MCP {} : {}", url, e.getMessage());
		}

		return false;

    }

	public void refreshSession() {
	    logger.info("Refreshing session with MCP");
	    Session session = loadSessionFromCache();
	    if (session != null){
            if (isSessionActive(session.getSessionId())) {
			    return;
		    }
	    }
		this.login();
	}    

    private void login() {
        if (!this.isConfigurationValid()) {
			return;
		}

        try {
            OAuth2TokenSerializer res = this.tokensApi.oauth2TokensCreate(appProperties.getMcpConfig().getUser(), appProperties.getMcpConfig().getPassword(), "password", null, null, null, null, null, null, null);
            this.storeSession(res.getAccessToken());
        } catch (Exception e){
            logger.error("Error when trying to login to MCP: {}", e.getMessage());
        }

    }

	private void storeSession(String sessionId) {
	    logger.debug("Storing Session-ID {}",sessionId);
		Session session = new Session(APP_ID,sessionId);
		this.lastSession=sessionId;
	    sessionRepo.save(session);
	}    

    private Session loadSessionFromCache(){
        Optional<Session> session = this.sessionRepo.findById(APP_ID);
        if (session.isPresent()) {
            this.lastSession=session.get().getSessionId();
            return session.get(); 
        }
        return null;
     }	

    private boolean isConfigurationValid() {
		
		if (StringUtils.isEmpty(appProperties.getMcpConfig().getBaseUrl())){
	        logger.error("MCP baseurl is empty - unable to login");
	        return false;
        }
   	    if (StringUtils.isEmpty(appProperties.getMcpConfig().getUser())){
	        logger.error("MCP User is empty - unable to login");
	        return false;
        }
   	    if (StringUtils.isEmpty(appProperties.getMcpConfig().getPassword())){
	        logger.error("MCP Password is empty - unable to login");
	        return false;
        }        
   	   	
   	    
   	    return true;
		
	}

	public void triggerSessionRefresh(){
		//we could perform a logout here for forcing a session refresh
		this.autoRefreshSession();
	}

	@Scheduled(fixedDelayString = "300000", initialDelayString = "${random.int(60000)}")
	private void autoRefreshSession() {
		if (!appProperties.getMcpConfig().isEnabled()){
			return;
		}
	    logger.info("Refreshing session with MCP");
	    Session session = loadSessionFromCache();
	    if (session != null){
	        if (isSessionActive(session.getSessionId())) {
			 return;
		    }
	    }
	    logger.warn("Session expired - refresh required");
		this.login();
	}	

	@PreDestroy
	private void shutdown() {
		if (!appProperties.getMcpConfig().isEnabled()){
			return;
		}
		logger.debug("Perform logout from MCP");
		
		String sessionId=this.lastSession;
		if(sessionId != null) {
			try {
				this.logoutSession(sessionId);
			} catch (Exception e) {
				logger.error("Error when trying to logout from MCP: {}", e.getMessage());
			}
		}
	}	

}