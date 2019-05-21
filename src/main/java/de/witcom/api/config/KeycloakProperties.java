package de.witcom.api.config;

public class KeycloakProperties {
	
	private String keycloakServerUrl;
	private String keycloakRealmId;
	
	public KeycloakProperties() {
		
		
	}
	
	public KeycloakProperties keycloakServerUrl(String keycloakServerUrl ) {
		this.keycloakServerUrl = keycloakServerUrl;
		return this;
	}
	public KeycloakProperties keycloakRealmId(String keycloakRealmId ) {
		this.keycloakRealmId = keycloakRealmId;
		return this;
	}	
	public String getKeycloakServerUrl() {
		return keycloakServerUrl;
	}

	public void setKeycloakServerUrl(String keycloakServerUrl) {
		this.keycloakServerUrl = keycloakServerUrl;
	}

	public String getKeycloakRealmId() {
		return keycloakRealmId;
	}

	public void setKeycloakRealmId(String keycloakRealmId) {
		this.keycloakRealmId = keycloakRealmId;
	}

}
