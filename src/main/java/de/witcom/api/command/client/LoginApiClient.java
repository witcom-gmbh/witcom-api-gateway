package de.witcom.api.command.client;

import org.springframework.cloud.openfeign.FeignClient;

import de.witcom.api.command.client.configuration.ClientConfiguration;

@FeignClient(name="loginApiClient", url="${application.command-config.base-url}" ,configuration = ClientConfiguration.class)
public interface LoginApiClient extends LoginApi {
}