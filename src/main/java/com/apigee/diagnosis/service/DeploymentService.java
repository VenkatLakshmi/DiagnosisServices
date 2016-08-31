package com.apigee.diagnosis.service;

import com.apigee.diagnosis.beans.APIDeploymentState;
import com.apigee.diagnosis.deployment.DeploymentAPIService;
import com.apigee.diagnosis.deployment.ZKAPIDeployInfoService;
import com.apigee.diagnosis.deployment.MPAPIDeployInfoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


import java.io.IOException;

/**
 * Created by senthil on 18/07/16.
 */
@RestController
public class DeploymentService {
    @RequestMapping(value = "/v1/diagnosis/zkdeploymentinfo/organizations/{org}/environments/{env}/apis/{api}/revisions/{revision}/status", produces = "application/json")
    public APIDeploymentState zkDeploymentService(@PathVariable String org,
                                                  @PathVariable String env,
                                                  @PathVariable String api,
                                                  @PathVariable String revision) throws IOException {
        APIDeploymentState apiDeploymentState = null;

        try {
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

    @RequestMapping(value = "/v1/diagnosis/mpdeploymentinfo/organizations/{org}/environments/{env}/apis/{api}/revisions/{revision}/status", produces = "application/json")
    public APIDeploymentState mpDeploymentService(@PathVariable String org,
                                                  @PathVariable String env,
                                                  @PathVariable String api,
                                                  @PathVariable String revision) throws IOException {
        APIDeploymentState apiDeploymentState = null;

        try {

            MPAPIDeployInfoService mpAPIDeployInfoService = new
                    MPAPIDeployInfoService(org, env, api, revision);

            apiDeploymentState = mpAPIDeployInfoService.getCompleteDeploymentInfoOnAllMPs();
            mpAPIDeployInfoService.close();

        } catch (IllegalArgumentException iae) {
            throw new ResourceNotFoundException(iae.getMessage());
        } catch (Exception e) {
            throw new ZKAPIDeployServiceException(e.getMessage());
        }
        return apiDeploymentState;
    }

    @RequestMapping(value = "/v1/diagnosis/deploymentinfo/organizations/{org}/environments/{env}/apis/{api}/revisions/{revision}/status", produces = "application/json")
    public APIDeploymentState deploymentService(@PathVariable String org,
                                                @PathVariable String env,
                                                @PathVariable String api,
                                                @PathVariable String revision) throws IOException {
        APIDeploymentState apiDeploymentState = null;
        try {
            DeploymentAPIService deploymentAPIService = new DeploymentAPIService(org,env,api,revision);

            apiDeploymentState = deploymentAPIService.getDeploymentStatus(org,env,api,revision);

        } catch (IllegalArgumentException iae) {
            throw new ResourceNotFoundException(iae.getMessage());
        } catch (Exception e) {
            throw new ZKAPIDeployServiceException(e.getMessage());
        }
        return apiDeploymentState;
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


