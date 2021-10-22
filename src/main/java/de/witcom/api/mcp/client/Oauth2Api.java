package de.witcom.api.mcp.client;

import de.witcom.api.mcp.tron.ApiClient;
import de.witcom.api.mcp.tron.EncodingUtils;

import de.witcom.api.mcp.tron.model.OAuth2TokenSerializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import feign.*;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2021-10-22T07:21:47.695783+02:00[Europe/Berlin]")
public interface Oauth2Api extends ApiClient.Api {


  /**
   * Change to the tenant context specified via tenant_context.
   * Change to the tenant context specified via tenant_context.
   * @param token  (required)
   * @param username Request token for user with username (required)
   * @param password Password for user (required)
   * @param grantType Accepted value is \\\&quot;password\\\&quot; (required)
   * @param tenant Tenant name of user (optional)
   * @param tenantContext Tenant name, request token for this tenant, applicable only if user can access multiple tenants (optional)
   * @param expiresIn Token expires in timeout seconds from created time (optional)
   * @param inactiveExpirationTime  (optional)
   * @param isSuccessful Deprecated. Only successfull logins are stored. (optional)
   * @param userTenantUuid  (optional)
   * @param radiusState Represents the Access-Challenge State attribute (optional)
   * @return OAuth2TokenSerializer
   */
  @RequestLine("POST /api/v1/oauth2/tokens/{token}/change_tenant_context")
  @Headers({
    "Content-Type: application/x-www-form-urlencoded",
    "Accept: */*",
  })
  OAuth2TokenSerializer oauth2TokensChangeTenantContext(@Param("token") String token, @Param("username") String username, @Param("password") String password, @Param("grant_type") String grantType, @Param("tenant") String tenant, @Param("tenant_context") String tenantContext, @Param("expires_in") Integer expiresIn, @Param("inactive_expiration_time") String inactiveExpirationTime, @Param("is_successful") String isSuccessful, @Param("user_tenant_uuid") String userTenantUuid, @Param("radius_state") String radiusState);

  /**
   * Create an OAuth2 Token.
   * Create an OAuth2 Token.
   * @param username Request token for user with username (required)
   * @param password Password for user (required)
   * @param grantType Accepted value is \\\&quot;password\\\&quot; (required)
   * @param tenant Tenant name of user (optional)
   * @param tenantContext Tenant name, request token for this tenant, applicable only if user can access multiple tenants (optional)
   * @param expiresIn Token expires in timeout seconds from created time (optional)
   * @param inactiveExpirationTime  (optional)
   * @param isSuccessful Deprecated. Only successfull logins are stored. (optional)
   * @param userTenantUuid  (optional)
   * @param radiusState Represents the Access-Challenge State attribute (optional)
   * @return OAuth2TokenSerializer
   */
  @RequestLine("POST /api/v1/oauth2/tokens")
  @Headers({
    "Content-Type: application/x-www-form-urlencoded",
    "Accept: */*",
  })
  OAuth2TokenSerializer oauth2TokensCreate(@Param("username") String username, @Param("password") String password, @Param("grant_type") String grantType, @Param("tenant") String tenant, @Param("tenant_context") String tenantContext, @Param("expires_in") Integer expiresIn, @Param("inactive_expiration_time") String inactiveExpirationTime, @Param("is_successful") String isSuccessful, @Param("user_tenant_uuid") String userTenantUuid, @Param("radius_state") String radiusState);

  /**
   * Delete an OAuth2 token.
   * Delete an OAuth2 token.
   * @param token  (required)
   */
  @RequestLine("DELETE /api/v1/oauth2/tokens/{token}")
  @Headers({
    "Accept: application/json",
  })
  void oauth2TokensDelete(@Param("token") String token);
}
