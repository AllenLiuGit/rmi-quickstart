package com.legend.rmi.quickstart.common;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * Created by allen on 6/28/16.
 */
public class ZooKeeperUtils {
    private static final Logger LOG = LoggerFactory.getLogger(ZooKeeperUtils.class);

    /**
     * 连接ZooKeeper服务器
     * @return
     */
    public static ZooKeeper connectZooKeeper(final CountDownLatch latch) {
        ZooKeeper zk = null;

        try {
            zk = new ZooKeeper(Constants.ZK_CONNECT_STRING, Constants.ZK_SESSION_TIMEOUT, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getState() == Event.KeeperState.SyncConnected) {
                        latch.countDown(); // 唤醒当前正在执行的线程
                    }
                }
            });

            latch.await(); // 使当前线程等待
        } catch (IOException ex) {
            LOG.error("Connect zookeeper with error: ", ex);
        } catch (InterruptedException ex) {
            LOG.error("Wait with error: ", ex);
        }

        return zk;
    }
}
