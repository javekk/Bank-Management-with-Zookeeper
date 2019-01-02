package eu.upm.adic.watcher;

import eu.upm.adic.Bank;
import eu.upm.adic.operation.OperationBank;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.List;


public class OperationWatcher implements Watcher {

    private ZooKeeper zk;
    private Bank bank;
    private String nodename;

    public OperationWatcher(ZooKeeper zk, String nodename, Bank bankInstance){
        this.zk = zk;
        this.bank = bankInstance;
        this.nodename = nodename;
    }
    @Override
    public void process(WatchedEvent event) {
        if (event.getPath().equals(this.nodename)) {
            List<String> operations = null;
            try {
                operations = zk.getChildren(this.nodename, false);
            } catch (KeeperException | InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("Operations: " + operations);

            for (String operation_id : operations) {
                String nodePath = this.nodename + "/" + operation_id;
                byte[] data = null;
                try {
                    data = zk.getData(nodePath, false, null);
                    Stat stat = zk.exists(nodePath, false);
                    zk.delete(nodePath, stat.getVersion());
                } catch (KeeperException | InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    OperationBank operation = null;
                    try {
                        operation = OperationBank.byteToObj(data);
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }

                    bank.handleReceiverMsg(operation);
                    if (this.bank.getIsLeader()) this.bank.sendMessages.operationToFollowers(operation);
                }
            }
        }
        try {
            zk.getChildren(this.nodename, this);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
