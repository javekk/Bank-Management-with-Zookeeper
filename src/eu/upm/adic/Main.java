package eu.upm.adic;

import eu.upm.adic.operation.OperationBank;
import eu.upm.adic.operation.OperationEnum;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.Random;
import java.util.Scanner;



public class Main {

	/*
	 *  Global variables
	 */
	private static final int SESSION_TIMEOUT = 5000;
	private static ZooKeeper zookeeper = null;
	private static Bank bank = null;
	private static String[] hosts = {"138.4.31.95:2181", "138.4.31.96:2182"};

	/**
	 * Entry point of the application. Responsible for initializing the application and providing a menu for users.
	 * @param args Not used.
	 * @throws KeeperException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws KeeperException, InterruptedException {
		//Start the session with a random host
		int i = new Random().nextInt(hosts.length);
		try {
			//init zookeeper
			zookeeper = new ZooKeeper(hosts[i], SESSION_TIMEOUT, null);
		} catch (IOException e) {
			e.printStackTrace();
		}

		//init the bank instance
        bank = new Bank(zookeeper);

		//We fill the bank DB with some dummy data so that it is not completely empty.
		//initDB(bank);

		boolean correct;
		int menuKey;
		boolean exit = false; //exit from the menu
		Scanner sc = new Scanner(System.in); //get input
		int accNumber = 0;
		int balance = 0;
		Client client;

		while (!exit) {
			try {
				correct = false;
				menuKey = 0;
				while (!correct) {
					System. out .println("- Enter client operation: \n 1) Create \n 2) Read\n 3) Update\n 4) Delete\n 5) BankDB\n 6) Exit");
					if (sc.hasNextInt()) {
						menuKey = sc.nextInt();
						correct = true;
					} else {
						sc.next();
						System.out.println("Hey dude that's Not an integer");
					}
				}

				switch (menuKey) {
					case 1: // Create client
						bank.createClient(createClient(sc));
						break;
					case 2: // Read client
						System. out .print("- Enter account number [int]= ");
						if (sc.hasNextInt()) {
							accNumber = sc.nextInt();
							client = bank.readClient(accNumber);
							System.out.println("Client: " + client);
						} else {
							System.out.println("Hey dude that's Not an integer");
							sc.next();
						}
						break;
					case 3: // Update client
						System. out .print("- Enter account number [int]= ");
						if (sc.hasNextInt()) {
							accNumber = sc.nextInt();
						} else {
							System.out.println("Hey dude that's Not an integer");
							sc.next();
						}
						System. out .print("- Enter account number [int]=");
						if (sc.hasNextInt()) {
							balance = sc.nextInt();
						} else {
							System.out.println("Hey dude that's Not an integer");
							sc.next();
						}
						bank.updateClient(accNumber, balance);
						break;
					case 4: // Delete client
						System. out .print("- Enter account number [int]=");
						if (sc.hasNextInt()) {
							accNumber = sc.nextInt();
							bank.deleteClient(accNumber);
						} else {
							System.out.println("Hey dude that's Not an integer");
							sc.next();
						}
						break;
					case 5: // Get bank DB
						System.out.println(bank.toString());
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

	/**
	* Responsible for printing the steps of user creation.
	* @param sc Scanner object to be used to obtain user input.
	*/
	public static Client createClient(Scanner sc) {
		int accNumber = 0;
		String name   = null;
		int balance   = 0;

		System.out.print("- Enter account number [int]= ");
		if (sc.hasNextInt()) {
			accNumber = sc.nextInt();
		} else {
			System.out.println("Hey dude that's Not an integerr");
			sc.next();
			return null;
		}

		System.out.print("-Enter name [String]= ");
		name = sc.next();

		System.out.print("-Enter balance [int]= ");
		if (sc.hasNextInt()) {
			balance = sc.nextInt();
		} else {
			System.out.println("Hey dude that's Not an integer");
			sc.next();
			return null;
		}
		return new Client(accNumber, name, balance);
	}


	/**
	 * Responsible for filling the DB with some dummy data.
	 * @param bank The bank object whose client DB is to be initialized.
	 **/
	public static void initDB(Bank bank) {
		SendMessagesBank messageBank = new SendMessagesBank(zookeeper, bank);
		messageBank.sendMessage(new OperationBank(
				OperationEnum.CREATE_CLIENT, new Client(1, "Angel Alarcón", 100)), bank.getIsLeader());
		messageBank.sendMessage(new OperationBank
				(OperationEnum.CREATE_CLIENT, new Client(2, "Bernardo Bueno", 200)), bank.getIsLeader());
		messageBank.sendMessage(new OperationBank(
				OperationEnum.CREATE_CLIENT, new Client(3, "Carlos Cepeda", 300)), bank.getIsLeader());
		messageBank.sendMessage(new OperationBank(
				OperationEnum.CREATE_CLIENT, new Client(4, "Daniel Díaz", 400)), bank.getIsLeader());
		messageBank.sendMessage(new OperationBank(
				OperationEnum.CREATE_CLIENT, new Client(5, "Eugenio Escobar", 500)), bank.getIsLeader());
		messageBank.sendMessage(new OperationBank(
				OperationEnum.CREATE_CLIENT, new Client(6, "Fernando Ferrero", 600)), bank.getIsLeader());
	}
}

