package de.witcom.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
@RefreshScope
public class ApplicationProperties {
    
    private final KeycloakConfig keycloakConfig = new KeycloakConfig();
    private final SplConfig splConfig = new SplConfig();
    private final DslPortalConfig dslPortalConfig = new DslPortalConfig();
    private final CommandConfig commandConfig = new CommandConfig();
    private final McpConfig mcpConfig = new McpConfig();

    public static class McpConfig {
        private String baseUrl="";
        @Deprecated
        private String apiKey="";
        private String user="";
        private String password="";
        
        public String getBaseUrl() {
            return baseUrl;
        }
        public String getPassword() {
            return password;
        }
        public void setPassword(String password) {
            this.password = password;
        }
        public String getUser() {
            return user;
        }
        public void setUser(String user) {
            this.user = user;
        }
        @Deprecated
        public String getApiKey() {
            return apiKey;
        }
        @Deprecated
        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        @Override
        public String toString() {
            return "McpConfig [apiKey=" + apiKey + ", baseUrl=" + baseUrl + "]";
        }
        
    }
    public static class CommandConfig {
        
		private String baseUrl="";
        private String user="";
        private String password="";
        private String group=null;
        private String mandant=null;

		public String getBaseUrl() {
			return baseUrl;
		}



		public void setBaseUrl(String baseUrl) {
			this.baseUrl = baseUrl;
		}



		public String getUser() {
			return user;
		}



		public void setUser(String user) {
			this.user = user;
		}



		public String getMandant() {
			return mandant;
		}



		public void setMandant(String mandant) {
			this.mandant = mandant;
		}



		public String getGroup() {
			return group;
		}



		public void setGroup(String group) {
			this.group = group;
		}



		public String getPassword() {
			return password;
		}



		public void setPassword(String password) {
			this.password = password;
		}



		@Override
		public String toString() {
			return "CommandConfig [baseUrl=" + baseUrl + ", user=" + user + ", password=" + password + ", group="
					+ group + ", mandant=" + mandant + "]";
		}
		
        
    }

    
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
		@Override
		public String toString() {
			return "SplConfig [splBaseUrl=" + splBaseUrl + ", splUser=" + splUser + ", splPassword=" + splPassword
					+ ", splTenant=" + splTenant + "]";
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
    public McpConfig getMcpConfig() {
        return mcpConfig;
    }
    public SplConfig getSplConfig(){
        return splConfig;
    }
    
    public DslPortalConfig getDslPortalConfig(){
        return dslPortalConfig;
    }
	public CommandConfig getCommandConfig() {
		return commandConfig;
	}
    
    
}
