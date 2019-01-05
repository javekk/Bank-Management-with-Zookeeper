package eu.upm.adic.operation;

import eu.upm.adic.Utilities;
import eu.upm.adic.watcher.OperationWatcher;
import eu.upm.adic.Bank;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

public class OperationManager {

    private ZooKeeper zk;

    public static String root = "/operations";
    public static String prefix = "node-";


    public OperationManager(ZooKeeper zkInstance){
        this.zk = zkInstance;
    }


}
