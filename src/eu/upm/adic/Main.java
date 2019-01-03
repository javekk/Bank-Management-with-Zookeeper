package eu.upm.adic;

import eu.upm.adic.operation.OperationBank;
import eu.upm.adic.operation.OperationEnum;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.Random;
import java.util.Scanner;

public class Main {

	private static final int SESSION_TIMEOUT = 5000;

	public static void main(String[] args) throws KeeperException, InterruptedException {

		ZooKeeper zk = null;
		Bank bank = null;

		String[] hosts = {"127.0.0.1:2181", "127.0.0.1:2182", "127.0.0.1:2183"};

		int i = new Random().nextInt(hosts.length);
		try {
			zk = new ZooKeeper(hosts[i], SESSION_TIMEOUT, null);
		} catch (IOException e) {
			e.printStackTrace();
		}

        bank = new Bank(zk);

        initDB(bank);

		boolean correct = false;
		int     menuKey = 0;
		boolean exit    = false;
		Scanner sc      = new Scanner(System.in);
		int accNumber   = 0;
		int balance     = 0;
		Client client   = null;
		boolean status  = false;

		while (!exit) {
			try {
				correct = false;
				menuKey = 0;
				while (!correct) {
					System. out .println(">>> Enter opn cliente.: 1) Create. 2) Read. 3) Update. 4) Delete. 5) BankDB. 6) Exit");
					if (sc.hasNextInt()) {
						menuKey = sc.nextInt();
						correct = true;
					} else {
						sc.next();
						System.out.println("The text provided is not an integer");
					}
				}

				switch (menuKey) {
					case 1: // Create client
						bank.createClient(createClient(sc));
						break;
					case 2: // Read client
						System. out .print(">>> Enter account number (int) = ");
						if (sc.hasNextInt()) {
							accNumber = sc.nextInt();
							client = bank.readClient(accNumber);
							System.out.println("Client: " + client);
						} else {
							System.out.println("The text provided is not an integer");
							sc.next();
						}
						break;
					case 3: // Update client
						System. out .print(">>> Enter account number (int) = ");
						if (sc.hasNextInt()) {
							accNumber = sc.nextInt();
						} else {
							System.out.println("The text provided is not an integer");
							sc.next();
						}
						System. out .print(">>> Enter balance (int) = ");
						if (sc.hasNextInt()) {
							balance = sc.nextInt();
						} else {
							System.out.println("The text provided is not an integer");
							sc.next();
						}
						bank.updateClient(accNumber, balance);
						break;
					case 4: // Delete client
						System. out .print(">>> Enter account number (int) = ");
						if (sc.hasNextInt()) {
							accNumber = sc.nextInt();
							bank.deleteClient(accNumber);
						} else {
							System.out.println("The text provided is not an integer");
							sc.next();
						}
						break;
					case 5: // Get bank DB
						System.out.println(bank.toString());
//						bank.sendCreateBank();
						break;
					case 6:
						exit = true;
						bank.close();
					default:
						break;
				}
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
		sc.close();
	}

	public static Client createClient(Scanner sc) {
		int accNumber = 0;
		String name   = null;
		int balance   = 0;

		System.out.print(">>> Enter account number (int) = ");
		if (sc.hasNextInt()) {
			accNumber = sc.nextInt();
		} else {
			System.out.println("The provised text provided is not an integer");
			sc.next();
			return null;
		}

		System.out.print(">>> Enter name (String) = ");
		name = sc.next();

		System.out.print(">>> Enter balance (int) = ");
		if (sc.hasNextInt()) {
			balance = sc.nextInt();
		} else {
			System.out.println("The provised text provided is not an integer");
			sc.next();
			return null;
		}
		return new Client(accNumber, name, balance);
	}

	public static void initDB(Bank bank) {

		bank.handleReceiverMsg(new OperationBank(OperationEnum.CREATE_CLIENT, new Client(1, "Angel Alarcón", 100)));
		bank.handleReceiverMsg(new OperationBank(OperationEnum.CREATE_CLIENT, new Client(2, "Bernardo Bueno", 200)));
		bank.handleReceiverMsg(new OperationBank(OperationEnum.CREATE_CLIENT, new Client(3, "Carlos Cepeda", 300)));
		bank.handleReceiverMsg(new OperationBank(OperationEnum.CREATE_CLIENT, new Client(4, "Daniel Díaz", 400)));
		bank.handleReceiverMsg(new OperationBank(OperationEnum.CREATE_CLIENT, new Client(5, "Eugenio Escobar", 500)));
		bank.handleReceiverMsg(new OperationBank(OperationEnum.CREATE_CLIENT, new Client(6, "Fernando Ferrero", 600)));
	}

}

