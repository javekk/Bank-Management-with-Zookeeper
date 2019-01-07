package eu.upm.adic;

import eu.upm.adic.node.NodeManager;
import eu.upm.adic.operation.OperationBank;
import eu.upm.adic.operation.OperationEnum;
import org.apache.zookeeper.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;

public class SendMessagesBank implements SendMessages {

	private ZooKeeper zookeeper;
	private Bank bank;

	public SendMessagesBank(ZooKeeper zk, Bank bank){
		this.zookeeper = zk;
		this.bank = bank;
	}

	private void sendMessage(OperationBank operation, boolean isLeader) {
		if (isLeader){
			operationToFollowers(operation);
		} else {
			operationToLeader(operation);
		}
	}

	private void operationToLeader(OperationBank operation) {
		byte[] operationBytes = new byte[0];
		try {
			operationBytes = OperationBank.objToByte(operation);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Get leader operationNodeName which is stored as data in the electionNodeName of the leader
		String leaderElectionNodeName = NodeManager.root + "/" + this.bank.getLeader();
		try {
			String leaderOperationNodeName = Utilities.getLeaderOptNodeName(zookeeper, leaderElectionNodeName);
			zookeeper.create(leaderOperationNodeName + "/", operationBytes,
					ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
		} catch (KeeperException | InterruptedException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public void operationToNode(OperationBank operation, String nodePath) {

		System.out.println("FOward to node: " + nodePath);

		byte[] operationBytes = new byte[0];
		try {
			operationBytes = OperationBank.objToByte(operation);
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			zookeeper.create(nodePath + "/", operationBytes,
					ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
		} catch (KeeperException | InterruptedException e) {
			e.printStackTrace();
		}

	}

	public void operationToFollowers(OperationBank operation) {

		System.out.println("forward operation to followers: " + operation);

		List<String> operationNodes = null;
		try {
			operationNodes = zookeeper.getChildren(NodeManager.root, false);
		} catch (KeeperException | InterruptedException e) {
			e.printStackTrace();
		}

		byte[] operationBytes = new byte[0];
		try {
			operationBytes = OperationBank.objToByte(operation);
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (Iterator iterator = operationNodes.iterator(); iterator.hasNext(); ) {
			String operation_node_id = (String) iterator.next();

			// Do not send the update to the leader (itself) again
			String leaderElectionNodeName = NodeManager.root + "/" + this.bank.getLeader();
			try {
				String leaderOperationNodeName = Utilities.getLeaderOptNodeName(zookeeper, leaderElectionNodeName);
				if (!operation_node_id.equals(leaderOperationNodeName)) {
					zookeeper.create(NodeManager.root + "/" + operation_node_id + "/", operationBytes,
							ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
				}
			} catch (KeeperException | InterruptedException | UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
	}

	public void sendAdd(Client client, boolean isLeader) {
		OperationBank operation = new OperationBank(OperationEnum.CREATE_CLIENT, client);
		if (isLeader) this.bank.handleIncomingMsg(operation);
		sendMessage(operation, isLeader);
	}

	public void sendUpdate(Client client, boolean isLeader) {
		OperationBank operation = new OperationBank(OperationEnum.UPDATE_CLIENT, client);
		if (isLeader) this.bank.handleIncomingMsg(operation);
		sendMessage(operation, isLeader);
	}

	public void sendDelete(Integer accountNumber, boolean isLeader) {
		OperationBank operation = new OperationBank(OperationEnum.DELETE_CLIENT, accountNumber);
		if (isLeader) this.bank.handleIncomingMsg(operation);
		sendMessage(operation, isLeader);
	}

	public void sendCreateBank (ClientDB clientDB, boolean isLeader) {

		// TODO only send to new connected server

		OperationBank operation = new OperationBank(OperationEnum.CREATE_BANK, clientDB);
		sendMessage(operation, isLeader);
	}
}
