package eu.upm.adic;


import java.io.Serializable;


/**
 *  This class provides the basic operation to handle client data
 *  Now the database it's just an hashmap, pretty easy
 */

public class ClientDB implements Serializable {

    private static final long serialVersionUID = 1L;


    //Our database is just an hashmap for now....
    private java.util.HashMap <Integer, Client> clientDB;

    public ClientDB (ClientDB clientDB) {
        this.clientDB = clientDB.getClientDB();
    }

    public ClientDB() {
        clientDB = new java.util.HashMap <Integer, Client>();
    }

    public java.util.HashMap <Integer, Client> getClientDB() {
        return this.clientDB;
    }


    public boolean createClient(Client client) {
        if (clientDB.containsKey(client.getAccountNumber())) {
            return false;
        } else {
            clientDB.put(client.getAccountNumber(), client);
            return true;
        }
    }

    public Client readClient(Integer accountNumber) {
        if (clientDB.containsKey(accountNumber)) {
            return clientDB.get(accountNumber);
        } else {
            return null;
        }
    }

    public boolean updateClient (int accNumber, int balance) {
        if (clientDB.containsKey(accNumber)) {
            Client client = clientDB.get(accNumber);
            client.setBalance(balance);
            clientDB.put(client.getAccountNumber(), client);
            return true;
        } else {
            return false;
        }
    }

    public boolean deleteClient(Integer accountNumber) {
        if (clientDB.containsKey(accountNumber)) {
            clientDB.remove(accountNumber);
            return true;
        } else {
            return false;
        }
    }

    public boolean createBank(ClientDB clientDB) {
        System.out.println("createBank");
        this.clientDB = clientDB.getClientDB();
        System.out.println(clientDB.toString());
        return true;
    }

    public String toString() {
        String aux = new String();

        for (java.util.HashMap.Entry <Integer, Client>  entry : clientDB.entrySet()) {
            aux = aux + entry.getValue().toString() + "\n";
        }
        return aux;
    }
}

