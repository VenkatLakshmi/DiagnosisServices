package com.apigee.diagnosis.beans;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by senthil on 07/09/16.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Report {
    private String issue;
    private String recommendation;

    public Report(String issue, String recommendation) {
        this.issue = issue;
        this.recommendation = recommendation;
    }

    public String getIssue() {
        return issue;
    }

    public void setIssue(String issue) {
        this.issue = issue;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    @Override
    public String toString() {
        return "Report{" +
                "issue='" + issue + '\'' +
                ", recommendation='" + recommendation + '\'' +
                '}';
    }
}
