package de.witcom.api.filter;

import java.net.HttpCookie;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.apache.commons.lang3.StringUtils;

import de.witcom.api.config.ApplicationProperties;
import de.witcom.api.model.Session;
import de.witcom.api.repo.SessionRepository;
import de.witcom.api.spl.swagger.model.BooleanHolder;
import de.witcom.api.spl.swagger.model.UserLoginDto;

@Service
public class SplSessionManager {
	
	/*
	@Value("${SPL_BASEURL:#{null}}")
	private String splBaseUrl;
	@Value("${SPL_USER:#{null}}")
	private String splUser;
	@Value("${SPL_PASSWORD:#{null}}")
	private String splPassword;
	@Value("${application.spl-config.spl-tenant:#{null}}")
	private String splTenant;
	*/

	private static final String APP_ID = "SERVICEPLANET";
	
    @Autowired
    SessionRepository sessionRepo;

	@Autowired
	ApplicationProperties appProperties;


	Logger logger = LoggerFactory.getLogger(SplSessionManager.class);
	
	public String getSessionId() {
	    
	    Session session = loadSessionFromCache();
	    if (session != null){
	        //Todo: Validierung ob noch gueltig
	        return session.getSessionId();    
	    }
	    
	    //Keine Session-ID da -> Login
	    this.login();
	    session = loadSessionFromCache();
	    if (session != null){
	        return session.getSessionId();    
	    }
	    //hier ging was in die hose
	    logger.warn("Unable to get Session-ID");
		return null;
		
	}
	
	private boolean isSessionActive(String sessionId) {
		// logger.debug("Check if Session {} is active",this.sessionId);
		
		
		String url = appProperties.getSplConfig().getSplBaseUrl() + "/serviceplanet/remote/service/v1/login/logged_in_user/active";
		RestTemplate restTemplate = new RestTemplate();

		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.setContentType(MediaType.APPLICATION_JSON);
		requestHeaders.set("Cookie", "JSESSIONID=" + sessionId);
		HttpEntity<Void> request = new HttpEntity<>(requestHeaders);

		try {
			ResponseEntity<BooleanHolder> response = restTemplate.exchange(url, HttpMethod.GET, request,
					BooleanHolder.class);
			if (!response.getStatusCode().equals(HttpStatus.OK)) {
				logger.error("Login to ServicePlanet was not successful - got Status : {}", response.getStatusCode());
			} else {
				return response.getBody().isValue();
			}
		} catch (Exception e) {
			logger.error("Error when trying to validate session in SPL {} : {}", url, e.getMessage());
		}

		return false;
	}
	
	private Session loadSessionFromCache(){
	   Optional<Session> session = this.sessionRepo.findById(APP_ID);
	   if (session.isPresent()) {
	       return session.get(); 
       }
       return null;
	}
	
	private void login() {
	    
	    if (StringUtils.isEmpty(appProperties.getSplConfig().getSplBaseUrl())){
	        logger.error("SPL BaseURL is empty - unable to login");
	        return;
        }
   	    if (StringUtils.isEmpty(appProperties.getSplConfig().getSplUser())){
	        logger.error("SPL User is empty - unable to login");
	        return;
        }
   	    if (StringUtils.isEmpty(appProperties.getSplConfig().getSplPassword())){
	        logger.error("SPL Password is empty - unable to login");
	        return;
        }        

		String url = appProperties.getSplConfig().getSplBaseUrl() + "/serviceplanet/remote/service/v1/login/authenticate";

		RestTemplate restTemplate = new RestTemplate();

		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
		map.add("loginname", appProperties.getSplConfig().getSplUser());
		map.add("password", appProperties.getSplConfig().getSplPassword());
		map.add("allowDropOldestSession","true");
		if (!StringUtils.isEmpty(appProperties.getSplConfig().getSplTenant())){
			map.add("tenant", appProperties.getSplConfig().getSplTenant());
		}
		
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map,
				requestHeaders);
		try {
			ResponseEntity<Object> response = restTemplate.postForEntity(url, request, Object.class);
			if (!response.getStatusCode().equals(HttpStatus.OK)) {
				logger.error("Login to ServicePlanet at {} was not successful - got Status : {}",url, response.getStatusCode());
			} else {
				if (response.getHeaders().get("Set-Cookie").isEmpty()) {
					logger.error("Unable to extract sessionid - got no cookies");
				} else {

					String cookieValue = null;
					for (String cookie : response.getHeaders().get("set-cookie")) {
						List<HttpCookie> cookies = HttpCookie.parse(cookie);
						if (cookies.get(0).getName().equals("JSESSIONID")) {
							cookieValue = cookies.get(0).getValue();
						}
					}
					if (cookieValue == null) {
						logger.error("Unable to login - no session cookie");
					} else {
						this.storeSession(cookieValue);
					}
				}
			}

		} catch (Exception e) {
			logger.error("Error when trying to login to SPL at {} : {}",url, e.getMessage());

		}
	}
	
	//@Scheduled(cron = "0 0/5 * * * ?")
	@Scheduled(fixedDelayString = "300000", initialDelayString = "${random.int(60000)}")
	private void refreshSession() {
	    logger.info("Refreshing session with SPL");
	    Session session = loadSessionFromCache();
	    if (session != null){
	        //Todo: Validierung ob noch gueltig
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
		//Todo - add expiration date
	    sessionRepo.save(session);
	}

}
