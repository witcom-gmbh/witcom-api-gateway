package de.witcom.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {
    
    private final KeycloakConfig keycloakConfig = new KeycloakConfig();
    private final SplConfig splConfig = new SplConfig();
    private final DslPortalConfig dslPortalConfig = new DslPortalConfig();

    public static class SplConfig {
        
        private String splBaseUrl="";
        private String splUser="";
        private String splPassword="";
        private String splTenant=null;
        
        public String getSplBaseUrl(){
            return splBaseUrl;
        }
        public void setSplBaseUrl(String splBaseUrl){
            this.splBaseUrl=splBaseUrl;
        }
        public String getSplUser(){
            return splUser;
        }
        public void setSplUser(String splUser){
            this.splUser=splUser;
        }
        public String getSplPassword(){
            return splPassword;
        }
        public void setSplPassword(String splPassword){
            this.splPassword=splPassword;
        }
        public String getSplTenant(){
            return splTenant;
        }
        public void setSplTenant(String splTenant){
            this.splTenant=splTenant;
        }
    }
    
    public static class DslPortalConfig {

        private String username="";
        private String password="";

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

    }

    public static class KeycloakConfig {

        private String keycloakServerUrl="";
        private String keycloakRealmId="";

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
    
    public KeycloakConfig getKeycloakConfig(){
        return keycloakConfig;
    }
    public SplConfig getSplConfig(){
        return splConfig;
    }
    
    public DslPortalConfig getDslPortalConfig(){
        return dslPortalConfig;
    }
    
    
}
