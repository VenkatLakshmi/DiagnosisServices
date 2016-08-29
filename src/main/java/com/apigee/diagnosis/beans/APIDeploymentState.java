package com.apigee.diagnosis.beans;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by senthil on 18/07/16.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class APIDeploymentState {


    private String org;
    private String env;
    private String api;
    private Revision[] revision;
    private String state;
    private String spec;


    public APIDeploymentState() {

    }

    public APIDeploymentState(String org, String env, String api, Revision[] revision, String state) {
        this.org = org;
        this.env = env;
        this.api = api;
        this.revision = revision;
        this.state = state;
    }

    public String getOrg() {
        return org;
    }

    public void setOrg(String org) {
        this.org = org;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getApi() {
        return api;
    }

    public void setApi(String api) {
        this.api = api;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getSpec() {
        return spec;
    }

    public void setSpec(String spec) {
        this.spec = spec;
    }

    public Revision[] getRevision() {
        return revision;
    }

    public void setRevision(Revision[] revision) {
        this.revision = revision;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append("organization="+org);
        sb.append(", environment="+env);
        sb.append(", apiproxy="+api);
        sb.append(", spec="+spec);
        sb.append(", state="+state);
        sb.append("]");
        return sb.toString();
    }
}
