package com.apigee.diagnosis.deployment;

import com.apigee.diagnosis.beans.APIInformation;
import com.apigee.diagnosis.beans.DeploymentInformation;
import com.apigee.diagnosis.beans.MPAPIDeploymentReport;
import com.apigee.diagnosis.beans.Revision;
import com.apigee.diagnosis.beans.Server;
import com.apigee.diagnosis.mp.MPManager;
import com.apigee.diagnosis.util.RestAPIExecutor;
import com.apigee.diagnosis.util.XMLResponseParser;
import com.apigee.diagnosis.zk.ZKClientManager;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


/**
 * Created by amar on 14/08/16.
 */
public class MPAPIDeployInfoService {
    private String org;
    private String env;
    private String api;
    private String revision;

    private final static String V1 = "v1";
    private final static String RUNTIME = "runtime";
    private final static String MP_PORT_NUMBER = "8080";

    private MPManager mpManager;

    // LOG instance
    private static Logger LOG = LoggerFactory.getLogger(MPAPIDeployInfoService.class);

    public String getOrg(){
        return org;
    }

    public String getEnv(){
        return env;
    }

    public String getApi(){
        return api;
    }

    public MPAPIDeployInfoService(String org, String env, String api)
            throws KeeperException, IOException, InterruptedException {
        if (org == null || env == null || api == null) {
            throw new IllegalArgumentException (
                    String.format("Parameters can't be null: org=%s, env=%s api=%s",
                            org, env, api));
        }
        this.org = org;
        this.env = env;
        this.api = api;
        mpManager = new MPManager();
    }

    /*
     * Checks if all the input arguments are valid
     *
     */
/*    private void validateInputArguments() throws KeeperException,
            InterruptedException, IllegalArgumentException {

        LOG.info("Validating Input Arguments");
        String nodePath = "/organizations/" + org;
        if (zkClientManager.getZNodeStats(nodePath) == null) {
            throw new IllegalArgumentException(org + " " + ErrorMessages.ORGANIZATION_DOES_NOT_EXIST);
        }

        nodePath = nodePath + "/environments/" + env;
        if (zkClientManager.getZNodeStats(nodePath) == null) {
            throw new IllegalArgumentException(env +  " " + ErrorMessages.ENVIRONMENT_DOES_NOT_EXIST);
        }

        nodePath = nodePath + "/apiproxies/" + api;
        if (zkClientManager.getZNodeStats(nodePath) == null) {
            throw new IllegalArgumentException(api + " " + ErrorMessages.APIPROXY_DOES_NOT_EXIST);
        }

        // validate revision only if it is non null value
        if (revision != null) {
            nodePath = nodePath + "/revisions/" + revision;
            if (zkClientManager.getZNodeStats(nodePath) == null) {
                throw new IllegalArgumentException(revision + " " + ErrorMessages.REVISION_DOES_NOT_EXIST);
            }
        }

        inputValid = true;
        LOG.info("Validation of Input Arguments is complete - Input Arguments are valid");
    }*/

    /*
     * Gets the deployed revision of the specific API on MP
     *
     * URL:
     * http://<ExternalIPofMP>:8080/v1/runtime/organizations/<org>/environments/<env>/apis/<api>/revisions
     */
    private String getAPIDeployedRevisionOnMP(String externalIP)
            throws Exception {

        String mpAPIDeployURL = "http://" + externalIP +
                ":" + MP_PORT_NUMBER +
                "/" + V1 +
                "/" + RUNTIME +
                "/organizations/" + org +
                "/environments/" + env +
                "/apis/" + api +
                "/revisions";

        LOG.info("Fetching the deployed revision " + mpAPIDeployURL);

        String apiRevision = RestAPIExecutor.executeGETAPI(mpAPIDeployURL,"dummyuser", "xml");

        LOG.info("Deployed revision on MP = " + apiRevision);
        return apiRevision;
    }

