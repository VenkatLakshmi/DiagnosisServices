package com.apigee.diagnosis.beans;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by amar on 11/09/16.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class APIInformation {
    private String org;
    private String env;
    private String api;
    private String revision;

    public String getOrg(){
        return org;
    }

    public String getEnv(){
        return env;
    }

    public String getApi(){
        return api;
    }

    public String getRevision(){
        return revision;
    }

    public APIInformation(String org, String env, String api) {
        this.org = org;
        this.env = env;
        this.api = api;
    }

    public APIInformation(String org, String env, String api, String revision) {
        this.org = org;
        this.env = env;
        this.api = api;
        this.revision = revision;
    }

    @Override
    public String toString() {
        return "APIInformation{" +
                "org='" + org + '\'' +
                ", env='" + env + '\'' +
                ", api='" + api + '\'' +
                ", revision='" + revision + '\'' +
                '}';
    }

}
