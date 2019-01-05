package eu.upm.adic.node;

import eu.upm.adic.Utilities;
import eu.upm.adic.watcher.ElectionWatcher;
import eu.upm.adic.watcher.NodeCreatedWatcher;
import eu.upm.adic.watcher.NodeCrashedWatcher;
import eu.upm.adic.Bank;
import eu.upm.adic.watcher.OperationWatcher;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class NodeManager {

    private ZooKeeper zookeeper;
    private Bank bank;

    public static String rootElection = "/elections";
    public static String rootMembers = "/members";
    public static String root = "/operations";


    private String prefix = "node-";

    public NodeManager(ZooKeeper zkInstance, Bank bankInstance){
        this.zookeeper = zkInstance;
        this.bank = bankInstance;
    }


    /**
     *          _                 _     _
     *    ___  | |   ___    ___  | |_  (_)   ___    _ __    ___
     *   / _ \ | |  / _ \  / __| | __| | |  / _ \  | '_ \  / __|
     *  |  __/ | | |  __/ | (__  | |_  | | | (_) | | | | | \__ \
     *   \___| |_|  \___|  \___|  \__| |_|  \___/  |_| |_| |___/
     */

    public String createElectionNode() throws KeeperException, InterruptedException {

        Utilities.existsOrCreateZnode(zookeeper, rootElection, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

        return Utilities.existsOrCreateZnode(zookeeper, rootElection + "/" + prefix, new byte[0],
                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL_SEQUENTIAL);
    }

    public void leaderElection() throws KeeperException, InterruptedException {

        List<String> nodes = zookeeper.getChildren(rootElection, false);
        int r = new Random().nextInt(100);
        // Loop for rand iterations
        // to wait that a few nodes join
        for (int i = 0; i < r; i++) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {

            }
        }

        Collections.sort(nodes);
        String leader = nodes.get(0);
        this.bank.setLeader(leader);
        if(leader.equals(this.bank.getElectionNodeName().replace(rootElection + "/", ""))){
            this.bank.setIsLeader(true);
            System.out.println("****You are the leader****");

//            this.bank.sendCreateBank();

            NodeCrashedWatcher nodeCrashedWatcher = new NodeCrashedWatcher();
            for (String node_id : nodes) {
                System.out.println("Node id: " + rootElection + "/" + node_id);
                zookeeper.exists(rootElection + "/" + node_id, nodeCrashedWatcher);
            }

        } else {
            this.bank.setIsLeader(false);
            System.out.println("The process " + leader + " is the leader");
            listenForLeaderNode(leader);
        }
    }

    private void listenForLeaderNode(String leaderNode){
        ElectionWatcher electionWatcher = new ElectionWatcher(this);
        try {
            zookeeper.exists(rootElection + "/" + leaderNode, electionWatcher);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     *                                  _
     *   _ __ ___     ___   _ __ ___   | |__     ___   _ __   ___
     *  | '_ ` _ \   / _ \ | '_ ` _ \  | '_ \   / _ \ | '__| / __|
     *  | | | | | | |  __/ | | | | | | | |_) | |  __/ | |    \__ \
     *  |_| |_| |_|  \___| |_| |_| |_| |_.__/   \___| |_|    |___/
     */

    public String createBaseNodes() throws KeeperException, InterruptedException {

        Utilities.existsOrCreateZnode(zookeeper, rootMembers, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.PERSISTENT);

        return Utilities.existsOrCreateZnode(zookeeper, rootMembers + "/" + prefix, new byte[0],
                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL_SEQUENTIAL);

    }

    public void listenForFollowingNode(String currentNodeId){
        String numeric_part = currentNodeId.replace(rootMembers + "/" + prefix, "");
        int id_int = Integer.parseInt(numeric_part);
        int next_member = id_int + 1;

        NodeCreatedWatcher nodeCreatedWatcher = new NodeCreatedWatcher(zookeeper, bank);
        try {
            zookeeper.exists(rootMembers + "/" + prefix + String.format("%010d", next_member), nodeCreatedWatcher);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     *                                         _     _
     *    ___    _ __     ___   _ __    __ _  | |_  (_)   ___    _ __
     *   / _ \  | '_ \   / _ \ | '__|  / _` | | __| | |  / _ \  | '_ \
     *  | (_) | | |_) | |  __/ | |    | (_| | | |_  | | | (_) | | | | |
     *   \___/  | .__/   \___| |_|     \__,_|  \__| |_|  \___/  |_| |_|
     *          |_|
     */


    public String createOperationsNode() throws KeeperException, InterruptedException {

        Utilities.existsOrCreateZnode(this.zookeeper, root, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.PERSISTENT);

        return Utilities.existsOrCreateZnode(this.zookeeper, root + "/" + prefix, new byte[0],
                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.PERSISTENT_SEQUENTIAL);

    }

    public void listenForOperationUpdates(Bank bankInstance, String nodeName){
        OperationWatcher operationWatcher = new OperationWatcher(this.zookeeper, nodeName, bankInstance);
        try {
            this.zookeeper.getChildren(nodeName, operationWatcher);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
