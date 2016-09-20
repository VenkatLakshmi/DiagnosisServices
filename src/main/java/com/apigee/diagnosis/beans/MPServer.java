package com.apigee.diagnosis.beans;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by amar on 16/09/16.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MPServer {
    String externalHostName;
    String externalIP;
    String internalHostName;
    String internalIP;
    String uUID;
    String region;
    String pod;
    String isUp;
    String up_time;
    MPBuildInfo buildInfo;

    public MPServer(String externalHostName, String externalIP,
                    String internalHostName, String internalIP, String uUID,
                    String region, String pod,
                    String isUp, String up_time,
                    MPBuildInfo buildInfo) {
        this.externalHostName = externalHostName;
        this.externalIP = externalIP;
        this.internalHostName = internalHostName;
        this.internalIP = internalIP;
        this.uUID = uUID;
        this.region = region;
        this.pod = pod;
        this.isUp = isUp;
        this.up_time = up_time;
        this.buildInfo = buildInfo;
    }

    public String getExternalHostName() {
        return externalHostName;
    }

    public void setExternalHostName(String externalHostName) {
        this.externalHostName = externalHostName;
    }

    public String getExternalIP() {
        return externalIP;
    }

    public void setExternalIP(String externalIP) {
        this.externalIP = externalIP;
    }

    public String getInternalHostName() {
        return internalHostName;
    }

    public void setInternalHostName(String internalHostName) {
        this.internalHostName = internalHostName;
    }

    public String getInternalIP() {
        return internalIP;
    }

    public void setInternalIP(String internalIP) {
        this.internalIP = internalIP;
    }

    public String getuUID() {
        return uUID;
    }

    public void setuUID(String uUID) {
        this.uUID = uUID;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String uUID) {
        this.region = region;
    }

    public String getPod() {
        return pod;
    }

    public void setPod(String pod) {
        this.pod = pod;
    }

    public String getIsUp() {
        return isUp;
    }

    public void setIsUp(String isUp) {
        this.isUp = isUp;
    }

    public String getUp_time() {
        return up_time;
    }

    public void setUp_time(String up_time) {
        this.up_time = up_time;
    }

    public MPBuildInfo getBuildInfo() {
        return buildInfo;
    }

    public void setBuildInfo(MPBuildInfo buildInfo) {
        this.buildInfo = buildInfo;
    }

    @Override
    public String toString() {
        return "MPServer{" +
                "externalHostName='" + externalHostName + '\'' +
                ", externalIP='" + externalIP + '\'' +
                ", internalHostName='" + internalHostName + '\'' +
                ", internalIP='" + internalIP + '\'' +
                ", uUID='" + uUID + '\'' +
                ", region='" + region + '\'' +
                ", pod='" + pod + '\'' +
                ", isUp='" + isUp + '\'' +
                ", up_time='" + up_time + '\'' +
                ", buildInfo='" + buildInfo + '\'' +
                '}';
    }

}
