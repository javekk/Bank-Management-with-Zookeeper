package eu.upm.adic.watcher;

import eu.upm.adic.node.NodeManager;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

/**
 * A watcher class for the /election znode.
 * This watcher is triggered when the leader node fails
 * or exits. A new election is then triggered.
 */
public class ElectionWatcher implements Watcher {

    private NodeManager nodeManager;

    public ElectionWatcher(NodeManager nodeManager){
        this.nodeManager = nodeManager;
    }

    @Override
    public void process(WatchedEvent event) {
        try {
            nodeManager.leaderElection();
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
