package eu.upm.adic;

public interface SendMessages {

	void sendAdd(Client client, boolean isLeader);

	void sendUpdate(Client client, boolean isLeader);

	void sendDelete(Integer accountNumber, boolean isLeader);

}
