package com.apigee.diagnosis.deployment;

import com.apigee.diagnosis.beans.*;
import com.apigee.diagnosis.util.JSONResponseParser;
import com.apigee.diagnosis.util.RestAPIExecutor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by senthil on 21/08/16.
 */
public class DeploymentAPIService {

    private String org;
    private String env;
    private String api;
    private String revision;

    //TODO: Make it as a system property.
    private static final String MGMT = "api.enterprise.apigee.com";
    private static final String LOCAL_URL = "localhost:8080";

    //TODO: Create an read-only sysadmin for apigee public cloud.
    private static final String USERNAME = "nagios@apigee.com";
    private static final String PASSWORD = "xnYbykQa7G";

    //Report Analysis
    private List<String> causeList = new ArrayList<String>();
    private List<Resolution> resolutionList = new ArrayList<Resolution>();

    private static Logger logger = LoggerFactory.getLogger(DeploymentAPIService.class);

    public String getOrg() {
        return org;
    }

    public String getEnv() {
        return env;
    }

    public String getApi() {
        return api;
    }

    public String getRevision() {
        return revision;
    }

    public DeploymentAPIService(String org, String env, String api, String revision) {
        this.org = org;
        this.env = env;
        this.api = api;
        this.revision = revision;
        validate();
    }

    private void validate() {
        //TODO: Validate Org, Env, API and Revision.
    }

    public DiagnosticReport getDeploymentStatus(String org, String env, String api, String revision) throws JSONException {
        List<Revision> revisions = new ArrayList<Revision>();
        if(revision == null) {
            String mgmtStatus = getMgmtDeploymentStatusAPI(org, env, api, revision);
            List<String> revNos = JSONResponseParser.getRevisions(mgmtStatus);
            for(String revNo : revNos)
                revisions.add(getDeploymentStatusForRevision(org, env, api, revNo));
        } else {
            revisions.add(getDeploymentStatusForRevision(org, env, api, revision));
        }

        // prepare the diagnosticResults
        String symptom = null;
        if (revisions.size() > 1 ) {
            symptom = DiagnosticMessages.SYMPTOM_DEPLOYMENT_MULTIPLE_REVISIONS +
                    " " + getApi() + " are deployed";
        }
        String cause = null;
        Resolution[] allResolutions = null;
        if (!causeList.isEmpty()) {
            // check if there is a single issue or multiple issues
            List<String> uniqueCauseList = new ArrayList<String>(new HashSet<String>( causeList ));

            // check if there is a single issue or multiple issues
            if (uniqueCauseList.size() == 1) {
                cause = uniqueCauseList.get(0);
                allResolutions = new Resolution[1];
                allResolutions[0] = resolutionList.get(0);
            } else {
                cause = uniqueCauseList.toString();
                allResolutions = resolutionList.toArray(new Resolution[resolutionList.size()]);
            }
        } else {
            // If no problems found
            symptom = DiagnosticMessages.SYMPTOM_NO_PROBLEM;
            cause = DiagnosticMessages.SYMPTOM_NO_PROBLEM;
            allResolutions = new Resolution[1];
            allResolutions[0] = new Resolution(DiagnosticMessages.PROBLEMSUMMARY_NO_PROBLEM,
                    DiagnosticMessages.RESOLUTION_NO_ACTION_REQUIRED);
        }

        Revision[] allRevisions = revisions.toArray(new Revision[revisions.size()]);
        Details details = new Details(allRevisions);
        DiagnosticInformation diagnosticInformation = new DiagnosticInformation(symptom, cause,
                details, allResolutions);

        // prepare the diagnosticReport
        APIInformation apiInformation = new APIInformation(getOrg(), getEnv(), getApi());
        DiagnosticReport diagnosticReport = new DiagnosticReport(DiagnosticMessages.DESCRIPTION_DEPLOYMENT_REPORT,
                apiInformation, diagnosticInformation);

        return diagnosticReport;

    }

