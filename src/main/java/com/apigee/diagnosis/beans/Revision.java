package com.apigee.diagnosis.beans;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by senthil on 25/08/16.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Revision {
    private String name;
    private Server[] servers;
    public Revision(String name, Server[] servers) {
        this.name = name;
        this.servers = servers;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Server[] getServers() {
        return servers;
    }

    public void setServers(Server[] servers) {
        this.servers = servers;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append("Revision = "+name);
        for(Server server : servers) {
            sb.append(", server="+server.toString());
        }
        sb.append("]");
        return sb.toString();
    }

}
