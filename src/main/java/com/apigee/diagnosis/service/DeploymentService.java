package com.apigee.diagnosis.service;

import com.apigee.diagnosis.beans.APIDeploymentState;
import com.apigee.diagnosis.deployment.ZKAPIDeployInfoService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
            ZKAPIDeployInfoService zkapiDeployInfoService = new
                    ZKAPIDeployInfoService(org, env, api, revision);

            apiDeploymentState = zkapiDeployInfoService.getCompleteDeploymentInfo();
            zkapiDeployInfoService.close();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return apiDeploymentState;

    }
}