    private Revision getDeploymentStatusForRevision(String org, String env, String api, String revision) {
        List<Server> servers = new ArrayList<Server>();
        String mgmtResponse = getMgmtDeploymentStatusAPI(org, env, api, revision);

        //Fetch MP API Response
        String mpResponse = getMPDeploymentStatusAPI(org, env, api,revision);

        logger.info("MGMT Response : " + mgmtResponse);

        // Check Mgmt API Deployment Status
        String mgmtStatus = checkMgmtStatus(mgmtResponse);

        if (mgmtStatus.equalsIgnoreCase(States.ERROR)) {
            servers = setServerStatusForErrorState(mgmtResponse, mpResponse, revision );
            Server allMPServers[] = servers.toArray(new Server[servers.size()]);
            Revision revisionBean = new Revision(revision, allMPServers);
            return revisionBean;
        } else {
            servers = setServerStatusForSuccessState(mgmtStatus, mpResponse, revision);
            Server allMPServers[] = servers.toArray(new Server[servers.size()]);
            Revision revisionBean = new Revision(revision, allMPServers);
            return revisionBean;
        }
    }

    private List<Server> setServerStatusForErrorState(String mgmtResponse, String mpResponse, String revision) {
        List<Server> servers = new ArrayList<Server>();
        try {
            JSONObject mgmtObj = new JSONObject(mgmtResponse);
            JSONArray serverObj1 = mgmtObj.getJSONArray("server");
            JSONObject mpObj = new JSONObject(mpResponse);
            JSONObject revObj = (JSONObject) mpObj.getJSONArray("revision").get(0);
            JSONArray serverObj2 = revObj.getJSONArray("servers");
            for (String mpServer : JSONResponseParser.getMessageProcessorList(serverObj2)) {
                servers.add(processServerStatusErrorState(mpServer, serverObj1, serverObj2, revision));
            }
        } catch (JSONException e) {
            logger.error("Invalid JSON response");
            e.printStackTrace();
        }
        return servers;
    }

    private List<Server> setServerStatusForSuccessState(String mgmtStatus, String mpResponse, String revision) {
        List<Server> servers = new ArrayList<Server>();
        try {
            JSONObject mpObj = new JSONObject(mpResponse);
            JSONObject deploymentObj = mpObj.getJSONObject("deploymentInformation");
            JSONObject revObj = (JSONObject) deploymentObj.getJSONArray("revision").get(0);
            JSONArray serverObj2 = revObj.getJSONArray("servers");
            for (String mpServer : JSONResponseParser.getMessageProcessorList(serverObj2)) {
                servers.add(processServerStatusSuccessState(mpServer, mgmtStatus, serverObj2, revision));
            }
        } catch (JSONException e) {
            logger.error("Invalid JSON response");
            e.printStackTrace();
        }
        return servers;
    }

    private Server processServerStatusErrorState(String mpServer, JSONArray serverObj1, JSONArray serverObj2, String revision) throws JSONException {
        String status = new String();
        for (int i = 0; i < serverObj1.length(); i++) {
            if (serverObj1.getJSONObject(i).getString("uUID").equalsIgnoreCase(mpServer)) {
                int j = 0;
                while (j < serverObj2.length()) {
                    if (serverObj2.getJSONObject(j).getString("uuid").equalsIgnoreCase(mpServer)) {
                        String mpStatus = serverObj2.getJSONObject(j).getString("status");
                        String mgmtStatus = serverObj1.getJSONObject(i).getString("status");
                        status = mgmtStatus.equalsIgnoreCase(mpStatus) ? States.SYNC : States.MISMATCH;
                        String errorMessage = null;
                        if (status == States.MISMATCH) {
                            errorMessage = "Mgmt State:" + mgmtStatus + " MP States:" + mpStatus;
                            if(serverObj1.getJSONObject(i).has("errorcode") && serverObj1.getJSONObject(i).has("errormessage")) {
                                resolutionList.add(createReport(serverObj1.getJSONObject(i).getString("errorcode"),
                                        serverObj1.getJSONObject(i).getString("errormessage")));
                            }
                        }
                        return new Server(serverObj2.getJSONObject(j).getString("hostname"),mpServer, status, null, errorMessage);
                    }
                    j++;
                }
            }
        }
        return null;
    }

    private Resolution createReport(String errorCode, String errorMessage) {
        //TODO : Recommend engine
        return new Resolution(errorCode,errorMessage);
    }


    private Server processServerStatusSuccessState(String mpServer, String mgmtStatus, JSONArray serverObj2, String revision) throws JSONException {
        String status = new String();
        int j = 0;
        while (j < serverObj2.length()) {
            if (serverObj2.getJSONObject(j).getString("uuid").equalsIgnoreCase(mpServer)) {
                String mpStatus = serverObj2.getJSONObject(j).getString("status");
                status = mgmtStatus.equalsIgnoreCase(mpStatus) ? States.SYNC : States.MISMATCH;
                String errorMessage = null;
                if (status == States.MISMATCH) {
                    errorMessage = "Deployment state on Management Server: " + mgmtStatus + " and on Message Processor: " + mpStatus;
                }
                compareDeploymentStatesAndPrepareResolution(mgmtStatus, mpStatus, revision);
                return new Server(serverObj2.getJSONObject(j).getString("hostname"), mpServer, status, null, errorMessage);
            }
            j++;
        }
        return null;
    }

