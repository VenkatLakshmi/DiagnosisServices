package com.apigee.diagnosis.deployment;

import com.apigee.diagnosis.beans.APIDeploymentState;
import com.apigee.diagnosis.beans.Revision;
import com.apigee.diagnosis.beans.Server;
import com.apigee.diagnosis.util.JSONResponseParser;
import com.apigee.diagnosis.util.RestAPIExecutor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
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

    private static final String MISMATCH = "Mismatch";
    private static final String SYNC = "Sync";
    private static final String DEPLOYED = "deployed";
    private static final String UNDEPLOYED = "undeployed";
    private static final String ERROR = "error";
    private static final String MISSING = "missing";

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
    }

    public APIDeploymentState getDeploymentStatus(String org, String env, String api, String revision) {
        List<Server> servers = new ArrayList<Server>();
        String mgmtResponse = getMgmtDeploymentStatusAPI(org, env, api, revision);

        //Fetch MP API Response
        String mpResponse = getMPDeploymentStatusAPI(org,env,api,revision);

        //Check Mgmt API Deployment Status
        String mgmtStatus = checkMgmtStatus(mgmtResponse);

        if (mgmtStatus.equalsIgnoreCase(ERROR) || mgmtStatus.equalsIgnoreCase(MISSING)) {
            servers = setServerStatusForErrorState(mgmtResponse, mpResponse);
            Server allMPServers[] = servers.toArray(new Server[servers.size()]);
            Revision revisionBean = new Revision(revision, allMPServers);
            return new APIDeploymentState(getOrg(), getEnv(), getApi(), new Revision[]{revisionBean}, null);
        } else {
            servers = setServerStatusForSuccessState(mgmtStatus, mpResponse);
            Server allMPServers[] = servers.toArray(new Server[servers.size()]);
            Revision revisionBean = new Revision(revision, allMPServers);
            return new APIDeploymentState(getOrg(), getEnv(), getApi(), new Revision[]{revisionBean}, null);
        }
    }

    private List<Server> setServerStatusForErrorState(String mgmtResponse, String mpResponse) {
        List<Server> servers = new ArrayList<Server>();
        try {
            JSONObject mgmtObj = new JSONObject(mgmtResponse);
            JSONArray serverObj1 = mgmtObj.getJSONArray("server");
            JSONObject mpObj = new JSONObject(mpResponse);
            JSONObject revObj = (JSONObject) mpObj.getJSONArray("revision").get(0);
            JSONArray serverObj2 = revObj.getJSONArray("servers");
            for (String mpServer : JSONResponseParser.getMessageProcessorList(serverObj2)) {
                servers.add(processServerStatusErrorState(mpServer, serverObj1, serverObj2));
            }
        } catch (JSONException e) {
            logger.error("Invalid JSON response");
            e.printStackTrace();
        }
        return servers;
    }

    private List<Server> setServerStatusForSuccessState(String mgmtStatus, String mpResponse) {
        List<Server> servers = new ArrayList<Server>();
        try {
            JSONObject mpObj = new JSONObject(mpResponse);
            JSONObject revObj = (JSONObject) mpObj.getJSONArray("revision").get(0);
            JSONArray serverObj2 = revObj.getJSONArray("servers");
            for (String mpServer : JSONResponseParser.getMessageProcessorList(serverObj2)) {
                servers.add(processServerStatusSuccessState(mpServer, mgmtStatus, serverObj2));
            }
        } catch (JSONException e) {
            logger.error("Invalid JSON response");
            e.printStackTrace();
        }
        return servers;
    }

    private Server processServerStatusErrorState(String mpServer, JSONArray serverObj1, JSONArray serverObj2) throws JSONException {
        String status = new String();
        for (int i = 0; i < serverObj1.length(); i++) {
            if (serverObj1.getJSONObject(i).getString("uUID").equalsIgnoreCase(mpServer)) {
                int j = 0;
                while (j < serverObj2.length()) {
                    if (serverObj2.getJSONObject(j).getString("uuid").equalsIgnoreCase(mpServer)) {
                        String mpStatus = serverObj2.getJSONObject(j).getString("status").equalsIgnoreCase("success") ? DEPLOYED : UNDEPLOYED;
                        String mgmtStatus = serverObj1.getJSONObject(i).getString("status");
                        status = mgmtStatus.equalsIgnoreCase(mpStatus) ? SYNC : MISMATCH;
                        String errorMessage = null;
                        if (status == MISMATCH) {
                            errorMessage = "Ideal State:" + mgmtStatus + " MP State:" + mpStatus;
                        }
                        return new Server(mpServer, status, null, errorMessage);
                    }
                    j++;
                }
            }
        }
        return null;
    }

    private Server processServerStatusSuccessState(String mpServer, String mgmtStatus, JSONArray serverObj2) throws JSONException {
        String status = new String();
        int j = 0;
        while (j < serverObj2.length()) {
            if (serverObj2.getJSONObject(j).getString("uuid").equalsIgnoreCase(mpServer)) {
                String mpStatus = serverObj2.getJSONObject(j).getString("status").equalsIgnoreCase("success") ? DEPLOYED : UNDEPLOYED;
                status = mgmtStatus.equalsIgnoreCase(mpStatus) ? SYNC : MISMATCH;
                String errorMessage = null;
                if (status == MISMATCH) {
                    errorMessage = "Ideal State:" + mgmtStatus + " MP State:" + mpStatus;
                }
                return new Server(mpServer, status, null, errorMessage);
            }
            j++;
        }
        return null;
    }

    private String checkMgmtStatus(String jsonResponse) {
        String status = new String();
        try {
            JSONObject mgmtObject = new JSONObject(jsonResponse);
            return mgmtObject.getString("state");
        } catch (JSONException e) {
            status = UNDEPLOYED;
        }
        return null;
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
            mpAPIDeployURL = "http://" + LOCAL_URL + "/v1/diagnosis/mpdeploymentinfo/organizations/" + org + "/environments/" + env + "/apis/" + api + "/status";
        } else {
            mpAPIDeployURL = "http://" + LOCAL_URL + "/v1/diagnosis/mpdeploymentinfo/organizations/" + org + "/environments/" + env + "/apis/" + api + "/revisions/" + revision + "/status";
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
