package eu.upm.adic.node;

import eu.upm.adic.watcher.ElectionWatcher;
import eu.upm.adic.watcher.NodeDownWatcher;
import eu.upm.adic.Bank;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ElectionManager {

    private ZooKeeper zk;
    private Bank bank;

    public static String root = "/election";
    private String prefix = "node-";

    public ElectionManager(ZooKeeper zkInstance, Bank bankInstance){
        this.zk = zkInstance;
        this.bank = bankInstance;
    }

    public String createElectionNode() throws KeeperException, InterruptedException {

        Utilities.existsOrCreateZnode(zk, root, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

        return Utilities.existsOrCreateZnode(zk, root + "/" + prefix, new byte[0],
                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL_SEQUENTIAL);
    }

    public void leaderElection() throws KeeperException, InterruptedException {

        List<String> nodes = zk.getChildren(root, false);
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
        if(leader.equals(this.bank.getElectionNodeName().replace(root + "/", ""))){
            this.bank.setIsLeader(true);
            System.out.println("****You are the leader****");

//            this.bank.sendCreateBank();

            NodeDownWatcher nodeDownWatcher = new NodeDownWatcher();
            for (String node_id : nodes) {
                System.out.println("Node id: " + root + "/" + node_id);
                zk.exists(root + "/" + node_id, nodeDownWatcher);
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
            zk.exists(root + "/" + leaderNode, electionWatcher);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
