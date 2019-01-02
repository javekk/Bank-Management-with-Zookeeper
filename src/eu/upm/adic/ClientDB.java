package eu.upm.adic;

import java.io.Serializable;

public class ClientDB implements Serializable {

	private static final long serialVersionUID = 1L;

	public java.util.HashMap <Integer, Client> clientDB;

	public ClientDB() {
		clientDB = new java.util.HashMap<>();
	}

	private java.util.HashMap <Integer, Client> getClientDB() {
		return this.clientDB;
	}
	
	boolean createClient(Client client) {
		if (clientDB.containsKey(client.getAccountNumber())) {
			return false;
		} else {
			clientDB.put(client.getAccountNumber(), client);
			return true;
		}		
	}

	Client readClient(Integer accountNumber) {
		if (clientDB.containsKey(accountNumber)) {
			return clientDB.get(accountNumber);
		} else {
			return null;
		}		
	}

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

	boolean deleteClient(Integer accountNumber) {
		if (clientDB.containsKey(accountNumber)) {
			clientDB.remove(accountNumber);
			return true;
		} else {
			return false;
		}	
	}

	boolean createBank(ClientDB clientDB) {
		this.clientDB = clientDB.getClientDB();
		return true;
	}
	
	public String toString() {
		String aux = "";

		for (java.util.HashMap.Entry <Integer, Client>  entry : clientDB.entrySet()) {
			aux = aux + entry.getValue().toString() + "\n";
		}
		return aux;
	}
}



