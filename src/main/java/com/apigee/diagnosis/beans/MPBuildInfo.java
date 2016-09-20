package com.apigee.diagnosis.beans;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by amar on 19/09/16.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MPBuildInfo {
    private String buildNumber;
    private String buildTimestamp;
    private String rPMName;
    private String releaseID;
    private String sCMBranch;
    private String sCMRevision;

    public MPBuildInfo (String buildNumber, String buildTimestamp,
                        String rPMName, String releaseID,
                        String sCMBranch, String sCMRevision) {
        this.buildNumber = buildNumber;
        this.buildTimestamp = buildTimestamp;
        this.rPMName = rPMName;
        this.releaseID = releaseID;
        this.sCMBranch = sCMBranch;
        this.sCMRevision = sCMRevision;
    }

    public String getBuildNumber() {
        return buildNumber;
    }

    public void setBuildNumber(String buildNumber) {
        this.buildNumber = buildNumber;
    }

    public String getBuildTimestamp() {
        return buildTimestamp;
    }

    public void setBuildTimestamp(String buildTimestamp) {
        this.buildTimestamp = buildTimestamp;
    }

    public String getrPMName() {
        return rPMName;
    }

    public void setrPMName(String rPMName) {
        this.rPMName = rPMName;
    }

    public String getReleaseID() {
        return releaseID;
    }

    public void setReleaseID(String releaseID) {
        this.releaseID = releaseID;
    }

    public String getsCMBranch() {
        return sCMBranch;
    }

    public void setsCMBranch(String sCMBranch) {
        this.sCMBranch = sCMRevision;
    }

    public String getsCMRevision() {
        return sCMRevision;
    }

    public void setsCMRevision(String sCMRevision) {
        this.sCMRevision = sCMRevision;
    }

    @Override
    public String toString() {
        return "BuildInfo{" +
                "buildNumber='" + buildNumber + '\'' +
                ", buildTimestamp='" + buildTimestamp + '\'' +
                ", rPMName ='" + rPMName + '\'' +
                ", releaseID ='" + releaseID + '\'' +
                ", sCMBranch ='" + sCMBranch + '\'' +
                ", sCMRevision ='" + sCMRevision + '\'' +
                '}';
    }

}
