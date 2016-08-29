package com.apigee.diagnosis.deployment;

import com.apigee.diagnosis.beans.APIDeploymentState;
import com.apigee.diagnosis.beans.Revision;
import com.apigee.diagnosis.beans.Server;
import com.apigee.diagnosis.zk.ZKClientManager;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Created by amar on 10/07/16.
 */
public class ZKAPIDeployInfoService {

    private String org;
    private String env;
    private String api;
    private String revision;

    // Znode path upto revision
    private String revisionNodePath;

    //
    private boolean inputValid = false;

    // instance of ZKClientManager
    private ZKClientManager zkClientManager;

    // czk01apigee (US EAST)
    private final static String ZK_SERVER_ENSEMBLE = "192.168.11.13";

    // Znode names
    private final static String STATE_NODE = "state";
    private final static String SPEC_NODE = "spec";
    private final static String SERVERS_NODE = "statuses";
    private final static String SERVERS_STATUS_NODE = "status";
    private final static String SERVERS_ERRORCODE_NODE = "errorcode";
    private final static String SERVERS_ERRORMESSAGE_NODE = "errormessage";

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

    public ZKAPIDeployInfoService(String org, String env, String api)
            throws KeeperException, IOException, InterruptedException {
        if (org == null || env == null || api == null) {
            throw new IllegalArgumentException (
                    String.format("Parameters can't be null: org=%s, env=%s api=%s",
                            org, env, api));
        }
        this.org = org;
        this.env = env;
        this.api = api;
        this.revisionNodePath = "/organizations/" + org +
                "/environments/" + env +
                "/apiproxies/" + api +
                "/revisions";

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

    public ZKAPIDeployInfoService(String org, String env,
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
        this.revisionNodePath = "/organizations/" + org +
                "/environments/" + env +
                "/apiproxies/" + api +
                "/revisions/" + revision;

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
     *    - checkRevision indicates if we need to validate revision or not
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
     * Gets the deployment state for the specific API set in this class
     * deployment state node path:
     *     /organizations/<org>/environments/<env>/apiproxies/<api>/revisions/<revision>/state
     */
    public String getAPIDeploymentState() throws KeeperException,
            InterruptedException {
        LOG.info("Fetching the deployment state");

        String apiDeploymentStatePath = this.revisionNodePath + "/" + STATE_NODE;
        String apiDeploymentState = zkClientManager.getZNodeData(apiDeploymentStatePath);

        LOG.info("Deployment state retrieved, state = " + apiDeploymentState);
        return apiDeploymentState;
    }

    /*
     * Gets the spec for the specific API set in this class
     * spec node path:
     *     /organizations/<org>/environments/<env>/apiproxies/<api>/revisions/<revision>/spec
     */
    public String getAPISpec() throws KeeperException, InterruptedException {
        LOG.info("Fetching the spec");

        String apiSpecPath = this.revisionNodePath + "/" + SPEC_NODE;
        String apiSpec = zkClientManager.getZNodeData(apiSpecPath);

        LOG.info("Spec retrieved, spec = " + apiSpec);
        return apiSpec;
    }

    /*
     * Gets the status of all the servers for the specific API
     * set in this class
     * servers node path:
     *     /organizations/<org>/environments/<env>/apiproxies/<api>/revisions/<revision>/statuses
     *
     * server status node path:
     *     /organizations/<org>/environments/<env>/apiproxies/<api>/revisions/<revision>/statuses/<server uuid>/status
     */
    public Server[] getAllServersStateofAPI() throws KeeperException,
            InterruptedException {

        LOG.info("Fetching the deployment status on all the servers");

        String apiServersPath = this.revisionNodePath + "/" + SERVERS_NODE;

        // Get all the servers
        List<String> serversList = zkClientManager.getZNodeChildren(apiServersPath);

        Server allServers[] = new Server[serversList.size()];
        if (serversList != null) {
            int serverIndex = 0;
            // Iterate through each server and get its status
            for (String serverUUID : serversList) {
                String apiServerStatusPath = apiServersPath + "/"
                        + serverUUID + "/"
                        + SERVERS_STATUS_NODE;
                String status = zkClientManager.getZNodeData(apiServerStatusPath);
                String errorcode = "";
                String errormessage = "";
                Server server = null;
                if (!status.equals("success")) {
                    String apiServerErrorCodePath = apiServersPath + "/"
                            + serverUUID + "/"
                            + SERVERS_ERRORCODE_NODE;
                    errorcode = zkClientManager.getZNodeData(apiServerErrorCodePath);

                    String apiServerErrorMessagePath = apiServersPath + "/"
                            + serverUUID + "/"
                            + SERVERS_ERRORMESSAGE_NODE;
                    errormessage = zkClientManager.getZNodeData(apiServerErrorMessagePath);
                    // add it to the server
                    server = new Server(serverUUID, status, errorcode, errormessage);

                } else {
                    server = new Server(serverUUID, status, null, null);
                }
                allServers[serverIndex++] = server;
            }
        }
        LOG.info("Deployment status on all the servers retrieved");
        return allServers;
    }

    public APIDeploymentState getCompleteDeploymentInfo() throws KeeperException,
            InterruptedException {
        String state = getAPIDeploymentState();
        String spec = getAPISpec();
        Server allServers[] = getAllServersStateofAPI();
        Revision revision = new Revision(getRevision(),allServers);
        APIDeploymentState apiDeploymentState = new APIDeploymentState(getOrg(), getEnv(), getApi(),new Revision[]{revision}, state);
        return apiDeploymentState;
    }

    public void close() throws InterruptedException {
        zkClientManager.closeConnection();
    }
}