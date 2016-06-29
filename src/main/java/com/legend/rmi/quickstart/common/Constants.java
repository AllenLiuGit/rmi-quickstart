package com.legend.rmi.quickstart.common;

/**
 * Created by allen on 6/28/16.
 */
public interface Constants {
    String ZK_CONNECT_STRING = "localhost:2181";
    int ZK_SESSION_TIMEOUT = 5000; // In milliseconds

    /**
     * $ bin/zkCli.sh -server localhost:2181
     * > create /rmi null
     * > create /rmi/registry null
     * > ls /
     */
    String ZK_REGISTRY_PATH = "/rmi/registry";
    String ZK_PROVIDER_PATH = ZK_REGISTRY_PATH + "/provider";
}
