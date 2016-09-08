package com.apigee.diagnosis.deployment;

import com.apigee.diagnosis.beans.APIDeploymentState;
import com.apigee.diagnosis.beans.Revision;
import com.apigee.diagnosis.beans.Server;
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

    //
    private boolean inputValid = false;

    // instance of ZKClientManager
    private ZKClientManager zkClientManager;

    // czk01apigee (US EAST)
    private final static String ZK_SERVER_ENSEMBLE = "192.168.11.13";

    // Znode names
    private final static String REGIONS_NODE = "regions";
    private final static String PODS_NODE ="pods";
    private final static String TYPES_NODE = "types";
    private final static String MESSAGE_PROCESSOR_NODE = "message-processor";
    private final static String SERVERS_NODE = "servers";
    private final static String INTERNAL_IP_NODE = "InternalIP";
    private final static String EXTERNAL_IP_NODE = "ExternalIP";
    private final static String EXTERNAL_HOSTNAME_NODE = "ExternalHostName";

    private final static String V1 = "v1";
    private final static String RUNTIME = "runtime";
    private final static String MP_PORT_NUMBER = "8080";
    // LOG instance
    private static Logger LOG = LoggerFactory.getLogger(ZKAPIDeployInfoService.class);

    public String getOrg(){
        return org;
    }

    public String getEnv(){
        return env;
    }

    public String getApi(){
        return api;
    }

    public String getRevision(){
        return revision;
    }

    private boolean isInputValid() {
        return inputValid;
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

        // open the connection with Zookeeper Ensemble
        initialize();

        try {
            // validate the arguments
            validateInputArguments();
        } catch (InterruptedException ie) {
            close();
            throw ie;
        } catch (IllegalArgumentException iae) {
            close();
            throw iae;
        }
    }

    public MPAPIDeployInfoService(String org, String env,
                                  String api, String revision)
            throws KeeperException, IOException, InterruptedException {
        if (org == null || env == null || api == null || revision == null) {
            throw new IllegalArgumentException (
                    String.format("Parameters can't be null: " +
                                    "org=%s, env=%s api=%s revision=%s",
                            org, env, api, revision));
        }
        this.org = org;
        this.env = env;
        this.api = api;
        this.revision = revision;

        // open the connection with Zookeeper Ensemble
        initialize();

        // validate the arguments
        try {
            validateInputArguments();
        } catch (InterruptedException ie) {
            close();
            throw ie;
        } catch (IllegalArgumentException iae) {
            close();
            throw iae;
        }
    }

    /*
     *  Initializes the connection with Zookeeper Ensemble
     */
    private void initialize() throws IOException, InterruptedException {
        zkClientManager = new ZKClientManager(ZK_SERVER_ENSEMBLE);
        zkClientManager.openConnection();
    }

    /*
     * Checks if all the input arguments are valid
     *
     */
    private void validateInputArguments() throws KeeperException,
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
    }

    /*
     * Gets all the regions for the specific organization and environment
     * set in this class
     * regions node path:
     *     /organizations/<org>/environments/<env>/regions
     */
    private List<String> getAllRegionsForOrgAndEnv() throws KeeperException,
            InterruptedException {
        LOG.info("Fetching the regions");

        String orgEnvRegionsPath = "/organizations/" + org +
                "/environments/" + env +
                "/" + REGIONS_NODE;
        List<String> regions = zkClientManager.getZNodeChildren(orgEnvRegionsPath);

        LOG.info("Regions retrieved " + regions);
        return regions;
    }

    /*
     * Gets all the regions for the specific organization and environment
     * set in this class
     * pods node path:
     *     /organizations/<org>/environments/<env>/regions/<region>/pods
     */
    private List<String> getPodsInRegion(String region) throws KeeperException,
            InterruptedException {
        LOG.info("Fetching the pods in region " + region);

        String orgEnvPodsPath = "/organizations/" + org +
                "/environments/" + env +
                "/" + REGIONS_NODE + "/" + region +
                "/" + PODS_NODE;
        List<String> pods = zkClientManager.getZNodeChildren(orgEnvPodsPath);


        LOG.info("Pods retrieved " + pods);
        return pods;
    }

    /*
     * Gets all the regions and pods for the specific organization and environment
     * set in this class
     */
    private Map<String, List<String>> getRegionsAndPodsForOrgAndEnv() throws KeeperException,
            InterruptedException {
        List <String> regions = getAllRegionsForOrgAndEnv();
        if (regions == null) {
            return null;
        }

        Map<String, List<String>> regionsAndPodsMap = new HashMap<String, List<String>>();
        for(String region: regions) {
            List<String> pods = getPodsInRegion(region);
            regionsAndPodsMap.put(region, pods);
        }
        return regionsAndPodsMap;
    }

    /*
     * Gets all the regions for the specific organization and environment
     * set in this class
     * pods node path:
     *     /organizations/<org>/environments/<env>/regions/<region>/pods
     */
    private List<String> getMPServerUUIDsInPod(String region, String pod) throws KeeperException,
            InterruptedException {
        LOG.info("Fetching the MPs in pod " + pod);

        String messageProcessorPath = "/" + REGIONS_NODE + "/" + region +
                "/" + PODS_NODE + "/" + pod +
                "/" + TYPES_NODE +
                "/" + MESSAGE_PROCESSOR_NODE;
        List<String> mpServerUUIDs = zkClientManager.getZNodeChildren(messageProcessorPath);

        LOG.info("MPs retrieved " + mpServerUUIDs);
        return mpServerUUIDs;
    }

    /*
     * Gets the InternalIP for the specific MP
     * InternalIP node path:
     *     /regions/<region-name>/pods/<pod-name>/servers/<server-uuid>/InternalIP
     */
    private String getMPInternalIP(String region, String pod,
                                   String mpUUID) throws KeeperException,
            InterruptedException {
        LOG.info("Fetching the Internal IP address of the MP " + mpUUID);

        String internalIPPath = "/" + REGIONS_NODE + "/" + region +
                "/" + PODS_NODE + "/" + pod +
                "/" + SERVERS_NODE + "/" + mpUUID +
                "/" + INTERNAL_IP_NODE;
        String internalIP = zkClientManager.getZNodeData(internalIPPath);

        LOG.info("MP Internal IP retrieved");
        return internalIP;
    }

    /*
     * Gets the ExternalIP for the specific MP
     * ExternalIP node path:
     *     /regions/<region-name>/pods/<pod-name>/servers/<server-uuid>/ExternalIP
     */
    private String getMPExternalIP(String region, String pod,
                                   String mpUUID) throws KeeperException,
            InterruptedException {
        LOG.info("Fetching the External IP address of the MP " + mpUUID);

        String externalIPPath = "/" + REGIONS_NODE + "/" + region +
                "/" + PODS_NODE + "/" + pod +
                "/" + SERVERS_NODE + "/" + mpUUID +
                "/" + EXTERNAL_IP_NODE;
        String externalIP = zkClientManager.getZNodeData(externalIPPath);

        LOG.info("MP External IP retrieved");
        return externalIP;
    }

    /*
     * Gets the ExternalHostName for the specific MP
     * ExternalIP node path:
     *     /regions/<region-name>/pods/<pod-name>/servers/<server-uuid>/ExternalHostName
     */
    private String getMPExternalHostName(String region, String pod,
                                   String mpUUID) throws KeeperException,
            InterruptedException {
        LOG.info("Fetching the External HostName of the MP " + mpUUID);

        String externalIPPath = "/" + REGIONS_NODE + "/" + region +
                "/" + PODS_NODE + "/" + pod +
                "/" + SERVERS_NODE + "/" + mpUUID +
                "/" + EXTERNAL_HOSTNAME_NODE;
        String externalIP = zkClientManager.getZNodeData(externalIPPath);

        LOG.info("MP External HostName retrieved");
        return externalIP;
    }

    /*
     * Gets all the revisions of the API Proxy for the specific organization
     * and environment set in this class
     * revisions node path:
     *     /organizations/<org>/environments/<env>/apis/<api>/regions
     */
    private List<String> getAllRevisionsOfAPIForOrgAndEnv() throws KeeperException,
            InterruptedException {
        LOG.info("Fetching the revisions");

        String revisionsPath = "/organizations/" + org +
                "/environments/" + env +
                "/apiproxies/" + api +
                "/revisions";
        List<String> revisions = zkClientManager.getZNodeChildren(revisionsPath);

        LOG.info("Revisions retrieved " + revisions);
        return revisions;
    }


    /*
     * Gets the deployed revision of the specific API on MP
     *
     * URL:
     * http://<ExternalIPofMP>:8080/v1/runtime/organizations/<org>/environments/<env>/apis/<api>/revisions
     */
    private String getAPIDeployedRevisionOnMP(String externalIP)
            throws Exception {
        LOG.info("Fetching the deployed revision");

        String mpAPIDeployURL = "http://" + externalIP +
                ":" + MP_PORT_NUMBER +
                "/" + V1 +
                "/" + RUNTIME +
                "/organizations/" + org +
                "/environments/" + env +
                "/apis/" + api +
                "/revisions";

        String apiRevision = RestAPIExecutor.executeGETAPI(mpAPIDeployURL,"dummyuser", "xml");

        LOG.info("Deployed revision on MP = " + apiRevision);
        return apiRevision;
    }

    /*
     * Gets all the details of all the MPs in the organization and environment
     * and specific revision set in this class
     *     1. Server UUID
     *     2. Internal IP
     *     3. External IP
     *     4. External HostName
     *     5. Deployment State of the API
     */
    public APIDeploymentState getCompleteDeploymentInfoOnAllMPs() throws IOException,
            KeeperException, InterruptedException, Exception {
        Map<String, List<String>> regionsAndPodsMap = getRegionsAndPodsForOrgAndEnv();

        if (regionsAndPodsMap == null) {
            return null;
        }

        ArrayList<Server> mpServersList = new ArrayList<Server>();

        // traverse through each of the pods in all the regions
        // and retrieve the MP server UUIDs
        for(Entry<String, List<String>> entry : regionsAndPodsMap.entrySet()){
            String region = entry.getKey();
            for (String pod: entry.getValue()) {
                List <String> mpsUUIDs = getMPServerUUIDsInPod(region, pod);
                // For each of the MP servers, fetch the internal and external IP addresses
                // and deployed revision of the API on the MP
                for (String mpUUID: mpsUUIDs) {
                    String internalIP = getMPInternalIP(region, pod, mpUUID);
                    String externalIP = getMPExternalIP(region, pod, mpUUID);
                    String externalHostName = getMPExternalHostName(region, pod, mpUUID);

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
                                    apiDeployedRevisionOnMP.equals(this.revision)) {
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
            Server allMPServers[] = mpServersList.toArray(new Server[mpServersList.size()]);
            Revision revision = new Revision(getRevision(), allMPServers);
            APIDeploymentState apiDeploymentState = new APIDeploymentState(getOrg(),
                    getEnv(), getApi(), new Revision[]{revision}, null);
            return apiDeploymentState;
        }

        return null;
    }

    /*
     * Gets all the details of all the MPs in the organization and environment
     * for all revisions
     *     1. Server UUID
     *     2. Internal IP
     *     3. External IP
     *     4. External HostName
     *     5. Deployment State of the API
     */
    public APIDeploymentState getCompleteDeploymentInfoOnAllMPsForAllRevisions() throws IOException,
            KeeperException, InterruptedException, Exception {
        Map<String, List<String>> regionsAndPodsMap = getRegionsAndPodsForOrgAndEnv();

        if (regionsAndPodsMap == null) {
            return null;
        }

        // Get all the revisions of the api for the specific org and env
        // from the Zookeeper
        List<String> allRevisions = getAllRevisionsOfAPIForOrgAndEnv();

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
                List <String> mpsUUIDs = getMPServerUUIDsInPod(region, pod);
                // For each of the MP servers, fetch the internal, external IP addresses
                // external HostName and deployed revision of the API on the MP
                for (String mpUUID: mpsUUIDs) {
                    String internalIP = getMPInternalIP(region, pod, mpUUID);
                    String externalIP = getMPExternalIP(region, pod, mpUUID);
                    String externalHostName = getMPExternalHostName(region, pod, mpUUID);

                    // Run the API on the MP to get the revisions deployed for the specific API
                    String xmlResponse = getAPIDeployedRevisionOnMP(externalIP);
                    ArrayList<String> deployedRevisionsList = XMLResponseParser.getItemListFromXMLResponse(xmlResponse);
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

        Revision revisions[] = new Revision[allRevisions.size()];
        int revisionIndex = 0;
        for (String revision: allRevisions) {
            ArrayList<Server> mpServersList = mpServersMap.get(revision);
            Server allMPServers[] = mpServersList.toArray(new Server[mpServersList.size()]);
            revisions[revisionIndex++] = new Revision(revision, allMPServers);
        }
        APIDeploymentState apiDeploymentState = new APIDeploymentState(getOrg(),
                getEnv(), getApi(), revisions, null);
        return apiDeploymentState;
    }

    public void close() throws InterruptedException {
        zkClientManager.closeConnection();
    }

}
