package de.witcom.api.config.properties;

import java.util.List;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "application")
@RefreshScope
@Data
@Validated
public class ApplicationProperties {
    

    @Data
    public static class McpConfig {
        private String baseUrl="";
        @Deprecated
        private String apiKey="";
        private String user="";
        private String password="";
        private boolean enabled=true;
        
    }

    @Data
    public static class CommandConfig {
        
        private boolean enabled=true;
        private String baseUrl="";
        private String user="";
        private String password="";
        private String group=null;
        private String mandant=null;

        
    }


    @Data
    public static class SplConfig {
        
        @Deprecated
        private String splBaseUrl="";
        @Deprecated
        private String splUser;
        @Deprecated
        private String splPassword="";
        @Deprecated
        private String splTenant=null;

        private boolean enabled=true;
        //@NotEmpty
        private List<ServicePlanetTenantConfiguration> tenants;
        

    }

    @Data
    public static class ServicePlanetTenantConfiguration {

        private String splTenant;
        @NotNull
        private String splBaseUrl="";
        @NotNull
        private String splUser="";
        @NotNull
        private String splPassword="";
        @NotNull
        private String tenantName;
        private boolean defaultTenant;

    }    
    
    @Data
    public static class DslPortalConfig {

        private String username="";
        private String password="";

    }

    @Data
    public static class KeycloakConfig {

        private String keycloakServerUrl="";
        private String keycloakRealmId="";

    }

    private KeycloakConfig keycloakConfig;
    private SplConfig splConfig;
    private DslPortalConfig dslPortalConfig;
    private CommandConfig commandConfig;
    private McpConfig mcpConfig;
    //private List<ServicePlanetTenantConfiguration> splConfiguration;

    
    
}
