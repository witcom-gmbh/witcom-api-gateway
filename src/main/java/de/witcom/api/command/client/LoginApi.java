/**
 * NOTE: This class is auto generated by the swagger code generator program (2.4.0).
 * https://github.com/swagger-api/swagger-codegen
 * Do not edit the class manually.
 */
package de.witcom.api.command.client;

import de.witcom.api.command.swagger.model.ChangeMandatorLoginRequestData;
import de.witcom.api.command.swagger.model.ChangeMandatorLoginResponse;
import de.witcom.api.command.swagger.model.ForeignLoginLoginRequestData;
import de.witcom.api.command.swagger.model.ForeignLoginLoginResponse;
import de.witcom.api.command.swagger.model.LoginGetActiveMandatorRequest;
import de.witcom.api.command.swagger.model.LoginGetActiveMandatorResponse;
import de.witcom.api.command.swagger.model.LoginGetMandatorsAndUserGroupsRequest;
import de.witcom.api.command.swagger.model.LoginGetMandatorsAndUserGroupsResponse;
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

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.util.List;
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2021-01-29T12:00:58.236+01:00")

@Api(value = "Login", description = "the Login API")
public interface LoginApi {

    @ApiOperation(value = "Change mandator / group", nickname = "changeMandatorLogin", notes = "Modifies the active mandator", response = ChangeMandatorLoginResponse.class, tags={ "login", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "returns ChangeMandatorLoginResponse", response = ChangeMandatorLoginResponse.class) })
    @RequestMapping(value = "/api/rest/entity/login/changeMandator",
        produces = "application/json", 
        consumes = "application/json",
        method = RequestMethod.POST)
    ChangeMandatorLoginResponse changeMandatorLogin(@NotNull @ApiParam(value = "Session-ID", required = true) @Valid @RequestParam(value = "sessionId", required = true) String sessionId,@ApiParam(value = "" ,required=true )  @Valid @RequestBody ChangeMandatorLoginRequestData body);


    @ApiOperation(value = "Foreign login", nickname = "foreignLoginLogin", notes = "Allows you to create a session for an arbitrary user using a technical user.", response = ForeignLoginLoginResponse.class, tags={ "login", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "returns ForeignLoginLoginResponse", response = ForeignLoginLoginResponse.class) })
    @RequestMapping(value = "/api/rest/entity/login/foreignLogin",
        produces = "application/json", 
        consumes = "application/json",
        method = RequestMethod.POST)
    ForeignLoginLoginResponse foreignLoginLogin(@NotNull @ApiParam(value = "Session-ID", required = true) @Valid @RequestParam(value = "sessionId", required = true) String sessionId,@ApiParam(value = "" ,required=true )  @Valid @RequestBody ForeignLoginLoginRequestData body);


    @ApiOperation(value = "Active mandator and user / group", nickname = "loginGetActiveMandator", notes = "Queries the active mandator and the user / group", response = LoginGetActiveMandatorResponse.class, tags={ "login", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "returns LoginGetActiveMandatorResponse", response = LoginGetActiveMandatorResponse.class) })
    @RequestMapping(value = "/api/rest/entity/login/getActiveMandator",
        produces = "application/json", 
        consumes = "application/json",
        method = RequestMethod.POST)
    LoginGetActiveMandatorResponse loginGetActiveMandator(@NotNull @ApiParam(value = "Session-ID", required = true) @Valid @RequestParam(value = "sessionId", required = true) String sessionId,@ApiParam(value = "" ,required=true )  @Valid @RequestBody LoginGetActiveMandatorRequest body);


    @ApiOperation(value = "Mandators, users and groups", nickname = "loginGetMandatorsAndUserGroups", notes = "Queries all possible mandators and their users / groups", response = LoginGetMandatorsAndUserGroupsResponse.class, tags={ "login", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "returns LoginGetMandatorsAndUserGroupsResponse", response = LoginGetMandatorsAndUserGroupsResponse.class) })
    @RequestMapping(value = "/api/rest/entity/login/getMandatorsAndUserGroups",
        produces = "application/json", 
        consumes = "application/json",
        method = RequestMethod.POST)
    LoginGetMandatorsAndUserGroupsResponse loginGetMandatorsAndUserGroups(@NotNull @ApiParam(value = "Session-ID", required = true) @Valid @RequestParam(value = "sessionId", required = true) String sessionId,@ApiParam(value = "" ,required=true )  @Valid @RequestBody LoginGetMandatorsAndUserGroupsRequest body);

}
