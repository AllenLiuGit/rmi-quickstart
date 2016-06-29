package com.legend.rmi.quickstart.client;

import com.legend.rmi.quickstart.common.HelloService;

/**
 * Created by allen on 6/28/16.
 */
public class Client {
    public static void main(String[] args) throws Exception {
        ServiceConsumer consumer = new ServiceConsumer();

        while (true) {
            HelloService helloService = consumer.lookup();
            String result = helloService.sayHello("Jack");
            System.out.println(result);
            Thread.sleep(3000);
        }
    }
}
