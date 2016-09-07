package com.apigee.diagnosis.beans;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by senthil on 18/07/16.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Server {

    private String hostname;
    private String uuid;
    private String status;
    private String errorcode;
    private String errormessage;

    public Server() {

    }

    public Server(String hostname, String uuid, String status, String errorcode, String errormessage) {
        this.hostname = hostname;
        this.uuid = uuid;
        this.status = status;
        this.errorcode = errorcode;
        this.errormessage = errormessage;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorcode() {
        return errorcode;
    }

    public void setError(String errorcode) {
        this.errorcode = errorcode;
    }

    public String getErrormessage() {
        return errormessage;
    }

    public void setErrormessage(String errormessage) {
        this.errormessage = errormessage;
    }

    @Override
    public String toString() {
        return "Server [hostname = " + hostname + " uuid=" + uuid + " status=" + status + " errorcode=" + errorcode + " errormessage=" + errormessage + "]";
    }

}