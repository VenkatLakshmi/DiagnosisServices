package com.apigee.diagnosis.mp;

import com.apigee.diagnosis.deployment.ZKAPIDeployInfoService;
import com.apigee.diagnosis.zk.ZKClientManager;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



/**
 * Created by amar on 16/09/16.
 */
public class MPManager {

    // instance of ZKClientManager
    private ZKClientManager zkClientManager;

    // czk01apigee (US EAST), czk04apigee (US WEST), czk060sy (AP SOUTHEAST), czk21eu (EU)
    private final static String ZK_SERVER_ENSEMBLE = "192.168.11.13,192.168.41.159,10.10.30.17,192.168.73.241";
    //private final static String ZK_SERVER_ENSEMBLE = initializeZKHosts();


    // Znode names
    private final static String REGIONS_NODE = "regions";
    private final static String PODS_NODE ="pods";
    private final static String TYPES_NODE = "types";
    private final static String MESSAGE_PROCESSOR_NODE = "message-processor";
    private final static String SERVERS_NODE = "servers";
    private final static String INTERNAL_IP_NODE = "InternalIP";
    private final static String EXTERNAL_IP_NODE = "ExternalIP";
    private final static String EXTERNAL_HOSTNAME_NODE = "ExternalHostName";
    private final static String DATA_NODE = "_data";

    // LOG instance
    private static Logger LOG = LoggerFactory.getLogger(MPManager.class);

    public MPManager() throws IOException, InterruptedException {
        openZKConnection();
    }

    /*
     *  Initializes the connection with Zookeeper Ensemble
     */
    private void openZKConnection() throws IOException, InterruptedException {
        zkClientManager = new ZKClientManager(ZK_SERVER_ENSEMBLE);
        zkClientManager.openConnection();
    }

    /*
     * Gets all the regions for the specific organization and environment
     * set in this class
     * regions node path:
     *     /organizations/<org>/environments/<env>/regions
     */
    public List<String> getAllRegionsForOrgAndEnv(String org, String env) throws KeeperException,
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
    public List<String> getPodsInRegion(String org, String env, String region) throws KeeperException,
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
    public Map<String, List<String>> getRegionsAndPodsForOrgAndEnv(String org,
                                                                   String env) throws KeeperException,
            InterruptedException {
        List <String> regions = getAllRegionsForOrgAndEnv(org, env);
        if (regions == null) {
            return null;
        }

        Map<String, List<String>> regionsAndPodsMap = new HashMap<String, List<String>>();
        for(String region: regions) {
            List<String> pods = getPodsInRegion(org, env, region);
            regionsAndPodsMap.put(region, pods);
        }
        return regionsAndPodsMap;
    }

    /*
     * Gets the UUIDs of all the MP Servers in a specific pod of a region
     * pods node path:
     *     /organizations/<org>/environments/<env>/regions/<region>/pods
     */
    public List<String> getMPServerUUIDsInPod(String region,
                                              String pod) throws KeeperException,
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
    public String getMPInternalIP(String region, String pod,
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
    public String getMPExternalIP(String region, String pod,
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
     * ExternalHostName node path:
     *     /regions/<region-name>/pods/<pod-name>/servers/<server-uuid>/ExternalHostName
     */
    public String getMPExternalHostName(String region, String pod,
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
     * Gets the ExternalHostName for the specific MP
     * ExternalHostName node path:
     *     /regions/<region-name>/pods/<pod-name>/servers/<server-uuid>/_data
     */
    public String getMPData(String region, String pod,
                                        String mpUUID) throws KeeperException,
            InterruptedException {
        LOG.info("Fetching the complete data of the MP " + mpUUID);

        String dataPath = "/" + REGIONS_NODE + "/" + region +
                "/" + PODS_NODE + "/" + pod +
                "/" + SERVERS_NODE + "/" + mpUUID +
                "/" + DATA_NODE;
        String data = zkClientManager.getZNodeData(dataPath);

        LOG.info("MP Data retrieved");
        return data;
    }

    /*
     * Gets all the revisions of the API Proxy for the specific organization
     * and environment set in this class
     * revisions node path:
     *     /organizations/<org>/environments/<env>/apis/<api>/regions
     */
    public List<String> getAllRevisionsOfAPIForOrgAndEnv(String org,
                                                         String env,
                                                         String api) throws KeeperException,
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

    public void closeZKConnection() throws InterruptedException {
        zkClientManager.closeConnection();
    }

}
