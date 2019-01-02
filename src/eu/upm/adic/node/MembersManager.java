package eu.upm.adic.node;

import eu.upm.adic.watcher.NewNodeWatcher;
import eu.upm.adic.Bank;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;


public class MembersManager {

    private ZooKeeper zk;
    private Bank bank;

    public static String root = "/members";
    public static String prefix = "node-";

    public MembersManager(ZooKeeper zkInstance, Bank bankInstance){
        this.zk = zkInstance;
        this.bank = bankInstance;
    }

    public String createBaseNodes() throws KeeperException, InterruptedException {

        Utilities.existsOrCreateZnode(zk, root, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.PERSISTENT);

        return Utilities.existsOrCreateZnode(zk, root + "/" + prefix, new byte[0],
                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL_SEQUENTIAL);

    }

    public void listenForFollowingNode(String currentNodeId){
        String numeric_part = currentNodeId.replace(root + "/" + prefix, "");
        int id_int = Integer.parseInt(numeric_part);
        int next_member = id_int + 1;

        NewNodeWatcher newNodeWatcher = new NewNodeWatcher(zk, bank);
        try {
            zk.exists(root + "/" + prefix + String.format("%010d", next_member), newNodeWatcher);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
