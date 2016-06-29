package com.legend.rmi.quickstart.server;

import com.legend.rmi.quickstart.common.Constants;
import com.legend.rmi.quickstart.common.ZooKeeperUtils;
import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.concurrent.CountDownLatch;

/**
 * Created by allen on 6/28/16.
 */
public class ServiceProvider {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceProvider.class);

    // 用于等待SyncConnected事件触发后,继续执行当前线程
    private CountDownLatch latch = new CountDownLatch(1);

    /**
     * 发布RMI服务,并将将RMI地址注册到ZooKeeper中
     * @param remote
     * @param host
     * @param port
     */
    public void publish(Remote remote, String host, int port) {
        String url = this.publishService(remote, host, port); // 发布RMI服务,并返回RMI地址
        this.registService(url); // 将RMI地址注册到ZooKeeper中
    }

    /**
     * 发布RMI服务
     * @param remote
     * @param host
     * @param port
     * @return
     */
    private String publishService(Remote remote, String host, int port) {
        String url = null;

        try {
            url = String.format("rmi://%s:%d/%s", host, port, remote.getClass().getName());
            LocateRegistry.createRegistry(port);
            Naming.rebind(url, remote);
            LOG.info("Publish rmi service (url: {})", url);
        } catch (MalformedURLException ex) {
            LOG.error("Publish service with error", ex);
        } catch (RemoteException ex) {
            LOG.error("Publish service with error", ex);
        }

        return url;
    }

    /**
     * 注册RMI地址到ZooKeeper中
     * @param url
     */
    private void registService(String url) {
        if (url == null) {
            return;
        }

        // Connect
        ZooKeeper zk = ZooKeeperUtils.connectZooKeeper(this.latch); // 连接ZooKeeper服务器,并返回ZooKeeper对象

        // Regist internal
        this.registServiceInternal(zk, url); // 创建ZNode并将RMI地址放入ZNode上
    }

    /**
     * 将RMI地址注册到ZooKeeper上
     * @param zk
     * @param url
     */
    private void registServiceInternal(ZooKeeper zk, String url) {
        if (zk == null || url == null || url.trim().length() == 0) {
            return;
        }

        try {
            byte[] data = url.getBytes();
            String path = zk.create(Constants.ZK_PROVIDER_PATH, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL); // 创建一个临时且有序的ZNode节点
        } catch (KeeperException ex) {
            LOG.error("Regist with error: ", ex);
        } catch (InterruptedException ex) {
            LOG.error("Regist with error: ", ex);
        }

    }
}
