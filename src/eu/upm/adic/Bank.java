package eu.upm.adic;

import eu.upm.adic.node.NodeManager;
import eu.upm.adic.operation.OperationBank;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

public class Bank {

	/*
	 *   General parameters
	 */
	private ClientDB clientDB; //Database with all the clients
	public SendMessagesBank sendMessagesBank;
	public ZooKeeper zookeeper;
	private String leader;

	private boolean isLeader = false;

	// Election
	private NodeManager nodeManager;
	private String electionNodeName;
	private String memberNodeName;


	// Operations
	public String operationNodeName;


	public Bank(ZooKeeper zookeeper) throws KeeperException, InterruptedException {

		this.zookeeper = zookeeper;

		nodeManager = new NodeManager(zookeeper, this);
		this.electionNodeName = nodeManager.createElectionNode();

		this.operationNodeName = nodeManager.createOperationsNode();

		this.memberNodeName = nodeManager.createBaseNodes();
		Stat stat = new Stat();

		zookeeper.setData(memberNodeName, this.operationNodeName.getBytes(), stat.getVersion());

		Thread.sleep(1000);

		this.clientDB = new ClientDB();

		nodeManager.leaderElection();
		nodeManager.listenForFollowingNode(memberNodeName);
		// Set a watcher for operations
		nodeManager.listenForOperationUpdates(this, this.operationNodeName);

		// We set as data for the electionNodeName the operationNodeName.
		// In this way we know who is the leader and its operationNodeName, so that followers
		// can forward the operations to the leader (which will then dispatch them to everyone).
		stat = new Stat();
		zookeeper.setData(this.electionNodeName, this.operationNodeName.getBytes(), stat.getVersion());

		this.sendMessagesBank = new SendMessagesBank(zookeeper, this);
	}

	public synchronized void handleIncomingMsg(OperationBank operation) {
		switch (operation.getOperation()) {
			case CREATE_CLIENT:
				clientDB.createClient(operation.getClient());
				break;
			case READ_CLIENT:
				clientDB.readClient(operation.getAccountNumber());
				break;
			case UPDATE_CLIENT:
				clientDB.updateClient(operation.getClient().getAccountNumber(),
									  operation.getClient().getBalance());
				break;
			case DELETE_CLIENT:
				clientDB.deleteClient(operation.getAccountNumber());
				break;
			case CREATE_BANK:
				clientDB.createBank(operation.getClientDB());
				break;
		}
	}

	public void createClient(Client client) {
		sendMessagesBank.sendAdd(client, isLeader);
	}

	public Client readClient(Integer accountNumber) {
		// Handled locally. No need for distributing
		return clientDB.readClient(accountNumber);
	}

	public void updateClient (int accNumber, int balance) {
		Client client = clientDB.readClient(accNumber);
		client.setBalance(balance);
		sendMessagesBank.sendUpdate(client, isLeader);
	}

	public void deleteClient(Integer accountNumber) {
		sendMessagesBank.sendDelete(accountNumber, isLeader);
	}

	public void sendCreateBank(){
		sendMessagesBank.sendCreateBank(clientDB, isLeader);
	}


	/*
	 * GETTER and SETTER
	 *
	 */
	public boolean getIsLeader(){
		return isLeader;
	}

	public void setIsLeader(boolean isLeader){
		this.isLeader = isLeader;
	}

	public String getElectionNodeName(){
		return this.electionNodeName;
	}

	public void setElectionNodeName(String electionNodeName){
		this.electionNodeName = electionNodeName;
	}

	public String getLeader() {
		return leader;
	}

	public void setLeader(String leader) {
		this.leader = leader;
	}

	public ClientDB getClientDB() {
		return clientDB;
	}

	public void setClientDB(ClientDB clientDB) {
		this.clientDB = clientDB;
	}

	public String toString() {
		return clientDB.toString();
	}

	public void close() {
		System.out.println("Session finished");
	}
}