    /*
     * Gets all the details of all the MPs in the organization and environment
     * and specific revision of the API
     *     1. Server UUID
     *     2. Internal IP
     *     3. External IP
     *     4. External HostName
     *     5. Deployment State of the API
     */
    public MPAPIDeploymentReport getAPIDeploymentInfoOnMPs(String revision) throws IOException,
            KeeperException, InterruptedException, Exception {
        Map<String, List<String>> regionsAndPodsMap =
                mpManager.getRegionsAndPodsForOrgAndEnv(getOrg(), getEnv());

        if (regionsAndPodsMap == null) {
            return null;
        }

        ArrayList<Server> mpServersList = new ArrayList<Server>();

        // traverse through each of the pods in all the regions
        // and retrieve the MP server UUIDs
        for(Entry<String, List<String>> entry : regionsAndPodsMap.entrySet()){
            String region = entry.getKey();
            for (String pod: entry.getValue()) {
                List <String> mpsUUIDs = mpManager.getMPServerUUIDsInPod(region, pod);
                // For each of the MP servers, fetch the internal and external IP addresses
                // and deployed revision of the API on the MP
                for (String mpUUID: mpsUUIDs) {
                    String internalIP = mpManager.getMPInternalIP(region, pod, mpUUID);
                    String externalIP = mpManager.getMPExternalIP(region, pod, mpUUID);
                    String externalHostName = mpManager.getMPExternalHostName(region, pod, mpUUID);

                    // Run the API on the MP to get the revision deployed for the specific API
                    String xmlResponse = getAPIDeployedRevisionOnMP(externalIP);
                    ArrayList<String> deployedRevisionsList = XMLResponseParser.getItemListFromXMLResponse(xmlResponse);
                    String apiDeployedState = "undeployed";
                    if (deployedRevisionsList != null
                            && !deployedRevisionsList.isEmpty()) {
                        // On the MP, there's a possibility that multiple revisions can be deployed
                        // iterate through all the revisions and check if the user specified
                        // revision is deployed or not
                        for (String apiDeployedRevisionOnMP: deployedRevisionsList) {
                            LOG.info("apiDeployedRevisionOnMP = " + apiDeployedRevisionOnMP);

                            if ((apiDeployedRevisionOnMP != null) &&
                                    !(apiDeployedRevisionOnMP.isEmpty()) &&
                                    apiDeployedRevisionOnMP.equals(revision)) {
                                apiDeployedState = "deployed";
                                LOG.info("apiDeployedRevisionOnMP = " + apiDeployedRevisionOnMP +
                                        " apiDeployedState = " + apiDeployedState);
                                break;
                            }
                        }
                    }

                    Server mpServer = new Server(externalHostName, mpUUID, apiDeployedState, null, null);
                    mpServersList.add(mpServer);
                } // end of MP UUIDs loop
            } // end of pod loop
        } // end of region loop

        if (!mpServersList.isEmpty()) {
            String description = DiagnosticMessages.DESCRIPTION_MP_API_DEPLOYMENT_REPORT;
            APIInformation apiInformation = new APIInformation(getOrg(), getEnv(), getApi());

            Server allMPServers[] = mpServersList.toArray(new Server[mpServersList.size()]);
            Revision[] revisions = new Revision[1];
            revisions[0]= new Revision(revision, allMPServers);

            DeploymentInformation deploymentInformation = new DeploymentInformation(revisions);

            MPAPIDeploymentReport mpAPIDeploymentReport =
                    new MPAPIDeploymentReport(description, apiInformation, deploymentInformation);
            return mpAPIDeploymentReport;
        }

        return null;
    }

