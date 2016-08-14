package com.apigee.diagnosis.zk;

import com.apigee.diagnosis.util.RestAPIExecutor;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Created by amar on 10/07/16.
 */
public class ZKClientManager implements ZKManager {

    private static ZooKeeper zooKeeper;

    private static ZKConnector zkConnector;

    // Set of ZooKeeper Servers Ensemble
    private String zkServersEnsemble;

    private static Logger LOG = LoggerFactory.getLogger(ZKClientManager.class);

    private final static String PINPOINT_ZKHOSTS_URL = "https://pinpointapi.apigee.net/inventory/v0/hosts";

    private final static String ACCESS_TOKEN = "eyJhbGciOiJSUzI1NiJ9.eyJqdGkiOiJmYzcyZDI3OS05OGY5LTQ3YzQtYTNmNC00MWMzOTBmNjM1OTYiLCJzdWIiOiI2M2FhOWU2YS1mNTQzLTRkYzAtOTYxMi02NjEzYjExY2I3YWMiLCJzY29wZSI6WyJwYXNzd29yZC53cml0ZSIsImFwcHJvdmFscy5tZSIsInNjaW0ubWUiLCJvcGVuaWQiLCJvYXV0aC5hcHByb3ZhbHMiXSwiY2xpZW50X2lkIjoicGlucG9pbnQiLCJjaWQiOiJwaW5wb2ludCIsImF6cCI6InBpbnBvaW50IiwidXNlcl9pZCI6IjYzYWE5ZTZhLWY1NDMtNGRjMC05NjEyLTY2MTNiMTFjYjdhYyIsIm9yaWdpbiI6InVzZXJncmlkIiwidXNlcl9uYW1lIjoiYWRldmVnb3dkYUBhcGlnZWUuY29tIiwiZW1haWwiOiJhZGV2ZWdvd2RhQGFwaWdlZS5jb20iLCJhdXRoX3RpbWUiOjE0Njg3NTkwOTQsInJldl9zaWciOiI1M2U5NGFlMiIsImlhdCI6MTQ2ODc1OTA5NSwiZXhwIjoxNDY4ODAyMjk1LCJpc3MiOiJodHRwczovL2xvZ2luLmFwaWdlZS5jb20vb2F1dGgvdG9rZW4iLCJ6aWQiOiJ1YWEiLCJhdWQiOlsicGlucG9pbnQiLCJzY2ltIiwib3BlbmlkIiwicGFzc3dvcmQiLCJhcHByb3ZhbHMiLCJvYXV0aCJdfQ.ZBk87dnJJ5tHRt1L_95JaBQPhjjOhRU0776796OzVom0ApWh0NmNceBsx4WW9H37FNxGezwbMb9_Ou0u2nsjNhaYc17RebJzGYczdHKqsaI7wL6mFS639-iov6f_YOWpWdiMgIfhGR1CFdOCzLh3iiEv3Gf4eATCIxcHTVXSUYtenYhUMz-pM9W_0YxbtjPjmaXoypdgZPxH2_j0uXBinCoaXsQZQaPQjfkLIDVBVVP8-c-Hym64IPLGN_RRfIlmtR43rvZSAu9EntvgcC39hCq2fpZidME8lG98lMq6WM06hrocKbt_Ht6Og8l_L04S_JH-VoWQFw4K26RP8ikDyw";

    public ZKClientManager() {
        zkServersEnsemble = initializeZKEnsemble();
    }

    public ZKClientManager(String zkServersEnsemble) {
        this.zkServersEnsemble = zkServersEnsemble;
    }

    /**
     * Initialize the ZooKeeper Servers Ensemble
     */
    private String initializeZKEnsemble() {
        // TODO: Invoke the Pinpoint API and get the list of ZK servers
        //String hosts = RestAPIExecutor.executeGETAPI(PINPOINT_ZKHOSTS_URL, ACCESS_TOKEN);
        //System.out.println(hosts);

        String zooKeeperServers = "1.2.3.4";

        return zooKeeperServers;
    }

    /**
     * Open the connection to the zookeeper ensemble
     */
    public void openConnection() throws IOException, InterruptedException {
        zkConnector = new ZKConnector();
        zooKeeper = zkConnector.connect(zkServersEnsemble);
    }

    /**
     * Close the zookeeper connection
     */
    public void closeConnection() throws InterruptedException {
        zkConnector.close();
    }

    /**
     * Create znode in zookeeper ensemble
     */
    @Override
    public void createZNode(String path, byte[] data) throws KeeperException,
            InterruptedException {
        zooKeeper.create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.PERSISTENT);
    }

    /**
     * Check existence of znode and its status, if znode is available
     */
    @Override
    public Stat getZNodeStats(String path) throws KeeperException,
            InterruptedException {
        LOG.info("Checking the existence of the node " + path);
        return zooKeeper.exists(path, true);
    }

    /**
     * Gets the data from a specific znode in zookeeper ensemble
     */
    @Override
    public String getZNodeData(String path) throws KeeperException,
            InterruptedException {
        Stat stat = getZNodeStats(path);
        String zNodeData = null;
        byte[] data = null;
        if (stat != null) {
            try {
                LOG.info("Reading the data from the node " + path);
                data = zooKeeper.getData(path, null, null);
                // convert the data into String if non null
                if (data != null) {
                    zNodeData = new String(data, "UTF-8");
                }
            }catch (Exception e) {
                LOG.error("Exception while reading the data from the node " + path + " " + e.getMessage());
            }
        } else {
            LOG.error("Node " + path + " does not exist");
        }

        return zNodeData;
    }

    /**
     * Gets all the children of a specific znode in zookeeper ensemble
     */
    @Override
    public List<String> getZNodeChildren(String path) throws KeeperException,
            InterruptedException {
        LOG.info("Listing all the children of the node " + path);
        Stat stat = getZNodeStats(path);
        List<String> children  = null;

        if (stat != null) {
            children = zooKeeper.getChildren(path, false);
        } else {
            LOG.error("Node " + path + " does not exist");
        }
        return children;
    }

    /**
     * Update the data in a znode in zookeeper ensemble
     */
    @Override
    public void updateZNode(String path, byte[] data) throws KeeperException,
            InterruptedException {
        int version = zooKeeper.exists(path, true).getVersion();
        zooKeeper.setData(path, data, version);
    }

    /**
     * Delete znode in zookeeper ensemble
     */
    @Override
    public void deleteZNode(String path) throws KeeperException,
            InterruptedException {
        int version = zooKeeper.exists(path, true).getVersion();
        zooKeeper.delete(path, version);
    }

}