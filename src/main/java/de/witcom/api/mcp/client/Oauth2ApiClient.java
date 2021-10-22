package de.witcom.api.mcp.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name="${oauth2.name:mcp-oauth2}", url="${application.mcp-config.base-url:http://localhost}/tron")
public interface Oauth2ApiClient extends Oauth2Api {
}
