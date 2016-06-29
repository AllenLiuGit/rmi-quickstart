package com.legend.rmi.quickstart.client;

import com.legend.rmi.quickstart.common.Constants;
import com.legend.rmi.quickstart.common.ZooKeeperUtils;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.rmi.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by allen on 6/28/16.
 */
public class ServiceConsumer {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceConsumer.class);

    // 用于等待 SyncConnected 事件触发后继续执行当前线程
    private CountDownLatch latch = new CountDownLatch(1);

    // 定义一个 volatile 成员变量，用于保存最新的 RMI 地址（考虑到该变量或许会被其它线程所修改，一旦修改后，该变量的值会影响到所有线程）
    private volatile List<String> rmiUrlList = new ArrayList<String>();

    public ServiceConsumer() {
        ZooKeeper zk = ZooKeeperUtils.connectZooKeeper(this.latch);

        this.watchNodeInternal(zk); // 观察子节点,并更新本地服务列表
    }

    /**
     * 监听节点变化并更新本地服务列表
     * @param zk
     */
    private void watchNodeInternal(final ZooKeeper zk) {
        if (zk == null) {
            return;
        }

        try {
            List<String> nodeList = zk.getChildren(Constants.ZK_REGISTRY_PATH, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getType() == Event.EventType.NodeChildrenChanged) {
                        watchNodeInternal(zk); // 若子节点有变化，则重新调用该方法（为了获取最新子节点中的数据）
                    }
                }
            });

            List<String> dataList = new ArrayList<String>(); // 用于存放所有子节点数据
            for (String node : nodeList) {
                byte[] data = zk.getData(Constants.ZK_REGISTRY_PATH + "/" + node, false, null);
                dataList.add(new String(data));
            }

            LOG.info("Node data: {}", dataList);
            rmiUrlList = dataList;
        } catch (KeeperException ex) {
            LOG.error("Regist with error: ", ex);
        } catch (InterruptedException ex) {
            LOG.error("Regist with error: ", ex);
        }
    }

    /**
     * 查找RMI服务
     * @param <T>
     * @return
     */
    public <T extends Remote> T lookup() {
        T service = null;
        int size = this.rmiUrlList.size();

        // 获取URL
        if (size > 0) {
            String url = null;
            if (size == 1) {
                url = this.rmiUrlList.get(0); // 若只有一个元素,直接返回
                LOG.info("Use only url: {}", url);
            } else {
                url = this.rmiUrlList.get(ThreadLocalRandom.current().nextInt(size)); // 如果存在多个元素,随机获取
                LOG.info("Use random url: {}", url);
            }

            // 从JNDI中查找RMI服务
            service = this.lookupServiceInternal(url);
        }



        return service;
    }

    /**
     * 从JNDI中查找RMI服务
     * @param url
     * @param <T>
     * @return
     */
    private <T> T lookupServiceInternal(String url) {
        T remote = null;

        try {
            remote = (T) Naming.lookup(url);
        } catch (NotBoundException| MalformedURLException | RemoteException ex) {
            if (ex instanceof ConnectException) {
                // 若连接中断，则使用 urlList 中第一个 RMI 地址来查找（这是一种简单的重试方式，确保不会抛出异常）
                LOG.info("ConnectException -> url: {}", url);
                if (this.rmiUrlList.size() != 0) {
                    url = this.rmiUrlList.get(0);
                    return this.lookupServiceInternal(url);
                }
            }
            LOG.error("Lookup service with error: ", ex);
        }

        return remote;
    }
}
