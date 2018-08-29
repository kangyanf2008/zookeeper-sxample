package com.kyf.server;

import com.kyf.server.utils.ServerInterfaceUtil;
import com.kyf.server.utils.ZooKeeperUtils;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.ACL;

public class ServerBootStrap {
    public static void main(String[] args) {
        String applicationName  = "/server" ;
        int serverPort = 65532;
        //开启服务地址
        String serverAddress  = "192.168.30.19:"+serverPort ;
        //zk连接地址
        String connectString    = "192.168.30.19:2181";
        int sessionTimeout      = 20000;
        Watcher watcher         = new Watcher() {
            @Override
            public void process(WatchedEvent event) {
System.out.println(event.getType() + "    "+ event.getState());
            }};

        ZooKeeperUtils.initZooKeeperCfg(connectString, sessionTimeout, null);
        ZooKeeper zk = ZooKeeperUtils.getZooKeeper();

        try {
           if(zk.exists(applicationName, true) == null){
                zk.create(applicationName, null ,ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT );
           }
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            //开启服务
            ServerInterfaceUtil.publicServer(serverPort);

            //向注册中心注册服务
            if(zk.exists(applicationName + "/" + serverAddress, true) == null){
                zk.create(applicationName + "/" + serverAddress, null ,ZooDefs.Ids.READ_ACL_UNSAFE, CreateMode.EPHEMERAL );
            }
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
       synchronized (ServerBootStrap.class) {
            while (true) {
                try {
                    System.out.printf("server run finished! serverPort:" + serverPort);
                    ServerBootStrap.class.wait();
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(-1);
                }
            }
        }
    }
}
