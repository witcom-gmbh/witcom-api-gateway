/**
 * NOTE: This class is auto generated by the swagger code generator program (2.4.0).
 * https://github.com/swagger-api/swagger-codegen
 * Do not edit the class manually.
 */
package de.witcom.api.command.client;

import de.witcom.api.command.swagger.model.LoginRequest;
import de.witcom.api.command.swagger.model.LoginResponse;
import de.witcom.api.command.swagger.model.ServiceStatusData;
import io.swagger.annotations.*;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
@jakarta.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2021-01-28T19:07:35.638+01:00")

@Api(value = "Rest", description = "the Rest API")
public interface RestApi {

    @ApiOperation(value = "", nickname = "login", notes = "", response = LoginResponse.class, tags={ "REST", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "returns LoginResponse", response = LoginResponse.class) })
    @RequestMapping(value = "/api/rest/businessGateway/login",
        produces = "application/json", 
        consumes = "application/json",
        method = RequestMethod.POST)
    LoginResponse login(@ApiParam(value = "" ,required=true )  @Valid @RequestBody LoginRequest body);


    @ApiOperation(value = "", nickname = "logout", notes = "", response = ServiceStatusData.class, tags={ "REST", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "returns LogoutResponse", response = ServiceStatusData.class) })
    @RequestMapping(value = "/api/rest/businessGateway/logout",
        produces = "application/json", 
        consumes = "",
        method = RequestMethod.POST)
    ServiceStatusData logout(@NotNull @ApiParam(value = "Session-ID", required = true) @Valid @RequestParam(value = "sessionId", required = true) String sessionId);

}
