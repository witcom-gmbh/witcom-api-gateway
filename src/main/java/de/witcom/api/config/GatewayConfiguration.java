package de.witcom.api.config;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractNameValueGatewayFilterFactory.NameValueConfig;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder.Builder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.util.Assert;

import de.witcom.api.command.client.CommandSessionManager;
import de.witcom.api.config.properties.ApplicationProperties;
import de.witcom.api.config.properties.ApplicationProperties.ServicePlanetTenantConfiguration;
import de.witcom.api.filter.KeyCloakFilter;
import de.witcom.api.filter.KeyCloakTokenService;
import de.witcom.api.filter.KeycloakCommandFilter;
import de.witcom.api.filter.ServicePlanetFilter;
import de.witcom.api.serviceplanet.SplSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Configuration
@RequiredArgsConstructor
@Log4j2
public class GatewayConfiguration {

    private final ApplicationProperties properties;
    private final SplSessionManager splSessionManager;
    private final CommandSessionManager cmdSessionManager;
    private final KeyCloakTokenService tokenService;

    private static final String HEADER_AUTHENTICATION = "Authentication";


    @Bean
    RouteLocator configureRoutes(RouteLocatorBuilder locatorBuilder) {
        log.info("Configuring api-gateway routes");

        Builder builder = locatorBuilder.routes();

        if (properties.getSplConfig().isEnabled()) {
            builder = addServicePlanetRoutes(builder);
        }
        if (properties.getCommandConfig().isEnabled()) {
            builder = addCommandRoutes(builder);
        }
        
        return builder
            .build();
        
    }

    private Builder addCommandRoutes(RouteLocatorBuilder.Builder builder) {
        String cmdBaseUrl = properties.getCommandConfig().getBaseUrl();
        String cmdResourceServer = "rmdb-resource-server";
        return builder
            .route("command-read", predicateSpec -> predicateSpec
                .path("/rmdb/api/rest/entity/easySearch/**",
                                "/rmdb/api/rest/entity/signalTrace/**",
                                "/rmdb/api/rest/entity/scrollQuery/getReport",
                                "/rmdb/api/rest/entity/zone/**")
                .filters(spec -> spec
                    .removeRequestHeader(HttpHeaders.ORIGIN)
                    .filter(commandKeycloakFilter(cmdResourceServer, "read", "read", null))
                    .rewritePath("/rmdb/(?<segment>.*)",
                        "/axis/${segment}")

                )
                .uri(cmdBaseUrl)
            )
            .route("command-base", predicateSpec -> predicateSpec
                .path("/rmdb/api/rest/entity/campus/**",
                                "/rmdb/api/rest/entity/building/**",
                                "/rmdb/api/rest/entity/floor/**",
                                "/rmdb/api/rest/entity/room/**",
                                "/rmdb/api/rest/entity/zone/**")
                .filters(spec -> spec
                    .removeRequestHeader(HttpHeaders.ORIGIN)
                    .filter(commandKeycloakFilter(cmdResourceServer, "read", "base-work", null))
                    .rewritePath("/rmdb/(?<segment>.*)",
                        "/axis/${segment}")

                )
                .uri(cmdBaseUrl)
            )
            .route("command-telco-unrouted", predicateSpec -> predicateSpec
                .path("/rmdb/api/rest/entity/serviceTelcoUnroutedMultipoint/**",
                                "/rmdb/api/rest/entity/serviceTelcoUnroutedPath/**",
                                "/rmdb/api/rest/entity/custom/tcoInterconnect/**",
                                "/rmdb/api/rest/entity/logicalPort/**",
                                "/rmdb/api/rest/entity/sVlan/**",
                                "/rmdb/api/rest/entity/vlan/**")
                .filters(spec -> spec
                    .removeRequestHeader(HttpHeaders.ORIGIN)
                    .filter(commandKeycloakFilter(
                                    cmdResourceServer, "read",
                                    "telco-unrouted-work",
                                    null))
                    .rewritePath("/rmdb/(?<segment>.*)",
                        "/axis/${segment}")

                )
                .uri(cmdBaseUrl)
            )
            .route("command-acm", predicateSpec -> predicateSpec
                .path("/rmdb/api/rest/entity/organization/**",
                                "/rmdb/api/rest/entity/person/**")
                .filters(spec -> spec
                    .removeRequestHeader(HttpHeaders.ORIGIN)
                    .filter(commandKeycloakFilter(cmdResourceServer, "read", "acm-work", null))
                    .rewritePath("/rmdb/(?<segment>.*)",
                        "/axis/${segment}")

                )
                .uri(cmdBaseUrl)
            )
            .route("command-configurationdata", predicateSpec -> predicateSpec
                .path("/rmdb/api/rest/entity/configurationData/**",
                                "/rmdb/api/rest/entity/configurationDataLayer/**",
                                "/rmdb/api/rest/entity/configurationDataAttributeSet/**",
                                "/rmdb/api/rest/entity/configurationDataConfiguration/**"
                            )
                .filters(spec -> spec
                    .removeRequestHeader(HttpHeaders.ORIGIN)
                    .filter(commandKeycloakFilter(cmdResourceServer, "read", "configurationdata-work", null))
                    .rewritePath("/rmdb/(?<segment>.*)",
                        "/axis/${segment}")

                )
                .uri(cmdBaseUrl)
            )            
            .route("command-default", predicateSpec -> predicateSpec
                .path("/rmdb/**")
                .filters(spec -> spec
                    .removeRequestHeader(HttpHeaders.ORIGIN)
                    .filter(commandKeycloakFilter(cmdResourceServer, "read", "full-work", null))
                    .rewritePath("/rmdb/(?<segment>.*)",
                        "/axis/${segment}")

                )
                .uri(cmdBaseUrl)
        );        
    }

