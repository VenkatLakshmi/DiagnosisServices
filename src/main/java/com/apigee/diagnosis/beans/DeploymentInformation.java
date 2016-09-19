package com.apigee.diagnosis.beans;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by amar on 18/09/16.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeploymentInformation {
    private Revision[] revision;

    public DeploymentInformation(Revision[] revision) {
        this.revision = revision;
    }

    public Revision[] getRevision() {
        return revision;
    }

    public void setRevision(Revision[] revision) {
        this.revision = revision;
    }

    @Override
    public String toString() {
        return "DeploymentInformation{" +
                "revision='" + revision + '\'' +
                '}';
    }
}

