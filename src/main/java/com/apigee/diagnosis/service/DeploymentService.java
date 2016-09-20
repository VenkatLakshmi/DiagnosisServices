package com.apigee.diagnosis.service;

import com.apigee.diagnosis.beans.APIDeploymentState;
import com.apigee.diagnosis.beans.DiagnosticReport;
import com.apigee.diagnosis.beans.MPAPIDeploymentReport;
import com.apigee.diagnosis.beans.MPInformationReport;
import com.apigee.diagnosis.deployment.DeploymentAPIService;
import com.apigee.diagnosis.deployment.ZKAPIDeployInfoService;
import com.apigee.diagnosis.deployment.MPAPIDeployInfoService;
import com.apigee.diagnosis.info.MPInfoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


import java.io.IOException;

/**
 * Created by senthil on 18/07/16.
 */
@RestController
public class DeploymentService {
    @RequestMapping(value = "/v1/diagnosis/organizations/{org}/environments/{env}/apis/{api}/revisions/{revision}/zkdeployments", produces = "application/json")
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

    @RequestMapping(value = "/v1/diagnosis/organizations/{org}/environments/{env}/apis/{api}/revisions/{revision}/mpdeployments", produces = "application/json")
    public MPAPIDeploymentReport mpDeploymentService(@PathVariable String org,
                                                  @PathVariable String env,
                                                  @PathVariable String api,
                                                  @PathVariable String revision) throws IOException {
        MPAPIDeploymentReport mpAPIDeploymentReport = null;

        try {

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
            MPInfoService mpInfoService = new MPInfoService();

            mpInformationReport = mpInfoService.getMPInformation(org, env);

        } catch (IllegalArgumentException iae) {
            throw new ResourceNotFoundException(iae.getMessage());
        } catch (Exception e) {
            throw e;
        }
        return mpInformationReport;
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