    private void compareDeploymentStatesAndPrepareResolution(String mgmtStatus,
                                                               String mpStatus,
                                                               String revision) {

        logger.info("compareDeploymentStatesAndPrepareResolution - mgmtStatus = " + mgmtStatus + " mpStatus = " + mpStatus);
        String cause = null;
        String recommendedAction = null;
        switch(mgmtStatus) {
            case States.DEPLOYED:
                if (mpStatus.equalsIgnoreCase(States.UNDEPLOYED)) {
                    cause = DiagnosticMessages.PROBLEMSUMMARY_DEPLOYMENT_STATUS_MISMATCH + " " + revision;
                    recommendedAction = DiagnosticMessages.RESOLUTION_UNDEPLOY_REVISION + " " + revision;
                    causeList.add(cause);
                    resolutionList.add(new Resolution(cause, recommendedAction));
                }
                break;
            case States.UNDEPLOYED:
                if (mpStatus.equalsIgnoreCase(States.DEPLOYED)) {
                    cause = DiagnosticMessages.PROBLEMSUMMARY_DEPLOYMENT_STATUS_MISMATCH + " " + revision;
                    recommendedAction = DiagnosticMessages.RESOLUTION_UNDEPLOY_REVISION + " " + revision;
                    causeList.add(cause);
                    resolutionList.add(new Resolution(cause, recommendedAction));
                }
                break;
            case States.MISSING:
                if (mpStatus.equalsIgnoreCase(States.UNDEPLOYED)) {
                    cause = DiagnosticMessages.PROBLEMSUMMARY_STALE_ENTRY + " " + revision;
                    recommendedAction = DiagnosticMessages.RESOLUTION_UNDEPLOY_REVISION + " " + revision;
                    causeList.add(cause);
                    resolutionList.add(new Resolution(cause, recommendedAction));
                }
                break;
            default:
                break;
        }

    }

    private String checkMgmtStatus(String jsonResponse) {
        String status = new String();
        try {
            JSONObject mgmtObject = new JSONObject(jsonResponse);
            status = mgmtObject.getString("state");
        } catch (JSONException e) {
            status = States.UNDEPLOYED;
        }
        return status;
    }

    private String getMgmtDeploymentStatusAPI(String org, String env, String api, String revision) {
        String mgmtAPIDeployURL = new String();
        String apiDeploymentStatus = new String();
        if (revision == null) {
            mgmtAPIDeployURL = "https://" + MGMT + "/v1/o/" + org + "/e/" + env + "/apis/" + api + "/deployments";
        } else {
            mgmtAPIDeployURL = "https://" + MGMT + "/v1/o/" + org + "/e/" + env + "/apis/" + api + "/revisions/" + revision + "/deployments";
        }
        try {
            logger.info("MGMT DEPLOYMENT API CALL: " + mgmtAPIDeployURL);
            apiDeploymentStatus = RestAPIExecutor.executeGETAPI(mgmtAPIDeployURL, USERNAME + ":" + PASSWORD, "json");
        } catch (Exception e) {
            apiDeploymentStatus = "{ }";
        }
        return apiDeploymentStatus;
    }

    private String getMPDeploymentStatusAPI(String org, String env, String api, String revision) {
        String mpAPIDeployURL = new String();
        String apiDeploymentStatus = new String();
        if (revision == null) {
            mpAPIDeployURL = "http://" + LOCAL_URL + "/v1/diagnosis/organizations/" + org + "/environments/" + env + "/apis/" + api + "/mpdeployments";
        } else {
            mpAPIDeployURL = "http://" + LOCAL_URL + "/v1/diagnosis/organizations/" + org + "/environments/" + env + "/apis/" + api + "/revisions/" + revision + "/mpdeployments";
        }
        try {
            logger.info(mpAPIDeployURL);
            apiDeploymentStatus = RestAPIExecutor.executeGETAPI(mpAPIDeployURL, USERNAME + ":" + PASSWORD, "json");
        } catch (Exception e) {
            apiDeploymentStatus = "{ }";
        }
        return apiDeploymentStatus;
    }

}