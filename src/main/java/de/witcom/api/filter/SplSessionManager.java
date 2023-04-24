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

import de.witcom.api.config.properties.ApplicationProperties;
import de.witcom.api.config.properties.ApplicationProperties.ServicePlanetTenantConfiguration;
import de.witcom.api.model.Session;
import de.witcom.api.repo.SessionRepository;
import de.witcom.api.spl.swagger.model.BooleanHolder;
import de.witcom.api.spl.swagger.model.UserLoginDto;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class SplSessionManager {
	
	private static final String APP_ID = "SERVICEPLANET";
	
    private final SessionRepository sessionRepo;
	private final ApplicationProperties appProperties;

	public SplSessionManager(SessionRepository sessionRepo, ApplicationProperties appProperties){
		this.sessionRepo = sessionRepo;
		this.appProperties = appProperties;
	}
	
	public String getSessionId(){
		//get session for the default tenant
		log.debug("No tenant provided - get session for default tenant");
		return getSessionId(getDefaultTenant());
	}

	/**
	 * Get sessino for given tenant-name
	 * 
	 * @param String tenant
	 * @return Session-ID
	 */
	public String getSessionId(String tenant) {

		if (appProperties.getSplConfig().getTenants() == null){
			throw new RuntimeException(String.format("Tenant %s not present in configuration",tenant));
		}
		//get the tenant-configuration
		ServicePlanetTenantConfiguration tenantConfig = appProperties.getSplConfig().getTenants()
			.stream()
			.filter(c -> c.getTenantName().equals(tenant))
			.findFirst()
			//not found ? there is a severe error in the config
			.orElseThrow(() -> new RuntimeException(String.format("Tenant %s not present in configuration",tenant)))
			;

		return getSessionId(tenantConfig);
	
	}

	private String getSessionId(ServicePlanetTenantConfiguration tenant) {
	    
	    Session session = loadSessionFromCache(tenant);
	    if (session != null){
	        //Todo: Validierung ob noch gueltig
	        return session.getSessionId();    
	    }
	    
	    //Keine Session-ID da -> Login
	    this.login(tenant);
	    session = loadSessionFromCache(tenant);
	    if (session != null){
	        return session.getSessionId();    
	    }
	    //hier ging was in die hose
	    log.warn("Unable to get Session-ID");
		return null;
		
	}
	
	private boolean isSessionActive(ServicePlanetTenantConfiguration tenant,String sessionId) {
		// log.debug("Check if Session {} is active",this.sessionId);
		
		
		String url = tenant.getSplBaseUrl() + "/serviceplanet/remote/service/v1/login/logged_in_user/active";
		RestTemplate restTemplate = new RestTemplate();

		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.setContentType(MediaType.APPLICATION_JSON);
		requestHeaders.set("Cookie", "JSESSIONID=" + sessionId);
		HttpEntity<Void> request = new HttpEntity<>(requestHeaders);

		try {
			ResponseEntity<BooleanHolder> response = restTemplate.exchange(url, HttpMethod.GET, request,
					BooleanHolder.class);
			if (!response.getStatusCode().equals(HttpStatus.OK)) {
				log.error("Login to ServicePlanet was not successful - got Status : {}", response.getStatusCode());
			} else {
				return response.getBody().isValue();
			}
		} catch (Exception e) {
			log.error("Error when trying to validate session in SPL {} : {}", url, e.getMessage());
		}

		return false;
	}
	
	private Session loadSessionFromCache(ServicePlanetTenantConfiguration tenant){

		String sessionKey = APP_ID + "_" + tenant.getTenantName();
		log.trace(String.format("Looking up session for tenant %s @ key %s",tenant.getSplTenant(),sessionKey));
		Optional<Session> session = this.sessionRepo.findById(sessionKey);
		if (session.isPresent()) {
			log.trace(String.format("Found session %s",session.get().toString()));
			return session.get(); 
		}
		return null;
	}

	private ServicePlanetTenantConfiguration getDefaultTenant(){

		//if there is no tenant-configuration-list, but the old deprecated single-tenant-config
		if(appProperties.getSplConfig().getTenants() == null || appProperties.getSplConfig().getTenants().isEmpty()){
			ServicePlanetTenantConfiguration config = new ServicePlanetTenantConfiguration();
			//use the old config
			log.warn("No SPL tenant-configuration present - use the old deprecated configuration");
			//if this is empty - let it fail
			config.setDefaultTenant(true);
			config.setSplBaseUrl(appProperties.getSplConfig().getSplBaseUrl());
			config.setSplUser(appProperties.getSplConfig().getSplUser());
			config.setSplPassword(appProperties.getSplConfig().getSplPassword());
			config.setTenantName("DEPRECATED_DEFAULT");
			return config;
		}

		//find and return the default tenant from the lis tof tenants
		return appProperties.getSplConfig().getTenants()
			.stream()
			.filter(c -> c.isDefaultTenant())
			//take the first tenant marked as default
			.findFirst()
			//if no tenant is marked as default - take the first one
			.orElse(appProperties.getSplConfig().getTenants().get(0));
		
	}
	
	private void login(ServicePlanetTenantConfiguration tenant) {
	    
	    if (StringUtils.isEmpty(tenant.getSplBaseUrl())){
	        log.error("SPL BaseURL is empty - unable to login");
	        return;
        }
   	    if (StringUtils.isEmpty(tenant.getSplUser())){
	        log.error("SPL User is empty - unable to login");
	        return;
        }
   	    if (StringUtils.isEmpty(tenant.getSplPassword())){
	        log.error("SPL Password is empty - unable to login");
	        return;
        }        

		String url = tenant.getSplBaseUrl() + "/serviceplanet/remote/service/v1/login/authenticate";

		RestTemplate restTemplate = new RestTemplate();

		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
		map.add("loginname", tenant.getSplUser());
		map.add("password", tenant.getSplPassword());
		map.add("allowDropOldestSession","true");
		if (!StringUtils.isEmpty(tenant.getSplTenant())){
			map.add("tenant", tenant.getSplTenant());
		}
		
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map,
				requestHeaders);
		try {
			ResponseEntity<Object> response = restTemplate.postForEntity(url, request, Object.class);
			if (!response.getStatusCode().equals(HttpStatus.OK)) {
				log.error("Login to ServicePlanet at {} was not successful - got Status : {}",url, response.getStatusCode());
			} else {
				if (response.getHeaders().get("Set-Cookie").isEmpty()) {
					log.error("Unable to extract sessionid - got no cookies");
				} else {

					String cookieValue = null;
					for (String cookie : response.getHeaders().get("set-cookie")) {
						List<HttpCookie> cookies = HttpCookie.parse(cookie);
						if (cookies.get(0).getName().equals("JSESSIONID")) {
							cookieValue = cookies.get(0).getValue();
						}
					}
					if (cookieValue == null) {
						log.error("Unable to login - no session cookie");
					} else {
						this.storeSession(tenant,cookieValue);
					}
				}
			}

		} catch (Exception e) {
			log.error("Error when trying to login to SPL at {} : {}",url, e.getMessage());

		}
	}
	
	//@Scheduled(cron = "0 0/5 * * * ?")
	@Scheduled(fixedDelayString = "300000", initialDelayString = "${random.int(60000)}")
	private void refreshSession() {
		if(!appProperties.getSplConfig().isEnabled()){
			return;
		}

		//refresh sessions for all tenants
		if(appProperties.getSplConfig().getTenants() != null){
			appProperties.getSplConfig().getTenants().forEach(tenant -> {
				refreshTenantSession(tenant);
			});
		} else {
			//old single-tenant configuration
			refreshTenantSession(getDefaultTenant());
		}

	}

	private void refreshTenantSession(ServicePlanetTenantConfiguration tenant){

		log.debug(String.format("Checking session for tenant %s", tenant.getTenantName()));
		Session session = loadSessionFromCache(tenant);
		if (session != null){
			//Todo: Validierung ob noch gueltig
			if (isSessionActive(tenant,session.getSessionId())) {
				return;
			}
		}
		log.warn(String.format("Session for tenant %s expired - refresh required",tenant.getTenantName()));
		this.login(tenant);

	}
	
	private void storeSession(ServicePlanetTenantConfiguration tenant,String sessionId) {
	    
		String sessionKey = APP_ID + "_" + tenant.getTenantName();
		log.debug(String.format("Storing Session-ID %s for tenant %s @ %s",sessionId,tenant.getTenantName(),sessionKey));
		Session session = new Session(sessionKey,sessionId);
		//Todo - add expiration date
	    sessionRepo.save(session);
	}

}
