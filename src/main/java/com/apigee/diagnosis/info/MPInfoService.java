package com.apigee.diagnosis.info;

import com.apigee.diagnosis.beans.*;
import com.apigee.diagnosis.deployment.DiagnosticMessages;
import com.apigee.diagnosis.mp.MPManager;
import com.apigee.diagnosis.util.RestAPIExecutor;
import com.apigee.diagnosis.util.XMLResponseParser;
import org.apache.zookeeper.KeeperException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by amar on 18/09/16.
 */
public class MPInfoService {

    private final static String V1 = "v1";
    private final static String BUILDINFO = "buildinfo";
    private final static String SERVERINFO = "servers/self";
    private final static String MP_PORT_NUMBER = "8080";

    private MPManager mpManager;

    // LOG instance
    private static Logger LOG = LoggerFactory.getLogger(MPInfoService.class);

    public MPInfoService() throws KeeperException, IOException, InterruptedException {
        this.mpManager = new MPManager();
    }

    public MPInformationReport getMPInformation(String org, String env)
            throws KeeperException, InterruptedException, JSONException, Exception {
        Map<String, List<String>> regionsAndPodsMap =
                mpManager.getRegionsAndPodsForOrgAndEnv(org, env);

        if (regionsAndPodsMap == null) {
            return null;
        }

        ArrayList<MPServer> mpServersList = new ArrayList<MPServer>();

        // traverse through each of the pods in all the regions
        // and retrieve the MP server UUIDs
        for(Map.Entry<String, List<String>> entry : regionsAndPodsMap.entrySet()){
            String region = entry.getKey();
            for (String pod: entry.getValue()) {
                List <String> mpsUUIDs = mpManager.getMPServerUUIDsInPod(region, pod);
                // For each of the MP servers, fetch the internal and external IP addresses
                // and deployed revision of the API on the MP
                for (String mpUUID: mpsUUIDs) {
                    String externalIP = mpManager.getMPExternalIP(region, pod, mpUUID);
                    String mpServerInfo = getMPServerInfo(externalIP);
                    JSONObject mpServerInfoJSON = new JSONObject(mpServerInfo);

                    String externalHostName = mpServerInfoJSON.getString("externalHostName");
                    String internalHostName = mpServerInfoJSON.getString("internalHostName");
                    String internalIP = mpServerInfoJSON.getString("internalIP");
                    String isUp = mpServerInfoJSON.getString("isUp");

                    JSONObject mpServerInfoTagsJSON = mpServerInfoJSON.getJSONObject("tags");
                    //JSONArray mpPropertyJSONArray = mpServerInfoJSON.getJSONArray("property");
                    //String upTime = mpPropertyJSONArray.getString(12);

                    MPBuildInfo mpBuildInfo = getMPBuildInfo(mpServerInfoJSON);

                    MPServer mpServer = new MPServer(externalHostName, externalIP,
                            internalHostName, internalIP, mpUUID, region, pod, isUp, null, mpBuildInfo);
                    mpServersList.add(mpServer);
                } // end of MP UUIDs loop
            } // end of pod loop
        } // end of region loop

        if (!mpServersList.isEmpty()) {
            String description = DiagnosticMessages.DESCRIPTION_MP_INFO_REPORT;

            MPServer mpServers[] = mpServersList.toArray(new MPServer[mpServersList.size()]);

            MPInformationReport mpInformationReport =
                    new MPInformationReport(description, org, env, mpServers);
            return mpInformationReport;
        }

        return null;
    }

    /*
     * Gets the server info of MP
     *
     * URL:
     * http://<ExternalIPofMP>:8080/v1/servers/self
     */
    private String getMPServerInfo(String externalIP) throws Exception {

        String mpBuildInfoURL = "http://" + externalIP +
                ":" + MP_PORT_NUMBER +
                "/" + V1 +
                "/" + SERVERINFO;

        LOG.info("Fetching the build info on MP " + mpBuildInfoURL);

        String buildInfo = RestAPIExecutor.executeGETAPI(mpBuildInfoURL, "dummyuser", "json");

        LOG.info("Build Info on MP = " + buildInfo);
        return buildInfo;
    }

    /*
     * Gets the build info from the MP server info
     *
     */
    private MPBuildInfo getMPBuildInfo(JSONObject mpServerInfoJSON) throws JSONException {
        JSONObject mpBuildInfoJSON = mpServerInfoJSON.getJSONObject("buildInfo");
        String buildNumber = mpBuildInfoJSON.getString("buildNumber");
        String buildTimestamp = mpBuildInfoJSON.getString("buildTimestamp");
        String rPMName = mpBuildInfoJSON.getString("rPMName");
        String releaseID = mpBuildInfoJSON.getString("releaseID");
        String sCMBranch = mpBuildInfoJSON.getString("sCMBranch");
        String sCMRevision = mpBuildInfoJSON.getString("sCMRevision");
        MPBuildInfo mpBuildInfo = new MPBuildInfo(buildNumber, buildTimestamp,
                rPMName, releaseID, sCMBranch, sCMRevision);

        return mpBuildInfo;
    }
}
