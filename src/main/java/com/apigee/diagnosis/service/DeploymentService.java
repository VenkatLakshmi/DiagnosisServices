package com.apigee.diagnosis.service;

import com.apigee.diagnosis.beans.APIDeploymentState;
import com.apigee.diagnosis.deployment.ZKAPIDeployInfoService;
import com.apigee.diagnosis.util.JSONConverter;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * Created by senthil on 18/07/16.
 */
@RestController
public class DeploymentService {
    @RequestMapping("/v1/api-diagnosis/zookeeper/organizations/{org}/environments/{env}/apis/{api}/revisions/{revision}/status")
    public String zkDeploymentService(@PathVariable String org,
                                      @PathVariable String env,
                                      @PathVariable String api,
                                      @PathVariable String revision) throws IOException {

        String message = new String();
        try {
            ZKAPIDeployInfoService zkapiDeployInfoService = new
                    ZKAPIDeployInfoService(org, env, api, revision);

            APIDeploymentState apiDeploymentState = zkapiDeployInfoService.getCompleteDeploymentInfo();
            zkapiDeployInfoService.close();
            message = JSONConverter.ObjectToJSON(apiDeploymentState);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return message;
    }
}
