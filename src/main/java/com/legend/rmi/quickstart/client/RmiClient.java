package com.legend.rmi.quickstart.client;

import com.legend.rmi.quickstart.common.HelloService;

import java.rmi.Naming;

/**
 * Created by allen on 6/28/16.
 */
public class RmiClient {
    public static void main(String[] args) throws Exception {
        String url = "rmi://localhost:1099/com.legend.rmi.quickstart.server.HelloServiceImpl";

        HelloService helloService = (HelloService) Naming.lookup(url);
        String result = helloService.sayHello("Allen");
        System.out.println(result);
    }
}
