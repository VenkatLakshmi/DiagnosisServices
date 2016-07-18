package com.apigee.diagnosis.beans;

/**
 * Created by senthil on 18/07/16.
 */
public class Server {

    private String uuid;
    private String status;
    private String error;

    public Server() {

    }

    public Server(String uuid, String status, String error) {
        this.uuid = uuid;
        this.status = status;
        this.error = error;
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

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "Server [uuid=" + uuid + " status=" + status + " error=" + error + "]";
    }

}
