package com.apigee.diagnosis.service;

import com.apigee.diagnosis.beans.APIDeploymentState;
import com.apigee.diagnosis.beans.DiagnosticReport;
import com.apigee.diagnosis.beans.MPAPIDeploymentReport;
import com.apigee.diagnosis.beans.MPInformationReport;
import com.apigee.diagnosis.deployment.DeploymentAPIService;
import com.apigee.diagnosis.deployment.MPAPIDeployInfoService;
import com.apigee.diagnosis.deployment.ZKAPIDeployInfoService;
import com.apigee.diagnosis.info.MPInfoService;
import com.apigee.diagnosis.util.RestAPIExecutor;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * Created by senthil on 18/07/16.
 */
@RestController
public class DeploymentService {
    private static final String USERNAME = "nagios@apigee.com";
    private static final String PASSWORD = "xnYbykQa7G";

    @RequestMapping(value = "/v1/diagnosis/organizations/{org}/environments/{env}/apis/{api}/revisions/{revision}/zkdeployments", produces = "application/json")
    public APIDeploymentState zkDeploymentService(@PathVariable String org,
                                                  @PathVariable String env,
                                                  @PathVariable String api,
                                                  @PathVariable String revision) throws IOException {
        APIDeploymentState apiDeploymentState = null;

        try {
            validateInputArguments(org, env, api, revision);
            ZKAPIDeployInfoService zkAPIDeployInfoService = new
                    ZKAPIDeployInfoService(org, env, api, revision);

            apiDeploymentState = zkAPIDeployInfoService.getCompleteDeploymentInfo();
            zkAPIDeployInfoService.close();

        } catch (IllegalArgumentException iae) {
            throw new ResourceNotFoundException(iae.getMessage());
        } catch (Exception e) {
            throw new ZKAPIDeployServiceException(e.getMessage());
        }
        return apiDeploymentState;
    }

    @RequestMapping(value = "/v1/diagnosis/organizations/{org}/environments/{env}/apis/{api}/revisions/{revision}/mpdeployments", produces = "application/json")
    public MPAPIDeploymentReport mpDeploymentService(@PathVariable String org,
                                                  @PathVariable String env,
                                                  @PathVariable String api,
                                                  @PathVariable String revision) throws IOException {
        MPAPIDeploymentReport mpAPIDeploymentReport = null;

        try {
            validateInputArguments(org, env, api, revision);
            MPAPIDeployInfoService mpAPIDeployInfoService = new
                    MPAPIDeployInfoService(org, env, api);

            mpAPIDeploymentReport = mpAPIDeployInfoService.getAPIDeploymentInfoOnMPs(revision);
            mpAPIDeployInfoService.close();

        } catch (IllegalArgumentException iae) {
            throw new ResourceNotFoundException(iae.getMessage());
        } catch (Exception e) {
            throw new ZKAPIDeployServiceException(e.getMessage());
        }
        return mpAPIDeploymentReport;
    }

    @RequestMapping(value = "/v1/diagnosis/organizations/{org}/environments/{env}/apis/{api}/mpdeployments", produces = "application/json")
    public MPAPIDeploymentReport mpDeploymentService(@PathVariable String org,
                                                  @PathVariable String env,
                                                  @PathVariable String api) throws IOException {
        MPAPIDeploymentReport mpAPIDeploymentReport = null;

        try {
            validateInputArguments(org, env, api, null);
            MPAPIDeployInfoService mpAPIDeployInfoService = new
                    MPAPIDeployInfoService(org, env, api);

            mpAPIDeploymentReport = mpAPIDeployInfoService.getAPIDeploymentInfoOnMPs();
            mpAPIDeployInfoService.close();

        } catch (IllegalArgumentException iae) {
            throw new ResourceNotFoundException(iae.getMessage());
        } catch (Exception e) {
            throw new ZKAPIDeployServiceException(e.getMessage());
        }
        return mpAPIDeploymentReport;
    }

