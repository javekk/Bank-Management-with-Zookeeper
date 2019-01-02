package eu.upm.adic;

public interface SendMessages {

	public void sendAdd(Client client, boolean isLeader);

	public void sendUpdate(Client client, boolean isLeader);

	public void sendDelete(Integer accountNumber, boolean isLeader);

	public void sendCreateBank (ClientDB clientDB, boolean isLeader);

}
