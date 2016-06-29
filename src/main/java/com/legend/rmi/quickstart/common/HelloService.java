package com.legend.rmi.quickstart.common;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by allen on 6/28/16.
 */
public interface HelloService extends Remote {
    String sayHello(String name) throws RemoteException;
}
