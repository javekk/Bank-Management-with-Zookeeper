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


	/*
	 * Global variables and Constructor
	 */
	private ZooKeeper zookeeper;
	private Bank bank;

	/**
	 * Constructor.
	 * @param zk Zookeeper instance to be used for coordinating bank messages.
	 * @param bank The bank instance that is responsible for handling messages.
	 */
	public SendMessagesBank(ZooKeeper zk, Bank bank){
		this.zookeeper = zk;
		this.bank = bank;
	}

	/**
	 * Responsible for deciding if the operation should be sent to every follower or the leader.
	 * @param operation The operation to be sent.
	 * @param isLeader Boolean value that specifies if the sender is a leader or not.
	 */
	private void sendMessage(OperationBank operation, boolean isLeader) {
		if (isLeader){
			operationToFollowers(operation);
		} else {
			operationToLeader(operation);
		}
	}

	/**
	 * Responsible for sending a given operation to every follower.
	 * @param operation The operation to be sent.
	 */
	private void operationToLeader(OperationBank operation) {

		byte[] operationBytes = new byte[0];
		try {
			operationBytes = OperationBank.objToByte(operation);
		} catch (IOException e) {
			e.printStackTrace();
		}

		/* In order to get the operation node name of the leader, first we need to check what is the name of
		* the child node under the /elections node created by the leader, since it contains the operation node name
		* as its data. */

		/* TODO: I think we made a mistake here, this should be rootElections
		* We stored the operation node name as data of the election node of the leader. We get the operation node name
		* back using the corresponding function. */
		String leaderElectionNodeName = NodeManager.rootOperations + "/" + this.bank.getLeader();
		try {
			String leaderOperationNodeName = NodeManager.getLeaderOptNodeName(zookeeper, leaderElectionNodeName);
			zookeeper.create(leaderOperationNodeName + "/", operationBytes,
					ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
		} catch (KeeperException | InterruptedException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Responsible for sending an operation to a specific node.
	 * @param operation The operation to be sent.
	 * @param nodePath The path to the specific recipient node.
	 */
	public void operationToNode(OperationBank operation, String nodePath) {

		System.out.println("Forward to node: " + nodePath);

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

	/**
	 * Responsible for letting every follower know of an operation.
	 * @param operation The operation to be dispatched.
	 */
	public void operationToFollowers(OperationBank operation) {

		System.out.println("Forward operation to followers: " + operation);

		List<String> operationNodes = null;
		try {
			operationNodes = zookeeper.getChildren(NodeManager.rootOperations, false);
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

			// The leader is the sender in this case, no need to send it the operation.
			String leaderElectionNodeName = NodeManager.rootOperations + "/" + this.bank.getLeader();
			try {
				String leaderOperationNodeName = NodeManager.getLeaderOptNodeName(zookeeper, leaderElectionNodeName);
				if (!operation_node_id.equals(leaderOperationNodeName)) {
					zookeeper.create(NodeManager.rootOperations + "/" + operation_node_id + "/", operationBytes,
							ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
				}
			} catch (KeeperException | InterruptedException | UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Responsible for sending an operation that adds the attached client to the DB.
	 * @param client The client object to be added.
	 * @param isLeader Boolean value that specifies if the sender is the leader or not.
	 */
	public void sendAdd(Client client, boolean isLeader) {
		OperationBank operation = new OperationBank(OperationEnum.CREATE_CLIENT, client);
		if (isLeader) this.bank.handleIncomingMsg(operation);
		sendMessage(operation, isLeader);
	}

	/**
	 * Responsible for sending an operation that modifies the data of a client in the DB.
	 * @param client The client object containing the updated client data.
	 * @param isLeader Boolean value that specifies if the sender is the leader or not.
	 */
	public void sendUpdate(Client client, boolean isLeader) {
		OperationBank operation = new OperationBank(OperationEnum.UPDATE_CLIENT, client);
		if (isLeader) this.bank.handleIncomingMsg(operation);
		sendMessage(operation, isLeader);
	}

	/**
	 * Responsible for sending an operation that deletes a client from the DB.
	 * @param accountNumber The account number belonging to the client whose account is to be deleted.
	 * @param isLeader Boolean value that specifies if the sender is the leader or not.
	 */
	public void sendDelete(Integer accountNumber, boolean isLeader) {
		OperationBank operation = new OperationBank(OperationEnum.DELETE_CLIENT, accountNumber);
		if (isLeader) this.bank.handleIncomingMsg(operation);
		sendMessage(operation, isLeader);
	}

	/**
	 * TODO:
	 * @param clientDB
	 * @param isLeader Boolean value that specifies if the sender is the leader or not.
	 */
	public void sendCreateBank (ClientDB clientDB, boolean isLeader) {

		// TODO only send to new connected server

		OperationBank operation = new OperationBank(OperationEnum.CREATE_BANK, clientDB);
		sendMessage(operation, isLeader);
	}
}
