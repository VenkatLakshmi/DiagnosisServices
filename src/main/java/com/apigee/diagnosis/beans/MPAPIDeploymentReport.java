package com.apigee.diagnosis.beans;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by amar on 16/09/16.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MPAPIDeploymentReport {
    private String description;
    private APIInformation apiInformation;
    private DeploymentInformation deploymentInformation;

    public MPAPIDeploymentReport(String description, APIInformation apiInformation,
                              DeploymentInformation deploymentInformation) {
        this.description = description;
        this.apiInformation = apiInformation;
        this.deploymentInformation = deploymentInformation;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public APIInformation getApiInformation() {
        return apiInformation;
    }

    public void setApiInformation(APIInformation apiInformation) {
        this.apiInformation = apiInformation;
    }

    public DeploymentInformation getDeploymentInformation() {
        return deploymentInformation;
    }

    public void setDeploymentInformation(DeploymentInformation deploymentInformation) {
        this.deploymentInformation = deploymentInformation;
    }


    @Override
    public String toString() {
        return "DiagnosticReport{" +
                "description='" + description + '\'' +
                ", apiInformation='" + apiInformation + '\'' +
                ", deploymentInformation ='" + deploymentInformation + '\'' +
                '}';
    }
}
