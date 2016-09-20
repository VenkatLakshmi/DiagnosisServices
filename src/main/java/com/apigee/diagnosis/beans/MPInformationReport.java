package com.apigee.diagnosis.beans;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by amar on 16/09/16.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MPInformationReport {
    String description;
    String org;
    String env;
    MPServer [] servers;

    public MPInformationReport(String description, String org,
                               String env, MPServer[] servers) {
        this.description = description;
        this.org = org;
        this.env = env;
        this.servers = servers;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public MPServer[] getServers() {
        return servers;
    }

    public void setServers(MPServer[] servers) {
        this.servers = servers;
    }

    @Override
    public String toString() {
        return "MPServer{" +
                "description='" + description + '\'' +
                "org='" + org + '\'' +
                ", env='" + env + '\'' +
                ", servers='" + servers + '\'' +
                '}';
    }
}