    @RequestMapping(value = "/v1/diagnosis/organizations/{org}/environments/{env}/apis/{api}/revisions/{revision}/deployments", produces = "application/json")
    public DiagnosticReport deploymentService(@PathVariable String org,
                                                @PathVariable String env,
                                                @PathVariable String api,
                                                @PathVariable String revision) throws Exception {
        DiagnosticReport diagnosticReport = null;
        try {
            validateInputArguments(org, env, api, revision);
            DeploymentAPIService deploymentAPIService = new DeploymentAPIService(org,env,api,revision);

            diagnosticReport = deploymentAPIService.getDeploymentStatus(org,env,api,revision);

        } catch (IllegalArgumentException iae) {
            throw new ResourceNotFoundException(iae.getMessage());
        } catch (Exception e) {
            throw e;
        }
        return diagnosticReport;
    }

    @RequestMapping(value = "/v1/diagnosis/organizations/{org}/environments/{env}/apis/{api}/deployments", produces = "application/json")
    public DiagnosticReport deploymentService(@PathVariable String org,
                                                @PathVariable String env,
                                                @PathVariable String api) throws Exception {
        DiagnosticReport diagnosticReport = null;
        try {
            validateInputArguments(org, env, api, null);
            DeploymentAPIService deploymentAPIService = new DeploymentAPIService(org,env,api,null);

            diagnosticReport = deploymentAPIService.getDeploymentStatus(org,env,api,null);

        } catch (IllegalArgumentException iae) {
            throw new ResourceNotFoundException(iae.getMessage());
        } catch (Exception e) {
            throw e;
        }
        return diagnosticReport;
    }

    @RequestMapping(value = "/v1/diagnosis/organizations/{org}/environments/{env}/mpinformation", produces = "application/json")
    public MPInformationReport mpInformationService(@PathVariable String org,
                                              @PathVariable String env) throws Exception {
        MPInformationReport mpInformationReport = null;
        try {
            validateInputArguments(org, env, null, null);
            MPInfoService mpInfoService = new MPInfoService();

            mpInformationReport = mpInfoService.getMPInformation(org, env);

        } catch (IllegalArgumentException iae) {
            throw new ResourceNotFoundException(iae.getMessage());
        } catch (Exception e) {
            throw e;
        }
        return mpInformationReport;
    }

    /*
     * validates the input arguments by making the management API call
     */
    private void validateInputArguments(String org, String env, String api, String revision) throws Exception {
        StringBuilder apiURL = new StringBuilder();
        apiURL.append("https://api.enterprise.apigee.com/v1/organizations/" + org);

        String extraPath;
        String deploymentsAPIPath = "/deployments";
        // Use virtualhosts API to make faster call when we have only org and env parameters
        String virtualHostsAPIPath = "/virtualhosts";
        // Append the remaining arguments if they are available
        if (env != null) {
            extraPath = "/environments/" + env;
            if (api != null) {
                extraPath += "/apis/" + api;
                if (revision != null) {
                    extraPath += "/revisions/" + revision;
                }
                apiURL.append(extraPath);
                apiURL.append(deploymentsAPIPath);
            } else {
                apiURL.append(extraPath);
                apiURL.append(virtualHostsAPIPath);
            }
        }

        // Execute the management API
        String response = RestAPIExecutor.executeGETAPI(apiURL.toString(), USERNAME + ":" + PASSWORD, "json");
        String message = null;
        try {
            JSONObject responseJSON = new JSONObject(response);
            // If the response contains "message", then it indicates that the management API failed
            message = responseJSON.getString("message");
        } catch(JSONException je) {
            // you reach here either if the response is not JSON string
            // or if the response string does not contain the JSON object "message"
            // Either case, swallow the exception silently
        }
        if (message!=null) {
            throw new IllegalArgumentException(message);
        }
    }
}

@ControllerAdvice
class DeploymentControllerAdvice {

    @ResponseBody
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ErrorResponse resourceNotFoundExceptionHandler(ResourceNotFoundException ex) {
        return new ErrorResponse(ex.getClass().getCanonicalName(), ex.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(ZKAPIDeployServiceException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    ErrorResponse zkAPIDeployServiceExceptionHandler(ZKAPIDeployServiceException ex) {
        return new ErrorResponse(ex.getClass().getCanonicalName(), ex.getMessage());
    }
}

@ResponseStatus(HttpStatus.NOT_FOUND)
class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
class ZKAPIDeployServiceException extends RuntimeException {

    public ZKAPIDeployServiceException(String message) {
        super(message);
    }
}


/**
 * Defines the JSON output format of error responses
 */
class ErrorResponse {
    public String code;

    public String message;

    public ErrorResponse(String code, String message) {
        this.code = code;
        this.message = message;
    }
}


