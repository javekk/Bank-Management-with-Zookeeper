package eu.upm.adic.node;

import eu.upm.adic.watcher.OperationWatcher;
import eu.upm.adic.Bank;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

public class OperationsManager {

    private ZooKeeper zk;

    public static String root = "/operations";
    public static String prefix = "node-";


    public OperationsManager(ZooKeeper zkInstance){
        this.zk = zkInstance;
    }

    public String createOperationsNode() throws KeeperException, InterruptedException {

        Utilities.existsOrCreateZnode(zk, root, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.PERSISTENT);

        return Utilities.existsOrCreateZnode(zk, root + "/" + prefix, new byte[0],
                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.PERSISTENT_SEQUENTIAL);

    }

    public void listenForOperationUpdates(Bank bankInstance, String nodeName){
        OperationWatcher operationWatcher = new OperationWatcher(this.zk, nodeName, bankInstance);
        try {
            this.zk.getChildren(nodeName, operationWatcher);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
