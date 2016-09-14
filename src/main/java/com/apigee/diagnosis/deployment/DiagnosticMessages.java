package com.apigee.diagnosis.deployment;

/**
 * Created by amar on 12/09/16.
 */
public class DiagnosticMessages {

    // Diagnostic Report Messages
    public static final String DESCRIPTION_DEPLOYMENT_REPORT = "API Deployment Errors Diagnosis Report";

    // Problem Symptom Messages
    public static final String SYMPTOM_NO_PROBLEM = "None";
    public static final String SYMPTOM_DEPLOYMENT_MULTIPLE_REVISIONS =
            "Multiple revisions of API Proxy";

    // Problem Summary Messages
    public static final String PROBLEMSUMMARY_NO_PROBLEM = "No Problems Found";
    public static final String PROBLEMSUMMARY_STALE_ENTRY = "Stale entries found in Management server for revision";
    public static final String PROBLEMSUMMARY_DEPLOYMENT_STATUS_MISMATCH = "Deployment state mismatch";

    // Problem Resolution Messages
    public static final String RESOLUTION_UNDEPLOY_REVISION = "Use Management API and undeploy the revision";
    public static final String RESOLUTION_RESTART_MP = "Restart Message Processor Server";
    public static final String RESOLUTION_NO_ACTION_REQUIRED = "No Action Required";
}