package com.apigee.diagnosis.zk;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * Created by amar on 11/07/16.
 */
public class ZKConnector {
    // zookeeper instance to access ZooKeeper ensemble
    private ZooKeeper zooKeeper;
    private CountDownLatch connectedSignal = new CountDownLatch(1);

    // Zookeeper Session timeout in milliseconds
    private final static int ZK_SESSION_TIMEOUT = 5000;

    /*
     * Connect to the Zookeeper ensemble
     * "127.0.0.1:3000,127.0.0.1:3001,127.0.0.1:3002"
     */
    public ZooKeeper connect(String zkHostPort) throws IOException,
            InterruptedException {
        zooKeeper = new ZooKeeper(zkHostPort, ZK_SESSION_TIMEOUT, new Watcher() {
            public void process(WatchedEvent event) {
                if (event.getState() == KeeperState.SyncConnected) {
                    connectedSignal.countDown();
                }
            }
        });
        connectedSignal.await();
        return zooKeeper;
    }

    // Disconnect from the Zookeeper server
    public void close() throws InterruptedException {
        zooKeeper.close();
    }
}