package eu.upm.adic.watcher;

import eu.upm.adic.Bank;
import eu.upm.adic.Client;
import eu.upm.adic.operation.OperationBank;
import eu.upm.adic.operation.OperationEnum;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.io.UnsupportedEncodingException;

public class NodeCreatedWatcher implements Watcher {

    private ZooKeeper zookeeper;
    private Bank bank;

    public NodeCreatedWatcher(ZooKeeper zkInstance, Bank bank){
        this.zookeeper = zkInstance;
        this.bank = bank;
    }

    @Override
    public void process(WatchedEvent event) {
        System.out.println("New node: " + event.getPath());
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Stat stat;
        String operationNodeName = null;
        try {
            stat = zookeeper.exists(event.getPath(), false);
            operationNodeName = new String(zookeeper.getData(event.getPath(), false, stat), "UTF-8");
        } catch (KeeperException | InterruptedException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // send current state (db) to the new node
        for (java.util.HashMap.Entry <Integer, Client>  entry : bank.getClientDB().clientDB.entrySet()) {
            Client c = entry.getValue();

            System.out.println("Customer: " + c);

            bank.sendMessagesBank.operationToNode(new OperationBank(
                        OperationEnum.CREATE_CLIENT,
                        new Client(c.getAccountNumber(), c.getName(), c.getBalance())
                    ), operationNodeName);
        }

        NodeCrashedWatcher nodeCrashedWatcher = new NodeCrashedWatcher();
        String nodeId = event.getPath();
        try {
            zookeeper.exists(nodeId, nodeCrashedWatcher);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
