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


	private NodeManager nodeManager;

	private String electionNodeName; // Election
	private String memberNodeName; //members
	public String operationNodeName; // Operations

	/**
	 * Constructor.
	 * @param zookeeper Zookeeper instance to be used to coordinate operations related to the bank instance.
	 * @throws KeeperException
	 * @throws InterruptedException
	 */
	public Bank(ZooKeeper zookeeper) throws KeeperException, InterruptedException {
		this.zookeeper = zookeeper;

		nodeManager = new NodeManager(zookeeper, this);

		// Creating nodes belonging to this instance. These are used to track elections,
		// operations and alive members of the cluster
		this.electionNodeName = nodeManager.createElectionNode();
		this.operationNodeName = nodeManager.createOperationsNode();
		this.memberNodeName = nodeManager.createBaseNode();

		// Linking the member node to the corresponding operation node by saving its reference.
		Stat stat = new Stat();
		zookeeper.setData(memberNodeName, this.operationNodeName.getBytes(), stat.getVersion());

		Thread.sleep(1000);

		this.clientDB = new ClientDB();

		nodeManager.leaderElection();
		nodeManager.listenForJoiningNodes(memberNodeName);
		nodeManager.listenForOperationUpdates(this, this.operationNodeName);

		/*Based on the consistency strategy, operations that change the DB should be performed by the leader
		which means that followers should be able to send such operations that they receive to the leader.
		Therefore, it is useful to know the name of the operation node which belongs to the leader.
		We decided to save it as the data of the election node of the leader.*/
		stat = new Stat();
		zookeeper.setData(this.electionNodeName, this.operationNodeName.getBytes(), stat.getVersion());

		this.sendMessagesBank = new SendMessagesBank(zookeeper, this);
	}

	/**
	 * Responsible for handling the command messages that the bank instance receives.
	 * @param operation The operation that is to be executed.
	 */
	public synchronized void handleIncomingMsg(OperationBank operation) {
		switch (operation.getOperation()) {
			case CREATE_CLIENT:
				clientDB.createClient(operation.getClient());
				break;
			case READ_CLIENT:
				clientDB.readClient(operation.getAccountNumber());
				break;
			case UPDATE_CLIENT:
				clientDB.updateClient(operation.getClient().getAccountNumber(), operation.getClient().getBalance());
				break;
			case DELETE_CLIENT:
				clientDB.deleteClient(operation.getAccountNumber());
				break;
			case CREATE_BANK:
				clientDB.createBank(operation.getClientDB());
				break;
		}
	}

	/**
	 * Responsible for creating a new client.
	 * @param client The client object to be saved.
	 */
	public void createClient(Client client) {
		sendMessagesBank.sendAdd(client, isLeader);
	}

	/**
	 * Responsible for reading the details of a client.
	 * @param accountNumber The account number of the client whose data needs to be returned.
	 * @return Client object.
	 */
	public Client readClient(Integer accountNumber) {
		// Handled locally, since read operations cannot undermine the consistency of the database.
		// Therefore, any server can serve the request and there is no need for distributing the operation.
		return clientDB.readClient(accountNumber);
	}

	/**
	 * Responsible for modifying the data of a given client.
	 * @param accNumber The account number of the client whose data needs to be modified.
	 * @param balance The new balance value that is to be saved.
	 */
	public void updateClient (int accNumber, int balance) {
		Client client = clientDB.readClient(accNumber);
		client.setBalance(balance);
		sendMessagesBank.sendUpdate(client, isLeader);
	}

	/**
	 * Responsible for the deletion of clients.
	 * @param accountNumber The account number of the client who is to be deleted from the DB.
	 */
	public void deleteClient(Integer accountNumber) {
		sendMessagesBank.sendDelete(accountNumber, isLeader);
	}

	/**
	 * Responsible for letting the user know about the end of the session.
	 */
	public void close() {
		System.out.println("Session finished");
	}

	/*
	 * Getters and setters
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
	
	public NodeManager getNodeManager(){
		return nodeManager;
	}

	public String toString() {
		return clientDB.toString();
	}
}
