package eu.upm.adic.node;

import eu.upm.adic.watcher.ElectionWatcher;
import eu.upm.adic.watcher.NodeCreatedWatcher;
import eu.upm.adic.watcher.NodeCrashedWatcher;
import eu.upm.adic.Bank;
import eu.upm.adic.watcher.OperationWatcher;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class NodeManager {

    private ZooKeeper zookeeper;
    private Bank bank;

    public static String rootElections = "/elections";
    public static String rootMembers = "/members";
    public static String rootOperations = "/operations";

    private String prefix = "node-";

    /**
     * Constructor.
     * @param zkInstance Zookeeper instance to be used to coordinate operations related to the bank instance.
     * @param bankInstance Bank object.
     */
    public NodeManager(ZooKeeper zkInstance, Bank bankInstance){
        this.zookeeper = zkInstance;
        this.bank = bankInstance;
    }


    /*
     *          _                 _     _
     *    ___  | |   ___    ___  | |_  (_)   ___    _ __    ___
     *   / _ \ | |  / _ \  / __| | __| | |  / _ \  | '_ \  / __|
     *  |  __/ | | |  __/ | (__  | |_  | | | (_) | | | | | \__ \
     *   \___| |_|  \___|  \___|  \__| |_|  \___/  |_| |_| |___/
     */

    /**
     * Responsible for the creation of the /elections root node and child node for the client.
     * @return The name of the child node.
     * @throws KeeperException
     * @throws InterruptedException
     */
    public String createElectionNode() throws KeeperException, InterruptedException {

        NodeManager.existsOrCreateZnode(zookeeper, rootElections, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

        return NodeManager.existsOrCreateZnode(zookeeper, rootElections + "/" + prefix, new byte[0],
                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL_SEQUENTIAL);
    }


    /**
     * Responsible for managing the election of a leader. It verifies if the current server is the leader or not.
     * If not, it stores the identity of the leader server.
     * @throws KeeperException
     * @throws InterruptedException
     */
    public void leaderElection() throws KeeperException, InterruptedException {

        List<String> nodes = zookeeper.getChildren(rootElections, false);

        /* wait for nodes join*/
        try {
            Thread.sleep(new Random().nextInt(100));
        } catch (InterruptedException e) {
                e.printStackTrace();
            }

        Collections.sort(nodes);
        String leader = nodes.get(0); //the first one is the leader
        this.bank.setLeader(leader);

        if(leader.equals(this.bank.getElectionNodeName().replace(rootElections + "/", ""))){

            this.bank.setIsLeader(true);
            System.out.println("-> Your server is the leader ");

            NodeCrashedWatcher nodeCrashedWatcher = new NodeCrashedWatcher(); //set watcher for crashes

            for (String node_id : nodes) {
                System.out.println("Node id: " + rootElections + "/" + node_id);
                zookeeper.exists(rootElections + "/" + node_id, nodeCrashedWatcher);
            }

        } else {

            this.bank.setIsLeader(false);
            System.out.println("Process " + leader + " is our leader");
            listenForLeaderNode(leader);
        }
    }

    /**
     * Responsible for creating a watch for the leader's node under the election root node.
     * @param leaderNode Name of the leader node.
     */
    private void listenForLeaderNode(String leaderNode){
        ElectionWatcher electionWatcher = new ElectionWatcher(this);
        try {
            zookeeper.exists(rootElections + "/" + leaderNode, electionWatcher);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    /*
     *                                  _
     *   _ __ ___     ___   _ __ ___   | |__     ___   _ __   ___
     *  | '_ ` _ \   / _ \ | '_ ` _ \  | '_ \   / _ \ | '__| / __|
     *  | | | | | | |  __/ | | | | | | | |_) | |  __/ | |    \__ \
     *  |_| |_| |_|  \___| |_| |_| |_| |_.__/   \___| |_|    |___/
     */

    /**
     * Responsible for the creation of the /members root node and child node for the client.
     * @return The name of the child node.
     * @throws KeeperException
     * @throws InterruptedException
     */
    public String createBaseNode() throws KeeperException, InterruptedException {

        NodeManager.existsOrCreateZnode(zookeeper, rootMembers, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.PERSISTENT);

        return NodeManager.existsOrCreateZnode(zookeeper, rootMembers + "/" + prefix, new byte[0],
                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL_SEQUENTIAL);

    }

    /**
     * Responsible for creating a watch for the upcoming joiners.
     * @param currentNodeId THe name of the current node.
     */
    public void listenForJoiningNodes(String currentNodeId){

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


    /**
     * Responsible for the creation of the /operations root node and child node for the client.
     * @return The name of the child node.
     * @throws KeeperException
     * @throws InterruptedException
     */
    public String createOperationsNode() throws KeeperException, InterruptedException {

        NodeManager.existsOrCreateZnode(this.zookeeper, rootOperations, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.PERSISTENT);

        return NodeManager.existsOrCreateZnode(this.zookeeper, rootOperations + "/" + prefix, new byte[0],
                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.PERSISTENT_SEQUENTIAL);
    }

    /**
     * Responsible for the creation of a watch for the corresponding operations node.
     * @param bankInstance The bank object.
     * @param nodeName The name of the current node.
     */
    public void listenForOperationUpdates(Bank bankInstance, String nodeName){
        OperationWatcher operationWatcher = new OperationWatcher(this.zookeeper, nodeName, bankInstance);
        try {
            this.zookeeper.getChildren(nodeName, operationWatcher);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     *           _     _   _   _   _     _
     *   _   _  | |_  (_) | | (_) | |_  (_)   ___   ___
     *  | | | | | __| | | | | | | | __| | |  / _ \ / __|
     *  | |_| | | |_  | | | | | | | |_  | | |  __/ \__ \
     *   \__,_|  \__| |_| |_| |_|  \__| |_|  \___| |___/
     *
     */

    /**
     * Checks if a particular node exists. If it does not, it creates it.
     * @param zookeeper Zookeeper instance.
     * @param path Path for the specific node.
     * @param data Data to be added when the node is created.
     * @param ACL ACL list.
     * @param createMode Creation mode.
     * @return Name of the node.
     * @throws KeeperException
     * @throws InterruptedException
     */
    public static String existsOrCreateZnode(ZooKeeper zookeeper, String path, byte[] data, List<ACL> ACL, CreateMode createMode) throws KeeperException, InterruptedException {
        Stat stat = zookeeper.exists(path, false);
        String nodename = null;
        if (stat == null){
            nodename = zookeeper.create(path, data, ACL, createMode);
        }
        return nodename;
    }

    /**
     * Responsible for returning the operations node belonging to the leader.
     * This is needed for determining where the followers can put the operations that they need to send to the leader.
     * @param zookeeper Zookeeper instance.
     * @param leaderElectionNodeName The name of the leader election node that stores the name of the leader operations node.
     * @return Name of the leader operations node.
     * @throws KeeperException
     * @throws InterruptedException
     * @throws UnsupportedEncodingException
     */
    public static String getLeaderOptNodeName(ZooKeeper zookeeper, String leaderElectionNodeName) throws KeeperException, InterruptedException, UnsupportedEncodingException {

        String leaderOperationNodeName = null;
        Stat stat = zookeeper.exists(leaderElectionNodeName, false);
        leaderOperationNodeName = new String(zookeeper.getData(leaderElectionNodeName, false, stat), "UTF-8");

        return leaderOperationNodeName;
    }


}
