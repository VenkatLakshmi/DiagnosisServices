package com.apigee.diagnosis.beans;

import com.fasterxml.jackson.annotation.JsonInclude;
import jdk.nashorn.internal.runtime.events.RecompilationEvent;

/**
 * Created by amar on 11/09/16.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DiagnosticInformation {
    private String symptom;
    private String cause;
    private Details details;
    private Resolution[] resolution;

    public String getSymptom() {
        return symptom;
    }

    public void setSymptom(String symptom) {
        this.symptom = symptom;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    public Details getDetails() {
        return details;
    }

    public void setDetails(Details details) {
        this.details = details;
    }

    public Resolution[] getResolution() {
        return resolution;
    }

    public void setResolution(Resolution[] resolution) {
        this.resolution = resolution;
    }

    public DiagnosticInformation(String symptom, String cause,
                                 Details details,
                                 Resolution[] resolution) {
        this.symptom = symptom;
        this.cause = cause;
        this.details = details;
        this.resolution = resolution;
    }

    @Override
    public String toString() {
        return "DiagnosticResults{" +
                "symptom='" + symptom + '\'' +
                ", cause='" + cause + '\'' +
                ", details='" + details + '\'' +
                ", resolution='" + resolution + '\'' +
                '}';
    }

}