    private RouteLocatorBuilder.Builder addServicePlanetRoutes(RouteLocatorBuilder.Builder builder) {
        Assert.notEmpty(properties.getSplConfig().getTenants(),
                        "Invalid SPL-Configuration - no tenants present");

        for (ServicePlanetTenantConfiguration tenant : properties.getSplConfig().getTenants()) {
            String tenantId = tenant.getTenantName().toLowerCase();
            String requiredRole = tenant.getRequiredResourceRole();
            log.debug("Setting up routes for spl-tenant {} on {}", tenantId, tenant.getSplBaseUrl());
            GatewayFilter filter = splSessionFilter(tenant.getTenantName());
            builder = builder
                // serviceplanet api-docs
                .route(String.format("spl-api-docs-%s", tenantId), predicateSpec -> predicateSpec
                        .path(String.format("/%s/remote/service/v1/docgen/swagger/**", tenantId))
                        .filters(spec -> spec
                            .removeRequestHeader(HttpHeaders.ORIGIN)
                            // we need the full path to the serviceplanet api. cloud-gateway only uses the host and port part of the URI
                            .rewritePath("/" + tenantId + "/(?<segment>.*)",
                                "/serviceplanet/remote/service/${segment}")
                        )
                        .uri(tenant.getSplBaseUrl())
                )
                // test route for spl-session
                .route(String.format("spl-test-%s", tenantId), predicateSpec -> predicateSpec
                        .path(String.format("/%s/get-with-spl-session", tenantId))
                        .filters(spec -> spec
                            .removeRequestHeader(HttpHeaders.ORIGIN)
                            // .removeRequestHeader(HttpHeaders.AUTHORIZATION)
                            // .removeRequestHeader(HEADER_AUTHENTICATION)
                            .filter(filter)
                            .rewritePath(String.format("/%s/get-with-spl-session", tenantId), "/get")
                        )
                        .uri("http://httpbin.org:80")
                )
                // serviceplanet api-calls
                .route(String.format("spl-api-%s", tenantId), predicateSpec -> predicateSpec
                        .path(String.format("/%s/**", tenantId))
                        .filters(spec -> spec
                            .removeRequestHeader(HttpHeaders.ORIGIN)
                            //.removeRequestHeader(HttpHeaders.AUTHORIZATION)
                            //.removeRequestHeader(HEADER_AUTHENTICATION)
                            .filter(keycloakFilter(requiredRole))
                            .filter(filter)
                            .removeRequestHeader(HttpHeaders.AUTHORIZATION)
                            .removeRequestHeader(HEADER_AUTHENTICATION)
                            // we need the full path to the serviceplanet api. cloud-gateway only uses the host and port part of the URI
                            .rewritePath("/" + tenantId + "/(?<segment>.*)",
                                "/serviceplanet/remote/service/${segment}")
                        )
                        .uri(tenant.getSplBaseUrl())

            )
            ; 
        }
        return builder;
    }


    private GatewayFilter keycloakFilter(String resourceRole) {

        NameValueConfig config =
            new NameValueConfig().setName("requiredRole").setValue(resourceRole);
        return new KeyCloakFilter(properties, tokenService).apply(config);

    }

    private GatewayFilter splSessionFilter(String tenant) {
        ServicePlanetFilter.Config config = new ServicePlanetFilter.Config();
        config.setTenant(tenant);
        ServicePlanetFilter filter = new ServicePlanetFilter(splSessionManager);
        return filter.apply(config);
    }
    
    private GatewayFilter commandKeycloakFilter(String resourceId, String roleRead, String roleWork, String roleDelete) {

        KeycloakCommandFilter.Config config = new KeycloakCommandFilter.Config();
        config.setResourceId(resourceId);
        config.setRoleDelete(roleDelete);
        config.setRoleRead(roleRead);
        config.setRoleWork(roleWork);
        KeycloakCommandFilter filter =
                        new KeycloakCommandFilter(cmdSessionManager, properties, tokenService);
        return filter.apply(config);
    }
}
