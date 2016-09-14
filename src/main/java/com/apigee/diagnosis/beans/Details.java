package com.apigee.diagnosis.beans;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by amar on 12/09/16.
 */

/*
 * This class contains details about the diagnosis performed
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Details {
    private Revision[] revision;

    public Details(Revision[] revision) {
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
        return "Details{" +
                "revision='" + revision + "'}";
    }
}
