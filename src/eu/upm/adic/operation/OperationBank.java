package eu.upm.adic.operation;

import eu.upm.adic.Client;
import eu.upm.adic.ClientDB;

import java.io.*;

public class OperationBank implements Serializable {

	private static final long serialVersionUID = 1L;
	private OperationEnum operation;
	private Client client = null;
	private Integer accountNumber = 0;
	private ClientDB clientDB = null;
	
   // ADD_CLIENT, UPDATE_CLIENT
	public OperationBank (OperationEnum operation, Client client) {
		this.operation = operation;
		this.client    = client;
	}
	
	// READ_CLIENT, DELETE_CLIENT
	public OperationBank (OperationEnum operation, Integer accountNumber ) {
		this.operation     = operation;
		this.accountNumber = accountNumber;
	}

	public OperationBank (OperationEnum operation, ClientDB clientDB) {
		this.operation = operation;
		this.clientDB  = clientDB;
	}

	
	public OperationEnum getOperation() {
		return operation;
	}

	public void setOperation(OperationEnum operation) {
		this.operation = operation;
	}

	public Client getClient() {
		return client;
	}

	public void setClient(Client client) {
		this.client = client;
	}

	public Integer getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(Integer accountNumber) {
		this.accountNumber = accountNumber;
	}
	
	public ClientDB getClientDB() {
		return clientDB;
	}

	public void setClientDB(ClientDB clientDB) {
		this.clientDB = clientDB;
	}


	/**
	 * @TODO
	 *
	 * 				ARE THESE METHODS BELOW REALLY NEEDED?
	 * @return
	 */
	@Override
	public String toString() {
		
		String string = "OperationBank [operation=" + operation;
		if (client != null) string = string + ", client=" + client.toString();
		string = string + ", accountNumber=" + accountNumber + "]\n";
		if (clientDB != null) string = string + clientDB.toString();
		System.out.println("toString: " + string);
		
		return string;
	}

	public static byte[] objToByte(OperationBank operationBank) throws IOException {
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		ObjectOutputStream objStream = new ObjectOutputStream(byteStream);
		objStream.writeObject(operationBank);

		return byteStream.toByteArray();
	}

	public static OperationBank byteToObj(byte[] bytes) throws IOException, ClassNotFoundException {
		ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
		ObjectInputStream objStream = new ObjectInputStream(byteStream);

		return (OperationBank) objStream.readObject();
	}
}