    /*
     * Gets all the details of all the MPs in the organization and environment
     * for all revisions of the API
     *     1. Server UUID
     *     2. Internal IP
     *     3. External IP
     *     4. External HostName
     *     5. Deployment State of the API
     */
    public MPAPIDeploymentReport getAPIDeploymentInfoOnMPs() throws IOException,
            KeeperException, InterruptedException, Exception {
        Map<String, List<String>> regionsAndPodsMap =
                mpManager.getRegionsAndPodsForOrgAndEnv(getOrg(), getEnv());

        if (regionsAndPodsMap == null) {
            return null;
        }

        // Get all the revisions of the api for the specific org and env
        // from the Zookeeper
        List<String> allRevisions =
                mpManager.getAllRevisionsOfAPIForOrgAndEnv(getOrg(), getEnv(), getApi());

        if (allRevisions == null) {
            return null;
        }

        // Create and initialize arraylist of servers for all revisions
        Map<String, ArrayList<Server>> mpServersMap = new HashMap<String, ArrayList<Server>>();
        for (String revision: allRevisions) {
            mpServersMap.put(revision, new ArrayList<Server>());
        }

        // traverse through each of the pods in all the regions
        // and retrieve the MP server UUIDs
        for(Entry<String, List<String>> entry : regionsAndPodsMap.entrySet()){
            String region = entry.getKey();
            for (String pod: entry.getValue()) {
                List <String> mpsUUIDs = mpManager.getMPServerUUIDsInPod(region, pod);
                // For each of the MP servers, fetch the internal, external IP addresses
                // external HostName and deployed revision of the API on the MP
                for (String mpUUID: mpsUUIDs) {
                    String internalIP = mpManager.getMPInternalIP(region, pod, mpUUID);
                    String externalIP = mpManager.getMPExternalIP(region, pod, mpUUID);
                    String externalHostName = mpManager.getMPExternalHostName(region, pod, mpUUID);

                    // Run the API on the MP to get the revisions deployed for the specific API
                    String xmlResponse = getAPIDeployedRevisionOnMP(externalIP);
                    ArrayList<String> deployedRevisionsList =
                            XMLResponseParser.getItemListFromXMLResponse(xmlResponse);
                    for(String revision: allRevisions) {
                        String apiDeployedState = "undeployed";
                        if (deployedRevisionsList != null
                                && !deployedRevisionsList.isEmpty()) {
                            // On the MP, there's a possibility that multiple revisions can be deployed
                            // iterate through all the revisions and check if the specific revision
                            // is deployed or not
                            for (String apiDeployedRevisionOnMP : deployedRevisionsList) {
                                LOG.info("apiDeployedRevisionOnMP = " + apiDeployedRevisionOnMP);

                                if ((apiDeployedRevisionOnMP != null) &&
                                        !(apiDeployedRevisionOnMP.isEmpty()) &&
                                        apiDeployedRevisionOnMP.equals(revision)) {
                                    apiDeployedState = "deployed";
                                    LOG.info("apiDeployedRevisionOnMP = " + apiDeployedRevisionOnMP +
                                            " apiDeployedState = " + apiDeployedState);
                                    break;
                                }
                            }
                        }
                        Server mpServer = new Server(externalHostName, mpUUID, apiDeployedState, null, null);
                        ArrayList<Server> mpServersList = mpServersMap.get(revision);
                        mpServersList.add(mpServer);
                    }
                } // end of MP UUIDs loop
            } // end of pod loop
        } // end of region loop

        // prepare the MPAPIDeploymentReport
        String description = DiagnosticMessages.DESCRIPTION_MP_API_DEPLOYMENT_REPORT;
        APIInformation apiInformation = new APIInformation(getOrg(), getEnv(), getApi());

        Revision revisions[] = new Revision[allRevisions.size()];
        int revisionIndex = 0;
        for (String revision: allRevisions) {
            ArrayList<Server> mpServersList = mpServersMap.get(revision);
            Server allMPServers[] = mpServersList.toArray(new Server[mpServersList.size()]);
            revisions[revisionIndex++] = new Revision(revision, allMPServers);
        }
        DeploymentInformation deploymentInformation = new DeploymentInformation(revisions);

        MPAPIDeploymentReport mpAPIDeploymentReport =
                new MPAPIDeploymentReport(description, apiInformation, deploymentInformation);

        return mpAPIDeploymentReport;
    }

    public void close() throws InterruptedException {
        mpManager.closeZKConnection();
    }
}