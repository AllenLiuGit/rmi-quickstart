package com.legend.rmi.quickstart.server;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

/**
 * Created by allen on 6/28/16.
 */
public class RmiServer {
    private static final int PORT = 1099;
    public static void main(String[] args) throws Exception {
        String url = "rmi://localhost:1099/com.legend.rmi.quickstart.server.HelloServiceImpl";
        LocateRegistry.createRegistry(PORT); // JNDI中创建注册表项
        Naming.rebind(url, new HelloServiceImpl()); // 绑定RMI地址和RMI服务实现类
    }
}
