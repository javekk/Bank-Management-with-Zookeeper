package eu.upm.adic;

import java.io.Serializable;

public class Client implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private int accountNumber;
	private String name;
	private int balance;
	
	public Client (int accountNumber, String name, int balance) {
		this.accountNumber = accountNumber;
		this.name          = name;
		this.balance       = balance;
	}


	/*
	 * GETTER AND SETTER
	 */
	public int getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(int accountNumber) {
		this.accountNumber = accountNumber;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getBalance() {
		return balance;
	}

	public void setBalance(int balance) {
		this.balance = balance;
	}

	@Override
	public String toString() {
		return "[" + accountNumber + ", " + name + ", " + balance + "]";
	}
}
