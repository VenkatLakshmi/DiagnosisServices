package com.apigee.diagnosis.beans;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by senthil on 07/09/16.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DiagnosticReport {
    private String description;
    private APIInformation apiInformation;
    private DiagnosticInformation diagnosticInformation;

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

    public DiagnosticInformation getDiagnosticInformation() {
        return diagnosticInformation;
    }

    public void setDiagnosticInformation(DiagnosticInformation diagnosticInformation) {
        this.diagnosticInformation = diagnosticInformation;
    }

    public DiagnosticReport(String description, APIInformation apiInformation,
                            DiagnosticInformation diagnosticInformation) {
        this.description = description;
        this.apiInformation = apiInformation;
        this.diagnosticInformation = diagnosticInformation;
    }

    @Override
    public String toString() {
        return "DiagnosticReport{" +
                "description='" + description + '\'' +
                ", apiInformation='" + apiInformation + '\'' +
                ", diagnosticInformation='" + diagnosticInformation + '\'' +
                '}';
    }
}