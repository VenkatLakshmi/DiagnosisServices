package com.apigee.diagnosis.beans;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by amar on 11/09/16.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Resolution {
    private String problemSummary;
    private String recommendedAction;

    public Resolution(String problemSummary, String recommendedAction) {
        this.problemSummary = problemSummary;
        this.recommendedAction = recommendedAction;
    }

    public String getProblemSummary() {
        return problemSummary;
    }

    public void setProblemSummary(String problemSummary) {
        this.problemSummary = problemSummary;
    }

    public String getRecommendedAction() {
        return recommendedAction;
    }

    public void setRecommendation(String recommendation) {
        this.recommendedAction = recommendedAction;
    }

    @Override
    public String toString() {
        return "DiagnosticInfo{" +
                "problemSummary='" + problemSummary + '\'' +
                ", recommendedAction='" + recommendedAction + '\'' +
                '}';
    }
}
