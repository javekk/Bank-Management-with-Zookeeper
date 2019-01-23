package eu.upm.adic;

import java.io.Serializable;

public class ClientDB implements Serializable {
	public java.util.HashMap <Integer, Client> clientDB;

	public ClientDB() {
		clientDB = new java.util.HashMap<>();
	}

	private java.util.HashMap <Integer, Client> getClientDB() {
		return this.clientDB;
	}

	/**
	 * Responsible for the creation of client and it also checks
	 * if a given client already exists or not.
	 * @param client The client object that is to be saved.
	 * @return True: the client is successfully saved. False: the client already exists.
	 */
	boolean createClient(Client client) {
		if (clientDB.containsKey(client.getAccountNumber())) {
			return false;
		} else {
			clientDB.put(client.getAccountNumber(), client);
			return true;
		}		
	}

	/**
	 * Responsible for reading the informations of a given client.
	 * @param accountNumber The primary key of the client whose details are to be returned.
	 * @return Client object if the client exists, null otherwise.
	 */
	Client readClient(Integer accountNumber) {
		if (clientDB.containsKey(accountNumber)) {
			return clientDB.get(accountNumber);
		} else {
			return null;
		}		
	}

	/**
	 * Responsible for modifying the data of clients.
	 * @param accNumber The account number of client whose data is to be modified.
	 * @param balance The new balance value to be saved.
	 * @return True: the data is successfully updated. False: the client does not exist.
	 */
	boolean updateClient(int accNumber, int balance) {
		if (clientDB.containsKey(accNumber)) {
			Client client = clientDB.get(accNumber);
			client.setBalance(balance);
			clientDB.put(client.getAccountNumber(), client);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Responsible for deleting a client.
	 * @param accountNumber The account number of ths client who is to be deleted from the DB.
	 * @return True: the client is successfully deleted. False: the client did not exist.
	 */
	boolean deleteClient(Integer accountNumber) {
		if (clientDB.containsKey(accountNumber)) {
			clientDB.remove(accountNumber);
			return true;
		} else {
			return false;
		}	
	}

	/**
	 * Create a client DB based on another ClientDB instance.
	 * @param clientDB The ClientDB instanced that should be used when creating a new instance.
	 * @return True: success. False: failure.
	 */
	boolean createBank(ClientDB clientDB) {
		if(clientDB.getClientDB() != null){
			this.clientDB = clientDB.getClientDB();
			return true;
		}
		return false;
	}

	/**
	 * Responsible for providing a String representation of the client DB.
	 * @return String representation.
	 */
	public String toString() {
		String aux = "";

		for (java.util.HashMap.Entry <Integer, Client>  entry : clientDB.entrySet()) {
			aux = aux + entry.getValue().toString() + "\n";
		}
		return aux;
	}
}



