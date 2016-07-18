package com.apigee.diagnosis.zk;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;

import java.util.List;

/**
 * Created by amar on 10/07/16.
 */
public interface ZKManager {

    /**
     * Create a Znode and save some data
     */
    public void createZNode(String path, byte[] data) throws KeeperException,
            InterruptedException;

    /**
     * Get the ZNode Stats
     */
    public Stat getZNodeStats(String path) throws KeeperException,
            InterruptedException;

    /**
     * Get ZNode Data
     */
    public String getZNodeData(String path) throws KeeperException,
            InterruptedException;

    /**
     * Get ZNode children
     */
    public List<String> getZNodeChildren(String path) throws KeeperException,
            InterruptedException;

    /**
     * Update the ZNode Data
     */
    public void updateZNode(String path, byte[] data) throws KeeperException,
            InterruptedException;

    /**
     * Delete the znode
     */
    public void deleteZNode(String path) throws KeeperException,
            InterruptedException;

}